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
 */
package com.arcadedb;

import com.arcadedb.log.DefaultLogger;
import com.arcadedb.log.LogManager;
import com.arcadedb.log.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

public class LoggerTest extends TestHelper {
  private boolean logged  = false;
  private boolean flushed = false;

  @Test
  public void testCustomLogger() {
    try {
      LogManager.instance().setLogger(new Logger() {

        @Override
        public void log(Object iRequester, Level iLevel, String iMessage, Throwable iException, String context, Object... args) {
          logged = true;
        }

        @Override
        public void flush() {
          flushed = true;
        }
      });

      LogManager.instance().log(this, Level.FINE, "This is a test");

      Assertions.assertEquals(true, logged);

      LogManager.instance().flush();

      Assertions.assertEquals(true, flushed);
    } finally {
      LogManager.instance().setLogger(new DefaultLogger());
    }
  }
}
