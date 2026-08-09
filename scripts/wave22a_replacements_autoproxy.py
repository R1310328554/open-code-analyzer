"""Chinese JavaDoc replacements for springframework wave22a autoproxy [0:11]."""

AUTOPROXY_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractBeanFactoryAwareAdvisingPostProcessor.java": [
        (
            "/**\n * Extension of {@link AbstractAdvisingBeanPostProcessor} which implements\n * {@link BeanFactoryAware}, adds exposure of the original target class for each\n * proxied bean ({@link AutoProxyUtils#ORIGINAL_TARGET_CLASS_ATTRIBUTE}),\n * and participates in an externally enforced target-class mode for any given bean\n * ({@link AutoProxyUtils#PRESERVE_TARGET_CLASS_ATTRIBUTE}).\n * This post-processor is therefore aligned with {@link AbstractAutoProxyCreator}.\n *\n * @author Juergen Hoeller\n * @since 4.2.3\n * @see AutoProxyUtils#shouldProxyTargetClass\n * @see AutoProxyUtils#determineTargetClass\n */",
            "/**\n * {@link AbstractAdvisingBeanPostProcessor} 的扩展，实现 {@link BeanFactoryAware}，\n * 为每个被代理 Bean 暴露原始目标类（{@link AutoProxyUtils#ORIGINAL_TARGET_CLASS_ATTRIBUTE}），\n * 并参与对给定 Bean 的外部强制目标类模式（{@link AutoProxyUtils#PRESERVE_TARGET_CLASS_ATTRIBUTE}）。\n * 因此本后处理器与 {@link AbstractAutoProxyCreator} 保持一致。\n *\n * @author Juergen Hoeller\n * @since 4.2.3\n * @see AutoProxyUtils#shouldProxyTargetClass\n * @see AutoProxyUtils#determineTargetClass\n */",
        ),
    ],
    "AutoProxyUtils.java": [
        (
            "/**\n * Utilities for auto-proxy aware components.\n * Mainly for internal use within the framework.\n *\n * @author Juergen Hoeller\n * @since 2.0.3\n * @see AbstractAutoProxyCreator\n * @see AbstractBeanFactoryAwareAdvisingPostProcessor\n */",
            "/**\n * 自动代理感知组件的工具类。\n * 主要用于框架内部。\n *\n * @author Juergen Hoeller\n * @since 2.0.3\n * @see AbstractAutoProxyCreator\n * @see AbstractBeanFactoryAwareAdvisingPostProcessor\n */",
        ),
        (
            "\t/**\n\t * The bean name of the internally managed auto-proxy creator.\n\t * @since 7.0\n\t */",
            "\t/**\n\t * 内部管理的自动代理创建器的 Bean 名称。\n\t * @since 7.0\n\t */",
        ),
        (
            "\t/**\n\t * Bean definition attribute that may indicate the interfaces to be proxied\n\t * (in case of it getting proxied in the first place). The value is either\n\t * a single interface {@code Class} or an array of {@code Class}, with an\n\t * empty array specifically signalling that all implemented interfaces need\n\t * to be proxied.\n\t * @since 7.0\n\t * @see #determineExposedInterfaces\n\t */",
            "\t/**\n\t * Bean 定义属性，可指示要代理的接口\n\t * （若该 Bean 首先会被代理）。值为单个接口 {@code Class}\n\t * 或 {@code Class} 数组；空数组明确表示需代理所有已实现接口。\n\t * @since 7.0\n\t * @see #determineExposedInterfaces\n\t */",
        ),
        (
            "\t/**\n\t * Attribute value for specifically signalling that all implemented interfaces\n\t * need to be proxied (through an empty {@code Class} array).\n\t * @since 7.0\n\t * @see #EXPOSED_INTERFACES_ATTRIBUTE\n\t */",
            "\t/**\n\t * 专门用于表示需代理所有已实现接口的属性值\n\t * （通过空 {@code Class} 数组）。\n\t * @since 7.0\n\t * @see #EXPOSED_INTERFACES_ATTRIBUTE\n\t */",
        ),
        (
            "\t/**\n\t * Bean definition attribute that may indicate whether a given bean is supposed\n\t * to be proxied with its target class (in case of it getting proxied in the first\n\t * place). The value is {@code Boolean.TRUE} or {@code Boolean.FALSE}.\n\t * <p>Proxy factories can set this attribute if they built a target class proxy\n\t * for a specific bean, and want to enforce that bean can always be cast\n\t * to its target class (even if AOP advices get applied through auto-proxying).\n\t * @see #shouldProxyTargetClass\n\t */",
            "\t/**\n\t * Bean 定义属性，可指示给定 Bean 是否应以目标类代理\n\t * （若该 Bean 首先会被代理）。值为 {@code Boolean.TRUE} 或 {@code Boolean.FALSE}。\n\t * <p>若代理工厂为特定 Bean 构建了目标类代理，\n\t * 并希望该 Bean 始终可转型为目标类\n\t * （即使通过自动代理应用了 AOP 通知），可设置此属性。\n\t * @see #shouldProxyTargetClass\n\t */",
        ),
        (
            "\t/**\n\t * Bean definition attribute that indicates the original target class of an\n\t * auto-proxied bean, for example, to be used for the introspection of annotations\n\t * on the target class behind an interface-based proxy.\n\t * @since 4.2.3\n\t * @see #determineTargetClass\n\t */",
            "\t/**\n\t * Bean 定义属性，指示自动代理 Bean 的原始目标类，\n\t * 例如用于内省基于接口的代理背后的目标类上的注解。\n\t * @since 4.2.3\n\t * @see #determineTargetClass\n\t */",
        ),
        (
            "\t/**\n\t * Apply default ProxyConfig settings to the given ProxyConfig instance, if necessary.\n\t * @param proxyConfig the current ProxyConfig instance\n\t * @param beanFactory the BeanFactory to take the default ProxyConfig from\n\t * @since 7.0\n\t * @see #DEFAULT_PROXY_CONFIG_BEAN_NAME\n\t * @see ProxyConfig#copyDefault\n\t */",
            "\t/**\n\t * 如有必要，将默认 ProxyConfig 设置应用到给定 ProxyConfig 实例。\n\t * @param proxyConfig 当前 ProxyConfig 实例\n\t * @param beanFactory 用于获取默认 ProxyConfig 的 BeanFactory\n\t * @since 7.0\n\t * @see #DEFAULT_PROXY_CONFIG_BEAN_NAME\n\t * @see ProxyConfig#copyDefault\n\t */",
        ),
        (
            "\t/**\n\t * Determine the specific interfaces for proxying the given bean, if any.\n\t * Checks the {@link #EXPOSED_INTERFACES_ATTRIBUTE \"exposedInterfaces\" attribute}\n\t * of the corresponding bean definition.\n\t * @param beanFactory the containing ConfigurableListableBeanFactory\n\t * @param beanName the name of the bean\n\t * @return whether the given bean should be proxied with its target class\n\t * @since 7.0\n\t * @see #EXPOSED_INTERFACES_ATTRIBUTE\n\t */",
            "\t/**\n\t * 确定代理给定 Bean 的特定接口（若有）。\n\t * 检查对应 Bean 定义的 {@link #EXPOSED_INTERFACES_ATTRIBUTE \"exposedInterfaces\" 属性}。\n\t * @param beanFactory 包含的 ConfigurableListableBeanFactory\n\t * @param beanName Bean 名称\n\t * @return 给定 Bean 是否应以目标类代理\n\t * @since 7.0\n\t * @see #EXPOSED_INTERFACES_ATTRIBUTE\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the given bean should be proxied with its target\n\t * class rather than its interfaces. Checks the\n\t * {@link #PRESERVE_TARGET_CLASS_ATTRIBUTE \"preserveTargetClass\" attribute}\n\t * of the corresponding bean definition.\n\t * @param beanFactory the containing ConfigurableListableBeanFactory\n\t * @param beanName the name of the bean\n\t * @return whether the given bean should be proxied with its target class\n\t * @see #PRESERVE_TARGET_CLASS_ATTRIBUTE\n\t */",
            "\t/**\n\t * 确定给定 Bean 是否应以目标类而非接口代理。\n\t * 检查对应 Bean 定义的\n\t * {@link #PRESERVE_TARGET_CLASS_ATTRIBUTE \"preserveTargetClass\" 属性}。\n\t * @param beanFactory 包含的 ConfigurableListableBeanFactory\n\t * @param beanName Bean 名称\n\t * @return 给定 Bean 是否应以目标类代理\n\t * @see #PRESERVE_TARGET_CLASS_ATTRIBUTE\n\t */",
        ),
        (
            "\t/**\n\t * Determine the original target class for the specified bean, if possible,\n\t * otherwise falling back to a regular {@code getType} lookup.\n\t * @param beanFactory the containing ConfigurableListableBeanFactory\n\t * @param beanName the name of the bean\n\t * @return the original target class as stored in the bean definition, if any\n\t * @since 4.2.3\n\t * @see org.springframework.beans.factory.BeanFactory#getType(String)\n\t */",
            "\t/**\n\t * 尽可能确定指定 Bean 的原始目标类，\n\t * 否则回退到常规 {@code getType} 查找。\n\t * @param beanFactory 包含的 ConfigurableListableBeanFactory\n\t * @param beanName Bean 名称\n\t * @return 存储在 Bean 定义中的原始目标类（若有）\n\t * @since 4.2.3\n\t * @see org.springframework.beans.factory.BeanFactory#getType(String)\n\t */",
        ),
        (
            "\t/**\n\t * Expose the given target class for the specified bean, if possible.\n\t * @param beanFactory the containing ConfigurableListableBeanFactory\n\t * @param beanName the name of the bean\n\t * @param targetClass the corresponding target class\n\t * @since 4.2.3\n\t */",
            "\t/**\n\t * 尽可能为指定 Bean 暴露给定目标类。\n\t * @param beanFactory 包含的 ConfigurableListableBeanFactory\n\t * @param beanName Bean 名称\n\t * @param targetClass 对应的目标类\n\t * @since 4.2.3\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the given bean name indicates an \"original instance\"\n\t * according to {@link AutowireCapableBeanFactory#ORIGINAL_INSTANCE_SUFFIX},\n\t * skipping any proxy attempts for it.\n\t * @param beanName the name of the bean\n\t * @param beanClass the corresponding bean class\n\t * @since 5.1\n\t * @see AutowireCapableBeanFactory#ORIGINAL_INSTANCE_SUFFIX\n\t */",
            "\t/**\n\t * 根据 {@link AutowireCapableBeanFactory#ORIGINAL_INSTANCE_SUFFIX}\n\t * 判断给定 Bean 名称是否表示「原始实例」，从而跳过对其的代理尝试。\n\t * @param beanName Bean 名称\n\t * @param beanClass 对应 Bean 类\n\t * @since 5.1\n\t * @see AutowireCapableBeanFactory#ORIGINAL_INSTANCE_SUFFIX\n\t */",
        ),
    ],
    "BeanFactoryAdvisorRetrievalHelper.java": [
        (
            "/**\n * Helper for retrieving standard Spring Advisors from a BeanFactory,\n * for use with auto-proxying.\n *\n * @author Juergen Hoeller\n * @since 2.0.2\n * @see AbstractAdvisorAutoProxyCreator\n */",
            "/**\n * 从 BeanFactory 检索标准 Spring Advisor 的辅助类，\n * 供自动代理使用。\n *\n * @author Juergen Hoeller\n * @since 2.0.2\n * @see AbstractAdvisorAutoProxyCreator\n */",
        ),
        (
            "\t/**\n\t * Create a new BeanFactoryAdvisorRetrievalHelper for the given BeanFactory.\n\t * @param beanFactory the ListableBeanFactory to scan\n\t */",
            "\t/**\n\t * 为给定 BeanFactory 创建新的 BeanFactoryAdvisorRetrievalHelper。\n\t * @param beanFactory 要扫描的 ListableBeanFactory\n\t */",
        ),
        (
            "\t/**\n\t * Find all eligible Advisor beans in the current bean factory,\n\t * ignoring FactoryBeans and excluding beans that are currently in creation.\n\t * @return the list of {@link org.springframework.aop.Advisor} beans\n\t * @see #isEligibleBean\n\t */",
            "\t/**\n\t * 查找当前 BeanFactory 中所有合格的 Advisor Bean，\n\t * 忽略 FactoryBean 并排除正在创建中的 Bean。\n\t * @return {@link org.springframework.aop.Advisor} Bean 列表\n\t * @see #isEligibleBean\n\t */",
        ),
        (
            "\t\t// Determine list of advisor bean names, if not cached already.",
            "\t\t// 若尚未缓存，则确定 Advisor Bean 名称列表。",
        ),
        (
            "\t\t\t// Do not initialize FactoryBeans here: We need to leave all regular beans\n\t\t\t// uninitialized to let the auto-proxy creator apply to them!",
            "\t\t\t// 此处不初始化 FactoryBean：需保持所有普通 Bean 未初始化，\n\t\t\t// 以便自动代理创建器对其生效！",
        ),
        (
            "\t\t\t\t\t\t\t\t// Ignore: indicates a reference back to the bean we're trying to advise.\n\t\t\t\t\t\t\t\t// We want to find advisors other than the currently created bean itself.",
            "\t\t\t\t\t\t\t\t// 忽略：表示回引到正试图被增强的 Bean。\n\t\t\t\t\t\t\t\t// 我们要找的是除当前正在创建的 Bean 自身以外的 Advisor。",
        ),
        (
            "\t/**\n\t * Determine whether the aspect bean with the given name is eligible.\n\t * <p>The default implementation always returns {@code true}.\n\t * @param beanName the name of the aspect bean\n\t * @return whether the bean is eligible\n\t */",
            "\t/**\n\t * 判断给定名称的切面 Bean 是否合格。\n\t * <p>默认实现始终返回 {@code true}。\n\t * @param beanName 切面 Bean 名称\n\t * @return 该 Bean 是否合格\n\t */",
        ),
    ],
    "BeanNameAutoProxyCreator.java": [
        (
            "/**\n * Auto proxy creator that identifies beans to proxy via a list of names.\n * Checks for direct, \"xxx*\", and \"*xxx\" matches.\n *\n * <p>For configuration details, see the javadoc of the parent class\n * AbstractAutoProxyCreator. Typically, you will specify a list of\n * interceptor names to apply to all identified beans, via the\n * \"interceptorNames\" property.\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 10.10.2003\n * @see #setBeanNames\n * @see #isMatch\n * @see #setInterceptorNames\n * @see AbstractAutoProxyCreator\n */",
            "/**\n * 通过名称列表识别要代理 Bean 的自动代理创建器。\n * 支持直接匹配、\"xxx*\" 和 \"*xxx\" 模式。\n *\n * <p>配置细节请参阅父类 AbstractAutoProxyCreator 的 JavaDoc。\n * 通常通过 \"interceptorNames\" 属性为所有识别到的 Bean 指定拦截器名称列表。\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 10.10.2003\n * @see #setBeanNames\n * @see #isMatch\n * @see #setInterceptorNames\n * @see AbstractAutoProxyCreator\n */",
        ),
        (
            "\t/**\n\t * Set the names of the beans that should automatically get wrapped with proxies.\n\t * A name can specify a prefix to match by ending with \"*\", for example, \"myBean,tx*\"\n\t * will match the bean named \"myBean\" and all beans whose name start with \"tx\".\n\t * <p><b>NOTE:</b> In case of a FactoryBean, only the objects created by the\n\t * FactoryBean will get proxied. If you intend to proxy a FactoryBean instance\n\t * itself (a rare use case), specify the bean name of the FactoryBean\n\t * including the factory-bean prefix \"&amp;\": for example, \"&amp;myFactoryBean\".\n\t * @see org.springframework.beans.factory.FactoryBean\n\t * @see org.springframework.beans.factory.BeanFactory#FACTORY_BEAN_PREFIX\n\t */",
            "\t/**\n\t * 设置应自动被代理包装的 Bean 名称。\n\t * 名称可通过以 \"*\" 结尾指定前缀匹配，例如 \"myBean,tx*\"\n\t * 将匹配名为 \"myBean\" 的 Bean 及所有以 \"tx\" 开头的 Bean。\n\t * <p><b>注意：</b>对于 FactoryBean，仅 FactoryBean 创建的对象会被代理。\n\t * 若要代理 FactoryBean 实例本身（罕见场景），\n\t * 需指定含工厂 Bean 前缀 \"&amp;\" 的名称，例如 \"&amp;myFactoryBean\"。\n\t * @see org.springframework.beans.factory.FactoryBean\n\t * @see org.springframework.beans.factory.BeanFactory#FACTORY_BEAN_PREFIX\n\t */",
        ),
        (
            "\t/**\n\t * Delegate to {@link AbstractAutoProxyCreator#getCustomTargetSource(Class, String)}\n\t * if the bean name matches one of the names in the configured list of supported\n\t * names, returning {@code null} otherwise.\n\t * @since 5.3\n\t * @see #setBeanNames(String...)\n\t */",
            "\t/**\n\t * 若 Bean 名称匹配配置的支持名称列表之一，\n\t * 则委托 {@link AbstractAutoProxyCreator#getCustomTargetSource(Class, String)}，\n\t * 否则返回 {@code null}。\n\t * @since 5.3\n\t * @see #setBeanNames(String...)\n\t */",
        ),
        (
            "\t/**\n\t * Identify as a bean to proxy if the bean name matches one of the names in\n\t * the configured list of supported names.\n\t * @see #setBeanNames(String...)\n\t */",
            "\t/**\n\t * 若 Bean 名称匹配配置的支持名称列表之一，则识别为需代理的 Bean。\n\t * @see #setBeanNames(String...)\n\t */",
        ),
        (
            "\t/**\n\t * Determine if the bean name for the given bean class matches one of the names\n\t * in the configured list of supported names.\n\t * @param beanClass the class of the bean to advise\n\t * @param beanName the name of the bean\n\t * @return {@code true} if the given bean name is supported\n\t * @see #setBeanNames(String...)\n\t */",
            "\t/**\n\t * 判断给定 Bean 类的 Bean 名称是否匹配配置的支持名称列表之一。\n\t * @param beanClass 要增强的 Bean 类\n\t * @param beanName Bean 名称\n\t * @return 若给定 Bean 名称被支持则为 {@code true}\n\t * @see #setBeanNames(String...)\n\t */",
        ),
        (
            "\t\t\t\t\tmappedName = mappedName.substring(1);  // length of '&'",
            "\t\t\t\t\tmappedName = mappedName.substring(1);  // '&' 的长度",
        ),
        (
            "\t/**\n\t * Determine if the given bean name matches the mapped name.\n\t * <p>The default implementation checks for \"xxx*\", \"*xxx\" and \"*xxx*\" matches,\n\t * as well as direct equality. Can be overridden in subclasses.\n\t * @param beanName the bean name to check\n\t * @param mappedName the name in the configured list of names\n\t * @return if the names match\n\t * @see org.springframework.util.PatternMatchUtils#simpleMatch(String, String)\n\t */",
            "\t/**\n\t * 判断给定 Bean 名称是否与映射名称匹配。\n\t * <p>默认实现检查 \"xxx*\"、\"*xxx\" 和 \"*xxx*\" 匹配及直接相等。\n\t * 子类可覆盖。\n\t * @param beanName 要检查的 Bean 名称\n\t * @param mappedName 配置名称列表中的名称\n\t * @return 名称是否匹配\n\t * @see org.springframework.util.PatternMatchUtils#simpleMatch(String, String)\n\t */",
        ),
    ],
    "DefaultAdvisorAutoProxyCreator.java": [
        (
            "/**\n * {@code BeanPostProcessor} implementation that creates AOP proxies based on all\n * candidate {@code Advisor}s in the current {@code BeanFactory}. This class is\n * completely generic; it contains no special code to handle any particular aspects,\n * such as pooling aspects.\n *\n * <p>It's possible to filter out advisors - for example, to use multiple post processors\n * of this type in the same factory - by setting the {@code usePrefix} property to true,\n * in which case only advisors beginning with the DefaultAdvisorAutoProxyCreator's bean\n * name followed by a dot (like \"aapc.\") will be used. This default prefix can be changed\n * from the bean name by setting the {@code advisorBeanNamePrefix} property.\n * The separator (.) will also be used in this case.\n *\n * @author Rod Johnson\n * @author Rob Harrop\n */",
            "/**\n * 基于当前 {@code BeanFactory} 中所有候选 {@code Advisor}\n * 创建 AOP 代理的 {@code BeanPostProcessor} 实现。本类完全通用，\n * 不含处理特定切面（如池化切面）的特殊代码。\n *\n * <p>可通过将 {@code usePrefix} 设为 true 过滤 Advisor——\n * 例如在同一工厂中使用多个此类后处理器——\n * 此时仅使用以 DefaultAdvisorAutoProxyCreator 的 Bean 名称\n * 加点开头（如 \"aapc.\"）的 Advisor。\n * 可通过 {@code advisorBeanNamePrefix} 属性更改默认前缀，\n * 此情况下分隔符（.）同样适用。\n *\n * @author Rod Johnson\n * @author Rob Harrop\n */",
        ),
        (
            "\t/** Separator between prefix and remainder of bean name. */",
            "\t/** Bean 名称前缀与剩余部分之间的分隔符。 */",
        ),
        (
            "\t/**\n\t * Set whether to only include advisors with a certain prefix in the bean name.\n\t * <p>Default is {@code false}, including all beans of type {@code Advisor}.\n\t * @see #setAdvisorBeanNamePrefix\n\t */",
            "\t/**\n\t * 设置是否仅包含 Bean 名称带特定前缀的 Advisor。\n\t * <p>默认为 {@code false}，包含所有 {@code Advisor} 类型 Bean。\n\t * @see #setAdvisorBeanNamePrefix\n\t */",
        ),
        (
            "\t/**\n\t * Return whether to only include advisors with a certain prefix in the bean name.\n\t */",
            "\t/**\n\t * 返回是否仅包含 Bean 名称带特定前缀的 Advisor。\n\t */",
        ),
        (
            "\t/**\n\t * Set the prefix for bean names that will cause them to be included for\n\t * auto-proxying by this object. This prefix should be set to avoid circular\n\t * references. Default value is the bean name of this object + a dot.\n\t * @param advisorBeanNamePrefix the exclusion prefix\n\t */",
            "\t/**\n\t * 设置使 Bean 被本对象纳入自动代理的 Bean 名称前缀。\n\t * 应设置此前缀以避免循环引用。默认值为本对象 Bean 名称加点。\n\t * @param advisorBeanNamePrefix 排除前缀\n\t */",
        ),
        (
            "\t/**\n\t * Return the prefix for bean names that will cause them to be included\n\t * for auto-proxying by this object.\n\t */",
            "\t/**\n\t * 返回使 Bean 被本对象纳入自动代理的 Bean 名称前缀。\n\t */",
        ),
        (
            "\t\t// If no infrastructure bean name prefix has been set, override it.",
            "\t\t// 若尚未设置基础设施 Bean 名称前缀，则覆盖它。",
        ),
        (
            "\t/**\n\t * Consider {@code Advisor} beans with the specified prefix as eligible, if activated.\n\t * @see #setUsePrefix\n\t * @see #setAdvisorBeanNamePrefix\n\t */",
            "\t/**\n\t * 若已激活，将带指定前缀的 {@code Advisor} Bean 视为合格。\n\t * @see #setUsePrefix\n\t * @see #setAdvisorBeanNamePrefix\n\t */",
        ),
    ],
    "InfrastructureAdvisorAutoProxyCreator.java": [
        (
            "/**\n * Auto-proxy creator that considers infrastructure Advisor beans only,\n * ignoring any application-defined Advisors.\n *\n * @author Juergen Hoeller\n * @since 2.0.7\n */",
            "/**\n * 仅考虑基础设施 Advisor Bean 的自动代理创建器，\n * 忽略所有应用定义的 Advisor。\n *\n * @author Juergen Hoeller\n * @since 2.0.7\n */",
        ),
    ],
    "ProxyCreationContext.java": [
        (
            "/**\n * Holder for the current proxy creation context, as exposed by auto-proxy creators\n * such as {@link AbstractAdvisorAutoProxyCreator}.\n *\n * @author Juergen Hoeller\n * @author Ramnivas Laddad\n * @since 2.5\n */",
            "/**\n * 当前代理创建上下文的持有者，\n * 由 {@link AbstractAdvisorAutoProxyCreator} 等自动代理创建器暴露。\n *\n * @author Juergen Hoeller\n * @author Ramnivas Laddad\n * @since 2.5\n */",
        ),
        (
            "\t/** ThreadLocal holding the current proxied bean name during Advisor matching. */",
            "\t/** 在 Advisor 匹配期间持有当前被代理 Bean 名称的 ThreadLocal。 */",
        ),
        (
            "\t/**\n\t * Return the name of the currently proxied bean instance.\n\t * @return the name of the bean, or {@code null} if none available\n\t */",
            "\t/**\n\t * 返回当前被代理 Bean 实例的名称。\n\t * @return Bean 名称，若无则 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Set the name of the currently proxied bean instance.\n\t * @param beanName the name of the bean, or {@code null} to reset it\n\t */",
            "\t/**\n\t * 设置当前被代理 Bean 实例的名称。\n\t * @param beanName Bean 名称，或 {@code null} 以重置\n\t */",
        ),
    ],
    "TargetSourceCreator.java": [
        (
            "/**\n * Implementations can create special target sources, such as pooling target\n * sources, for particular beans. For example, they may base their choice\n * on attributes, such as a pooling attribute, on the target class.\n *\n * <p>AbstractAutoProxyCreator can support a number of TargetSourceCreators,\n * which will be applied in order.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 实现类可为特定 Bean 创建特殊 TargetSource，\n * 例如池化 TargetSource。选择可基于目标类上的属性（如池化属性）。\n *\n * <p>AbstractAutoProxyCreator 可支持多个 TargetSourceCreator，\n * 按顺序应用。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/**\n\t * Create a special TargetSource for the given bean, if any.\n\t * @param beanClass the class of the bean to create a TargetSource for\n\t * @param beanName the name of the bean\n\t * @return a special TargetSource or {@code null} if this TargetSourceCreator isn't\n\t * interested in the particular bean\n\t */",
            "\t/**\n\t * 为给定 Bean 创建特殊 TargetSource（若有）。\n\t * @param beanClass 要创建 TargetSource 的 Bean 类\n\t * @param beanName Bean 名称\n\t * @return 特殊 TargetSource；若本 TargetSourceCreator 对该 Bean 无兴趣则 {@code null}\n\t */",
        ),
    ],
    "AbstractBeanFactoryBasedTargetSourceCreator.java": [
        (
            "/**\n * Convenient superclass for\n * {@link org.springframework.aop.framework.autoproxy.TargetSourceCreator}\n * implementations that require creating multiple instances of a prototype bean.\n *\n * <p>Uses an internal BeanFactory to manage the target instances,\n * copying the original bean definition to this internal factory.\n * This is necessary because the original BeanFactory will just\n * contain the proxy instance created through auto-proxying.\n *\n * <p>Requires running in an\n * {@link org.springframework.beans.factory.support.AbstractBeanFactory}.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource\n * @see org.springframework.beans.factory.support.AbstractBeanFactory\n */",
            "/**\n * 需要为原型 Bean 创建多个实例的\n * {@link org.springframework.aop.framework.autoproxy.TargetSourceCreator}\n * 实现的便捷超类。\n *\n * <p>使用内部 BeanFactory 管理目标实例，\n * 将原始 Bean 定义复制到该内部工厂。\n * 这是必要的，因为原始 BeanFactory 仅包含通过自动代理创建的代理实例。\n *\n * <p>需在 {@link org.springframework.beans.factory.support.AbstractBeanFactory} 中运行。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource\n * @see org.springframework.beans.factory.support.AbstractBeanFactory\n */",
        ),
        (
            "\t/** Internally used DefaultListableBeanFactory instances, keyed by bean name. */",
            "\t/** 内部使用的 DefaultListableBeanFactory 实例，按 Bean 名称索引。 */",
        ),
        (
            "\t/**\n\t * Return the BeanFactory that this TargetSourceCreators runs in.\n\t */",
            "\t/**\n\t * 返回本 TargetSourceCreator 运行的 BeanFactory。\n\t */",
        ),
        (
            "\t//---------------------------------------------------------------------\n\t// Implementation of the TargetSourceCreator interface\n\t//---------------------------------------------------------------------",
            "\t//---------------------------------------------------------------------\n\t// TargetSourceCreator 接口实现\n\t//---------------------------------------------------------------------",
        ),
        (
            "\t\t// We need to override just this bean definition, as it may reference other beans\n\t\t// and we're happy to take the parent's definition for those.\n\t\t// Always use prototype scope if demanded.",
            "\t\t// 仅需覆盖此 Bean 定义，因其可能引用其他 Bean，\n\t\t// 而那些 Bean 我们乐于采用父级定义。\n\t\t// 若要求则始终使用 prototype 作用域。",
        ),
        (
            "\t\t// Complete configuring the PrototypeTargetSource.",
            "\t\t// 完成 PrototypeTargetSource 的配置。",
        ),
        (
            "\t/**\n\t * Return the internal BeanFactory to be used for the specified bean.\n\t * @param beanName the name of the target bean\n\t * @return the internal BeanFactory to be used\n\t */",
            "\t/**\n\t * 返回用于指定 Bean 的内部 BeanFactory。\n\t * @param beanName 目标 Bean 名称\n\t * @return 要使用的内部 BeanFactory\n\t */",
        ),
        (
            "\t/**\n\t * Build an internal BeanFactory for resolving target beans.\n\t * @param containingFactory the containing BeanFactory that originally defines the beans\n\t * @return an independent internal BeanFactory to hold copies of some target beans\n\t */",
            "\t/**\n\t * 构建用于解析目标 Bean 的内部 BeanFactory。\n\t * @param containingFactory 最初定义 Bean 的包含 BeanFactory\n\t * @return 用于持有部分目标 Bean 副本的独立内部 BeanFactory\n\t */",
        ),
        (
            "\t\t// Set parent so that references (up container hierarchies) are correctly resolved.",
            "\t\t// 设置父级以便正确解析（向上容器层次）的引用。",
        ),
        (
            "\t\t// Required so that all BeanPostProcessors, Scopes, etc become available.",
            "\t\t// 以便所有 BeanPostProcessor、Scope 等可用。",
        ),
        (
            "\t\t// Filter out BeanPostProcessors that are part of the AOP infrastructure,\n\t\t// since those are only meant to apply to beans defined in the original factory.",
            "\t\t// 过滤属于 AOP 基础设施的 BeanPostProcessor，\n\t\t// 因为它们仅应应用于原始工厂中定义的 Bean。",
        ),
        (
            "\t/**\n\t * Destroys the internal BeanFactory on shutdown of the TargetSourceCreator.\n\t * @see #getInternalBeanFactoryForBean\n\t */",
            "\t/**\n\t * 在 TargetSourceCreator 关闭时销毁内部 BeanFactory。\n\t * @see #getInternalBeanFactoryForBean\n\t */",
        ),
        (
            "\t//---------------------------------------------------------------------\n\t// Template methods to be implemented by subclasses\n\t//---------------------------------------------------------------------",
            "\t//---------------------------------------------------------------------\n\t// 子类需实现的模板方法\n\t//---------------------------------------------------------------------",
        ),
        (
            "\t/**\n\t * Return whether this TargetSourceCreator is prototype-based.\n\t * The scope of the target bean definition will be set accordingly.\n\t * <p>Default is \"true\".\n\t * @see org.springframework.beans.factory.config.BeanDefinition#isSingleton()\n\t */",
            "\t/**\n\t * 返回本 TargetSourceCreator 是否基于原型。\n\t * 目标 Bean 定义的作用域将相应设置。\n\t * <p>默认为 \"true\"。\n\t * @see org.springframework.beans.factory.config.BeanDefinition#isSingleton()\n\t */",
        ),
        (
            "\t/**\n\t * Subclasses must implement this method to return a new AbstractPrototypeBasedTargetSource\n\t * if they wish to create a custom TargetSource for this bean, or {@code null} if they are\n\t * not interested it in, in which case no special target source will be created.\n\t * Subclasses should not call {@code setTargetBeanName} or {@code setBeanFactory}\n\t * on the AbstractPrototypeBasedTargetSource: This class' implementation of\n\t * {@code getTargetSource()} will do that.\n\t * @param beanClass the class of the bean to create a TargetSource for\n\t * @param beanName the name of the bean\n\t * @return the AbstractPrototypeBasedTargetSource, or {@code null} if we don't match this\n\t */",
            "\t/**\n\t * 子类必须实现本方法：若要为该 Bean 创建自定义 TargetSource，\n\t * 则返回新的 AbstractPrototypeBasedTargetSource；若无兴趣则 {@code null}，\n\t * 此时不会创建特殊 TargetSource。\n\t * 子类不应在 AbstractPrototypeBasedTargetSource 上调用\n\t * {@code setTargetBeanName} 或 {@code setBeanFactory}：\n\t * 本类的 {@code getTargetSource()} 实现会处理。\n\t * @param beanClass 要创建 TargetSource 的 Bean 类\n\t * @param beanName Bean 名称\n\t * @return AbstractPrototypeBasedTargetSource，若不匹配则 {@code null}\n\t */",
        ),
    ],
    "LazyInitTargetSourceCreator.java": [
        (
            "/**\n * {@code TargetSourceCreator} that enforces a {@link LazyInitTargetSource} for\n * each bean that is defined as \"lazy-init\". This will lead to a proxy created for\n * each of those beans, allowing to fetch a reference to such a bean without\n * actually initializing the target bean instance.\n *\n * <p>To be registered as custom {@code TargetSourceCreator} for an auto-proxy\n * creator, in combination with custom interceptors for specific beans or for the\n * creation of lazy-init proxies only. For example, as an autodetected\n * infrastructure bean in an XML application context definition:\n *\n * <pre class=\"code\">\n * &lt;bean class=\"org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator\"&gt;\n *   &lt;property name=\"beanNames\" value=\"*\" /&gt; &lt;!-- apply to all beans --&gt;\n *   &lt;property name=\"customTargetSourceCreators\"&gt;\n *     &lt;list&gt;\n *       &lt;bean class=\"org.springframework.aop.framework.autoproxy.target.LazyInitTargetSourceCreator\" /&gt;\n *     &lt;/list&gt;\n *   &lt;/property&gt;\n * &lt;/bean&gt;\n *\n * &lt;bean id=\"myLazyInitBean\" class=\"mypackage.MyBeanClass\" lazy-init=\"true\"&gt;\n *   &lt;!-- ... --&gt;\n * &lt;/bean&gt;</pre>\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 1.2\n * @see org.springframework.beans.factory.config.BeanDefinition#isLazyInit\n * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#setCustomTargetSourceCreators\n * @see org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator\n */",
            "/**\n * 为每个定义为 \"lazy-init\" 的 Bean 强制使用 {@link LazyInitTargetSource} 的\n * {@code TargetSourceCreator}。这将为每个此类 Bean 创建代理，\n * 允许获取其引用而无需实际初始化目标 Bean 实例。\n *\n * <p>作为自动代理创建器的自定义 {@code TargetSourceCreator} 注册，\n * 可与针对特定 Bean 的自定义拦截器组合，或仅用于创建 lazy-init 代理。\n * 例如作为 XML 应用上下文定义中自动检测的基础设施 Bean：\n *\n * <pre class=\"code\">\n * &lt;bean class=\"org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator\"&gt;\n *   &lt;property name=\"beanNames\" value=\"*\" /&gt; &lt;!-- 应用于所有 Bean --&gt;\n *   &lt;property name=\"customTargetSourceCreators\"&gt;\n *     &lt;list&gt;\n *       &lt;bean class=\"org.springframework.aop.framework.autoproxy.target.LazyInitTargetSourceCreator\" /&gt;\n *     &lt;/list&gt;\n *   &lt;/property&gt;\n * &lt;/bean&gt;\n *\n * &lt;bean id=\"myLazyInitBean\" class=\"mypackage.MyBeanClass\" lazy-init=\"true\"&gt;\n *   &lt;!-- ... --&gt;\n * &lt;/bean&gt;</pre>\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 1.2\n * @see org.springframework.beans.factory.config.BeanDefinition#isLazyInit\n * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#setCustomTargetSourceCreators\n * @see org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator\n */",
        ),
    ],
    "QuickTargetSourceCreator.java": [
        (
            "/**\n * Convenient TargetSourceCreator using bean name prefixes to create one of three\n * well-known TargetSource types:\n * <ul>\n * <li>: CommonsPool2TargetSource</li>\n * <li>% ThreadLocalTargetSource</li>\n * <li>! PrototypeTargetSource</li>\n * </ul>\n *\n * @author Rod Johnson\n * @author Stephane Nicoll\n * @see org.springframework.aop.target.CommonsPool2TargetSource\n * @see org.springframework.aop.target.ThreadLocalTargetSource\n * @see org.springframework.aop.target.PrototypeTargetSource\n */",
            "/**\n * 通过 Bean 名称前缀创建三种知名 TargetSource 类型的便捷 TargetSourceCreator：\n * <ul>\n * <li>: CommonsPool2TargetSource</li>\n * <li>% ThreadLocalTargetSource</li>\n * <li>! PrototypeTargetSource</li>\n * </ul>\n *\n * @author Rod Johnson\n * @author Stephane Nicoll\n * @see org.springframework.aop.target.CommonsPool2TargetSource\n * @see org.springframework.aop.target.ThreadLocalTargetSource\n * @see org.springframework.aop.target.PrototypeTargetSource\n */",
        ),
        (
            "\t/**\n\t * The CommonsPool2TargetSource prefix.\n\t */",
            "\t/**\n\t * CommonsPool2TargetSource 前缀。\n\t */",
        ),
        (
            "\t/**\n\t * The ThreadLocalTargetSource prefix.\n\t */",
            "\t/**\n\t * ThreadLocalTargetSource 前缀。\n\t */",
        ),
        (
            "\t/**\n\t * The PrototypeTargetSource prefix.\n\t */",
            "\t/**\n\t * PrototypeTargetSource 前缀。\n\t */",
        ),
        (
            "\t\t\t// No match. Don't create a custom target source.",
            "\t\t\t// 无匹配。不创建自定义 TargetSource。",
        ),
    ],
}
