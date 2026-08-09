"""Chinese JavaDoc replacements for PemPrivateKeyParser and LambdaSafe."""

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "PemPrivateKeyParser.java": [
        (
            "/**\n * Parser for PKCS private key files in PEM format.\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @author Moritz Halbritter\n */",
            "/**\n * PEM 格式 PKCS 私钥文件的解析器。\n * 支持 PKCS#1 RSA、SEC1 EC、PKCS#8 及加密 PKCS#8 等多种 PEM 头尾格式，\n * 并将 DER 编码内容转换为 {@link PrivateKey}。\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @author Moritz Halbritter\n */",
        ),
        (
            "\t\t\tencoder.integer(0x00); // Version 0",
            "\t\t\tencoder.integer(0x00); // 版本 0",
        ),
        (
            "\t/**\n\t * Parse a private key from the specified string.\n\t * @param text the text to parse\n\t * @return the parsed private key\n\t */",
            "\t/**\n\t * 从指定字符串解析私钥。\n\t *\n\t * @param text the text to parse 待解析的 PEM 文本\n\t * @return the parsed private key 解析得到的私钥\n\t */",
        ),
        (
            "\t/**\n\t * Parse a private key from the specified string, using the provided password for\n\t * decryption if necessary.\n\t * @param text the text to parse\n\t * @param password the password used to decrypt an encrypted private key\n\t * @return the parsed private key\n\t */",
            "\t/**\n\t * 从指定字符串解析私钥，必要时使用给定密码解密。\n\t *\n\t * @param text the text to parse 待解析的 PEM 文本\n\t * @param password the password used to decrypt an encrypted private key 解密加密私钥所用的密码\n\t * @return the parsed private key 解析得到的私钥\n\t */",
        ),
        (
            "\t/**\n\t * Parser for a specific PEM format.\n\t */",
            "\t/**\n\t * 针对特定 PEM 格式的解析器。\n\t */",
        ),
        (
            "\t\t\t\tcatch (InvalidKeySpecException | NoSuchAlgorithmException ex) {\n\t\t\t\t\t// Ignore\n\t\t\t\t}",
            "\t\t\t\tcatch (InvalidKeySpecException | NoSuchAlgorithmException ex) {\n\t\t\t\t\t// 忽略\n\t\t\t\t}",
        ),
        (
            "\t/**\n\t * Simple ASN.1 DER encoder.\n\t */",
            "\t/**\n\t * 简单的 ASN.1 DER 编码器。\n\t */",
        ),
        (
            "\t/**\n\t * An ASN.1 DER encoded element.\n\t */",
            "\t/**\n\t * ASN.1 DER 编码元素。\n\t */",
        ),
        (
            "\t/**\n\t * Decryptor for PKCS8 encoded private keys.\n\t */",
            "\t/**\n\t * PKCS#8 编码私钥的解密器。\n\t */",
        ),
        (
            "\t/**\n\t * ANS.1 encoded object identifier.\n\t */",
            "\t/**\n\t * ASN.1 编码的对象标识符（OID）。\n\t */",
        ),
    ],
    "LambdaSafe.java": [
        (
            "/**\n * Utility that can be used to invoke lambdas in a safe way. Primarily designed to help\n * support generically typed callbacks where {@link ClassCastException class cast\n * exceptions} need to be dealt with due to class erasure.\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
            "/**\n * 以安全方式调用 lambda 的工具类。\n * 主要用于支持泛型回调：因类型擦除导致的 {@link ClassCastException 类型转换异常}\n * 会被识别并优雅处理，而非直接抛出。\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
        ),
        (
            "\t/**\n\t * Start a call to a single callback instance, dealing with common generic type\n\t * concerns and exceptions.\n\t * @param callbackType the callback type (a {@link FunctionalInterface functional\n\t * interface})\n\t * @param callbackInstance the callback instance (may be a lambda)\n\t * @param argument the primary argument passed to the callback\n\t * @param additionalArguments any additional arguments passed to the callback\n\t * @param <C> the callback type\n\t * @param <A> the primary argument type\n\t * @return a {@link Callback} instance that can be invoked.\n\t */",
            "\t/**\n\t * 发起对单个回调实例的调用，处理常见的泛型类型匹配与异常。\n\t *\n\t * @param callbackType the callback type (a {@link FunctionalInterface functional\n\t * interface}) 回调类型（{@link FunctionalInterface 函数式接口}）\n\t * @param callbackInstance the callback instance (may be a lambda) 回调实例（可为 lambda）\n\t * @param argument the primary argument passed to the callback 传给回调的主参数\n\t * @param additionalArguments any additional arguments passed to the callback 传给回调的附加参数\n\t * @param <C> the callback type 回调类型\n\t * @param <A> the primary argument type 主参数类型\n\t * @return a {@link Callback} instance that can be invoked 可调用的 {@link Callback} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Start a call to callback instances, dealing with common generic type concerns and\n\t * exceptions.\n\t * @param callbackType the callback type (a {@link FunctionalInterface functional\n\t * interface})\n\t * @param callbackInstances the callback instances (elements may be lambdas)\n\t * @param argument the primary argument passed to the callbacks\n\t * @param additionalArguments any additional arguments passed to the callbacks\n\t * @param <C> the callback type\n\t * @param <A> the primary argument type\n\t * @return a {@link Callbacks} instance that can be invoked.\n\t */",
            "\t/**\n\t * 发起对多个回调实例的调用，处理常见的泛型类型匹配与异常。\n\t *\n\t * @param callbackType the callback type (a {@link FunctionalInterface functional\n\t * interface}) 回调类型（{@link FunctionalInterface 函数式接口}）\n\t * @param callbackInstances the callback instances (elements may be lambdas) 回调实例集合（元素可为 lambda）\n\t * @param argument the primary argument passed to the callbacks 传给各回调的主参数\n\t * @param additionalArguments any additional arguments passed to the callbacks 传给各回调的附加参数\n\t * @param <C> the callback type 回调类型\n\t * @param <A> the primary argument type 主参数类型\n\t * @return a {@link Callbacks} instance that can be invoked 可调用的 {@link Callbacks} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Abstract base class for lambda safe callbacks.\n\t *\n\t * @param <C> the callback type\n\t * @param <A> the primary argument type\n\t * @param <SELF> the self class reference\n\t */",
            "\t/**\n\t * lambda 安全回调的抽象基类。\n\t *\n\t * @param <C> the callback type 回调类型\n\t * @param <A> the primary argument type 主参数类型\n\t * @param <SELF> the self class reference 自引用类型\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Use the specified logger source to report any lambda failures.\n\t\t * @param loggerSource the logger source to use\n\t\t * @return this instance\n\t\t */",
            "\t\t/**\n\t\t * 使用指定 logger 源报告 lambda 调用失败。\n\t\t *\n\t\t * @param loggerSource the logger source to use 用作 logger 源的类\n\t\t * @return this instance 当前实例\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Use the specified logger to report any lambda failures.\n\t\t * @param logger the logger to use\n\t\t * @return this instance\n\t\t */",
            "\t\t/**\n\t\t * 使用指定 logger 报告 lambda 调用失败。\n\t\t *\n\t\t * @param logger the logger to use 待使用的 logger\n\t\t * @return this instance 当前实例\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Use a specific filter to determine when a callback should apply. If no explicit\n\t\t * filter is set filter will be attempted using the generic type on the callback\n\t\t * type.\n\t\t * @param filter the filter to use\n\t\t * @return this instance\n\t\t * @since 3.4.8\n\t\t */",
            "\t\t/**\n\t\t * 使用指定过滤器决定回调何时适用。\n\t\t * 若未显式设置过滤器，将尝试根据回调类型的泛型参数进行匹配。\n\t\t *\n\t\t * @param filter the filter to use 待使用的过滤器\n\t\t * @return this instance 当前实例\n\t\t * @since 3.4.8\n\t\t */",
        ),
        (
            "\t\t\t// On Java 8, the message starts with the class name: \"java.lang.String cannot\n\t\t\t// be cast...\"",
            "\t\t\t// Java 8 上消息以类名开头：\"java.lang.String cannot be cast...\"",
        ),
        (
            "\t\t\t// On Java 11, the message starts with \"class ...\" a.k.a. Class.toString()",
            "\t\t\t// Java 11 上消息以 \"class ...\" 开头，即 Class.toString() 的形式",
        ),
        (
            "\t\t\t// On Java 9, the message used to contain the module name:\n\t\t\t// \"java.base/java.lang.String cannot be cast...\"",
            "\t\t\t// Java 9 上消息曾包含模块名：\n\t\t\t// \"java.base/java.lang.String cannot be cast...\"",
        ),
        (
            "\t/**\n\t * Represents a single callback that can be invoked in a lambda safe way.\n\t *\n\t * @param <C> the callback type\n\t * @param <A> the primary argument type\n\t */",
            "\t/**\n\t * 表示单个可以 lambda 安全方式调用的回调。\n\t *\n\t * @param <C> the callback type 回调类型\n\t * @param <A> the primary argument type 主参数类型\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Invoke the callback instance where the callback method returns void.\n\t\t * @param invoker the invoker used to invoke the callback\n\t\t */",
            "\t\t/**\n\t\t * 调用返回 void 的回调实例。\n\t\t *\n\t\t * @param invoker the invoker used to invoke the callback 用于调用回调的 invoker\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Invoke the callback instance where the callback method returns a result.\n\t\t * @param invoker the invoker used to invoke the callback\n\t\t * @param <R> the result type\n\t\t * @return the result of the invocation (may be {@link InvocationResult#noResult}\n\t\t * if the callback was not invoked)\n\t\t */",
            "\t\t/**\n\t\t * 调用返回结果的回调实例。\n\t\t *\n\t\t * @param invoker the invoker used to invoke the callback 用于调用回调的 invoker\n\t\t * @param <R> the result type 结果类型\n\t\t * @return the result of the invocation (may be {@link InvocationResult#noResult}\n\t\t * if the callback was not invoked) 调用结果（若回调未执行则可能为 {@link InvocationResult#noResult}）\n\t\t */",
        ),
        (
            "\t/**\n\t * Represents a collection of callbacks that can be invoked in a lambda safe way.\n\t *\n\t * @param <C> the callback type\n\t * @param <A> the primary argument type\n\t */",
            "\t/**\n\t * 表示一组可以 lambda 安全方式调用的回调。\n\t *\n\t * @param <C> the callback type 回调类型\n\t * @param <A> the primary argument type 主参数类型\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Invoke the callback instances where the callback method returns void.\n\t\t * @param invoker the invoker used to invoke the callback\n\t\t */",
            "\t\t/**\n\t\t * 调用返回 void 的各回调实例。\n\t\t *\n\t\t * @param invoker the invoker used to invoke the callback 用于调用回调的 invoker\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Invoke the callback instances where the callback method returns a result.\n\t\t * @param invoker the invoker used to invoke the callback\n\t\t * @param <R> the result type\n\t\t * @return the results of the invocation (may be an empty stream if no callbacks\n\t\t * could be called)\n\t\t */",
            "\t\t/**\n\t\t * 调用返回结果的各回调实例。\n\t\t *\n\t\t * @param invoker the invoker used to invoke the callback 用于调用回调的 invoker\n\t\t * @param <R> the result type 结果类型\n\t\t * @return the results of the invocation (may be an empty stream if no callbacks\n\t\t * could be called) 调用结果流（若无回调被调用则可能为空）\n\t\t */",
        ),
        (
            "\t/**\n\t * A filter that can be used to restrict when a callback is used.\n\t *\n\t * @param <C> the callback type\n\t * @param <A> the primary argument type\n\t * @since 3.4.8\n\t */",
            "\t/**\n\t * 用于限制回调何时被使用的过滤器。\n\t *\n\t * @param <C> the callback type 回调类型\n\t * @param <A> the primary argument type 主参数类型\n\t * @since 3.4.8\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Determine if the given callback matches and should be invoked.\n\t\t * @param callbackType the callback type (the functional interface)\n\t\t * @param callbackInstance the callback instance (the implementation)\n\t\t * @param argument the primary argument\n\t\t * @param additionalArguments any additional arguments\n\t\t * @return if the callback matches and should be invoked\n\t\t */",
            "\t\t/**\n\t\t * 判断给定回调是否匹配并应被调用。\n\t\t *\n\t\t * @param callbackType the callback type (the functional interface) 回调类型（函数式接口）\n\t\t * @param callbackInstance the callback instance (the implementation) 回调实例（实现）\n\t\t * @param argument the primary argument 主参数\n\t\t * @param additionalArguments any additional arguments 附加参数\n\t\t * @return if the callback matches and should be invoked 是否匹配且应调用\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Return a {@link Filter} that allows all callbacks to be invoked.\n\t\t * @param <C> the callback type\n\t\t * @param <A> the primary argument type\n\t\t * @return an \"allow all\" filter\n\t\t */",
            "\t\t/**\n\t\t * 返回允许所有回调被调用的 {@link Filter}。\n\t\t *\n\t\t * @param <C> the callback type 回调类型\n\t\t * @param <A> the primary argument type 主参数类型\n\t\t * @return an \"allow all\" filter \"允许全部\" 过滤器\n\t\t */",
        ),
        (
            "\t/**\n\t * {@link Filter} that matches when the callback has a single generic and primary\n\t * argument is an instance of it.\n\t */",
            "\t/**\n\t * 当回调具有单个泛型参数且主参数为该泛型实例时匹配的 {@link Filter}。\n\t */",
        ),
        (
            "\t/**\n\t * The result of a callback which may be a value, {@code null} or absent entirely if\n\t * the callback wasn't suitable. Similar in design to {@link Optional} but allows for\n\t * {@code null} as a valid value.\n\t *\n\t * @param <R> the result type\n\t */",
            "\t/**\n\t * 回调调用结果：可能是值、{@code null}，或在回调不适用时完全缺失。\n\t * 设计类似 {@link Optional}，但允许 {@code null} 作为有效值。\n\t *\n\t * @param <R> the result type 结果类型\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Return true if a result in present.\n\t\t * @return if a result is present\n\t\t */",
            "\t\t/**\n\t\t * 若存在结果则返回 {@code true}。\n\t\t *\n\t\t * @return if a result is present 是否存在结果\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Return the result of the invocation or {@code null} if the callback wasn't\n\t\t * suitable.\n\t\t * @return the result of the invocation or {@code null}\n\t\t */",
            "\t\t/**\n\t\t * 返回调用结果；若回调不适用则返回 {@code null}。\n\t\t *\n\t\t * @return the result of the invocation or {@code null} 调用结果或 {@code null}\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Return the result of the invocation or the given fallback if the callback\n\t\t * wasn't suitable.\n\t\t * @param fallback the fallback to use when there is no result\n\t\t * @return the result of the invocation or the fallback\n\t\t */",
            "\t\t/**\n\t\t * 返回调用结果；若回调不适用则返回给定回退值。\n\t\t *\n\t\t * @param fallback the fallback to use when there is no result 无结果时使用的回退值\n\t\t * @return the result of the invocation or the fallback 调用结果或回退值\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Create a new {@link InvocationResult} instance with the specified value.\n\t\t * @param value the value (may be {@code null})\n\t\t * @param <R> the result type\n\t\t * @return an {@link InvocationResult}\n\t\t */",
            "\t\t/**\n\t\t * 使用指定值创建新的 {@link InvocationResult} 实例。\n\t\t *\n\t\t * @param value the value (may be {@code null}) 值（可为 {@code null}）\n\t\t * @param <R> the result type 结果类型\n\t\t * @return an {@link InvocationResult} {@link InvocationResult} 实例\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Return an {@link InvocationResult} instance representing no result.\n\t\t * @param <R> the result type\n\t\t * @return an {@link InvocationResult}\n\t\t */",
            "\t\t/**\n\t\t * 返回表示无结果的 {@link InvocationResult} 实例。\n\t\t *\n\t\t * @param <R> the result type 结果类型\n\t\t * @return an {@link InvocationResult} {@link InvocationResult} 实例\n\t\t */",
        ),
    ],
}
