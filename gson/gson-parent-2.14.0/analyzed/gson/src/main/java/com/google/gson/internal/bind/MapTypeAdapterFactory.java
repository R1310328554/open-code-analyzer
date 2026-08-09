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

package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.GsonTypes;
import com.google.gson.internal.JsonReaderInternalAccess;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 将 Map 适配为 JSON 对象或 JSON 数组。
 *
 * <h2>Map 作为 JSON 对象</h2>
 *
 * 对于基本类型键或未启用复杂 Map 键序列化时，将 Java {@link Map} 转换为 JSON 对象。
 * 这要求 Map 键可序列化为字符串；对某些键类型不足。例如键为网格点的 Map，默认 JSON 形式编码合理：
 *
 * <pre>{@code
 * Map<Point, String> original = new LinkedHashMap<>();
 * original.put(new Point(5, 6), "a");
 * original.put(new Point(8, 8), "b");
 * System.out.println(gson.toJson(original, type));
 * }</pre>
 *
 * 上述代码输出如下 JSON 对象：
 *
 * <pre>{@code
 * {
 *   "(5,6)": "a",
 *   "(8,8)": "b"
 * }
 * }</pre>
 *
 * 但 GSON 无法反序列化该值，因为 JSON 字符串名仅为 Map 键的 {@link Object#toString() toString()}。
 * 尝试将上述 JSON 转为对象会抛出解析异常：
 *
 * <pre>com.google.gson.JsonParseException: Expecting object found: "(5,6)"
 *   at com.google.gson.JsonObjectDeserializationVisitor.visitFieldUsingCustomHandler
 *   at com.google.gson.ObjectNavigator.navigateClassFields
 *   ...</pre>
 *
 * <h2>Map 作为 JSON 数组</h2>
 *
 * 在需要且启用复杂 Map 键序列化时，此类型适配器采用将 Map 编码为 Map 条目数组的替代方案。
 * 每个 Map 条目为包含键和值的二元数组。此方式更灵活，因为任意类型均可作 Map 键，不限于字符串；
 * 但可移植性较差，因为接收方须了解 Map 条目约定。
 *
 * <p>创建 GSON 实例时注册此适配器：
 *
 * <pre>{@code
 * Gson gson = new GsonBuilder()
 *   .registerTypeAdapter(Map.class, new MapAsArrayTypeAdapter())
 *   .create();
 * }</pre>
 *
 * 这将改变上述代码发出的 JSON 结构，得到数组，其元素为 Map 条目：
 *
 * <pre>{@code
 * [
 *   [
 *     {
 *       "x": 5,
 *       "y": 6
 *     },
 *     "a",
 *   ],
 *   [
 *     {
 *       "x": 8,
 *       "y": 8
 *     },
 *     "b"
 *   ]
 * ]
 * }</pre>
 *
 * 只要注册了此适配器，该格式即可正常序列化与反序列化。
 */
public final class MapTypeAdapterFactory implements TypeAdapterFactory {
  private final ConstructorConstructor constructorConstructor;
  final boolean complexMapKeySerialization;

  public MapTypeAdapterFactory(
      ConstructorConstructor constructorConstructor, boolean complexMapKeySerialization) {
    this.constructorConstructor = constructorConstructor;
    this.complexMapKeySerialization = complexMapKeySerialization;
  }

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
    Type type = typeToken.getType();

    Class<? super T> rawType = typeToken.getRawType();
    if (!Map.class.isAssignableFrom(rawType)) {
      return null;
    }

    Type[] keyAndValueTypes = GsonTypes.getMapKeyAndValueTypes(type, rawType);
    Type keyType = keyAndValueTypes[0];
    Type valueType = keyAndValueTypes[1];
    TypeAdapter<?> keyAdapter = getKeyAdapter(gson, keyType);
    TypeAdapter<?> wrappedKeyAdapter =
        new TypeAdapterRuntimeTypeWrapper<>(gson, keyAdapter, keyType);
    TypeAdapter<?> valueAdapter = gson.getAdapter(TypeToken.get(valueType));
    TypeAdapter<?> wrappedValueAdapter =
        new TypeAdapterRuntimeTypeWrapper<>(gson, valueAdapter, valueType);
    // Don't allow Unsafe usage to create instance; instances might be in broken state and calling
    // Map methods could lead to confusing exceptions
    boolean allowUnsafe = false;
    ObjectConstructor<T> constructor = constructorConstructor.get(typeToken, allowUnsafe);

    @SuppressWarnings({"unchecked", "rawtypes"})
    // we don't define a type parameter for the key or value types
    TypeAdapter<T> result = new Adapter(wrappedKeyAdapter, wrappedValueAdapter, constructor);
    return result;
  }

  /** 返回将值写为字符串的类型适配器。 */
  private TypeAdapter<?> getKeyAdapter(Gson context, Type keyType) {
    return (keyType == boolean.class || keyType == Boolean.class)
        ? TypeAdapters.BOOLEAN_AS_STRING
        : context.getAdapter(TypeToken.get(keyType));
  }

  private final class Adapter<K, V> extends TypeAdapter<Map<K, V>> {
    private final TypeAdapter<K> keyTypeAdapter;
    private final TypeAdapter<V> valueTypeAdapter;
    private final ObjectConstructor<? extends Map<K, V>> constructor;

    Adapter(
        TypeAdapter<K> keyTypeAdapter,
        TypeAdapter<V> valueTypeAdapter,
        ObjectConstructor<? extends Map<K, V>> constructor) {
      this.keyTypeAdapter = keyTypeAdapter;
      this.valueTypeAdapter = valueTypeAdapter;
      this.constructor = constructor;
    }

    @Override
    public Map<K, V> read(JsonReader in) throws IOException {
      JsonToken peek = in.peek();
      if (peek == JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      Map<K, V> map = constructor.construct();

      if (peek == JsonToken.BEGIN_ARRAY) {
        in.beginArray();
        while (in.hasNext()) {
          in.beginArray(); // entry array
          K key = keyTypeAdapter.read(in);
          V value = valueTypeAdapter.read(in);
          if (map.containsKey(key)) {
            throw new JsonSyntaxException("duplicate key: " + key);
          }
          map.put(key, value);
          in.endArray();
        }
        in.endArray();
      } else {
        in.beginObject();
        while (in.hasNext()) {
          JsonReaderInternalAccess.INSTANCE.promoteNameToValue(in);
          K key = keyTypeAdapter.read(in);
          V value = valueTypeAdapter.read(in);
          if (map.containsKey(key)) {
            throw new JsonSyntaxException("duplicate key: " + key);
          }
          map.put(key, value);
        }
        in.endObject();
      }
      return map;
    }

    @Override
    public void write(JsonWriter out, Map<K, V> map) throws IOException {
      if (map == null) {
        out.nullValue();
        return;
      }

      if (!complexMapKeySerialization) {
        out.beginObject();
        for (Map.Entry<K, V> entry : map.entrySet()) {
          out.name(String.valueOf(entry.getKey()));
          valueTypeAdapter.write(out, entry.getValue());
        }
        out.endObject();
        return;
      }

      boolean hasComplexKeys = false;
      List<JsonElement> keys = new ArrayList<>(map.size());

      List<V> values = new ArrayList<>(map.size());
      for (Map.Entry<K, V> entry : map.entrySet()) {
        JsonElement keyElement = keyTypeAdapter.toJsonTree(entry.getKey());
        keys.add(keyElement);
        values.add(entry.getValue());
        hasComplexKeys |= keyElement.isJsonArray() || keyElement.isJsonObject();
      }

      if (hasComplexKeys) {
        out.beginArray();
        for (int i = 0, size = keys.size(); i < size; i++) {
          out.beginArray(); // entry array
          Streams.write(keys.get(i), out);
          valueTypeAdapter.write(out, values.get(i));
          out.endArray();
        }
        out.endArray();
      } else {
        out.beginObject();
        for (int i = 0, size = keys.size(); i < size; i++) {
          JsonElement keyElement = keys.get(i);
          out.name(keyToString(keyElement));
          valueTypeAdapter.write(out, values.get(i));
        }
        out.endObject();
      }
    }

    private String keyToString(JsonElement keyElement) {
      if (keyElement.isJsonPrimitive()) {
        JsonPrimitive primitive = keyElement.getAsJsonPrimitive();
        if (primitive.isNumber()) {
          return String.valueOf(primitive.getAsNumber());
        } else if (primitive.isBoolean()) {
          return Boolean.toString(primitive.getAsBoolean());
        } else if (primitive.isString()) {
          return primitive.getAsString();
        } else {
          throw new AssertionError();
        }
      } else if (keyElement.isJsonNull()) {
        return "null";
      } else {
        throw new AssertionError();
      }
    }
  }
}
