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

import com.google.errorprone.annotations.InlineMe;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * 将 JSON 解析为 {@link JsonElement} 解析树的解析器。
 *
 * <p>JSON 数据以 {@linkplain JsonReader#setStrictness(Strictness) 宽松模式} 解析。
 *
 * <p>以下示例展示如何从字符串解析：
 *
 * <pre>
 * String json = "{\"key\": \"value\"}";
 * JsonElement jsonElement = JsonParser.parseString(json);
 * JsonObject jsonObject = jsonElement.getAsJsonObject();
 * </pre>
 *
 * <p>也可从 reader 解析：
 *
 * <pre>
 * try (Reader reader = new FileReader("my-data.json", StandardCharsets.UTF_8)) {
 *   JsonElement jsonElement = JsonParser.parseReader(reader);
 *   JsonObject jsonObject = jsonElement.getAsJsonObject();
 * }
 * </pre>
 *
 * <p>若需从 {@link JsonReader} 解析以满足更定制的解析需求，以下示例展示如何实现：
 *
 * <pre>
 * String json = "{\"skipObj\": {\"skipKey\": \"skipValue\"}, \"obj\": {\"key\": \"value\"}}";
 * try (JsonReader jsonReader = new JsonReader(new StringReader(json))) {
 *   jsonReader.beginObject();
 *   while (jsonReader.hasNext()) {
 *     String fieldName = jsonReader.nextName();
 *     if (fieldName.equals("skipObj")) {
 *       jsonReader.skipValue();
 *     } else {
 *       JsonElement jsonElement = JsonParser.parseReader(jsonReader);
 *       JsonObject jsonObject = jsonElement.getAsJsonObject();
 *     }
 *   }
 *   jsonReader.endObject();
 * }
 * </pre>
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @since 1.3
 */
public final class JsonParser {
  /**
   * @deprecated 无需实例化此类，请改用静态方法。
   */
  @Deprecated
  public JsonParser() {}

  /**
   * 将指定 JSON 字符串解析为解析树。若 JSON 字符串包含多个顶层 JSON 元素或存在尾部数据，将抛出异常。
   *
   * <p>JSON 字符串以 {@linkplain JsonReader#setStrictness(Strictness) 宽松模式} 解析。
   *
   * @param json JSON 文本
   * @return 与指定 JSON 对应的 {@link JsonElement} 解析树
   * @throws JsonParseException 若指定文本不是有效 JSON
   * @since 2.8.6
   */
  public static JsonElement parseString(String json) throws JsonSyntaxException {
    return parseReader(new StringReader(json));
  }

  /**
   * 将 reader 提供的完整 JSON 字符串解析为解析树。若 JSON 字符串包含多个顶层 JSON 元素或存在尾部数据，
   * 将抛出异常。
   *
   * <p>JSON 数据以 {@linkplain JsonReader#setStrictness(Strictness) 宽松模式} 解析。
   *
   * @param reader JSON 文本
   * @return 与指定 JSON 对应的 {@link JsonElement} 解析树
   * @throws JsonParseException 若发生 IOException 或指定文本不是有效 JSON
   * @since 2.8.6
   */
  public static JsonElement parseReader(Reader reader) throws JsonIOException, JsonSyntaxException {
    try {
      JsonReader jsonReader = new JsonReader(reader);
      JsonElement element = parseReader(jsonReader);
      if (!element.isJsonNull() && jsonReader.peek() != JsonToken.END_DOCUMENT) {
        throw new JsonSyntaxException("Did not consume the entire document.");
      }
      return element;
    } catch (MalformedJsonException | NumberFormatException e) {
      throw new JsonSyntaxException(e);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  /**
   * 从 JSON 流返回下一个值作为解析树。与其他 {@code parse} 方法不同，若 JSON 数据包含多个顶层 JSON
   * 元素或存在尾部数据，不会抛出异常。
   *
   * <p>若 {@linkplain JsonReader#getStrictness() reader 的严格性} 为 {@link
   * Strictness#STRICT}，则使用该严格性进行解析。否则严格性将临时改为 {@link Strictness#LENIENT}，
   * 并在本方法返回后恢复。
   *
   * @throws JsonParseException 若发生 IOException 或指定文本不是有效 JSON
   * @since 2.8.6
   */
  public static JsonElement parseReader(JsonReader reader)
      throws JsonIOException, JsonSyntaxException {
    Strictness strictness = reader.getStrictness();
    if (strictness == Strictness.LEGACY_STRICT) {
      // For backward compatibility change to LENIENT if reader has default strictness LEGACY_STRICT
      reader.setStrictness(Strictness.LENIENT);
    }
    try {
      return Streams.parse(reader);
    } catch (StackOverflowError | OutOfMemoryError e) {
      throw new JsonParseException("Failed parsing JSON source: " + reader + " to Json", e);
    } finally {
      reader.setStrictness(strictness);
    }
  }

  /**
   * @deprecated 请使用 {@link JsonParser#parseString}
   */
  @Deprecated
  @InlineMe(replacement = "JsonParser.parseString(json)", imports = "com.google.gson.JsonParser")
  public JsonElement parse(String json) throws JsonSyntaxException {
    return parseString(json);
  }

  /**
   * @deprecated 请使用 {@link JsonParser#parseReader(Reader)}
   */
  @Deprecated
  @InlineMe(replacement = "JsonParser.parseReader(json)", imports = "com.google.gson.JsonParser")
  public JsonElement parse(Reader json) throws JsonIOException, JsonSyntaxException {
    return parseReader(json);
  }

  /**
   * @deprecated 请使用 {@link JsonParser#parseReader(JsonReader)}
   */
  @Deprecated
  @InlineMe(replacement = "JsonParser.parseReader(json)", imports = "com.google.gson.JsonParser")
  public JsonElement parse(JsonReader json) throws JsonIOException, JsonSyntaxException {
    return parseReader(json);
  }
}
