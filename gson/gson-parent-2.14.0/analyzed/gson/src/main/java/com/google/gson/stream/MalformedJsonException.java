/*
 * Copyright (C) 2010 Google Inc.
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

package com.google.gson.stream;

import com.google.gson.Strictness;
import java.io.IOException;

/**
 * 当读者遇到格式错误的 JSON 时抛出。通过将 {@link Strictness#LENIENT} 用于 {@link
 * JsonReader#setStrictness(Strictness)} 可以忽略某些语法错误。
 */
public final class MalformedJsonException extends IOException {
  private static final long serialVersionUID = 1L;

  public MalformedJsonException(String msg) {
    super(msg);
  }

  public MalformedJsonException(String msg, Throwable throwable) {
    super(msg, throwable);
  }

  public MalformedJsonException(Throwable throwable) {
    super(throwable);
  }
}
