"""Wave 14b small replacements."""
SMALL_REPLACEMENTS = {
"Validated.java": [
("Variant of JSR-303's {@link jakarta.validation.Valid}, supporting the\n * specification of validation groups. Designed for convenient use with\n * Spring's JSR-303 support but not JSR-303 specific.","JSR-303 {@link jakarta.validation.Valid} 的变体，支持指定校验分组。\n * 便于与 Spring 的 JSR-303 支持配合使用，但并非 JSR-303 专用。"),
("Can be used, for example, with Spring MVC handler methods arguments.\n * Supported through {@link org.springframework.validation.SmartValidator}'s\n * validation hint concept, with validation group classes acting as hint objects.","例如可用于 Spring MVC 处理器方法参数。\n * 通过 {@link org.springframework.validation.SmartValidator} 的校验提示概念支持，\n * 校验分组类作为提示对象。"),
("Can also be used with method level validation, indicating that a specific\n * class is supposed to be validated at the method level (acting as a pointcut\n * for the corresponding validation interceptor), but also optionally specifying\n * the validation groups for method-level validation in the annotated class.\n * Applying this annotation at the method level allows for overriding the\n * validation groups for a specific method but does not serve as a pointcut;\n * a class-level annotation is nevertheless necessary to trigger method validation\n * for a specific bean to begin with. Can also be used as a meta-annotation on a\n * custom stereotype annotation or a custom group-specific validated annotation.","也可用于方法级校验，表示特定类应在方法级进行校验\n *（作为对应校验拦截器的切点），并可选择指定被注解类的方法级校验分组。\n * 在方法级应用本注解可覆盖特定方法的校验分组，但不作为切点；\n * 要触发特定 Bean 的方法校验，仍需要类级注解。\n * 还可用作自定义构造型注解或自定义分组校验注解的元注解。"),
("This annotation may be used as a <em>meta-annotation</em> to create custom\n * <em>composed annotations</em>.","本注解可用作<em>元注解</em>以创建自定义<em>组合注解</em>。"),
("Specify one or more validation groups to apply to the validation step\n\t * kicked off by this annotation.","指定本注解触发的校验步骤所应用的校验分组。"),
("JSR-303 defines validation groups as custom annotations which an application declares\n\t * for the sole purpose of using them as type-safe group arguments, as implemented in\n\t * {@link org.springframework.validation.beanvalidation.SpringValidatorAdapter}.","JSR-303 将校验分组定义为应用声明的自定义注解，\n\t * 专门用作类型安全的分组参数，如 {@link org.springframework.validation.beanvalidation.SpringValidatorAdapter} 中的实现。"),
("Other {@link org.springframework.validation.SmartValidator} implementations may\n\t * support class arguments in other ways as well.","其他 {@link org.springframework.validation.SmartValidator} 实现也可能以其他方式支持类参数。"),
],
"ValidationAnnotationUtils.java": [
("Utility class for handling validation annotations.","处理校验注解的工具类。"),
("Mainly for internal use within the framework.","主要供框架内部使用。"),
("Determine any validation hints for the given annotation.","确定给定注解的校验提示。"),
("This implementation checks for Spring's\n\t * {@link org.springframework.validation.annotation.Validated},\n\t * {@code @jakarta.validation.Valid}, and custom annotations whose\n\t * name starts with \"Valid\" which may optionally declare validation\n\t * hints through the \"value\" attribute.","本实现检查 Spring 的\n\t * {@link org.springframework.validation.annotation.Validated}、\n\t * {@code @jakarta.validation.Valid}，以及名称以 \"Valid\" 开头的自定义注解，\n\t * 后者可通过 \"value\" 属性可选地声明校验提示。"),
("@param ann the annotation (potentially a validation annotation)","@param ann 注解（可能是校验注解）"),
("@return the validation hints to apply (possibly an empty array),\n\t * or {@code null} if this annotation does not trigger any validation","@return 要应用的校验提示（可能为空数组），\n\t * 若本注解不触发任何校验则返回 {@code null}"),
("Determine the applicable validation groups from an\n\t * {@link org.springframework.validation.annotation.Validated @Validated}\n\t * annotation either on the method, or on the containing target class of\n\t * the method, or for an AOP proxy without a target (with all behavior in\n\t * advisors), also check on proxied interfaces.","从方法上的\n\t * {@link org.springframework.validation.annotation.Validated @Validated}\n\t * 注解、方法所属目标类，或对于无目标对象的 AOP 代理（行为全在 advisor 中）\n\t * 还检查被代理接口，确定适用的校验分组。"),
],
"BeanValidationPostProcessor.java": [
("Simple {@link BeanPostProcessor} that checks JSR-303 constraint annotations\n * in Spring-managed beans, throwing an initialization exception in case of\n * constraint violations right before calling the bean's init method (if any).","简单的 {@link BeanPostProcessor}，检查 Spring 托管 Bean 中的 JSR-303 约束注解，\n * 在调用 Bean 的 init 方法（若有）之前，若存在约束违规则抛出初始化异常。"),
("Set the JSR-303 Validator to delegate to for validating beans.\n\t * <p>Default is the default ValidatorFactory's default Validator.","设置用于校验 Bean 的 JSR-303 Validator 委托对象。\n\t * <p>默认为默认 ValidatorFactory 的默认 Validator。"),
("Set the JSR-303 ValidatorFactory to delegate to for validating beans,\n\t * using its default Validator.\n\t * <p>Default is the default ValidatorFactory's default Validator.","设置用于校验 Bean 的 JSR-303 ValidatorFactory 委托对象，\n\t * 使用其默认 Validator。\n\t * <p>默认为默认 ValidatorFactory 的默认 Validator。"),
("Choose whether to perform validation after bean initialization\n\t * (i.e. after init methods) instead of before (which is the default).\n\t * <p>Default is \"false\" (before initialization). Switch this to \"true\"\n\t * (after initialization) if you would like to give init methods a chance\n\t * to populate constrained fields before they get validated.","选择是在 Bean 初始化之后（即 init 方法之后）而非之前（默认）执行校验。\n\t * <p>默认为 \"false\"（初始化之前）。若希望 init 方法有机会\n\t * 在校验前填充受约束字段，可设为 \"true\"（初始化之后）。"),
("Perform validation of the given bean.\n\t * @param bean the bean instance to validate","执行给定 Bean 的校验。\n\t * @param bean 要校验的 Bean 实例"),
],
"CustomValidatorBean.java": [
("Configurable bean class that exposes a specific JSR-303 Validator\n * through its original interface as well as through the Spring\n * {@link org.springframework.validation.Validator} interface.","可配置的 Bean 类，通过原始接口以及 Spring\n * {@link org.springframework.validation.Validator} 接口暴露特定的 JSR-303 Validator。"),
("Set the ValidatorFactory to obtain the target Validator from.\n\t * <p>Default is {@link jakarta.validation.Validation#buildDefaultValidatorFactory()}.","设置用于获取目标 Validator 的 ValidatorFactory。\n\t * <p>默认为 {@link jakarta.validation.Validation#buildDefaultValidatorFactory()}。"),
("Specify a custom MessageInterpolator to use for this Validator.","指定本 Validator 使用的自定义 MessageInterpolator。"),
("Specify a custom TraversableResolver to use for this Validator.","指定本 Validator 使用的自定义 TraversableResolver。"),
],
"LocaleContextMessageInterpolator.java": [
("Delegates to a target {@link MessageInterpolator} implementation but enforces Spring's\n * managed Locale. Typically used to wrap the validation provider's default interpolator.","委托给目标 {@link MessageInterpolator} 实现，但强制使用 Spring 管理的 Locale。\n * 通常用于包装校验提供者的默认插值器。"),
("Create a new LocaleContextMessageInterpolator, wrapping the given target interpolator.\n\t * @param targetInterpolator the target MessageInterpolator to wrap","创建新的 LocaleContextMessageInterpolator，包装给定的目标插值器。\n\t * @param targetInterpolator 要包装的目标 MessageInterpolator"),
],
"MessageSourceResourceBundleLocator.java": [
("Implementation of Hibernate Validator's {@link ResourceBundleLocator} interface,\n * exposing a Spring {@link MessageSource} as localized {@link MessageSourceResourceBundle}.","Hibernate Validator {@link ResourceBundleLocator} 接口的实现，\n * 将 Spring {@link MessageSource} 暴露为本地化的 {@link MessageSourceResourceBundle}。"),
("Build a MessageSourceResourceBundleLocator for the given MessageSource.\n\t * @param messageSource the Spring MessageSource to wrap","为给定 MessageSource 构建 MessageSourceResourceBundleLocator。\n\t * @param messageSource 要包装的 Spring MessageSource"),
],
"OptionalValidatorFactoryBean.java": [
("{@link LocalValidatorFactoryBean} subclass that simply turns\n * {@link org.springframework.validation.Validator} calls into no-ops\n * in case of no Bean Validation provider being available.","{@link LocalValidatorFactoryBean} 子类，在无 Bean Validation 提供者可用时\n * 将 {@link org.springframework.validation.Validator} 调用变为空操作。"),
("This is the actual class used by Spring's MVC configuration namespace,\n * in case of the {@code jakarta.validation} API being present but no explicit\n * Validator having been configured.","当存在 {@code jakarta.validation} API 但未显式配置 Validator 时，\n * 这是 Spring MVC 配置命名空间实际使用的类。"),
],
"SpringConstraintValidatorFactory.java": [
("JSR-303 {@link ConstraintValidatorFactory} implementation that delegates to a\n * Spring BeanFactory for creating autowired {@link ConstraintValidator} instances.","JSR-303 {@link ConstraintValidatorFactory} 实现，\n * 委托 Spring BeanFactory 创建可自动装配的 {@link ConstraintValidator} 实例。"),
("Note that this class is meant for programmatic use, not for declarative use\n * in a standard {@code validation.xml} file. Consider\n * {@link org.springframework.web.bind.support.SpringWebConstraintValidatorFactory}\n * for declarative use in a web application, for example, with JAX-RS or JAX-WS.","注意，本类用于编程式使用，而非标准 {@code validation.xml} 文件中的声明式使用。\n * 在 Web 应用（如 JAX-RS 或 JAX-WS）中声明式使用请考虑\n * {@link org.springframework.web.bind.support.SpringWebConstraintValidatorFactory}。"),
("Create a new SpringConstraintValidatorFactory for the given BeanFactory.\n\t * @param beanFactory the target BeanFactory","为给定 BeanFactory 创建新的 SpringConstraintValidatorFactory。\n\t * @param beanFactory 目标 BeanFactory"),
("Create a new SpringConstraintValidatorFactory for the given BeanFactory.\n\t * @param beanFactory the target BeanFactory\n\t * @param defaultConstraintValidatorFactory the default ConstraintValidatorFactory\n\t * as exposed by the validation provider (for creating provider-internal validator\n\t * implementations which might not be publicly accessible in a module path setup)","为给定 BeanFactory 创建新的 SpringConstraintValidatorFactory。\n\t * @param beanFactory 目标 BeanFactory\n\t * @param defaultConstraintValidatorFactory 校验提供者暴露的默认 ConstraintValidatorFactory\n\t *（用于创建模块路径设置中可能无法公开访问的提供者内部校验器实现）"),
],
"DefaultMethodValidationResult.java": [
("Default {@link MethodValidationResult} implementation as a simple container.","默认 {@link MethodValidationResult} 实现，作为简单容器。"),
],
"EmptyMethodValidationResult.java": [
("{@link MethodValidationResult} with an empty list of results.","结果列表为空的 {@link MethodValidationResult} 实现。\n * 适合作为无校验错误的常量使用；目标对象和方法的 getter 不支持。"),
],
"MethodValidationException.java": [
("Exception that is a {@link MethodValidationResult}.","同时实现 {@link MethodValidationResult} 的运行时异常。\n * 在方法校验失败且未提供替代处理时抛出。"),
("\n\n\tpublic MethodValidationException(MethodValidationResult validationResult) {","\n\n\t/** 使用给定方法校验结果构造异常。 @param validationResult 方法校验结果（必填） */\n\tpublic MethodValidationException(MethodValidationResult validationResult) {"),
],
}
