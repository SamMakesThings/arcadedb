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
/* Generated By:JJTree: Do not edit this line. OExpansion.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Document;
import com.arcadedb.database.Identifiable;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.Result;
import com.arcadedb.query.sql.executor.ResultInternal;

import java.util.*;
import java.util.stream.*;

public class NestedProjection extends SimpleNode {
  protected List<NestedProjectionItem> includeItems = new ArrayList<>();
  protected List<NestedProjectionItem> excludeItems = new ArrayList<>();
  protected NestedProjectionItem       starItem;
  private   PInteger                   recursion; //not used for now

  public NestedProjection(final int id) {
    super(id);
  }

  /**
   * @param expression
   * @param input
   * @param context
   */
  public Object apply(final Expression expression, final Object input, final CommandContext context) {
    if (input instanceof Result) {
      return apply(expression, (Result) input, context, recursion == null ? 0 : recursion.getValue().intValue());
    }
    if (input instanceof Identifiable) {
      return apply(expression, (Document) ((Identifiable) input).getRecord(), context, recursion == null ? 0 : recursion.getValue().intValue());
    }
    if (input instanceof Map) {
      return apply(expression, (Map) input, context, recursion == null ? 0 : recursion.getValue().intValue());
    }
    if (input instanceof Collection) {
      return ((Collection) input).stream().map(x -> apply(expression, x, context)).collect(Collectors.toList());
    }
    Iterator iter = null;
    if (input instanceof Iterable) {
      iter = ((Iterable) input).iterator();
    }
    if (input instanceof Iterator) {
      iter = (Iterator) input;
    }
    if (iter != null) {
      final List result = new ArrayList();
      while (iter.hasNext()) {
        result.add(apply(expression, iter.next(), context));
      }
      return result;
    }
    return input;
  }

  private Object apply(final Expression expression, final Result elem, final CommandContext context, final int recursion) {
    final ResultInternal result = new ResultInternal();
    if (starItem != null || includeItems.size() == 0) {
      for (final String property : elem.getPropertyNames()) {
        if (isExclude(property)) {
          continue;
        }
        result.setProperty(property, convert(tryExpand(expression, property, elem.getProperty(property), context, recursion)));
      }
    }
    if (includeItems.size() > 0) {
      //TODO manage wildcards!
      for (final NestedProjectionItem item : includeItems) {
        final String alias = item.alias != null ? item.alias.getStringValue() : item.expression.getDefaultAlias().getStringValue();
        Object value = item.expression.execute(elem, context);
        if (item.expansion != null) {
          value = item.expand(expression, alias, value, context, recursion - 1);
        }
        result.setProperty(alias, convert(value));
      }
    }
    return result;
  }

  private boolean isExclude(final String propertyName) {
    for (final NestedProjectionItem item : excludeItems) {
      if (item.matches(propertyName)) {
        return true;
      }
    }
    return false;
  }

  private Object tryExpand(final Expression rootExpr, final String propName, final Object propValue, final CommandContext context, final int recursion) {
    for (final NestedProjectionItem item : includeItems) {
      if (item.matches(propName) && item.expansion != null) {
        return item.expand(rootExpr, propName, propValue, context, recursion);
      }
    }
    return propValue;
  }

  private Object apply(final Expression expression, final Document input, final CommandContext context, final int recursion) {
    final Document elem = input;
    final ResultInternal result = new ResultInternal();
    if (starItem != null || includeItems.size() == 0) {
      for (final String property : elem.getPropertyNames()) {
        if (isExclude(property)) {
          continue;
        }
        result.setProperty(property, convert(tryExpand(expression, property, elem.get(property), context, recursion)));
      }
    }
    if (includeItems.size() > 0) {
      //TODO manage wildcards!
      for (final NestedProjectionItem item : includeItems) {
        final String alias = item.alias != null ? item.alias.getStringValue() : item.expression.getDefaultAlias().getStringValue();
        Object value = item.expression.execute(elem, context);
        if (item.expansion != null) {
          value = item.expand(expression, alias, value, context, recursion - 1);
        }
        result.setProperty(alias, convert(value));
      }
    }
    return result;
  }

  private Object apply(final Expression expression, final Map<String, Object> input, final CommandContext context, final int recursion) {
    final ResultInternal result = new ResultInternal();

    if (starItem != null || includeItems.size() == 0) {
      for (final Map.Entry<String, Object> entry : input.entrySet()) {
        if (isExclude(entry.getKey())) {
          continue;
        }
        result.setProperty(entry.getKey(), convert(tryExpand(expression, entry.getKey(), entry.getValue(), context, recursion)));
      }
    }
    if (includeItems.size() > 0) {
      //TODO manage wildcards!
      for (final NestedProjectionItem item : includeItems) {
        final String alias = item.alias != null ? item.alias.getStringValue() : item.expression.getDefaultAlias().getStringValue();
        final ResultInternal elem = new ResultInternal();
        input.entrySet().forEach(x -> elem.setProperty(x.getKey(), x.getValue()));
        Object value = item.expression.execute(elem, context);
        if (item.expansion != null) {
          value = item.expand(expression, alias, value, context, recursion - 1);
        }
        result.setProperty(alias, convert(value));
      }
    }
    return result;
  }

  @Override
  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    builder.append(":{");
    boolean first = true;
    if (starItem != null) {
      starItem.toString(params, builder);
      first = false;
    }
    for (final NestedProjectionItem item : includeItems) {
      if (!first) {
        builder.append(", ");
      }
      item.toString(params, builder);
      first = false;
    }
    for (final NestedProjectionItem item : excludeItems) {
      if (!first) {
        builder.append(", ");
      }
      item.toString(params, builder);
      first = false;
    }

    builder.append("}");
    if (recursion != null) {
      builder.append("[");
      recursion.toString(params, builder);
      builder.append("]");
    }
  }

  public NestedProjection copy() {
    final NestedProjection result = new NestedProjection(-1);
    result.includeItems = includeItems.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.excludeItems = excludeItems.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.starItem = starItem == null ? null : starItem.copy();
    result.recursion = recursion == null ? null : recursion.copy();
    return result;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final NestedProjection that = (NestedProjection) o;

    if (!Objects.equals(includeItems, that.includeItems))
      return false;
    if (!Objects.equals(excludeItems, that.excludeItems))
      return false;
    if (!Objects.equals(starItem, that.starItem))
      return false;
    return Objects.equals(recursion, that.recursion);
  }

  @Override
  public int hashCode() {
    int result = includeItems != null ? includeItems.hashCode() : 0;
    result = 31 * result + (excludeItems != null ? excludeItems.hashCode() : 0);
    result = 31 * result + (starItem != null ? starItem.hashCode() : 0);
    result = 31 * result + (recursion != null ? recursion.hashCode() : 0);
    return result;
  }

  private Object convert(final Object value) {
//    if (value instanceof ORidBag) {
//      List result = new ArrayList();
//      ((ORidBag) value).forEach(x -> result.add(x));
//      return result;
//    }
    return value;
  }
}
/* JavaCC - OriginalChecksum=a7faf9beb3c058e28999b17cb43b26f6 (do not edit this line) */
