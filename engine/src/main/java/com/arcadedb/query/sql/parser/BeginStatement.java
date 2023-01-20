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
/* Generated By:JJTree: Do not edit this line. OBeginStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.InternalResultSet;
import com.arcadedb.query.sql.executor.ResultInternal;
import com.arcadedb.query.sql.executor.ResultSet;

import java.util.*;

public class BeginStatement extends SimpleExecStatement {
  protected Identifier isolation;

  public BeginStatement(final int id) {
    super(id);
  }

  @Override
  public ResultSet executeSimple(final CommandContext context) {
    context.getDatabase().begin();
    final InternalResultSet result = new InternalResultSet();
    final ResultInternal item = new ResultInternal();
    item.setProperty("operation", "begin");
    result.add(item);
    return result;
  }

  @Override
  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    builder.append("BEGIN");
    if (isolation != null) {
      builder.append(" ISOLATION ");
      isolation.toString(params, builder);
    }
  }

  @Override
  public BeginStatement copy() {
    final BeginStatement result = new BeginStatement(-1);
    result.isolation = isolation == null ? null : isolation.copy();
    return result;
  }

  @Override
  protected Object[] getIdentityElements() {
    return new Object[] { isolation };
  }

}
/* JavaCC - OriginalChecksum=aaa994acbe63cc4169fe33144d412fed (do not edit this line) */
