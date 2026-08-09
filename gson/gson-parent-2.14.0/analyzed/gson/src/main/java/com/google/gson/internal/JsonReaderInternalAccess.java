/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal;

import com.google.gson.stream.JsonReader;
import java.io.IOException;

/** Gson 内部专用的 {@link JsonReader} 内部 API。 */
public abstract class JsonReaderInternalAccess {
  @SuppressWarnings({"ConstantField", "NonFinalStaticField"})
  public static volatile JsonReaderInternalAccess INSTANCE;

  /** 将当前属性名 token 提升为字符串值。 */
  public abstract void promoteNameToValue(JsonReader reader) throws IOException;
}
