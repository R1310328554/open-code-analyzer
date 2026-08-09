"""Chinese JavaDoc replacements for springframework wave21b autoproxy [20]."""

AUTOPROXY_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractAdvisorAutoProxyCreator.java": [
        (
            "/**\n * Generic auto proxy creator that builds AOP proxies for specific beans\n * based on detected Advisors for each bean.\n *\n * <p>Subclasses may override the {@link #findCandidateAdvisors()} method to\n * return a custom list of Advisors applying to any object. Subclasses can\n * also override the inherited {@link #shouldSkip} method to exclude certain\n * objects from auto-proxying.\n *\n * <p>Advisors or advices requiring ordering should be annotated with\n * {@link org.springframework.core.annotation.Order @Order} or implement the\n * {@link org.springframework.core.Ordered} interface. This class sorts\n * advisors using the {@link AnnotationAwareOrderComparator}. Advisors that are\n * not annotated with {@code @Order} or don't implement the {@code Ordered}\n * interface will be considered as unordered; they will appear at the end of the\n * advisor chain in an undefined order.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see #findCandidateAdvisors\n */",
            "/**\n * 通用自动代理创建器：根据检测到的 Advisor\n * 为特定 Bean 构建 AOP 代理。\n *\n * <p>子类可覆盖 {@link #findCandidateAdvisors()} 方法，\n * 返回适用于任意对象的自定义 Advisor 列表。\n * 子类也可覆盖继承的 {@link #shouldSkip} 方法，\n * 将特定对象排除在自动代理之外。\n *\n * <p>需要排序的 Advisor 或 Advice 应标注\n * {@link org.springframework.core.annotation.Order @Order} 或实现\n * {@link org.springframework.core.Ordered} 接口。本类使用\n * {@link AnnotationAwareOrderComparator} 排序 Advisor。\n * 未标注 {@code @Order} 或未实现 {@code Ordered} 接口的 Advisor\n * 视为无序，将以未定义顺序出现在 Advisor 链末尾。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see #findCandidateAdvisors\n */",
        ),
        (
            "\t/**\n\t * Find all eligible Advisors for auto-proxying this class.\n\t * @param beanClass the clazz to find advisors for\n\t * @param beanName the name of the currently proxied bean\n\t * @return the empty List, not {@code null},\n\t * if there are no pointcuts or interceptors\n\t * @see #findCandidateAdvisors\n\t * @see #sortAdvisors\n\t * @see #extendAdvisors\n\t */",
            "\t/**\n\t * 查找适用于自动代理本类的所有合格 Advisor。\n\t * @param beanClass 要查找 Advisor 的类\n\t * @param beanName 当前被代理 Bean 的名称\n\t * @return 若无切入点或拦截器则返回空 List（非 {@code null}）\n\t * @see #findCandidateAdvisors\n\t * @see #sortAdvisors\n\t * @see #extendAdvisors\n\t */",
        ),
        (
            "\t/**\n\t * Find all candidate Advisors to use in auto-proxying.\n\t * @return the List of candidate Advisors\n\t */",
            "\t/**\n\t * 查找用于自动代理的所有候选 Advisor。\n\t * @return 候选 Advisor 列表\n\t */",
        ),
        (
            "\t/**\n\t * Search the given candidate Advisors to find all Advisors that\n\t * can apply to the specified bean.\n\t * @param candidateAdvisors the candidate Advisors\n\t * @param beanClass the target's bean class\n\t * @param beanName the target's bean name\n\t * @return the List of applicable Advisors\n\t * @see ProxyCreationContext#getCurrentProxiedBeanName()\n\t */",
            "\t/**\n\t * 在候选 Advisor 中搜索所有可应用于指定 Bean 的 Advisor。\n\t * @param candidateAdvisors 候选 Advisor\n\t * @param beanClass 目标 Bean 类\n\t * @param beanName 目标 Bean 名称\n\t * @return 适用的 Advisor 列表\n\t * @see ProxyCreationContext#getCurrentProxiedBeanName()\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the Advisor bean with the given name is eligible\n\t * for proxying in the first place.\n\t * @param beanName the name of the Advisor bean\n\t * @return whether the bean is eligible\n\t */",
            "\t/**\n\t * 返回给定名称的 Advisor Bean 是否具备代理资格。\n\t * @param beanName Advisor Bean 名称\n\t * @return 该 Bean 是否合格\n\t */",
        ),
        (
            "\t/**\n\t * Sort advisors based on ordering. Subclasses may choose to override this\n\t * method to customize the sorting strategy.\n\t * @param advisors the source List of Advisors\n\t * @return the sorted List of Advisors\n\t * @see org.springframework.core.Ordered\n\t * @see org.springframework.core.annotation.Order\n\t * @see org.springframework.core.annotation.AnnotationAwareOrderComparator\n\t */",
            "\t/**\n\t * 按排序规则对 Advisor 排序。子类可覆盖以自定义排序策略。\n\t * @param advisors 源 Advisor 列表\n\t * @return 排序后的 Advisor 列表\n\t * @see org.springframework.core.Ordered\n\t * @see org.springframework.core.annotation.Order\n\t * @see org.springframework.core.annotation.AnnotationAwareOrderComparator\n\t */",
        ),
        (
            "\t/**\n\t * Extension hook that subclasses can override to register additional Advisors,\n\t * given the sorted Advisors obtained to date.\n\t * <p>The default implementation is empty.\n\t * <p>Typically used to add Advisors that expose contextual information\n\t * required by some of the later advisors.\n\t * @param candidateAdvisors the Advisors that have already been identified as\n\t * applying to a given bean\n\t */",
            "\t/**\n\t * 扩展钩子：子类可覆盖以注册额外 Advisor，\n\t * 基于目前已排序的 Advisor。\n\t * <p>默认实现为空。\n\t * <p>通常用于添加暴露后续 Advisor 所需上下文信息的 Advisor。\n\t * @param candidateAdvisors 已识别为适用于给定 Bean 的 Advisor\n\t */",
        ),
        (
            "\t/**\n\t * This auto-proxy creator always returns pre-filtered Advisors.\n\t */",
            "\t/**\n\t * 本自动代理创建器始终返回预过滤的 Advisor。\n\t */",
        ),
        (
            "\t/**\n\t * Subclass of BeanFactoryAdvisorRetrievalHelper that delegates to\n\t * surrounding AbstractAdvisorAutoProxyCreator facilities.\n\t */",
            "\t/**\n\t * BeanFactoryAdvisorRetrievalHelper 的子类，\n\t * 委托给外围 AbstractAdvisorAutoProxyCreator 设施。\n\t */",
        ),
    ],
}
