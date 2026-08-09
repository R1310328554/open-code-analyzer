/*
 * Copyright (C) 2009 Google Inc.
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

import com.google.gson.internal.bind.TypeAdapters;

/**
 * 定义序列化时 {@code long}/{@code Long} 的预期输出格式。
 *
 * @since 1.3
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public enum LongSerializationPolicy {
  /**
   * 默认策略：{@code Long} 输出为 JSON 数字，例如 {@code {"f":123}}。
   * {@code null} 序列化为 {@link JsonNull}。
   */
  DEFAULT() {
    @Override
    public JsonElement serialize(Long value) {
      if (value == null) {
        return JsonNull.INSTANCE;
      }
      return new JsonPrimitive(value);
    }

    @Override
    TypeAdapter<Number> typeAdapter() {
      return TypeAdapters.LONG;
    }
  },

  /**
   * 将 long 序列化为带引号字符串，例如 {@code {"f":"123"}}。
   * {@code null} 序列化为 {@link JsonNull}。
   */
  STRING() {
    @Override
    public JsonElement serialize(Long value) {
      if (value == null) {
        return JsonNull.INSTANCE;
      }
      return new JsonPrimitive(value.toString());
    }

    @Override
    TypeAdapter<Number> typeAdapter() {
      return TypeAdapters.LONG_AS_STRING;
    }
  };

  /**
   * 使用此策略序列化 {@code value}。
   *
   * @param value 待序列化的 long 值
   * @return 序列化后的 {@link JsonElement}
   */
  public abstract JsonElement serialize(Long value);

  /** 返回与此策略对应的 {@link TypeAdapter}。 */
  abstract TypeAdapter<Number> typeAdapter();
}
