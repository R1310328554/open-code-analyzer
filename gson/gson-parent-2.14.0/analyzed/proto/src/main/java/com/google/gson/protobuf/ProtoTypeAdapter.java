/*
 * Copyright (C) 2010 Google Inc.
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

package com.google.gson.protobuf;

import static java.util.Objects.requireNonNull;

import com.google.common.base.CaseFormat;
import com.google.common.collect.MapMaker;
import com.google.common.reflect.TypeToken;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.protobuf.DescriptorProtos.EnumValueOptions;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Extension;
import com.google.protobuf.Message;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * 用于 protocol buffers 的 GSON 类型适配器，支持按枚举数值或名称序列化，并支持自定义 proto 字段名。
 *
 * <p>可通过 {@link Builder#setFieldNameSerializationFormat(CaseFormat, CaseFormat)} 指定读写 JSON 时字段名的命名格式。
 *
 * <p>使用自定义 proto 字段名的默认序列化/反序列化示例：
 *
 * <pre>
 * message MyMessage {
 *   // 将序列化为 'osBuildID' 而非默认的 'osBuildId'。
 *   string os_build_id = 1 [(serialized_name) = "osBuildID"];
 * }
 * </pre>
 *
 * @author Inderjeet Singh
 * @author Emmanuel Cron
 * @author Stanley Wang
 */
public class ProtoTypeAdapter implements JsonSerializer<Message>, JsonDeserializer<Message> {
  /** 控制枚举<i>值</i>的序列化方式。 */
  public enum EnumSerialization {
    /**
     * 使用枚举的<b>数值</b>序列化与反序列化；此时忽略枚举上的自定义值名。
     */
    NUMBER,
    /** 使用枚举的<b>名称</b>序列化与反序列化。 */
    NAME;
  }

  /** {@link ProtoTypeAdapter} 的构建器。 */
  public static class Builder {
    private final Set<Extension<FieldOptions, String>> serializedNameExtensions;
    private final Set<Extension<EnumValueOptions, String>> serializedEnumValueExtensions;
    private EnumSerialization enumSerialization;
    private CaseFormat protoFormat;
    private CaseFormat jsonFormat;
    private boolean shouldUseJsonNameFieldOption;

    private Builder(
        EnumSerialization enumSerialization,
        CaseFormat fromFieldNameFormat,
        CaseFormat toFieldNameFormat) {
      this.serializedNameExtensions = new HashSet<>();
      this.serializedEnumValueExtensions = new HashSet<>();
      setEnumSerialization(enumSerialization);
      setFieldNameSerializationFormat(fromFieldNameFormat, toFieldNameFormat);
      this.shouldUseJsonNameFieldOption = false;
    }

    @CanIgnoreReturnValue
    public Builder setEnumSerialization(EnumSerialization enumSerialization) {
      this.enumSerialization = requireNonNull(enumSerialization);
      return this;
    }

    /**
     * 设置字段名序列化格式。第一个参数表示 proto 字段名的源格式，第二个参数表示写出 JSON 时使用的目标格式。
     *
     * <p>For example, if you use the following parameters: {@link CaseFormat#LOWER_UNDERSCORE},
     * {@link CaseFormat#LOWER_CAMEL}, the following conversion will occur:
     *
     * <pre>{@code
     * PROTO     <->  JSON
     * my_field       myField
     * foo            foo
     * n__id_ct       nIdCt
     * }</pre>
     */
    @CanIgnoreReturnValue
    public Builder setFieldNameSerializationFormat(
        CaseFormat fromFieldNameFormat, CaseFormat toFieldNameFormat) {
      this.protoFormat = fromFieldNameFormat;
      this.jsonFormat = toFieldNameFormat;
      return this;
    }

    /**
     * Adds a field proto annotation that, when set, overrides the default field name
     * serialization/deserialization. For example, if you add the '{@code serialized_name}'
     * annotation and you define a field in your proto like the one below:
     *
     * <pre>
     * string client_app_id = 1 [(serialized_name) = "appId"];
     * </pre>
     *
     * ...the adapter will serialize the field using '{@code appId}' instead of the default ' {@code
     * clientAppId}'. This lets you customize the name serialization of any proto field.
     */
    @CanIgnoreReturnValue
    public Builder addSerializedNameExtension(
        Extension<FieldOptions, String> serializedNameExtension) {
      serializedNameExtensions.add(requireNonNull(serializedNameExtension));
      return this;
    }

    /**
     * Adds an enum value proto annotation that, when set, overrides the default <b>enum</b> value
     * serialization/deserialization of this adapter. For example, if you add the ' {@code
     * serialized_value}' annotation and you define an enum in your proto like the one below:
     *
     * <pre>
     * enum MyEnum {
     *   UNKNOWN = 0;
     *   CLIENT_APP_ID = 1 [(serialized_value) = "APP_ID"];
     *   TWO = 2 [(serialized_value) = "2"];
     * }
     * </pre>
     *
     * ...the adapter will serialize the value {@code CLIENT_APP_ID} as "{@code APP_ID}" and the
     * value {@code TWO} as "{@code 2}". This works for both serialization and deserialization.
     *
     * <p>Note that you need to set the enum serialization of this adapter to {@link
     * EnumSerialization#NAME}, otherwise these annotations will be ignored.
     */
    @CanIgnoreReturnValue
    public Builder addSerializedEnumValueExtension(
        Extension<EnumValueOptions, String> serializedEnumValueExtension) {
      serializedEnumValueExtensions.add(requireNonNull(serializedEnumValueExtension));
      return this;
    }

    /**
     * Sets or unsets a flag (default false) that, when set, causes the adapter to use the {@code
     * json_name} field option from a proto field for serialization. Unlike other field options that
     * can be defined as annotations on a proto field, {@code json_name} cannot be accessed via a
     * proto field's {@link FieldDescriptor#getOptions} and registered via {@link
     * ProtoTypeAdapter.Builder#addSerializedNameExtension}.
     *
     * <p>This flag is subordinate to any custom serialized name extensions added to this adapter.
     * In other words, serialized name extensions take precedence over this setting. For example, a
     * field defined like:
     *
     * <pre>
     * string client_app_id = 1 [json_name = "foo", (serialized_name) = "bar"];
     * </pre>
     *
     * ...will be serialized as '{@code bar}' if {@code shouldUseJsonNameFieldOption} is set to
     * {@code true} and the '{@code serialized_name}' annotation is added to the adapter.
     *
     * @since 2.12.0
     */
    @CanIgnoreReturnValue
    public Builder setShouldUseJsonNameFieldOption(boolean shouldUseJsonNameFieldOption) {
      this.shouldUseJsonNameFieldOption = shouldUseJsonNameFieldOption;
      return this;
    }

    public ProtoTypeAdapter build() {
      return new ProtoTypeAdapter(
          enumSerialization,
          protoFormat,
          jsonFormat,
          serializedNameExtensions,
          serializedEnumValueExtensions,
          shouldUseJsonNameFieldOption);
    }
  }

  /**
   * 创建 {@link ProtoTypeAdapter} 构建器，默认枚举序列化为 {@link EnumSerialization#NAME}，字段名从 {@link CaseFormat#LOWER_UNDERSCORE} 转为 {@link CaseFormat#LOWER_CAMEL}。
   */
  public static Builder newBuilder() {
    return new Builder(EnumSerialization.NAME, CaseFormat.LOWER_UNDERSCORE, CaseFormat.LOWER_CAMEL);
  }

  private static final FieldDescriptor.Type ENUM_TYPE = FieldDescriptor.Type.ENUM;

  private static final ConcurrentMap<String, ConcurrentMap<Class<?>, Method>> mapOfMapOfMethods =
      new MapMaker().makeMap();

  private final EnumSerialization enumSerialization;
  private final CaseFormat protoFormat;
  private final CaseFormat jsonFormat;
  private final Set<Extension<FieldOptions, String>> serializedNameExtensions;
  private final Set<Extension<EnumValueOptions, String>> serializedEnumValueExtensions;
  private final boolean shouldUseJsonNameFieldOption;

  private ProtoTypeAdapter(
      EnumSerialization enumSerialization,
      CaseFormat protoFormat,
      CaseFormat jsonFormat,
      Set<Extension<FieldOptions, String>> serializedNameExtensions,
      Set<Extension<EnumValueOptions, String>> serializedEnumValueExtensions,
      boolean shouldUseJsonNameFieldOption) {
    this.enumSerialization = enumSerialization;
    this.protoFormat = protoFormat;
    this.jsonFormat = jsonFormat;
    this.serializedNameExtensions = serializedNameExtensions;
    this.serializedEnumValueExtensions = serializedEnumValueExtensions;
    this.shouldUseJsonNameFieldOption = shouldUseJsonNameFieldOption;
  }

  @Override
  public JsonElement serialize(Message src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject ret = new JsonObject();
    Map<FieldDescriptor, Object> fields = src.getAllFields();

    for (Map.Entry<FieldDescriptor, Object> fieldPair : fields.entrySet()) {
      FieldDescriptor desc = fieldPair.getKey();
      String name = getCustSerializedName(desc);

      if (desc.getType() == ENUM_TYPE) {
        // Enum collections are also returned as ENUM_TYPE
        if (fieldPair.getValue() instanceof Collection) {
          // Build the array to avoid infinite loop
          JsonArray array = new JsonArray();
          @SuppressWarnings("unchecked")
          Collection<EnumValueDescriptor> enumDescs =
              (Collection<EnumValueDescriptor>) fieldPair.getValue();
          for (EnumValueDescriptor enumDesc : enumDescs) {
            array.add(context.serialize(getEnumValue(enumDesc)));
            ret.add(name, array);
          }
        } else {
          EnumValueDescriptor enumDesc = ((EnumValueDescriptor) fieldPair.getValue());
          ret.add(name, context.serialize(getEnumValue(enumDesc)));
        }
      } else {
        ret.add(name, context.serialize(fieldPair.getValue()));
      }
    }
    return ret;
  }

  @Override
  public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    try {
      JsonObject jsonObject = json.getAsJsonObject();
      @SuppressWarnings("unchecked")
      Class<? extends Message> protoClass = (Class<? extends Message>) typeOfT;

      if (DynamicMessage.class.isAssignableFrom(protoClass)) {
        throw new IllegalStateException("only generated messages are supported");
      }

      // 调用 ProtoClass.newBuilder()
      Message.Builder protoBuilder =
          (Message.Builder) getCachedMethod(protoClass, "newBuilder").invoke(null);

      Message defaultInstance =
          (Message) getCachedMethod(protoClass, "getDefaultInstance").invoke(null);

      Descriptor protoDescriptor =
          (Descriptor) getCachedMethod(protoClass, "getDescriptor").invoke(null);
      // 对所有可用字段调用 setter
      for (FieldDescriptor fieldDescriptor : protoDescriptor.getFields()) {
        String jsonFieldName = getCustSerializedName(fieldDescriptor);

        JsonElement jsonElement = jsonObject.get(jsonFieldName);
        if (jsonElement != null && !jsonElement.isJsonNull()) {
          // Do not reuse jsonFieldName here, it might have a custom value
          Object fieldValue;
          if (fieldDescriptor.getType() == ENUM_TYPE) {
            if (jsonElement.isJsonArray()) {
              // Handling array
              Collection<EnumValueDescriptor> enumCollection =
                  new ArrayList<>(jsonElement.getAsJsonArray().size());
              for (JsonElement element : jsonElement.getAsJsonArray()) {
                enumCollection.add(
                    findValueByNameAndExtension(fieldDescriptor.getEnumType(), element));
              }
              fieldValue = enumCollection;
            } else {
              // No array, just a plain value
              fieldValue = findValueByNameAndExtension(fieldDescriptor.getEnumType(), jsonElement);
            }
            protoBuilder.setField(fieldDescriptor, fieldValue);
          } else if (fieldDescriptor.isRepeated()) {
            // If the type is an array, then we have to grab the type from the class.
            // protobuf java field names are always lower camel case
            String protoArrayFieldName =
                protoFormat.to(CaseFormat.LOWER_CAMEL, fieldDescriptor.getName()) + "_";
            Field protoArrayField = protoClass.getDeclaredField(protoArrayFieldName);

            @SuppressWarnings("unchecked")
            TypeToken<? extends List<?>> protoArrayFieldType =
                (TypeToken<? extends List<?>>) TypeToken.of(protoArrayField.getGenericType());
            // Get the type as `List<E>`, otherwise type might be Protobuf internal interface for
            // which no instance can be created
            Type protoArrayResolvedFieldType =
                protoArrayFieldType.getSupertype(List.class).getType();
            fieldValue = context.deserialize(jsonElement, protoArrayResolvedFieldType);
            protoBuilder.setField(fieldDescriptor, fieldValue);
          } else {
            Object field = defaultInstance.getField(fieldDescriptor);
            fieldValue = context.deserialize(jsonElement, field.getClass());
            protoBuilder.setField(fieldDescriptor, fieldValue);
          }
        }
      }
      return protoBuilder.build();
    } catch (Exception e) {
      throw new JsonParseException("Error while parsing proto", e);
    }
  }

  /**
   * 从 {@link FieldDescriptor} 的字段选项获取自定义序列化字段名，若无则使用 proto 字段名的格式转换结果。
   */
  private String getCustSerializedName(FieldDescriptor fieldDescriptor) {
    FieldOptions options = fieldDescriptor.getOptions();
    for (Extension<FieldOptions, String> extension : serializedNameExtensions) {
      if (options.hasExtension(extension)) {
        return options.getExtension(extension);
      }
    }
    if (shouldUseJsonNameFieldOption && fieldDescriptor.toProto().hasJsonName()) {
      return fieldDescriptor.getJsonName();
    }
    return protoFormat.to(jsonFormat, fieldDescriptor.getName());
  }

  /**
   * 从给定选项中获取自定义枚举值名；未找到时返回指定的默认值。
   */
  private String getCustSerializedEnumValue(EnumValueOptions options, String defaultValue) {
    for (Extension<EnumValueOptions, String> extension : serializedEnumValueExtensions) {
      if (options.hasExtension(extension)) {
        return options.getExtension(extension);
      }
    }
    return defaultValue;
  }

  /**
   * 根据构造时指定的 {@link EnumSerialization} 返回用于序列化的枚举值表示。
   */
  private Object getEnumValue(EnumValueDescriptor enumDesc) {
    if (enumSerialization == EnumSerialization.NAME) {
      return getCustSerializedEnumValue(enumDesc.getOptions(), enumDesc.getName());
    } else {
      return enumDesc.getNumber();
    }
  }

  /**
   * 在 {@link EnumDescriptor} 中查找与 JSON 元素匹配的枚举值：{@link EnumSerialization#NAME} 时按名称（含扩展值），否则按数值。 If matching by name, it uses the extension value if it is defined, otherwise it uses
   * its default value.
   *
   * @throws IllegalArgumentException 未找到匹配的名称或数值时
   */
  private EnumValueDescriptor findValueByNameAndExtension(
      EnumDescriptor desc, JsonElement jsonElement) {
    if (enumSerialization == EnumSerialization.NAME) {
      // With enum name
      for (EnumValueDescriptor enumDesc : desc.getValues()) {
        String enumValue = getCustSerializedEnumValue(enumDesc.getOptions(), enumDesc.getName());
        if (enumValue.equals(jsonElement.getAsString())) {
          return enumDesc;
        }
      }
      throw new IllegalArgumentException(
          String.format("Unrecognized enum name: %s", jsonElement.getAsString()));
    } else {
      // With enum value
      EnumValueDescriptor fieldValue = desc.findValueByNumber(jsonElement.getAsInt());
      if (fieldValue == null) {
        throw new IllegalArgumentException(
            String.format("Unrecognized enum value: %d", jsonElement.getAsInt()));
      }
      return fieldValue;
    }
  }

  private static Method getCachedMethod(
      Class<?> clazz, String methodName, Class<?>... methodParamTypes)
      throws NoSuchMethodException {
    ConcurrentMap<Class<?>, Method> mapOfMethods = mapOfMapOfMethods.get(methodName);
    if (mapOfMethods == null) {
      mapOfMethods = new MapMaker().makeMap();
      ConcurrentMap<Class<?>, Method> previous =
          mapOfMapOfMethods.putIfAbsent(methodName, mapOfMethods);
      mapOfMethods = previous == null ? mapOfMethods : previous;
    }

    Method method = mapOfMethods.get(clazz);
    if (method == null) {
      method = clazz.getMethod(methodName, methodParamTypes);
      mapOfMethods.putIfAbsent(clazz, method);
      // NB: it doesn't matter which method we return in the event of a race.
    }
    return method;
  }
}
