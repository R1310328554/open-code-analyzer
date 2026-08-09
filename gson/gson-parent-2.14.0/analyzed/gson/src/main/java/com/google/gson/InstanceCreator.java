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
 * 为未定义无参构造函数的类创建实例而实现的接口。若能修改该类，应改为添加私有或公有无参构造函数。
 * 但对于库类（如 JDK 类）或无法获取源码的第三方库，则无法这样做。此时应为该类定义实例创建器。
 * 此接口的实现须在 Gson 能够使用之前通过 {@link GsonBuilder#registerTypeAdapter(Type, Object)}
 * 方法注册。
 *
 * <p>以下示例说明定义 InstanceCreator 可能有用的情况。下面定义的 {@code Id} 类没有默认无参构造函数。
 *
 * <pre>
 * public class Id&lt;T&gt; {
 *   private final Class&lt;T&gt; clazz;
 *   private final long value;
 *   public Id(Class&lt;T&gt; clazz, long value) {
 *     this.clazz = clazz;
 *     this.value = value;
 *   }
 * }
 * </pre>
 *
 * <p>若 Gson 在反序列化过程中遇到 {@code Id} 类型的对象，将抛出异常。最简单的解决方法是添加如下
 * （公有或私有）无参构造函数：
 *
 * <pre>
 * private Id() {
 *   this(Object.class, 0L);
 * }
 * </pre>
 *
 * <p>但假设开发者无法访问 {@code Id} 类的源码，或不想为其定义无参构造函数。开发者可以为 {@code Id}
 * 定义 {@code InstanceCreator} 来解决此问题：
 *
 * <pre>
 * class IdInstanceCreator implements InstanceCreator&lt;Id&gt; {
 *   public Id createInstance(Type type) {
 *     return new Id(Object.class, 0L);
 *   }
 * }
 * </pre>
 *
 * <p>注意，创建实例的字段内容并不重要，因为 Gson 会用 JSON 中指定的反序列化值覆盖它们。还应确保返回
 * <i>新</i> 对象而非共享对象，因为其字段会被覆盖。开发者需要按如下方式将 {@code IdInstanceCreator}
 * 注册到 Gson：
 *
 * <pre>
 * Gson gson = new GsonBuilder().registerTypeAdapter(Id.class, new IdInstanceCreator()).create();
 * </pre>
 *
 * @param <T> 此实现将创建的对象类型
 * @see GsonBuilder#registerTypeAdapter(Type, Object)
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public interface InstanceCreator<T> {

  /**
   * Gson 在反序列化期间调用此回调方法以创建指定类型的实例。返回实例的字段会被 JSON 中的数据覆盖。
   * 由于对象先前内容会被销毁并覆盖，请勿返回在其他地方仍有用的实例。尤其不要返回共享实例，始终使用
   * {@code new} 创建新实例。
   *
   * @param type 以 {@link Type} 表示的参数化 T
   * @return 类型为 T 的默认对象实例
   */
  T createInstance(Type type);
}
