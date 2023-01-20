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
/* Generated By:JJTree: Do not edit this line. OUpdateEdgeStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.UpdateExecutionPlan;
import com.arcadedb.query.sql.executor.UpdateExecutionPlanner;

import java.util.stream.*;

public class UpdateEdgeStatement extends UpdateStatement {
  public UpdateEdgeStatement(final int id) {
    super(id);
  }

  protected String getStatementType() {
    return "UPDATE EDGE ";
  }

  @Override
  public UpdateExecutionPlan createExecutionPlan(final CommandContext context, final boolean enableProfiling) {
    final UpdateExecutionPlanner planner = new UpdateExecutionPlanner(this);
    return planner.createExecutionPlan(context, enableProfiling);
  }

  @Override
  public UpdateEdgeStatement copy() {
    final UpdateEdgeStatement result = new UpdateEdgeStatement(-1);
    result.target = target == null ? null : target.copy();
    result.operations = operations == null ? null : operations.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.upsert = upsert;
    result.returnBefore = returnBefore;
    result.returnAfter = returnAfter;
    result.returnProjection = returnProjection == null ? null : returnProjection.copy();
    result.whereClause = whereClause == null ? null : whereClause.copy();
    result.limit = limit == null ? null : limit.copy();
    result.timeout = timeout == null ? null : timeout.copy();
    return result;
  }

}
/* JavaCC - OriginalChecksum=496f32976ee84e3a3a89d1410dc134c5 (do not edit this line) */
