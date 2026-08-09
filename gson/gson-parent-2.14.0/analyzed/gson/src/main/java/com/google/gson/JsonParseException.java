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
 * 在解析 JSON 字符串过程中出现严重问题时抛出此异常。Gson 基础设施的主要用途之一即为此类。
 * 若传入的 JSON 格式错误或存在恶意内容，将抛出此异常的实例。
 *
 * <p>此异常为 {@link RuntimeException}，因为它会暴露给客户端。使用 {@link RuntimeException}
 * 可避免客户端的不良编码实践（捕获异常后什么都不做）。通常，遇到解析错误时希望直接失败
 * （即客户端往往不知道如何从 {@link JsonParseException} 中恢复）。
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class JsonParseException extends RuntimeException {
  static final long serialVersionUID = -4086729973971783390L;

  /**
   * 使用指定消息创建异常。若正在包装另一异常，请考虑改用 {@link #JsonParseException(String,
   * Throwable)}。
   *
   * @param msg 描述此异常可能原因的错误消息
   */
  public JsonParseException(String msg) {
    super(msg);
  }

  /**
   * 使用指定消息和原因创建异常。
   *
   * @param msg 描述发生了什么的错误消息
   * @param cause 导致此异常抛出的根异常
   */
  public JsonParseException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * 使用指定原因创建异常。若能描述发生了什么，请考虑改用 {@link #JsonParseException(String,
   * Throwable)}。
   *
   * @param cause 导致此异常抛出的根异常
   */
  public JsonParseException(Throwable cause) {
    super(cause);
  }
}
