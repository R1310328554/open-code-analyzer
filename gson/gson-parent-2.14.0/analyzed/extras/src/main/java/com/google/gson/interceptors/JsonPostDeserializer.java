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

import com.google.gson.InstanceCreator;

/**
 * 由希望在对象反序列化后检查或修改该对象的类实现此接口。此类必须定义无参构造函数，或为其注册
 * {@link InstanceCreator}。
 *
 * @author Inderjeet Singh
 */
public interface JsonPostDeserializer<T> {

  /** Gson 在对象从 JSON 反序列化完成后调用此方法。 */
  public void postDeserialize(T object);
}
