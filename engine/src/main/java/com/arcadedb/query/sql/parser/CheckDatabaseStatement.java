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
/* Generated by: JJTree: Do not edit this line. CheckDatabaseStatement.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.engine.DatabaseChecker;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.InternalResultSet;
import com.arcadedb.query.sql.executor.ResultInternal;
import com.arcadedb.query.sql.executor.ResultSet;

import java.util.*;
import java.util.stream.*;

public class CheckDatabaseStatement extends SimpleExecStatement {
  protected final Set<BucketIdentifier> buckets = new HashSet<>();
  protected final Set<Identifier>       types   = new HashSet<>();
  protected       boolean               fix     = false;

  public CheckDatabaseStatement(final int id) {
    super(id);
  }

  @Override
  public ResultSet executeSimple(final CommandContext context) {
    final ResultInternal result = new ResultInternal();
    result.setProperty("operation", "check database");

    if (context.getDatabase().isTransactionActive())
      context.getDatabase().rollback();

    final DatabaseChecker checker = new DatabaseChecker(context.getDatabase().getWrappedDatabaseInstance());
    checker.setVerboseLevel(0);
    checker.setBuckets(buckets.stream().map(x -> x.getValue()).collect(Collectors.toSet()));
    checker.setTypes(types.stream().map(x -> (x.getStringValue().startsWith("\"") || x.getStringValue().startsWith("'")) ?
        x.getStringValue().substring(1, x.getStringValue().length() - 1) :
        x.getStringValue()).collect(Collectors.toSet()));
    checker.setFix(fix);

    final Map<String, Object> checkResult = checker.check();

    result.setPropertiesFromMap(checkResult);

    final InternalResultSet rs = new InternalResultSet();
    rs.add(result);
    return rs;
  }

  @Override
  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    builder.append("CHECK DATABASE");

    if (!types.isEmpty()) {
      builder.append(" TYPE ");
      final Iterator<Identifier> iterator = types.iterator();
      for (int i = 0; iterator.hasNext(); i++) {
        builder.append(iterator.next().getStringValue());

        if (i > 0)
          builder.append(",");
      }
    }

    if (!buckets.isEmpty()) {
      builder.append(" BUCKET ");
      final Iterator<BucketIdentifier> iterator = buckets.iterator();
      for (int i = 0; iterator.hasNext(); i++) {
        final Object bucket = iterator.next().getValue();
        builder.append(bucket);

        if (i > 0)
          builder.append(",");
      }
    }
  }
}
/* ParserGeneratorCC - OriginalChecksum=8b4b56a95655bca6baea744bc4c6aedd (do not edit this line) */
