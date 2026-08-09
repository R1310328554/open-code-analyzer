/*
 * Copyright (C) 2010 Google Inc.
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
 * Gson 读取或写入格式错误 JSON 元素时抛出。
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class JsonSyntaxException extends JsonParseException {

  private static final long serialVersionUID = 1L;

  public JsonSyntaxException(String msg) {
    super(msg);
  }

  public JsonSyntaxException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * @param cause 根异常；若能描述实际情况建议用带消息的构造器
   */
  public JsonSyntaxException(Throwable cause) {
    super(cause);
  }
}
