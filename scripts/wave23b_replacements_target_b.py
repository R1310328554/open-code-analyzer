"""Chinese JavaDoc replacements for springframework wave23b target package [9:14]."""

TARGET_B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "PrototypeTargetSource.java": [
        (
            "/**\n * {@link org.springframework.aop.TargetSource} implementation that\n * creates a new instance of the target bean for each request,\n * destroying each instance on release (after each request).\n *\n * <p>Obtains bean instances from its containing\n * {@link org.springframework.beans.factory.BeanFactory}.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see #setBeanFactory\n * @see #setTargetBeanName\n */",
            "/**\n * 每次请求创建目标 Bean 新实例、并在释放（每次请求后）时销毁的\n * {@link org.springframework.aop.TargetSource} 实现。\n *\n * <p>从其所在的 {@link org.springframework.beans.factory.BeanFactory} 获取 Bean 实例。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see #setBeanFactory\n * @see #setTargetBeanName\n */",
        ),
        (
            "\t/**\n\t * Obtain a new prototype instance for every call.\n\t * @see #newPrototypeInstance()\n\t */",
            "\t/**\n\t * 每次调用获取新原型实例。\n\t * @see #newPrototypeInstance()\n\t */",
        ),
        (
            "\t/**\n\t * Destroy the given independent instance.\n\t * @see #destroyPrototypeInstance\n\t */",
            "\t/**\n\t * 销毁给定独立实例。\n\t * @see #destroyPrototypeInstance\n\t */",
        ),
    ],
    "SimpleBeanTargetSource.java": [
        (
            "/**\n * Simple {@link org.springframework.aop.TargetSource} implementation,\n * freshly obtaining the specified target bean from its containing\n * Spring {@link org.springframework.beans.factory.BeanFactory}.\n *\n * <p>Can obtain any kind of target bean: singleton, scoped, or prototype.\n * Typically used for scoped beans.\n *\n * @author Juergen Hoeller\n * @since 2.0.3\n */",
            "/**\n * 简单的 {@link org.springframework.aop.TargetSource} 实现，\n * 每次从所在 Spring {@link org.springframework.beans.factory.BeanFactory}\n * 获取指定目标 Bean。\n *\n * <p>可获取任意类型目标 Bean：单例、作用域或原型。\n * 通常用于作用域 Bean。\n *\n * @author Juergen Hoeller\n * @since 2.0.3\n */",
        ),
    ],
    "SingletonTargetSource.java": [
        (
            "/**\n * Implementation of the {@link org.springframework.aop.TargetSource} interface\n * that holds a given object. This is the default implementation of the TargetSource\n * interface, as used by the Spring AOP framework. There is usually no need to\n * create objects of this class in application code.\n *\n * <p>This class is serializable. However, the actual serializability of a\n * SingletonTargetSource will depend on whether the target is serializable.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see org.springframework.aop.framework.AdvisedSupport#setTarget(Object)\n */",
            "/**\n * 持有给定对象的 {@link org.springframework.aop.TargetSource} 接口实现。\n * 这是 Spring AOP 框架使用的 TargetSource 接口默认实现。\n * 应用代码通常无需创建本类对象。\n *\n * <p>本类可序列化，但 SingletonTargetSource 的实际可序列性取决于目标是否可序列化。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see org.springframework.aop.framework.AdvisedSupport#setTarget(Object)\n */",
        ),
        (
            "\t/** use serialVersionUID from Spring 1.2 for interoperability. */",
            "\t/** 使用 Spring 1.2 的 serialVersionUID 以保证互操作性。 */",
        ),
        (
            "\t/** Target cached and invoked using reflection. */",
            "\t/** 缓存并通过反射调用的目标对象。 */",
        ),
        (
            "\t/**\n\t * Create a new SingletonTargetSource for the given target.\n\t * @param target the target object\n\t */",
            "\t/**\n\t * 为给定目标创建 SingletonTargetSource。\n\t * @param target 目标对象\n\t */",
        ),
        (
            "\t/**\n\t * Two invoker interceptors are equal if they have the same target or if the\n\t * targets or the targets are equal.\n\t */",
            "\t/**\n\t * 若目标相同或目标对象相等，则两个调用拦截器相等。\n\t */",
        ),
        (
            "\t/**\n\t * SingletonTargetSource uses the hash code of the target object.\n\t */",
            "\t/**\n\t * SingletonTargetSource 使用目标对象的哈希码。\n\t */",
        ),
    ],
    "ThreadLocalTargetSource.java": [
        (
            "/**\n * Alternative to an object pool. This {@link org.springframework.aop.TargetSource}\n * uses a threading model in which every thread has its own copy of the target.\n * There's no contention for targets. Target object creation is kept to a minimum\n * on the running server.\n *\n * <p>Application code is written as to a normal pool; callers can't assume they\n * will be dealing with the same instance in invocations in different threads.\n * However, state can be relied on during the operations of a single thread:\n * for example, if one caller makes repeated calls on the AOP proxy.\n *\n * <p>Cleanup of thread-bound objects is performed on BeanFactory destruction,\n * calling their {@code DisposableBean.destroy()} method if available.\n * Be aware that many thread-bound objects can be around until the application\n * actually shuts down.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @see ThreadLocalTargetSourceStats\n * @see org.springframework.beans.factory.DisposableBean#destroy()\n */",
            "/**\n * 对象池的替代方案。本 {@link org.springframework.aop.TargetSource}\n * 采用每线程持有目标副本的线程模型。\n * 目标无竞争，运行服务器上目标对象创建次数最少。\n *\n * <p>应用代码写法类似普通池；调用方不能假设不同线程调用会处理同一实例。\n * 但在单线程操作期间可依赖状态：例如同一调用方多次调用 AOP 代理。\n *\n * <p>BeanFactory 销毁时清理线程绑定对象，\n * 若可用则调用其 {@code DisposableBean.destroy()}。\n * 注意：许多线程绑定对象可能直到应用真正关闭才释放。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @see ThreadLocalTargetSourceStats\n * @see org.springframework.beans.factory.DisposableBean#destroy()\n */",
        ),
        (
            "\t/**\n\t * ThreadLocal holding the target associated with the current\n\t * thread. Unlike most ThreadLocals, which are static, this variable\n\t * is meant to be per thread per instance of the ThreadLocalTargetSource class.\n\t */",
            "\t/**\n\t * 持有当前线程关联目标的 ThreadLocal。\n\t * 与多数 static 的 ThreadLocal 不同，\n\t * 本变量为每个 ThreadLocalTargetSource 实例、每线程一份。\n\t */",
        ),
        (
            "\t/**\n\t * Set of managed targets, enabling us to keep track of the targets we've created.\n\t */",
            "\t/**\n\t * 受管目标集合，用于跟踪已创建的目标。\n\t */",
        ),
        (
            "\t/**\n\t * Implementation of abstract getTarget() method.\n\t * We look for a target held in a ThreadLocal. If we don't find one,\n\t * we create one and bind it to the thread. No synchronization is required.\n\t */",
            "\t/**\n\t * 抽象 getTarget() 方法的实现。\n\t * 在 ThreadLocal 中查找目标；若无则创建并绑定到线程。无需同步。\n\t */",
        ),
        (
            "\t\t\t// Associate target with ThreadLocal.",
            "\t\t\t// 将目标关联到 ThreadLocal。",
        ),
        (
            "\t/**\n\t * Dispose of targets if necessary; clear ThreadLocal.\n\t * @see #destroyPrototypeInstance\n\t */",
            "\t/**\n\t * 必要时销毁目标；清除 ThreadLocal。\n\t * @see #destroyPrototypeInstance\n\t */",
        ),
        (
            "\t\tlogger.debug(\"Destroying ThreadLocalTargetSource bindings\");",
            "\t\tlogger.debug(\"正在销毁 ThreadLocalTargetSource 绑定\");",
        ),
        (
            "\t\t// Clear ThreadLocal, just in case.",
            "\t\t// 清除 ThreadLocal，以防万一。",
        ),
        (
            "\t/**\n\t * Return an introduction advisor mixin that allows the AOP proxy to be\n\t * cast to ThreadLocalInvokerStats.\n\t */",
            "\t/**\n\t * 返回 introduction advisor mixin，\n\t * 允许将 AOP 代理转型为 ThreadLocalInvokerStats。\n\t */",
        ),
    ],
    "ThreadLocalTargetSourceStats.java": [
        (
            "/**\n * Statistics for a ThreadLocal TargetSource.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * ThreadLocal TargetSource 的统计信息。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/**\n\t * Return the number of client invocations.\n\t */",
            "\t/**\n\t * 返回客户端调用次数。\n\t */",
        ),
        (
            "\t/**\n\t * Return the number of hits that were satisfied by a thread-bound object.\n\t */",
            "\t/**\n\t * 返回由线程绑定对象满足的命中次数。\n\t */",
        ),
        (
            "\t/**\n\t * Return the number of thread-bound objects created.\n\t */",
            "\t/**\n\t * 返回已创建的线程绑定对象数量。\n\t */",
        ),
    ],
}
