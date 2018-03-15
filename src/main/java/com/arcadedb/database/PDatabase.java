package com.arcadedb.database;

import com.arcadedb.engine.PFileManager;
import com.arcadedb.engine.PPageManager;
import com.arcadedb.schema.PSchema;
import com.arcadedb.serializer.PBinarySerializer;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public interface PDatabase {
  interface PTransaction {
    void execute(PDatabase database);
  }

  String getDatabasePath();

  PTransactionContext getTransaction();

  void drop();

  void close();

  boolean isTransactionActive();

  void checkTransactionIsActive();

  void transaction(PTransaction txBlock);

  void setAutoTransaction(boolean autoTransaction);

  void begin();

  void commit();

  void rollback();

  void scanType(String className, PRecordCallback callback);

  void scanBucket(String bucketName, PRecordCallback callback);

  Iterator<PRecord> bucketIterator(String bucketName);

  PRecord lookupByRID(PRID rid);

  List<? extends PRecord> lookupByKey(String type, String[] properties, Object[] keys);

  void saveRecord(PModifiableDocument record);

  void saveRecord(PRecord record, String bucketName);

  void deleteRecord(PRID rid);

  long countType(String typeName);

  long countBucket(String bucketName);

  PModifiableDocument newDocument();

  PVertex newVertex();

  PEdge newEdge();

  PSchema getSchema();

  PFileManager getFileManager();

  PRecordFactory getRecordFactory();

  PBinarySerializer getSerializer();

  PPageManager getPageManager();

  Object executeInLock(Callable<Object> callable);
}
