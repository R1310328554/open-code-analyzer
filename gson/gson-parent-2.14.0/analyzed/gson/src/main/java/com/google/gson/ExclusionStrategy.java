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
 * 用于决定某个字段或类是否应作为 JSON 输出/输入的一部分进行序列化或反序列化的策略（或规则）定义。
 *
 * <p>以下是展示如何使用此排除机制的若干示例。
 *
 * <p><strong>根据特定类类型排除字段和对象：</strong>
 *
 * <pre class="code">
 * private static class SpecificClassExclusionStrategy implements ExclusionStrategy {
 *   private final Class&lt;?&gt; excludedThisClass;
 *
 *   public SpecificClassExclusionStrategy(Class&lt;?&gt; excludedThisClass) {
 *     this.excludedThisClass = excludedThisClass;
 *   }
 *
 *   public boolean shouldSkipClass(Class&lt;?&gt; clazz) {
 *     return excludedThisClass.equals(clazz);
 *   }
 *
 *   public boolean shouldSkipField(FieldAttributes f) {
 *     return excludedThisClass.equals(f.getDeclaredClass());
 *   }
 * }
 * </pre>
 *
 * <p><strong>根据特定注解排除字段和对象：</strong>
 *
 * <pre class="code">
 * public &#64;interface FooAnnotation {
 *   // some implementation here
 * }
 *
 * // Excludes any field (or class) that is tagged with an "&#64;FooAnnotation"
 * private static class FooAnnotationExclusionStrategy implements ExclusionStrategy {
 *   public boolean shouldSkipClass(Class&lt;?&gt; clazz) {
 *     return clazz.getAnnotation(FooAnnotation.class) != null;
 *   }
 *
 *   public boolean shouldSkipField(FieldAttributes f) {
 *     return f.getAnnotation(FooAnnotation.class) != null;
 *   }
 * }
 * </pre>
 *
 * <p>若要为 {@code Gson} 配置用户自定义的排除策略，需要使用 {@code GsonBuilder}。以下示例展示如何通过
 * {@code GsonBuilder} 为 Gson 配置上述示例之一：
 *
 * <pre class="code">
 * ExclusionStrategy excludeStrings = new UserDefinedExclusionStrategy(String.class);
 * Gson gson = new GsonBuilder()
 *     .setExclusionStrategies(excludeStrings)
 *     .create();
 * </pre>
 *
 * <p>对于某些模型类，您可能只想序列化某个字段，但在反序列化时排除它。为此，可以照常编写
 * {@code ExclusionStrategy}，但通过 {@link
 * GsonBuilder#addDeserializationExclusionStrategy(ExclusionStrategy)} 方法注册。例如：
 *
 * <pre class="code">
 * ExclusionStrategy excludeStrings = new UserDefinedExclusionStrategy(String.class);
 * Gson gson = new GsonBuilder()
 *     .addDeserializationExclusionStrategy(excludeStrings)
 *     .create();
 * </pre>
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @see GsonBuilder#setExclusionStrategies(ExclusionStrategy...)
 * @see GsonBuilder#addDeserializationExclusionStrategy(ExclusionStrategy)
 * @see GsonBuilder#addSerializationExclusionStrategy(ExclusionStrategy)
 * @since 1.4
 */
public interface ExclusionStrategy {

  /**
   * 决定在序列化或反序列化过程中是否应跳过某字段。
   *
   * @param f 待判断的字段对象
   * @return 若应忽略该字段则返回 true，否则返回 false
   */
  boolean shouldSkipField(FieldAttributes f);

  /**
   * 决定某类是否应被序列化或反序列化。
   *
   * @param clazz 待判断的类对象
   * @return 若应忽略该类则返回 true，否则返回 false
   */
  boolean shouldSkipClass(Class<?> clazz);
}
