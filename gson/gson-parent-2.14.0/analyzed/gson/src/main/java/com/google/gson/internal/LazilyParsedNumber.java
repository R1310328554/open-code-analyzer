/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gson.internal;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.math.BigDecimal;

/**
 * 持有数值，在需要时延迟解析为具体数字类型。
 *
 * @author Inderjeet Singh
 */
@SuppressWarnings("serial")
public final class LazilyParsedNumber extends Number {
  /** 以字符串保存的原始数值。 */
  private final String value;

  /**
   * @param value 不得为 null
   */
  public LazilyParsedNumber(String value) {
    this.value = value;
  }

  private BigDecimal asBigDecimal() {
    return NumberLimits.parseBigDecimal(value);
  }

  @Override
  public int intValue() {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      try {
        return (int) Long.parseLong(value);
      } catch (NumberFormatException nfe) {
        return asBigDecimal().intValue();
      }
    }
  }

  @Override
  public long longValue() {
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      return asBigDecimal().longValue();
    }
  }

  @Override
  public float floatValue() {
    return Float.parseFloat(value);
  }

  @Override
  public double doubleValue() {
    return Double.parseDouble(value);
  }

  @Override
  public String toString() {
    return value;
  }

  /** 序列化时转为 BigDecimal，反序列化端无需 Gson。 */
  private Object writeReplace() throws ObjectStreamException {
    return asBigDecimal();
  }

  private void readObject(ObjectInputStream in) throws IOException {
    throw new InvalidObjectException("Deserialization is unsupported");
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof LazilyParsedNumber) {
      LazilyParsedNumber other = (LazilyParsedNumber) obj;
      return value.equals(other.value);
    }
    return false;
  }
}
