"""Chinese JavaDoc replacements for springframework wave14a validation binding [4:10]."""

VALIDATION_BINDING_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "BindingErrorProcessor.java": [
        (
            "/**\n * Strategy for processing {@code DataBinder}'s missing field errors,\n * and for translating a {@code PropertyAccessException} to a\n * {@code FieldError}.\n *\n * <p>The error processor is pluggable so you can treat errors differently\n * if you want to. A default implementation is provided for typical needs.\n *\n * <p>Note: As of Spring 2.0, this interface operates on a given BindingResult,\n * to be compatible with any binding strategy (bean property, direct field access, etc).\n * It can still receive a BindException as argument (since a BindException implements\n * the BindingResult interface as well) but no longer operates on it directly.\n *\n * @author Alef Arendsen\n * @author Juergen Hoeller\n * @since 1.2\n * @see DataBinder#setBindingErrorProcessor\n * @see DefaultBindingErrorProcessor\n * @see BindingResult\n * @see BindException\n */",
            "/**\n * 处理 {@code DataBinder} 缺失字段错误，并将 {@code PropertyAccessException}\n * 翻译为 {@code FieldError} 的策略接口。\n *\n * <p>错误处理器可插拔，可按需定制错误处理方式；\n * 典型场景下提供了默认实现。\n *\n * <p>注意：自 Spring 2.0 起，本接口基于给定 BindingResult 操作，\n * 以兼容任意绑定策略（bean 属性、直接字段访问等）。\n * 仍可接收 BindException 参数（因 BindException 也实现 BindingResult），\n * 但不再直接在其上操作。\n *\n * @author Alef Arendsen\n * @author Juergen Hoeller\n * @since 1.2\n * @see DataBinder#setBindingErrorProcessor\n * @see DefaultBindingErrorProcessor\n * @see BindingResult\n * @see BindException\n */",
        ),
        (
            "\t/**\n\t * Apply the missing field error to the given BindException.\n\t * <p>Usually, a field error is created for a missing required field.\n\t * @param missingField the field that was missing during binding\n\t * @param bindingResult the errors object to add the error(s) to.\n\t * You can add more than just one error or maybe even ignore it.\n\t * The {@code BindingResult} object features convenience utils such as\n\t * a {@code resolveMessageCodes} method to resolve an error code.\n\t * @see BeanPropertyBindingResult#addError\n\t * @see BeanPropertyBindingResult#resolveMessageCodes\n\t */",
            "\t/**\n\t * 将缺失字段错误应用到给定 BindException。\n\t * <p>通常为缺失的必填字段创建字段错误。\n\t * @param missingField 绑定过程中缺失的字段\n\t * @param bindingResult 要添加错误的 Errors 对象。\n\t * 可添加多个错误，甚至忽略该错误。\n\t * {@code BindingResult} 提供 {@code resolveMessageCodes} 等便捷工具解析错误码。\n\t * @see BeanPropertyBindingResult#addError\n\t * @see BeanPropertyBindingResult#resolveMessageCodes\n\t */",
        ),
        (
            "\t/**\n\t * Translate the given {@code PropertyAccessException} to an appropriate\n\t * error registered on the given {@code Errors} instance.\n\t * <p>Note that two error types are available: {@code FieldError} and\n\t * {@code ObjectError}. Usually, field errors are created, but in certain\n\t * situations one might want to create a global {@code ObjectError} instead.\n\t * @param ex the {@code PropertyAccessException} to translate\n\t * @param bindingResult the errors object to add the error(s) to.\n\t * You can add more than just one error or maybe even ignore it.\n\t * The {@code BindingResult} object features convenience utils such as\n\t * a {@code resolveMessageCodes} method to resolve an error code.\n\t * @see Errors\n\t * @see FieldError\n\t * @see ObjectError\n\t * @see MessageCodesResolver\n\t * @see BeanPropertyBindingResult#addError\n\t * @see BeanPropertyBindingResult#resolveMessageCodes\n\t */",
            "\t/**\n\t * 将给定 {@code PropertyAccessException} 翻译为注册到给定 {@code Errors} 实例的适当错误。\n\t * <p>注意：可用错误类型有 {@code FieldError} 与 {@code ObjectError}。\n\t * 通常创建字段错误，但某些情况下可能希望创建全局 {@code ObjectError}。\n\t * @param ex 要翻译的 {@code PropertyAccessException}\n\t * @param bindingResult 要添加错误的 Errors 对象。\n\t * 可添加多个错误，甚至忽略该错误。\n\t * {@code BindingResult} 提供 {@code resolveMessageCodes} 等便捷工具解析错误码。\n\t * @see Errors\n\t * @see FieldError\n\t * @see ObjectError\n\t * @see MessageCodesResolver\n\t * @see BeanPropertyBindingResult#addError\n\t * @see BeanPropertyBindingResult#resolveMessageCodes\n\t */",
        ),
    ],
    "BindingResult.java": [
        (
            "/**\n * General interface that represents binding results. Extends the\n * {@link Errors} interface for error registration capabilities,\n * allowing for a {@link Validator} to be applied, and adds\n * binding-specific analysis and model building.\n *\n * <p>Serves as result holder for a {@link DataBinder}, obtained via\n * the {@link DataBinder#getBindingResult()} method. BindingResult\n * implementations can also be used directly, for example to invoke\n * a {@link Validator} on it (for example, as part of a unit test).\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see DataBinder\n * @see Errors\n * @see Validator\n * @see BeanPropertyBindingResult\n * @see DirectFieldBindingResult\n * @see MapBindingResult\n */",
            "/**\n * 表示绑定结果的通用接口。扩展 {@link Errors} 接口以支持错误注册，\n * 便于应用 {@link Validator}，并增加绑定相关的分析与模型构建能力。\n *\n * <p>作为 {@link DataBinder} 的结果持有者，\n * 通过 {@link DataBinder#getBindingResult()} 获取。\n * BindingResult 实现也可直接使用，例如对其调用 {@link Validator}（如单元测试中）。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see DataBinder\n * @see Errors\n * @see Validator\n * @see BeanPropertyBindingResult\n * @see DirectFieldBindingResult\n * @see MapBindingResult\n */",
        ),
        (
            "\t/**\n\t * Prefix for the name of the BindingResult instance in a model,\n\t * followed by the object name.\n\t */",
            "\t/**\n\t * 模型中 BindingResult 实例名称的前缀，后接对象名。\n\t */",
        ),
        (
            "\t/**\n\t * Return the wrapped target object, which may be a bean, an object with\n\t * public fields, a Map - depending on the concrete binding strategy.\n\t */",
            "\t/**\n\t * 返回被包装的目标对象，可能是 bean、含 public 字段的对象或 Map，\n\t * 取决于具体绑定策略。\n\t */",
        ),
        (
            "\t/**\n\t * Return a model Map for the obtained state, exposing a BindingResult\n\t * instance as '{@link #MODEL_KEY_PREFIX MODEL_KEY_PREFIX} + objectName'\n\t * and the object itself as 'objectName'.\n\t * <p>Note that the Map is constructed every time you're calling this method.\n\t * Adding things to the map and then re-calling this method will not work.\n\t * <p>The attributes in the model Map returned by this method are usually\n\t * included in the {@link org.springframework.web.servlet.ModelAndView}\n\t * for a form view that uses Spring's {@code bind} tag in a JSP,\n\t * which needs access to the BindingResult instance. Spring's pre-built\n\t * form controllers will do this for you when rendering a form view.\n\t * When building the ModelAndView instance yourself, you need to include\n\t * the attributes from the model Map returned by this method.\n\t * @see #getObjectName()\n\t * @see #MODEL_KEY_PREFIX\n\t * @see org.springframework.web.servlet.ModelAndView\n\t * @see org.springframework.web.servlet.tags.BindTag\n\t */",
            "\t/**\n\t * 返回当前状态的 model Map，以\n\t * '{@link #MODEL_KEY_PREFIX MODEL_KEY_PREFIX} + objectName' 暴露 BindingResult 实例，\n\t * 以 'objectName' 暴露对象本身。\n\t * <p>注意：每次调用本方法都会重新构造 Map；\n\t * 向 Map 添加内容后再调用本方法不会生效。\n\t * <p>本方法返回的 model Map 属性通常包含在\n\t * {@link org.springframework.web.servlet.ModelAndView} 中，\n\t * 供 JSP 中使用 Spring {@code bind} 标签的表单视图访问 BindingResult。\n\t * Spring 预置表单控制器在渲染表单视图时会自动处理；\n\t * 自行构建 ModelAndView 时需包含本方法返回 model Map 中的属性。\n\t * @see #getObjectName()\n\t * @see #MODEL_KEY_PREFIX\n\t * @see org.springframework.web.servlet.ModelAndView\n\t * @see org.springframework.web.servlet.tags.BindTag\n\t */",
        ),
        (
            "\t/**\n\t * Extract the raw field value for the given field.\n\t * Typically used for comparison purposes.\n\t * @param field the field to check\n\t * @return the current value of the field in its raw form, or {@code null} if not known\n\t */",
            "\t/**\n\t * 提取给定字段的原始字段值，通常用于比较。\n\t * @param field 要检查的字段\n\t * @return 字段当前原始值，未知时返回 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Find a custom property editor for the given type and property.\n\t * @param field the path of the property (name or nested path), or\n\t * {@code null} if looking for an editor for all properties of the given type\n\t * @param valueType the type of the property (can be {@code null} if a property\n\t * is given but should be specified in any case for consistency checking)\n\t * @return the registered editor, or {@code null} if none\n\t */",
            "\t/**\n\t * 查找给定类型与属性的自定义属性编辑器。\n\t * @param field 属性路径（名称或嵌套路径），\n\t * 若查找给定类型所有属性的编辑器则为 {@code null}\n\t * @param valueType 属性类型（若已给定属性可为 {@code null}，\n\t * 但为一致性检查仍应指定）\n\t * @return 已注册的编辑器，无则返回 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Return the underlying PropertyEditorRegistry.\n\t * @return the PropertyEditorRegistry, or {@code null} if none\n\t * available for this BindingResult\n\t */",
            "\t/**\n\t * 返回底层 PropertyEditorRegistry。\n\t * @return PropertyEditorRegistry，本 BindingResult 无可用实例时返回 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Resolve the given error code into message codes.\n\t * <p>Calls the configured {@link MessageCodesResolver} with appropriate parameters.\n\t * @param errorCode the error code to resolve into message codes\n\t * @return the resolved message codes\n\t */",
            "\t/**\n\t * 将给定错误码解析为消息码。\n\t * <p>以适当参数调用已配置的 {@link MessageCodesResolver}。\n\t * @param errorCode 要解析为消息码的错误码\n\t * @return 解析后的消息码\n\t */",
        ),
        (
            "\t/**\n\t * Resolve the given error code into message codes for the given field.\n\t * <p>Calls the configured {@link MessageCodesResolver} with appropriate parameters.\n\t * @param errorCode the error code to resolve into message codes\n\t * @param field the field to resolve message codes for\n\t * @return the resolved message codes\n\t */",
            "\t/**\n\t * 将给定错误码解析为指定字段的消息码。\n\t * <p>以适当参数调用已配置的 {@link MessageCodesResolver}。\n\t * @param errorCode 要解析为消息码的错误码\n\t * @param field 要解析消息码的字段\n\t * @return 解析后的消息码\n\t */",
        ),
        (
            "\t/**\n\t * Add a custom {@link ObjectError} or {@link FieldError} to the errors list.\n\t * <p>Intended to be used by cooperating strategies such as {@link BindingErrorProcessor}.\n\t * @see ObjectError\n\t * @see FieldError\n\t * @see BindingErrorProcessor\n\t */",
            "\t/**\n\t * 向错误列表添加自定义 {@link ObjectError} 或 {@link FieldError}。\n\t * <p>供 {@link BindingErrorProcessor} 等协作策略使用。\n\t * @see ObjectError\n\t * @see FieldError\n\t * @see BindingErrorProcessor\n\t */",
        ),
        (
            "\t/**\n\t * Record the given value for the specified field.\n\t * <p>To be used when a target object cannot be constructed, making\n\t * the original field values available through {@link #getFieldValue}.\n\t * In case of a registered error, the rejected value will be exposed\n\t * for each affected field.\n\t * @param field the field to record the value for\n\t * @param type the type of the field\n\t * @param value the original value\n\t * @since 5.0.4\n\t */",
            "\t/**\n\t * 记录指定字段的给定值。\n\t * <p>在无法构造目标对象时使用，使原始字段值可通过 {@link #getFieldValue} 获取。\n\t * 若已注册错误，每个受影响字段将暴露被拒绝的值。\n\t * @param field 要记录值的字段\n\t * @param type 字段类型\n\t * @param value 原始值\n\t * @since 5.0.4\n\t */",
        ),
        (
            "\t/**\n\t * Mark the specified disallowed field as suppressed.\n\t * <p>The data binder invokes this for each field value that was\n\t * detected to target a disallowed field.\n\t * @see DataBinder#setAllowedFields\n\t */",
            "\t/**\n\t * 将指定的不允许字段标记为已抑制。\n\t * <p>数据绑定器对每个检测到指向不允许字段的字段值调用本方法。\n\t * @see DataBinder#setAllowedFields\n\t */",
        ),
        (
            "\t/**\n\t * Return the list of fields that were suppressed during the bind process.\n\t * <p>Can be used to determine whether any field values were targeting\n\t * disallowed fields.\n\t * @see DataBinder#setAllowedFields\n\t */",
            "\t/**\n\t * 返回绑定过程中被抑制的字段列表。\n\t * <p>可用于判断是否有字段值指向了不允许的字段。\n\t * @see DataBinder#setAllowedFields\n\t */",
        ),
    ],
    "BindingResultUtils.java": [
        (
            "/**\n * Convenience methods for looking up BindingResults in a model Map.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see BindingResult#MODEL_KEY_PREFIX\n */",
            "/**\n * 在 model Map 中查找 BindingResult 的便捷方法。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see BindingResult#MODEL_KEY_PREFIX\n */",
        ),
        (
            "\t/**\n\t * Find the BindingResult for the given name in the given model.\n\t * @param model the model to search\n\t * @param name the name of the target object to find a BindingResult for\n\t * @return the BindingResult, or {@code null} if none found\n\t * @throws IllegalStateException if the attribute found is not of type BindingResult\n\t */",
            "\t/**\n\t * 在给定 model 中查找指定名称的 BindingResult。\n\t * @param model 要搜索的 model\n\t * @param name 要查找 BindingResult 的目标对象名称\n\t * @return BindingResult，未找到时返回 {@code null}\n\t * @throws IllegalStateException 若找到的属性不是 BindingResult 类型\n\t */",
        ),
        (
            "\t/**\n\t * Find a required BindingResult for the given name in the given model.\n\t * @param model the model to search\n\t * @param name the name of the target object to find a BindingResult for\n\t * @return the BindingResult (never {@code null})\n\t * @throws IllegalStateException if no BindingResult found\n\t */",
            "\t/**\n\t * 在给定 model 中查找必需的 BindingResult。\n\t * @param model 要搜索的 model\n\t * @param name 要查找 BindingResult 的目标对象名称\n\t * @return BindingResult（永不为 {@code null}）\n\t * @throws IllegalStateException 若未找到 BindingResult\n\t */",
        ),
    ],
    "DefaultBindingErrorProcessor.java": [
        (
            "/**\n * Default {@link BindingErrorProcessor} implementation.\n *\n * <p>Uses the \"required\" error code and the field name to resolve message codes\n * for a missing field error.\n *\n * <p>Creates a {@code FieldError} for each {@code PropertyAccessException}\n * given, using the {@code PropertyAccessException}'s error code (\"typeMismatch\",\n * \"methodInvocation\") for resolving message codes.\n *\n * @author Alef Arendsen\n * @author Juergen Hoeller\n * @since 1.2\n * @see #MISSING_FIELD_ERROR_CODE\n * @see DataBinder#setBindingErrorProcessor\n * @see BeanPropertyBindingResult#addError\n * @see BeanPropertyBindingResult#resolveMessageCodes\n * @see org.springframework.beans.PropertyAccessException#getErrorCode\n * @see org.springframework.beans.TypeMismatchException#ERROR_CODE\n * @see org.springframework.beans.MethodInvocationException#ERROR_CODE\n */",
            "/**\n * 默认 {@link BindingErrorProcessor} 实现。\n *\n * <p>对缺失字段错误，使用 \"required\" 错误码与字段名解析消息码。\n *\n * <p>对每个 {@code PropertyAccessException} 创建 {@code FieldError}，\n * 使用异常的 errorCode（\"typeMismatch\"、\"methodInvocation\"）解析消息码。\n *\n * @author Alef Arendsen\n * @author Juergen Hoeller\n * @since 1.2\n * @see #MISSING_FIELD_ERROR_CODE\n * @see DataBinder#setBindingErrorProcessor\n * @see BeanPropertyBindingResult#addError\n * @see BeanPropertyBindingResult#resolveMessageCodes\n * @see org.springframework.beans.PropertyAccessException#getErrorCode\n * @see org.springframework.beans.TypeMismatchException#ERROR_CODE\n * @see org.springframework.beans.MethodInvocationException#ERROR_CODE\n */",
        ),
        (
            "\t/**\n\t * Error code that a missing field error (i.e. a required field not\n\t * found in the list of property values) will be registered with:\n\t * \"required\".\n\t */",
            "\t/**\n\t * 缺失字段错误（即必填字段未出现在属性值列表中）注册时使用的错误码：\"required\"。\n\t */",
        ),
        (
            "\t/**\n\t * Return FieldError arguments for a binding error on the given field.\n\t * Invoked for each missing required field and each type mismatch.\n\t * <p>The default implementation returns a single argument indicating the field name\n\t * (of type DefaultMessageSourceResolvable, with \"objectName.field\" and \"field\" as codes).\n\t * @param objectName the name of the target object\n\t * @param field the field that caused the binding error\n\t * @return the Object array that represents the FieldError arguments\n\t * @see org.springframework.validation.FieldError#getArguments\n\t * @see org.springframework.context.support.DefaultMessageSourceResolvable\n\t */",
            "\t/**\n\t * 返回给定字段绑定错误的 FieldError 参数。\n\t * 对每个缺失必填字段与类型不匹配都会调用。\n\t * <p>默认实现返回单个参数表示字段名\n\t * （类型为 DefaultMessageSourceResolvable，codes 为 \"objectName.field\" 与 \"field\"）。\n\t * @param objectName 目标对象名称\n\t * @param field 导致绑定错误的字段\n\t * @return 表示 FieldError 参数的 Object 数组\n\t * @see org.springframework.validation.FieldError#getArguments\n\t * @see org.springframework.context.support.DefaultMessageSourceResolvable\n\t */",
        ),
        (
            "\t/**\n\t * Subclass of {@code FieldError} with Spring-style default message rendering.\n\t */",
            "\t/**\n\t * 采用 Spring 风格默认消息渲染的 {@code FieldError} 子类。\n\t */",
        ),
    ],
    "DirectFieldBindingResult.java": [
        (
            "/**\n * Special implementation of the Errors and BindingResult interfaces,\n * supporting registration and evaluation of binding errors on value objects.\n * Performs direct field access instead of going through JavaBean getters.\n *\n * <p>Since Spring 4.1 this implementation is able to traverse nested fields.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see DataBinder#getBindingResult()\n * @see DataBinder#initDirectFieldAccess()\n * @see BeanPropertyBindingResult\n */",
            "/**\n * Errors 与 BindingResult 接口的特殊实现，\n * 支持在值对象上注册并评估绑定错误。\n * 直接访问字段，而非通过 JavaBean getter。\n *\n * <p>自 Spring 4.1 起，本实现可遍历嵌套字段。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see DataBinder#getBindingResult()\n * @see DataBinder#initDirectFieldAccess()\n * @see BeanPropertyBindingResult\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code DirectFieldBindingResult} for the given target.\n\t * @param target the target object to bind onto\n\t * @param objectName the name of the target object\n\t */",
            "\t/**\n\t * 为给定目标创建新的 {@code DirectFieldBindingResult}。\n\t * @param target 要绑定到的目标对象\n\t * @param objectName 目标对象名称\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code DirectFieldBindingResult} for the given target.\n\t * @param target the target object to bind onto\n\t * @param objectName the name of the target object\n\t * @param autoGrowNestedPaths whether to \"auto-grow\" a nested path that contains a null value\n\t */",
            "\t/**\n\t * 为给定目标创建新的 {@code DirectFieldBindingResult}。\n\t * @param target 要绑定到的目标对象\n\t * @param objectName 目标对象名称\n\t * @param autoGrowNestedPaths 是否对含 null 值的嵌套路径进行“自动扩展”\n\t */",
        ),
        (
            "\t/**\n\t * Returns the DirectFieldAccessor that this instance uses.\n\t * Creates a new one if none existed before.\n\t * @see #createDirectFieldAccessor()\n\t */",
            "\t/**\n\t * 返回本实例使用的 DirectFieldAccessor。\n\t * 若此前不存在则创建新实例。\n\t * @see #createDirectFieldAccessor()\n\t */",
        ),
        (
            "\t/**\n\t * Create a new DirectFieldAccessor for the underlying target object.\n\t * @see #getTarget()\n\t */",
            "\t/**\n\t * 为底层目标对象创建新的 DirectFieldAccessor。\n\t * @see #getTarget()\n\t */",
        ),
    ],
}
