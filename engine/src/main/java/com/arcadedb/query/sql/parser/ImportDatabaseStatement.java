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
/* Generated by: JJTree: Do not edit this line. ImportDatabaseStatement.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Database;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.InternalResultSet;
import com.arcadedb.query.sql.executor.ResultInternal;
import com.arcadedb.query.sql.executor.ResultSet;

import java.lang.reflect.*;
import java.util.*;

public class ImportDatabaseStatement extends SimpleExecStatement {

  protected       Url                         url;
  protected       Expression                  key;
  protected       Expression                  value;
  protected final Map<Expression, Expression> settings = new HashMap<>();

  public ImportDatabaseStatement(final int id) {
    super(id);
  }

  @Override
  public ResultSet executeSimple(final CommandContext context) {
    final String targetUrl = this.url.getUrlString();
    final ResultInternal result = new ResultInternal();
    result.setProperty("operation", "import database");
    result.setProperty("fromUrl", targetUrl);

    try {
      final Class<?> clazz = Class.forName("com.arcadedb.integration.importer.Importer");
      final Object importer = clazz.getConstructor(Database.class, String.class).newInstance(context.getDatabase(), url.getUrlString());

      // TRANSFORM SETTINGS
      final Map<String, String> settingsToString = new HashMap<>();
      for (final Map.Entry<Expression, Expression> entry : settings.entrySet())
        settingsToString.put(entry.getKey().value.toString(), entry.getValue().value.toString());

      clazz.getMethod("setSettings", Map.class).invoke(importer, settingsToString);
      clazz.getMethod("load").invoke(importer);

    } catch (final ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException e) {
      throw new CommandExecutionException("Error on importing database, importer libs not found in classpath", e);
    } catch (final InvocationTargetException e) {
      throw new CommandExecutionException("Error on importing database", e.getTargetException());
    }

    result.setProperty("result", "OK");

    final InternalResultSet rs = new InternalResultSet();
    rs.add(result);
    return rs;
  }

  @Override
  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    builder.append("IMPORT DATABASE ");
    url.toString(params, builder);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    final ImportDatabaseStatement that = (ImportDatabaseStatement) o;
    return Objects.equals(url, that.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url);
  }

  @Override
  public Statement copy() {
    final ImportDatabaseStatement result = new ImportDatabaseStatement(-1);
    result.url = this.url;
    return result;
  }
}
/* ParserGeneratorCC - OriginalChecksum=ed8df9761ba25c4fca4bc31ece14a5f3 (do not edit this line) */
