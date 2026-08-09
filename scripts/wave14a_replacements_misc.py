"""Chinese JavaDoc replacements for Spring Boot wave14a misc (threading, validation, reactive)."""

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "Threading.java": [
        (
            "/**\n * Threading of the application.\n *\n * @author Moritz Halbritter\n * @since 3.2.0\n */",
            "/**\n * 应用线程模型枚举。\n * 用于判断当前应使用平台线程还是虚拟线程。\n *\n * @author Moritz Halbritter\n * @since 3.2.0\n */",
        ),
        (
            "\t/**\n\t * Platform threads. Active if virtual threads are not active.\n\t */",
            "\t/**\n\t * 平台线程。虚拟线程未激活时生效。\n\t */",
        ),
        (
            "\t/**\n\t * Virtual threads. Active if {@code spring.threads.virtual.enabled} is {@code true}\n\t * and running on Java 21 or later.\n\t */",
            "\t/**\n\t * 虚拟线程。当 {@code spring.threads.virtual.enabled} 为 {@code true}\n\t * 且运行于 Java 21 及以上时生效。\n\t */",
        ),
        (
            "\t/**\n\t * Determines whether the threading is active.\n\t * @param environment the environment\n\t * @return whether the threading is active\n\t */",
            "\t/**\n\t * 判断当前线程模型是否处于活动状态。\n\t *\n\t * @param environment the environment 环境\n\t * @return whether the threading is active 是否活动\n\t */",
        ),
    ],
    "Instantiator.java": [
        (
            "/**\n * Simple factory used to instantiate objects by injecting available parameters.\n *\n * @param <T> the type to instantiate\n * @author Phillip Webb\n * @author Scott Frederick\n * @since 2.4.0\n */",
            "/**\n * 通过注入可用构造参数实例化对象的简单工厂。\n * 按参数数量从多到少尝试构造函数，支持类名或 {@link Class} 列表批量实例化。\n *\n * @param <T> the type to instantiate 要实例化的类型\n * @author Phillip Webb\n * @author Scott Frederick\n * @since 2.4.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link Instantiator} instance for the given type.\n\t * @param type the type to instantiate\n\t * @param availableParameters consumer used to register available parameters\n\t */",
            "\t/**\n\t * 为给定类型创建新的 {@link Instantiator} 实例。\n\t *\n\t * @param type the type to instantiate 要实例化的类型\n\t * @param availableParameters consumer used to register available parameters 注册可用参数的回调\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link Instantiator} instance for the given type.\n\t * @param type the type to instantiate\n\t * @param availableParameters consumer used to register available parameters\n\t * @param failureHandler a {@link FailureHandler} that will be called in case of\n\t * failure when instantiating objects\n\t * @since 2.7.0\n\t */",
            "\t/**\n\t * 为给定类型创建新的 {@link Instantiator} 实例。\n\t *\n\t * @param type the type to instantiate 要实例化的类型\n\t * @param availableParameters consumer used to register available parameters 注册可用参数的回调\n\t * @param failureHandler a {@link FailureHandler} that will be called in case of\n\t * failure when instantiating objects 实例化失败时调用的处理器\n\t * @since 2.7.0\n\t */",
        ),
        (
            "\t/**\n\t * Instantiate the given set of class name, injecting constructor arguments as\n\t * necessary.\n\t * @param names the class names to instantiate\n\t * @return a list of instantiated instances\n\t */",
            "\t/**\n\t * 实例化给定类名集合，必要时注入构造参数。\n\t *\n\t * @param names the class names to instantiate 要实例化的类名\n\t * @return a list of instantiated instances 实例列表\n\t */",
        ),
        (
            "\t/**\n\t * Instantiate the given set of class name, injecting constructor arguments as\n\t * necessary.\n\t * @param classLoader the source classloader\n\t * @param names the class names to instantiate\n\t * @return a list of instantiated instances\n\t * @since 2.4.8\n\t */",
            "\t/**\n\t * 实例化给定类名集合，必要时注入构造参数。\n\t *\n\t * @param classLoader the source classloader 源类加载器\n\t * @param names the class names to instantiate 要实例化的类名\n\t * @return a list of instantiated instances 实例列表\n\t * @since 2.4.8\n\t */",
        ),
        (
            "\t/**\n\t * Instantiate the given set of class name, injecting constructor arguments as\n\t * necessary.\n\t * @param name the class name to instantiate\n\t * @return an instantiated instance\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 实例化给定类名，必要时注入构造参数。\n\t *\n\t * @param name the class name to instantiate 要实例化的类名\n\t * @return an instantiated instance 实例\n\t * @since 3.4.0\n\t */",
        ),
        (
            "\t/**\n\t * Instantiate the given set of class name, injecting constructor arguments as\n\t * necessary.\n\t * @param classLoader the source classloader\n\t * @param name the class name to instantiate\n\t * @return an instantiated instance\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 实例化给定类名，必要时注入构造参数。\n\t *\n\t * @param classLoader the source classloader 源类加载器\n\t * @param name the class name to instantiate 要实例化的类名\n\t * @return an instantiated instance 实例\n\t * @since 3.4.0\n\t */",
        ),
        (
            "\t/**\n\t * Instantiate the given class, injecting constructor arguments as necessary.\n\t * @param type the type to instantiate\n\t * @return an instantiated instance\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 实例化给定类，必要时注入构造参数。\n\t *\n\t * @param type the type to instantiate 要实例化的类型\n\t * @return an instantiated instance 实例\n\t * @since 3.4.0\n\t */",
        ),
        (
            "\t/**\n\t * Instantiate the given set of classes, injecting constructor arguments as necessary.\n\t * @param types the types to instantiate\n\t * @return a list of instantiated instances\n\t * @since 2.4.8\n\t */",
            "\t/**\n\t * 实例化给定类集合，必要时注入构造参数。\n\t *\n\t * @param types the types to instantiate 要实例化的类型\n\t * @return a list of instantiated instances 实例列表\n\t * @since 2.4.8\n\t */",
        ),
        (
            "\t/**\n\t * Get an injectable argument instance for the given type. This method can be used\n\t * when manually instantiating an object without reflection.\n\t * @param <A> the argument type\n\t * @param type the argument type\n\t * @return the argument to inject or {@code null}\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 获取给定类型的可注入参数实例。可在不使用反射的手动实例化场景中使用。\n\t *\n\t * @param <A> the argument type 参数类型\n\t * @param type the argument type 参数类型\n\t * @return the argument to inject or {@code null} 要注入的参数或 {@code null}\n\t * @since 3.4.0\n\t */",
        ),
        (
            "\t/**\n\t * Callback used to register available parameters.\n\t */",
            "\t/**\n\t * 用于注册可用构造参数的回调接口。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Add a parameter with an instance value.\n\t\t * @param type the parameter type\n\t\t * @param instance the instance that should be injected\n\t\t */",
            "\t\t/**\n\t\t * 添加带实例值的参数。\n\t\t *\n\t\t * @param type the parameter type 参数类型\n\t\t * @param instance the instance that should be injected 要注入的实例\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Add a parameter with an instance factory.\n\t\t * @param type the parameter type\n\t\t * @param factory the factory used to create the instance that should be injected\n\t\t */",
            "\t\t/**\n\t\t * 添加工厂方式提供的参数。\n\t\t *\n\t\t * @param type the parameter type 参数类型\n\t\t * @param factory the factory used to create the instance that should be injected 创建实例的工厂\n\t\t */",
        ),
        (
            "\t/**\n\t * {@link Supplier} that provides a class type.\n\t */",
            "\t/**\n\t * 提供 {@link Class} 类型的 {@link Supplier}。\n\t */",
        ),
        (
            "\t/**\n\t * Strategy for handling a failure that occurs when instantiating a type.\n\t *\n\t * @since 2.7.0\n\t */",
            "\t/**\n\t * 实例化类型失败时的处理策略。\n\t *\n\t * @since 2.7.0\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Handle the {@code failure} that occurred when instantiating the {@code type}\n\t\t * that was expected to be of the given {@code typeSupplier}.\n\t\t * @param type the type\n\t\t * @param implementationName the name of the implementation type\n\t\t * @param failure the failure that occurred\n\t\t */",
            "\t\t/**\n\t\t * 处理实例化 {@code type} 时发生的 {@code failure}。\n\t\t *\n\t\t * @param type the type 目标类型\n\t\t * @param implementationName the name of the implementation type 实现类名\n\t\t * @param failure the failure that occurred 发生的异常\n\t\t */",
        ),
    ],
    "MessageInterpolatorFactory.java": [
        (
            "/**\n * {@link ObjectFactory} that can be used to create a {@link MessageInterpolator}.\n * Attempts to pick the most appropriate {@link MessageInterpolator} based on the\n * classpath.\n *\n * @author Phillip Webb\n * @since 1.5.0\n */",
            "/**\n * 用于创建 {@link MessageInterpolator} 的 {@link ObjectFactory}。\n * 根据类路径选择最合适的 {@link MessageInterpolator}，失败时尝试 Hibernate Validator 回退实现。\n *\n * @author Phillip Webb\n * @since 1.5.0\n */",
        ),
        (
            "\t/**\n\t * Creates a new {@link MessageInterpolatorFactory} that will produce a\n\t * {@link MessageInterpolator} that uses the given {@code messageSource} to resolve\n\t * any message parameters before final interpolation.\n\t * @param messageSource message source to be used by the interpolator\n\t * @since 2.6.0\n\t */",
            "\t/**\n\t * 创建新的 {@link MessageInterpolatorFactory}，\n\t * 生成的 {@link MessageInterpolator} 会在最终插值前通过 {@code messageSource} 解析消息参数。\n\t *\n\t * @param messageSource message source to be used by the interpolator 插值器使用的消息源\n\t * @since 2.6.0\n\t */",
        ),
        (
            "\t\t\t\t// Swallow and continue\n",
            "\t\t\t\t// 吞掉异常并继续\n",
        ),
    ],
    "MessageSourceMessageInterpolator.java": [
        (
            "/**\n * Resolves any message parameters through {@link MessageSource} and then interpolates a\n * message using the underlying {@link MessageInterpolator}.\n *\n * @author Dmytro Nosan\n * @author Scott Frederick\n */",
            "/**\n * 通过 {@link MessageSource} 解析消息参数，再使用底层 {@link MessageInterpolator} 插值。\n * 支持嵌套 {@code {}} 参数替换与转义，并检测循环引用。\n *\n * @author Dmytro Nosan\n * @author Scott Frederick\n */",
        ),
        (
            "\t/**\n\t * Recursively replaces all message parameters.\n\t * <p>\n\t * The message parameter prefix <code>&#123;</code> and suffix <code>&#125;</code> can\n\t * be escaped using {@code \\}, e.g. <code>\\&#123;escaped\\&#125;</code>.\n\t * @param message the message containing the parameters to be replaced\n\t * @param locale the locale to use when resolving replacements\n\t * @return the message with parameters replaced\n\t */",
            "\t/**\n\t * 递归替换全部消息参数。\n\t * <p>\n\t * 参数前缀 <code>&#123;</code> 与后缀 <code>&#125;</code> 可用 {@code \\} 转义，\n\t * 例如 <code>\\&#123;escaped\\&#125;</code>。\n\t *\n\t * @param message the message containing the parameters to be replaced 含待替换参数的消息\n\t * @param locale the locale to use when resolving replacements 解析替换时使用的区域\n\t * @return the message with parameters replaced 参数已替换的消息\n\t */",
        ),
    ],
    "FilteredMethodValidationPostProcessor.java": [
        (
            "/**\n * Custom {@link MethodValidationPostProcessor} that applies\n * {@link MethodValidationExcludeFilter exclusion filters}.\n *\n * @author Andy Wilkinson\n * @since 2.4.0\n */",
            "/**\n * 应用 {@link MethodValidationExcludeFilter exclusion filters} 的\n * 自定义 {@link MethodValidationPostProcessor}。\n * 在识别可参与方法校验后处理的 Bean 时排除指定类型。\n *\n * @author Andy Wilkinson\n * @since 2.4.0\n */",
        ),
        (
            "\t/**\n\t * Creates a new {@code FilteredMethodValidationPostProcessor} that will apply the\n\t * given {@code excludeFilters} when identifying beans that are eligible for method\n\t * validation post-processing.\n\t * @param excludeFilters filters to apply\n\t */",
            "\t/**\n\t * 创建新的 {@code FilteredMethodValidationPostProcessor}，\n\t * 在识别可参与方法校验后处理的 Bean 时应用给定 {@code excludeFilters}。\n\t *\n\t * @param excludeFilters filters to apply 要应用的排除过滤器\n\t */",
        ),
        (
            "\t/**\n\t * Creates a new {@code FilteredMethodValidationPostProcessor} that will apply the\n\t * given {@code excludeFilters} when identifying beans that are eligible for method\n\t * validation post-processing.\n\t * @param excludeFilters filters to apply\n\t */",
            "\t/**\n\t * 创建新的 {@code FilteredMethodValidationPostProcessor}，\n\t * 在识别可参与方法校验后处理的 Bean 时应用给定 {@code excludeFilters}。\n\t *\n\t * @param excludeFilters filters to apply 要应用的排除过滤器\n\t */",
        ),
    ],
    "MethodValidationExcludeFilter.java": [
        (
            "/**\n * A filter for excluding types from method validation.\n *\n * @author Andy Wilkinson\n * @since 2.4.0\n * @see FilteredMethodValidationPostProcessor\n */",
            "/**\n * 从方法校验中排除类型的过滤器。\n *\n * @author Andy Wilkinson\n * @since 2.4.0\n * @see FilteredMethodValidationPostProcessor\n */",
        ),
        (
            "\t/**\n\t * Evaluate whether to exclude the given {@code type} from method validation.\n\t * @param type the type to evaluate\n\t * @return {@code true} to exclude the type from method validation, otherwise\n\t * {@code false}.\n\t */",
            "\t/**\n\t * 判断是否将给定 {@code type} 从方法校验中排除。\n\t *\n\t * @param type the type to evaluate 待评估的类型\n\t * @return {@code true} to exclude the type from method validation, otherwise\n\t * {@code false}. 排除则为 {@code true}，否则 {@code false}\n\t */",
        ),
        (
            "\t/**\n\t * Factory method to create a {@link MethodValidationExcludeFilter} that excludes\n\t * classes by annotation found using an {@link SearchStrategy#INHERITED_ANNOTATIONS\n\t * inherited annotations search strategy}.\n\t * @param annotationType the annotation to check\n\t * @return a {@link MethodValidationExcludeFilter} instance\n\t */",
            "\t/**\n\t * 工厂方法：创建按注解排除类的 {@link MethodValidationExcludeFilter}，\n\t * 使用 {@link SearchStrategy#INHERITED_ANNOTATIONS inherited annotations search strategy} 查找注解。\n\t *\n\t * @param annotationType the annotation to check 要检查的注解类型\n\t * @return a {@link MethodValidationExcludeFilter} instance 过滤器实例\n\t */",
        ),
        (
            "\t/**\n\t * Factory method to create a {@link MethodValidationExcludeFilter} that excludes\n\t * classes by annotation found using the given search strategy.\n\t * @param annotationType the annotation to check\n\t * @param searchStrategy the annotation search strategy\n\t * @return a {@link MethodValidationExcludeFilter} instance\n\t */",
            "\t/**\n\t * 工厂方法：创建按注解排除类的 {@link MethodValidationExcludeFilter}，\n\t * 使用给定搜索策略查找注解。\n\t *\n\t * @param annotationType the annotation to check 要检查的注解类型\n\t * @param searchStrategy the annotation search strategy 注解搜索策略\n\t * @return a {@link MethodValidationExcludeFilter} instance 过滤器实例\n\t */",
        ),
    ],
    "AnnotationConfigReactiveWebApplicationContext.java": [
        (
            "/**\n * {@link ConfigurableReactiveWebApplicationContext} that accepts annotated classes as\n * input - in particular {@link Configuration @Configuration}-annotated classes, but also\n * plain {@link Component @Component} classes and JSR-330 compliant classes using\n * {@code javax.inject} annotations. Allows for registering classes one by one (specifying\n * class names as config location) as well as for classpath scanning (specifying base\n * packages as config location).\n * <p>\n * Note: In case of multiple {@code @Configuration} classes, later {@code @Bean}\n * definitions will override ones defined in earlier loaded files. This can be leveraged\n * to deliberately override certain bean definitions through an extra Configuration class.\n *\n * @author Phillip Webb\n * @author Stephane Nicoll\n * @since 2.0.0\n * @see AnnotationConfigApplicationContext\n */",
            "/**\n * 接受注解类作为输入的 {@link ConfigurableReactiveWebApplicationContext} 实现。\n * 支持 {@link Configuration @Configuration}、{@link Component @Component}\n * 以及 JSR-330 {@code javax.inject} 注解类；可逐个注册类或按基包扫描。\n * <p>\n * 注意：多个 {@code @Configuration} 类时，后加载的 {@code @Bean} 定义会覆盖先加载的，\n * 可通过额外 Configuration 类 deliberately 覆盖 Bean 定义。\n *\n * @author Phillip Webb\n * @author Stephane Nicoll\n * @since 2.0.0\n * @see AnnotationConfigApplicationContext\n */",
        ),
        (
            "\t/**\n\t * Create a new AnnotationConfigReactiveWebApplicationContext that needs to be\n\t * populated through {@link #register} calls and then manually {@linkplain #refresh\n\t * refreshed}.\n\t */",
            "\t/**\n\t * 创建新的 AnnotationConfigReactiveWebApplicationContext，\n\t * 需通过 {@link #register} 注册后手动 {@linkplain #refresh refreshed}。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new AnnotationConfigApplicationContext with the given\n\t * DefaultListableBeanFactory.\n\t * @param beanFactory the DefaultListableBeanFactory instance to use for this context\n\t * @since 2.2.0\n\t */",
            "\t/**\n\t * 使用给定 DefaultListableBeanFactory 创建新的 AnnotationConfigApplicationContext。\n\t *\n\t * @param beanFactory the DefaultListableBeanFactory instance to use for this context 本上下文使用的 BeanFactory\n\t * @since 2.2.0\n\t */",
        ),
        (
            "\t/**\n\t * Create a new AnnotationConfigApplicationContext, deriving bean definitions from the\n\t * given annotated classes and automatically refreshing the context.\n\t * @param annotatedClasses one or more annotated classes, e.g.\n\t * {@link Configuration @Configuration} classes\n\t * @since 2.2.0\n\t */",
            "\t/**\n\t * 根据给定注解类推导 Bean 定义并自动刷新的 AnnotationConfigApplicationContext。\n\t *\n\t * @param annotatedClasses one or more annotated classes, e.g.\n\t * {@link Configuration @Configuration} classes 一个或多个注解类\n\t * @since 2.2.0\n\t */",
        ),
        (
            "\t/**\n\t * Create a new AnnotationConfigApplicationContext, scanning for bean definitions in\n\t * the given packages and automatically refreshing the context.\n\t * @param basePackages the packages to check for annotated classes\n\t * @since 2.2.0\n\t */",
            "\t/**\n\t * 扫描给定包中的 Bean 定义并自动刷新的 AnnotationConfigApplicationContext。\n\t *\n\t * @param basePackages the packages to check for annotated classes 要扫描的包\n\t * @since 2.2.0\n\t */",
        ),
        (
            "\t\t// We must be careful not to expose classpath resources\n",
            "\t\t// 须避免暴露类路径资源\n",
        ),
    ],
    "ConfigurableReactiveWebApplicationContext.java": [
        (
            "/**\n * Interface to provide configuration for a reactive web application.\n *\n * @author Stephane Nicoll\n * @since 2.0.0\n */",
            "/**\n * 为响应式 Web 应用提供配置能力的接口。\n * 组合 {@link ConfigurableApplicationContext} 与 {@link ReactiveWebApplicationContext}。\n *\n * @author Stephane Nicoll\n * @since 2.0.0\n */",
        ),
    ],
}
