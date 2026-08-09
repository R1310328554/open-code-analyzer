/*
 * Copyright (C) 2009 Google Inc.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * 存储字段属性的数据对象。
 *
 * <p>此类不可变，因此可在线程间安全共享。
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @since 1.4
 */
public final class FieldAttributes {
  private final Field field;

  /**
   * 从 {@code f} 构造字段属性对象。
   *
   * @param f 用于提取属性的字段
   */
  public FieldAttributes(Field f) {
    this.field = Objects.requireNonNull(f);
  }

  /**
   * 获取包含此字段的声明类。
   *
   * @return 包含此字段的声明类
   */
  public Class<?> getDeclaringClass() {
    return field.getDeclaringClass();
  }

  /**
   * 获取字段名称。
   *
   * @return 字段名称
   */
  public String getName() {
    return field.getName();
  }

  /**
   * 返回字段声明的泛型类型。
   *
   * <p>例如，假设以下类定义：
   *
   * <pre class="code">
   * public class Foo {
   *   private String bar;
   *   private List&lt;String&gt; red;
   * }
   *
   * Type listParameterizedType = new TypeToken&lt;List&lt;String&gt;&gt;() {}.getType();
   * </pre>
   *
   * <p>对于 {@code bar} 字段，此方法返回 {@code String.class}；对于 {@code red} 字段，返回
   * {@code listParameterizedType}。
   *
   * @return 此字段声明的具体类型
   */
  public Type getDeclaredType() {
    return field.getGenericType();
  }

  /**
   * 返回为此字段声明的 {@code Class} 对象。
   *
   * <p>例如，假设以下类定义：
   *
   * <pre class="code">
   * public class Foo {
   *   private String bar;
   *   private List&lt;String&gt; red;
   * }
   * </pre>
   *
   * <p>对于 {@code bar} 字段，此方法返回 {@code String.class}；对于 {@code red} 字段，返回
   * {@code List.class}。
   *
   * @return 为此字段声明的具体类对象
   */
  public Class<?> getDeclaredClass() {
    return field.getType();
  }

  /**
   * 若此字段上存在 {@code T} 注解则返回该注解对象，否则返回 {@code null}。
   *
   * @param annotation 要获取的注解类
   * @return 若注解绑定到该字段则返回注解实例，否则返回 {@code null}
   */
  public <T extends Annotation> T getAnnotation(Class<T> annotation) {
    return field.getAnnotation(annotation);
  }

  /**
   * 返回此字段上的所有注解。
   *
   * @return 字段上所有注解的数组
   * @since 1.4
   */
  public Collection<Annotation> getAnnotations() {
    return Arrays.asList(field.getAnnotations());
  }

  /**
   * 若字段以 {@code modifier} 修饰符定义则返回 {@code true}。
   *
   * <p>此方法应按如下方式调用：
   *
   * <pre class="code">
   * boolean hasPublicModifier = fieldAttribute.hasModifier(java.lang.reflect.Modifier.PUBLIC);
   * </pre>
   *
   * @see java.lang.reflect.Modifier
   */
  public boolean hasModifier(int modifier) {
    return (field.getModifiers() & modifier) != 0;
  }

  @Override
  public String toString() {
    return field.toString();
  }
}
