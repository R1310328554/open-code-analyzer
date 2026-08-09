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

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * 定义 JSON 字段名称若干标准命名约定的枚举。此枚举应与 {@link com.google.gson.GsonBuilder} 配合使用，
 * 以配置 {@link com.google.gson.Gson} 实例，将 Java 字段名正确转换为所需的 JSON 字段名。
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public enum FieldNamingPolicy implements FieldNamingStrategy {

  /** 在 Gson 中使用此命名策略可确保字段名保持不变。 */
  IDENTITY() {
    @Override
    public String translateName(Field f) {
      return f.getName();
    }
  },

  /**
   * 在 Gson 中使用此命名策略可确保 Java 字段名的首"字母"在序列化为 JSON 形式时大写。
   *
   * <p>以下是"Java 字段名" ---&gt; "JSON 字段名"形式的若干示例：
   *
   * <ul>
   *   <li>someFieldName ---&gt; SomeFieldName
   *   <li>_someFieldName ---&gt; _SomeFieldName
   * </ul>
   */
  UPPER_CAMEL_CASE() {
    @Override
    public String translateName(Field f) {
      return upperCaseFirstLetter(f.getName());
    }
  },

  /**
   * 在 Gson 中使用此命名策略可确保 Java 字段名的首"字母"在序列化为 JSON 形式时大写，且单词之间以空格分隔。
   *
   * <p>以下是"Java 字段名" ---&gt; "JSON 字段名"形式的若干示例：
   *
   * <ul>
   *   <li>someFieldName ---&gt; Some Field Name
   *   <li>_someFieldName ---&gt; _Some Field Name
   * </ul>
   *
   * @since 1.4
   */
  UPPER_CAMEL_CASE_WITH_SPACES() {
    @Override
    public String translateName(Field f) {
      return upperCaseFirstLetter(separateCamelCase(f.getName(), ' '));
    }
  },

  /**
   * 在 Gson 中使用此命名策略可将 Java 字段名从驼峰形式转换为全大写字段名，单词之间以下划线（_）分隔。
   *
   * <p>以下是"Java 字段名" ---&gt; "JSON 字段名"形式的若干示例：
   *
   * <ul>
   *   <li>someFieldName ---&gt; SOME_FIELD_NAME
   *   <li>_someFieldName ---&gt; _SOME_FIELD_NAME
   *   <li>aStringField ---&gt; A_STRING_FIELD
   *   <li>aURL ---&gt; A_U_R_L
   * </ul>
   *
   * @since 2.9.0
   */
  UPPER_CASE_WITH_UNDERSCORES() {
    @Override
    public String translateName(Field f) {
      return separateCamelCase(f.getName(), '_').toUpperCase(Locale.ENGLISH);
    }
  },

  /**
   * 在 Gson 中使用此命名策略可将 Java 字段名从驼峰形式转换为全小写字段名，单词之间以下划线（_）分隔。
   *
   * <p>以下是"Java 字段名" ---&gt; "JSON 字段名"形式的若干示例：
   *
   * <ul>
   *   <li>someFieldName ---&gt; some_field_name
   *   <li>_someFieldName ---&gt; _some_field_name
   *   <li>aStringField ---&gt; a_string_field
   *   <li>aURL ---&gt; a_u_r_l
   * </ul>
   */
  LOWER_CASE_WITH_UNDERSCORES() {
    @Override
    public String translateName(Field f) {
      return separateCamelCase(f.getName(), '_').toLowerCase(Locale.ENGLISH);
    }
  },

  /**
   * 在 Gson 中使用此命名策略可将 Java 字段名从驼峰形式转换为全小写字段名，单词之间以连字符（-）分隔。
   *
   * <p>以下是"Java 字段名" ---&gt; "JSON 字段名"形式的若干示例：
   *
   * <ul>
   *   <li>someFieldName ---&gt; some-field-name
   *   <li>_someFieldName ---&gt; _some-field-name
   *   <li>aStringField ---&gt; a-string-field
   *   <li>aURL ---&gt; a-u-r-l
   * </ul>
   *
   * 不建议在 JavaScript 中使用连字符，因为连字符在表达式中也用作减号。因此，以连字符命名的字段必须始终
   * 作为带引号的属性访问，如 {@code myobject['my-field']}。以对象字段方式访问 {@code
   * myobject.my-field} 将导致非预期的 JavaScript 表达式。
   *
   * @since 1.4
   */
  LOWER_CASE_WITH_DASHES() {
    @Override
    public String translateName(Field f) {
      return separateCamelCase(f.getName(), '-').toLowerCase(Locale.ENGLISH);
    }
  },

  /**
   * 在 Gson 中使用此命名策略可将 Java 字段名从驼峰形式转换为全小写字段名，单词之间以点（.）分隔。
   *
   * <p>以下是"Java 字段名" ---&gt; "JSON 字段名"形式的若干示例：
   *
   * <ul>
   *   <li>someFieldName ---&gt; some.field.name
   *   <li>_someFieldName ---&gt; _some.field.name
   *   <li>aStringField ---&gt; a.string.field
   *   <li>aURL ---&gt; a.u.r.l
   * </ul>
   *
   * 不建议在 JavaScript 中使用点，因为点在表达式中也用作成员访问符。因此，以点命名的字段必须始终作为
   * 带引号的属性访问，如 {@code myobject['my.field']}。以对象字段方式访问 {@code myobject.my.field}
   * 将导致非预期的 JavaScript 表达式。
   *
   * @since 2.8.4
   */
  LOWER_CASE_WITH_DOTS() {
    @Override
    public String translateName(Field f) {
      return separateCamelCase(f.getName(), '.').toLowerCase(Locale.ENGLISH);
    }
  };

  /**
   * 将使用驼峰命名定义单词分隔的字段名转换为以指定 {@code separator} 分隔的独立单词。
   */
  static String separateCamelCase(String name, char separator) {
    StringBuilder translation = new StringBuilder();
    for (int i = 0, length = name.length(); i < length; i++) {
      char character = name.charAt(i);
      if (Character.isUpperCase(character) && translation.length() != 0) {
        translation.append(separator);
      }
      translation.append(character);
    }
    return translation.toString();
  }

  /** 确保 JSON 字段名以大写字母开头。 */
  static String upperCaseFirstLetter(String s) {
    int length = s.length();
    for (int i = 0; i < length; i++) {
      char c = s.charAt(i);
      if (Character.isLetter(c)) {
        if (Character.isUpperCase(c)) {
          return s;
        }

        char uppercased = Character.toUpperCase(c);
        // For leading letter only need one substring
        if (i == 0) {
          return uppercased + s.substring(1);
        } else {
          return s.substring(0, i) + uppercased + s.substring(i + 1);
        }
      }
    }

    return s;
  }
}
