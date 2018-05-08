/*
 *
 *  *  Copyright 2010-2016 OrientDB LTD (http://orientdb.com)
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *
 *  * For more information: http://orientdb.com
 *
 */
package com.arcadedb.sql.function.misc;

import com.arcadedb.database.PIdentifiable;
import com.arcadedb.sql.executor.OCommandContext;
import com.arcadedb.sql.function.OSQLFunctionAbstract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Returns the current date time.
 *
 * @author Luca Garulli (l.garulli--(at)--orientdb.com)
 * @see OSQLFunctionDate
 */
public class OSQLFunctionSysdate extends OSQLFunctionAbstract {
  public static final String NAME = "sysdate";

  private final Date             now;
  private       SimpleDateFormat format;

  /**
   * Get the date at construction to have the same date for all the iteration.
   */
  public OSQLFunctionSysdate() {
    super(NAME, 0, 2);
    now = new Date();
  }

  public Object execute(final Object iThis, final PIdentifiable iCurrentRecord, Object iCurrentResult, final Object[] iParams,
      OCommandContext iContext) {
    if (iParams.length == 0)
      return now;

    if (format == null) {
      final TimeZone tz =
          iParams.length > 0 ? TimeZone.getTimeZone(iParams[0].toString()) : iContext.getDatabase().getSchema().getTimeZone();

      if (iParams.length > 1)
        format = new SimpleDateFormat((String) iParams[1]);
      else
        format = new SimpleDateFormat(iContext.getDatabase().getSchema().getDateFormat());

      format.setTimeZone(tz);
    }

    return format.format(now);
  }

  public boolean aggregateResults(final Object[] configuredParameters) {
    return false;
  }

  public String getSyntax() {
    return "sysdate([<format>] [,<timezone>])";
  }

  @Override
  public Object getResult() {
    return null;
  }
}
