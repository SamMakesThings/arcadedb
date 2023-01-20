/*
 * Copyright © 2021-present Arcade Data Ltd (info@arcadedata.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-FileCopyrightText: 2021-present Arcade Data Ltd (info@arcadedata.com)
 * SPDX-License-Identifier: Apache-2.0
 */
/* Generated By:JJTree: Do not edit this line. OTruncateRecordStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.ResultSet;

import java.util.*;
import java.util.stream.*;

public class TruncateRecordStatement extends SimpleExecStatement {
  protected Rid       record;
  protected List<Rid> records;

  public TruncateRecordStatement(final int id) {
    super(id);
  }

  @Override
  public ResultSet executeSimple(final CommandContext context) {
//    List<ORid> recs = new ArrayList<>();
//    if (record != null) {
//      recs.add(record);
//    } else {
//      recs.addAll(records);
//    }
//
//    OInternalResultSet rs = new OInternalResultSet();
//    final ODatabaseDocumentInternal database = (ODatabaseDocumentInternal) context.getDatabase();
//    for (ORid rec : recs) {
//      try {
//        final ORecordId rid = rec.toRecordId((OResult) null, context);
//        final OStorageOperationResult<Boolean> result = database.getStorage().deleteRecord(rid, -1, 0, null);
//        database.getLocalCache().deleteRecord(rid);
//
//        if (result.getResult()) {
//          OResultInternal recordRes = new OResultInternal();
//          recordRes.setProperty("operation", "truncate record");
//          recordRes.setProperty("record", rec.toString());
//          rs.add(recordRes);
//        }
//      } catch (Exception e) {
//        throw OException.wrapException(new PCommandExecutionException("Error on executing command"), e);
//      }
//    }
//
//    return rs;
    throw new UnsupportedOperationException();
  }

  @Override
  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    builder.append("TRUNCATE RECORD ");
    if (record != null) {
      record.toString(params, builder);
    } else {
      builder.append("[");
      boolean first = true;
      for (final Rid r : records) {
        if (!first) {
          builder.append(",");
        }
        r.toString(params, builder);
        first = false;
      }
      builder.append("]");
    }
  }

  @Override
  public TruncateRecordStatement copy() {
    final TruncateRecordStatement result = new TruncateRecordStatement(-1);
    result.record = record == null ? null : record.copy();
    result.records = records == null ? null : records.stream().map(x -> x.copy()).collect(Collectors.toList());
    return result;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final TruncateRecordStatement that = (TruncateRecordStatement) o;

    if (!Objects.equals(record, that.record))
      return false;
    return Objects.equals(records, that.records);
  }

  @Override
  public int hashCode() {
    int result = record != null ? record.hashCode() : 0;
    result = 31 * result + (records != null ? records.hashCode() : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=9da68e9fe4c4bf94a12d8a6f8864097a (do not edit this line) */
