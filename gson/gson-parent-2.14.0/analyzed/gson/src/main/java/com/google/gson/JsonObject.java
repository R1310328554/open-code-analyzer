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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.internal.LinkedTreeMap;
import java.util.Map;
import java.util.Set;

/**
 * 表示 JSON 中对象类型的类。对象由名值对组成，其中名称为字符串，值可为任意其他 {@link JsonElement} 类型。
 * 由此可构建以本节点为根的 JsonElement 树。成员元素按添加顺序维护。此类不支持 {@code null} 值。若任何
 * 方法的 value 参数为 {@code null}，将转换为 {@link JsonNull}。
 *
 * <p>{@code JsonObject} 未实现 {@link Map} 接口，但可通过 {@link #asMap()} 获取其 {@code Map} 视图。
 *
 * <p>有关如何将 {@code JsonObject} 及一般任何 {@code JsonElement} 与 JSON 相互转换的详情，请参阅
 * {@link JsonElement} 文档。
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class JsonObject extends JsonElement {
  private final LinkedTreeMap<String, JsonElement> members = new LinkedTreeMap<>(false);

  /** 创建空的 JsonObject。 */
  @SuppressWarnings("deprecation") // superclass constructor
  public JsonObject() {}

  /**
   * 创建此元素及其所有子元素的深拷贝。
   *
   * @since 2.8.2
   */
  @Override
  public JsonObject deepCopy() {
    JsonObject result = new JsonObject();
    for (Map.Entry<String, JsonElement> entry : members.entrySet()) {
      result.add(entry.getKey(), entry.getValue().deepCopy());
    }
    return result;
  }

  /**
   * 向自身添加成员（名值对）。名称必须为 String，值可为任意 {@link JsonElement}，从而允许构建以本节点为根
   * 的完整 JsonElement 树。
   *
   * @param property 成员名称
   * @param value 成员对象
   */
  public void add(String property, JsonElement value) {
    members.put(property, value == null ? JsonNull.INSTANCE : value);
  }

  /**
   * 从此对象中移除 {@code property}。
   *
   * @param property 要移除的成员名称
   * @return 被移除的 {@link JsonElement} 对象，若不存在该名称的成员则返回 {@code null}
   * @since 1.3
   */
  @CanIgnoreReturnValue
  public JsonElement remove(String property) {
    return members.remove(property);
  }

  /**
   * 添加字符串成员的便捷方法。指定值将转换为 String 类型的 {@link JsonPrimitive}。
   *
   * @param property 成员名称
   * @param value 与成员关联的字符串值
   */
  public void addProperty(String property, String value) {
    add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
  }

  /**
   * 添加数字成员的便捷方法。指定值将转换为 Number 类型的 {@link JsonPrimitive}。
   *
   * @param property 成员名称
   * @param value 与成员关联的数字值
   */
  public void addProperty(String property, Number value) {
    add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
  }

  /**
   * 添加布尔成员的便捷方法。指定值将转换为 Boolean 类型的 {@link JsonPrimitive}。
   *
   * @param property 成员名称
   * @param value 与成员关联的布尔值
   */
  public void addProperty(String property, Boolean value) {
    add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
  }

  /**
   * 添加字符成员的便捷方法。指定值将转换为 Character 类型的 {@link JsonPrimitive}。
   *
   * @param property 成员名称
   * @param value 与成员关联的字符值
   */
  public void addProperty(String property, Character value) {
    add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
  }

  /**
   * 返回此对象的成员集合。集合有序，顺序与元素添加顺序一致。
   *
   * @return 此对象的成员集合
   */
  public Set<Map.Entry<String, JsonElement>> entrySet() {
    return members.entrySet();
  }

  /**
   * 返回成员键值集合。
   *
   * @return 成员键的 String 集合
   * @since 2.8.1
   */
  public Set<String> keySet() {
    return members.keySet();
  }

  /**
   * 返回对象中键值对的数量。
   *
   * @return 对象中键值对的数量
   * @since 2.7
   */
  public int size() {
    return members.size();
  }

  /**
   * 若对象中键值对数量为零则返回 true。
   *
   * @return 若对象中键值对数量为零则返回 true
   * @since 2.10.1
   */
  public boolean isEmpty() {
    return members.isEmpty();
  }

  /**
   * 检查此对象中是否存在指定名称的成员。
   *
   * @param memberName 待检查是否存在的成员名称
   * @return 若存在指定名称的成员则返回 true，否则返回 false
   */
  public boolean has(String memberName) {
    return members.containsKey(memberName);
  }

  /**
   * 返回指定名称的成员。
   *
   * @param memberName 请求的成员名称
   * @return 匹配名称的成员，若不存在则返回 {@code null}
   */
  public JsonElement get(String memberName) {
    return members.get(memberName);
  }

  /**
   * 将指定成员作为 {@link JsonPrimitive} 获取的便捷方法。
   *
   * @param memberName 请求的成员名称
   * @return 与指定成员对应的 {@code JsonPrimitive}，若不存在则返回 {@code null}
   * @throws ClassCastException 若成员不是 {@code JsonPrimitive} 类型
   */
  public JsonPrimitive getAsJsonPrimitive(String memberName) {
    return (JsonPrimitive) members.get(memberName);
  }

  /**
   * 将指定成员作为 {@link JsonArray} 获取的便捷方法。
   *
   * @param memberName 请求的成员名称
   * @return 与指定成员对应的 {@code JsonArray}，若不存在则返回 {@code null}
   * @throws ClassCastException 若成员不是 {@code JsonArray} 类型
   */
  public JsonArray getAsJsonArray(String memberName) {
    return (JsonArray) members.get(memberName);
  }

  /**
   * 将指定成员作为 {@link JsonObject} 获取的便捷方法。
   *
   * @param memberName 请求的成员名称
   * @return 与指定成员对应的 {@code JsonObject}，若不存在则返回 {@code null}
   * @throws ClassCastException 若成员不是 {@code JsonObject} 类型
   */
  public JsonObject getAsJsonObject(String memberName) {
    return (JsonObject) members.get(memberName);
  }

  /**
   * 返回此 {@code JsonObject} 的可变 {@link Map} 视图。对 {@code Map} 的修改会反映在此
   * {@code JsonObject} 中，反之亦然。
   *
   * <p>{@code Map} 不允许 {@code null} 键或值。与 {@code JsonObject} 的 {@code null} 处理方式不同，
   * 尝试添加 {@code null} 会抛出 {@link NullPointerException}。JSON null 值请使用 {@link JsonNull}。
   *
   * @return 可变的 {@code Map} 视图
   * @since 2.10
   */
  public Map<String, JsonElement> asMap() {
    // It is safe to expose the underlying map because it disallows null keys and values
    return members;
  }

  /**
   * 判断另一对象是否与此对象相等。仅当另一对象是 {@code JsonObject} 的实例且成员相等（忽略顺序）时，
   * 才视为相等。
   */
  @Override
  public boolean equals(Object o) {
    return (o == this) || (o instanceof JsonObject && ((JsonObject) o).members.equals(members));
  }

  /**
   * 返回此对象的哈希码。此方法根据此对象的成员计算哈希码，忽略顺序。
   */
  @Override
  public int hashCode() {
    return members.hashCode();
  }
}
