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

import com.google.gson.stream.JsonReader;
import java.io.IOException;

/**
 * 在反序列化 {@link Object} 和 {@link Number} 且具体数字类型事先未知时，控制数字如何反序列化的策略。
 *
 * @see ToNumberPolicy
 * @see GsonBuilder#setObjectToNumberStrategy(ToNumberStrategy)
 * @see GsonBuilder#setNumberToNumberStrategy(ToNumberStrategy)
 * @since 2.8.9
 */
public interface ToNumberStrategy {

  /**
   * 从 JSON 读取器读取一个数字。策略应只读取一个值，且不为 {@code null}。
   *
   * @param in JSON 读取器
   * @return 读取的数字
   */
  Number readNumber(JsonReader in) throws IOException;
}
