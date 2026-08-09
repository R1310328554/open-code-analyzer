/*
 * Copyright (C) 2021 Google Inc.
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

package com.google.gson.internal.reflect;

import com.google.gson.JsonIOException;
import com.google.gson.internal.GsonBuildConfig;
import com.google.gson.internal.TroubleshootingGuide;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/** 反射工具：可访问性、Record 支持及人类可读的对象描述。 */
public class ReflectionHelper {

  private static final RecordHelper RECORD_HELPER;

  static {
    RecordHelper instance;
    try {
      // Try to construct the RecordSupportedHelper, if this fails, records are not supported on
      // this JVM.
      instance = new RecordSupportedHelper();
    } catch (ReflectiveOperationException e) {
      instance = new RecordNotSupportedHelper();
    }
    RECORD_HELPER = instance;
  }

  private ReflectionHelper() {}

  private static String getInaccessibleTroubleshootingSuffix(Exception e) {
    // Class was added in Java 9, therefore cannot use instanceof
    if (e.getClass().getName().equals("java.lang.reflect.InaccessibleObjectException")) {
      String message = e.getMessage();
      String troubleshootingId =
          message != null && message.contains("to module com.google.gson")
              ? "reflection-inaccessible-to-module-gson"
              : "reflection-inaccessible";
      return "\nSee " + TroubleshootingGuide.createUrl(troubleshootingId);
    }
    return "";
  }

  /**
   * 内部实现：使 {@link AccessibleObject} 可访问。
   *
   * @param object 对其调用 {@link AccessibleObject#setAccessible(boolean)} 的对象
   * @throws JsonIOException 设置可访问失败时
   */
  public static void makeAccessible(AccessibleObject object) throws JsonIOException {
    try {
      object.setAccessible(true);
    } catch (Exception exception) {
      String description = getAccessibleObjectDescription(object, false);
      throw new JsonIOException(
          "Failed making "
              + description
              + " accessible; either increase its visibility"
              + " or write a custom TypeAdapter for its declaring type."
              + getInaccessibleTroubleshootingSuffix(exception),
          exception);
    }
  }

  /**
   * 返回 {@link AccessibleObject} 的简短可读描述，通常比 {@link AccessibleObject#toString()} 更短。
   *
   * @param object 待描述对象
   * @param uppercaseFirstLetter 是否将首字母大写
   */
  public static String getAccessibleObjectDescription(
      AccessibleObject object, boolean uppercaseFirstLetter) {
    String description;

    if (object instanceof Field) {
      description = "field '" + fieldToString((Field) object) + "'";
    } else if (object instanceof Method) {
      Method method = (Method) object;

      StringBuilder methodSignatureBuilder = new StringBuilder(method.getName());
      appendExecutableParameters(method, methodSignatureBuilder);
      String methodSignature = methodSignatureBuilder.toString();

      description = "method '" + method.getDeclaringClass().getName() + "#" + methodSignature + "'";
    } else if (object instanceof Constructor) {
      description = "constructor '" + constructorToString((Constructor<?>) object) + "'";
    } else {
      description = "<unknown AccessibleObject> " + object.toString();
    }

    if (uppercaseFirstLetter && Character.isLowerCase(description.charAt(0))) {
      description = Character.toUpperCase(description.charAt(0)) + description.substring(1);
    }
    return description;
  }

  /** 生成字段的字符串表示，省略修饰符与字段类型。 */
  public static String fieldToString(Field field) {
    return field.getDeclaringClass().getName() + "#" + field.getName();
  }

  /** 生成构造器的字符串表示，例如 {@code java.lang.String(char[], int, int)}。 */
  public static String constructorToString(Constructor<?> constructor) {
    StringBuilder stringBuilder = new StringBuilder(constructor.getDeclaringClass().getName());
    appendExecutableParameters(constructor, stringBuilder);

    return stringBuilder.toString();
  }

  // Ideally parameter type would be java.lang.reflect.Executable, but that was added
  // in Android API level 26
  private static void appendExecutableParameters(
      AccessibleObject executable, StringBuilder stringBuilder) {
    stringBuilder.append('(');

    Class<?>[] parameters =
        (executable instanceof Method)
            ? ((Method) executable).getParameterTypes()
            : ((Constructor<?>) executable).getParameterTypes();
    for (int i = 0; i < parameters.length; i++) {
      if (i > 0) {
        stringBuilder.append(", ");
      }
      stringBuilder.append(parameters[i].getSimpleName());
    }

    stringBuilder.append(')');
  }

  public static boolean isStatic(Class<?> clazz) {
    return Modifier.isStatic(clazz.getModifiers());
  }

  /** 判断类是否为匿名类或非 static 局部类。 */
  public static boolean isAnonymousOrNonStaticLocal(Class<?> clazz) {
    return !isStatic(clazz) && (clazz.isAnonymousClass() || clazz.isLocalClass());
  }

  /**
   * 尝试使构造器可访问。
   *
   * @param constructor 目标构造器
   * @return 失败时返回异常消息，成功返回 {@code null}
   */
  public static String tryMakeAccessible(Constructor<?> constructor) {
    try {
      constructor.setAccessible(true);
      return null;
    } catch (Exception exception) {
      return "Failed making constructor '"
          + constructorToString(constructor)
          + "' accessible; either increase its visibility or write a custom InstanceCreator or"
          + " TypeAdapter for its declaring type: "
          // Include the message since it might contain more detailed information
          + exception.getMessage()
          + getInaccessibleTroubleshootingSuffix(exception);
    }
  }

  /** 若 JVM 支持 Record，等价于 {@link Class#isRecord()}。 */
  public static boolean isRecord(Class<?> raw) {
    return RECORD_HELPER.isRecord(raw);
  }

  public static String[] getRecordComponentNames(Class<?> raw) {
    return RECORD_HELPER.getRecordComponentNames(raw);
  }

  /** 查找与给定 Record 字段对应的访问器方法。 */
  public static Method getAccessor(Class<?> raw, Field field) {
    return RECORD_HELPER.getAccessor(raw, field);
  }

  public static <T> Constructor<T> getCanonicalRecordConstructor(Class<T> raw) {
    return RECORD_HELPER.getCanonicalRecordConstructor(raw);
  }

  public static RuntimeException createExceptionForUnexpectedIllegalAccess(
      IllegalAccessException exception) {
    throw new RuntimeException(
        "Unexpected IllegalAccessException occurred (Gson "
            + GsonBuildConfig.VERSION
            + "). Certain ReflectionAccessFilter features require Java >= 9 to work correctly. If"
            + " you are not using ReflectionAccessFilter, report this to the Gson maintainers.",
        exception);
  }

  private static RuntimeException createExceptionForRecordReflectionException(
      ReflectiveOperationException exception) {
    throw new RuntimeException(
        "Unexpected ReflectiveOperationException occurred"
            + " (Gson "
            + GsonBuildConfig.VERSION
            + ")."
            + " To support Java records, reflection is utilized to read out information"
            + " about records. All these invocations happens after it is established"
            + " that records exist in the JVM. This exception is unexpected behavior.",
        exception);
  }

  /** Record 受支持时的反射抽象。 */
  private abstract static class RecordHelper {
    abstract boolean isRecord(Class<?> clazz);

    abstract String[] getRecordComponentNames(Class<?> clazz);

    abstract <T> Constructor<T> getCanonicalRecordConstructor(Class<T> raw);

    abstract Method getAccessor(Class<?> raw, Field field);
  }

  private static class RecordSupportedHelper extends RecordHelper {
    private final Method isRecord;
    private final Method getRecordComponents;
    private final Method getName;
    private final Method getType;

    private RecordSupportedHelper() throws NoSuchMethodException, ClassNotFoundException {
      isRecord = Class.class.getMethod("isRecord");
      getRecordComponents = Class.class.getMethod("getRecordComponents");
      Class<?> classRecordComponent = Class.forName("java.lang.reflect.RecordComponent");
      getName = classRecordComponent.getMethod("getName");
      getType = classRecordComponent.getMethod("getType");
    }

    @Override
    boolean isRecord(Class<?> raw) {
      try {
        return (boolean) isRecord.invoke(raw);
      } catch (ReflectiveOperationException e) {
        throw createExceptionForRecordReflectionException(e);
      }
    }

    @Override
    String[] getRecordComponentNames(Class<?> raw) {
      try {
        Object[] recordComponents = (Object[]) getRecordComponents.invoke(raw);
        String[] componentNames = new String[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i++) {
          componentNames[i] = (String) getName.invoke(recordComponents[i]);
        }
        return componentNames;
      } catch (ReflectiveOperationException e) {
        throw createExceptionForRecordReflectionException(e);
      }
    }

    @Override
    public <T> Constructor<T> getCanonicalRecordConstructor(Class<T> raw) {
      try {
        Object[] recordComponents = (Object[]) getRecordComponents.invoke(raw);
        Class<?>[] recordComponentTypes = new Class<?>[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i++) {
          recordComponentTypes[i] = (Class<?>) getType.invoke(recordComponents[i]);
        }
        // Uses getDeclaredConstructor because implicit constructor has same visibility as record
        // and might therefore not be public
        return raw.getDeclaredConstructor(recordComponentTypes);
      } catch (ReflectiveOperationException e) {
        throw createExceptionForRecordReflectionException(e);
      }
    }

    @Override
    public Method getAccessor(Class<?> raw, Field field) {
      try {
        // Records consists of record components, each with a unique name, a corresponding field and
        // accessor method with the same name. Ref.:
        // https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html#jls-8.10.3
        return raw.getMethod(field.getName());
      } catch (ReflectiveOperationException e) {
        throw createExceptionForRecordReflectionException(e);
      }
    }
  }

  /** JVM 不支持 Record 时使用的实现。 */
  private static class RecordNotSupportedHelper extends RecordHelper {

    @Override
    boolean isRecord(Class<?> clazz) {
      return false;
    }

    @Override
    String[] getRecordComponentNames(Class<?> clazz) {
      throw new UnsupportedOperationException(
          "Records are not supported on this JVM, this method should not be called");
    }

    @Override
    <T> Constructor<T> getCanonicalRecordConstructor(Class<T> raw) {
      throw new UnsupportedOperationException(
          "Records are not supported on this JVM, this method should not be called");
    }

    @Override
    public Method getAccessor(Class<?> raw, Field field) {
      throw new UnsupportedOperationException(
          "Records are not supported on this JVM, this method should not be called");
    }
  }
}
