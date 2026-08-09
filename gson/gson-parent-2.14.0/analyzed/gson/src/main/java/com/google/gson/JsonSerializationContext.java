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

import java.lang.reflect.Type;

/**
 * 在调用 {@link JsonSerializer#serialize(Object, Type, JsonSerializationContext)} 方法时，
 * 传递给自定义序列化器的序列化上下文。
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public interface JsonSerializationContext {

  /**
   * 对指定对象调用默认序列化。
   *
   * @param src 需要序列化的对象
   * @return 与 {@code src} 序列化形式对应的 {@link JsonElement} 树
   */
  JsonElement serialize(Object src);

  /**
   * 对指定对象调用默认序列化，并传入具体类型信息。绝不应在 {@link
   * JsonSerializer#serialize(Object, Type, JsonSerializationContext)} 方法接收到的元素上调用此方法，
   * 否则 Gson 会再次调用自定义序列化器，导致无限循环。
   *
   * @param src 需要序列化的对象
   * @param typeOfSrc src 对象的实际泛化类型
   * @return 与 {@code src} 序列化形式对应的 {@link JsonElement} 树
   */
  JsonElement serialize(Object src, Type typeOfSrc);
}
