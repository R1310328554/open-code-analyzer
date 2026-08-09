"""Chinese JavaDoc replacements for springframework wave14a validation rest (abstract+messages+validator)."""

VALIDATION_REST_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractErrors.java": [
        (
            "/**\n * Abstract implementation of the {@link Errors} interface.\n * Provides nested path handling but does not define concrete management\n * of {@link ObjectError ObjectErrors} and {@link FieldError FieldErrors}.\n *\n * @author Juergen Hoeller\n * @author Rossen Stoyanchev\n * @since 2.5.3\n * @see AbstractBindingResult\n */",
            "/**\n * {@link Errors} 接口的抽象实现。\n * 提供嵌套路径处理，但不定义 {@link ObjectError ObjectErrors}\n * 与 {@link FieldError FieldErrors} 的具体管理方式。\n *\n * @author Juergen Hoeller\n * @author Rossen Stoyanchev\n * @since 2.5.3\n * @see AbstractBindingResult\n */",
        ),
        (
            "\t/**\n\t * Actually set the nested path.\n\t * Delegated to by setNestedPath and pushNestedPath.\n\t */",
            "\t/**\n\t * 实际设置嵌套路径。\n\t * 由 setNestedPath 与 pushNestedPath 委托调用。\n\t */",
        ),
        (
            "\t/**\n\t * Transform the given field into its full path,\n\t * regarding the nested path of this instance.\n\t */",
            "\t/**\n\t * 根据本实例的嵌套路径，将给定字段转换为完整路径。\n\t */",
        ),
        (
            "\t/**\n\t * Determine the canonical field name for the given field.\n\t * <p>The default implementation simply returns the field name as-is.\n\t * @param field the original field name\n\t * @return the canonical field name\n\t */",
            "\t/**\n\t * 确定给定字段的规范字段名。\n\t * <p>默认实现直接原样返回字段名。\n\t * @param field 原始字段名\n\t * @return 规范字段名\n\t */",
        ),
        (
            "\t/**\n\t * Check whether the given FieldError matches the given field.\n\t * @param field the field that we are looking up FieldErrors for\n\t * @param fieldError the candidate FieldError\n\t * @return whether the FieldError matches the given field\n\t */",
            "\t/**\n\t * 检查给定 {@link FieldError} 是否与给定字段匹配。\n\t * @param field 要查找 FieldError 的字段\n\t * @param fieldError 候选 FieldError\n\t * @return 该 FieldError 是否与给定字段匹配\n\t */",
        ),
        (
            "\t\t// Optimization: use charAt and regionMatches instead of endsWith and startsWith (SPR-11304)",
            "\t\t// 优化：使用 charAt 与 regionMatches 替代 endsWith 与 startsWith（SPR-11304）",
        ),
    ],
    "AbstractPropertyBindingResult.java": [
        (
            "/**\n * Abstract base class for {@link BindingResult} implementations that work with\n * Spring's {@link org.springframework.beans.PropertyAccessor} mechanism.\n * Pre-implements field access through delegation to the corresponding\n * PropertyAccessor methods.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see #getPropertyAccessor()\n * @see org.springframework.beans.PropertyAccessor\n * @see org.springframework.beans.ConfigurablePropertyAccessor\n */",
            "/**\n * 基于 Spring {@link org.springframework.beans.PropertyAccessor} 机制的\n * {@link BindingResult} 实现抽象基类。\n * 通过委托对应 PropertyAccessor 方法预实现字段访问。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see #getPropertyAccessor()\n * @see org.springframework.beans.PropertyAccessor\n * @see org.springframework.beans.ConfigurablePropertyAccessor\n */",
        ),
        (
            "\t/**\n\t * Create a new AbstractPropertyBindingResult instance.\n\t * @param objectName the name of the target object\n\t * @see DefaultMessageCodesResolver\n\t */",
            "\t/**\n\t * 创建新的 AbstractPropertyBindingResult 实例。\n\t * @param objectName 目标对象名称\n\t * @see DefaultMessageCodesResolver\n\t */",
        ),
        (
            "\t/**\n\t * Returns the underlying PropertyAccessor.\n\t * @see #getPropertyAccessor()\n\t */",
            "\t/**\n\t * 返回底层 PropertyAccessor。\n\t * @see #getPropertyAccessor()\n\t */",
        ),
        (
            "\t/**\n\t * Returns the canonical property name.\n\t * @see org.springframework.beans.PropertyAccessorUtils#canonicalPropertyName\n\t */",
            "\t/**\n\t * 返回规范属性名。\n\t * @see org.springframework.beans.PropertyAccessorUtils#canonicalPropertyName\n\t */",
        ),
        (
            "\t/**\n\t * Determines the field type from the property type.\n\t * @see #getPropertyAccessor()\n\t */",
            "\t/**\n\t * 根据属性类型确定字段类型。\n\t * @see #getPropertyAccessor()\n\t */",
        ),
        (
            "\t/**\n\t * Fetches the field value from the PropertyAccessor.\n\t * @see #getPropertyAccessor()\n\t */",
            "\t/**\n\t * 从 PropertyAccessor 获取字段值。\n\t * @see #getPropertyAccessor()\n\t */",
        ),
        (
            "\t/**\n\t * Formats the field value based on registered PropertyEditors.\n\t * @see #getCustomEditor\n\t */",
            "\t/**\n\t * 基于已注册的 PropertyEditor 格式化字段值。\n\t * @see #getCustomEditor\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the custom PropertyEditor for the given field, if any.\n\t * @param fixedField the fully qualified field name\n\t * @return the custom PropertyEditor, or {@code null}\n\t */",
            "\t/**\n\t * 获取给定字段的自定义 PropertyEditor（若有）。\n\t * @param fixedField 完全限定字段名\n\t * @return 自定义 PropertyEditor，或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * This implementation exposes a PropertyEditor adapter for a Formatter,\n\t * if applicable.\n\t */",
            "\t/**\n\t * 本实现会在适用时暴露 Formatter 对应的 PropertyEditor 适配器。\n\t */",
        ),
        (
            "\t/**\n\t * Provide the PropertyAccessor to work with, according to the\n\t * concrete strategy of access.\n\t * <p>Note that a PropertyAccessor used by a BindingResult should\n\t * always have its \"extractOldValueForEditor\" flag set to \"true\"\n\t * by default, since this is typically possible without side effects\n\t * for model objects that serve as data binding target.\n\t * @see ConfigurablePropertyAccessor#setExtractOldValueForEditor\n\t */",
            "\t/**\n\t * 按具体访问策略提供要使用的 PropertyAccessor。\n\t * <p>注意：BindingResult 使用的 PropertyAccessor 默认应将\n\t * {@code extractOldValueForEditor} 标志设为 {@code true}，\n\t * 因为作为数据绑定目标的模型对象通常可无副作用地提取旧值。\n\t * @see ConfigurablePropertyAccessor#setExtractOldValueForEditor\n\t */",
        ),
    ],
    "BeanPropertyBindingResult.java": [
        (
            "/**\n * Default implementation of the {@link Errors} and {@link BindingResult}\n * interfaces, for the registration and evaluation of binding errors on\n * JavaBean objects.\n *\n * <p>Performs standard JavaBean property access, also supporting nested\n * properties. Normally, application code will work with the\n * {@code Errors} interface or the {@code BindingResult} interface.\n * A {@link DataBinder} returns its {@code BindingResult} via\n * {@link DataBinder#getBindingResult()}.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see DataBinder#getBindingResult()\n * @see DataBinder#initBeanPropertyAccess()\n * @see DirectFieldBindingResult\n */",
            "/**\n * {@link Errors} 与 {@link BindingResult} 接口的默认实现，\n * 用于在 JavaBean 对象上注册并评估绑定错误。\n *\n * <p>执行标准 JavaBean 属性访问，并支持嵌套属性。\n * 通常应用代码使用 {@code Errors} 或 {@code BindingResult} 接口。\n * {@link DataBinder} 通过 {@link DataBinder#getBindingResult()} 返回其 BindingResult。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see DataBinder#getBindingResult()\n * @see DataBinder#initBeanPropertyAccess()\n * @see DirectFieldBindingResult\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code BeanPropertyBindingResult} for the given target.\n\t * @param target the target bean to bind onto\n\t * @param objectName the name of the target object\n\t */",
            "\t/**\n\t * 为给定目标创建新的 {@code BeanPropertyBindingResult}。\n\t * @param target 要绑定到的目标 bean\n\t * @param objectName 目标对象名称\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code BeanPropertyBindingResult} for the given target.\n\t * @param target the target bean to bind onto\n\t * @param objectName the name of the target object\n\t * @param autoGrowNestedPaths whether to \"auto-grow\" a nested path that contains a null value\n\t * @param autoGrowCollectionLimit the limit for array and collection auto-growing\n\t */",
            "\t/**\n\t * 为给定目标创建新的 {@code BeanPropertyBindingResult}。\n\t * @param target 要绑定到的目标 bean\n\t * @param objectName 目标对象名称\n\t * @param autoGrowNestedPaths 是否对含 null 值的嵌套路径进行“自动扩展”\n\t * @param autoGrowCollectionLimit 数组与集合自动扩展的上限\n\t */",
        ),
        (
            "\t/**\n\t * Returns the {@link BeanWrapper} that this instance uses.\n\t * Creates a new one if none existed before.\n\t * @see #createBeanWrapper()\n\t */",
            "\t/**\n\t * 返回本实例使用的 {@link BeanWrapper}。\n\t * 若此前不存在则创建新实例。\n\t * @see #createBeanWrapper()\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link BeanWrapper} for the underlying target object.\n\t * @see #getTarget()\n\t */",
            "\t/**\n\t * 为底层目标对象创建新的 {@link BeanWrapper}。\n\t * @see #getTarget()\n\t */",
        ),
    ],
    "BindException.java": [
        (
            "/**\n * Thrown when binding errors are considered fatal. Implements the\n * {@link BindingResult} interface (and its super-interface {@link Errors})\n * to allow for the direct analysis of binding errors.\n *\n * <p>As of Spring 2.0, this is a special-purpose class. Normally,\n * application code will work with the {@link BindingResult} interface,\n * or with a {@link DataBinder} that in turn exposes a BindingResult via\n * {@link org.springframework.validation.DataBinder#getBindingResult()}.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @see BindingResult\n * @see DataBinder#getBindingResult()\n * @see DataBinder#close()\n */",
            "/**\n * 当绑定错误被视为致命时抛出。实现 {@link BindingResult} 接口\n * （及其超接口 {@link Errors}），以便直接分析绑定错误。\n *\n * <p>自 Spring 2.0 起，这是专用类。通常应用代码使用 {@link BindingResult} 接口，\n * 或使用 {@link DataBinder}，后者通过\n * {@link org.springframework.validation.DataBinder#getBindingResult()} 暴露 BindingResult。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @see BindingResult\n * @see DataBinder#getBindingResult()\n * @see DataBinder#close()\n */",
        ),
        (
            "\t/**\n\t * Create a new BindException instance for a BindingResult.\n\t * @param bindingResult the BindingResult instance to wrap\n\t */",
            "\t/**\n\t * 为 BindingResult 创建新的 BindException 实例。\n\t * @param bindingResult 要包装的 BindingResult 实例\n\t */",
        ),
        (
            "\t/**\n\t * Create a new BindException instance for a target bean.\n\t * @param target the target bean to bind onto\n\t * @param objectName the name of the target object\n\t * @see BeanPropertyBindingResult\n\t */",
            "\t/**\n\t * 为目标 bean 创建新的 BindException 实例。\n\t * @param target 要绑定到的目标 bean\n\t * @param objectName 目标对象名称\n\t * @see BeanPropertyBindingResult\n\t */",
        ),
        (
            "\t/**\n\t * Return the BindingResult that this BindException wraps.\n\t */",
            "\t/**\n\t * 返回本 BindException 所包装的 BindingResult。\n\t */",
        ),
        (
            "\t/**\n\t * Returns diagnostic information about the errors held in this object.\n\t */",
            "\t/**\n\t * 返回本对象所持有错误的诊断信息。\n\t */",
        ),
    ],
}
