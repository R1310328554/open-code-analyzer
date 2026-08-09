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

import com.google.gson.annotations.SerializedName;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * 为 Gson 提供自定义字段命名的机制。这使客户端代码能将字段名转换为普通 Java 字段声明规则所不支持的特定约定。
 * 例如，Java 不支持字段名中包含 "-" 字符。
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @since 1.3
 */
public interface FieldNamingStrategy {

  /**
   * 将字段名转换为其 JSON 字段名表示形式。
   *
   * @param f 待转换的字段对象
   * @return 转换后的字段名
   * @since 1.3
   */
  String translateName(Field f);

  /**
   * 返回此字段在反序列化时使用的备用名称。与 {@link SerializedName#alternate()} 类似。
   *
   * @param f 字段对象
   * @return 备用字段名列表
   * @since 2.13.1
   */
  default List<String> alternateNames(Field f) {
    return Collections.emptyList();
  }
}
