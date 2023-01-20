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
/* Generated By:JJTree: Do not edit this line. OSleepStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.InternalResultSet;
import com.arcadedb.query.sql.executor.ResultInternal;
import com.arcadedb.query.sql.executor.ResultSet;

import java.util.*;

public class SleepStatement extends SimpleExecStatement {
  protected PInteger millis;

  public SleepStatement(final int id) {
    super(id);
  }

  @Override
  public ResultSet executeSimple(final CommandContext context) {

    final InternalResultSet result = new InternalResultSet();
    final ResultInternal item = new ResultInternal();
    item.setProperty("operation", "sleep");
    try {
      Thread.sleep(millis.getValue().intValue());
      item.setProperty("result", "OK");
      item.setProperty("millis", millis.getValue().intValue());
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      item.setProperty("result", "failure");
      item.setProperty("errorType", e.getClass().getSimpleName());
      item.setProperty("errorMessage", e.getMessage());
    }
    result.add(item);
    return result;
  }

  @Override
  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    builder.append("SLEEP ");
    millis.toString(params, builder);
  }

  @Override
  public SleepStatement copy() {
    final SleepStatement result = new SleepStatement(-1);
    result.millis = millis == null ? null : millis.copy();
    return result;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final SleepStatement that = (SleepStatement) o;

    return Objects.equals(millis, that.millis);
  }

  @Override
  public int hashCode() {
    return millis != null ? millis.hashCode() : 0;
  }
}
/* JavaCC - OriginalChecksum=2ea765ee266d4215414908b0e09c0779 (do not edit this line) */
