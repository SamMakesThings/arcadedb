/*
 * Copyright 2023 Arcade Data Ltd
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.arcadedb.query.sql.method.misc;

import com.arcadedb.query.sql.executor.SQLMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.*;

import static org.assertj.core.api.Assertions.assertThat;

class SQLMethodAsDecimalTest {
  private SQLMethod method;

  @BeforeEach
  void setUp() {
    method = new SQLMethodAsDecimal();
  }

  @Test
  void testNulIsReturnedAsNull() {
    final Object result = method.execute(null, null, null, null, null);
    assertThat(result).isNull();
  }

  @Test
  void testStringToDecimal() {
    final Object result = method.execute("10.0", null, null, "10.0", null);
    assertThat(result).isInstanceOf(BigDecimal.class);
    assertThat(result).isEqualTo(new BigDecimal("10.0"));
  }

  @Test
  void testLongToDecimal() {
    final Object result = method.execute(10l, null, null, 10l, null);
    assertThat(result).isInstanceOf(BigDecimal.class);
    assertThat(result).isEqualTo(new BigDecimal("10"));
  }

  @Test
  void testDecimalToDecimal() {
    final Object result = method.execute(10.0f, null, null, 10.0f, null);
    assertThat(result).isInstanceOf(BigDecimal.class);
    assertThat(result).isEqualTo(new BigDecimal("10.0"));
  }

  @Test
  void testIntegerToDecimal() {
    final Object result = method.execute(10, null, null, 10, null);
    assertThat(result).isInstanceOf(BigDecimal.class);
    assertThat(result).isEqualTo(new BigDecimal("10"));
  }

}
