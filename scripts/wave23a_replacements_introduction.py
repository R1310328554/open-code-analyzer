"""Chinese JavaDoc replacements for springframework wave23a introduction support [0:3]."""

INTRODUCTION_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "DelegatePerTargetObjectIntroductionInterceptor.java": [
        (
            "/**\n * Convenient implementation of the\n * {@link org.springframework.aop.IntroductionInterceptor} interface.\n *\n * <p>This differs from {@link DelegatingIntroductionInterceptor} in that a single\n * instance of this class can be used to advise multiple target objects, and each target\n * object will have its <i>own</i> delegate (whereas DelegatingIntroductionInterceptor\n * shares the same delegate, and hence the same state across all targets).\n *\n * <p>The {@code suppressInterface} method can be used to suppress interfaces\n * implemented by the delegate class but which should not be introduced to the\n * owning AOP proxy.\n *\n * <p>An instance of this class is serializable if the delegates are.\n *\n * <p><i>Note: There are some implementation similarities between this class and\n * {@link DelegatingIntroductionInterceptor} that suggest a possible refactoring\n * to extract a common ancestor class in the future.</i>\n *\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @since 2.0\n * @see #suppressInterface\n * @see DelegatingIntroductionInterceptor\n */",
            "/**\n * {@link org.springframework.aop.IntroductionInterceptor} 接口的便捷实现。\n *\n * <p>与 {@link DelegatingIntroductionInterceptor} 的区别在于：\n * 本类的一个实例可用于通知多个目标对象，每个目标对象拥有<i>各自</i>的委托\n * （而 DelegatingIntroductionInterceptor 共享同一委托，因此所有目标共享同一状态）。\n *\n * <p>可使用 {@code suppressInterface} 方法抑制委托类已实现、\n * 但不应引入到所属 AOP 代理的接口。\n *\n * <p>若委托可序列化，则本类实例也可序列化。\n *\n * <p><i>注意：本类与 {@link DelegatingIntroductionInterceptor} 在实现上存在相似性，\n * 未来可能重构提取公共祖先类。</i>\n *\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @since 2.0\n * @see #suppressInterface\n * @see DelegatingIntroductionInterceptor\n */",
        ),
        (
            "\t/**\n\t * Hold weak references to keys as we don't want to interfere with garbage collection..\n\t */",
            "\t/**\n\t * 对键持有弱引用，避免干扰垃圾回收。\n\t */",
        ),
        (
            "\t\t// Create a new delegate now (but don't store it in the map).\n\t\t// We do this for two reasons:\n\t\t// 1) to fail early if there is a problem instantiating delegates\n\t\t// 2) to populate the interface map once and once only",
            "\t\t// 立即创建新委托（但不存入 map）。\n\t\t// 原因有二：\n\t\t// 1) 若实例化委托有问题则尽早失败\n\t\t// 2) 仅一次性填充接口 map",
        ),
        (
            "\t/**\n\t * Subclasses may need to override this if they want to perform custom\n\t * behavior in around advice. However, subclasses should invoke this\n\t * method, which handles introduced interfaces and forwarding to the target.\n\t */",
            "\t/**\n\t * 若子类需在环绕通知中执行自定义行为，可覆盖本方法。\n\t * 但子类应调用本方法，以处理引入接口及向目标转发。\n\t */",
        ),
        (
            "\t\t\t// Using the following method rather than direct reflection,\n\t\t\t// we get correct handling of InvocationTargetException\n\t\t\t// if the introduced method throws an exception.",
            "\t\t\t// 使用以下方法而非直接反射，\n\t\t\t// 可在引入方法抛出异常时正确处理 InvocationTargetException。",
        ),
        (
            "\t\t\t// Massage return value if possible: if the delegate returned itself,\n\t\t\t// we really want to return the proxy.",
            "\t\t\t// 尽可能调整返回值：若委托返回自身，\n\t\t\t// 实际应返回代理。",
        ),
        (
            "\t/**\n\t * Proceed with the supplied {@link org.aopalliance.intercept.MethodInterceptor}.\n\t * Subclasses can override this method to intercept method invocations on the\n\t * target object which is useful when an introduction needs to monitor the object\n\t * that it is introduced into. This method is <strong>never</strong> called for\n\t * {@link MethodInvocation MethodInvocations} on the introduced interfaces.\n\t */",
            "\t/**\n\t * 继续执行提供的 {@link org.aopalliance.intercept.MethodInterceptor}。\n\t * 子类可覆盖本方法以拦截目标对象上的方法调用，\n\t * 适用于引入需监控被引入对象的情况。\n\t * 对引入接口上的 {@link MethodInvocation MethodInvocation} <strong>永不</strong>调用本方法。\n\t */",
        ),
        (
            "\t\t// If we get here, just pass the invocation on.",
            "\t\t// 执行到此则直接传递调用。",
        ),
    ],
    "DelegatingIntroductionInterceptor.java": [
        (
            "/**\n * Convenient implementation of the\n * {@link org.springframework.aop.IntroductionInterceptor} interface.\n *\n * <p>Subclasses merely need to extend this class and implement the interfaces\n * to be introduced themselves. In this case the delegate is the subclass\n * instance itself. Alternatively a separate delegate may implement the\n * interface, and be set via the delegate bean property.\n *\n * <p>Delegates or subclasses may implement any number of interfaces.\n * All interfaces except IntroductionInterceptor are picked up from\n * the subclass or delegate by default.\n *\n * <p>The {@code suppressInterface} method can be used to suppress interfaces\n * implemented by the delegate but which should not be introduced to the owning\n * AOP proxy.\n *\n * <p>An instance of this class is serializable if the delegate is.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 16.11.2003\n * @see #suppressInterface\n * @see DelegatePerTargetObjectIntroductionInterceptor\n */",
            "/**\n * {@link org.springframework.aop.IntroductionInterceptor} 接口的便捷实现。\n *\n * <p>子类只需继承本类并实现要引入的接口即可，\n * 此时委托即为子类实例本身。也可由独立委托实现接口，\n * 并通过 delegate bean 属性设置。\n *\n * <p>委托或子类可实现任意数量接口。\n * 默认从子类或委托收集除 IntroductionInterceptor 外的所有接口。\n *\n * <p>可使用 {@code suppressInterface} 方法抑制委托已实现、\n * 但不应引入到所属 AOP 代理的接口。\n *\n * <p>若委托可序列化，则本类实例也可序列化。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 16.11.2003\n * @see #suppressInterface\n * @see DelegatePerTargetObjectIntroductionInterceptor\n */",
        ),
        (
            "\t/**\n\t * Object that actually implements the interfaces.\n\t * May be \"this\" if a subclass implements the introduced interfaces.\n\t */",
            "\t/**\n\t * 实际实现接口的对象。\n\t * 若子类实现引入接口，则可为 \"this\"。\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new DelegatingIntroductionInterceptor, providing\n\t * a delegate that implements the interfaces to be introduced.\n\t * @param delegate the delegate that implements the introduced interfaces\n\t */",
            "\t/**\n\t * 构造新的 DelegatingIntroductionInterceptor，\n\t * 提供实现要引入接口的委托。\n\t * @param delegate 实现引入接口的委托\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new DelegatingIntroductionInterceptor.\n\t * The delegate will be the subclass, which must implement\n\t * additional interfaces.\n\t */",
            "\t/**\n\t * 构造新的 DelegatingIntroductionInterceptor。\n\t * 委托为子类本身，子类须实现额外接口。\n\t */",
        ),
        (
            "\t/**\n\t * Both constructors use this init method, as it is impossible to pass\n\t * a \"this\" reference from one constructor to another.\n\t * @param delegate the delegate object\n\t */",
            "\t/**\n\t * 两个构造函数均使用此 init 方法，\n\t * 因无法将 \"this\" 引用从一个构造函数传给另一个。\n\t * @param delegate 委托对象\n\t */",
        ),
        (
            "\t\t// We don't want to expose the control interface",
            "\t\t// 不暴露控制接口",
        ),
        (
            "\t/**\n\t * Subclasses may need to override this if they want to perform custom\n\t * behavior in around advice. However, subclasses should invoke this\n\t * method, which handles introduced interfaces and forwarding to the target.\n\t */",
            "\t/**\n\t * 若子类需在环绕通知中执行自定义行为，可覆盖本方法。\n\t * 但子类应调用本方法，以处理引入接口及向目标转发。\n\t */",
        ),
        (
            "\t\t\t// Using the following method rather than direct reflection, we\n\t\t\t// get correct handling of InvocationTargetException\n\t\t\t// if the introduced method throws an exception.",
            "\t\t\t// 使用以下方法而非直接反射，\n\t\t\t// 可在引入方法抛出异常时正确处理 InvocationTargetException。",
        ),
        (
            "\t\t\t// Massage return value if possible: if the delegate returned itself,\n\t\t\t// we really want to return the proxy.",
            "\t\t\t// 尽可能调整返回值：若委托返回自身，\n\t\t\t// 实际应返回代理。",
        ),
        (
            "\t/**\n\t * Proceed with the supplied {@link org.aopalliance.intercept.MethodInterceptor}.\n\t * Subclasses can override this method to intercept method invocations on the\n\t * target object which is useful when an introduction needs to monitor the object\n\t * that it is introduced into. This method is <strong>never</strong> called for\n\t * {@link MethodInvocation MethodInvocations} on the introduced interfaces.\n\t */",
            "\t/**\n\t * 继续执行提供的 {@link org.aopalliance.intercept.MethodInterceptor}。\n\t * 子类可覆盖本方法以拦截目标对象上的方法调用，\n\t * 适用于引入需监控被引入对象的情况。\n\t * 对引入接口上的 {@link MethodInvocation MethodInvocation} <strong>永不</strong>调用本方法。\n\t */",
        ),
        (
            "\t\t// If we get here, just pass the invocation on.",
            "\t\t// 执行到此则直接传递调用。",
        ),
    ],
    "IntroductionInfoSupport.java": [
        (
            "/**\n * Support for implementations of {@link org.springframework.aop.IntroductionInfo}.\n *\n * <p>Allows subclasses to conveniently add all interfaces from a given object,\n * and to suppress interfaces that should not be added. Also allows for querying\n * all introduced interfaces.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 为 {@link org.springframework.aop.IntroductionInfo} 实现提供支持。\n *\n * <p>便于子类从给定对象添加全部接口，\n * 并抑制不应添加的接口。也可查询所有引入接口。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/**\n\t * Suppress the specified interface, which may have been autodetected\n\t * due to the delegate implementing it. Call this method to exclude\n\t * internal interfaces from being visible at the proxy level.\n\t * <p>Does nothing if the interface is not implemented by the delegate.\n\t * @param ifc the interface to suppress\n\t */",
            "\t/**\n\t * 抑制指定接口（可能因委托实现而被自动检测）。\n\t * 调用本方法可在代理层排除内部接口。\n\t * <p>若委托未实现该接口则不执行任何操作。\n\t * @param ifc 要抑制的接口\n\t */",
        ),
        (
            "\t/**\n\t * Check whether the specified interfaces is a published introduction interface.\n\t * @param ifc the interface to check\n\t * @return whether the interface is part of this introduction\n\t */",
            "\t/**\n\t * 检查指定接口是否为已发布的引入接口。\n\t * @param ifc 待检查的接口\n\t * @return 该接口是否属于本引入\n\t */",
        ),
        (
            "\t/**\n\t * Publish all interfaces that the given delegate implements at the proxy level.\n\t * @param delegate the delegate object\n\t */",
            "\t/**\n\t * 在代理层发布给定委托实现的所有接口。\n\t * @param delegate 委托对象\n\t */",
        ),
        (
            "\t/**\n\t * Is this method on an introduced interface?\n\t * @param mi the method invocation\n\t * @return whether the invoked method is on an introduced interface\n\t */",
            "\t/**\n\t * 该方法是否位于引入接口上？\n\t * @param mi 方法调用\n\t * @return 被调用方法是否位于引入接口上\n\t */",
        ),
        (
            "\t\t\t// Work it out and cache it.",
            "\t\t\t// 计算并缓存结果。",
        ),
        (
            "\t//---------------------------------------------------------------------\n\t// Serialization support\n\t//---------------------------------------------------------------------",
            "\t//---------------------------------------------------------------------\n\t// 序列化支持\n\t//---------------------------------------------------------------------",
        ),
        (
            "\t/**\n\t * This method is implemented only to restore the logger.\n\t * We don't make the logger static as that would mean that subclasses\n\t * would use this class's log category.\n\t */",
            "\t/**\n\t * 实现本方法仅用于恢复 logger。\n\t * 不将 logger 设为 static，否则子类会使用本类的日志类别。\n\t */",
        ),
        (
            "\t\t// Rely on default serialization; just initialize state after deserialization.",
            "\t\t// 依赖默认序列化；反序列化后仅初始化状态。",
        ),
        (
            "\t\t// Initialize transient fields.",
            "\t\t// 初始化 transient 字段。",
        ),
    ],
}
