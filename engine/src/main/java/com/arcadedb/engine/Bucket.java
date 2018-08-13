/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.engine;

import com.arcadedb.database.*;
import com.arcadedb.exception.DatabaseOperationException;
import com.arcadedb.exception.RecordNotFoundException;
import com.arcadedb.utility.FileUtils;
import com.arcadedb.utility.LogManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.arcadedb.database.Binary.INT_SERIALIZED_SIZE;
import static com.arcadedb.database.Binary.LONG_SERIALIZED_SIZE;

/**
 * PAGE CONTENT = [version(long:8),recordCountInPage(short:2),recordOffsetsInPage(2048*uint=8192)]
 * <p>
 * Record size is the length of the record or -1 if a placeholder is stored and -2 for the placeholder itself.
 */
public class Bucket extends PaginatedComponent {
  public static final String BUCKET_EXT          = "bucket";
  public static final int    MAX_RECORDS_IN_PAGE = 2048;
  public static final int    DEF_PAGE_SIZE       = 65536;

  protected static final int PAGE_RECORD_COUNT_IN_PAGE_OFFSET = 0;
  protected static final int PAGE_RECORD_TABLE_OFFSET         = PAGE_RECORD_COUNT_IN_PAGE_OFFSET + Binary.SHORT_SERIALIZED_SIZE;
  protected static final int CONTENT_HEADER_SIZE              = PAGE_RECORD_TABLE_OFFSET + (MAX_RECORDS_IN_PAGE * INT_SERIALIZED_SIZE);

  public static class PaginatedComponentFactoryHandler implements PaginatedComponentFactory.PaginatedComponentFactoryHandler {
    @Override
    public PaginatedComponent create(final Database database, final String name, final String filePath, final int id, final PaginatedFile.MODE mode,
        final int pageSize) throws IOException {
      return new Bucket(database, name, filePath, id, mode, pageSize);
    }
  }

  /**
   * Called at creation time.
   */
  public Bucket(final Database database, final String name, final String filePath, final PaginatedFile.MODE mode, final int pageSize) throws IOException {
    super(database, name, filePath, database.getFileManager().newFileId(), BUCKET_EXT, mode, pageSize);
  }

  /**
   * Called at load time.
   */
  public Bucket(final Database database, final String name, final String filePath, final int id, final PaginatedFile.MODE mode, final int pageSize)
      throws IOException {
    super(database, name, filePath, id, mode, pageSize);
  }

  public RID createRecord(final Record record) {
    return createRecordInternal(record, false);
  }

  public void updateRecord(final Record record) {
    updateRecordInternal(record, record.getIdentity(), false);
  }

  public Binary getRecord(final RID rid) {
    return getRecordInternal(rid, false);
  }

  public boolean existsRecord(final RID rid) {
    final int pageId = (int) rid.getPosition() / Bucket.MAX_RECORDS_IN_PAGE;
    final int positionInPage = (int) (rid.getPosition() % Bucket.MAX_RECORDS_IN_PAGE);

    if (pageId >= pageCount.get()) {
      int txPageCount = getTotalPages();
      if (pageId >= txPageCount)
        return false;
    }

    try {
      final BasePage page = database.getTransaction().getPage(new PageId(file.getFileId(), pageId), pageSize);

      final short recordCountInPage = page.readShort(PAGE_RECORD_COUNT_IN_PAGE_OFFSET);
      if (positionInPage >= recordCountInPage)
        return false;

      final int recordPositionInPage = (int) page.readUnsignedInt(PAGE_RECORD_TABLE_OFFSET + positionInPage * INT_SERIALIZED_SIZE);
      final long recordSize[] = page.readNumberAndSize(recordPositionInPage);

      return recordSize[0] > 0 || recordSize[0] == -1;

    } catch (IOException e) {
      throw new DatabaseOperationException("Error on checking record existence for " + rid);
    }
  }

  public void deleteRecord(final RID rid) {
    deleteRecordInternal(rid, false);
  }

  public void scan(final RawRecordCallback callback) {
    final int txPageCount = getTotalPages();

    try {
      for (int pageId = 0; pageId < txPageCount; ++pageId) {
        final BasePage page = database.getTransaction().getPage(new PageId(file.getFileId(), pageId), pageSize);
        final short recordCountInPage = page.readShort(PAGE_RECORD_COUNT_IN_PAGE_OFFSET);

        if (recordCountInPage > 0) {
          for (int recordIdInPage = 0; recordIdInPage < recordCountInPage; ++recordIdInPage) {
            final int recordPositionInPage = (int) page.readUnsignedInt(PAGE_RECORD_TABLE_OFFSET + recordIdInPage * INT_SERIALIZED_SIZE);
            final long recordSize[] = page.readNumberAndSize(recordPositionInPage);

            if (recordSize[0] > 0) {
              // NOT DELETED
              final int recordContentPositionInPage = recordPositionInPage + (int) recordSize[1];

              final RID rid = new RID(database, id, pageId * MAX_RECORDS_IN_PAGE + recordIdInPage);

              final Binary view = page.getImmutableView(recordContentPositionInPage, (int) recordSize[0]);

              if (!callback.onRecord(rid, view))
                return;

            } else if (recordSize[0] == -1) {
              // PLACEHOLDER
              final RID rid = new RID(database, id, pageId * MAX_RECORDS_IN_PAGE + recordIdInPage);

              final Binary view = getRecordInternal(new RID(database, id, page.readLong((int) (recordPositionInPage + recordSize[1]))), true);

              if (!callback.onRecord(rid, view))
                return;
            }
          }
        }
      }
    } catch (IOException e) {
      throw new DatabaseOperationException("Cannot scan bucket '" + name + "'", e);
    }
  }

  public Iterator<Record> iterator() throws IOException {
    return new BucketIterator(this, database);
  }

  public int getId() {
    return id;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Bucket))
      return false;

    return ((Bucket) obj).id == this.id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  public long count() {
    long total = 0;

    final int txPageCount = getTotalPages();

    try {
      for (int pageId = 0; pageId < txPageCount; ++pageId) {
        final BasePage page = database.getTransaction().getPage(new PageId(file.getFileId(), pageId), pageSize);
        final short recordCountInPage = page.readShort(PAGE_RECORD_COUNT_IN_PAGE_OFFSET);

        if (recordCountInPage > 0) {
          for (int recordIdInPage = 0; recordIdInPage < recordCountInPage; ++recordIdInPage) {
            final int recordPositionInPage = (int) page.readUnsignedInt(PAGE_RECORD_TABLE_OFFSET + recordIdInPage * INT_SERIALIZED_SIZE);
            final long recordSize[] = page.readNumberAndSize(recordPositionInPage);

            if (recordSize[0] > 0 || recordSize[0] == -1)
              total++;

          }
        }
      }
    } catch (IOException e) {
      throw new DatabaseOperationException("Cannot count bucket '" + name + "'", e);
    }
    return total;
  }

  protected Map<String, Long> check() {
    final Map<String, Long> stats = new HashMap<>();

    final int totalPages = getTotalPages();

    LogManager.instance()
        .info(this, "- Checking bucket '%s' (totalPages=%d spaceOnDisk=%s pageSize=%s)...", name, totalPages, FileUtils.getSizeAsString(totalPages * pageSize),
            FileUtils.getSizeAsString(pageSize));

    long totalRecords = 0;
    long totalActiveRecords = 0;
    long totalPlaceholderRecords = 0;
    long totalSurrogateRecords = 0;
    long totalDeletedRecords = 0;
    long totalMaxOffset = 0;

    for (int pageId = 0; pageId < totalPages; ++pageId) {
      try {
        final BasePage page = database.getTransaction().getPage(new PageId(file.getFileId(), pageId), pageSize);
        final short recordCountInPage = page.readShort(PAGE_RECORD_COUNT_IN_PAGE_OFFSET);

        int pageActiveRecords = 0;
        int pagePlaceholderRecords = 0;
        int pageSurrogateRecords = 0;
        int pageDeletedRecords = 0;
        int pageMaxOffset = 0;

        for (int positionInPage = 0; positionInPage < recordCountInPage; ++positionInPage) {
          final int recordPositionInPage = (int) page.readUnsignedInt(PAGE_RECORD_TABLE_OFFSET + positionInPage * INT_SERIALIZED_SIZE);
          final long recordSize[] = page.readNumberAndSize(recordPositionInPage);

          totalRecords++;

          if (recordSize[0] == 0) {
            pageDeletedRecords++;
            totalDeletedRecords++;
          } else if (recordSize[0] < -1) {
            pageSurrogateRecords++;
            totalSurrogateRecords++;
            recordSize[0] *= -1;
          } else if (recordSize[0] == -1) {
            pagePlaceholderRecords++;
            totalPlaceholderRecords++;
            recordSize[0] = 5;
          } else {
            pageActiveRecords++;
            totalActiveRecords++;
          }

          if (recordPositionInPage + recordSize[1] + recordSize[0] > pageMaxOffset)
            pageMaxOffset = (int) (recordPositionInPage + recordSize[1] + recordSize[0]);
        }

        totalMaxOffset += pageMaxOffset;

        LogManager.instance().debug(this, "-- Page %d records=%d (actives=%d deleted=%d placeholders=%d surrogates=%d) maxOffset=%d", pageId, recordCountInPage,
            pageActiveRecords, pageDeletedRecords, pagePlaceholderRecords, pageSurrogateRecords, pageMaxOffset);

      } catch (IOException e) {
        LogManager.instance().info(this, "- Unknown error on checking page %d: %s", pageId, e.toString());
      }
    }

    final float avgPageUsed = totalPages > 0 ? (float) (totalMaxOffset / totalPages) * 100f / pageSize : 0;

    LogManager.instance()
        .info(this, "-- Total records=%d (actives=%d deleted=%d placeholders=%d surrogates=%d) avgPageUsed=%.2f%%", totalRecords, totalActiveRecords,
            totalDeletedRecords, totalPlaceholderRecords, totalSurrogateRecords, avgPageUsed);

    stats.put("pageSize", (long) pageSize);
    stats.put("totalRecords", totalRecords);
    stats.put("totalPages", (long) totalPages);
    stats.put("totalActiveRecords", totalActiveRecords);
    stats.put("totalPlaceholderRecords", totalPlaceholderRecords);
    stats.put("totalSurrogateRecords", totalSurrogateRecords);
    stats.put("totalDeletedRecords", totalDeletedRecords);
    stats.put("totalMaxOffset", totalMaxOffset);

    return stats;
  }

  private Binary getRecordInternal(final RID rid, final boolean readPlaceHolder) {
    final int pageId = (int) rid.getPosition() / Bucket.MAX_RECORDS_IN_PAGE;
    final int positionInPage = (int) (rid.getPosition() % Bucket.MAX_RECORDS_IN_PAGE);

    if (pageId >= pageCount.get()) {
      int txPageCount = getTotalPages();
      if (pageId >= txPageCount)
        throw new RecordNotFoundException("Record " + rid + " not found", rid);
    }

    try {
      final BasePage page = database.getTransaction().getPage(new PageId(file.getFileId(), pageId), pageSize);

      final short recordCountInPage = page.readShort(PAGE_RECORD_COUNT_IN_PAGE_OFFSET);
      if (positionInPage >= recordCountInPage)
        throw new RecordNotFoundException("Record " + rid + " not found", rid);

      final int recordPositionInPage = (int) page.readUnsignedInt(PAGE_RECORD_TABLE_OFFSET + positionInPage * INT_SERIALIZED_SIZE);
      final long recordSize[] = page.readNumberAndSize(recordPositionInPage);

      if (recordSize[0] == 0)
        // DELETED
        throw new RecordNotFoundException("Record " + rid + " not found", rid);

      if (recordSize[0] < -1) {
        if (!readPlaceHolder)
          // PLACEHOLDER
          throw new RecordNotFoundException("Record " + rid + " not found", rid);

        recordSize[0] *= -1;
      }

      if (recordSize[0] == -1) {
        // FOUND PLACEHOLDER, LOAD THE REAL RECORD
        return getRecord(new RID(database, rid.getBucketId(), page.readLong((int) (recordPositionInPage + recordSize[1]))));
      }

      final int recordContentPositionInPage = (int) (recordPositionInPage + recordSize[1]);

      return page.getImmutableView(recordContentPositionInPage, (int) recordSize[0]);

    } catch (IOException e) {
      throw new DatabaseOperationException("Error on lookup of record " + rid);
    }
  }

  private RID createRecordInternal(final Record record, final boolean isPlaceHolder) {
    final Binary buffer = database.getSerializer().serialize(database, record);

    if (buffer.size() > pageSize - CONTENT_HEADER_SIZE)
      // TODO: SUPPORT MULTI-PAGE CONTENT
      throw new DatabaseOperationException("Record too big to be stored, size=" + buffer.size());

    // RECORD SIZE CANNOT BE < 5 BYTES IN CASE OF UPDATE AND PLACEHOLDER, 5 BYTES IS THE SPACE REQUIRED TO HOST THE PLACEHOLDER
    while (buffer.size() < 5)
      buffer.putByte(buffer.size() - 1, (byte) 0);

    try {
      int newPosition = -1;
      ModifiablePage lastPage = null;
      int recordCountInPage = -1;
      boolean createNewPage = false;

      final int txPageCounter = getTotalPages();

      if (txPageCounter > 0) {
        lastPage = database.getTransaction().getPageToModify(new PageId(file.getFileId(), txPageCounter - 1), pageSize, false);
        recordCountInPage = lastPage.readShort(PAGE_RECORD_COUNT_IN_PAGE_OFFSET);
        if (recordCountInPage >= MAX_RECORDS_IN_PAGE)
          // RECORD TOO BIG FOR THIS PAGE, USE A NEW PAGE
          createNewPage = true;
        else if (recordCountInPage > 0) {
          // GET FIRST EMPTY POSITION
          final int lastRecordPositionInPage = (int) lastPage.readUnsignedInt(PAGE_RECORD_TABLE_OFFSET + (recordCountInPage - 1) * INT_SERIALIZED_SIZE);
          final long[] lastRecordSize = lastPage.readNumberAndSize(lastRecordPositionInPage);

          if (lastRecordSize[0] > 0)
            newPosition = lastRecordPositionInPage + (int) lastRecordSize[0] + (int) lastRecordSize[1];
          else if (lastRecordSize[0] == -1)
            newPosition = lastRecordPositionInPage + LONG_SERIALIZED_SIZE + 1;
          else
            newPosition = lastRecordPositionInPage + (int) (-1 * lastRecordSize[0]) + (int) lastRecordSize[1];

          if (newPosition + INT_SERIALIZED_SIZE + buffer.size() > lastPage.getMaxContentSize())
            // RECORD TOO BIG FOR THIS PAGE, USE A NEW PAGE
            createNewPage = true;

        } else
          // FIRST RECORD, START RIGHT AFTER THE HEADER
          newPosition = CONTENT_HEADER_SIZE;
      } else
        createNewPage = true;

      if (createNewPage) {
        lastPage = database.getTransaction().addPage(new PageId(file.getFileId(), txPageCounter), pageSize);
        //lastPage.blank(0, CONTENT_HEADER_SIZE);
        newPosition = CONTENT_HEADER_SIZE;
        recordCountInPage = 0;
      }

      final RID rid = new RID(database, file.getFileId(), lastPage.getPageId().getPageNumber() * MAX_RECORDS_IN_PAGE + recordCountInPage);

      final byte[] array = buffer.toByteArray();

      final int byteWritten = lastPage.writeNumber(newPosition, isPlaceHolder ? (-1 * array.length) : array.length);
      lastPage.writeByteArray(newPosition + byteWritten, array);

      lastPage.writeUnsignedInt(PAGE_RECORD_TABLE_OFFSET + recordCountInPage * INT_SERIALIZED_SIZE, newPosition);

      lastPage.writeShort(PAGE_RECORD_COUNT_IN_PAGE_OFFSET, (short) ++recordCountInPage);

      LogManager.instance().debug(this, "Created record %s (page=%s records=%d threadId=%d)", rid, lastPage, recordCountInPage, Thread.currentThread().getId());

      ((RecordInternal) record).setBuffer(buffer.copy());

      return rid;

    } catch (IOException e) {
      throw new DatabaseOperationException("Cannot add a new record to the bucket '" + name + "'", e);
    }
  }

  private boolean updateRecordInternal(final Record record, final RID rid, final boolean updatePlaceholder) {
    final Binary buffer = database.getSerializer().serialize(database, record);

    final int pageId = (int) rid.getPosition() / Bucket.MAX_RECORDS_IN_PAGE;
    final int positionInPage = (int) (rid.getPosition() % Bucket.MAX_RECORDS_IN_PAGE);

    if (pageId >= pageCount.get()) {
      int txPageCount = getTotalPages();
      if (pageId >= txPageCount)
        throw new RecordNotFoundException("Record " + rid + " not found", rid);
    }

    try {
      final ModifiablePage page = database.getTransaction().getPageToModify(new PageId(file.getFileId(), pageId), pageSize, false);
      final short recordCountInPage = page.readShort(PAGE_RECORD_COUNT_IN_PAGE_OFFSET);
      if (positionInPage >= recordCountInPage)
        throw new RecordNotFoundException("Record " + rid + " not found", rid);

      int recordPositionInPage = (int) page.readUnsignedInt(PAGE_RECORD_TABLE_OFFSET + positionInPage * INT_SERIALIZED_SIZE);
      final long recordSize[] = page.readNumberAndSize(recordPositionInPage);
      if (recordSize[0] == 0)
        // DELETED
        throw new RecordNotFoundException("Record " + rid + " not found", rid);

      boolean isPlaceHolder = false;
      if (recordSize[0] < -1) {
        if (!updatePlaceholder)
          throw new RecordNotFoundException("Record " + rid + " not found", rid);

        isPlaceHolder = true;
        recordSize[0] *= -1;

      } else if (recordSize[0] == -1) {

        // FOUND A RECORD POINTED FROM A PLACEHOLDER
        final RID placeHolderRID = new RID(database, id, page.readLong((int) (recordPositionInPage + recordSize[1])));
        if (updateRecordInternal(record, placeHolderRID, true))
          return true;

        // DELETE OLD PLACEHOLDER
        deleteRecordInternal(placeHolderRID, true);

        recordSize[0] = LONG_SERIALIZED_SIZE;
        recordSize[1] = 1;
      }

      if (buffer.size() > recordSize[0]) {
        // MAKE ROOM IN THE PAGE IF POSSIBLE

        final int lastRecordPositionInPage = (int) page.readUnsignedInt(PAGE_RECORD_TABLE_OFFSET + (recordCountInPage - 1) * INT_SERIALIZED_SIZE);
        final long lastRecordSize[] = page.readNumberAndSize(lastRecordPositionInPage);

        if (lastRecordSize[0] == -1) {
          lastRecordSize[0] = LONG_SERIALIZED_SIZE;
          lastRecordSize[1] = 1;
        } else if (lastRecordSize[0] < -1) {
          lastRecordSize[0] *= -1;
        }

        final int pageOccupied = (int) (lastRecordPositionInPage + lastRecordSize[0] + lastRecordSize[1]);

        final int bufferSizeLength = Binary.getNumberSpace(isPlaceHolder ? -1 * buffer.size() : buffer.size());

        final int delta = (int) (buffer.size() + bufferSizeLength - recordSize[0] - recordSize[1]);

        if (page.getMaxContentSize() - pageOccupied > delta) {
          // THERE IS SPACE LEFT IN THE PAGE, SHIFT ON THE RIGHT THE EXISTENT RECORDS

          if (positionInPage < recordCountInPage - 1) {
            // NOT LAST RECORD IN PAGE, SHIFT NEXT RECORDS
            final int nextRecordPositionInPage = (int) page.readUnsignedInt(PAGE_RECORD_TABLE_OFFSET + (positionInPage + 1) * INT_SERIALIZED_SIZE);

            final int newPos = nextRecordPositionInPage + delta;

            page.move(nextRecordPositionInPage, newPos, pageOccupied - nextRecordPositionInPage);

            // TODO: CALCULATE THE REAL SIZE TO COMPACT DELETED RECORDS/PLACEHOLDERS
            for (int pos = positionInPage + 1; pos < recordCountInPage; ++pos) {
              final int nextRecordPosInPage = (int) page.readUnsignedInt(PAGE_RECORD_TABLE_OFFSET + pos * INT_SERIALIZED_SIZE);

              if (nextRecordPosInPage == 0)
                page.writeUnsignedInt(PAGE_RECORD_TABLE_OFFSET + pos * INT_SERIALIZED_SIZE, 0);
              else
                page.writeUnsignedInt(PAGE_RECORD_TABLE_OFFSET + pos * INT_SERIALIZED_SIZE, nextRecordPosInPage + delta);

              assert nextRecordPosInPage + delta < page.getMaxContentSize();
            }
          }

          recordSize[1] = page.writeNumber(recordPositionInPage, isPlaceHolder ? -1 * buffer.size() : buffer.size());
          final int recordContentPositionInPage = (int) (recordPositionInPage + recordSize[1]);

          page.writeByteArray(recordContentPositionInPage, buffer.toByteArray());

          LogManager.instance()
              .debug(this, "Updated record %s by allocating new space on the same page (page=%s threadId=%d)", rid, page, Thread.currentThread().getId());

        } else {
          if (isPlaceHolder)
            // CANNOT CREATE A PLACEHOLDER OF PLACEHOLDER
            return false;

          // STORE THE RECORD SOMEWHERE ELSE AND CREATE HERE A PLACEHOLDER THAT POINTS TO THE NEW POSITION. IN THIS WAY THE RID IS PRESERVED
          final RID realRID = createRecordInternal(record, true);

          final int bytesWritten = page.writeNumber(recordPositionInPage, -1);
          page.writeLong(recordPositionInPage + bytesWritten, realRID.getPosition());
          LogManager.instance()
              .debug(this, "Updated record %s by allocating new space with a placeholder (page=%s threadId=%d)", rid, page, Thread.currentThread().getId());
        }
      } else {

        recordSize[1] = page.writeNumber(recordPositionInPage, isPlaceHolder ? -1 * buffer.size() : buffer.size());
        final int recordContentPositionInPage = (int) (recordPositionInPage + recordSize[1]);
        page.writeByteArray(recordContentPositionInPage, buffer.toByteArray());

        LogManager.instance()
            .debug(this, "Updated record %s with the same size or less as before (page=%s threadId=%d)", rid, page, Thread.currentThread().getId());

      }

      ((RecordInternal) record).setBuffer(buffer.copy());

      return true;

    } catch (IOException e) {
      throw new DatabaseOperationException("Error on update record " + rid);
    }
  }

  private void deleteRecordInternal(final RID rid, final boolean deletePlaceholder) {

    final int pageId = (int) rid.getPosition() / Bucket.MAX_RECORDS_IN_PAGE;
    final int positionInPage = (int) (rid.getPosition() % Bucket.MAX_RECORDS_IN_PAGE);

    if (pageId >= pageCount.get()) {
      int txPageCount = getTotalPages();
      if (pageId >= txPageCount)
        throw new RecordNotFoundException("Record " + rid + " not found", rid);
    }

    try {
      final ModifiablePage page = database.getTransaction().getPageToModify(new PageId(file.getFileId(), pageId), pageSize, false);
      final short recordCountInPage = page.readShort(PAGE_RECORD_COUNT_IN_PAGE_OFFSET);
      if (positionInPage >= recordCountInPage)
        throw new RecordNotFoundException("Record " + rid + " not found", rid);

      int recordPositionInPage = (int) page.readUnsignedInt(PAGE_RECORD_TABLE_OFFSET + positionInPage * INT_SERIALIZED_SIZE);
      final long[] removedRecordSize = page.readNumberAndSize(recordPositionInPage);
      if (removedRecordSize[0] == 0)
        // ALREADY DELETED
        throw new RecordNotFoundException("Record " + rid + " not found", rid);

      if (removedRecordSize[0] < -1) {
        if (!deletePlaceholder)
          // CANNOT DELETE A PLACEHOLDER DIRECTLY
          throw new RecordNotFoundException("Record " + rid + " not found", rid);
        removedRecordSize[0] *= -1;
      }

      // CONTENT SIZE = 0 MEANS DELETED
      page.writeNumber(recordPositionInPage, 0);

//      recordPositionInPage++;
//
//      AVOID COMPACTION DURING DELETE
//      // COMPACT PAGE BY SHIFTING THE RECORDS TO THE LEFT
//      for (int pos = positionInPage + 1; pos < recordCountInPage; ++pos) {
//        final int nextRecordPosInPage = (int) page.readUnsignedInt(PAGE_RECORD_TABLE_OFFSET + pos * INT_SERIALIZED_SIZE);
//        final byte[] record = page.readBytes(nextRecordPosInPage);
//
//        final int bytesWritten = page.writeBytes(recordPositionInPage, record);
//
//        // OVERWRITE POS TABLE WITH NEW POSITION
//        page.writeUnsignedInt(PAGE_RECORD_TABLE_OFFSET + pos * INT_SERIALIZED_SIZE, recordPositionInPage);
//
//        recordPositionInPage += bytesWritten;
//      }
//
//      page.writeShort(PAGE_RECORD_COUNT_IN_PAGE_OFFSET, (short) (recordCountInPage - 1));

      LogManager.instance().debug(this, "Deleted record %s (page=%s threadId=%d)", rid, page, Thread.currentThread().getId());

    } catch (IOException e) {
      throw new DatabaseOperationException("Error on deletion of record " + rid);
    }
  }
}
