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
/* Generated By:JJTree: Do not edit this line. OExpression.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Identifiable;
import com.arcadedb.database.Record;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.query.sql.executor.AggregationContext;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.Result;
import com.arcadedb.query.sql.executor.ResultInternal;

import java.util.*;

/**
 * this class is only used by the query executor to store pre-calculated values and store them in a temporary AST. It's not produced
 * by parsing
 */
public class ValueExpression extends Expression {
  public ValueExpression(final Object val) {
    super(-1);
    this.value = val;
  }

  public Object execute(final Identifiable iCurrentRecord, final CommandContext context) {
    return value;
  }

  public Object execute(final Result iCurrentRecord, final CommandContext context) {
    return value;
  }

  public boolean isBaseIdentifier() {
    return false;
  }

  public boolean isEarlyCalculated() {
    return true;
  }

  public Identifier getDefaultAlias() {
    return new Identifier(String.valueOf(value));
  }

  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    builder.append(value);
  }

  public boolean supportsBasicCalculation() {
    return true;
  }

  public boolean isIndexedFunctionCal(CommandContext context) {
    return false;
  }

  public boolean canExecuteIndexedFunctionWithoutIndex(final FromClause target, final CommandContext context, final BinaryCompareOperator operator,
      final Object right) {
    return false;
  }

  public boolean allowsIndexedFunctionExecutionOnTarget(final FromClause target, final CommandContext context, final BinaryCompareOperator operator,
      final Object right) {
    return false;
  }

  public boolean executeIndexedFunctionAfterIndexSearch(final FromClause target, final CommandContext context, final BinaryCompareOperator operator,
      final Object right) {
    return false;
  }

  public boolean isExpand() {
    return false;
  }

  public ValueExpression getExpandContent() {
    return null;
  }

  public boolean isAggregate(CommandContext context) {
    return false;
  }

  public ValueExpression splitForAggregation(final AggregateProjectionSplit aggregateSplit) {
    return this;
  }

  public AggregationContext getAggregationContext(final CommandContext context) {
    throw new CommandExecutionException("Cannot aggregate on " + this);
  }

  public ValueExpression copy() {
    final ValueExpression result = new ValueExpression(-1);
    result.value = value;
    return result;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final ValueExpression that = (ValueExpression) o;
    return that.value.equals(this.value);
  }

  @Override
  public int hashCode() {
    return 1;
  }

  public void extractSubQueries(final SubQueryCollector collector) {
    // NO ACTIONS
  }

  public void extractSubQueries(final Identifier letAlias, final SubQueryCollector collector) {
    // NO ACTIONS
  }

  public boolean refersToParent() {
    return false;
  }

  List<String> getMatchPatternInvolvedAliases() {
    return null;
  }

  public void applyRemove(final ResultInternal result, final CommandContext context) {
    throw new CommandExecutionException("Cannot apply REMOVE " + this);
  }

  public boolean isCount() {
    return false;
  }

  public boolean isDefinedFor(final Result currentRecord) {
    return true;
  }

  public boolean isDefinedFor(final Record currentRecord) {
    return true;
  }

  public boolean isCacheable() {
    return true;
  }
}
/* JavaCC - OriginalChecksum=9c860224b121acdc89522ae97010be01 (do not edit this line) */
