"""Chinese JavaDoc replacements for springframework wave21a AOP framework [7:16] part B (Advised, AopProxyUtils)."""

AOP_FRAMEWORK_B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "Advised.java": [
        (
            "/**\n * Interface to be implemented by classes that hold the configuration\n * of a factory of AOP proxies. This configuration includes the\n * Interceptors and other advice, Advisors, and the proxied interfaces.\n *\n * <p>Any AOP proxy obtained from Spring can be cast to this interface to\n * allow manipulation of its AOP advice.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 13.03.2003\n * @see org.springframework.aop.framework.AdvisedSupport\n */",
            "/**\n * 由持有 AOP 代理工厂配置的类实现的接口。\n * 该配置包括 Interceptor 及其他 advice、Advisor 与被代理接口。\n *\n * <p>从 Spring 获取的任意 AOP 代理可转型为本接口以操作其 AOP advice。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 13.03.2003\n * @see org.springframework.aop.framework.AdvisedSupport\n */",
        ),
        (
            "\t/**\n\t * Return whether the Advised configuration is frozen,\n\t * in which case no advice changes can be made.\n\t */",
            "\t/**\n\t * 返回 Advised 配置是否已冻结，冻结后不可修改 advice。\n\t */",
        ),
        (
            "\t/**\n\t * Are we proxying the full target class instead of specified interfaces?\n\t */",
            "\t/**\n\t * 是否代理完整目标类而非指定接口。\n\t */",
        ),
        (
            "\t/**\n\t * Return the interfaces proxied by the AOP proxy.\n\t * <p>Will not include the target class, which may also be proxied.\n\t */",
            "\t/**\n\t * 返回 AOP 代理所代理的接口。\n\t * <p>不包含目标类（目标类本身也可能被代理）。\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the given interface is proxied.\n\t * @param ifc the interface to check\n\t */",
            "\t/**\n\t * 判断给定接口是否被代理。\n\t * @param ifc 待检查的接口\n\t */",
        ),
        (
            "\t/**\n\t * Change the {@code TargetSource} used by this {@code Advised} object.\n\t * <p>Only works if the configuration isn't {@linkplain #isFrozen frozen}.\n\t * @param targetSource new TargetSource to use\n\t */",
            "\t/**\n\t * 更改本 {@code Advised} 对象使用的 {@code TargetSource}。\n\t * <p>仅当配置未 {@linkplain #isFrozen 冻结} 时有效。\n\t * @param targetSource 新的 TargetSource\n\t */",
        ),
        (
            "\t/**\n\t * Return the {@code TargetSource} used by this {@code Advised} object.\n\t */",
            "\t/**\n\t * 返回本 {@code Advised} 对象使用的 {@code TargetSource}。\n\t */",
        ),
        (
            "\t/**\n\t * Set whether the proxy should be exposed by the AOP framework as a\n\t * {@link ThreadLocal} for retrieval via the {@link AopContext} class.\n\t * <p>It can be necessary to expose the proxy if an advised object needs\n\t * to invoke a method on itself with advice applied. Otherwise, if an\n\t * advised object invokes a method on {@code this}, no advice will be applied.\n\t * <p>Default is {@code false}, for optimal performance.\n\t */",
            "\t/**\n\t * 设置 AOP 框架是否将代理以 {@link ThreadLocal} 暴露，\n\t * 以便通过 {@link AopContext} 类获取。\n\t * <p>若被通知对象需在自身上调用带 advice 的方法，可能需要暴露代理。\n\t * 否则对 {@code this} 调用方法时不会应用 advice。\n\t * <p>默认为 {@code false}，以获得最佳性能。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the factory should expose the proxy as a {@link ThreadLocal}.\n\t * <p>It can be necessary to expose the proxy if an advised object needs\n\t * to invoke a method on itself with advice applied. Otherwise, if an\n\t * advised object invokes a method on {@code this}, no advice will be applied.\n\t * <p>Getting the proxy is analogous to an EJB calling {@code getEJBObject()}.\n\t * @see AopContext\n\t */",
            "\t/**\n\t * 返回工厂是否应将代理以 {@link ThreadLocal} 暴露。\n\t * <p>若被通知对象需在自身上调用带 advice 的方法，可能需要暴露代理。\n\t * 否则对 {@code this} 调用方法时不会应用 advice。\n\t * <p>获取代理类似 EJB 调用 {@code getEJBObject()}。\n\t * @see AopContext\n\t */",
        ),
        (
            "\t/**\n\t * Set whether this proxy configuration is pre-filtered so that it only\n\t * contains applicable advisors (matching this proxy's target class).\n\t * <p>Default is \"false\". Set this to \"true\" if the advisors have been\n\t * pre-filtered already, meaning that the ClassFilter check can be skipped\n\t * when building the actual advisor chain for proxy invocations.\n\t * @see org.springframework.aop.ClassFilter\n\t */",
            "\t/**\n\t * 设置本代理配置是否已预过滤，仅包含适用的通知器（匹配本代理目标类）。\n\t * <p>默认为 \"false\"。若通知器已预过滤，设为 \"true\"，\n\t * 构建代理调用的实际通知器链时可跳过 ClassFilter 检查。\n\t * @see org.springframework.aop.ClassFilter\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this proxy configuration is pre-filtered so that it only\n\t * contains applicable advisors (matching this proxy's target class).\n\t */",
            "\t/**\n\t * 返回本代理配置是否已预过滤，仅包含适用的通知器（匹配本代理目标类）。\n\t */",
        ),
        (
            "\t/**\n\t * Return the advisors applying to this proxy.\n\t * @return a list of Advisors applying to this proxy (never {@code null})\n\t */",
            "\t/**\n\t * 返回应用于本代理的通知器。\n\t * @return 应用于本代理的通知器列表（永不 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Return the number of advisors applying to this proxy.\n\t * <p>The default implementation delegates to {@code getAdvisors().length}.\n\t * @since 5.3.1\n\t */",
            "\t/**\n\t * 返回应用于本代理的通知器数量。\n\t * <p>默认实现委托给 {@code getAdvisors().length}。\n\t * @since 5.3.1\n\t */",
        ),
        (
            "\t/**\n\t * Add an advisor at the end of the advisor chain.\n\t * <p>The Advisor may be an {@link org.springframework.aop.IntroductionAdvisor},\n\t * in which new interfaces will be available when a proxy is next obtained\n\t * from the relevant factory.\n\t * @param advisor the advisor to add to the end of the chain\n\t * @throws AopConfigException in case of invalid advice\n\t */",
            "\t/**\n\t * 在通知器链末尾添加通知器。\n\t * <p>通知器可以是 {@link org.springframework.aop.IntroductionAdvisor}，\n\t * 下次从相关工厂获取代理时将可用新接口。\n\t * @param advisor 要添加到链末尾的通知器\n\t * @throws AopConfigException advice 无效时\n\t */",
        ),
        (
            "\t/**\n\t * Add an Advisor at the specified position in the chain.\n\t * @param advisor the advisor to add at the specified position in the chain\n\t * @param pos position in chain (0 is head). Must be valid.\n\t * @throws AopConfigException in case of invalid advice\n\t */",
            "\t/**\n\t * 在链的指定位置添加通知器。\n\t * @param advisor 要添加的通知器\n\t * @param pos 链中位置（0 为头部），须有效\n\t * @throws AopConfigException advice 无效时\n\t */",
        ),
        (
            "\t/**\n\t * Remove the given advisor.\n\t * @param advisor the advisor to remove\n\t * @return {@code true} if the advisor was removed; {@code false}\n\t * if the advisor was not found and hence could not be removed\n\t */",
            "\t/**\n\t * 移除给定通知器。\n\t * @param advisor 要移除的通知器\n\t * @return 若已移除则为 {@code true}；\n\t * 未找到因而无法移除则为 {@code false}\n\t */",
        ),
        (
            "\t/**\n\t * Remove the advisor at the given index.\n\t * @param index the index of advisor to remove\n\t * @throws AopConfigException if the index is invalid\n\t */",
            "\t/**\n\t * 移除指定索引处的通知器。\n\t * @param index 要移除的通知器索引\n\t * @throws AopConfigException 索引无效时\n\t */",
        ),
        (
            "\t/**\n\t * Return the index (from 0) of the given advisor,\n\t * or -1 if no such advisor applies to this proxy.\n\t * <p>The return value of this method can be used to index into the advisors array.\n\t * @param advisor the advisor to search for\n\t * @return index from 0 of this advisor, or -1 if there's no such advisor\n\t */",
            "\t/**\n\t * 返回给定通知器从 0 起的索引，\n\t * 若无适用通知器则返回 -1。\n\t * <p>返回值可用于索引通知器数组。\n\t * @param advisor 要查找的通知器\n\t * @return 该通知器从 0 起的索引，不存在则为 -1\n\t */",
        ),
        (
            "\t/**\n\t * Replace the given advisor.\n\t * <p><b>Note:</b> If the advisor is an {@link org.springframework.aop.IntroductionAdvisor}\n\t * and the replacement is not or implements different interfaces, the proxy will need\n\t * to be re-obtained or the old interfaces won't be supported and the new interface\n\t * won't be implemented.\n\t * @param a the advisor to replace\n\t * @param b the advisor to replace it with\n\t * @return whether it was replaced. If the advisor wasn't found in the\n\t * list of advisors, this method returns {@code false} and does nothing.\n\t * @throws AopConfigException in case of invalid advice\n\t */",
            "\t/**\n\t * 替换给定通知器。\n\t * <p><b>注意：</b> 若通知器为 {@link org.springframework.aop.IntroductionAdvisor}\n\t * 而替换项不是或实现不同接口，须重新获取代理，\n\t * 否则旧接口不受支持且新接口不会实现。\n\t * @param a 要替换的通知器\n\t * @param b 替换为的通知器\n\t * @return 是否已替换。若通知器列表中未找到，\n\t * 返回 {@code false} 且不执行任何操作。\n\t * @throws AopConfigException advice 无效时\n\t */",
        ),
        (
            "\t/**\n\t * Add the given AOP Alliance advice to the tail of the advice (interceptor) chain.\n\t * <p>This will be wrapped in a DefaultPointcutAdvisor with a pointcut that always\n\t * applies, and returned from the {@code getAdvisors()} method in this wrapped form.\n\t * <p>Note that the given advice will apply to all invocations on the proxy,\n\t * even to the {@code toString()} method! Use appropriate advice implementations\n\t * or specify appropriate pointcuts to apply to a narrower set of methods.\n\t * @param advice the advice to add to the tail of the chain\n\t * @throws AopConfigException in case of invalid advice\n\t * @see #addAdvice(int, Advice)\n\t * @see org.springframework.aop.support.DefaultPointcutAdvisor\n\t */",
            "\t/**\n\t * 将给定 AOP Alliance advice 添加到 advice（拦截器）链尾部。\n\t * <p>将包装为始终适用的 DefaultPointcutAdvisor，\n\t * 并以包装形式从 {@code getAdvisors()} 返回。\n\t * <p>注意：给定 advice 将应用于代理上的所有调用，\n\t * 包括 {@code toString()}！请使用合适的 advice 实现\n\t * 或指定更窄的切点以限制方法范围。\n\t * @param advice 要添加到链尾部的 advice\n\t * @throws AopConfigException advice 无效时\n\t * @see #addAdvice(int, Advice)\n\t * @see org.springframework.aop.support.DefaultPointcutAdvisor\n\t */",
        ),
        (
            "\t/**\n\t * Add the given AOP Alliance Advice at the specified position in the advice chain.\n\t * <p>This will be wrapped in a {@link org.springframework.aop.support.DefaultPointcutAdvisor}\n\t * with a pointcut that always applies, and returned from the {@link #getAdvisors()}\n\t * method in this wrapped form.\n\t * <p>Note: The given advice will apply to all invocations on the proxy,\n\t * even to the {@code toString()} method! Use appropriate advice implementations\n\t * or specify appropriate pointcuts to apply to a narrower set of methods.\n\t * @param pos index from 0 (head)\n\t * @param advice the advice to add at the specified position in the advice chain\n\t * @throws AopConfigException in case of invalid advice\n\t */",
            "\t/**\n\t * 在 advice 链的指定位置添加给定 AOP Alliance Advice。\n\t * <p>将包装为始终适用的 {@link org.springframework.aop.support.DefaultPointcutAdvisor}，\n\t * 并以包装形式从 {@link #getAdvisors()} 返回。\n\t * <p>注意：给定 advice 将应用于代理上的所有调用，\n\t * 包括 {@code toString()}！请使用合适的 advice 实现\n\t * 或指定更窄的切点以限制方法范围。\n\t * @param pos 从 0（头部）起的索引\n\t * @param advice 要添加到 advice 链指定位置的 advice\n\t * @throws AopConfigException advice 无效时\n\t */",
        ),
        (
            "\t/**\n\t * Remove the Advisor containing the given advice.\n\t * @param advice the advice to remove\n\t * @return {@code true} of the advice was found and removed;\n\t * {@code false} if there was no such advice\n\t */",
            "\t/**\n\t * 移除包含给定 advice 的通知器。\n\t * @param advice 要移除的 advice\n\t * @return 若找到并移除则为 {@code true}；\n\t * 无此 advice 则为 {@code false}\n\t */",
        ),
        (
            "\t/**\n\t * Return the index (from 0) of the given AOP Alliance Advice,\n\t * or -1 if no such advice is an advice for this proxy.\n\t * <p>The return value of this method can be used to index into\n\t * the advisors array.\n\t * @param advice the AOP Alliance advice to search for\n\t * @return index from 0 of this advice, or -1 if there's no such advice\n\t */",
            "\t/**\n\t * 返回给定 AOP Alliance Advice 从 0 起的索引，\n\t * 若无适用 advice 则返回 -1。\n\t * <p>返回值可用于索引通知器数组。\n\t * @param advice 要查找的 AOP Alliance advice\n\t * @return 该 advice 从 0 起的索引，不存在则为 -1\n\t */",
        ),
        (
            "\t/**\n\t * As {@code toString()} will normally be delegated to the target,\n\t * this returns the equivalent for the AOP proxy.\n\t * @return a string description of the proxy configuration\n\t */",
            "\t/**\n\t * 因 {@code toString()} 通常委托给目标，\n\t * 本方法返回 AOP 代理的等价描述。\n\t * @return 代理配置的字符串描述\n\t */",
        ),
    ],
    "AopProxyUtils.java": [
        (
            "/**\n * Utility methods for AOP proxy factories.\n *\n * <p>Mainly for internal use within the AOP framework.\n *\n * <p>See {@link org.springframework.aop.support.AopUtils} for a collection of\n * generic AOP utility methods which do not depend on AOP framework internals.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @see org.springframework.aop.support.AopUtils\n */",
            "/**\n * AOP 代理工厂的工具方法。\n *\n * <p>主要供 AOP 框架内部使用。\n *\n * <p>不依赖 AOP 框架内部的通用 AOP 工具方法见\n * {@link org.springframework.aop.support.AopUtils}。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @see org.springframework.aop.support.AopUtils\n */",
        ),
        (
            "\t/**\n\t * Obtain the singleton target object behind the given proxy, if any.\n\t * @param candidate the (potential) proxy to check\n\t * @return the singleton target object managed in a {@link SingletonTargetSource},\n\t * or {@code null} in any other case (not a proxy, not an existing singleton target)\n\t * @since 4.3.8\n\t * @see Advised#getTargetSource()\n\t * @see SingletonTargetSource#getTarget()\n\t */",
            "\t/**\n\t * 获取给定代理背后的单例目标对象（若有）。\n\t * @param candidate 待检查的（潜在）代理\n\t * @return {@link SingletonTargetSource} 中管理的单例目标对象，\n\t * 其他情况（非代理、非现有单例目标）返回 {@code null}\n\t * @since 4.3.8\n\t * @see Advised#getTargetSource()\n\t * @see SingletonTargetSource#getTarget()\n\t */",
        ),
        (
            "\t/**\n\t * Determine the ultimate target class of the given bean instance, traversing\n\t * not only a top-level proxy but any number of nested proxies as well &mdash;\n\t * as long as possible without side effects, that is, just for singleton targets.\n\t * @param candidate the instance to check (might be an AOP proxy)\n\t * @return the ultimate target class (or the plain class of the given\n\t * object as fallback; never {@code null})\n\t * @see org.springframework.aop.TargetClassAware#getTargetClass()\n\t * @see Advised#getTargetSource()\n\t */",
            "\t/**\n\t * 确定给定 Bean 实例的终极目标类，不仅遍历顶层代理，\n\t * 还遍历任意层嵌套代理——在无副作用前提下，即仅针对单例目标。\n\t * @param candidate 待检查的实例（可能是 AOP 代理）\n\t * @return 终极目标类（或回退为给定对象的普通类；永不 {@code null}）\n\t * @see org.springframework.aop.TargetClassAware#getTargetClass()\n\t * @see Advised#getTargetSource()\n\t */",
        ),
        (
            "\t/**\n\t * Complete the set of interfaces that are typically required in a JDK dynamic\n\t * proxy generated by Spring AOP.\n\t * <p>Specifically, {@link SpringProxy}, {@link Advised}, and {@link DecoratingProxy}\n\t * will be appended to the set of user-specified interfaces.\n\t * <p>This method can be useful when registering\n\t * {@linkplain org.springframework.aot.hint.ProxyHints proxy hints} for Spring's\n\t * AOT support, as demonstrated in the following example which uses this method\n\t * via a {@code static} import.\n\t * <pre class=\"code\">\n\t * RuntimeHints hints = ...\n\t * hints.proxies().registerJdkProxy(completeJdkProxyInterfaces(MyInterface.class));\n\t * </pre>\n\t * @param userInterfaces the set of user-specified interfaces implemented by\n\t * the component to be proxied\n\t * @return the complete set of interfaces that the proxy should implement\n\t * @throws IllegalArgumentException if a supplied {@code Class} is {@code null},\n\t * is not an {@linkplain Class#isInterface() interface}, or is a\n\t * {@linkplain Class#isSealed() sealed} interface\n\t * @since 6.0\n\t * @see SpringProxy\n\t * @see Advised\n\t * @see DecoratingProxy\n\t * @see org.springframework.aot.hint.RuntimeHints#proxies()\n\t * @see org.springframework.aot.hint.ProxyHints#registerJdkProxy(Class...)\n\t */",
            "\t/**\n\t * 补全 Spring AOP 生成的 JDK 动态代理通常所需的接口集合。\n\t * <p>具体而言，{@link SpringProxy}、{@link Advised} 与 {@link DecoratingProxy}\n\t * 会追加到用户指定接口集合中。\n\t * <p>为 Spring AOT 支持注册\n\t * {@linkplain org.springframework.aot.hint.ProxyHints 代理提示} 时本方法很有用，\n\t * 如下例通过 {@code static} 导入使用本方法：\n\t * <pre class=\"code\">\n\t * RuntimeHints hints = ...\n\t * hints.proxies().registerJdkProxy(completeJdkProxyInterfaces(MyInterface.class));\n\t * </pre>\n\t * @param userInterfaces 待代理组件实现的用户指定接口集合\n\t * @return 代理应实现的完整接口集合\n\t * @throws IllegalArgumentException 若提供的 {@code Class} 为 {@code null}、\n\t * 非 {@linkplain Class#isInterface() 接口}，或为\n\t * {@linkplain Class#isSealed() 密封} 接口\n\t * @since 6.0\n\t * @see SpringProxy\n\t * @see Advised\n\t * @see DecoratingProxy\n\t * @see org.springframework.aot.hint.RuntimeHints#proxies()\n\t * @see org.springframework.aot.hint.ProxyHints#registerJdkProxy(Class...)\n\t */",
        ),
        (
            "\t/**\n\t * Determine the complete set of interfaces to proxy for the given AOP configuration.\n\t * <p>This will always add the {@link Advised} interface unless the AdvisedSupport's\n\t * {@link AdvisedSupport#setOpaque \"opaque\"} flag is on. Always adds the\n\t * {@link org.springframework.aop.SpringProxy} marker interface.\n\t * @param advised the proxy config\n\t * @return the complete set of interfaces to proxy\n\t * @see SpringProxy\n\t * @see Advised\n\t */",
            "\t/**\n\t * 确定给定 AOP 配置应代理的完整接口集合。\n\t * <p>除非 AdvisedSupport 的 {@link AdvisedSupport#setOpaque \"opaque\"} 标志开启，\n\t * 否则始终添加 {@link Advised} 接口。始终添加\n\t * {@link org.springframework.aop.SpringProxy} 标记接口。\n\t * @param advised 代理配置\n\t * @return 应代理的完整接口集合\n\t * @see SpringProxy\n\t * @see Advised\n\t */",
        ),
        (
            "\t/**\n\t * Determine the complete set of interfaces to proxy for the given AOP configuration.\n\t * <p>This will always add the {@link Advised} interface unless the AdvisedSupport's\n\t * {@link AdvisedSupport#setOpaque \"opaque\"} flag is on. Always adds the\n\t * {@link org.springframework.aop.SpringProxy} marker interface.\n\t * @param advised the proxy config\n\t * @param decoratingProxy whether to expose the {@link DecoratingProxy} interface\n\t * @return the complete set of interfaces to proxy\n\t * @since 4.3\n\t * @see SpringProxy\n\t * @see Advised\n\t * @see DecoratingProxy\n\t */",
            "\t/**\n\t * 确定给定 AOP 配置应代理的完整接口集合。\n\t * <p>除非 AdvisedSupport 的 {@link AdvisedSupport#setOpaque \"opaque\"} 标志开启，\n\t * 否则始终添加 {@link Advised} 接口。始终添加\n\t * {@link org.springframework.aop.SpringProxy} 标记接口。\n\t * @param advised 代理配置\n\t * @param decoratingProxy 是否暴露 {@link DecoratingProxy} 接口\n\t * @return 应代理的完整接口集合\n\t * @since 4.3\n\t * @see SpringProxy\n\t * @see Advised\n\t * @see DecoratingProxy\n\t */",
        ),
        (
            "\t\t\t// No user-specified interfaces: check whether target class is an interface.",
            "\t\t\t// 无用户指定接口：检查目标类是否为接口。",
        ),
        (
            "\t\t\t// Only non-sealed interfaces are actually eligible for JDK proxying (on JDK 17)",
            "\t\t\t// 仅非密封接口才真正适合 JDK 代理（JDK 17 上）",
        ),
        (
            "\t/**\n\t * Extract the user-specified interfaces that the given proxy implements,\n\t * i.e. all non-Advised interfaces that the proxy implements.\n\t * @param proxy the proxy to analyze (usually a JDK dynamic proxy)\n\t * @return all user-specified interfaces that the proxy implements,\n\t * in the original order (never {@code null} or empty)\n\t * @see Advised\n\t */",
            "\t/**\n\t * 提取给定代理实现的用户指定接口，\n\t * 即代理实现的除 Advised 外的所有接口。\n\t * @param proxy 待分析的代理（通常为 JDK 动态代理）\n\t * @return 代理实现的用户指定接口，保持原始顺序\n\t * （永不 {@code null} 或空）\n\t * @see Advised\n\t */",
        ),
        (
            "\t/**\n\t * Check equality of the proxies behind the given AdvisedSupport objects.\n\t * Not the same as equality of the AdvisedSupport objects:\n\t * rather, equality of interfaces, advisors and target sources.\n\t */",
            "\t/**\n\t * 检查给定 AdvisedSupport 对象背后代理的相等性。\n\t * 不同于 AdvisedSupport 对象本身的相等性：\n\t * 而是比较接口、通知器与目标源。\n\t */",
        ),
        (
            "\t/**\n\t * Check equality of the proxied interfaces behind the given AdvisedSupport objects.\n\t */",
            "\t/**\n\t * 检查给定 AdvisedSupport 对象背后被代理接口的相等性。\n\t */",
        ),
        (
            "\t/**\n\t * Check equality of the advisors behind the given AdvisedSupport objects.\n\t */",
            "\t/**\n\t * 检查给定 AdvisedSupport 对象背后通知器的相等性。\n\t */",
        ),
        (
            "\t/**\n\t * Adapt the given arguments to the target signature in the given method,\n\t * if necessary: in particular, if a given vararg argument array does not\n\t * match the array type of the declared vararg parameter in the method.\n\t * @param method the target method\n\t * @param arguments the given arguments\n\t * @return a cloned argument array, or the original if no adaptation is needed\n\t * @since 4.2.3\n\t */",
            "\t/**\n\t * 必要时将给定参数适配为目标方法签名，\n\t * 尤其当可变参数数组与方法的声明可变参数数组类型不匹配时。\n\t * @param method 目标方法\n\t * @param arguments 给定参数\n\t * @return 克隆后的参数数组，无需适配则返回原数组\n\t * @since 4.2.3\n\t */",
        ),
    ],
}
