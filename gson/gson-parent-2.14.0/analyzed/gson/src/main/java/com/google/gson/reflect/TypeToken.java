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

package com.google.gson.reflect;

import com.google.gson.internal.GsonTypes;
import com.google.gson.internal.TroubleshootingGuide;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 表示泛型类型 {@code T}。Java 无法在运行时直接表示泛型，本类通过匿名子类在运行时保留类型信息。
 *
 * <p>例如创建 {@code List<String>} 的类型字面量：
 * {@code TypeToken<List<String>> list = new TypeToken<List<String>>() {};}
 *
 * <p>匿名 {@code TypeToken} 子类不得捕获类型变量（如 {@code TypeToken<List<T>>}），否则擦除后 Gson 无法获得真实类型，
 * 编译期看似类型安全却可能在运行时抛出 {@code ClassCastException}。
 *
 * <p>若泛型实参仅在运行时可用，可使用 {@link #getParameterized(Type, Type...)}。
 *
 * @author Bob Lee
 * @author Sven Mawson
 * @author Jesse Wilson
 */
public class TypeToken<T> {
  private final Class<? super T> rawType;
  private final Type type;
  private final int hashCode;

  /**
   * 构造新的类型字面量，从类型参数推导所表示的类。
   *
   * <p>客户端应创建空的匿名子类，例如 {@code new TypeToken<List<String>>() {}}
   *
   * @throws IllegalArgumentException 若匿名子类捕获了类型变量（如 {@code TypeToken<List<T>>}）
   */
  @SuppressWarnings("unchecked")
  protected TypeToken() {
    this.type = getTypeTokenTypeArgument();
    this.rawType = (Class<? super T>) GsonTypes.getRawType(type);
    this.hashCode = type.hashCode();
  }

  /** 非安全：手动指定 {@link Type} 构造类型字面量。 */
  @SuppressWarnings("unchecked")
  private TypeToken(Type type) {
    this.type = GsonTypes.canonicalize(Objects.requireNonNull(type));
    this.rawType = (Class<? super T>) GsonTypes.getRawType(this.type);
    this.hashCode = this.type.hashCode();
  }

  private static boolean isCapturingTypeVariablesForbidden() {
    return !Objects.equals(System.getProperty("gson.allowCapturingTypeVariables"), "true");
  }

  /** 验证 {@code this} 为 {@code TypeToken} 的直接子类，并返回 {@code T} 的规范化类型实参。 */
  private Type getTypeTokenTypeArgument() {
    Type superclass = getClass().getGenericSuperclass();
    if (superclass instanceof ParameterizedType) {
      ParameterizedType parameterized = (ParameterizedType) superclass;
      if (parameterized.getRawType() == TypeToken.class) {
        Type typeArgument = GsonTypes.canonicalize(parameterized.getActualTypeArguments()[0]);

        if (isCapturingTypeVariablesForbidden()) {
          verifyNoTypeVariable(typeArgument);
        }
        return typeArgument;
      }
    }
    // Check for raw TypeToken as superclass
    else if (superclass == TypeToken.class) {
      throw new IllegalStateException(
          "TypeToken must be created with a type argument: new TypeToken<...>() {}; When using code"
              + " shrinkers (ProGuard, R8, ...) make sure that generic signatures are preserved."
              + "\nSee "
              + TroubleshootingGuide.createUrl("type-token-raw"));
    }

    // User created subclass of subclass of TypeToken
    throw new IllegalStateException("Must only create direct subclasses of TypeToken");
  }

  private static void verifyNoTypeVariable(Type type) {
    if (type instanceof TypeVariable) {
      TypeVariable<?> typeVariable = (TypeVariable<?>) type;
      throw new IllegalArgumentException(
          "TypeToken type argument must not contain a type variable; captured type variable "
              + typeVariable.getName()
              + " declared by "
              + typeVariable.getGenericDeclaration()
              + "\nSee "
              + TroubleshootingGuide.createUrl("typetoken-type-variable"));
    } else if (type instanceof GenericArrayType) {
      verifyNoTypeVariable(((GenericArrayType) type).getGenericComponentType());
    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type ownerType = parameterizedType.getOwnerType();
      if (ownerType != null) {
        verifyNoTypeVariable(ownerType);
      }

      for (Type typeArgument : parameterizedType.getActualTypeArguments()) {
        verifyNoTypeVariable(typeArgument);
      }
    } else if (type instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) type;
      for (Type bound : wildcardType.getLowerBounds()) {
        verifyNoTypeVariable(bound);
      }
      for (Type bound : wildcardType.getUpperBounds()) {
        verifyNoTypeVariable(bound);
      }
    } else if (type == null) {
      // Occurs in Eclipse IDE and certain Java versions (e.g. Java 11.0.18) when capturing type
      // variable declared by method of local class, see
      // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/975
      throw new IllegalArgumentException(
          "TypeToken captured `null` as type argument; probably a compiler / runtime bug");
    }
  }

  /** 返回原始（非泛型）类型。 */
  public final Class<? super T> getRawType() {
    return rawType;
  }

  /** 返回底层 {@link Type} 实例。 */
  public final Type getType() {
    return type;
  }

  /**
   * 判断给定 {@link Class} 是否可赋值给本类型。
   *
   * @deprecated 对含通配符的类型可能与 javac 不一致
   */
  @Deprecated
  public boolean isAssignableFrom(Class<?> cls) {
    return isAssignableFrom((Type) cls);
  }

  /**
   * 判断给定 {@link Type} 是否可赋值给本类型。
   *
   * @deprecated 对含通配符的类型可能与 javac 不一致
   */
  @Deprecated
  public boolean isAssignableFrom(Type from) {
    if (from == null) {
      return false;
    }

    if (type.equals(from)) {
      return true;
    }

    if (type instanceof Class<?>) {
      return rawType.isAssignableFrom(GsonTypes.getRawType(from));
    } else if (type instanceof ParameterizedType) {
      return isAssignableFrom(from, (ParameterizedType) type, new HashMap<String, Type>());
    } else if (type instanceof GenericArrayType) {
      return rawType.isAssignableFrom(GsonTypes.getRawType(from))
          && isAssignableFrom(from, (GenericArrayType) type);
    } else {
      throw buildUnsupportedTypeException(
          type, Class.class, ParameterizedType.class, GenericArrayType.class);
    }
  }

  /**
   * 判断给定 {@link TypeToken} 是否可赋值给本类型。
   *
   * @deprecated 对含通配符的类型可能与 javac 不一致
   */
  @Deprecated
  public boolean isAssignableFrom(TypeToken<?> token) {
    return isAssignableFrom(token.getType());
  }

  /** 对 {@link GenericArrayType} 执行可赋值性检查的私有辅助方法。 */
  private static boolean isAssignableFrom(Type from, GenericArrayType to) {
    Type toGenericComponentType = to.getGenericComponentType();
    if (toGenericComponentType instanceof ParameterizedType) {
      Type t = from;
      if (from instanceof GenericArrayType) {
        t = ((GenericArrayType) from).getGenericComponentType();
      } else if (from instanceof Class<?>) {
        Class<?> classType = (Class<?>) from;
        while (classType.isArray()) {
          classType = classType.getComponentType();
        }
        t = classType;
      }
      return isAssignableFrom(
          t, (ParameterizedType) toGenericComponentType, new HashMap<String, Type>());
    }
    // No generic defined on "to"; therefore, return true and let other
    // checks determine assignability
    return true;
  }

  /** 递归执行类型安全可赋值性检查的私有辅助方法。 */
  private static boolean isAssignableFrom(
      Type from, ParameterizedType to, Map<String, Type> typeVarMap) {

    if (from == null) {
      return false;
    }

    if (to.equals(from)) {
      return true;
    }

    // First figure out the class and any type information.
    Class<?> clazz = GsonTypes.getRawType(from);
    ParameterizedType ptype = null;
    if (from instanceof ParameterizedType) {
      ptype = (ParameterizedType) from;
    }

    // Load up parameterized variable info if it was parameterized.
    if (ptype != null) {
      Type[] tArgs = ptype.getActualTypeArguments();
      TypeVariable<?>[] tParams = clazz.getTypeParameters();
      for (int i = 0; i < tArgs.length; i++) {
        Type arg = tArgs[i];
        TypeVariable<?> var = tParams[i];
        while (arg instanceof TypeVariable<?>) {
          TypeVariable<?> v = (TypeVariable<?>) arg;
          arg = typeVarMap.get(v.getName());
        }
        typeVarMap.put(var.getName(), arg);
      }

      // check if they are equivalent under our current mapping.
      if (typeEquals(ptype, to, typeVarMap)) {
        return true;
      }
    }

    for (Type itype : clazz.getGenericInterfaces()) {
      if (isAssignableFrom(itype, to, new HashMap<>(typeVarMap))) {
        return true;
      }
    }

    // Interfaces didn't work, try the superclass.
    Type sType = clazz.getGenericSuperclass();
    return isAssignableFrom(sType, to, new HashMap<>(typeVarMap));
  }

  /** 在 typeVarMap 变量替换下，判断两个参数化类型是否完全相等。 */
  private static boolean typeEquals(
      ParameterizedType from, ParameterizedType to, Map<String, Type> typeVarMap) {
    if (from.getRawType().equals(to.getRawType())) {
      Type[] fromArgs = from.getActualTypeArguments();
      Type[] toArgs = to.getActualTypeArguments();
      for (int i = 0; i < fromArgs.length; i++) {
        if (!matches(fromArgs[i], toArgs[i], typeVarMap)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private static IllegalArgumentException buildUnsupportedTypeException(
      Type token, Class<?>... expected) {

    // Build exception message
    StringBuilder exceptionMessage = new StringBuilder("Unsupported type, expected one of: ");
    for (Class<?> clazz : expected) {
      exceptionMessage.append(clazz.getName()).append(", ");
    }
    exceptionMessage
        .append("but got: ")
        .append(token.getClass().getName())
        .append(", for type token: ")
        .append(token.toString());

    return new IllegalArgumentException(exceptionMessage.toString());
  }

  /** 判断两类型是否相同，或在给定 typeMap 下等价。 */
  private static boolean matches(Type from, Type to, Map<String, Type> typeMap) {
    return to.equals(from)
        || (from instanceof TypeVariable
            && to.equals(typeMap.get(((TypeVariable<?>) from).getName())));
  }

  @Override
  public final int hashCode() {
    return this.hashCode;
  }

  @Override
  public final boolean equals(Object o) {
    return o instanceof TypeToken<?> && GsonTypes.equals(type, ((TypeToken<?>) o).type);
  }

  @Override
  public final String toString() {
    return GsonTypes.typeToString(type);
  }

  /** 为给定 {@link Type} 获取类型字面量。 */
  public static TypeToken<?> get(Type type) {
    return new TypeToken<>(type);
  }

  /** 为给定 {@link Class} 获取类型字面量。 */
  public static <T> TypeToken<T> get(Class<T> type) {
    return new TypeToken<>(type);
  }

  /**
   * 将 {@code typeArguments} 应用于 {@code rawType}，得到参数化类型的类型字面量；适用于编译期无法获得泛型实参的场景。
   *
   * <p>返回 {@code TypeToken<?>}，无编译期类型安全，须传入正确数量的类型实参。
   *
   * <p>若 {@code rawType} 非泛型且未提供实参，则委托 {@link #get(Class)}。
   *
   * @throws IllegalArgumentException {@code rawType} 非 {@code Class} 或实参无效时
   */
  public static TypeToken<?> getParameterized(Type rawType, Type... typeArguments) {
    Objects.requireNonNull(rawType);
    Objects.requireNonNull(typeArguments);

    // Perform basic validation here because this is the only public API where users
    // can create malformed parameterized types
    if (!(rawType instanceof Class)) {
      // See also https://bugs.openjdk.org/browse/JDK-8250659
      throw new IllegalArgumentException("rawType must be of type Class, but was " + rawType);
    }
    Class<?> rawClass = (Class<?>) rawType;
    TypeVariable<?>[] typeVariables = rawClass.getTypeParameters();

    int expectedArgsCount = typeVariables.length;
    int actualArgsCount = typeArguments.length;
    if (actualArgsCount != expectedArgsCount) {
      throw new IllegalArgumentException(
          rawClass.getName()
              + " requires "
              + expectedArgsCount
              + " type arguments, but got "
              + actualArgsCount);
    }

    // For legacy reasons create a TypeToken(Class) if the type is not generic
    if (typeArguments.length == 0) {
      return get(rawClass);
    }

    // Check for this here to avoid misleading exception thrown by ParameterizedTypeImpl
    if (GsonTypes.requiresOwnerType(rawType)) {
      throw new IllegalArgumentException(
          "Raw type "
              + rawClass.getName()
              + " is not supported because it requires specifying an owner type");
    }

    for (int i = 0; i < expectedArgsCount; i++) {
      Type typeArgument =
          Objects.requireNonNull(typeArguments[i], "Type argument must not be null");
      Class<?> rawTypeArgument = GsonTypes.getRawType(typeArgument);
      TypeVariable<?> typeVariable = typeVariables[i];

      for (Type bound : typeVariable.getBounds()) {
        Class<?> rawBound = GsonTypes.getRawType(bound);

        if (!rawBound.isAssignableFrom(rawTypeArgument)) {
          throw new IllegalArgumentException(
              "Type argument "
                  + typeArgument
                  + " does not satisfy bounds for type variable "
                  + typeVariable
                  + " declared by "
                  + rawType);
        }
      }
    }

    return new TypeToken<>(GsonTypes.newParameterizedTypeWithOwner(null, rawClass, typeArguments));
  }

  /** 获取元素类型为 {@code componentType} 的数组类型字面量。 */
  public static TypeToken<?> getArray(Type componentType) {
    return new TypeToken<>(GsonTypes.arrayOf(componentType));
  }
}
