/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.stream;

/**
 * JSON 编码字符串中的结构、名称或值类型。
 * @author Jesse Wilson
 * @since 1.6
 */
public enum JsonToken {

  /**
   * JSON 数组的打开。使用 {@link JsonWriter#beginArray} 编写并使用 {@link JsonReader#beginArray} 读取。
   */
  BEGIN_ARRAY,

  /**
   * JSON 数组的结束。使用 {@link JsonWriter#endArray} 编写并使用 {@link JsonReader#endArray} 读取。
   */
  END_ARRAY,

  /**
   * 打开一个 JSON 对象。使用 {@link JsonWriter#beginObject} 编写并使用 {@link JsonReader#beginObject} 读取。
   */
  BEGIN_OBJECT,

  /**
   * JSON 对象的结束。使用 {@link JsonWriter#endObject} 编写并使用 {@link JsonReader#endObject} 读取。
   */
  END_OBJECT,

  /**
   * JSON 属性名称。在对象内，标记在名称及其值之间交替。使用 {@link JsonWriter#name} 编写并使用 {@link
   * JsonReader#nextName} 读取
   */
  NAME,

  /** JSON 字符串。 */
  STRING,

  /**
   * 此 API 中由 Java {@code double}、{@code long} 或 {@code int} 表示的 JSON 编号。
   */
  NUMBER,

  /** JSON 布尔值 {@code true} 或 {@code false}。 */
  BOOLEAN,

  /** JSON {@code null}。 */
  NULL,

  /**
   * JSON 流的结尾。此标记值由 {@link JsonReader#peek()} 返回，以表明 JSON 编码值不再有标记。
   */
  END_DOCUMENT
}
