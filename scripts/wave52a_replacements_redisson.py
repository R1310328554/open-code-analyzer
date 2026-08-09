"""Chinese annotation replacements for Redisson 4.7.0 wave-52a annotation/array [0:15]."""
from __future__ import annotations

_A = "redisson/src/main/java/org/redisson/api/"

W52A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    f"{_A}annotation/EmptyAsAbsent.java": [
        (
            '/**\n * Marks a Reactor {@code Mono}-returning method whose result, when the\n * underlying value is an empty {@link java.util.Map} or\n * {@link java.util.Collection}, should complete without emitting an\n * {@code onNext} signal.\n *\n * <p>Without this annotation, a method returning {@code Mono<Map<K, V>>}\n * whose underlying operation yields an empty map will emit that empty map\n * via {@code onNext} and then complete. With this annotation, the same\n * operation completes the {@code Mono} empty &mdash; allowing reactive\n * pipelines to use {@code switchIfEmpty}, {@code defaultIfEmpty},\n * {@code flatMap}, and similar operators as intended for one-or-zero\n * publishers.\n *\n * @author Nikita Koksharov\n *\n */',
            '/**\n * 标记返回 Reactor {@code Mono} 的方法：当底层结果为空的 {@link java.util.Map} 或\n * {@link java.util.Collection} 时，{@code Mono} 应以空完成结束，而不发出 {@code onNext} 信号。\n *\n * <p>未加此注解时，返回 {@code Mono<Map<K, V>>} 的方法在底层得到空 Map 时会通过 {@code onNext}\n * 发出该空 Map 再完成。加上此注解后，相同操作会以空 {@code Mono} 完成，便于响应式管道按\n * “零或一”语义使用 {@code switchIfEmpty}、{@code defaultIfEmpty}、{@code flatMap} 等算子。\n *\n * @author Nikita Koksharov\n *\n */',
        ),
    ],
    "EmptyAsAbsent.java": [
        (
            '/**\n * Marks a Reactor {@code Mono}-returning method whose result, when the\n * underlying value is an empty {@link java.util.Map} or\n * {@link java.util.Collection}, should complete without emitting an\n * {@code onNext} signal.\n *\n * <p>Without this annotation, a method returning {@code Mono<Map<K, V>>}\n * whose underlying operation yields an empty map will emit that empty map\n * via {@code onNext} and then complete. With this annotation, the same\n * operation completes the {@code Mono} empty &mdash; allowing reactive\n * pipelines to use {@code switchIfEmpty}, {@code defaultIfEmpty},\n * {@code flatMap}, and similar operators as intended for one-or-zero\n * publishers.\n *\n * @author Nikita Koksharov\n *\n */',
            '/**\n * 标记返回 Reactor {@code Mono} 的方法：当底层结果为空的 {@link java.util.Map} 或\n * {@link java.util.Collection} 时，{@code Mono} 应以空完成结束，而不发出 {@code onNext} 信号。\n *\n * <p>未加此注解时，返回 {@code Mono<Map<K, V>>} 的方法在底层得到空 Map 时会通过 {@code onNext}\n * 发出该空 Map 再完成。加上此注解后，相同操作会以空 {@code Mono} 完成，便于响应式管道按\n * “零或一”语义使用 {@code switchIfEmpty}、{@code defaultIfEmpty}、{@code flatMap} 等算子。\n *\n * @author Nikita Koksharov\n *\n */',
        ),
    ],
    f"{_A}annotation/RCascade.java": [
        (
            '/**\n * Specifies that the defined cascade types are applied to the object/objects contained in Live Object field.\n * \n * @author Nikita Koksharov\n *\n */',
            '/**\n * 指定对 Live Object 字段中所含对象应用的级联（Cascade）类型。\n * \n * @author Nikita Koksharov\n *\n */',
        ),
        (
            '/**\n     * List of applied cascade types.\n     * \n     * @return value\n     */',
            '/**\n     * 要应用的级联类型列表。\n     * \n     * @return 级联类型数组\n     */',
        ),
    ],
    "RCascade.java": [
        (
            '/**\n * Specifies that the defined cascade types are applied to the object/objects contained in Live Object field.\n * \n * @author Nikita Koksharov\n *\n */',
            '/**\n * 指定对 Live Object 字段中所含对象应用的级联（Cascade）类型。\n * \n * @author Nikita Koksharov\n *\n */',
        ),
        (
            '/**\n     * List of applied cascade types.\n     * \n     * @return value\n     */',
            '/**\n     * 要应用的级联类型列表。\n     * \n     * @return 级联类型数组\n     */',
        ),
    ],
    f"{_A}annotation/REntity.java": [
        (
            '/**\n * Specifies that the class is a Live Object. \n *\n * @author Rui Gu (https://github.com/jackygurui)\n */',
            '/**\n * 标记该类为 Live Object（存于 Redis 的实时对象实体）。\n *\n * @author Rui Gu (https://github.com/jackygurui)\n */',
        ),
        (
            '/**\n     * (Optional) Live Object naming scheme. Defines how to assign key names for each instance of this class. \n     * Used to create a reference to an existing Live Object and materialising a new one in redis. \n     * Defaults to {@link DefaultNamingScheme} implementation.\n     * \n     * @return value\n     */',
            '/**\n     * （可选）Live Object 命名方案；定义该类每个实例在 Redis 中的键名规则。\n     * 用于引用已有 Live Object 或在 Redis 中物化新实例。默认为 {@link DefaultNamingScheme}。\n     * \n     * @return 命名方案实现类\n     */',
        ),
        (
            '/**\n     * (Optional) Live Object state codec. \n     * <code>null</code> means to use codec specified in Redisson configuration\n     * \n     * @return value\n     */',
            '/**\n     * （可选）Live Object 状态编解码器。\n     * 为 {@code null} 时使用 Redisson 配置中的默认 {@link Codec}。\n     * \n     * @return 编解码器实现类\n     */',
        ),
        (
            '/**\n     * (Optional) Live Object field transformation. \n     * Defaults to {@link TransformationMode#ANNOTATION_BASED}\n     * \n     * @return value\n     */',
            '/**\n     * （可选）Live Object 字段映射/transform 模式。\n     * 默认为 {@link TransformationMode#ANNOTATION_BASED}。\n     * \n     * @return 字段转换模式\n     */',
        ),
    ],
    "REntity.java": [
        (
            '/**\n * Specifies that the class is a Live Object. \n *\n * @author Rui Gu (https://github.com/jackygurui)\n */',
            '/**\n * 标记该类为 Live Object（存于 Redis 的实时对象实体）。\n *\n * @author Rui Gu (https://github.com/jackygurui)\n */',
        ),
        (
            '/**\n     * (Optional) Live Object naming scheme. Defines how to assign key names for each instance of this class. \n     * Used to create a reference to an existing Live Object and materialising a new one in redis. \n     * Defaults to {@link DefaultNamingScheme} implementation.\n     * \n     * @return value\n     */',
            '/**\n     * （可选）Live Object 命名方案；定义该类每个实例在 Redis 中的键名规则。\n     * 用于引用已有 Live Object 或在 Redis 中物化新实例。默认为 {@link DefaultNamingScheme}。\n     * \n     * @return 命名方案实现类\n     */',
        ),
        (
            '/**\n     * (Optional) Live Object state codec. \n     * <code>null</code> means to use codec specified in Redisson configuration\n     * \n     * @return value\n     */',
            '/**\n     * （可选）Live Object 状态编解码器。\n     * 为 {@code null} 时使用 Redisson 配置中的默认 {@link Codec}。\n     * \n     * @return 编解码器实现类\n     */',
        ),
        (
            '/**\n     * (Optional) Live Object field transformation. \n     * Defaults to {@link TransformationMode#ANNOTATION_BASED}\n     * \n     * @return value\n     */',
            '/**\n     * （可选）Live Object 字段映射/transform 模式。\n     * 默认为 {@link TransformationMode#ANNOTATION_BASED}。\n     * \n     * @return 字段转换模式\n     */',
        ),
    ],
    f"{_A}annotation/RFieldAccessor.java": [
        (
            '/**\n * Specifies that the method is a field accessor for Live Object.\n * Example:\n * <pre>\n *       &#064;RFieldAccessor\n *       public void set(String field, T value) {\n *       }\n *       \n *       &#064;RFieldAccessor\n *       public Object get(String field) {\n *           return null;\n *       }\n * </pre>\n * \n * @deprecated This annotation has been split into the more explicit\n *             {@link RGetter} and {@link RSetter} annotations, which also\n *             remove the requirement for the accessor methods to be named\n *             {@code get} and {@code set}. Use {@link RGetter} to mark a field\n *             getter and {@link RSetter} to mark a field setter instead.\n *\n * @author Rui Gu (https://github.com/jackygurui)\n */',
            '/**\n * 标记该方法为 Live Object 的通用字段访问器（读/写）。\n * Example:\n * <pre>\n *       &#064;RFieldAccessor\n *       public void set(String field, T value) {\n *       }\n *       \n *       &#064;RFieldAccessor\n *       public Object get(String field) {\n *           return null;\n *       }\n * </pre>\n * \n * @deprecated 已拆分为更明确的 {@link RGetter} 与 {@link RSetter}，且不再要求方法必须命名为\n *             {@code get}/{@code set}。请改用 {@link RGetter} 标记字段读取、{@link RSetter} 标记字段写入。\n *\n * @author Rui Gu (https://github.com/jackygurui)\n */',
        ),
    ],
    "RFieldAccessor.java": [
        (
            '/**\n * Specifies that the method is a field accessor for Live Object.\n * Example:\n * <pre>\n *       &#064;RFieldAccessor\n *       public void set(String field, T value) {\n *       }\n *       \n *       &#064;RFieldAccessor\n *       public Object get(String field) {\n *           return null;\n *       }\n * </pre>\n * \n * @deprecated This annotation has been split into the more explicit\n *             {@link RGetter} and {@link RSetter} annotations, which also\n *             remove the requirement for the accessor methods to be named\n *             {@code get} and {@code set}. Use {@link RGetter} to mark a field\n *             getter and {@link RSetter} to mark a field setter instead.\n *\n * @author Rui Gu (https://github.com/jackygurui)\n */',
            '/**\n * 标记该方法为 Live Object 的通用字段访问器（读/写）。\n * Example:\n * <pre>\n *       &#064;RFieldAccessor\n *       public void set(String field, T value) {\n *       }\n *       \n *       &#064;RFieldAccessor\n *       public Object get(String field) {\n *           return null;\n *       }\n * </pre>\n * \n * @deprecated 已拆分为更明确的 {@link RGetter} 与 {@link RSetter}，且不再要求方法必须命名为\n *             {@code get}/{@code set}。请改用 {@link RGetter} 标记字段读取、{@link RSetter} 标记字段写入。\n *\n * @author Rui Gu (https://github.com/jackygurui)\n */',
        ),
    ],
    f"{_A}annotation/RGetter.java": [
        (
            '/**\n * Specifies that the method is a generic field getter for a Live Object.\n * <p>\n * The annotated method takes a single {@link String} argument - the name of the\n * field to read - and returns its value. Unlike the deprecated\n * {@link RFieldAccessor} annotation, the method may have any name.\n * Example:\n * <pre>\n *       &#064;RGetter\n *       public &lt;T&gt; T get(String field) {\n *           return null;\n *       }\n * </pre>\n *\n * @see RSetter\n *\n * @author Nikita Koksharov\n */',
            '/**\n * 标记该方法为 Live Object 的通用字段读取器。\n * <p>\n * 被注解方法接受一个 {@link String} 参数（字段名）并返回其值。与已废弃的 {@link RFieldAccessor}\n * 不同，方法名不必为 {@code get}。\n * Example:\n * <pre>\n *       &#064;RGetter\n *       public &lt;T&gt; T get(String field) {\n *           return null;\n *       }\n * </pre>\n *\n * @see RSetter\n *\n * @author Nikita Koksharov\n */',
        ),
    ],
    "RGetter.java": [
        (
            '/**\n * Specifies that the method is a generic field getter for a Live Object.\n * <p>\n * The annotated method takes a single {@link String} argument - the name of the\n * field to read - and returns its value. Unlike the deprecated\n * {@link RFieldAccessor} annotation, the method may have any name.\n * Example:\n * <pre>\n *       &#064;RGetter\n *       public &lt;T&gt; T get(String field) {\n *           return null;\n *       }\n * </pre>\n *\n * @see RSetter\n *\n * @author Nikita Koksharov\n */',
            '/**\n * 标记该方法为 Live Object 的通用字段读取器。\n * <p>\n * 被注解方法接受一个 {@link String} 参数（字段名）并返回其值。与已废弃的 {@link RFieldAccessor}\n * 不同，方法名不必为 {@code get}。\n * Example:\n * <pre>\n *       &#064;RGetter\n *       public &lt;T&gt; T get(String field) {\n *           return null;\n *       }\n * </pre>\n *\n * @see RSetter\n *\n * @author Nikita Koksharov\n */',
        ),
    ],
    f"{_A}annotation/RId.java": [
        (
            '/**\n * Specifies that the field is a Live Object\'s id field.\n * Only single field could be specified per class. \n *\n * @author Rui Gu (https://github.com/jackygurui)\n */',
            '/**\n * 标记该字段为 Live Object 的主键（ID）字段；每个类只能指定一个。\n *\n * @author Rui Gu (https://github.com/jackygurui)\n */',
        ),
        (
            '/**\n     * (Optional) Live Object id generator. By default id is required to be fill during object creation.\n     * \n     * @see UUIDGenerator\n     * @see LongGenerator\n     */',
            '/**\n     * （可选）Live Object ID 生成器；默认创建对象时必须手动填入 ID。\n     * \n     * @see UUIDGenerator\n     * @see LongGenerator\n     */',
        ),
    ],
    "RId.java": [
        (
            '/**\n * Specifies that the field is a Live Object\'s id field.\n * Only single field could be specified per class. \n *\n * @author Rui Gu (https://github.com/jackygurui)\n */',
            '/**\n * 标记该字段为 Live Object 的主键（ID）字段；每个类只能指定一个。\n *\n * @author Rui Gu (https://github.com/jackygurui)\n */',
        ),
        (
            '/**\n     * (Optional) Live Object id generator. By default id is required to be fill during object creation.\n     * \n     * @see UUIDGenerator\n     * @see LongGenerator\n     */',
            '/**\n     * （可选）Live Object ID 生成器；默认创建对象时必须手动填入 ID。\n     * \n     * @see UUIDGenerator\n     * @see LongGenerator\n     */',
        ),
    ],
    f"{_A}annotation/RIndex.java": [
        (
            '/**\n * Specifies that the field is used in search index.\n * \n * @see Conditions\n * @see RLiveObjectService#find\n *\n * @author Nikita Koksharov\n */',
            '/**\n * 标记该字段参与 Live Object 搜索索引，可用于条件查询。\n * \n * @see Conditions\n * @see RLiveObjectService#find\n *\n * @author Nikita Koksharov\n */',
        ),
    ],
    "RIndex.java": [
        (
            '/**\n * Specifies that the field is used in search index.\n * \n * @see Conditions\n * @see RLiveObjectService#find\n *\n * @author Nikita Koksharov\n */',
            '/**\n * 标记该字段参与 Live Object 搜索索引，可用于条件查询。\n * \n * @see Conditions\n * @see RLiveObjectService#find\n *\n * @author Nikita Koksharov\n */',
        ),
    ],
    f"{_A}annotation/RInject.java": [
        (
            '/**\n * Specifies that the field value is filled up with RedissonClient instance. \n * \n * @author Nikita Koksharov\n *\n */',
            '/**\n * 标记该字段在 Live Object 实例化时自动注入 {@code RedissonClient} 引用。\n * \n * @author Nikita Koksharov\n *\n */',
        ),
    ],
    "RInject.java": [
        (
            '/**\n * Specifies that the field value is filled up with RedissonClient instance. \n * \n * @author Nikita Koksharov\n *\n */',
            '/**\n * 标记该字段在 Live Object 实例化时自动注入 {@code RedissonClient} 引用。\n * \n * @author Nikita Koksharov\n *\n */',
        ),
    ],
    f"{_A}annotation/RObjectField.java": [
        (
            '/**\n * By default <code>namingScheme</code> and/or <code>codec</code> parameters specified in {@link REntity}\n * are applied for each Live Object field. \n * \n * This annotation allows to specify custom <code>namingScheme</code> and/or <code>codec</code> parameters \n * for any Live Object field except that marked with {@link RId}.\n *\n * @author Rui Gu (https://github.com/jackygurui)\n */',
            '/**\n * 默认情况下 {@link REntity} 上的 {@code namingScheme} 与 {@code codec} 会应用到每个 Live Object 字段。\n * \n * 本注解允许为除 {@link RId} 主键外的任意字段单独指定 {@code namingScheme} 和/或 {@code codec}。\n *\n * @author Rui Gu (https://github.com/jackygurui)\n */',
        ),
        (
            '/**\n     * (Optional) Live Object naming scheme. Defines how to assign key names for each instance of this class. \n     * Used to create a reference to an existing Live Object and materialising a new one in redis. \n     * Defaults to {@link DefaultNamingScheme} implementation.\n     * \n     * @return scheme\n     */',
            '/**\n     * （可选）字段级 Live Object 命名方案；定义嵌套对象在 Redis 中的键名规则。\n     * 用于引用已有对象或物化新实例。默认为 {@link DefaultNamingScheme}。\n     * \n     * @return 命名方案实现类\n     */',
        ),
        (
            '/**\n     * (Optional) Live Object state codec.\n     * <code>null</code> means to use codec specified in Redisson configuration\n     * \n     * @return codec\n     */',
            '/**\n     * （可选）字段级 Live Object 状态编解码器。\n     * 为 {@code null} 时使用 Redisson 配置中的默认 {@link Codec}。\n     * \n     * @return 编解码器实现类\n     */',
        ),
    ],
    "RObjectField.java": [
        (
            '/**\n * By default <code>namingScheme</code> and/or <code>codec</code> parameters specified in {@link REntity}\n * are applied for each Live Object field. \n * \n * This annotation allows to specify custom <code>namingScheme</code> and/or <code>codec</code> parameters \n * for any Live Object field except that marked with {@link RId}.\n *\n * @author Rui Gu (https://github.com/jackygurui)\n */',
            '/**\n * 默认情况下 {@link REntity} 上的 {@code namingScheme} 与 {@code codec} 会应用到每个 Live Object 字段。\n * \n * 本注解允许为除 {@link RId} 主键外的任意字段单独指定 {@code namingScheme} 和/或 {@code codec}。\n *\n * @author Rui Gu (https://github.com/jackygurui)\n */',
        ),
        (
            '/**\n     * (Optional) Live Object naming scheme. Defines how to assign key names for each instance of this class. \n     * Used to create a reference to an existing Live Object and materialising a new one in redis. \n     * Defaults to {@link DefaultNamingScheme} implementation.\n     * \n     * @return scheme\n     */',
            '/**\n     * （可选）字段级 Live Object 命名方案；定义嵌套对象在 Redis 中的键名规则。\n     * 用于引用已有对象或物化新实例。默认为 {@link DefaultNamingScheme}。\n     * \n     * @return 命名方案实现类\n     */',
        ),
        (
            '/**\n     * (Optional) Live Object state codec.\n     * <code>null</code> means to use codec specified in Redisson configuration\n     * \n     * @return codec\n     */',
            '/**\n     * （可选）字段级 Live Object 状态编解码器。\n     * 为 {@code null} 时使用 Redisson 配置中的默认 {@link Codec}。\n     * \n     * @return 编解码器实现类\n     */',
        ),
    ],
    f"{_A}annotation/RRemoteAsync.java": [
        (
            '/**\n * Annotation used to mark interface as asynchronous \n * client interface for remote service interface. \n * <p>\n * All method signatures must match with remote service interface,\n * but return type must be <code>org.redisson.api.RFuture</code>.\n * <p>\n * It\'s not necessary to add all methods from remote service.\n * Add only those which are needed. \n * \n * @see org.redisson.api.RFuture\n * \n * @author Nikita Koksharov\n *\n */',
            '/**\n * 标记接口为远程服务的异步客户端代理；方法签名须与远程服务接口一致，\n * 但返回类型必须为 {@code org.redisson.api.RFuture}。\n * <p>\n * 不必声明远程服务的全部方法，仅添加需要调用的即可。\n * \n * @see org.redisson.api.RFuture\n * \n * @author Nikita Koksharov\n *\n */',
        ),
        (
            '/**\n     * Remote interface class used to register\n     * \n     * @return class used to register\n     */',
            '/**\n     * 注册远程服务时对应的远程接口类。\n     * \n     * @return 远程服务接口 Class\n     */',
        ),
    ],
    "RRemoteAsync.java": [
        (
            '/**\n * Annotation used to mark interface as asynchronous \n * client interface for remote service interface. \n * <p>\n * All method signatures must match with remote service interface,\n * but return type must be <code>org.redisson.api.RFuture</code>.\n * <p>\n * It\'s not necessary to add all methods from remote service.\n * Add only those which are needed. \n * \n * @see org.redisson.api.RFuture\n * \n * @author Nikita Koksharov\n *\n */',
            '/**\n * 标记接口为远程服务的异步客户端代理；方法签名须与远程服务接口一致，\n * 但返回类型必须为 {@code org.redisson.api.RFuture}。\n * <p>\n * 不必声明远程服务的全部方法，仅添加需要调用的即可。\n * \n * @see org.redisson.api.RFuture\n * \n * @author Nikita Koksharov\n *\n */',
        ),
        (
            '/**\n     * Remote interface class used to register\n     * \n     * @return class used to register\n     */',
            '/**\n     * 注册远程服务时对应的远程接口类。\n     * \n     * @return 远程服务接口 Class\n     */',
        ),
    ],
    f"{_A}annotation/RRemoteReactive.java": [
        (
            '/**\n * Annotation used to mark interface as Reactive \n * client interface for remote service interface. \n * <p>\n * All method signatures must match with remote service interface,\n * but return type must be <code>reactor.core.publisher.Mono</code>.\n * <p>\n * It\'s not necessary to add all methods from remote service.\n * Add only those which are needed. \n * \n * @see reactor.core.publisher.Mono\n * \n * @author Nikita Koksharov\n *\n */',
            '/**\n * 标记接口为远程服务的 Reactor 响应式客户端代理；方法签名须与远程服务接口一致，\n * 但返回类型必须为 {@code reactor.core.publisher.Mono}。\n * <p>\n * 不必声明远程服务的全部方法，仅添加需要调用的即可。\n * \n * @see reactor.core.publisher.Mono\n * \n * @author Nikita Koksharov\n *\n */',
        ),
        (
            '/**\n     * Remote interface class used to register\n     * \n     * @return class used to register\n     */',
            '/**\n     * 注册远程服务时对应的远程接口类。\n     * \n     * @return 远程服务接口 Class\n     */',
        ),
    ],
    "RRemoteReactive.java": [
        (
            '/**\n * Annotation used to mark interface as Reactive \n * client interface for remote service interface. \n * <p>\n * All method signatures must match with remote service interface,\n * but return type must be <code>reactor.core.publisher.Mono</code>.\n * <p>\n * It\'s not necessary to add all methods from remote service.\n * Add only those which are needed. \n * \n * @see reactor.core.publisher.Mono\n * \n * @author Nikita Koksharov\n *\n */',
            '/**\n * 标记接口为远程服务的 Reactor 响应式客户端代理；方法签名须与远程服务接口一致，\n * 但返回类型必须为 {@code reactor.core.publisher.Mono}。\n * <p>\n * 不必声明远程服务的全部方法，仅添加需要调用的即可。\n * \n * @see reactor.core.publisher.Mono\n * \n * @author Nikita Koksharov\n *\n */',
        ),
        (
            '/**\n     * Remote interface class used to register\n     * \n     * @return class used to register\n     */',
            '/**\n     * 注册远程服务时对应的远程接口类。\n     * \n     * @return 远程服务接口 Class\n     */',
        ),
    ],
    f"{_A}annotation/RRemoteRx.java": [
        (
            '/**\n * Annotation used to mark interface as RxJava2 \n * client interface for remote service interface. \n * <p>\n * All method signatures must match with remote service interface,\n * but return type must be one of the following:\n *   <ul>\n *      <li>io.reactivex.rxjava3.core.Completable</li>\n *      <li>io.reactivex.rxjava3.core.Single</li>\n *      <li>io.reactivex.rxjava3.core.Maybe</li>\n *   </ul>\n * <p>\n * It\'s not necessary to add all methods from remote service.\n * Add only those which are needed. \n * \n * @see io.reactivex.rxjava3.core.Completable\n * @see io.reactivex.rxjava3.core.Single\n * @see io.reactivex.rxjava3.core.Maybe\n * \n * @author Nikita Koksharov\n *\n */',
            '/**\n * 标记接口为远程服务的 RxJava 客户端代理；方法签名须与远程服务接口一致，\n * 但返回类型必须为以下之一：\n *   <ul>\n *      <li>io.reactivex.rxjava3.core.Completable</li>\n *      <li>io.reactivex.rxjava3.core.Single</li>\n *      <li>io.reactivex.rxjava3.core.Maybe</li>\n *   </ul>\n * <p>\n * 不必声明远程服务的全部方法，仅添加需要调用的即可。\n * \n * @see io.reactivex.rxjava3.core.Completable\n * @see io.reactivex.rxjava3.core.Single\n * @see io.reactivex.rxjava3.core.Maybe\n * \n * @author Nikita Koksharov\n *\n */',
        ),
        (
            '/**\n     * Remote interface class used to register\n     * \n     * @return class used to register\n     */',
            '/**\n     * 注册远程服务时对应的远程接口类。\n     * \n     * @return 远程服务接口 Class\n     */',
        ),
    ],
    "RRemoteRx.java": [
        (
            '/**\n * Annotation used to mark interface as RxJava2 \n * client interface for remote service interface. \n * <p>\n * All method signatures must match with remote service interface,\n * but return type must be one of the following:\n *   <ul>\n *      <li>io.reactivex.rxjava3.core.Completable</li>\n *      <li>io.reactivex.rxjava3.core.Single</li>\n *      <li>io.reactivex.rxjava3.core.Maybe</li>\n *   </ul>\n * <p>\n * It\'s not necessary to add all methods from remote service.\n * Add only those which are needed. \n * \n * @see io.reactivex.rxjava3.core.Completable\n * @see io.reactivex.rxjava3.core.Single\n * @see io.reactivex.rxjava3.core.Maybe\n * \n * @author Nikita Koksharov\n *\n */',
            '/**\n * 标记接口为远程服务的 RxJava 客户端代理；方法签名须与远程服务接口一致，\n * 但返回类型必须为以下之一：\n *   <ul>\n *      <li>io.reactivex.rxjava3.core.Completable</li>\n *      <li>io.reactivex.rxjava3.core.Single</li>\n *      <li>io.reactivex.rxjava3.core.Maybe</li>\n *   </ul>\n * <p>\n * 不必声明远程服务的全部方法，仅添加需要调用的即可。\n * \n * @see io.reactivex.rxjava3.core.Completable\n * @see io.reactivex.rxjava3.core.Single\n * @see io.reactivex.rxjava3.core.Maybe\n * \n * @author Nikita Koksharov\n *\n */',
        ),
        (
            '/**\n     * Remote interface class used to register\n     * \n     * @return class used to register\n     */',
            '/**\n     * 注册远程服务时对应的远程接口类。\n     * \n     * @return 远程服务接口 Class\n     */',
        ),
    ],
    f"{_A}annotation/RSetter.java": [
        (
            '/**\n * Specifies that the method is a generic field setter for a Live Object.\n * <p>\n * The annotated method takes two arguments - the name of the field to write as a\n * {@link String} and the new value - and stores the value into that field. Unlike\n * the deprecated {@link RFieldAccessor} annotation, the method may have any name.\n * Example:\n * <pre>\n *       &#064;RSetter\n *       public &lt;T&gt; void set(String field, T value) {\n *       }\n * </pre>\n *\n * @see RGetter\n *\n * @author Nikita Koksharov\n */',
            '/**\n * 标记该方法为 Live Object 的通用字段写入器。\n * <p>\n * 被注解方法接受两个参数：字段名（{@link String}）与新值，并将值写入对应字段。与已废弃的\n * {@link RFieldAccessor} 不同，方法名不必为 {@code set}。\n * Example:\n * <pre>\n *       &#064;RSetter\n *       public &lt;T&gt; void set(String field, T value) {\n *       }\n * </pre>\n *\n * @see RGetter\n *\n * @author Nikita Koksharov\n */',
        ),
    ],
    "RSetter.java": [
        (
            '/**\n * Specifies that the method is a generic field setter for a Live Object.\n * <p>\n * The annotated method takes two arguments - the name of the field to write as a\n * {@link String} and the new value - and stores the value into that field. Unlike\n * the deprecated {@link RFieldAccessor} annotation, the method may have any name.\n * Example:\n * <pre>\n *       &#064;RSetter\n *       public &lt;T&gt; void set(String field, T value) {\n *       }\n * </pre>\n *\n * @see RGetter\n *\n * @author Nikita Koksharov\n */',
            '/**\n * 标记该方法为 Live Object 的通用字段写入器。\n * <p>\n * 被注解方法接受两个参数：字段名（{@link String}）与新值，并将值写入对应字段。与已废弃的\n * {@link RFieldAccessor} 不同，方法名不必为 {@code set}。\n * Example:\n * <pre>\n *       &#064;RSetter\n *       public &lt;T&gt; void set(String field, T value) {\n *       }\n * </pre>\n *\n * @see RGetter\n *\n * @author Nikita Koksharov\n */',
        ),
    ],
    f"{_A}array/ArrayEntry.java": [
        (
            '/**\n * Array entry object.\n *\n * @param <V> value type\n *\n * @author lamnt2008\n *\n */',
            '/**\n * Redis 数组（{@code RArray}）中的索引-值条目。\n *\n * @param <V> 元素值类型\n *\n * @author lamnt2008\n *\n */',
        ),
        (
            '/**\n     * Returns array index.\n     *\n     * @return array index\n     */',
            '/**\n     * 返回数组下标。\n     *\n     * @return 数组下标\n     */',
        ),
        (
            '/**\n     * Returns value stored at array index.\n     *\n     * @return value stored at array index\n     */',
            '/**\n     * 返回该下标处存储的元素值。\n     *\n     * @return 元素值\n     */',
        ),
    ],
    "ArrayEntry.java": [
        (
            '/**\n * Array entry object.\n *\n * @param <V> value type\n *\n * @author lamnt2008\n *\n */',
            '/**\n * Redis 数组（{@code RArray}）中的索引-值条目。\n *\n * @param <V> 元素值类型\n *\n * @author lamnt2008\n *\n */',
        ),
        (
            '/**\n     * Returns array index.\n     *\n     * @return array index\n     */',
            '/**\n     * 返回数组下标。\n     *\n     * @return 数组下标\n     */',
        ),
        (
            '/**\n     * Returns value stored at array index.\n     *\n     * @return value stored at array index\n     */',
            '/**\n     * 返回该下标处存储的元素值。\n     *\n     * @return 元素值\n     */',
        ),
    ],
    f"{_A}array/ArrayFullInfo.java": [
        (
            '/**\n * Full array information object.\n * <p>\n * Extends {@link ArrayInfo} with the extended statistics returned only when\n * full information is requested through {@code RArray.getFullInfo()}.\n *\n * @author Nikita Koksharov\n *\n */',
            '/**\n * Redis 数组完整元信息对象。\n * <p>\n * 继承 {@link ArrayInfo}，在通过 {@code RArray.getFullInfo()} 请求完整信息时返回额外的切片统计字段。\n *\n * @author Nikita Koksharov\n *\n */',
        ),
        (
            '/**\n     * Returns number of dense slices.\n     *\n     * @return number of dense slices\n     */',
            '/**\n     * 返回稠密（dense）切片数量。\n     *\n     * @return 稠密切片数\n     */',
        ),
        (
            '/**\n     * Returns number of sparse slices.\n     *\n     * @return number of sparse slices\n     */',
            '/**\n     * 返回稀疏（sparse）切片数量。\n     *\n     * @return 稀疏切片数\n     */',
        ),
        (
            '/**\n     * Returns average dense slice size.\n     *\n     * @return average dense slice size\n     */',
            '/**\n     * 返回稠密切片的平均大小。\n     *\n     * @return 稠密切片平均大小\n     */',
        ),
        (
            '/**\n     * Returns average dense slice fill ratio.\n     *\n     * @return average dense slice fill ratio\n     */',
            '/**\n     * 返回稠密切片的平均填充率。\n     *\n     * @return 稠密切片平均填充率\n     */',
        ),
        (
            '/**\n     * Returns average sparse slice size.\n     *\n     * @return average sparse slice size\n     */',
            '/**\n     * 返回稀疏切片的平均大小。\n     *\n     * @return 稀疏切片平均大小\n     */',
        ),
    ],
    "ArrayFullInfo.java": [
        (
            '/**\n * Full array information object.\n * <p>\n * Extends {@link ArrayInfo} with the extended statistics returned only when\n * full information is requested through {@code RArray.getFullInfo()}.\n *\n * @author Nikita Koksharov\n *\n */',
            '/**\n * Redis 数组完整元信息对象。\n * <p>\n * 继承 {@link ArrayInfo}，在通过 {@code RArray.getFullInfo()} 请求完整信息时返回额外的切片统计字段。\n *\n * @author Nikita Koksharov\n *\n */',
        ),
        (
            '/**\n     * Returns number of dense slices.\n     *\n     * @return number of dense slices\n     */',
            '/**\n     * 返回稠密（dense）切片数量。\n     *\n     * @return 稠密切片数\n     */',
        ),
        (
            '/**\n     * Returns number of sparse slices.\n     *\n     * @return number of sparse slices\n     */',
            '/**\n     * 返回稀疏（sparse）切片数量。\n     *\n     * @return 稀疏切片数\n     */',
        ),
        (
            '/**\n     * Returns average dense slice size.\n     *\n     * @return average dense slice size\n     */',
            '/**\n     * 返回稠密切片的平均大小。\n     *\n     * @return 稠密切片平均大小\n     */',
        ),
        (
            '/**\n     * Returns average dense slice fill ratio.\n     *\n     * @return average dense slice fill ratio\n     */',
            '/**\n     * 返回稠密切片的平均填充率。\n     *\n     * @return 稠密切片平均填充率\n     */',
        ),
        (
            '/**\n     * Returns average sparse slice size.\n     *\n     * @return average sparse slice size\n     */',
            '/**\n     * 返回稀疏切片的平均大小。\n     *\n     * @return 稀疏切片平均大小\n     */',
        ),
    ],
}
