/*
 * Copyright (C) 2021 Google Inc.
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
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * 定义两种标准数字读取策略，以及克服 Gson 将数字反序列化为 {@link Object}/{@link Number} 时历史局限的策略。
 *
 * @see ToNumberStrategy
 * @since 2.8.9
 */
public enum ToNumberPolicy implements ToNumberStrategy {

  /** 数字以 {@link Double} 读取；反序列化为 {@link Object} 时的默认策略。 */
  DOUBLE {
    @Override
    public Double readNumber(JsonReader in) throws IOException {
      return in.nextDouble();
    }
  },

  /** 以字符串支撑的延迟解析数字读取；反序列化为 {@link Number} 时的默认策略。 */
  LAZILY_PARSED_NUMBER {
    @Override
    public Number readNumber(JsonReader in) throws IOException {
      return new LazilyParsedNumber(in.nextString());
    }
  },

  /** 根据 JSON 表示读取为 {@link Long} 或 {@link Double}；非宽松模式下拒绝无穷/NaN。 */
  LONG_OR_DOUBLE {
    @Override
    public Number readNumber(JsonReader in) throws IOException, JsonParseException {
      String value = in.nextString();
      if (value.indexOf('.') >= 0) {
        return parseAsDouble(value, in);
      } else {
        try {
          return Long.parseLong(value);
        } catch (NumberFormatException e) {
          return parseAsDouble(value, in);
        }
      }
    }

    private Number parseAsDouble(String value, JsonReader in) throws IOException {
      try {
        Double d = Double.valueOf(value);
        if ((d.isInfinite() || d.isNaN()) && !in.isLenient()) {
          throw new MalformedJsonException(
              "JSON forbids NaN and infinities: " + d + "; at path " + in.getPreviousPath());
        }
        return d;
      } catch (NumberFormatException e) {
        throw new JsonParseException(
            "Cannot parse " + value + "; at path " + in.getPreviousPath(), e);
      }
    }
  },

  /** 以 {@link BigDecimal} 读取任意长度数字。 */
  BIG_DECIMAL {
    @Override
    public BigDecimal readNumber(JsonReader in) throws IOException {
      String value = in.nextString();
      try {
        return NumberLimits.parseBigDecimal(value);
      } catch (NumberFormatException e) {
        throw new JsonParseException(
            "Cannot parse " + value + "; at path " + in.getPreviousPath(), e);
      }
    }
  }
}
