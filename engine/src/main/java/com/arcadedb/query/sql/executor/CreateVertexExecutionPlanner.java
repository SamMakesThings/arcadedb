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
package com.arcadedb.query.sql.executor;

import com.arcadedb.exception.CommandSQLParsingException;
import com.arcadedb.query.sql.parser.CreateVertexStatement;

import java.util.*;

/**
 * @author Luigi Dell'Aquila (luigi.dellaquila-(at)-gmail.com)
 */
public class CreateVertexExecutionPlanner extends InsertExecutionPlanner {
  public CreateVertexExecutionPlanner(final CreateVertexStatement statement) {
    this.targetType = statement.getTargetType() == null ? null : statement.getTargetType().copy();
    this.targetBucketName = statement.getTargetBucketName() == null ? null : statement.getTargetBucketName().copy();
    this.targetBucket = statement.getTargetBucket() == null ? null : statement.getTargetBucket().copy();
    if (this.targetType == null && this.targetBucket == null && this.targetBucketName == null)
      throw new CommandSQLParsingException("Missing target");

    this.insertBody = statement.getInsertBody() == null ? null : statement.getInsertBody().copy();
    this.returnStatement = statement.getReturnStatement() == null ? null : statement.getReturnStatement().copy();
  }

  @Override
  public InsertExecutionPlan createExecutionPlan(final CommandContext context, final boolean enableProfiling) {
    final InsertExecutionPlan prev = super.createExecutionPlan(context, enableProfiling);
    final List<ExecutionStep> steps = new ArrayList<>(prev.getSteps());
    final InsertExecutionPlan result = new InsertExecutionPlan(context);

    handleCheckType(result, context, enableProfiling);
    for (final ExecutionStep step : steps)
      result.chain((ExecutionStepInternal) step);

    return result;
  }

  private void handleCheckType(final InsertExecutionPlan result, final CommandContext context, final boolean profilingEnabled) {
    if (targetType != null)
      result.chain(new CheckIsVertexTypeStep(targetType.getStringValue(), context, profilingEnabled));
  }
}
