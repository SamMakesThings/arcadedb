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
/* Generated By:JJTree: Do not edit this line. OCreatePropertyStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Database;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.InternalResultSet;
import com.arcadedb.query.sql.executor.ResultInternal;
import com.arcadedb.query.sql.executor.ResultSet;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.Property;
import com.arcadedb.schema.Type;

import java.util.*;
import java.util.stream.*;

public class CreatePropertyStatement extends DDLStatement {
  public Identifier                             typeName;
  public Identifier                             propertyName;
  public Identifier                             propertyType;
  public List<CreatePropertyAttributeStatement> attributes = new ArrayList<CreatePropertyAttributeStatement>();
  boolean ifNotExists = false;

  public CreatePropertyStatement(final int id) {
    super(id);
  }

  @Override
  public ResultSet executeDDL(final CommandContext context) {
    final ResultInternal result = new ResultInternal();
    result.setProperty("operation", "create property");
    result.setProperty("typeName", typeName.getStringValue());
    result.setProperty("propertyName", propertyName.getStringValue());
    executeInternal(context, result);
    final InternalResultSet rs = new InternalResultSet();
    rs.add(result);
    return rs;
  }

  private void executeInternal(final CommandContext context, final ResultInternal result) {
    final Database db = context.getDatabase();
    final DocumentType typez = db.getSchema().getType(typeName.getStringValue());
    if (typez == null) {
      throw new CommandExecutionException("Type not found: " + typeName.getStringValue());
    }
    if (typez.existsProperty(propertyName.getStringValue())) {
      if (ifNotExists) {
        return;
      }
      throw new CommandExecutionException("Property " + typeName.getStringValue() + "." + propertyName.getStringValue() + " already exists");
    }

    final Type type = Type.valueOf(propertyType.getStringValue().toUpperCase(Locale.ENGLISH));

    // CREATE IT LOCALLY
    final Property internalProp = typez.createProperty(propertyName.getStringValue(), type);
    for (final CreatePropertyAttributeStatement attr : attributes) {
      final Object val = attr.setOnProperty(internalProp, context);
      result.setProperty(attr.settingName.getStringValue(), val);
    }
  }

  @Override
  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    builder.append("CREATE PROPERTY ");
    typeName.toString(params, builder);
    builder.append(".");
    propertyName.toString(params, builder);
    if (ifNotExists) {
      builder.append(" IF NOT EXISTS");
    }
    builder.append(" ");
    propertyType.toString(params, builder);

    if (!attributes.isEmpty()) {
      builder.append(" (");
      for (int i = 0; i < attributes.size(); i++) {
        final CreatePropertyAttributeStatement att = attributes.get(i);
        att.toString(params, builder);

        if (i < attributes.size() - 1) {
          builder.append(", ");
        }
      }
      builder.append(")");
    }
  }

  @Override
  public CreatePropertyStatement copy() {
    final CreatePropertyStatement result = new CreatePropertyStatement(-1);
    result.typeName = typeName == null ? null : typeName.copy();
    result.propertyName = propertyName == null ? null : propertyName.copy();
    result.propertyType = propertyType == null ? null : propertyType.copy();
    result.ifNotExists = ifNotExists;
    result.attributes = attributes == null ? null : attributes.stream().map(x -> x.copy()).collect(Collectors.toList());
    return result;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final CreatePropertyStatement that = (CreatePropertyStatement) o;

    if (!Objects.equals(typeName, that.typeName))
      return false;
    if (!Objects.equals(propertyName, that.propertyName))
      return false;
    if (!Objects.equals(propertyType, that.propertyType))
      return false;
    if (!Objects.equals(attributes, that.attributes))
      return false;
    return ifNotExists == that.ifNotExists;
  }

  @Override
  public int hashCode() {
    int result = typeName != null ? typeName.hashCode() : 0;
    result = 31 * result + (propertyName != null ? propertyName.hashCode() : 0);
    result = 31 * result + (propertyType != null ? propertyType.hashCode() : 0);
    result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=ff78676483d59013ab10b13bde2678d3 (do not edit this line) */
