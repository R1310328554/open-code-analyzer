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

package com.google.gson.typeadapters;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 适配运行时类型可能与声明类型不同的值。当字段的声明类型与 Gson 反序列化该字段时应创建的类型不一致时，
 * 需要使用此适配器。例如，考虑以下类型：
 *
 * <pre>{@code
 * abstract class Shape {
 *   int x;
 *   int y;
 * }
 * class Circle extends Shape {
 *   int radius;
 * }
 * class Rectangle extends Shape {
 *   int width;
 *   int height;
 * }
 * class Diamond extends Shape {
 *   int width;
 *   int height;
 * }
 * class Drawing {
 *   Shape bottomShape;
 *   Shape topShape;
 * }
 * }</pre>
 *
 * <p>若缺少额外的类型信息，序列化后的 JSON 会产生歧义。此绘图中的底部形状究竟是矩形还是菱形？
 *
 * <pre>{@code
 * {
 *   "bottomShape": {
 *     "width": 10,
 *     "height": 5,
 *     "x": 0,
 *     "y": 0
 *   },
 *   "topShape": {
 *     "radius": 2,
 *     "x": 4,
 *     "y": 1
 *   }
 * }
 * }</pre>
 *
 * 此类通过在序列化 JSON 中添加类型信息，并在反序列化时遵循该类型信息来解决上述问题：
 *
 * <pre>{@code
 * {
 *   "bottomShape": {
 *     "type": "Diamond",
 *     "width": 10,
 *     "height": 5,
 *     "x": 0,
 *     "y": 0
 *   },
 *   "topShape": {
 *     "type": "Circle",
 *     "radius": 2,
 *     "x": 4,
 *     "y": 1
 *   }
 * }
 * }</pre>
 *
 * 类型字段名（{@code "type"}）和类型标签（{@code "Rectangle"}）均可配置。
 *
 * <h2>注册类型</h2>
 *
 * 将基类型和类型字段名传入 {@link #of} 工厂方法以创建 {@code RuntimeTypeAdapterFactory}。若未显式指定
 * 类型字段名，则使用 {@code "type"}。
 *
 * <pre>{@code
 * RuntimeTypeAdapterFactory<Shape> shapeAdapterFactory
 *     = RuntimeTypeAdapterFactory.of(Shape.class, "type");
 * }</pre>
 *
 * 接下来注册所有子类型。每个子类型都必须显式注册，以防止注入攻击。若未显式指定类型标签，则使用类型的
 * 简单名称。
 *
 * <pre>{@code
 * shapeAdapterFactory.registerSubtype(Rectangle.class, "Rectangle");
 * shapeAdapterFactory.registerSubtype(Circle.class, "Circle");
 * shapeAdapterFactory.registerSubtype(Diamond.class, "Diamond");
 * }</pre>
 *
 * 最后，在应用的 Gson 构建器中注册该类型适配器工厂：
 *
 * <pre>{@code
 * Gson gson = new GsonBuilder()
 *     .registerTypeAdapterFactory(shapeAdapterFactory)
 *     .create();
 * }</pre>
 *
 * 与 {@code GsonBuilder} 类似，此 API 支持链式调用：
 *
 * <pre>{@code
 * RuntimeTypeAdapterFactory<Shape> shapeAdapterFactory = RuntimeTypeAdapterFactory.of(Shape.class)
 *     .registerSubtype(Rectangle.class)
 *     .registerSubtype(Circle.class)
 *     .registerSubtype(Diamond.class);
 * }</pre>
 *
 * <h2>序列化与反序列化</h2>
 *
 * 要序列化和反序列化多态对象，必须显式指定基类型。
 *
 * <pre>{@code
 * Diamond diamond = new Diamond();
 * String json = gson.toJson(diamond, Shape.class);
 * }</pre>
 *
 * 然后：
 *
 * <pre>{@code
 * Shape shape = gson.fromJson(json, Shape.class);
 * }</pre>
 */
public final class RuntimeTypeAdapterFactory<T> implements TypeAdapterFactory {
  private final Class<?> baseType;
  private final String typeFieldName;
  private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap<>();
  private final Map<Class<?>, String> subtypeToLabel = new LinkedHashMap<>();
  private final boolean maintainType;
  private boolean recognizeSubtypes;

  private RuntimeTypeAdapterFactory(Class<?> baseType, String typeFieldName, boolean maintainType) {
    if (typeFieldName == null || baseType == null) {
      throw new NullPointerException();
    }
    this.baseType = baseType;
    this.typeFieldName = typeFieldName;
    this.maintainType = maintainType;
  }

  /**
   * 为 {@code baseType} 创建新的运行时类型适配器，使用 {@code typeFieldName} 作为类型字段名。类型字段名区分大小写。
   *
   * @param maintainType 若为 true，则类型字段会包含在反序列化后的对象中
   */
  public static <T> RuntimeTypeAdapterFactory<T> of(
      Class<T> baseType, String typeFieldName, boolean maintainType) {
    return new RuntimeTypeAdapterFactory<>(baseType, typeFieldName, maintainType);
  }

  /**
   * 为 {@code baseType} 创建新的运行时类型适配器，使用 {@code typeFieldName} 作为类型字段名。类型字段名区分大小写。
   */
  public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType, String typeFieldName) {
    return new RuntimeTypeAdapterFactory<>(baseType, typeFieldName, false);
  }

  /**
   * 为 {@code baseType} 创建新的运行时类型适配器，使用 {@code "type"} 作为类型字段名。
   */
  public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType) {
    return new RuntimeTypeAdapterFactory<>(baseType, "type", false);
  }

  /**
   * 确保此工厂不仅处理给定的 {@code baseType}，还处理该类型的任意子类型。
   */
  @CanIgnoreReturnValue
  public RuntimeTypeAdapterFactory<T> recognizeSubtypes() {
    this.recognizeSubtypes = true;
    return this;
  }

  /**
   * 注册由 {@code label} 标识的 {@code type}。标签区分大小写。
   *
   * @throws IllegalArgumentException 若 {@code type} 或 {@code label} 已在此类型适配器上注册
   */
  @CanIgnoreReturnValue
  public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type, String label) {
    if (type == null || label == null) {
      throw new NullPointerException();
    }
    if (subtypeToLabel.containsKey(type) || labelToSubtype.containsKey(label)) {
      throw new IllegalArgumentException("types and labels must be unique");
    }
    labelToSubtype.put(label, type);
    subtypeToLabel.put(type, label);
    return this;
  }

  /**
   * 注册由 {@link Class#getSimpleName 简单名称} 标识的 {@code type}。标签区分大小写。
   *
   * @throws IllegalArgumentException 若 {@code type} 或其简单名称已在此类型适配器上注册
   */
  @CanIgnoreReturnValue
  public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type) {
    return registerSubtype(type, type.getSimpleName());
  }

  @Override
  public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
    if (type == null) {
      return null;
    }
    Class<?> rawType = type.getRawType();
    boolean handle =
        recognizeSubtypes ? baseType.isAssignableFrom(rawType) : baseType.equals(rawType);
    if (!handle) {
      return null;
    }

    TypeAdapter<JsonElement> jsonElementAdapter = gson.getAdapter(JsonElement.class);
    Map<String, TypeAdapter<?>> labelToDelegate = new LinkedHashMap<>();
    Map<Class<?>, TypeAdapter<?>> subtypeToDelegate = new LinkedHashMap<>();
    for (Map.Entry<String, Class<?>> entry : labelToSubtype.entrySet()) {
      TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
      labelToDelegate.put(entry.getKey(), delegate);
      subtypeToDelegate.put(entry.getValue(), delegate);
    }

    return new TypeAdapter<R>() {
      @Override
      public R read(JsonReader in) throws IOException {
        JsonElement jsonElement = jsonElementAdapter.read(in);
        JsonElement labelJsonElement;
        if (maintainType) {
          labelJsonElement = jsonElement.getAsJsonObject().get(typeFieldName);
        } else {
          labelJsonElement = jsonElement.getAsJsonObject().remove(typeFieldName);
        }

        if (labelJsonElement == null) {
          throw new JsonParseException(
              "cannot deserialize "
                  + baseType
                  + " because it does not define a field named "
                  + typeFieldName);
        }
        String label = labelJsonElement.getAsString();
        @SuppressWarnings("unchecked") // registration requires that subtype extends T
        TypeAdapter<R> delegate = (TypeAdapter<R>) labelToDelegate.get(label);
        if (delegate == null) {
          throw new JsonParseException(
              "cannot deserialize "
                  + baseType
                  + " subtype named "
                  + label
                  + "; did you forget to register a subtype?");
        }
        return delegate.fromJsonTree(jsonElement);
      }

      @Override
      public void write(JsonWriter out, R value) throws IOException {
        Class<?> srcType = value.getClass();
        String label = subtypeToLabel.get(srcType);
        @SuppressWarnings("unchecked") // registration requires that subtype extends T
        TypeAdapter<R> delegate = (TypeAdapter<R>) subtypeToDelegate.get(srcType);
        if (delegate == null) {
          throw new JsonParseException(
              "cannot serialize " + srcType.getName() + "; did you forget to register a subtype?");
        }
        JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();

        if (maintainType) {
          jsonElementAdapter.write(out, jsonObject);
          return;
        }

        JsonObject clone = new JsonObject();

        if (jsonObject.has(typeFieldName)) {
          throw new JsonParseException(
              "cannot serialize "
                  + srcType.getName()
                  + " because it already defines a field named "
                  + typeFieldName);
        }
        clone.add(typeFieldName, new JsonPrimitive(label));

        for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
          clone.add(e.getKey(), e.getValue());
        }
        jsonElementAdapter.write(out, clone);
      }
    }.nullSafe();
  }
}
