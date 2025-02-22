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

import com.arcadedb.exception.TimeoutException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by luigidellaquila on 26/07/16.
 */
public class DistinctExecutionStepTest {

  @Test
  public void test() {
    final CommandContext ctx = new BasicCommandContext();
    final DistinctExecutionStep step = new DistinctExecutionStep(ctx, false);

    final AbstractExecutionStep prev = new AbstractExecutionStep(ctx, false) {
      boolean done = false;

      @Override
      public ResultSet syncPull(final CommandContext ctx, final int nRecords) throws TimeoutException {
        final InternalResultSet result = new InternalResultSet();
        if (!done) {
          for (int i = 0; i < 10; i++) {
            final ResultInternal item = new ResultInternal();
            item.setProperty("name", i % 2 == 0 ? "foo" : "bar");
            result.add(item);
          }
          done = true;
        }
        return result;
      }
    };

    step.setPrevious(prev);
    final ResultSet res = step.syncPull(ctx, 10);
    Assertions.assertTrue(res.hasNext());
    res.next();
    Assertions.assertTrue(res.hasNext());
    res.next();
    Assertions.assertFalse(res.hasNext());
  }
}
