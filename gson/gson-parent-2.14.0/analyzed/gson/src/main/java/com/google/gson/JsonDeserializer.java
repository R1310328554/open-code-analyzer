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
 * 表示 JSON 自定义反序列化器的接口。若对 Gson 的默认反序列化不满意，应编写自定义反序列化器，并通过
 * {@link GsonBuilder#registerTypeAdapter(Type, Object)} 注册。
 *
 * <p>以下示例说明定义反序列化器可能有用的情况。下面定义的 {@code Id} 类有两个字段：{@code clazz}
 * 和 {@code value}。
 *
 * <pre>
 * public class Id&lt;T&gt; {
 *   private final Class&lt;T&gt; clazz;
 *   private final long value;
 *   public Id(Class&lt;T&gt; clazz, long value) {
 *     this.clazz = clazz;
 *     this.value = value;
 *   }
 *   public long getValue() {
 *     return value;
 *   }
 * }
 * </pre>
 *
 * <p>{@code Id(com.foo.MyObject.class, 20L)} 的默认反序列化要求 JSON 字符串为
 * <code>{"clazz":"com.foo.MyObject","value":20}</code>。假设您已知 {@code Id} 将反序列化到的字段类型，
 * 因此只想从 JSON 字符串 {@code 20} 反序列化。可通过编写自定义反序列化器实现：
 *
 * <pre>
 * class IdDeserializer implements JsonDeserializer&lt;Id&gt; {
 *   public Id deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
 *       throws JsonParseException {
 *     long idValue = json.getAsJsonPrimitive().getAsLong();
 *     return new Id((Class) typeOfT, idValue);
 *   }
 * }
 * </pre>
 *
 * <p>还需按如下方式将 {@code IdDeserializer} 注册到 Gson：
 *
 * <pre>
 * Gson gson = new GsonBuilder().registerTypeAdapter(Id.class, new IdDeserializer()).create();
 * </pre>
 *
 * <p>反序列化器应无状态且线程安全，否则 {@link Gson} 的线程安全保证可能不适用。
 *
 * <p>新应用应优先使用 {@link TypeAdapter}，其流式 API 比此接口的树 API 更高效。
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @param <T> 注册反序列化器的类型。反序列化器可能被要求反序列化 T 的特定泛型类型。
 */
public interface JsonDeserializer<T> {

  /**
   * Gson 在反序列化过程中遇到指定类型字段时调用此回调方法。
   *
   * <p>在此回调方法的实现中，应考虑调用 {@link
   * JsonDeserializationContext#deserialize(JsonElement, Type)} 方法为返回对象的任何非平凡字段创建对象。
   * 但绝不应在同一类型上传入 {@code json} 调用它，否则会导致无限循环（Gson 会再次调用您的回调方法）。
   *
   * @param json 正在反序列化的 JSON 数据
   * @param typeOfT 要反序列化到的对象类型
   * @return 指定类型 typeOfT 的反序列化对象，为 {@code T} 的子类
   * @throws JsonParseException 若 json 不符合 {@code typeOfT} 的预期格式
   */
  T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException;
}
