/*
 * Copyright (C) 2011 Google Inc.
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

package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * 将 Gson 1.x 树形风格适配器（{@link JsonSerializer}/{@link JsonDeserializer}）包装为流式 {@link TypeAdapter}。
 * 树形适配器可能仅支持序列化或反序列化，因此本类可按需查找委托适配器。
 */
public final class TreeTypeAdapter<T> extends SerializationDelegatingTypeAdapter<T> {
  private final JsonSerializer<T> serializer;
  private final JsonDeserializer<T> deserializer;
  final Gson gson;
  private final TypeToken<T> typeToken;

  /** 仅作为 {@link Gson#getDelegateAdapter(TypeAdapterFactory, TypeToken)} 的 {@code skipPast} 参数，不得用于其他用途。 */
  private final TypeAdapterFactory skipPastForGetDelegateAdapter;

  private final GsonContextImpl context = new GsonContextImpl();
  private final boolean nullSafe;

  /**
   * 委托适配器延迟创建：可能用不到，且创建可能失败。字段须为 {@code volatile}，因 {@link Gson} 保证线程安全。
   */
  private volatile TypeAdapter<T> delegate;

  public TreeTypeAdapter(
      JsonSerializer<T> serializer,
      JsonDeserializer<T> deserializer,
      Gson gson,
      TypeToken<T> typeToken,
      TypeAdapterFactory skipPast,
      boolean nullSafe) {
    this.serializer = serializer;
    this.deserializer = deserializer;
    this.gson = gson;
    this.typeToken = typeToken;
    this.skipPastForGetDelegateAdapter = skipPast;
    this.nullSafe = nullSafe;
  }

  public TreeTypeAdapter(
      JsonSerializer<T> serializer,
      JsonDeserializer<T> deserializer,
      Gson gson,
      TypeToken<T> typeToken,
      TypeAdapterFactory skipPast) {
    this(serializer, deserializer, gson, typeToken, skipPast, true);
  }

  @Override
  public T read(JsonReader in) throws IOException {
    if (deserializer == null) {
      return delegate().read(in);
    }
    JsonElement value = Streams.parse(in);
    if (nullSafe && value.isJsonNull()) {
      return null;
    }
    return deserializer.deserialize(value, typeToken.getType(), context);
  }

  @Override
  public void write(JsonWriter out, T value) throws IOException {
    if (serializer == null) {
      delegate().write(out, value);
      return;
    }
    if (nullSafe && value == null) {
      out.nullValue();
      return;
    }
    JsonElement tree = serializer.serialize(value, typeToken.getType(), context);
    Streams.write(tree, out);
  }

  private TypeAdapter<T> delegate() {
    // A race might lead to `delegate` being assigned by multiple threads but the last assignment
    // will stick
    TypeAdapter<T> d = delegate;
    if (d == null) {
      d = delegate = gson.getDelegateAdapter(skipPastForGetDelegateAdapter, typeToken);
    }
    return d;
  }

  /**
   * 返回用于序列化的类型适配器。若本 {@code TreeTypeAdapter} 有 {@link #serializer} 则返回 {@code this}，否则返回委托。
   */
  @Override
  public TypeAdapter<T> getSerializationDelegate() {
    return serializer != null ? this : delegate();
  }

  /** 返回新工厂：仅当类型与 {@code exactType} 完全匹配时创建适配器。 */
  public static TypeAdapterFactory newFactory(TypeToken<?> exactType, Object typeAdapter) {
    return new SingleTypeFactory(typeAdapter, exactType, false, null);
  }

  /** 返回新工厂：类型或其原始类型与 {@code exactType} 匹配时创建适配器。 */
  public static TypeAdapterFactory newFactoryWithMatchRawType(
      TypeToken<?> exactType, Object typeAdapter) {
    // only bother matching raw types if exact type is a raw type
    boolean matchRawType = exactType.getType() == exactType.getRawType();
    return new SingleTypeFactory(typeAdapter, exactType, matchRawType, null);
  }

  /** 返回新工厂：当类型的原始类型可赋值给 {@code hierarchyType} 时创建适配器。 */
  public static TypeAdapterFactory newTypeHierarchyFactory(
      Class<?> hierarchyType, Object typeAdapter) {
    return new SingleTypeFactory(typeAdapter, null, false, hierarchyType);
  }

  private static final class SingleTypeFactory implements TypeAdapterFactory {
    private final TypeToken<?> exactType;
    private final boolean matchRawType;
    private final Class<?> hierarchyType;
    private final JsonSerializer<?> serializer;
    private final JsonDeserializer<?> deserializer;

    SingleTypeFactory(
        Object typeAdapter, TypeToken<?> exactType, boolean matchRawType, Class<?> hierarchyType) {
      serializer = typeAdapter instanceof JsonSerializer ? (JsonSerializer<?>) typeAdapter : null;
      deserializer =
          typeAdapter instanceof JsonDeserializer ? (JsonDeserializer<?>) typeAdapter : null;
      if (serializer == null && deserializer == null) {
        Objects.requireNonNull(typeAdapter);
        throw new IllegalArgumentException(
            "Type adapter "
                + typeAdapter.getClass().getName()
                + " must implement JsonSerializer or JsonDeserializer");
      }
      this.exactType = exactType;
      this.matchRawType = matchRawType;
      this.hierarchyType = hierarchyType;
    }

    @SuppressWarnings("unchecked") // guarded by typeToken.equals() call
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      boolean matches =
          exactType != null
              ? exactType.equals(type) || (matchRawType && exactType.getType() == type.getRawType())
              : hierarchyType.isAssignableFrom(type.getRawType());
      return matches
          ? new TreeTypeAdapter<>(
              (JsonSerializer<T>) serializer, (JsonDeserializer<T>) deserializer, gson, type, this)
          : null;
    }
  }

  private final class GsonContextImpl
      implements JsonSerializationContext, JsonDeserializationContext {
    @Override
    public JsonElement serialize(Object src) {
      return gson.toJsonTree(src);
    }

    @Override
    public JsonElement serialize(Object src, Type typeOfSrc) {
      return gson.toJsonTree(src, typeOfSrc);
    }

    @Override
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <R> R deserialize(JsonElement json, Type typeOfT) throws JsonParseException {
      return gson.fromJson(json, typeOfT);
    }
  }
}
