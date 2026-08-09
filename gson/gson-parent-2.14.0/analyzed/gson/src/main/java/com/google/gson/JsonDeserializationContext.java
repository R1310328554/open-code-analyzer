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
 * 在调用 {@link JsonDeserializer#deserialize(JsonElement, Type, JsonDeserializationContext)} 方法时，
 * 传递给自定义反序列化器的反序列化上下文。
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public interface JsonDeserializationContext {

  /**
   * 对指定对象调用默认反序列化。绝不应在 {@link JsonDeserializer#deserialize(JsonElement, Type,
   * JsonDeserializationContext)} 方法接收到的元素上调用此方法，否则 Gson 会再次调用自定义反序列化器，
   * 导致无限循环。
   *
   * @param json 解析树
   * @param typeOfT 期望返回值的类型
   * @param <T> 反序列化对象的类型
   * @return 类型为 typeOfT 的对象
   * @throws JsonParseException 若解析树不包含预期数据
   */
  @SuppressWarnings("TypeParameterUnusedInFormals")
  <T> T deserialize(JsonElement json, Type typeOfT) throws JsonParseException;
}
