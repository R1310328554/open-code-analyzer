#!/usr/bin/env python3
"""Apply Chinese annotations to gson wave-4 slice [16:25]."""

from pathlib import Path

ROOT = Path("/workspace/gson/gson-parent-2.14.0/analyzed")

REPLACEMENTS = {
    ROOT / "metrics/src/main/java/com/google/gson/metrics/BagOfPrimitives.java": [
        (
            "/**\n * Class with a bunch of primitive fields\n *\n * @author Inderjeet Singh\n */",
            "/**\n * 包含多个基本类型字段的简单 POJO，供 Gson 性能基准测试使用。\n *\n * @author Inderjeet Singh\n */",
        ),
        (
            "public class BagOfPrimitives {\n  public static final long DEFAULT_VALUE = 0;",
            "public class BagOfPrimitives {\n  /** 各字段的默认值。 */\n  public static final long DEFAULT_VALUE = 0;",
        ),
        (
            "  public String getExpectedJson() {",
            "  /** 返回与当前字段值对应的预期 JSON 字符串。 */\n  public String getExpectedJson() {",
        ),
    ],
    ROOT / "metrics/src/main/java/com/google/gson/metrics/BagOfPrimitivesDeserializationBenchmark.java": [
        (
            "/**\n * Caliper based micro benchmarks for Gson\n *\n * @author Inderjeet Singh\n * @author Jesse Wilson\n * @author Joel Leitch\n */",
            "/**\n * 基于 Caliper 的 Gson 反序列化微基准测试。\n *\n * @author Inderjeet Singh\n * @author Jesse Wilson\n * @author Joel Leitch\n */",
        ),
        (
            "  /** Benchmark to measure Gson performance for deserializing an object */",
            "  /** 测量 Gson 默认路径反序列化 {@link BagOfPrimitives} 的性能。 */",
        ),
        (
            "  /** Benchmark to measure deserializing objects by hand */",
            "  /** 测量手工使用 {@link JsonReader} 流式解析并构造对象的性能。 */",
        ),
        (
            "  /**\n   * This benchmark measures the ideal Gson performance: the cost of parsing a JSON stream and\n   * setting object values by reflection. We should strive to reduce the discrepancy between this\n   * and {@link #timeBagOfPrimitivesDefault(int)} .\n   */",
            "  /**\n   * 测量 Gson 的理想性能下界：解析 JSON 流并通过反射赋值的开销。应尽量缩小与 {@link\n   * #timeBagOfPrimitivesDefault(int)} 的差距。\n   */",
        ),
    ],
    ROOT / "metrics/src/main/java/com/google/gson/metrics/CollectionsDeserializationBenchmark.java": [
        (
            "/**\n * Caliper based micro benchmarks for Gson\n *\n * @author Inderjeet Singh\n */",
            "/**\n * 基于 Caliper 的 Gson 集合反序列化微基准测试。\n *\n * @author Inderjeet Singh\n */",
        ),
        (
            "  /** Benchmark to measure Gson performance for deserializing an object */",
            "  /** 测量 Gson 默认路径反序列化 {@code List<BagOfPrimitives>} 的性能。 */",
        ),
        (
            "  /** Benchmark to measure deserializing objects by hand */",
            "  /** 测量手工流式解析 JSON 数组并构造列表的性能。 */",
        ),
        (
            "  /**\n   * This benchmark measures the ideal Gson performance: the cost of parsing a JSON stream and\n   * setting object values by reflection. We should strive to reduce the discrepancy between this\n   * and {@link #timeCollectionsDefault(int)} .\n   */",
            "  /**\n   * 测量 Gson 的理想性能下界：解析 JSON 数组并通过反射赋值的开销。应尽量缩小与 {@link\n   * #timeCollectionsDefault(int)} 的差距。\n   */",
        ),
    ],
    ROOT / "metrics/src/main/java/com/google/gson/metrics/NonUploadingCaliperRunner.java": [
        (
            "class NonUploadingCaliperRunner {",
            "/** 运行 Caliper 基准测试但不将结果上传到远程服务器的辅助类。 */\nclass NonUploadingCaliperRunner {",
        ),
        (
            "  private static String[] concat(String first, String... others) {",
            "  /** 将首个参数与后续参数拼接为新的命令行参数数组。 */\n  private static String[] concat(String first, String... others) {",
        ),
        (
            "  public static void run(Class<?> c, String[] args) {\n    // Disable result upload; Caliper uploads results to webapp by default, see\n    // https://github.com/google/caliper/issues/356",
            "  /** 以禁用结果上传的方式启动指定基准测试类。 */\n  public static void run(Class<?> c, String[] args) {\n    // 禁用结果上传；Caliper 默认会上传结果，见 https://github.com/google/caliper/issues/356",
        ),
    ],
    ROOT / "metrics/src/main/java/com/google/gson/metrics/SerializationBenchmark.java": [
        (
            "/**\n * Caliper based micro benchmarks for Gson serialization\n *\n * @author Inderjeet Singh\n * @author Jesse Wilson\n * @author Joel Leitch\n */",
            "/**\n * 基于 Caliper 的 Gson 序列化微基准测试。\n *\n * @author Inderjeet Singh\n * @author Jesse Wilson\n * @author Joel Leitch\n */",
        ),
        (
            "  public void timeObjectSerialization(int reps) {",
            "  /** 测量将 {@link BagOfPrimitives} 序列化为 JSON 字符串的性能。 */\n  public void timeObjectSerialization(int reps) {",
        ),
    ],
    ROOT / "test-jpms/src/main/java/module-info.java": [
        (
            "/**\n * Dummy module to prevent Maven Compiler Plugin from failing if {@code module-info.java} exists\n * only in test sources:\n *\n * <blockquote>\n *\n * Can't compile test sources when main sources are missing a module descriptor\n *\n * </blockquote>\n */",
            "/**\n * 占位模块，避免仅在测试源码中存在 {@code module-info.java} 时 Maven Compiler Plugin 报错：\n *\n * <blockquote>\n *\n * Can't compile test sources when main sources are missing a module descriptor\n *\n * </blockquote>\n *\n * <p>主源码缺少模块描述符时无法编译测试源码；此空模块满足编译器要求。\n */",
        ),
    ],
    ROOT / "metrics/src/main/java/com/google/gson/metrics/ParseBenchmark.java": [
        (
            "/**\n * Measure Gson and Jackson parsing and binding performance.\n *\n * <p>This benchmark requires that ParseBenchmarkData.zip is on the classpath. That file contains\n * Twitter feed data, which is representative of what applications will be parsing.\n */",
            "/**\n * 测量 Gson 与 Jackson 的 JSON 解析与对象绑定性能。\n *\n * <p>此基准测试要求 classpath 上存在 {@code ParseBenchmarkData.zip}，其中包含 Twitter 信息流数据，代表典型应用解析场景。\n */",
        ),
        (
            "public final class ParseBenchmark {\n  @Param Document document;\n  @Param Api api;\n\n  private enum Document {",
            "public final class ParseBenchmark {\n  /** 待解析的 JSON 文档类型。 */\n  @Param Document document;\n  /** 使用的解析 API（Gson 流式/DOM/绑定或 Jackson 流式/绑定）。 */\n  @Param Api api;\n\n  /** 基准测试使用的 JSON 文档。 */\n  private enum Document {",
        ),
        (
            "  private enum Api {",
            "  /** 解析 API 实现（Gson 或 Jackson 的不同模式）。 */\n  private enum Api {",
        ),
        (
            "  public void timeParse(int reps) throws Exception {",
            "  /** 重复执行解析操作以测量耗时。 */\n  public void timeParse(int reps) throws Exception {",
        ),
        (
            "  interface Parser {",
            "  /** 解析器接口：将字符数组解析为指定文档类型的对象。 */\n  interface Parser {",
        ),
        (
            "  private static class GsonStreamParser implements Parser {",
            "  /** 使用 Gson {@link JsonReader} 逐 token 流式遍历 JSON。 */\n  private static class GsonStreamParser implements Parser {",
        ),
        (
            "  private static class GsonSkipParser implements Parser {",
            "  /** 使用 Gson {@link JsonReader#skipValue()} 跳过整个 JSON 文档。 */\n  private static class GsonSkipParser implements Parser {",
        ),
        (
            "  private static class JacksonStreamParser implements Parser {",
            "  /** 使用 Jackson 流式 API 逐 token 遍历 JSON。 */\n  private static class JacksonStreamParser implements Parser {",
        ),
        (
            "  private static class GsonDomParser implements Parser {",
            "  /** 使用 Gson {@link JsonParser} 将 JSON 解析为 DOM 树。 */\n  private static class GsonDomParser implements Parser {",
        ),
        (
            "  private static class GsonBindParser implements Parser {",
            "  /** 使用 Gson {@link Gson#fromJson} 将 JSON 绑定为 Java 对象。 */\n  private static class GsonBindParser implements Parser {",
        ),
        (
            "  private static class JacksonBindParser implements Parser {",
            "  /** 使用 Jackson {@link ObjectMapper} 将 JSON 绑定为 Java 对象。 */\n  private static class JacksonBindParser implements Parser {",
        ),
        (
            "  @SuppressWarnings(\"MemberName\")\n  static class Tweet {",
            "  /** Twitter 推文模型，字段名与 JSON 键对应。 */\n  @SuppressWarnings(\"MemberName\")\n  static class Tweet {",
        ),
        (
            "  @SuppressWarnings(\"MemberName\")\n  static class User {",
            "  /** Twitter 用户模型。 */\n  @SuppressWarnings(\"MemberName\")\n  static class User {",
        ),
        (
            "  static class Feed {",
            "  /** Google Reader 订阅源模型。 */\n  static class Feed {",
        ),
    ],
}

# LegacyProtoTypeAdapterFactory and ProtoTypeAdapter handled separately due to size
LEGACY_REPLACEMENTS = [
    (
        "/**\n * A {@link TypeAdapterFactory} that supports the broken JSON mapping that Gson users get for\n * protobuf messages if they forget to register a proper handler such as {@link ProtoTypeAdapter}.\n * <b>This class is a migration aid.</b> If your project currently uses it, you should consider\n * migrating to {@link ProtoTypeAdapter} or similar. That does <i>change</i> the JSON encoding,\n * though, so there can be compatibility concerns.\n *\n * <p>The default JSON mapping for protobuf messages is derived by examining the private fields of\n * the generated protobuf classes. That's obviously very fragile, and leads to ugly JSON that is not\n * what people would reasonably expect. For example, here is what a serialized {@code\n * .google.protobuf.Duration} might look like:\n *\n * <pre>\n * {\n *   \"seconds_\": 10,\n *   \"nanos_\": 20,\n *   \"bitField0_\": 3\n * }\n * </pre>\n *\n * <p>Notice the underscore at the end of each field name and the extra field {@code bitField0_}\n * whose meaning is unlikely to be obvious to typical observers.\n *\n * <p>This class does not support Java Proto Lite.\n */",
        "/**\n * 为未注册 {@link ProtoTypeAdapter} 等正确处理器时 Gson 对 protobuf 消息产生的错误 JSON 映射提供兼容的 {@link\n * TypeAdapterFactory}。<b>此类为迁移辅助工具。</b>若项目当前使用它，应考虑迁移至 {@link ProtoTypeAdapter} 或类似实现。迁移会<i>改变</i>\n * JSON 编码，因此可能存在兼容性问题。\n *\n * <p>protobuf 消息的默认 JSON 映射来自对生成类私有字段的反射，非常脆弱，且会产生不符合预期的 JSON。例如序列化后的 {@code\n * .google.protobuf.Duration} 可能如下：\n *\n * <pre>\n * {\n *   \"seconds_\": 10,\n *   \"nanos_\": 20,\n *   \"bitField0_\": 3\n * }\n * </pre>\n *\n * <p>注意字段名末尾的下划线，以及含义不明显的 {@code bitField0_} 额外字段。\n *\n * <p>不支持 Java Proto Lite。\n */",
    ),
    (
        "  // In what follows, RTAF means ReflectiveTypeAdapterFactory, which is the fallback that Gson\n  // uses for classes that don't have an explicit TypeAdapter. When serializing or deserializing\n  // a Java object, RTAF reflects on the instance fields of the object's class to determine\n  // its JSON representation. To output JSON, it outputs a JSON object with one key-value\n  // pair for each instance field. The keys are the field names and the values are the JSON\n  // representations of the field contents. To read JSON, it creates a new instance of the class\n  // using its private no-arg constructor then reflectively sets each of the instance fields based\n  // on the key-value pairs in the JSON object. Our Adapter here mimics both of these things\n  // without depending on the private implementation details of generated proto message classes.\n  // Specifically, it attempts to behave the same as RTAF would when reflecting on the version of\n  // proto generated code that was current in early 2026.\n\n  private static final class Adapter<T extends Message> extends TypeAdapter<T> {",
        "  // 下文 RTAF 指 ReflectiveTypeAdapterFactory，即 Gson 对无显式 TypeAdapter 的类的回退方案。\n  // 序列化/反序列化 Java 对象时，RTAF 通过反射实例字段决定 JSON 表示：写出时每个字段对应一个键值对；\n  // 读入时用私有无参构造创建实例再反射赋值。本 Adapter 在不依赖生成 proto 类私有实现细节的前提下模拟该行为，\n  // 尽量与 2026 年初 proto 生成代码被 RTAF 反射时的表现一致。\n\n  /** 针对具体 protobuf 消息类型的适配器。 */\n  private static final class Adapter<T extends Message> extends TypeAdapter<T> {",
    ),
    (
        "    // The field `private int bitField0_` tracks presence for the first 32 fields that have\n    // presence, with bit 0 corresponding to the first field, and so on. The field `private int\n    // bitField1_` tracks presence for the next 32 fields with presence, and so on.",
        "    // 字段 `private int bitField0_` 用前 32 个带 presence 的字段的位掩码跟踪 presence（bit 0 对应第一个字段，依此类推）。\n    // `private int bitField1_` 跟踪下一组 32 个，以此类推。",
    ),
    (
        "      // Now use the presence bitmask to determine whether to keep each value read from the JSON. If\n      // a field with presence does not have a 1 bit in the bitmask, then we clear it, but only if\n      // its value is the same as the default value. Its Java field in the source message will have\n      // been copied into the JSON by RTAF regardless of \"presence\", so if it really is absent then\n      // that value should be the same as the default. Doing this avoids a problem where the set of\n      // fields with presence might have changed between the time the proto was converted to JSON\n      // and now. If that happens, the bits in the bitmask may be completely bogus, but at worst we\n      // will mark a field as absent when it should be present but with the default value. That is\n      // actually a much better failure mode than RTAF. If the meaning of the bits in the bitmask\n      // has changed then RTAF can mark a field as absent even though a non-default value has been\n      // read into the corresponding Java field.\n      // We do have another failure mode that RTAF doesn't have: if the default value of a field has\n      // changed, then we might set that field to the old default value when it should have been\n      // absent.",
        "      // 根据 presence 位掩码决定 JSON 中读到的各字段是否保留：带 presence 的字段若对应位为 0 且值等于默认值则清除。\n      // RTAF 会无视 presence 将 Java 字段写入 JSON，故真正 absent 时值应等于默认值。这样可缓解 proto 定义变更后位掩码失效的问题。",
    ),
    (
        "      // Finally, set any string-valued oneof fields.",
        "      // 设置字符串类型 oneof 字段",
    ),
    (
        "    // A oneof `foo` is represented as two fields in the generated proto class: `int fooCase` and\n    // `Object foo`. RTAF can deserialize this, but except in special cases it will produce a\n    // corrupt message where getting the set oneof field will throw a `ClassCastException`.\n    // The most important of those special cases is when the oneof field is of type `string`. We\n    // handle that here: if the JSON has `\"fooCase\":2,\"foo\":\"hello\"` then the oneof case is a\n    // string and we will set it. Because of the way the JSON is read from a stream, we have to\n    // record the \"fooCase\" and \"foo\" fields separately, and join the read values from the two maps\n    // here at the end.",
        "    // oneof `foo` 在生成类中表示为 `int fooCase` 与 `Object foo` 两个字段。RTAF 可反序列化，\n    // 但除字符串 oneof 等特殊情况外常产生损坏消息（读取已设 oneof 字段会抛 ClassCastException）。\n    // 此处处理 JSON 形如 `\"fooCase\":2,\"foo\":\"hello\"` 的字符串 oneof；因流式读取需分别记录 case 与 value 再在末尾合并。",
    ),
    (
        "      // Since RTAF doesn't know any better, it will serialize a ByteString as a JSON object with\n      // two fields:\n      // - \"bytes\": the actual byte contents of the ByteString, a JSON array of integers;\n      // - \"hash\": an integer hash code of the ByteString.",
        "      // RTAF 会将 ByteString 序列化为含 \"bytes\"（字节数组）与 \"hash\"（哈希码）两个字段的 JSON 对象",
    ),
    (
        "      // Consistently with RTAF, we write all fields, regardless of presence.",
        "      // 与 RTAF 一致：写出所有字段，不论 presence",
    ),
    (
        "          // Writing null is consistent with what RTAF does, and avoids the infinite recursion we\n          // could otherwise get for a recursive message.",
        "          // 写 null 与 RTAF 行为一致，并避免递归消息导致无限递归",
    ),
    (
        "  private static class DynamicAdapter extends TypeAdapter<Message> {",
        "  /** 处理 {@code Message.class} 基类时的动态分发适配器。 */\n  private static class DynamicAdapter extends TypeAdapter<Message> {",
    ),
]

PROTO_REPLACEMENTS = [
    (
        "/**\n * GSON type adapter for protocol buffers that knows how to serialize enums either by using their\n * values or their names, and also supports custom proto field names.\n *\n * <p>You can specify which case representation is used for the proto fields when writing/reading\n * the JSON payload by calling {@link Builder#setFieldNameSerializationFormat(CaseFormat,\n * CaseFormat)}.\n *\n * <p>An example of default serialization/deserialization using custom proto field names is shown\n * below:\n *\n * <pre>\n * message MyMessage {\n *   // Will be serialized as 'osBuildID' instead of the default 'osBuildId'.\n *   string os_build_id = 1 [(serialized_name) = \"osBuildID\"];\n * }\n * </pre>\n *\n * @author Inderjeet Singh\n * @author Emmanuel Cron\n * @author Stanley Wang\n */",
        "/**\n * 用于 protocol buffers 的 GSON 类型适配器，支持按枚举数值或名称序列化，并支持自定义 proto 字段名。\n *\n * <p>可通过 {@link Builder#setFieldNameSerializationFormat(CaseFormat, CaseFormat)} 指定读写 JSON 时字段名的命名格式。\n *\n * <p>使用自定义 proto 字段名的默认序列化/反序列化示例：\n *\n * <pre>\n * message MyMessage {\n *   // 将序列化为 'osBuildID' 而非默认的 'osBuildId'。\n *   string os_build_id = 1 [(serialized_name) = \"osBuildID\"];\n * }\n * </pre>\n *\n * @author Inderjeet Singh\n * @author Emmanuel Cron\n * @author Stanley Wang\n */",
    ),
    (
        "  /** Determines how enum <u>values</u> should be serialized. */",
        "  /** 控制枚举<i>值</i>的序列化方式。 */",
    ),
    (
        "    /**\n     * Serializes and deserializes enum values using their <b>number</b>. When this is used, custom\n     * value names set on enums are ignored.\n     */",
        "    /**\n     * 使用枚举的<b>数值</b>序列化与反序列化；此时忽略枚举上的自定义值名。\n     */",
    ),
    (
        "    /** Serializes and deserializes enum values using their <b>name</b>. */",
        "    /** 使用枚举的<b>名称</b>序列化与反序列化。 */",
    ),
    (
        "  /** Builder for {@link ProtoTypeAdapter}s. */",
        "  /** {@link ProtoTypeAdapter} 的构建器。 */",
    ),
    (
        "    /**\n     * Sets the field names serialization format. The first parameter defines how to read the format\n     * of the proto field names you are converting to JSON. The second parameter defines which\n     * format to use when serializing them.\n     *\n     * <p>For example, if you use the following parameters: {@link CaseFormat#LOWER_UNDERSCORE},\n     * {@link CaseFormat#LOWER_CAMEL}, the following conversion will occur:\n     *\n     * <pre>{@code\n     * PROTO     <->  JSON\n     * my_field       myField\n     * foo            foo\n     * n__id_ct       nIdCt\n     * }</pre>\n     */",
        "    /**\n     * 设置字段名序列化格式。第一个参数表示 proto 字段名的源格式，第二个参数表示写出 JSON 时使用的目标格式。\n     *\n     * <p>例如使用 {@link CaseFormat#LOWER_UNDERSCORE} 与 {@link CaseFormat#LOWER_CAMEL} 时：\n     *\n     * <pre>{@code\n     * PROTO     <->  JSON\n     * my_field       myField\n     * foo            foo\n     * n__id_ct       nIdCt\n     * }</pre>\n     */",
    ),
    (
        "    /**\n     * Adds a field proto annotation that, when set, overrides the default field name\n     * serialization/deserialization. For example, if you add the '{@code serialized_name}'\n     * annotation and you define a field in your proto like the one below:\n     *\n     * <pre>\n     * string client_app_id = 1 [(serialized_name) = \"appId\"];\n     * </pre>\n     *\n     * ...the adapter will serialize the field using '{@code appId}' instead of the default ' {@code\n     * clientAppId}'. This lets you customize the name serialization of any proto field.\n     */",
        "    /**\n     * 添加 proto 字段注解扩展：当字段设置该注解时，覆盖默认的字段名序列化/反序列化。例如添加 {@code serialized_name}\n     * 注解且 proto 定义如下：\n     *\n     * <pre>\n     * string client_app_id = 1 [(serialized_name) = \"appId\"];\n     * </pre>\n     *\n     * ...适配器将使用 {@code appId} 而非默认的 {@code clientAppId} 序列化该字段。\n     */",
    ),
    (
        "    /**\n     * Adds an enum value proto annotation that, when set, overrides the default <b>enum</b> value\n     * serialization/deserialization of this adapter. For example, if you add the ' {@code\n     * serialized_value}' annotation and you define an enum in your proto like the one below:\n     *\n     * <pre>\n     * enum MyEnum {\n     *   UNKNOWN = 0;\n     *   CLIENT_APP_ID = 1 [(serialized_value) = \"APP_ID\"];\n     *   TWO = 2 [(serialized_value) = \"2\"];\n     * }\n     * </pre>\n     *\n     * ...the adapter will serialize the value {@code CLIENT_APP_ID} as \"{@code APP_ID}\" and the\n     * value {@code TWO} as \"{@code 2}\". This works for both serialization and deserialization.\n     *\n     * <p>Note that you need to set the enum serialization of this adapter to {@link\n     * EnumSerialization#NAME}, otherwise these annotations will be ignored.\n     */",
        "    /**\n     * 添加枚举值 proto 注解扩展：当设置时覆盖默认的<b>枚举值</b>序列化/反序列化。例如添加 {@code serialized_value}\n     * 注解且 proto 枚举定义如下：\n     *\n     * <pre>\n     * enum MyEnum {\n     *   UNKNOWN = 0;\n     *   CLIENT_APP_ID = 1 [(serialized_value) = \"APP_ID\"];\n     *   TWO = 2 [(serialized_value) = \"2\"];\n     * }\n     * </pre>\n     *\n     * ...适配器将 {@code CLIENT_APP_ID} 序列化为 \"{@code APP_ID}\"，{@code TWO} 为 \"{@code 2}\"，读写均适用。\n     *\n     * <p>须将枚举序列化模式设为 {@link EnumSerialization#NAME}，否则忽略这些注解。\n     */",
    ),
    (
        "    /**\n     * Sets or unsets a flag (default false) that, when set, causes the adapter to use the {@code\n     * json_name} field option from a proto field for serialization. Unlike other field options that\n     * can be defined as annotations on a proto field, {@code json_name} cannot be accessed via a\n     * proto field's {@link FieldDescriptor#getOptions} and registered via {@link\n     * ProtoTypeAdapter.Builder#addSerializedNameExtension}.\n     *\n     * <p>This flag is subordinate to any custom serialized name extensions added to this adapter.\n     * In other words, serialized name extensions take precedence over this setting. For example, a\n     * field defined like:\n     *\n     * <pre>\n     * string client_app_id = 1 [json_name = \"foo\", (serialized_name) = \"bar\"];\n     * </pre>\n     *\n     * ...will be serialized as '{@code bar}' if {@code shouldUseJsonNameFieldOption} is set to\n     * {@code true} and the '{@code serialized_name}' annotation is added to the adapter.\n     *\n     * @since 2.12.0\n     */",
        "    /**\n     * 设置是否使用 proto 字段的 {@code json_name} 选项进行序列化（默认 false）。与可通过 {@link\n     * FieldDescriptor#getOptions} 注册的自定义字段名扩展不同，{@code json_name} 无法通过 {@link\n     * ProtoTypeAdapter.Builder#addSerializedNameExtension} 注册。\n     *\n     * <p>自定义 serialized_name 扩展优先于本设置。例如字段定义为：\n     *\n     * <pre>\n     * string client_app_id = 1 [json_name = \"foo\", (serialized_name) = \"bar\"];\n     * </pre>\n     *\n     * ...在 {@code shouldUseJsonNameFieldOption} 为 true 且适配器已添加 {@code serialized_name} 扩展时，将序列化为\n     * {@code bar}。\n     *\n     * @since 2.12.0\n     */",
    ),
    (
        "  /**\n   * Creates a new {@link ProtoTypeAdapter} builder, defaulting enum serialization to {@link\n   * EnumSerialization#NAME} and converting field serialization from {@link\n   * CaseFormat#LOWER_UNDERSCORE} to {@link CaseFormat#LOWER_CAMEL}.\n   */",
        "  /**\n   * 创建 {@link ProtoTypeAdapter} 构建器，默认枚举序列化为 {@link EnumSerialization#NAME}，字段名从 {@link\n   * CaseFormat#LOWER_UNDERSCORE} 转为 {@link CaseFormat#LOWER_CAMEL}。\n   */",
    ),
    (
        "      // Invoke the ProtoClass.newBuilder() method",
        "      // 调用 ProtoClass.newBuilder()",
    ),
    (
        "      // Call setters on all of the available fields",
        "      // 对所有可用字段调用 setter",
    ),
    (
        "  /**\n   * Retrieves the custom field name for a given FieldDescriptor via its field options, falling back\n   * to its name as a default.\n   */",
        "  /**\n   * 从 {@link FieldDescriptor} 的字段选项获取自定义序列化字段名，若无则使用 proto 字段名的格式转换结果。\n   */",
    ),
    (
        "  /**\n   * Retrieves the custom enum value name from the given options, and if not found, returns the\n   * specified default value.\n   */",
        "  /**\n   * 从给定选项中获取自定义枚举值名；未找到时返回指定的默认值。\n   */",
    ),
    (
        "  /**\n   * Returns the enum value to use for serialization, depending on the value of {@link\n   * EnumSerialization} that was given to this adapter.\n   */",
        "  /**\n   * 根据构造时指定的 {@link EnumSerialization} 返回用于序列化的枚举值表示。\n   */",
    ),
    (
        "  /**\n   * Finds an enum value in the given {@link EnumDescriptor} that matches the given JSON element,\n   * either by name if the current adapter is using {@link EnumSerialization#NAME}, otherwise by\n   * number. If matching by name, it uses the extension value if it is defined, otherwise it uses\n   * its default value.\n   *\n   * @throws IllegalArgumentException if a matching name/number was not found\n   */",
        "  /**\n   * 在 {@link EnumDescriptor} 中查找与 JSON 元素匹配的枚举值：{@link EnumSerialization#NAME} 时按名称（含扩展值），否则按数值。\n   *\n   * @throws IllegalArgumentException 未找到匹配的名称或数值时\n   */",
    ),
]


def apply_replacements(path: Path, pairs):
    text = path.read_text(encoding="utf-8")
    original = text
    for old, new in pairs:
        if old not in text:
            raise ValueError(f"Pattern not found in {path}:\n{old[:80]}...")
        text = text.replace(old, new, 1)
    if text != original:
        path.write_text(text, encoding="utf-8")
        print(f"OK {path}")
    else:
        print(f"SKIP (no change) {path}")


def main():
    for path, pairs in REPLACEMENTS.items():
        apply_replacements(path, pairs)
    apply_replacements(
        ROOT / "proto/src/main/java/com/google/gson/protobuf/LegacyProtoTypeAdapterFactory.java",
        LEGACY_REPLACEMENTS,
    )
    apply_replacements(
        ROOT / "proto/src/main/java/com/google/gson/protobuf/ProtoTypeAdapter.java",
        PROTO_REPLACEMENTS,
    )


if __name__ == "__main__":
    main()
