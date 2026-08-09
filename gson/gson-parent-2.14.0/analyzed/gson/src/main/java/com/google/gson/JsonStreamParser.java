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

import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 允许从指定 reader 异步读取多个 {@link JsonElement} 的流式解析器。JSON 数据以宽松模式解析，
 * 另见 {@link JsonReader#setStrictness(Strictness)}。
 *
 * <p>此类在特定条件下是线程安全的（参见《Effective Java》第二版第 70 条）。要在多线程间正确使用此类，
 * 需要添加外部同步。例如：
 *
 * <pre>
 * JsonStreamParser parser = new JsonStreamParser("['first'] {'second':10} 'third'");
 * JsonElement element;
 * synchronized (parser) {  // synchronize on an object shared by threads
 *   if (parser.hasNext()) {
 *     element = parser.next();
 *   }
 * }
 * </pre>
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @since 1.4
 */
public final class JsonStreamParser implements Iterator<JsonElement> {
  private final JsonReader parser;
  private final Object lock;

  /**
   * @param json 包含彼此拼接的 JSON 元素的字符串
   * @since 1.4
   */
  public JsonStreamParser(String json) {
    this(new StringReader(json));
  }

  /**
   * @param reader 包含彼此拼接的 JSON 元素的数据流
   * @since 1.4
   */
  public JsonStreamParser(Reader reader) {
    parser = new JsonReader(reader);
    parser.setStrictness(Strictness.LENIENT);
    lock = new Object();
  }

  /**
   * 返回 reader 上下一个可用的 {@link JsonElement}。若无可用元素则抛出 {@link
   * NoSuchElementException}。
   *
   * @return reader 上下一个可用的 {@code JsonElement}
   * @throws JsonParseException 若输入流为格式错误的 JSON
   * @throws NoSuchElementException 若无可用的 {@code JsonElement}
   * @since 1.4
   */
  @Override
  public JsonElement next() throws JsonParseException {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    try {
      return Streams.parse(parser);
    } catch (StackOverflowError | OutOfMemoryError e) {
      throw new JsonParseException("Failed parsing JSON source to Json", e);
    }
  }

  /**
   * 若输入上有可消费的 {@link JsonElement} 则返回 true。
   *
   * @return 若输入上有可消费的 {@link JsonElement} 则返回 true，否则返回 false
   * @throws JsonParseException 若输入流为格式错误的 JSON
   * @since 1.4
   */
  @Override
  public boolean hasNext() {
    synchronized (lock) {
      try {
        return parser.peek() != JsonToken.END_DOCUMENT;
      } catch (MalformedJsonException e) {
        throw new JsonSyntaxException(e);
      } catch (IOException e) {
        throw new JsonIOException(e);
      }
    }
  }

  /**
   * 此可选的 {@link Iterator} 方法对流式解析不适用，因此未实现。
   *
   * @since 1.4
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
