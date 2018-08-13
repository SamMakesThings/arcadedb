/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb;

import com.arcadedb.database.*;
import com.arcadedb.exception.DuplicatedKeyException;
import com.arcadedb.exception.NeedRetryException;
import com.arcadedb.index.Index;
import com.arcadedb.index.IndexCursor;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.SchemaImpl;
import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultSet;
import com.arcadedb.utility.LogManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class LSMTreeIndexTest extends BaseTest {
  private static final int    TOT       = 10000;
  private static final String TYPE_NAME = "V";

  @Test
  public void testGet() {
    database.transaction(new Database.Transaction() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final Index[] indexes = database.getSchema().getIndexes();

        for (int i = 0; i < TOT; ++i) {
          final List<Integer> results = new ArrayList<>();
          for (Index index : indexes) {
            final Set<RID> value = index.get(new Object[] { i });
            if (!value.isEmpty())
              results.add((Integer) ((Document) value.iterator().next().getRecord()).get("id"));
          }

          total++;
          Assertions.assertEquals(1, results.size());
          Assertions.assertEquals(i, (int) results.get(0));
        }

        Assertions.assertEquals(TOT, total);
      }
    });
  }

  @Test
  public void testRemoveKeys() {
    database.transaction(new Database.Transaction() {
      @Override
      public void execute(Database database) {
        int total = 0;

        final Index[] indexes = database.getSchema().getIndexes();

        for (int i = 0; i < TOT; ++i) {
          int found = 0;

          final Object[] key = new Object[] { i };

          for (Index index : indexes) {
            final Set<RID> value = index.get(key);
            if (!value.isEmpty()) {
              index.remove(key);
              found++;
              total++;
            }
          }

          Assertions.assertEquals(1, found, "Key '" + Arrays.toString(key) + "' found " + found + " times");
        }

        Assertions.assertEquals(TOT, total);

        // GET EACH ITEM TO CHECK IT HAS BEEN DELETED
        for (int i = 0; i < TOT; ++i) {
          for (Index index : indexes)
            Assertions.assertTrue(index.get(new Object[] { i }).isEmpty(), "Found item with key " + i);
        }
      }
    });
  }

  @Test
  public void testRemoveEntries() {
    database.transaction(new Database.Transaction() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final Index[] indexes = database.getSchema().getIndexes();

        for (int i = 0; i < TOT; ++i) {
          int found = 0;

          final Object[] key = new Object[] { i };

          for (Index index : indexes) {
            final Set<RID> value = index.get(key);
            if (!value.isEmpty()) {
              for (RID r : value)
                index.remove(key, r);
              found++;
              total++;
            }
          }

          Assertions.assertEquals(1, found, "Key '" + Arrays.toString(key) + "' found " + found + " times");
        }

        Assertions.assertEquals(TOT, total);

        // GET EACH ITEM TO CHECK IT HAS BEEN DELETED
        for (int i = 0; i < TOT; ++i) {
          for (Index index : indexes)
            Assertions.assertTrue(index.get(new Object[] { i }).isEmpty(), "Found item with key " + i);
        }

      }
    });
  }

  @Test
  public void testRemoveEntriesMultipleTimes() {
    database.transaction(new Database.Transaction() {
      @Override
      public void execute(Database database) {
        int total = 0;

        final Index[] indexes = database.getSchema().getIndexes();

        for (int i = 0; i < TOT; ++i) {
          int found = 0;

          final Object[] key = new Object[] { i };

          for (Index index : indexes) {
            final Set<RID> value = index.get(key);
            if (!value.isEmpty()) {
              for (RID r : value) {
                for (int k = 0; k < 10; ++k)
                  index.remove(key, r);
              }
              found++;
              total++;
            }
          }

          Assertions.assertEquals(1, found, "Key '" + Arrays.toString(key) + "' found " + found + " times");
        }

        Assertions.assertEquals(TOT, total);

        // GET EACH ITEM TO CHECK IT HAS BEEN DELETED
        for (int i = 0; i < TOT; ++i) {
          for (Index index : indexes)
            Assertions.assertTrue(index.get(new Object[] { i }).isEmpty(), "Found item with key " + i);
        }
      }
    });
  }

  @Test
  public void testRemoveAndPutEntries() {
    database.transaction(new Database.Transaction() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final Index[] indexes = database.getSchema().getIndexes();

        for (int i = 0; i < TOT; ++i) {
          int found = 0;

          final Object[] key = new Object[] { i };

          for (Index index : indexes) {
            final Set<RID> value = index.get(key);
            if (!value.isEmpty()) {
              for (RID r : value) {
                index.remove(key, r);
                index.put(key, r);
                index.remove(key, r);
              }
              found++;
              total++;
            }
          }

          Assertions.assertEquals(1, found, "Key '" + Arrays.toString(key) + "' found " + found + " times");
        }

        Assertions.assertEquals(TOT, total);

        // GET EACH ITEM TO CHECK IT HAS BEEN DELETED
        for (int i = 0; i < TOT; ++i) {
          for (Index index : indexes)
            Assertions.assertTrue(index.get(new Object[] { i }).isEmpty(), "Found item with key " + i);
        }

      }
    });
  }

  @Test
  public void testUpdateKeys() {
    database.transaction(new Database.Transaction() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final ResultSet resultSet = database.query("sql", "select from " + TYPE_NAME);
        for (ResultSet it = resultSet; it.hasNext(); ) {
          final Result r = it.next();

          final ModifiableDocument record = (ModifiableDocument) r.getElement().get().modify();
          record.set("id", (Integer) record.get("id") + 1000000);
          record.save();
        }

        database.commit();
        database.begin();

        final Index[] indexes = database.getSchema().getIndexes();

        // ORIGINAL KEYS SHOULD BE REMOVED
        for (int i = 0; i < TOT; ++i) {
          int found = 0;

          final Object[] key = new Object[] { i };

          for (Index index : indexes) {
            final Set<RID> value = index.get(key);
            if (!value.isEmpty()) {
              found++;
              total++;
            }
          }

          Assertions.assertEquals(0, found, "Key '" + Arrays.toString(key) + "' found " + found + " times");
        }

        Assertions.assertEquals(0, total);

        total = 0;

        // CHECK FOR NEW KEYS
        for (int i = 1000000; i < 1000000 + TOT; ++i) {
          int found = 0;

          final Object[] key = new Object[] { i };

          for (Index index : indexes) {
            final Set<RID> value = index.get(key);
            if (!value.isEmpty()) {
              for (RID r : value) {
                index.remove(key, r);
                found++;
              }
              total++;
            }
          }

          Assertions.assertEquals(1, found, "Key '" + Arrays.toString(key) + "' found " + found + " times");
        }

        Assertions.assertEquals(TOT, total);

        // GET EACH ITEM TO CHECK IT HAS BEEN DELETED
        for (int i = 0; i < TOT; ++i) {
          for (Index index : indexes)
            Assertions.assertTrue(index.get(new Object[] { i }).isEmpty(), "Found item with key " + i);
        }

      }
    });
  }

  @Test
  public void testPutDuplicates() {
    database.transaction(new Database.Transaction() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final Index[] indexes = database.getSchema().getIndexes();

        for (int i = 0; i < TOT; ++i) {
          int found = 0;

          final Object[] key = new Object[] { i };

          for (Index index : indexes) {

            final Set<RID> value = index.get(key);
            if (!value.isEmpty()) {
              try {
                index.put(key, new RID(database, 10, 10));
                Assertions.fail();
              } catch (DuplicatedKeyException e) {
                // OK
              }
              found++;
              total++;
            }
          }

          Assertions.assertEquals(1, found, "Key '" + Arrays.toString(key) + "' found " + found + " times");
        }

        Assertions.assertEquals(TOT, total);
      }
    });
  }

  @Test
  public void testScanIndexAscending() throws IOException {
    database.transaction(new Database.Transaction() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final Index[] indexes = database.getSchema().getIndexes();
        for (Index index : indexes) {
          Assertions.assertNotNull(index);

          final IndexCursor iterator;
          try {
            iterator = index.iterator(true);
            Assertions.assertNotNull(iterator);

            while (iterator.hasNext()) {
              Assertions.assertNotNull(iterator.next());

              Assertions.assertNotNull(iterator.getKeys());
              Assertions.assertEquals(1, iterator.getKeys().length);

              total++;
            }
          } catch (IOException e) {
            Assertions.fail(e);
          }
        }

        Assertions.assertEquals(TOT, total);
      }
    });
  }

  @Test
  public void testScanIndexDescending() throws IOException {
    database.transaction(new Database.Transaction() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final Index[] indexes = database.getSchema().getIndexes();
        for (Index index : indexes) {
          Assertions.assertNotNull(index);

          final IndexCursor iterator;
          try {
            iterator = index.iterator(false);
            Assertions.assertNotNull(iterator);

            while (iterator.hasNext()) {
              Assertions.assertNotNull(iterator.next());

              Assertions.assertNotNull(iterator.getKeys());
              Assertions.assertEquals(1, iterator.getKeys().length);

              total++;
            }
          } catch (IOException e) {
            Assertions.fail(e);
          }
        }

        Assertions.assertEquals(TOT, total);
      }
    });
  }

  @Test
  public void testScanIndexAscendingPartial() throws IOException {
    database.transaction(new Database.Transaction() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final Index[] indexes = database.getSchema().getIndexes();
        for (Index index : indexes) {
          Assertions.assertNotNull(index);

          final IndexCursor iterator;
          try {
            iterator = index.iterator(true, new Object[] { 10 });

            Assertions.assertNotNull(iterator);

            while (iterator.hasNext()) {
              Assertions.assertNotNull(iterator.next());

              Assertions.assertNotNull(iterator.getKeys());
              Assertions.assertEquals(1, iterator.getKeys().length);

              total++;
            }
          } catch (IOException e) {
            Assertions.fail(e);
          }
        }

        Assertions.assertEquals(TOT - 10, total);
      }
    });
  }

  @Test
  public void testScanIndexDescendingPartial() throws IOException {
    database.transaction(new Database.Transaction() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final Index[] indexes = database.getSchema().getIndexes();
        for (Index index : indexes) {
          Assertions.assertNotNull(index);

          final IndexCursor iterator;
          try {
            iterator = index.iterator(false, new Object[] { 9 });
            Assertions.assertNotNull(iterator);

            while (iterator.hasNext()) {
              Assertions.assertNotNull(iterator.next());

              Assertions.assertNotNull(iterator.getKeys());
              Assertions.assertEquals(1, iterator.getKeys().length);

              total++;
            }
          } catch (IOException e) {
            Assertions.fail(e);
          }
        }

        Assertions.assertEquals(10, total);
      }
    });
  }

  @Test
  public void testScanIndexRange() throws IOException {
    database.transaction(new Database.Transaction() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final Index[] indexes = database.getSchema().getIndexes();
        for (Index index : indexes) {
          Assertions.assertNotNull(index);

          final IndexCursor iterator;
          try {
            iterator = index.range(new Object[] { 10 }, new Object[] { 19 });
            Assertions.assertNotNull(iterator);

            while (iterator.hasNext()) {
              Identifiable value = (Identifiable) iterator.next();

              Assertions.assertNotNull(value);

              int fieldValue = (int) ((Document) value.getRecord()).get("id");
              Assertions.assertTrue(fieldValue >= 10 && fieldValue <= 19);

              Assertions.assertNotNull(iterator.getKeys());
              Assertions.assertEquals(1, iterator.getKeys().length);

              total++;
            }
          } catch (IOException e) {
            Assertions.fail(e);
          }
        }

        Assertions.assertEquals(10, total);
      }
    });
  }

  @Test
  public void testUnique() {
    final long startingWith = database.countType(TYPE_NAME, true);

    final long total = 5000;
    final int maxRetries = 100;

    final Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors()];

    final AtomicLong needRetryExceptions = new AtomicLong();
    final AtomicLong duplicatedExceptions = new AtomicLong();
    final AtomicLong crossThreadsInserted = new AtomicLong();

    LogManager.instance().info(this, "%s Started with %d threads", getClass(), threads.length);

    for (int i = 0; i < threads.length; ++i) {
      threads[i] = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            int threadInserted = 0;
            for (int i = TOT; i < TOT + total; ++i) {
              boolean keyPresent = false;
              for (int retry = 0; retry < maxRetries && !keyPresent; ++retry) {
                try {
                  Thread.sleep(new Random().nextInt(10));
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }

                database.begin();
                try {
                  final ModifiableDocument v = database.newDocument(TYPE_NAME);
                  v.set("id", i);
                  v.set("name", "Jay");
                  v.set("surname", "Miner");
                  v.save();

                  database.commit();

                  threadInserted++;
                  crossThreadsInserted.incrementAndGet();

                  if (threadInserted % 1000 == 0)
                    LogManager.instance()
                        .info(this, "%s Thread %d inserted %d records with key %d (total=%d)", getClass(), Thread.currentThread().getId(), i, threadInserted,
                            crossThreadsInserted.get());

                  keyPresent = true;

                } catch (NeedRetryException e) {
                  needRetryExceptions.incrementAndGet();
                  if (database.isTransactionActive())
                    database.rollback();
                  continue;
                } catch (DuplicatedKeyException e) {
                  duplicatedExceptions.incrementAndGet();
                  keyPresent = true;
                  if (database.isTransactionActive())
                    database.rollback();
                } catch (Exception e) {
                  LogManager.instance().error(this, "%s Thread %d Generic Exception", e, getClass(), Thread.currentThread().getId());
                  if (database.isTransactionActive())
                    database.rollback();
                  return;
                }
              }

              if (!keyPresent)
                LogManager.instance()
                    .warn(this, "%s Thread %d Cannot create key %d after %d retries! (total=%d)", getClass(), Thread.currentThread().getId(), i, maxRetries,
                        crossThreadsInserted.get());

            }

            LogManager.instance().info(this, "%s Thread %d completed (inserted=%d)", getClass(), Thread.currentThread().getId(), threadInserted);

          } catch (Exception e) {
            LogManager.instance().error(this, "%s Thread %d Error", e, getClass(), Thread.currentThread().getId());
          }
        }

      });
    }

    for (int i = 0; i < threads.length; ++i)
      threads[i].start();

    for (int i = 0; i < threads.length; ++i) {
      try {
        threads[i].join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    LogManager.instance().info(this, "%s Completed (inserted=%d needRetryExceptions=%d duplicatedExceptions=%d)", getClass(), crossThreadsInserted.get(),
        needRetryExceptions.get(), duplicatedExceptions.get());

    Assertions.assertEquals(total, crossThreadsInserted.get());
//    Assertions.assertTrue(needRetryExceptions.get() > 0);
    Assertions.assertTrue(duplicatedExceptions.get() > 0);

    Assertions.assertEquals(startingWith + total, database.countType(TYPE_NAME, true));
  }

  protected void beginTest() {
    database.transaction(new Database.Transaction() {
      @Override
      public void execute(Database database) {
        Assertions.assertFalse(database.getSchema().existsType(TYPE_NAME));

        final DocumentType type = database.getSchema().createDocumentType(TYPE_NAME, 3);
        type.createProperty("id", Integer.class);
        final Index[] indexes = database.getSchema().createClassIndexes(SchemaImpl.INDEX_TYPE.LSM_TREE, true, TYPE_NAME, new String[] { "id" }, 1000);

        for (int i = 0; i < TOT; ++i) {
          final ModifiableDocument v = database.newDocument(TYPE_NAME);
          v.set("id", i);
          v.set("name", "Jay");
          v.set("surname", "Miner");

          v.save();
        }

        database.commit();
        database.begin();

        for (Index index : indexes) {
          Assertions.assertTrue(index.getStats().get("pages") > 1);
        }
      }
    });
  }
}