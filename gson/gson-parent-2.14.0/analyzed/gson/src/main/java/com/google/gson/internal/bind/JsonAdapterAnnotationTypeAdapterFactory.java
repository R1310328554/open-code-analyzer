/*
 * Copyright (C) 2014 Google Inc.
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
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.reflect.TypeToken;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 给定类型 T，查找 {@link JsonAdapter} 注解，并将指定类的实例用作默认类型适配器。
 *
 * @since 2.3
 */
public final class JsonAdapterAnnotationTypeAdapterFactory implements TypeAdapterFactory {
  private static class DummyTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      throw new AssertionError("Factory should not be used");
    }
  }

  /** 用于类上 {@code @JsonAdapter} 所创建 {@link TreeTypeAdapter} 的工厂。 */
  private static final TypeAdapterFactory TREE_TYPE_CLASS_DUMMY_FACTORY =
      new DummyTypeAdapterFactory();

  /** 用于字段上 {@code @JsonAdapter} 所创建 {@link TreeTypeAdapter} 的工厂。 */
  private static final TypeAdapterFactory TREE_TYPE_FIELD_DUMMY_FACTORY =
      new DummyTypeAdapterFactory();

  private final ConstructorConstructor constructorConstructor;

  /**
   * 对类：若标注 {@code @JsonAdapter} 且引用 {@link TypeAdapterFactory}，则缓存工厂实例以防重复请求。
   * 必须为 {@link ConcurrentMap}，因为 {@link Gson} 保证线程安全。
   */
  // Note: In case these strong reference to TypeAdapterFactory instances are considered
  // a memory leak in the future, could consider switching to WeakReference<TypeAdapterFactory>
  private final ConcurrentMap<Class<?>, TypeAdapterFactory> adapterFactoryMap;

  public JsonAdapterAnnotationTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
    this.constructorConstructor = constructorConstructor;
    this.adapterFactoryMap = new ConcurrentHashMap<>();
  }

  // Separate helper method to make sure callers retrieve annotation in a consistent way
  private static JsonAdapter getAnnotation(Class<?> rawType) {
    return rawType.getAnnotation(JsonAdapter.class);
  }

  // this is not safe; requires that user has specified correct adapter class for @JsonAdapter
  @SuppressWarnings("unchecked")
  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> targetType) {
    Class<? super T> rawType = targetType.getRawType();
    JsonAdapter annotation = getAnnotation(rawType);
    if (annotation == null) {
      return null;
    }
    return (TypeAdapter<T>)
        getTypeAdapter(constructorConstructor, gson, targetType, annotation, true);
  }

  // Separate helper method to make sure callers create adapter in a consistent way
  private static Object createAdapter(
      ConstructorConstructor constructorConstructor, Class<?> adapterClass) {
    // TODO: The exception messages created by ConstructorConstructor are currently written in the
    // context of deserialization and for example suggest usage of TypeAdapter, which would not work
    // for @JsonAdapter usage
    // TODO: Should probably not allow usage of Unsafe; instances might be in broken state and
    // calling adapter methods on them might lead to confusing exceptions
    boolean allowUnsafe = true;
    return constructorConstructor.get(TypeToken.get(adapterClass), allowUnsafe).construct();
  }

  private TypeAdapterFactory putFactoryAndGetCurrent(Class<?> rawType, TypeAdapterFactory factory) {
    // Uses putIfAbsent in case multiple threads concurrently create factory
    TypeAdapterFactory existingFactory = adapterFactoryMap.putIfAbsent(rawType, factory);
    return existingFactory != null ? existingFactory : factory;
  }

  TypeAdapter<?> getTypeAdapter(
      ConstructorConstructor constructorConstructor,
      Gson gson,
      TypeToken<?> type,
      JsonAdapter annotation,
      boolean isClassAnnotation) {
    Object instance = createAdapter(constructorConstructor, annotation.value());

    TypeAdapter<?> typeAdapter;
    boolean nullSafe = annotation.nullSafe();
    if (instance instanceof TypeAdapter) {
      typeAdapter = (TypeAdapter<?>) instance;
    } else if (instance instanceof TypeAdapterFactory) {
      TypeAdapterFactory factory = (TypeAdapterFactory) instance;

      if (isClassAnnotation) {
        factory = putFactoryAndGetCurrent(type.getRawType(), factory);
      }

      typeAdapter = factory.create(gson, type);
    } else if (instance instanceof JsonSerializer || instance instanceof JsonDeserializer) {
      JsonSerializer<?> serializer =
          instance instanceof JsonSerializer ? (JsonSerializer<?>) instance : null;
      JsonDeserializer<?> deserializer =
          instance instanceof JsonDeserializer ? (JsonDeserializer<?>) instance : null;

      // Uses dummy factory instances because TreeTypeAdapter needs a 'skipPast' factory for
      // `Gson.getDelegateAdapter` call and has to differentiate there whether TreeTypeAdapter was
      // created for @JsonAdapter on class or field
      TypeAdapterFactory skipPast;
      if (isClassAnnotation) {
        skipPast = TREE_TYPE_CLASS_DUMMY_FACTORY;
      } else {
        skipPast = TREE_TYPE_FIELD_DUMMY_FACTORY;
      }
      @SuppressWarnings({"unchecked", "rawtypes"})
      TypeAdapter<?> tempAdapter =
          new TreeTypeAdapter(serializer, deserializer, gson, type, skipPast, nullSafe);
      typeAdapter = tempAdapter;

      // TreeTypeAdapter handles nullSafe; don't additionally call `nullSafe()`
      nullSafe = false;
    } else {
      throw new IllegalArgumentException(
          "Invalid attempt to bind an instance of "
              + instance.getClass().getName()
              + " as a @JsonAdapter for "
              + type.toString()
              + ". @JsonAdapter value must be a TypeAdapter, TypeAdapterFactory,"
              + " JsonSerializer or JsonDeserializer.");
    }

    if (typeAdapter != null && nullSafe) {
      typeAdapter = typeAdapter.nullSafe();
    }

    return typeAdapter;
  }

  /**
   * 返回 {@code factory} 是否为在 {@code type} 上的 {@code @JsonAdapter} 所创建的类型适配器工厂。
   */
  public boolean isClassJsonAdapterFactory(TypeToken<?> type, TypeAdapterFactory factory) {
    Objects.requireNonNull(type);
    Objects.requireNonNull(factory);

    if (factory == TREE_TYPE_CLASS_DUMMY_FACTORY) {
      return true;
    }

    // Using raw type to match behavior of `create(Gson, TypeToken<T>)` above
    Class<?> rawType = type.getRawType();

    TypeAdapterFactory existingFactory = adapterFactoryMap.get(rawType);
    if (existingFactory != null) {
      // Checks for reference equality, like it is done by `Gson.getDelegateAdapter`
      return existingFactory == factory;
    }

    // If no factory has been created for the type yet check manually for a @JsonAdapter annotation
    // which specifies a TypeAdapterFactory
    // Otherwise behavior would not be consistent, depending on whether or not adapter had been
    // requested before call to `isClassJsonAdapterFactory` was made
    JsonAdapter annotation = getAnnotation(rawType);
    if (annotation == null) {
      return false;
    }

    Class<?> adapterClass = annotation.value();
    if (!TypeAdapterFactory.class.isAssignableFrom(adapterClass)) {
      return false;
    }

    Object adapter = createAdapter(constructorConstructor, adapterClass);
    TypeAdapterFactory newFactory = (TypeAdapterFactory) adapter;

    return putFactoryAndGetCurrent(rawType, newFactory) == factory;
  }
}
