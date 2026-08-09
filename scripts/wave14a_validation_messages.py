"""Chinese JavaDoc replacements for springframework wave14a validation messages [8:15]."""

VALIDATION_MESSAGES_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "DefaultMessageCodesResolver.java": [
        (
            "/**\n * Default implementation of the {@link MessageCodesResolver} interface.\n *\n * <p>Will create two message codes for an object error, in the following order (when\n * using the {@link Format#PREFIX_ERROR_CODE prefixed}\n * {@link #setMessageCodeFormatter(MessageCodeFormatter) formatter}):\n * <ul>\n * <li>1.: code + \".\" + object name\n * <li>2.: code\n * </ul>\n *\n * <p>Will create four message codes for a field specification, in the following order:\n * <ul>\n * <li>1.: code + \".\" + object name + \".\" + field\n * <li>2.: code + \".\" + field\n * <li>3.: code + \".\" + field type\n * <li>4.: code\n * </ul>\n *\n * <p>For example, in case of code \"typeMismatch\", object name \"user\", field \"age\":\n * <ul>\n * <li>1. try \"typeMismatch.user.age\"\n * <li>2. try \"typeMismatch.age\"\n * <li>3. try \"typeMismatch.int\"\n * <li>4. try \"typeMismatch\"\n * </ul>\n *\n * <p>This resolution algorithm thus can be leveraged for example to show\n * specific messages for binding errors like \"required\" and \"typeMismatch\":\n * <ul>\n * <li>at the object + field level (\"age\" field, but only on \"user\");\n * <li>at the field level (all \"age\" fields, no matter which object name);\n * <li>or at the general level (all fields, on any object).\n * </ul>\n *\n * <p>In case of array, {@link List} or {@link java.util.Map} properties,\n * both codes for specific elements and for the whole collection are\n * generated. Assuming a field \"name\" of an array \"groups\" in object \"user\":\n * <ul>\n * <li>1. try \"typeMismatch.user.groups[0].name\"\n * <li>2. try \"typeMismatch.user.groups.name\"\n * <li>3. try \"typeMismatch.groups[0].name\"\n * <li>4. try \"typeMismatch.groups.name\"\n * <li>5. try \"typeMismatch.name\"\n * <li>6. try \"typeMismatch.java.lang.String\"\n * <li>7. try \"typeMismatch\"\n * </ul>\n *\n * <p>By default the {@code errorCode}s will be placed at the beginning of constructed\n * message strings. The {@link #setMessageCodeFormatter(MessageCodeFormatter)\n * messageCodeFormatter} property can be used to specify an alternative concatenation\n * {@link MessageCodeFormatter format}.\n *\n * <p>In order to group all codes into a specific category within your resource bundles,\n * for example, \"validation.typeMismatch.name\" instead of the default \"typeMismatch.name\",\n * consider specifying a {@link #setPrefix prefix} to be applied.\n *\n * @author Juergen Hoeller\n * @author Phillip Webb\n * @author Chris Beams\n * @since 1.0.1\n */",
            "/**\n * {@link MessageCodesResolver} 接口的默认实现。\n *\n * <p>对象错误将按以下顺序生成两条消息码（使用\n * {@link Format#PREFIX_ERROR_CODE 前缀式}\n * {@link #setMessageCodeFormatter(MessageCodeFormatter) 格式化器} 时）：\n * <ul>\n * <li>1.：code + \".\" + 对象名\n * <li>2.：code\n * </ul>\n *\n * <p>字段规范将按以下顺序生成四条消息码：\n * <ul>\n * <li>1.：code + \".\" + 对象名 + \".\" + 字段\n * <li>2.：code + \".\" + 字段\n * <li>3.：code + \".\" + 字段类型\n * <li>4.：code\n * </ul>\n *\n * <p>例如，code 为 \"typeMismatch\"、对象名为 \"user\"、字段为 \"age\" 时：\n * <ul>\n * <li>1. 尝试 \"typeMismatch.user.age\"\n * <li>2. 尝试 \"typeMismatch.age\"\n * <li>3. 尝试 \"typeMismatch.int\"\n * <li>4. 尝试 \"typeMismatch\"\n * </ul>\n *\n * <p>因此该解析算法可用于为 \"required\"、\"typeMismatch\" 等绑定错误\n * 展示特定消息：\n * <ul>\n * <li>对象 + 字段级别（\"age\" 字段，但仅限 \"user\" 对象）；\n * <li>字段级别（所有 \"age\" 字段，不限对象名）；\n * <li>或通用级别（任意对象上的所有字段）。\n * </ul>\n *\n * <p>对于数组、{@link List} 或 {@link java.util.Map} 属性，\n * 会同时生成针对特定元素与整个集合的消息码。\n * 假设对象 \"user\" 中数组 \"groups\" 的字段 \"name\"：\n * <ul>\n * <li>1. 尝试 \"typeMismatch.user.groups[0].name\"\n * <li>2. 尝试 \"typeMismatch.user.groups.name\"\n * <li>3. 尝试 \"typeMismatch.groups[0].name\"\n * <li>4. 尝试 \"typeMismatch.groups.name\"\n * <li>5. 尝试 \"typeMismatch.name\"\n * <li>6. 尝试 \"typeMismatch.java.lang.String\"\n * <li>7. 尝试 \"typeMismatch\"\n * </ul>\n *\n * <p>默认情况下 {@code errorCode} 置于构造消息字符串的开头。\n * 可通过 {@link #setMessageCodeFormatter(MessageCodeFormatter) messageCodeFormatter}\n * 属性指定替代的 {@link MessageCodeFormatter 拼接格式}。\n *\n * <p>若要在资源包中将所有码归入特定类别，\n * 例如 \"validation.typeMismatch.name\" 而非默认 \"typeMismatch.name\"，\n * 可考虑指定要应用的 {@link #setPrefix prefix}。\n *\n * @author Juergen Hoeller\n * @author Phillip Webb\n * @author Chris Beams\n * @since 1.0.1\n */",
        ),
        (
            "\t/**\n\t * The separator that this implementation uses when resolving message codes.\n\t */",
            "\t/**\n\t * 本实现在解析消息码时使用的分隔符。\n\t */",
        ),
        (
            "\t/**\n\t * Specify a prefix to be applied to any code built by this resolver.\n\t * <p>Default is none. Specify, for example, \"validation.\" to get\n\t * error codes like \"validation.typeMismatch.name\".\n\t */",
            "\t/**\n\t * 指定本解析器构造的任意消息码所应用的前缀。\n\t * <p>默认为无。例如指定 \"validation.\" 可得到\n\t * \"validation.typeMismatch.name\" 等错误码。\n\t */",
        ),
        (
            "\t/**\n\t * Return the prefix to be applied to any code built by this resolver.\n\t * <p>Returns an empty String in case of no prefix.\n\t */",
            "\t/**\n\t * 返回本解析器构造的任意消息码所应用的前缀。\n\t * <p>无前缀时返回空字符串。\n\t */",
        ),
        (
            "\t/**\n\t * Specify the format for message codes built by this resolver.\n\t * <p>The default is {@link Format#PREFIX_ERROR_CODE}.\n\t * @since 3.2\n\t * @see Format\n\t */",
            "\t/**\n\t * 指定本解析器构造的消息码格式。\n\t * <p>默认为 {@link Format#PREFIX_ERROR_CODE}。\n\t * @since 3.2\n\t * @see Format\n\t */",
        ),
        (
            "\t/**\n\t * Build the code list for the given code and field: an\n\t * object/field-specific code, a field-specific code, a plain error code.\n\t * <p>Arrays, Lists and Maps are resolved both for specific elements and\n\t * the whole collection.\n\t * <p>See the {@link DefaultMessageCodesResolver class level javadoc} for\n\t * details on the generated codes.\n\t * @return the list of codes\n\t */",
            "\t/**\n\t * 为给定 code 与 field 构建码列表：\n\t * 对象/字段特定码、字段特定码、纯错误码。\n\t * <p>数组、List 与 Map 会同时解析特定元素与整个集合。\n\t * <p>生成码的详情见 {@link DefaultMessageCodesResolver 类级 JavaDoc}。\n\t * @return 码列表\n\t */",
        ),
        (
            "\t/**\n\t * Add both keyed and non-keyed entries for the supplied {@code field}\n\t * to the supplied field list.\n\t */",
            "\t/**\n\t * 为给定 {@code field} 同时添加带键与不带键的条目到字段列表。\n\t */",
        ),
        (
            "\t/**\n\t * Post-process the given message code, built by this resolver.\n\t * <p>The default implementation applies the specified prefix, if any.\n\t * @param code the message code as built by this resolver\n\t * @return the final message code to be returned\n\t * @see #setPrefix\n\t */",
            "\t/**\n\t * 对本解析器构造的给定消息码进行后处理。\n\t * <p>默认实现应用指定前缀（若有）。\n\t * @param code 本解析器构造的消息码\n\t * @return 最终返回的消息码\n\t * @see #setPrefix\n\t */",
        ),
        (
            "\t/**\n\t * Common message code formats.\n\t * @see MessageCodeFormatter\n\t * @see DefaultMessageCodesResolver#setMessageCodeFormatter(MessageCodeFormatter)\n\t */",
            "\t/**\n\t * 常用消息码格式。\n\t * @see MessageCodeFormatter\n\t * @see DefaultMessageCodesResolver#setMessageCodeFormatter(MessageCodeFormatter)\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Prefix the error code at the beginning of the generated message code. for example:\n\t\t * {@code errorCode + \".\" + object name + \".\" + field}\n\t\t */",
            "\t\t/**\n\t\t * 将错误码置于生成消息码的开头，例如：\n\t\t * {@code errorCode + \".\" + object name + \".\" + field}\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Postfix the error code at the end of the generated message code. for example:\n\t\t * {@code object name + \".\" + field + \".\" + errorCode}\n\t\t */",
            "\t\t/**\n\t\t * 将错误码置于生成消息码的末尾，例如：\n\t\t * {@code object name + \".\" + field + \".\" + errorCode}\n\t\t */",
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
            "/**\n * 从校验错误码构建消息码的策略接口。\n * DataBinder 用它为 ObjectError 与 FieldError 构建 codes 列表。\n *\n * <p>生成的消息码对应 MessageSourceResolvable 的 codes\n * （由 ObjectError 与 FieldError 实现）。\n *\n * @author Juergen Hoeller\n * @since 1.0.1\n * @see DataBinder#setMessageCodesResolver\n * @see ObjectError\n * @see FieldError\n * @see org.springframework.context.MessageSourceResolvable#getCodes()\n */",
        ),
        (
            "\t/**\n\t * Build message codes for the given error code and object name.\n\t * Used for building the codes list of an ObjectError.\n\t * @param errorCode the error code used for rejecting the object\n\t * @param objectName the name of the object\n\t * @return the message codes to use\n\t */",
            "\t/**\n\t * 为给定错误码与对象名构建消息码。\n\t * 用于构建 ObjectError 的 codes 列表。\n\t * @param errorCode 用于拒绝对象的错误码\n\t * @param objectName 对象名称\n\t * @return 要使用的消息码\n\t */",
        ),
        (
            "\t/**\n\t * Build message codes for the given error code and field specification.\n\t * Used for building the codes list of an FieldError.\n\t * @param errorCode the error code used for rejecting the value\n\t * @param objectName the name of the object\n\t * @param field the field name\n\t * @param fieldType the field type (may be {@code null} if not determinable)\n\t * @return the message codes to use\n\t */",
            "\t/**\n\t * 为给定错误码与字段规范构建消息码。\n\t * 用于构建 FieldError 的 codes 列表。\n\t * @param errorCode 用于拒绝值的错误码\n\t * @param objectName 对象名称\n\t * @param field 字段名\n\t * @param fieldType 字段类型（无法确定时可为 {@code null}）\n\t * @return 要使用的消息码\n\t */",
        ),
    ],
}
