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
 * JSON 读取器或编写器中的词法作用域元素。
 * @author Jesse Wilson
 * @since 1.6
 */
final class JsonScope {
  private JsonScope() {}

  /** 空数组：下一元素前无需分隔符。 */
  static final int EMPTY_ARRAY = 1;

  /** 非空数组：下一元素前需要分隔符。 */
  static final int NONEMPTY_ARRAY = 2;

  /** 空对象：下一元素前无需分隔符。 */
  static final int EMPTY_OBJECT = 3;

  /** 对象最近元素为键，下一元素必须是值。 */
  static final int DANGLING_NAME = 4;

  /** 非空对象：下一元素前需要分隔符。 */
  static final int NONEMPTY_OBJECT = 5;

  /** 尚未开始顶层值。 */
  static final int EMPTY_DOCUMENT = 6;

  /** 已开始顶层值。 */
  static final int NONEMPTY_DOCUMENT = 7;

  /** 文档已关闭，不可再访问。 */
  static final int CLOSED = 8;
}
