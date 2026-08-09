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

/**
 * 表示 JSON {@code null} 值的类。
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @since 1.2
 */
public final class JsonNull extends JsonElement {
  /**
   * {@code JsonNull} 的单例实例。
   *
   * @since 1.8
   */
  public static final JsonNull INSTANCE = new JsonNull();

  /**
   * 创建新的 {@code JsonNull} 对象。
   *
   * @deprecated 自 Gson 1.8 起已弃用，请改用 {@link #INSTANCE}。
   */
  @Deprecated
  public JsonNull() {
    // Do nothing
  }

  /**
   * 由于是不可变值，返回同一实例。
   *
   * @since 2.8.2
   */
  @Override
  public JsonNull deepCopy() {
    return INSTANCE;
  }

  /** 所有 {@code JsonNull} 实例因无法区分而具有相同的哈希码。 */
  @Override
  public int hashCode() {
    return JsonNull.class.hashCode();
  }

  /** 所有 {@code JsonNull} 实例均视为相等。 */
  @Override
  public boolean equals(Object other) {
    return other instanceof JsonNull;
  }
}
