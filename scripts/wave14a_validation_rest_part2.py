"""Chinese JavaDoc replacements for springframework wave14a validation messages+validator."""

VALIDATION_REST_PART2_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "DefaultMessageCodesResolver.java": [
        (
            "/**\n * Default implementation of the {@link MessageCodesResolver} interface.\n *\n * <p>Will create two message codes for an object error, in the following order (when\n * using the {@link Format#PREFIX_ERROR_CODE prefixed}\n * {@link #setMessageCodeFormatter(MessageCodeFormatter) formatter}):\n * <ul>\n * <li>1.: code + \".\" + object name\n * <li>2.: code\n * </ul>\n *\n * <p>Will create four message codes for a field specification, in the following order:\n * <ul>\n * <li>1.: code + \".\" + object name + \".\" + field\n * <li>2.: code + \".\" + field\n * <li>3.: code + \".\" + field type\n * <li>4.: code\n * </ul>\n *\n * <p>For example, in case of code \"typeMismatch\", object name \"user\", field \"age\":\n * <ul>\n * <li>1. try \"typeMismatch.user.age\"\n * <li>2. try \"typeMismatch.age\"\n * <li>3. try \"typeMismatch.int\"\n * <li>4. try \"typeMismatch\"\n * </ul>\n *\n * <p>This resolution algorithm thus can be leveraged for example to show\n * specific messages for binding errors like \"required\" and \"typeMismatch\":\n * <ul>\n * <li>at the object + field level (\"age\" field, but only on \"user\");\n * <li>at the field level (all \"age\" fields, no matter which object name);\n * <li>or at the general level (all fields, on any object).\n * </ul>\n *\n * <p>In case of array, {@link List} or {@link java.util.Map} properties,\n * both codes for specific elements and for the whole collection are\n * generated. Assuming a field \"name\" of an array \"groups\" in object \"user\":\n * <ul>\n * <li>1. try \"typeMismatch.user.groups[0].name\"\n * <li>2. try \"typeMismatch.user.groups.name\"\n * <li>3. try \"typeMismatch.groups[0].name\"\n * <li>4. try \"typeMismatch.groups.name\"\n * <li>5. try \"typeMismatch.name\"\n * <li>6. try \"typeMismatch.java.lang.String\"\n * <li>7. try \"typeMismatch\"\n * </ul>\n *\n * <p>By default the {@code errorCode}s will be placed at the beginning of constructed\n * message strings. The {@link #setMessageCodeFormatter(MessageCodeFormatter)\n * messageCodeFormatter} property can be used to specify an alternative concatenation\n * {@link MessageCodeFormatter format}.\n *\n * <p>In order to group all codes into a specific category within your resource bundles,\n * for example, \"validation.typeMismatch.name\" instead of the default \"typeMismatch.name\",\n * consider specifying a {@link #setPrefix prefix} to be applied.\n *\n * @author Juergen Hoeller\n * @author Phillip Webb\n * @author Chris Beams\n * @since 1.0.1\n */",
            "/**\n * {@link MessageCodesResolver} 接口的默认实现。\n *\n * <p>对对象错误将按以下顺序创建两条消息码\n *（使用 {@link Format#PREFIX_ERROR_CODE 前缀}\n * {@link #setMessageCodeFormatter(MessageCodeFormatter) 格式化器} 时）：\n * <ul>\n * <li>1.: code + \".\" + object name\n * <li>2.: code\n * </ul>\n *\n * <p>对字段规格将按以下顺序创建四条消息码：\n * <ul>\n * <li>1.: code + \".\" + object name + \".\" + field\n * <li>2.: code + \".\" + field\n * <li>3.: code + \".\" + field type\n * <li>4.: code\n * </ul>\n *\n * <p>例如 errorCode 为 \"typeMismatch\"、object name 为 \"user\"、field 为 \"age\" 时：\n * <ul>\n * <li>1. 尝试 \"typeMismatch.user.age\"\n * <li>2. 尝试 \"typeMismatch.age\"\n * <li>3. 尝试 \"typeMismatch.int\"\n * <li>4. 尝试 \"typeMismatch\"\n * </ul>\n *\n * <p>因此该解析算法可用于为 \"required\"、\"typeMismatch\" 等绑定错误\n * 显示不同粒度的消息：\n * <ul>\n * <li>对象 + 字段级别（\"age\" 字段，但仅限 \"user\" 对象）；\n * <li>字段级别（所有 \"age\" 字段，不限对象名）；\n * <li>或通用级别（任意对象上的所有字段）。\n * </ul>\n *\n * <p>对于数组、{@link List} 或 {@link java.util.Map} 属性，\n * 会同时生成针对特定元素与整个集合的 codes。\n * 假设 object \"user\" 中数组 \"groups\" 的字段 \"name\"：\n * <ul>\n * <li>1. 尝试 \"typeMismatch.user.groups[0].name\"\n * <li>2. 尝试 \"typeMismatch.user.groups.name\"\n * <li>3. 尝试 \"typeMismatch.groups[0].name\"\n * <li>4. 尝试 \"typeMismatch.groups.name\"\n * <li>5. 尝试 \"typeMismatch.name\"\n * <li>6. 尝试 \"typeMismatch.java.lang.String\"\n * <li>7. 尝试 \"typeMismatch\"\n * </ul>\n *\n * <p>默认 {@code errorCode} 置于构造消息串的开头。\n * 可通过 {@link #setMessageCodeFormatter(MessageCodeFormatter) messageCodeFormatter}\n * 属性指定替代的 {@link MessageCodeFormatter 拼接格式}。\n *\n * <p>若要在资源包中将所有 codes 归入特定类别，\n * 例如 \"validation.typeMismatch.name\" 而非默认 \"typeMismatch.name\"，\n * 可指定要应用的 {@link #setPrefix prefix}。\n *\n * @author Juergen Hoeller\n * @author Phillip Webb\n * @author Chris Beams\n * @since 1.0.1\n */",
        ),
        (
            "\t/**\n\t * The separator that this implementation uses when resolving message codes.\n\t */",
            "\t/**\n\t * 本实现解析消息码时使用的分隔符。\n\t */",
        ),
        (
            "\t/**\n\t * Specify a prefix to be applied to any code built by this resolver.\n\t * <p>Default is none. Specify, for example, \"validation.\" to get\n\t * error codes like \"validation.typeMismatch.name\".\n\t */",
            "\t/**\n\t * 指定本解析器构建的任意 code 要应用的前缀。\n\t * <p>默认无前缀。例如指定 \"validation.\" 可得到\n\t * \"validation.typeMismatch.name\" 等错误码。\n\t */",
        ),
        (
            "\t/**\n\t * Return the prefix to be applied to any code built by this resolver.\n\t * <p>Returns an empty String in case of no prefix.\n\t */",
            "\t/**\n\t * 返回本解析器构建的任意 code 要应用的前缀。\n\t * <p>无前缀时返回空字符串。\n\t */",
        ),
        (
            "\t/**\n\t * Specify the format for message codes built by this resolver.\n\t * <p>The default is {@link Format#PREFIX_ERROR_CODE}.\n\t * @since 3.2\n\t * @see Format\n\t */",
            "\t/**\n\t * 指定本解析器构建消息码的格式。\n\t * <p>默认为 {@link Format#PREFIX_ERROR_CODE}。\n\t * @since 3.2\n\t * @see Format\n\t */",
        ),
        (
            "\t/**\n\t * Build the code list for the given code and field: an\n\t * object/field-specific code, a field-specific code, a plain error code.\n\t * <p>Arrays, Lists and Maps are resolved both for specific elements and\n\t * the whole collection.\n\t * <p>See the {@link DefaultMessageCodesResolver class level javadoc} for\n\t * details on the generated codes.\n\t * @return the list of codes\n\t */",
            "\t/**\n\t * 为给定 code 与 field 构建 code 列表：\n\t * 对象/字段特定 code、字段特定 code、纯 error code。\n\t * <p>数组、List 与 Map 会同时解析特定元素与整个集合。\n\t * <p>生成 codes 的详情见 {@link DefaultMessageCodesResolver 类级 JavaDoc}。\n\t * @return code 列表\n\t */",
        ),
        (
            "\t/**\n\t * Add both keyed and non-keyed entries for the supplied {@code field}\n\t * to the supplied field list.\n\t */",
            "\t/**\n\t * 将给定 {@code field} 的带键与不带键条目均添加到给定字段列表。\n\t */",
        ),
        (
            "\t/**\n\t * Post-process the given message code, built by this resolver.\n\t * <p>The default implementation applies the specified prefix, if any.\n\t * @param code the message code as built by this resolver\n\t * @return the final message code to be returned\n\t * @see #setPrefix\n\t */",
            "\t/**\n\t * 对本解析器构建的给定消息码进行后处理。\n\t * <p>默认实现应用指定前缀（若有）。\n\t * @param code 本解析器构建的消息码\n\t * @return 最终返回的消息码\n\t * @see #setPrefix\n\t */",
        ),
        (
            "\t/**\n\t * Common message code formats.\n\t * @see MessageCodeFormatter\n\t * @see DefaultMessageCodesResolver#setMessageCodeFormatter(MessageCodeFormatter)\n\t */",
            "\t/**\n\t * 常见消息码格式。\n\t * @see MessageCodeFormatter\n\t * @see DefaultMessageCodesResolver#setMessageCodeFormatter(MessageCodeFormatter)\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Prefix the error code at the beginning of the generated message code. for example:\n\t\t * {@code errorCode + \".\" + object name + \".\" + field}\n\t\t */",
            "\t\t/**\n\t\t * 将 errorCode 前缀到生成的消息码开头，例如：\n\t\t * {@code errorCode + \".\" + object name + \".\" + field}\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Postfix the error code at the end of the generated message code. for example:\n\t\t * {@code object name + \".\" + field + \".\" + errorCode}\n\t\t */",
            "\t\t/**\n\t\t * 将 errorCode 后缀到生成的消息码末尾，例如：\n\t\t * {@code object name + \".\" + field + \".\" + errorCode}\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Concatenate the given elements, delimiting each with\n\t\t * {@link DefaultMessageCodesResolver#CODE_SEPARATOR}, skipping zero-length or\n\t\t * null elements altogether.\n\t\t */",
            "\t\t/**\n\t\t * 拼接给定元素，以 {@link DefaultMessageCodesResolver#CODE_SEPARATOR} 分隔，\n\t\t * 完全跳过零长度或 null 元素。\n\t\t */",
        ),
    ],
    "MessageCodeFormatter.java": [
        (
            "/**\n * A strategy interface for formatting message codes.\n *\n * @author Chris Beams\n * @since 3.2\n * @see DefaultMessageCodesResolver\n * @see DefaultMessageCodesResolver.Format\n */",
            "/**\n * 格式化消息码的策略接口。\n *\n * @author Chris Beams\n * @since 3.2\n * @see DefaultMessageCodesResolver\n * @see DefaultMessageCodesResolver.Format\n */",
        ),
        (
            "\t/**\n\t * Build and return a message code consisting of the given fields,\n\t * usually delimited by {@link DefaultMessageCodesResolver#CODE_SEPARATOR}.\n\t * @param errorCode for example: \"typeMismatch\"\n\t * @param objectName for example: \"user\"\n\t * @param field for example, \"age\"\n\t * @return concatenated message code, for example: \"typeMismatch.user.age\"\n\t * @see DefaultMessageCodesResolver.Format\n\t */",
            "\t/**\n\t * 构建并返回由给定字段组成的消息码，\n\t * 通常以 {@link DefaultMessageCodesResolver#CODE_SEPARATOR} 分隔。\n\t * @param errorCode 例如：\"typeMismatch\"\n\t * @param objectName 例如：\"user\"\n\t * @param field 例如：\"age\"\n\t * @return 拼接后的消息码，例如：\"typeMismatch.user.age\"\n\t * @see DefaultMessageCodesResolver.Format\n\t */",
        ),
    ],
    "MessageCodesResolver.java": [
        (
            "/**\n * Strategy interface for building message codes from validation error codes.\n * Used by DataBinder to build the codes list for ObjectErrors and FieldErrors.\n *\n * <p>The resulting message codes correspond to the codes of a\n * MessageSourceResolvable (as implemented by ObjectError and FieldError).\n *\n * @author Juergen Hoeller\n * @since 1.0.1\n * @see DataBinder#setMessageCodesResolver\n * @see ObjectError\n * @see FieldError\n * @see org.springframework.context.MessageSourceResolvable#getCodes()\n */",
            "/**\n * 从校验错误码构建消息码的策略接口。\n * 供 DataBinder 为 ObjectError 与 FieldError 构建 codes 列表。\n *\n * <p>生成的消息码对应 MessageSourceResolvable 的 codes\n *（由 ObjectError 与 FieldError 实现）。\n *\n * @author Juergen Hoeller\n * @since 1.0.1\n * @see DataBinder#setMessageCodesResolver\n * @see ObjectError\n * @see FieldError\n * @see org.springframework.context.MessageSourceResolvable#getCodes()\n */",
        ),
        (
            "\t/**\n\t * Build message codes for the given error code and object name.\n\t * Used for building the codes list of an ObjectError.\n\t * @param errorCode the error code used for rejecting the object\n\t * @param objectName the name of the object\n\t * @return the message codes to use\n\t */",
            "\t/**\n\t * 为给定 errorCode 与 objectName 构建消息码。\n\t * 用于构建 ObjectError 的 codes 列表。\n\t * @param errorCode 用于拒绝对象的 errorCode\n\t * @param objectName 对象名称\n\t * @return 要使用的消息码\n\t */",
        ),
        (
            "\t/**\n\t * Build message codes for the given error code and field specification.\n\t * Used for building the codes list of an FieldError.\n\t * @param errorCode the error code used for rejecting the value\n\t * @param objectName the name of the object\n\t * @param field the field name\n\t * @param fieldType the field type (may be {@code null} if not determinable)\n\t * @return the message codes to use\n\t */",
            "\t/**\n\t * 为给定 errorCode 与字段规格构建消息码。\n\t * 用于构建 FieldError 的 codes 列表。\n\t * @param errorCode 用于拒绝值的 errorCode\n\t * @param objectName 对象名称\n\t * @param field 字段名\n\t * @param fieldType 字段类型（无法确定时可为 {@code null}）\n\t * @return 要使用的消息码\n\t */",
        ),
    ],
    "SmartValidator.java": [
        (
            "/**\n * Extended variant of the {@link Validator} interface, adding support for\n * validation 'hints'.\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 3.1\n */",
            "/**\n * {@link Validator} 接口的扩展变体，增加对校验“提示（hints）”的支持。\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 3.1\n */",
        ),
        (
            "\t/**\n\t * Validate the supplied {@code target} object, which must be of a type of {@link Class}\n\t * for which the {@link #supports(Class)} method typically returns {@code true}.\n\t * <p>The supplied {@link Errors errors} instance can be used to report any\n\t * resulting validation errors.\n\t * <p><b>This variant of {@code validate()} supports validation hints, such as\n\t * validation groups against a JSR-303 provider</b> (in which case, the provided hint\n\t * objects need to be annotation arguments of type {@code Class}).\n\t * <p>Note: Validation hints may get ignored by the actual target {@code Validator},\n\t * in which case this method should behave just like its regular\n\t * {@link #validate(Object, Errors)} sibling.\n\t * @param target the object that is to be validated\n\t * @param errors contextual state about the validation process\n\t * @param validationHints one or more hint objects to be passed to the validation engine\n\t * @see jakarta.validation.Validator#validate(Object, Class[])\n\t */",
            "\t/**\n\t * 校验提供的 {@code target} 对象，其类型须为 {@link #supports(Class)} 通常返回 {@code true} 的类型。\n\t * <p>提供的 {@link Errors errors} 实例可用于报告产生的校验错误。\n\t * <p><b>本 {@code validate()} 变体支持校验提示，\n\t * 例如针对 JSR-303 提供者的校验组</b>（此时提供的 hint 对象须为 {@code Class} 类型的注解参数）。\n\t * <p>注意：实际目标 {@code Validator} 可能忽略校验提示，\n\t * 此时本方法行为应与常规 {@link #validate(Object, Errors)} 相同。\n\t * @param target 要校验的对象\n\t * @param errors 校验过程的上下文状态\n\t * @param validationHints 要传递给校验引擎的一个或多个 hint 对象\n\t * @see jakarta.validation.Validator#validate(Object, Class[])\n\t */",
        ),
        (
            "\t/**\n\t * Validate the supplied value for the specified field on the target type,\n\t * reporting the same validation errors as if the value would be bound to\n\t * the field on an instance of the target class.\n\t * @param targetType the target type\n\t * @param fieldName the name of the field\n\t * @param value the candidate value\n\t * @param errors contextual state about the validation process\n\t * @param validationHints one or more hint objects to be passed to the validation engine\n\t * @since 5.1\n\t * @see jakarta.validation.Validator#validateValue(Class, String, Object, Class[])\n\t */",
            "\t/**\n\t * 校验目标类型上指定字段的给定值，\n\t * 报告与将该值绑定到目标类实例字段时相同的校验错误。\n\t * @param targetType 目标类型\n\t * @param fieldName 字段名\n\t * @param value 候选值\n\t * @param errors 校验过程的上下文状态\n\t * @param validationHints 要传递给校验引擎的一个或多个 hint 对象\n\t * @since 5.1\n\t * @see jakarta.validation.Validator#validateValue(Class, String, Object, Class[])\n\t */",
        ),
        (
            "\t/**\n\t * Return a contained validator instance of the specified type, unwrapping\n\t * as far as necessary.\n\t * @param type the class of the object to return\n\t * @param <T> the type of the object to return\n\t * @return a validator instance of the specified type; {@code null} if there\n\t * isn't a nested validator; an exception may be raised if the specified\n\t * validator type does not match.\n\t * @since 6.1\n\t */",
            "\t/**\n\t * 返回指定类型的内部校验器实例，必要时逐层解包。\n\t * @param type 要返回的对象的 Class\n\t * @param <T> 要返回的对象类型\n\t * @return 指定类型的校验器实例；无嵌套校验器时返回 {@code null}；\n\t * 指定校验器类型不匹配时可能抛出异常。\n\t * @since 6.1\n\t */",
        ),
    ],
    "TypedValidator.java": [
        (
            "/**\n * Validator instance returned by {@link Validator#forInstanceOf(Class, BiConsumer)}\n * and {@link Validator#forType(Class, BiConsumer)}.\n *\n * @author Toshiaki Maki\n * @author Arjen Poutsma\n * @since 6.1\n * @param <T> the target object type\n */",
            "/**\n * 由 {@link Validator#forInstanceOf(Class, BiConsumer)} 与\n * {@link Validator#forType(Class, BiConsumer)} 返回的 Validator 实例。\n *\n * @author Toshiaki Maki\n * @author Arjen Poutsma\n * @since 6.1\n * @param <T> 目标对象类型\n */",
        ),
    ],
    "ValidationUtils.java": [
        (
            "/**\n * Utility class offering convenient methods for invoking a {@link Validator}\n * and for rejecting empty fields.\n *\n * <p>Checks for an empty field in {@code Validator} implementations can become\n * one-liners when using {@link #rejectIfEmpty} or {@link #rejectIfEmptyOrWhitespace}.\n *\n * @author Juergen Hoeller\n * @author Dmitriy Kopylenko\n * @since 06.05.2003\n * @see Validator\n * @see Errors\n */",
            "/**\n * 提供调用 {@link Validator} 与拒绝空字段便捷方法的工具类。\n *\n * <p>在 {@code Validator} 实现中检查空字段时，\n * 使用 {@link #rejectIfEmpty} 或 {@link #rejectIfEmptyOrWhitespace} 可简化为一行代码。\n *\n * @author Juergen Hoeller\n * @author Dmitriy Kopylenko\n * @since 06.05.2003\n * @see Validator\n * @see Errors\n */",
        ),
        (
            "\t/**\n\t * Invoke the given {@link Validator} for the supplied object and\n\t * {@link Errors} instance.\n\t * @param validator the {@code Validator} to be invoked\n\t * @param target the object to bind the parameters to\n\t * @param errors the {@link Errors} instance that should store the errors\n\t * @throws IllegalArgumentException if either of the {@code Validator} or {@code Errors}\n\t * arguments is {@code null}, or if the supplied {@code Validator} does not\n\t * {@link Validator#supports(Class) support} the validation of the supplied object's type\n\t */",
            "\t/**\n\t * 对给定对象与 {@link Errors} 实例调用给定 {@link Validator}。\n\t * @param validator 要调用的 {@code Validator}\n\t * @param target 要绑定参数的对象\n\t * @param errors 应存储错误的 {@link Errors} 实例\n\t * @throws IllegalArgumentException 若 {@code Validator} 或 {@code Errors} 参数为 {@code null}，\n\t * 或提供的 {@code Validator} 不 {@link Validator#supports(Class) 支持} 给定对象类型的校验\n\t */",
        ),
        (
            "\t/**\n\t * Invoke the given {@link Validator}/{@link SmartValidator} for the supplied object and\n\t * {@link Errors} instance.\n\t * @param validator the {@code Validator} to be invoked\n\t * @param target the object to bind the parameters to\n\t * @param errors the {@link Errors} instance that should store the errors\n\t * @param validationHints one or more hint objects to be passed to the validation engine\n\t * @throws IllegalArgumentException if either of the {@code Validator} or {@code Errors}\n\t * arguments is {@code null}, or if the supplied {@code Validator} does not\n\t * {@link Validator#supports(Class) support} the validation of the supplied object's type\n\t */",
            "\t/**\n\t * 对给定对象与 {@link Errors} 实例调用给定 {@link Validator}/{@link SmartValidator}。\n\t * @param validator 要调用的 {@code Validator}\n\t * @param target 要绑定参数的对象\n\t * @param errors 应存储错误的 {@link Errors} 实例\n\t * @param validationHints 要传递给校验引擎的一个或多个 hint 对象\n\t * @throws IllegalArgumentException 若 {@code Validator} 或 {@code Errors} 参数为 {@code null}，\n\t * 或提供的 {@code Validator} 不 {@link Validator#supports(Class) 支持} 给定对象类型的校验\n\t */",
        ),
        (
            "\t/**\n\t * Reject the given field with the given error code if the value is empty.\n\t * <p>An 'empty' value in this context means either {@code null} or\n\t * the empty string \"\".\n\t * <p>The object whose field is being validated does not need to be passed\n\t * in because the {@link Errors} instance can resolve field values by itself\n\t * (it will usually hold an internal reference to the target object).\n\t * @param errors the {@code Errors} instance to register errors on\n\t * @param field the field name to check\n\t * @param errorCode the error code, interpretable as message key\n\t */",
            "\t/**\n\t * 若值为空，以给定 errorCode 拒绝给定字段。\n\t * <p>此上下文中“空”指 {@code null} 或空字符串 \"\"。\n\t * <p>无需传入被校验字段所属对象，\n\t * 因 {@link Errors} 实例可自行解析字段值（通常持有目标对象的内部引用）。\n\t * @param errors 要注册错误的 {@code Errors} 实例\n\t * @param field 要检查的字段名\n\t * @param errorCode 错误码，可解释为消息键\n\t */",
        ),
        (
            "\t/**\n\t * Reject the given field with the given error code and default message\n\t * if the value is empty.\n\t * <p>An 'empty' value in this context means either {@code null} or\n\t * the empty string \"\".\n\t * <p>The object whose field is being validated does not need to be passed\n\t * in because the {@link Errors} instance can resolve field values by itself\n\t * (it will usually hold an internal reference to the target object).\n\t * @param errors the {@code Errors} instance to register errors on\n\t * @param field the field name to check\n\t * @param errorCode error code, interpretable as message key\n\t * @param defaultMessage fallback default message\n\t */",
            "\t/**\n\t * 若值为空，以给定 errorCode 与 defaultMessage 拒绝给定字段。\n\t * <p>此上下文中“空”指 {@code null} 或空字符串 \"\"。\n\t * <p>无需传入被校验字段所属对象，\n\t * 因 {@link Errors} 实例可自行解析字段值（通常持有目标对象的内部引用）。\n\t * @param errors 要注册错误的 {@code Errors} 实例\n\t * @param field 要检查的字段名\n\t * @param errorCode 错误码，可解释为消息键\n\t * @param defaultMessage 后备默认消息\n\t */",
        ),
        (
            "\t/**\n\t * Reject the given field with the given error code and error arguments\n\t * if the value is empty.\n\t * <p>An 'empty' value in this context means either {@code null} or\n\t * the empty string \"\".\n\t * <p>The object whose field is being validated does not need to be passed\n\t * in because the {@link Errors} instance can resolve field values by itself\n\t * (it will usually hold an internal reference to the target object).\n\t * @param errors the {@code Errors} instance to register errors on\n\t * @param field the field name to check\n\t * @param errorCode the error code, interpretable as message key\n\t * @param errorArgs the error arguments, for argument binding via MessageFormat\n\t * (can be {@code null})\n\t */",
            "\t/**\n\t * 若值为空，以给定 errorCode 与 errorArgs 拒绝给定字段。\n\t * <p>此上下文中“空”指 {@code null} 或空字符串 \"\"。\n\t * <p>无需传入被校验字段所属对象，\n\t * 因 {@link Errors} 实例可自行解析字段值（通常持有目标对象的内部引用）。\n\t * @param errors 要注册错误的 {@code Errors} 实例\n\t * @param field 要检查的字段名\n\t * @param errorCode 错误码，可解释为消息键\n\t * @param errorArgs 错误参数，用于 MessageFormat 参数绑定（可为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Reject the given field with the given error code, error arguments\n\t * and default message if the value is empty.\n\t * <p>An 'empty' value in this context means either {@code null} or\n\t * the empty string \"\".\n\t * <p>The object whose field is being validated does not need to be passed\n\t * in because the {@link Errors} instance can resolve field values by itself\n\t * (it will usually hold an internal reference to the target object).\n\t * @param errors the {@code Errors} instance to register errors on\n\t * @param field the field name to check\n\t * @param errorCode the error code, interpretable as message key\n\t * @param errorArgs the error arguments, for argument binding via MessageFormat\n\t * (can be {@code null})\n\t * @param defaultMessage fallback default message\n\t */",
            "\t/**\n\t * 若值为空，以给定 errorCode、errorArgs 与 defaultMessage 拒绝给定字段。\n\t * <p>此上下文中“空”指 {@code null} 或空字符串 \"\"。\n\t * <p>无需传入被校验字段所属对象，\n\t * 因 {@link Errors} 实例可自行解析字段值（通常持有目标对象的内部引用）。\n\t * @param errors 要注册错误的 {@code Errors} 实例\n\t * @param field 要检查的字段名\n\t * @param errorCode 错误码，可解释为消息键\n\t * @param errorArgs 错误参数，用于 MessageFormat 参数绑定（可为 {@code null}）\n\t * @param defaultMessage 后备默认消息\n\t */",
        ),
        (
            "\t/**\n\t * Reject the given field with the given error code if the value is empty\n\t * or just contains whitespace.\n\t * <p>An 'empty' value in this context means either {@code null},\n\t * the empty string \"\", or consisting wholly of whitespace.\n\t * <p>The object whose field is being validated does not need to be passed\n\t * in because the {@link Errors} instance can resolve field values by itself\n\t * (it will usually hold an internal reference to the target object).\n\t * @param errors the {@code Errors} instance to register errors on\n\t * @param field the field name to check\n\t * @param errorCode the error code, interpretable as message key\n\t */",
            "\t/**\n\t * 若值为空或仅含空白，以给定 errorCode 拒绝给定字段。\n\t * <p>此上下文中“空”指 {@code null}、空字符串 \"\" 或全为空白字符。\n\t * <p>无需传入被校验字段所属对象，\n\t * 因 {@link Errors} 实例可自行解析字段值（通常持有目标对象的内部引用）。\n\t * @param errors 要注册错误的 {@code Errors} 实例\n\t * @param field 要检查的字段名\n\t * @param errorCode 错误码，可解释为消息键\n\t */",
        ),
        (
            "\t/**\n\t * Reject the given field with the given error code and default message\n\t * if the value is empty or just contains whitespace.\n\t * <p>An 'empty' value in this context means either {@code null},\n\t * the empty string \"\", or consisting wholly of whitespace.\n\t * <p>The object whose field is being validated does not need to be passed\n\t * in because the {@link Errors} instance can resolve field values by itself\n\t * (it will usually hold an internal reference to the target object).\n\t * @param errors the {@code Errors} instance to register errors on\n\t * @param field the field name to check\n\t * @param errorCode the error code, interpretable as message key\n\t * @param defaultMessage fallback default message\n\t */",
            "\t/**\n\t * 若值为空或仅含空白，以给定 errorCode 与 defaultMessage 拒绝给定字段。\n\t * <p>此上下文中“空”指 {@code null}、空字符串 \"\" 或全为空白字符。\n\t * <p>无需传入被校验字段所属对象，\n\t * 因 {@link Errors} 实例可自行解析字段值（通常持有目标对象的内部引用）。\n\t * @param errors 要注册错误的 {@code Errors} 实例\n\t * @param field 要检查的字段名\n\t * @param errorCode 错误码，可解释为消息键\n\t * @param defaultMessage 后备默认消息\n\t */",
        ),
        (
            "\t/**\n\t * Reject the given field with the given error code and error arguments\n\t * if the value is empty or just contains whitespace.\n\t * <p>An 'empty' value in this context means either {@code null},\n\t * the empty string \"\", or consisting wholly of whitespace.\n\t * <p>The object whose field is being validated does not need to be passed\n\t * in because the {@link Errors} instance can resolve field values by itself\n\t * (it will usually hold an internal reference to the target object).\n\t * @param errors the {@code Errors} instance to register errors on\n\t * @param field the field name to check\n\t * @param errorCode the error code, interpretable as message key\n\t * @param errorArgs the error arguments, for argument binding via MessageFormat\n\t * (can be {@code null})\n\t */",
            "\t/**\n\t * 若值为空或仅含空白，以给定 errorCode 与 errorArgs 拒绝给定字段。\n\t * <p>此上下文中“空”指 {@code null}、空字符串 \"\" 或全为空白字符。\n\t * <p>无需传入被校验字段所属对象，\n\t * 因 {@link Errors} 实例可自行解析字段值（通常持有目标对象的内部引用）。\n\t * @param errors 要注册错误的 {@code Errors} 实例\n\t * @param field 要检查的字段名\n\t * @param errorCode 错误码，可解释为消息键\n\t * @param errorArgs 错误参数，用于 MessageFormat 参数绑定（可为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Reject the given field with the given error code, error arguments\n\t * and default message if the value is empty or just contains whitespace.\n\t * <p>An 'empty' value in this context means either {@code null},\n\t * the empty string \"\", or consisting wholly of whitespace.\n\t * <p>The object whose field is being validated does not need to be passed\n\t * in because the {@link Errors} instance can resolve field values by itself\n\t * (it will usually hold an internal reference to the target object).\n\t * @param errors the {@code Errors} instance to register errors on\n\t * @param field the field name to check\n\t * @param errorCode the error code, interpretable as message key\n\t * @param errorArgs the error arguments, for argument binding via MessageFormat\n\t * (can be {@code null})\n\t * @param defaultMessage fallback default message\n\t */",
            "\t/**\n\t * 若值为空或仅含空白，以给定 errorCode、errorArgs 与 defaultMessage 拒绝给定字段。\n\t * <p>此上下文中“空”指 {@code null}、空字符串 \"\" 或全为空白字符。\n\t * <p>无需传入被校验字段所属对象，\n\t * 因 {@link Errors} 实例可自行解析字段值（通常持有目标对象的内部引用）。\n\t * @param errors 要注册错误的 {@code Errors} 实例\n\t * @param field 要检查的字段名\n\t * @param errorCode 错误码，可解释为消息键\n\t * @param errorArgs 错误参数，用于 MessageFormat 参数绑定（可为 {@code null}）\n\t * @param defaultMessage 后备默认消息\n\t */",
        ),
    ],
}
