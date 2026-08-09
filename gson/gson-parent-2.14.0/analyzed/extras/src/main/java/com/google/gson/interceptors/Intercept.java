/*
 * Copyright (C) 2012 Google Inc.
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

package com.google.gson.interceptors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用此注解为 Gson 处理完成后的类实例指定各类拦截器。例如，可在对象从 JSON 反序列化之后对其进行校验。
 * 以下示例展示了该注解的用法：
 *
 * <p>以下示例展示了该注解的用法：
 *
 * <pre>
 * &#64;Intercept(postDeserialize=UserValidator.class)
 * public class User {
 *   String name;
 *   String password;
 *   String emailAddress;
 * }
 *
 * public class UserValidator implements JsonPostDeserializer&lt;User&gt; {
 *   public void postDeserialize(User user) {
 *     // Do some checks on user
 *     if (user.name == null || user.password == null) {
 *       throw new JsonParseException("name and password are required fields.");
 *     }
 *     if (user.emailAddress == null) {
 *       emailAddress = "unknown"; // assign a default value.
 *     }
 *   }
 * }
 * </pre>
 *
 * @author Inderjeet Singh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Intercept {

  /**
   * 指定在实例反序列化完成后应调用的方法所在类。
   */
  @SuppressWarnings("rawtypes")
  public Class<? extends JsonPostDeserializer> postDeserialize();
}
