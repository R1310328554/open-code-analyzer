"""Chinese JavaDoc replacements for springframework wave14a validation errors [10:17]."""

VALIDATION_ERRORS_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "Errors.java": [
        (
            "/**\n * Stores and exposes information about data-binding and validation errors\n * for a specific object.\n *\n * <p>Field names are typically properties of the target object (for example, \"name\"\n * when binding to a customer object). Implementations may also support nested\n * fields in case of nested objects (for example, \"address.street\"), in conjunction\n * with subtree navigation via {@link #setNestedPath}: for example, an\n * {@code AddressValidator} may validate \"address\", not being aware that this\n * is a nested object of a top-level customer object.\n *\n * <p>Note: {@code Errors} objects are single-threaded.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see Validator\n * @see ValidationUtils\n * @see SimpleErrors\n * @see BindingResult\n */",
            "/**\n * 存储并暴露特定对象的数据绑定与校验错误信息。\n *\n * <p>字段名通常是目标对象的属性（例如绑定到 customer 对象时为 \"name\"）。\n * 实现也可支持嵌套对象的嵌套字段（例如 \"address.street\"），\n * 配合 {@link #setNestedPath} 进行子树导航：\n * 例如 {@code AddressValidator} 可校验 \"address\"，\n * 而无需知晓它是顶层 customer 对象的嵌套对象。\n *\n * <p>注意：{@code Errors} 对象非线程安全。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see Validator\n * @see ValidationUtils\n * @see SimpleErrors\n * @see BindingResult\n */",
        ),
        (
            "\t/**\n\t * The separator between path elements in a nested path,\n\t * for example in \"customer.name\" or \"customer.address.street\".\n\t * <p>\".\" = same as the\n\t * {@link org.springframework.beans.PropertyAccessor#NESTED_PROPERTY_SEPARATOR nested property separator}\n\t * in the beans package.\n\t */",
            "\t/**\n\t * 嵌套路径中路径元素之间的分隔符，\n\t * 例如 \"customer.name\" 或 \"customer.address.street\"。\n\t * <p>\".\" 与 beans 包中\n\t * {@link org.springframework.beans.PropertyAccessor#NESTED_PROPERTY_SEPARATOR 嵌套属性分隔符} 相同。\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of the bound root object.\n\t */",
            "\t/**\n\t * 返回被绑定根对象的名称。\n\t */",
        ),
        (
            "\t/**\n\t * Allow context to be changed so that standard validators can validate\n\t * subtrees. Reject calls prepend the given path to the field names.\n\t * <p>For example, an address validator could validate the subobject\n\t * \"address\" of a customer object.\n\t * <p>The default implementation throws {@code UnsupportedOperationException}\n\t * since not all {@code Errors} implementations support nested paths.\n\t * @param nestedPath nested path within this object,\n\t * for example, \"address\" (defaults to \"\", {@code null} is also acceptable).\n\t * Can end with a dot: both \"address\" and \"address.\" are valid.\n\t * @see #getNestedPath()\n\t */",
            "\t/**\n\t * 允许更改上下文，使标准校验器可校验子树。\n\t * reject 调用会将给定路径前缀到字段名。\n\t * <p>例如地址校验器可校验 customer 对象的 \"address\" 子对象。\n\t * <p>默认实现抛出 {@code UnsupportedOperationException}，\n\t * 因并非所有 {@code Errors} 实现都支持嵌套路径。\n\t * @param nestedPath 本对象内的嵌套路径，\n\t * 例如 \"address\"（默认为 \"\"，{@code null} 也可接受）。\n\t * 可以点结尾：\"address\" 与 \"address.\" 均有效。\n\t * @see #getNestedPath()\n\t */",
        ),
        (
            "\t/**\n\t * Return the current nested path of this {@link Errors} object.\n\t * <p>Returns a nested path with a dot, i.e. \"address.\", for easy\n\t * building of concatenated paths. Default is an empty String.\n\t * @see #setNestedPath(String)\n\t */",
            "\t/**\n\t * 返回本 {@link Errors} 对象的当前嵌套路径。\n\t * <p>返回带点号的嵌套路径，即 \"address.\"，便于拼接路径。默认为空字符串。\n\t * @see #setNestedPath(String)\n\t */",
        ),
        (
            "\t/**\n\t * Push the given sub path onto the nested path stack.\n\t * <p>A {@link #popNestedPath()} call will reset the original\n\t * nested path before the corresponding\n\t * {@code pushNestedPath(String)} call.\n\t * <p>Using the nested path stack allows to set temporary nested paths\n\t * for subobjects without having to worry about a temporary path holder.\n\t * <p>For example: current path \"spouse.\", pushNestedPath(\"child\") &rarr;\n\t * result path \"spouse.child.\"; popNestedPath() &rarr; \"spouse.\" again.\n\t * <p>The default implementation throws {@code UnsupportedOperationException}\n\t * since not all {@code Errors} implementations support nested paths.\n\t * @param subPath the sub path to push onto the nested path stack\n\t * @see #popNestedPath()\n\t */",
            "\t/**\n\t * 将给定子路径压入嵌套路径栈。\n\t * <p>调用 {@link #popNestedPath()} 会恢复对应 {@code pushNestedPath(String)} 调用前的原始嵌套路径。\n\t * <p>使用嵌套路径栈可为子对象设置临时嵌套路径，无需额外临时路径持有者。\n\t * <p>例如：当前路径 \"spouse.\"，pushNestedPath(\"child\") &rarr;\n\t * 结果路径 \"spouse.child.\"；popNestedPath() &rarr; 恢复为 \"spouse.\"。\n\t * <p>默认实现抛出 {@code UnsupportedOperationException}，\n\t * 因并非所有 {@code Errors} 实现都支持嵌套路径。\n\t * @param subPath 要压入嵌套路径栈的子路径\n\t * @see #popNestedPath()\n\t */",
        ),
        (
            "\t/**\n\t * Pop the former nested path from the nested path stack.\n\t * @throws IllegalStateException if there is no former nested path on the stack\n\t * @see #pushNestedPath(String)\n\t */",
            "\t/**\n\t * 从嵌套路径栈弹出先前的嵌套路径。\n\t * @throws IllegalStateException 若栈上无先前的嵌套路径\n\t * @see #pushNestedPath(String)\n\t */",
        ),
        (
            "\t/**\n\t * Register a global error for the entire target object,\n\t * using the given error description.\n\t * @param errorCode error code, interpretable as a message key\n\t * @see #reject(String, Object[], String)\n\t */",
            "\t/**\n\t * 使用给定错误描述为整个目标对象注册全局错误。\n\t * @param errorCode 错误码，可解释为消息键\n\t * @see #reject(String, Object[], String)\n\t */",
        ),
        (
            "\t/**\n\t * Register a global error for the entire target object,\n\t * using the given error description.\n\t * @param errorCode error code, interpretable as a message key\n\t * @param defaultMessage fallback default message\n\t * @see #reject(String, Object[], String)\n\t */",
            "\t/**\n\t * 使用给定错误描述为整个目标对象注册全局错误。\n\t * @param errorCode 错误码，可解释为消息键\n\t * @param defaultMessage 后备默认消息\n\t * @see #reject(String, Object[], String)\n\t */",
        ),
        (
            "\t/**\n\t * Register a global error for the entire target object,\n\t * using the given error description.\n\t * @param errorCode error code, interpretable as a message key\n\t * @param errorArgs error arguments, for argument binding via MessageFormat\n\t * (can be {@code null})\n\t * @param defaultMessage fallback default message\n\t * @see #rejectValue(String, String, Object[], String)\n\t */",
            "\t/**\n\t * 使用给定错误描述为整个目标对象注册全局错误。\n\t * @param errorCode 错误码，可解释为消息键\n\t * @param errorArgs 错误参数，用于 MessageFormat 参数绑定（可为 {@code null}）\n\t * @param defaultMessage 后备默认消息\n\t * @see #rejectValue(String, String, Object[], String)\n\t */",
        ),
        (
            "\t/**\n\t * Register a field error for the specified field of the current object\n\t * (respecting the current nested path, if any), using the given error\n\t * description.\n\t * <p>The field name may be {@code null} or empty String to indicate\n\t * the current object itself rather than a field of it. This may result\n\t * in a corresponding field error within the nested object graph or a\n\t * global error if the current object is the top object.\n\t * @param field the field name (may be {@code null} or empty String)\n\t * @param errorCode error code, interpretable as a message key\n\t * @see #rejectValue(String, String, Object[], String)\n\t */",
            "\t/**\n\t * 使用给定错误描述为当前对象（若有则尊重当前嵌套路径）的指定字段注册字段错误。\n\t * <p>字段名可为 {@code null} 或空字符串，表示当前对象本身而非其字段。\n\t * 这可能在嵌套对象图中产生对应字段错误，\n\t * 若当前对象为顶层对象则产生全局错误。\n\t * @param field 字段名（可为 {@code null} 或空字符串）\n\t * @param errorCode 错误码，可解释为消息键\n\t * @see #rejectValue(String, String, Object[], String)\n\t */",
        ),
        (
            "\t/**\n\t * Register a field error for the specified field of the current object\n\t * (respecting the current nested path, if any), using the given error\n\t * description.\n\t * <p>The field name may be {@code null} or empty String to indicate\n\t * the current object itself rather than a field of it. This may result\n\t * in a corresponding field error within the nested object graph or a\n\t * global error if the current object is the top object.\n\t * @param field the field name (may be {@code null} or empty String)\n\t * @param errorCode error code, interpretable as a message key\n\t * @param defaultMessage fallback default message\n\t * @see #rejectValue(String, String, Object[], String)\n\t */",
            "\t/**\n\t * 使用给定错误描述为当前对象（若有则尊重当前嵌套路径）的指定字段注册字段错误。\n\t * <p>字段名可为 {@code null} 或空字符串，表示当前对象本身而非其字段。\n\t * 这可能在嵌套对象图中产生对应字段错误，\n\t * 若当前对象为顶层对象则产生全局错误。\n\t * @param field 字段名（可为 {@code null} 或空字符串）\n\t * @param errorCode 错误码，可解释为消息键\n\t * @param defaultMessage 后备默认消息\n\t * @see #rejectValue(String, String, Object[], String)\n\t */",
        ),
        (
            "\t/**\n\t * Register a field error for the specified field of the current object\n\t * (respecting the current nested path, if any), using the given error\n\t * description.\n\t * <p>The field name may be {@code null} or empty String to indicate\n\t * the current object itself rather than a field of it. This may result\n\t * in a corresponding field error within the nested object graph or a\n\t * global error if the current object is the top object.\n\t * @param field the field name (may be {@code null} or empty String)\n\t * @param errorCode error code, interpretable as a message key\n\t * @param errorArgs error arguments, for argument binding via MessageFormat\n\t * (can be {@code null})\n\t * @param defaultMessage fallback default message\n\t * @see #reject(String, Object[], String)\n\t */",
            "\t/**\n\t * 使用给定错误描述为当前对象（若有则尊重当前嵌套路径）的指定字段注册字段错误。\n\t * <p>字段名可为 {@code null} 或空字符串，表示当前对象本身而非其字段。\n\t * 这可能在嵌套对象图中产生对应字段错误，\n\t * 若当前对象为顶层对象则产生全局错误。\n\t * @param field 字段名（可为 {@code null} 或空字符串）\n\t * @param errorCode 错误码，可解释为消息键\n\t * @param errorArgs 错误参数，用于 MessageFormat 参数绑定（可为 {@code null}）\n\t * @param defaultMessage 后备默认消息\n\t * @see #reject(String, Object[], String)\n\t */",
        ),
        (
            "\t/**\n\t * Add all errors from the given {@code Errors} instance to this\n\t * {@code Errors} instance.\n\t * <p>This is a convenience method to avoid repeated {@code reject(..)}\n\t * calls for merging an {@code Errors} instance into another\n\t * {@code Errors} instance.\n\t * <p>Note that the passed-in {@code Errors} instance is supposed\n\t * to refer to the same target object, or at least contain compatible errors\n\t * that apply to the target object of this {@code Errors} instance.\n\t * <p>The default implementation throws {@code UnsupportedOperationException}\n\t * since not all {@code Errors} implementations support {@code #addAllErrors}.\n\t * @param errors the {@code Errors} instance to merge in\n\t * @see #getAllErrors()\n\t */",
            "\t/**\n\t * 将给定 {@code Errors} 实例的所有错误添加到本 {@code Errors} 实例。\n\t * <p>便捷方法，避免为合并 Errors 实例而重复调用 {@code reject(..)}。\n\t * <p>注意：传入的 {@code Errors} 实例应指向同一目标对象，\n\t * 或至少包含适用于本 Errors 实例目标对象的兼容错误。\n\t * <p>默认实现抛出 {@code UnsupportedOperationException}，\n\t * 因并非所有 {@code Errors} 实现都支持 {@code #addAllErrors}。\n\t * @param errors 要合并的 {@code Errors} 实例\n\t * @see #getAllErrors()\n\t */",
        ),
        (
            "\t/**\n\t * Throw the mapped exception with a message summarizing the recorded errors.\n\t * @param messageToException a function mapping the message to the exception,\n\t * for example, {@code IllegalArgumentException::new} or {@code IllegalStateException::new}\n\t * @param <T> the exception type to be thrown\n\t * @since 6.1\n\t * @see #toString()\n\t */",
            "\t/**\n\t * 抛出映射异常，消息汇总已记录的错误。\n\t * @param messageToException 将消息映射为异常的函数，\n\t * 例如 {@code IllegalArgumentException::new} 或 {@code IllegalStateException::new}\n\t * @param <T> 要抛出的异常类型\n\t * @since 6.1\n\t * @see #toString()\n\t */",
        ),
        (
            "\t/**\n\t * Determine if there were any errors.\n\t * @see #hasGlobalErrors()\n\t * @see #hasFieldErrors()\n\t */",
            "\t/**\n\t * 判断是否存在任何错误。\n\t * @see #hasGlobalErrors()\n\t * @see #hasFieldErrors()\n\t */",
        ),
        (
            "\t/**\n\t * Determine the total number of errors.\n\t * @see #getGlobalErrorCount()\n\t * @see #getFieldErrorCount()\n\t */",
            "\t/**\n\t * 确定错误总数。\n\t * @see #getGlobalErrorCount()\n\t * @see #getFieldErrorCount()\n\t */",
        ),
        (
            "\t/**\n\t * Get all errors, both global and field ones.\n\t * @return a list of {@link ObjectError}/{@link FieldError} instances\n\t * @see #getGlobalErrors()\n\t * @see #getFieldErrors()\n\t */",
            "\t/**\n\t * 获取所有错误，包括全局错误与字段错误。\n\t * @return {@link ObjectError}/{@link FieldError} 实例列表\n\t * @see #getGlobalErrors()\n\t * @see #getFieldErrors()\n\t */",
        ),
        (
            "\t/**\n\t * Determine if there were any global errors.\n\t * @see #hasFieldErrors()\n\t */",
            "\t/**\n\t * 判断是否存在全局错误。\n\t * @see #hasFieldErrors()\n\t */",
        ),
        (
            "\t/**\n\t * Determine the number of global errors.\n\t * @see #getFieldErrorCount()\n\t */",
            "\t/**\n\t * 确定全局错误数量。\n\t * @see #getFieldErrorCount()\n\t */",
        ),
        (
            "\t/**\n\t * Get all global errors.\n\t * @return a list of {@link ObjectError} instances\n\t * @see #getFieldErrors()\n\t */",
            "\t/**\n\t * 获取所有全局错误。\n\t * @return {@link ObjectError} 实例列表\n\t * @see #getFieldErrors()\n\t */",
        ),
        (
            "\t/**\n\t * Get the <i>first</i> global error, if any.\n\t * @return the global error, or {@code null}\n\t * @see #getFieldError()\n\t */",
            "\t/**\n\t * 获取<i>第一个</i>全局错误（若有）。\n\t * @return 全局错误，或 {@code null}\n\t * @see #getFieldError()\n\t */",
        ),
        (
            "\t/**\n\t * Determine if there were any errors associated with a field.\n\t * @see #hasGlobalErrors()\n\t */",
            "\t/**\n\t * 判断是否存在与字段相关的错误。\n\t * @see #hasGlobalErrors()\n\t */",
        ),
        (
            "\t/**\n\t * Determine the number of errors associated with a field.\n\t * @see #getGlobalErrorCount()\n\t */",
            "\t/**\n\t * 确定与字段相关的错误数量。\n\t * @see #getGlobalErrorCount()\n\t */",
        ),
        (
            "\t/**\n\t * Get all errors associated with a field.\n\t * @return a List of {@link FieldError} instances\n\t * @see #getGlobalErrors()\n\t */",
            "\t/**\n\t * 获取与字段相关的所有错误。\n\t * @return {@link FieldError} 实例列表\n\t * @see #getGlobalErrors()\n\t */",
        ),
        (
            "\t/**\n\t * Get the <i>first</i> error associated with a field, if any.\n\t * @return the field-specific error, or {@code null}\n\t * @see #getGlobalError()\n\t */",
            "\t/**\n\t * 获取与字段相关的<i>第一个</i>错误（若有）。\n\t * @return 字段特定错误，或 {@code null}\n\t * @see #getGlobalError()\n\t */",
        ),
        (
            "\t/**\n\t * Determine if there were any errors associated with the given field.\n\t * @param field the field name\n\t * @see #hasFieldErrors()\n\t */",
            "\t/**\n\t * 判断给定字段是否存在相关错误。\n\t * @param field 字段名\n\t * @see #hasFieldErrors()\n\t */",
        ),
        (
            "\t/**\n\t * Determine the number of errors associated with the given field.\n\t * @param field the field name\n\t * @see #getFieldErrorCount()\n\t */",
            "\t/**\n\t * 确定给定字段相关错误的数量。\n\t * @param field 字段名\n\t * @see #getFieldErrorCount()\n\t */",
        ),
        (
            "\t/**\n\t * Get all errors associated with the given field.\n\t * <p>Implementations may support not only full field names like\n\t * \"address.street\" but also pattern matches like \"address.*\".\n\t * @param field the field name\n\t * @return a List of {@link FieldError} instances\n\t * @see #getFieldErrors()\n\t */",
            "\t/**\n\t * 获取与给定字段相关的所有错误。\n\t * <p>实现可支持完整字段名（如 \"address.street\"）\n\t * 以及模式匹配（如 \"address.*\"）。\n\t * @param field 字段名\n\t * @return {@link FieldError} 实例列表\n\t * @see #getFieldErrors()\n\t */",
        ),
        (
            "\t/**\n\t * Get the first error associated with the given field, if any.\n\t * @param field the field name\n\t * @return the field-specific error, or {@code null}\n\t * @see #getFieldError()\n\t */",
            "\t/**\n\t * 获取与给定字段相关的第一个错误（若有）。\n\t * @param field 字段名\n\t * @return 字段特定错误，或 {@code null}\n\t * @see #getFieldError()\n\t */",
        ),
        (
            "\t/**\n\t * Return the current value of the given field, either the current\n\t * bean property value or a rejected update from the last binding.\n\t * <p>Allows for convenient access to user-specified field values,\n\t * even if there were type mismatches.\n\t * @param field the field name\n\t * @return the current value of the given field\n\t * @see #getFieldType(String)\n\t */",
            "\t/**\n\t * 返回给定字段的当前值，可能是当前 bean 属性值或上次绑定中被拒绝的更新值。\n\t * <p>便于访问用户指定的字段值，即使存在类型不匹配。\n\t * @param field 字段名\n\t * @return 给定字段的当前值\n\t * @see #getFieldType(String)\n\t */",
        ),
        (
            "\t/**\n\t * Determine the type of the given field, as far as possible.\n\t * <p>Implementations should be able to determine the type even\n\t * when the field value is {@code null}, for example from some\n\t * associated descriptor.\n\t * @param field the field name\n\t * @return the type of the field, or {@code null} if not determinable\n\t * @see #getFieldValue(String)\n\t */",
            "\t/**\n\t * 尽可能确定给定字段的类型。\n\t * <p>实现应能在字段值为 {@code null} 时仍确定类型，\n\t * 例如通过关联描述符。\n\t * @param field 字段名\n\t * @return 字段类型，无法确定时返回 {@code null}\n\t * @see #getFieldValue(String)\n\t */",
        ),
        (
            "\t/**\n\t * Return a summary of the recorded errors,\n\t * for example, for inclusion in an exception message.\n\t * @see #failOnError(Function)\n\t */",
            "\t/**\n\t * 返回已记录错误的摘要，例如用于异常消息。\n\t * @see #failOnError(Function)\n\t */",
        ),
    ],
    "FieldError.java": [
        (
            "/**\n * Encapsulates a field error, that is, a reason for rejecting a specific\n * field value.\n *\n * <p>See the {@link DefaultMessageCodesResolver} javadoc for details on\n * how a message code list is built for a {@code FieldError}.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 10.03.2003\n * @see DefaultMessageCodesResolver\n */",
            "/**\n * 封装字段错误，即拒绝特定字段值的原因。\n *\n * <p>关于 {@code FieldError} 消息码列表的构建方式，\n * 详见 {@link DefaultMessageCodesResolver} 的 JavaDoc。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 10.03.2003\n * @see DefaultMessageCodesResolver\n */",
        ),
        (
            "\t/**\n\t * Create a new FieldError instance.\n\t * @param objectName the name of the affected object\n\t * @param field the affected field of the object\n\t * @param defaultMessage the default message to be used to resolve this message\n\t */",
            "\t/**\n\t * 创建新的 FieldError 实例。\n\t * @param objectName 受影响对象的名称\n\t * @param field 受影响对象的字段\n\t * @param defaultMessage 用于解析本消息的默认消息\n\t */",
        ),
        (
            "\t/**\n\t * Create a new FieldError instance.\n\t * @param objectName the name of the affected object\n\t * @param field the affected field of the object\n\t * @param rejectedValue the rejected field value\n\t * @param bindingFailure whether this error represents a binding failure\n\t * (like a type mismatch); else, it is a validation failure\n\t * @param codes the codes to be used to resolve this message\n\t * @param arguments the array of arguments to be used to resolve this message\n\t * @param defaultMessage the default message to be used to resolve this message\n\t */",
            "\t/**\n\t * 创建新的 FieldError 实例。\n\t * @param objectName 受影响对象的名称\n\t * @param field 受影响对象的字段\n\t * @param rejectedValue 被拒绝的字段值\n\t * @param bindingFailure 本错误是否表示绑定失败（如类型不匹配）；\n\t * 否则为校验失败\n\t * @param codes 用于解析本消息的 codes\n\t * @param arguments 用于解析本消息的参数数组\n\t * @param defaultMessage 用于解析本消息的默认消息\n\t */",
        ),
        (
            "\t/**\n\t * Return the affected field of the object.\n\t */",
            "\t/**\n\t * 返回受影响对象的字段。\n\t */",
        ),
        (
            "\t/**\n\t * Return the rejected field value.\n\t */",
            "\t/**\n\t * 返回被拒绝的字段值。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this error represents a binding failure\n\t * (like a type mismatch); otherwise it is a validation failure.\n\t */",
            "\t/**\n\t * 返回本错误是否表示绑定失败（如类型不匹配）；\n\t * 否则为校验失败。\n\t */",
        ),
    ],
    "MapBindingResult.java": [
        (
            "/**\n * Map-based implementation of the BindingResult interface,\n * supporting registration and evaluation of binding errors on\n * Map attributes.\n *\n * <p>Can be used as errors holder for custom binding onto a\n * Map, for example when invoking a Validator for a Map object.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see java.util.Map\n */",
            "/**\n * 基于 Map 的 BindingResult 接口实现，\n * 支持在 Map 属性上注册并评估绑定错误。\n *\n * <p>可作为自定义绑定到 Map 的错误持有者，\n * 例如对 Map 对象调用 Validator 时。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see java.util.Map\n */",
        ),
        (
            "\t/**\n\t * Create a new MapBindingResult instance.\n\t * @param target the target Map to bind onto\n\t * @param objectName the name of the target object\n\t */",
            "\t/**\n\t * 创建新的 MapBindingResult 实例。\n\t * @param target 要绑定到的目标 Map\n\t * @param objectName 目标对象名称\n\t */",
        ),
        (
            "\t/**\n\t * Return the target Map to bind onto.\n\t */",
            "\t/**\n\t * 返回要绑定到的目标 Map。\n\t */",
        ),
    ],
    "ObjectError.java": [
        (
            "/**\n * Encapsulates an object error, that is, a global reason for rejecting\n * an object.\n *\n * <p>See the {@link DefaultMessageCodesResolver} javadoc for details on\n * how a message code list is built for an {@code ObjectError}.\n *\n * @author Juergen Hoeller\n * @since 10.03.2003\n * @see FieldError\n * @see DefaultMessageCodesResolver\n */",
            "/**\n * 封装对象错误，即拒绝整个对象的全局原因。\n *\n * <p>关于 {@code ObjectError} 消息码列表的构建方式，\n * 详见 {@link DefaultMessageCodesResolver} 的 JavaDoc。\n *\n * @author Juergen Hoeller\n * @since 10.03.2003\n * @see FieldError\n * @see DefaultMessageCodesResolver\n */",
        ),
        (
            "\t/**\n\t * Create a new instance of the ObjectError class.\n\t * @param objectName the name of the affected object\n\t * @param defaultMessage the default message to be used to resolve this message\n\t */",
            "\t/**\n\t * 创建新的 ObjectError 实例。\n\t * @param objectName 受影响对象的名称\n\t * @param defaultMessage 用于解析本消息的默认消息\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance of the ObjectError class.\n\t * @param objectName the name of the affected object\n\t * @param codes the codes to be used to resolve this message\n\t * @param arguments\tthe array of arguments to be used to resolve this message\n\t * @param defaultMessage the default message to be used to resolve this message\n\t */",
            "\t/**\n\t * 创建新的 ObjectError 实例。\n\t * @param objectName 受影响对象的名称\n\t * @param codes 用于解析本消息的 codes\n\t * @param arguments 用于解析本消息的参数数组\n\t * @param defaultMessage 用于解析本消息的默认消息\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of the affected object.\n\t */",
            "\t/**\n\t * 返回受影响对象的名称。\n\t */",
        ),
        (
            "\t/**\n\t * Preserve the source behind this error: possibly an {@link Exception}\n\t * (typically {@link org.springframework.beans.PropertyAccessException})\n\t * or a Bean Validation {@link jakarta.validation.ConstraintViolation}.\n\t * <p>Note that any such source object is being stored as transient:\n\t * that is, it won't be part of a serialized error representation.\n\t * @param source the source object\n\t * @since 5.0.4\n\t */",
            "\t/**\n\t * 保留本错误背后的源对象：可能是 {@link Exception}\n\t * （通常为 {@link org.springframework.beans.PropertyAccessException}）\n\t * 或 Bean Validation 的 {@link jakarta.validation.ConstraintViolation}。\n\t * <p>注意：此类源对象以 transient 存储，\n\t * 即不会成为序列化错误表示的一部分。\n\t * @param source 源对象\n\t * @since 5.0.4\n\t */",
        ),
        (
            "\t/**\n\t * Unwrap the source behind this error: possibly an {@link Exception}\n\t * (typically {@link org.springframework.beans.PropertyAccessException})\n\t * or a Bean Validation {@link jakarta.validation.ConstraintViolation}.\n\t * <p>The cause of the outermost exception will be introspected as well,\n\t * for example, the underlying conversion exception or exception thrown from a setter\n\t * (instead of having to unwrap the {@code PropertyAccessException} in turn).\n\t * @return the source object of the given type\n\t * @throws IllegalArgumentException if no such source object is available\n\t * (i.e. none specified or not available anymore after deserialization)\n\t * @since 5.0.4\n\t */",
            "\t/**\n\t * 解包本错误背后的源对象：可能是 {@link Exception}\n\t * （通常为 {@link org.springframework.beans.PropertyAccessException}）\n\t * 或 Bean Validation 的 {@link jakarta.validation.ConstraintViolation}。\n\t * <p>也会内省最外层异常的 cause，\n\t * 例如底层转换异常或 setter 抛出的异常\n\t * （无需再逐层解包 {@code PropertyAccessException}）。\n\t * @return 给定类型的源对象\n\t * @throws IllegalArgumentException 若无可用源对象\n\t * （即未指定或反序列化后不再可用）\n\t * @since 5.0.4\n\t */",
        ),
        (
            "\t/**\n\t * Check the source behind this error: possibly an {@link Exception}\n\t * (typically {@link org.springframework.beans.PropertyAccessException})\n\t * or a Bean Validation {@link jakarta.validation.ConstraintViolation}.\n\t * <p>The cause of the outermost exception will be introspected as well,\n\t * for example, the underlying conversion exception or exception thrown from a setter\n\t * (instead of having to unwrap the {@code PropertyAccessException} in turn).\n\t * @return whether this error has been caused by a source object of the given type\n\t * @since 5.0.4\n\t */",
            "\t/**\n\t * 检查本错误背后的源对象：可能是 {@link Exception}\n\t * （通常为 {@link org.springframework.beans.PropertyAccessException}）\n\t * 或 Bean Validation 的 {@link jakarta.validation.ConstraintViolation}。\n\t * <p>也会内省最外层异常的 cause，\n\t * 例如底层转换异常或 setter 抛出的异常\n\t * （无需再逐层解包 {@code PropertyAccessException}）。\n\t * @return 本错误是否由给定类型的源对象引起\n\t * @since 5.0.4\n\t */",
        ),
    ],
    "SimpleErrors.java": [
        (
            "/**\n * A simple implementation of the {@link Errors} interface, managing global\n * errors and field errors for a top-level target object. Flexibly retrieves\n * field values through bean property getter methods, and automatically\n * falls back to raw field access if necessary.\n *\n * <p>Note that this {@link Errors} implementation comes without support for\n * nested paths. It is exclusively designed for the validation of individual\n * top-level objects, not aggregating errors from multiple sources.\n * If this is insufficient for your purposes, use a binding-capable\n * {@link Errors} implementation such as {@link BeanPropertyBindingResult}.\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see Validator#validateObject(Object)\n * @see BeanPropertyBindingResult\n * @see DirectFieldBindingResult\n */",
            "/**\n * {@link Errors} 接口的简单实现，管理顶层目标对象的全局错误与字段错误。\n * 通过 bean 属性 getter 灵活获取字段值，必要时自动回退到原始字段访问。\n *\n * <p>注意：本 {@link Errors} 实现不支持嵌套路径，\n * 专用于校验单个顶层对象，不聚合多源错误。\n * 若不足，请使用支持绑定的 {@link Errors} 实现，如 {@link BeanPropertyBindingResult}。\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see Validator#validateObject(Object)\n * @see BeanPropertyBindingResult\n * @see DirectFieldBindingResult\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link SimpleErrors} holder for the given target,\n\t * using the simple name of the target class as the object name.\n\t * @param target the target to wrap\n\t */",
            "\t/**\n\t * 为给定目标创建新的 {@link SimpleErrors} 持有者，\n\t * 以目标类的简单类名作为对象名。\n\t * @param target 要包装的目标\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link SimpleErrors} holder for the given target.\n\t * @param target the target to wrap\n\t * @param objectName the name of the target object for error reporting\n\t */",
            "\t/**\n\t * 为给定目标创建新的 {@link SimpleErrors} 持有者。\n\t * @param target 要包装的目标\n\t * @param objectName 用于错误报告的目标对象名称\n\t */",
        ),
    ],
}
