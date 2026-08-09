/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.gson;

import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.internal.NumberLimits;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * 表示 JSON 原始值的类。原始值可以是 String、Java 基本类型或 Java 基本类型包装类。
 *
 * <p>有关如何将 {@code JsonPrimitive} 及一般任何 {@code JsonElement} 与 JSON 相互转换的详情，请参阅
 * {@link JsonElement} 文档。
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class JsonPrimitive extends JsonElement {

  private final Object value;

  /**
   * 创建包含布尔值的原始值。
   *
   * @param bool 用于创建原始值的布尔值
   */
  // "deprecation" suppression for superclass constructor
  // "UnnecessaryBoxedVariable" Error Prone warning is correct since method does not accept
  // null, but cannot be changed anymore since this is public API
  @SuppressWarnings({"deprecation", "UnnecessaryBoxedVariable"})
  public JsonPrimitive(Boolean bool) {
    value = Objects.requireNonNull(bool);
  }

  /**
   * 创建包含 {@link Number} 的原始值。
   *
   * @param number 用于创建原始值的数字
   */
  @SuppressWarnings("deprecation") // superclass constructor
  public JsonPrimitive(Number number) {
    value = Objects.requireNonNull(number);
  }

  /**
   * 创建包含 String 值的原始值。
   *
   * @param string 用于创建原始值的字符串
   */
  @SuppressWarnings("deprecation") // superclass constructor
  public JsonPrimitive(String string) {
    value = Objects.requireNonNull(string);
  }

  /**
   * 创建包含字符的原始值。由于 JSON 仅支持 String，字符会转换为一个字符的字符串。
   *
   * @param c 用于创建原始值的字符
   */
  // "deprecation" suppression for superclass constructor
  // "UnnecessaryBoxedVariable" Error Prone warning is correct since method does not accept
  // null, but cannot be changed anymore since this is public API
  @SuppressWarnings({"deprecation", "UnnecessaryBoxedVariable"})
  public JsonPrimitive(Character c) {
    // convert characters to strings since in JSON, characters are represented as a single
    // character string
    value = Objects.requireNonNull(c).toString();
  }

  /**
   * 由于原始值不可变，返回相同值。
   *
   * @since 2.8.2
   */
  @Override
  public JsonPrimitive deepCopy() {
    return this;
  }

  /**
   * 检查此原始值是否包含布尔值。
   *
   * @return 若包含布尔值则返回 true，否则返回 false
   */
  public boolean isBoolean() {
    return value instanceof Boolean;
  }

  /**
   * 将此元素作为布尔值获取的便捷方法。若此原始值 {@linkplain #isBoolean() 不是布尔值}，则使用
   * {@link Boolean#parseBoolean(String)} 解析字符串值。即 {@code "true"}（忽略大小写）视为
   * {@code true}，其他任何值视为 {@code false}。
   */
  @Override
  public boolean getAsBoolean() {
    if (isBoolean()) {
      return (Boolean) value;
    }
    // Check to see if the value as a String is "true" in any case.
    return Boolean.parseBoolean(getAsString());
  }

  /**
   * 检查此原始值是否包含 Number。
   *
   * @return 若包含 Number 则返回 true，否则返回 false
   */
  public boolean isNumber() {
    return value instanceof Number;
  }

  /**
   * 将此元素作为 {@link Number} 获取的便捷方法。若此原始值 {@linkplain #isString() 是字符串}，则构造
   * 延迟解析的 {@code Number}，在调用其任何方法时解析字符串（可能导致 {@link NumberFormatException}）。
   *
   * @throws UnsupportedOperationException 若此原始值既不是数字也不是字符串
   */
  @Override
  public Number getAsNumber() {
    if (value instanceof Number) {
      return (Number) value;
    } else if (value instanceof String) {
      return new LazilyParsedNumber((String) value);
    }
    throw new UnsupportedOperationException("Primitive is neither a number nor a string");
  }

  /**
   * 检查此原始值是否包含 String 值。
   *
   * @return 若包含 String 值则返回 true，否则返回 false
   */
  public boolean isString() {
    return value instanceof String;
  }

  // Don't add Javadoc, inherit it from super implementation; no exceptions are thrown here
  @Override
  public String getAsString() {
    if (value instanceof String) {
      return (String) value;
    } else if (isNumber()) {
      return getAsNumber().toString();
    } else if (isBoolean()) {
      return ((Boolean) value).toString();
    }
    throw new AssertionError("Unexpected value type: " + value.getClass());
  }

  /**
   * @throws NumberFormatException {@inheritDoc}
   */
  @Override
  public double getAsDouble() {
    return isNumber() ? getAsNumber().doubleValue() : Double.parseDouble(getAsString());
  }

  /**
   * @throws NumberFormatException {@inheritDoc}
   */
  @Override
  public BigDecimal getAsBigDecimal() {
    return value instanceof BigDecimal
        ? (BigDecimal) value
        : NumberLimits.parseBigDecimal(getAsString());
  }

  /**
   * @throws NumberFormatException {@inheritDoc}
   */
  @Override
  public BigInteger getAsBigInteger() {
    return value instanceof BigInteger
        ? (BigInteger) value
        : isIntegral(this)
            ? BigInteger.valueOf(this.getAsNumber().longValue())
            : NumberLimits.parseBigInteger(this.getAsString());
  }

  /**
   * @throws NumberFormatException {@inheritDoc}
   */
  @Override
  public float getAsFloat() {
    return isNumber() ? getAsNumber().floatValue() : Float.parseFloat(getAsString());
  }

  /**
   * 将此元素作为基本类型 long 获取的便捷方法。
   *
   * @return 此元素的基本类型 long 值
   * @throws NumberFormatException {@inheritDoc}
   */
  @Override
  public long getAsLong() {
    return isNumber() ? getAsNumber().longValue() : Long.parseLong(getAsString());
  }

  /**
   * @throws NumberFormatException {@inheritDoc}
   */
  @Override
  public short getAsShort() {
    return isNumber() ? getAsNumber().shortValue() : Short.parseShort(getAsString());
  }

  /**
   * @throws NumberFormatException {@inheritDoc}
   */
  @Override
  public int getAsInt() {
    return isNumber() ? getAsNumber().intValue() : Integer.parseInt(getAsString());
  }

  /**
   * @throws NumberFormatException {@inheritDoc}
   */
  @Override
  public byte getAsByte() {
    return isNumber() ? getAsNumber().byteValue() : Byte.parseByte(getAsString());
  }

  /**
   * @throws UnsupportedOperationException 若此原始值的字符串值为空
   * @deprecated 此方法具有误导性，因为它并非将此元素作为 char 获取，而是作为字符串的首字符。
   */
  @Deprecated
  @Override
  public char getAsCharacter() {
    String s = getAsString();
    if (s.isEmpty()) {
      throw new UnsupportedOperationException("String value is empty");
    } else {
      return s.charAt(0);
    }
  }

  /** 返回此对象的哈希码。 */
  @Override
  public int hashCode() {
    if (value == null) {
      return 31;
    }
    // Using recommended hashing algorithm from Effective Java for longs and doubles
    if (isIntegral(this)) {
      long value = getAsNumber().longValue();
      return (int) (value ^ (value >>> 32));
    }
    if (value instanceof Number) {
      long value = Double.doubleToLongBits(getAsNumber().doubleValue());
      return (int) (value ^ (value >>> 32));
    }
    return value.hashCode();
  }

  /**
   * 判断另一对象是否与此对象相等。仅当另一对象是 {@code JsonPrimitive} 的实例且具有相等值时，才视为相等。
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    JsonPrimitive other = (JsonPrimitive) obj;
    if (value == null) {
      return other.value == null;
    }
    if (isIntegral(this) && isIntegral(other)) {
      return (this.value instanceof BigInteger || other.value instanceof BigInteger)
          ? this.getAsBigInteger().equals(other.getAsBigInteger())
          : this.getAsNumber().longValue() == other.getAsNumber().longValue();
    }
    if (value instanceof Number && other.value instanceof Number) {
      if (value instanceof BigDecimal && other.value instanceof BigDecimal) {
        // Uses compareTo to ignore scale of values, e.g. `0` and `0.00` should be considered equal
        return this.getAsBigDecimal().compareTo(other.getAsBigDecimal()) == 0;
      }

      double thisAsDouble = this.getAsDouble();
      double otherAsDouble = other.getAsDouble();
      // Don't use Double.compare(double, double) because that considers -0.0 and +0.0 not equal
      return (thisAsDouble == otherAsDouble)
          || (Double.isNaN(thisAsDouble) && Double.isNaN(otherAsDouble));
    }
    return value.equals(other.value);
  }

  /**
   * 若指定数字为整数类型（Long、Integer、Short、Byte、BigInteger）则返回 true。
   */
  private static boolean isIntegral(JsonPrimitive primitive) {
    if (primitive.value instanceof Number) {
      Number number = (Number) primitive.value;
      return number instanceof BigInteger
          || number instanceof Long
          || number instanceof Integer
          || number instanceof Short
          || number instanceof Byte;
    }
    return false;
  }
}
