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
 * 表示 JSON 自定义序列化器的接口。若对 Gson 的默认序列化不满意，应编写自定义序列化器，并通过
 * {@link com.google.gson.GsonBuilder#registerTypeAdapter(Type, Object)} 注册。
 *
 * <p>以下示例说明定义序列化器可能有用的情况。下面定义的 {@code Id} 类有两个字段：{@code clazz}
 * 和 {@code value}。
 *
 * <pre>
 * public class Id&lt;T&gt; {
 *   private final Class&lt;T&gt; clazz;
 *   private final long value;
 *
 *   public Id(Class&lt;T&gt; clazz, long value) {
 *     this.clazz = clazz;
 *     this.value = value;
 *   }
 *
 *   public long getValue() {
 *     return value;
 *   }
 * }
 * </pre>
 *
 * <p>{@code Id(com.foo.MyObject.class, 20L)} 的默认序列化结果为 <code>
 * {"clazz":"com.foo.MyObject","value":20}</code>。假设您只希望输出值为 {@code 20}。可通过编写自定义
 * 序列化器实现：
 *
 * <pre>
 * class IdSerializer implements JsonSerializer&lt;Id&gt; {
 *   public JsonElement serialize(Id id, Type typeOfId, JsonSerializationContext context) {
 *     return new JsonPrimitive(id.getValue());
 *   }
 * }
 * </pre>
 *
 * <p>还需按如下方式将 {@code IdSerializer} 注册到 Gson：
 *
 * <pre>
 * Gson gson = new GsonBuilder().registerTypeAdapter(Id.class, new IdSerializer()).create();
 * </pre>
 *
 * <p>序列化器应无状态且线程安全，否则 {@link Gson} 的线程安全保证可能不适用。
 *
 * <p>新应用应优先使用 {@link TypeAdapter}，其流式 API 比此接口的树 API 更高效。
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @param <T> 注册序列化器的类型。序列化器可能被要求序列化 T 的特定泛型类型。
 */
public interface JsonSerializer<T> {

  /**
   * Gson 在序列化过程中遇到指定类型字段时调用此回调方法。
   *
   * <p>在此回调方法的实现中，应考虑调用 {@link
   * JsonSerializationContext#serialize(Object, Type)} 方法为 {@code src} 对象的任何非平凡字段创建
   * JsonElement。但绝不应在 {@code src} 对象本身上调用它，否则会导致无限循环（Gson 会再次调用您的
   * 回调方法）。
   *
   * @param src 需要转换为 JSON 的对象
   * @param typeOfSrc 源对象的实际类型（完整泛化版本）
   * @return 与指定对象对应的 JsonElement
   */
  JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context);
}
