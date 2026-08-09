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
 * 当 Gson 无法读取输入流或写入输出流时抛出此异常。
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
@SuppressWarnings("MemberName") // class name is part of the public API
public final class JsonIOException extends JsonParseException {
  private static final long serialVersionUID = 1L;

  public JsonIOException(String msg) {
    super(msg);
  }

  public JsonIOException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * 使用指定原因创建异常。若能描述发生了什么，请考虑改用 {@link #JsonIOException(String,
   * Throwable)}。
   *
   * @param cause 导致此异常抛出的根异常
   */
  public JsonIOException(Throwable cause) {
    super(cause);
  }
}
