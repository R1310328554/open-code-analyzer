"""Chinese JavaDoc replacements for springframework wave23b target package [1:8]."""

TARGET_A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractLazyCreationTargetSource.java": [
        (
            "/**\n * {@link org.springframework.aop.TargetSource} implementation that will\n * lazily create a user-managed object.\n *\n * <p>Creation of the lazy target object is controlled by the user by implementing\n * the {@link #createObject()} method. This {@code TargetSource} will invoke\n * this method the first time the proxy is accessed.\n *\n * <p>Useful when you need to pass a reference to some dependency to an object\n * but you don't actually want the dependency to be created until it is first used.\n * A typical scenario for this is a connection to a remote resource.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 1.2.4\n * @see #isInitialized()\n * @see #createObject()\n */",
            "/**\n * 延迟创建用户管理对象的 {@link org.springframework.aop.TargetSource} 实现。\n *\n * <p>通过实现 {@link #createObject()} 方法由用户控制延迟目标对象的创建。\n * 本 {@code TargetSource} 在首次访问代理时调用该方法。\n *\n * <p>适用于需要向对象传递某依赖引用、但希望直到首次使用时才创建该依赖的场景。\n * 典型用例是连接远程资源。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 1.2.4\n * @see #isInitialized()\n * @see #createObject()\n */",
        ),
        (
            "\t/** Logger available to subclasses. */",
            "\t/** 供子类使用的日志记录器。 */",
        ),
        (
            "\t/** The lazily initialized target object. */",
            "\t/** 延迟初始化的目标对象。 */",
        ),
        (
            "\t/**\n\t * Return whether the lazy target object of this TargetSource\n\t * has already been fetched.\n\t */",
            "\t/**\n\t * 返回本 TargetSource 的延迟目标对象是否已被获取。\n\t */",
        ),
        (
            "\t/**\n\t * This default implementation returns {@code null} if the\n\t * target is {@code null} (it is hasn't yet been initialized),\n\t * or the target class if the target has already been initialized.\n\t * <p>Subclasses may wish to override this method in order to provide\n\t * a meaningful value when the target is still {@code null}.\n\t * @see #isInitialized()\n\t */",
            "\t/**\n\t * 默认实现：若目标为 {@code null}（尚未初始化）则返回 {@code null}，\n\t * 若已初始化则返回目标类。\n\t * <p>子类可覆盖本方法，在目标仍为 {@code null} 时提供有意义的值。\n\t * @see #isInitialized()\n\t */",
        ),
        (
            "\t/**\n\t * Returns the lazy-initialized target object,\n\t * creating it on-the-fly if it doesn't exist already.\n\t * @see #createObject()\n\t */",
            "\t/**\n\t * 返回延迟初始化的目标对象；若尚不存在则即时创建。\n\t * @see #createObject()\n\t */",
        ),
        (
            "\t\t\tlogger.debug(\"Initializing lazy target object\");",
            "\t\t\tlogger.debug(\"正在初始化延迟目标对象\");",
        ),
        (
            "\t/**\n\t * Subclasses should implement this method to return the lazy initialized object.\n\t * Called the first time the proxy is invoked.\n\t * @return the created object\n\t * @throws Exception if creation failed\n\t */",
            "\t/**\n\t * 子类应实现本方法以返回延迟初始化的对象。\n\t * 在首次调用代理时触发。\n\t * @return 创建的对象\n\t * @throws Exception 若创建失败\n\t */",
        ),
    ],
    "AbstractPoolingTargetSource.java": [
        (
            "/**\n * Abstract base class for pooling {@link org.springframework.aop.TargetSource}\n * implementations which maintain a pool of target instances, acquiring and\n * releasing a target object from the pool for each method invocation.\n * This abstract base class is independent of concrete pooling technology;\n * see the subclass {@link CommonsPool2TargetSource} for a concrete example.\n *\n * <p>Subclasses must implement the {@link #getTarget} and\n * {@link #releaseTarget} methods based on their chosen object pool.\n * The {@link #newPrototypeInstance()} method inherited from\n * {@link AbstractPrototypeBasedTargetSource} can be used to create objects\n * in order to put them into the pool.\n *\n * <p>Subclasses must also implement some monitoring methods from the\n * {@link PoolingConfig} interface. The {@link #getPoolingConfigMixin()} method\n * makes these stats available on proxied objects through an IntroductionAdvisor.\n *\n * <p>This class implements the {@link org.springframework.beans.factory.DisposableBean}\n * interface in order to force subclasses to implement a {@link #destroy()}\n * method, closing down their object pool.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see #getTarget\n * @see #releaseTarget\n * @see #destroy\n */",
            "/**\n * 池化 {@link org.springframework.aop.TargetSource} 实现的抽象基类，\n * 维护目标实例池，每次方法调用时从池中获取并释放目标对象。\n * 本抽象基类与具体池化技术无关；\n * 具体示例见子类 {@link CommonsPool2TargetSource}。\n *\n * <p>子类须根据所选对象池实现 {@link #getTarget} 与\n * {@link #releaseTarget} 方法。\n * 可继承 {@link AbstractPrototypeBasedTargetSource} 的\n * {@link #newPrototypeInstance()} 方法创建对象并放入池中。\n *\n * <p>子类还须实现 {@link PoolingConfig} 接口的部分监控方法。\n * {@link #getPoolingConfigMixin()} 通过 IntroductionAdvisor\n * 在代理对象上暴露这些统计信息。\n *\n * <p>本类实现 {@link org.springframework.beans.factory.DisposableBean} 接口，\n * 强制子类实现 {@link #destroy()} 方法以关闭对象池。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see #getTarget\n * @see #releaseTarget\n * @see #destroy\n */",
        ),
        (
            "\t/** The maximum size of the pool. */",
            "\t/** 池的最大容量。 */",
        ),
        (
            "\t/**\n\t * Set the maximum size of the pool.\n\t * Default is -1, indicating no size limit.\n\t */",
            "\t/**\n\t * 设置池的最大容量。\n\t * 默认为 -1，表示无大小限制。\n\t */",
        ),
        (
            "\t/**\n\t * Return the maximum size of the pool.\n\t */",
            "\t/**\n\t * 返回池的最大容量。\n\t */",
        ),
        (
            "\t/**\n\t * Create the pool.\n\t * @throws Exception to avoid placing constraints on pooling APIs\n\t */",
            "\t/**\n\t * 创建对象池。\n\t * @throws Exception 避免对池化 API 施加约束\n\t */",
        ),
        (
            "\t/**\n\t * Acquire an object from the pool.\n\t * @return an object from the pool\n\t * @throws Exception we may need to deal with checked exceptions from pool\n\t * APIs, so we're forgiving with our exception signature\n\t */",
            "\t/**\n\t * 从池中获取对象。\n\t * @return 池中的对象\n\t * @throws Exception 池化 API 可能抛出受检异常，故签名较宽松\n\t */",
        ),
        (
            "\t/**\n\t * Return the given object to the pool.\n\t * @param target object that must have been acquired from the pool\n\t * via a call to {@code getTarget()}\n\t * @throws Exception to allow pooling APIs to throw exception\n\t * @see #getTarget\n\t */",
            "\t/**\n\t * 将给定对象归还池中。\n\t * @param target 须通过 {@code getTarget()} 从池中获取的对象\n\t * @throws Exception 允许池化 API 抛出异常\n\t * @see #getTarget\n\t */",
        ),
        (
            "\t/**\n\t * Return an IntroductionAdvisor that provides a mixin\n\t * exposing statistics about the pool maintained by this object.\n\t */",
            "\t/**\n\t * 返回提供 mixin 的 IntroductionAdvisor，\n\t * 暴露本对象维护的池统计信息。\n\t */",
        ),
    ],
    "AbstractPrototypeBasedTargetSource.java": [
        (
            "/**\n * Base class for dynamic {@link org.springframework.aop.TargetSource} implementations\n * that create new prototype bean instances to support a pooling or\n * new-instance-per-invocation strategy.\n *\n * <p>Such TargetSources must run in a {@link BeanFactory}, as it needs to\n * call the {@code getBean} method to create a new prototype instance.\n * Therefore, this base class extends {@link AbstractBeanFactoryBasedTargetSource}.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see org.springframework.beans.factory.BeanFactory#getBean\n * @see PrototypeTargetSource\n * @see ThreadLocalTargetSource\n * @see CommonsPool2TargetSource\n */",
            "/**\n * 动态 {@link org.springframework.aop.TargetSource} 实现的基类，\n * 创建新的原型 Bean 实例以支持池化或每次调用新建实例的策略。\n *\n * <p>此类 TargetSource 须在 {@link BeanFactory} 中运行，\n * 因需调用 {@code getBean} 创建新原型实例。\n * 因此本基类继承 {@link AbstractBeanFactoryBasedTargetSource}。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see org.springframework.beans.factory.BeanFactory#getBean\n * @see PrototypeTargetSource\n * @see ThreadLocalTargetSource\n * @see CommonsPool2TargetSource\n */",
        ),
        (
            "\t\t// Check whether the target bean is defined as prototype.",
            "\t\t// 检查目标 Bean 是否定义为 prototype。",
        ),
        (
            "\t/**\n\t * Subclasses should call this method to create a new prototype instance.\n\t * @throws BeansException if bean creation failed\n\t */",
            "\t/**\n\t * 子类应调用本方法创建新原型实例。\n\t * @throws BeansException 若 Bean 创建失败\n\t */",
        ),
        (
            "\t/**\n\t * Subclasses should call this method to destroy an obsolete prototype instance.\n\t * @param target the bean instance to destroy\n\t */",
            "\t/**\n\t * 子类应调用本方法销毁过时的原型实例。\n\t * @param target 待销毁的 Bean 实例\n\t */",
        ),
        (
            "\t//---------------------------------------------------------------------\n\t// Serialization support\n\t//---------------------------------------------------------------------",
            "\t//---------------------------------------------------------------------\n\t// 序列化支持\n\t//---------------------------------------------------------------------",
        ),
        (
            "\t/**\n\t * Replaces this object with a SingletonTargetSource on serialization.\n\t * Protected as otherwise it won't be invoked for subclasses.\n\t * (The {@code writeReplace()} method must be visible to the class\n\t * being serialized.)\n\t * <p>With this implementation of this method, there is no need to mark\n\t * non-serializable fields in this class or subclasses as transient.\n\t */",
            "\t/**\n\t * 序列化时用 SingletonTargetSource 替换本对象。\n\t * 设为 protected，否则子类无法调用。\n\t * （{@code writeReplace()} 须对正在序列化的类可见。）\n\t * <p>采用本实现后，无需将本类或子类中不可序列化字段标记为 transient。\n\t */",
        ),
        (
            "\t\t\t// Create disconnected SingletonTargetSource/EmptyTargetSource.",
            "\t\t\t// 创建断开的 SingletonTargetSource/EmptyTargetSource。",
        ),
    ],
    "CommonsPool2TargetSource.java": [
        (
            "/**\n * {@link org.springframework.aop.TargetSource} implementation that holds\n * objects in a configurable Apache Commons2 Pool.\n *\n * <p>By default, an instance of {@code GenericObjectPool} is created.\n * Subclasses may change the type of {@code ObjectPool} used by\n * overriding the {@code createObjectPool()} method.\n *\n * <p>Provides many configuration properties mirroring those of the Commons Pool\n * {@code GenericObjectPool} class; these properties are passed to the\n * {@code GenericObjectPool} during construction. If creating a subclass of this\n * class to change the {@code ObjectPool} implementation type, pass in the values\n * of configuration properties that are relevant to your chosen implementation.\n *\n * <p>The {@code testOnBorrow}, {@code testOnReturn} and {@code testWhileIdle}\n * properties are explicitly not mirrored because the implementation of\n * {@code PoolableObjectFactory} used by this class does not implement\n * meaningful validation. All exposed Commons Pool properties use the\n * corresponding Commons Pool defaults.\n *\n * @author Rod Johnson\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Stephane Nicoll\n * @author Kazuki Shimizu\n * @since 4.2\n * @see GenericObjectPool\n * @see #createObjectPool()\n * @see #setMaxSize\n * @see #setMaxIdle\n * @see #setMinIdle\n * @see #setMaxWait\n * @see #setTimeBetweenEvictionRunsMillis\n * @see #setMinEvictableIdleTimeMillis\n */",
            "/**\n * 在可配置的 Apache Commons2 Pool 中持有对象的\n * {@link org.springframework.aop.TargetSource} 实现。\n *\n * <p>默认创建 {@code GenericObjectPool} 实例。\n * 子类可通过覆盖 {@code createObjectPool()} 更改使用的 {@code ObjectPool} 类型。\n *\n * <p>提供与 Commons Pool {@code GenericObjectPool} 类对应的诸多配置属性；\n * 构造时传入 {@code GenericObjectPool}。\n * 若创建子类以更改 {@code ObjectPool} 实现类型，\n * 请传入与所选实现相关的配置属性值。\n *\n * <p>显式不镜像 {@code testOnBorrow}、{@code testOnReturn} 与 {@code testWhileIdle}，\n * 因本类使用的 {@code PoolableObjectFactory} 实现未提供有意义的校验。\n * 所有暴露的 Commons Pool 属性均使用对应默认值。\n *\n * @author Rod Johnson\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Stephane Nicoll\n * @author Kazuki Shimizu\n * @since 4.2\n * @see GenericObjectPool\n * @see #createObjectPool()\n * @see #setMaxSize\n * @see #setMaxIdle\n * @see #setMinIdle\n * @see #setMaxWait\n * @see #setTimeBetweenEvictionRunsMillis\n * @see #setMinEvictableIdleTimeMillis\n */",
        ),
        (
            "\t/**\n\t * The Apache Commons {@code ObjectPool} used to pool target objects.\n\t */",
            "\t/**\n\t * 用于池化目标对象的 Apache Commons {@code ObjectPool}。\n\t */",
        ),
        (
            "\t/**\n\t * Create a CommonsPoolTargetSource with default settings.\n\t * Default maximum size of the pool is 8.\n\t * @see #setMaxSize\n\t * @see GenericObjectPoolConfig#setMaxTotal\n\t */",
            "\t/**\n\t * 以默认设置创建 CommonsPoolTargetSource。\n\t * 池默认最大容量为 8。\n\t * @see #setMaxSize\n\t * @see GenericObjectPoolConfig#setMaxTotal\n\t */",
        ),
        (
            "\t/**\n\t * Set the maximum number of idle objects in the pool.\n\t * Default is 8.\n\t * @see GenericObjectPool#setMaxIdle\n\t */",
            "\t/**\n\t * 设置池中空闲对象的最大数量。\n\t * 默认为 8。\n\t * @see GenericObjectPool#setMaxIdle\n\t */",
        ),
        (
            "\t/**\n\t * Creates and holds an ObjectPool instance.\n\t * @see #createObjectPool()\n\t */",
            "\t/**\n\t * 创建并持有 ObjectPool 实例。\n\t * @see #createObjectPool()\n\t */",
        ),
        (
            "\t\tlogger.debug(\"Creating Commons object pool\");",
            "\t\tlogger.debug(\"正在创建 Commons 对象池\");",
        ),
        (
            "\t/**\n\t * Subclasses can override this if they want to return a specific Commons pool.\n\t * They should apply any configuration properties to the pool here.\n\t * <p>Default is a GenericObjectPool instance with the given pool size.\n\t * @return an empty Commons {@code ObjectPool}.\n\t * @see GenericObjectPool\n\t * @see #setMaxSize\n\t */",
            "\t/**\n\t * 子类可覆盖以返回特定 Commons 池。\n\t * 应在此将配置属性应用到池上。\n\t * <p>默认为具有给定池大小的 GenericObjectPool 实例。\n\t * @return 空的 Commons {@code ObjectPool}\n\t * @see GenericObjectPool\n\t * @see #setMaxSize\n\t */",
        ),
        (
            "\t/**\n\t * Borrows an object from the {@code ObjectPool}.\n\t */",
            "\t/**\n\t * 从 {@code ObjectPool} 借用对象。\n\t */",
        ),
        (
            "\t/**\n\t * Returns the specified object to the underlying {@code ObjectPool}.\n\t */",
            "\t/**\n\t * 将指定对象归还底层 {@code ObjectPool}。\n\t */",
        ),
        (
            "\t/**\n\t * Closes the underlying {@code ObjectPool} when destroying this object.\n\t */",
            "\t/**\n\t * 销毁本对象时关闭底层 {@code ObjectPool}。\n\t */",
        ),
        (
            "\t\t\tlogger.debug(\"Closing Commons ObjectPool\");",
            "\t\t\tlogger.debug(\"正在关闭 Commons ObjectPool\");",
        ),
        (
            "\t//----------------------------------------------------------------------------\n\t// Implementation of org.apache.commons.pool2.PooledObjectFactory interface\n\t//----------------------------------------------------------------------------",
            "\t//----------------------------------------------------------------------------\n\t// org.apache.commons.pool2.PooledObjectFactory 接口实现\n\t//----------------------------------------------------------------------------",
        ),
    ],
    "EmptyTargetSource.java": [
        (
            "/**\n * Canonical {@code TargetSource} when there is no target\n * (or just the target class known), and behavior is supplied\n * by interfaces and advisors only.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 无目标（或仅已知目标类）时的标准 {@code TargetSource}，\n * 行为完全由接口与 Advisor 提供。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/** use serialVersionUID from Spring 1.2 for interoperability. */",
            "\t/** 使用 Spring 1.2 的 serialVersionUID 以保证互操作性。 */",
        ),
        (
            "\t//---------------------------------------------------------------------\n\t// Static factory methods\n\t//---------------------------------------------------------------------",
            "\t//---------------------------------------------------------------------\n\t// 静态工厂方法\n\t//---------------------------------------------------------------------",
        ),
        (
            "\t/**\n\t * The canonical (Singleton) instance of this {@link EmptyTargetSource}.\n\t */",
            "\t/**\n\t * 本 {@link EmptyTargetSource} 的标准（单例）实例。\n\t */",
        ),
        (
            "\t/**\n\t * Return an EmptyTargetSource for the given target Class.\n\t * @param targetClass the target Class (may be {@code null})\n\t * @see #getTargetClass()\n\t */",
            "\t/**\n\t * 为给定目标 Class 返回 EmptyTargetSource。\n\t * @param targetClass 目标 Class（可为 {@code null}）\n\t * @see #getTargetClass()\n\t */",
        ),
        (
            "\t/**\n\t * Return an EmptyTargetSource for the given target Class.\n\t * @param targetClass the target Class (may be {@code null})\n\t * @param isStatic whether the TargetSource should be marked as static\n\t * @see #getTargetClass()\n\t */",
            "\t/**\n\t * 为给定目标 Class 返回 EmptyTargetSource。\n\t * @param targetClass 目标 Class（可为 {@code null}）\n\t * @param isStatic TargetSource 是否标记为 static\n\t * @see #getTargetClass()\n\t */",
        ),
        (
            "\t//---------------------------------------------------------------------\n\t// Instance implementation\n\t//---------------------------------------------------------------------",
            "\t//---------------------------------------------------------------------\n\t// 实例实现\n\t//---------------------------------------------------------------------",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@link EmptyTargetSource} class.\n\t * <p>This constructor is {@code private} to enforce the\n\t * Singleton pattern / factory method pattern.\n\t * @param targetClass the target class to expose (may be {@code null})\n\t * @param isStatic whether the TargetSource is marked as static\n\t */",
            "\t/**\n\t * 创建 {@link EmptyTargetSource} 的新实例。\n\t * <p>本构造器为 {@code private}，以强制单例/工厂方法模式。\n\t * @param targetClass 要暴露的目标类（可为 {@code null}）\n\t * @param isStatic TargetSource 是否标记为 static\n\t */",
        ),
        (
            "\t/**\n\t * Always returns the specified target Class, or {@code null} if none.\n\t */",
            "\t/**\n\t * 始终返回指定的目标 Class；若无则 {@code null}。\n\t */",
        ),
        (
            "\t/**\n\t * Always returns {@code true}.\n\t */",
            "\t/**\n\t * 始终返回 {@code true}。\n\t */",
        ),
        (
            "\t/**\n\t * Always returns {@code null}.\n\t */",
            "\t/**\n\t * 始终返回 {@code null}。\n\t */",
        ),
        (
            "\t/**\n\t * Returns the canonical instance on deserialization in case\n\t * of no target class, thus protecting the Singleton pattern.\n\t */",
            "\t/**\n\t * 在无目标类时反序列化返回标准实例，从而保护单例模式。\n\t */",
        ),
    ],
    "HotSwappableTargetSource.java": [
        (
            "/**\n * {@link org.springframework.aop.TargetSource} implementation that\n * caches a local target object, but allows the target to be swapped\n * while the application is running.\n *\n * <p>If configuring an object of this class in a Spring IoC container,\n * use constructor injection.\n *\n * <p>This TargetSource is serializable if the target is at the time\n * of serialization.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 缓存本地目标对象、但允许在应用运行期间交换目标的\n * {@link org.springframework.aop.TargetSource} 实现。\n *\n * <p>在 Spring IoC 容器中配置本类对象时，请使用构造器注入。\n *\n * <p>若目标在序列化时可序列化，则本 TargetSource 可序列化。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/** use serialVersionUID from Spring 1.2 for interoperability. */",
            "\t/** 使用 Spring 1.2 的 serialVersionUID 以保证互操作性。 */",
        ),
        (
            "\t/** The current target object. */",
            "\t/** 当前目标对象。 */",
        ),
        (
            "\t/**\n\t * Create a new HotSwappableTargetSource with the given initial target object.\n\t * @param initialTarget the initial target object\n\t */",
            "\t/**\n\t * 以给定初始目标对象创建 HotSwappableTargetSource。\n\t * @param initialTarget 初始目标对象\n\t */",
        ),
        (
            "\t/**\n\t * Return the type of the current target object.\n\t * <p>The returned type should usually be constant across all target objects.\n\t */",
            "\t/**\n\t * 返回当前目标对象的类型。\n\t * <p>返回值通常对所有目标对象保持一致。\n\t */",
        ),
        (
            "\t/**\n\t * Swap the target, returning the old target object.\n\t * @param newTarget the new target object\n\t * @return the old target object\n\t * @throws IllegalArgumentException if the new target is invalid\n\t */",
            "\t/**\n\t * 交换目标，返回旧目标对象。\n\t * @param newTarget 新目标对象\n\t * @return 旧目标对象\n\t * @throws IllegalArgumentException 若新目标无效\n\t */",
        ),
        (
            "\t/**\n\t * Two HotSwappableTargetSources are equal if the current target objects are equal.\n\t */",
            "\t/**\n\t * 若当前目标对象相等，则两个 HotSwappableTargetSource 相等。\n\t */",
        ),
    ],
    "LazyInitTargetSource.java": [
        (
            "/**\n * {@link org.springframework.aop.TargetSource} that lazily accesses a\n * singleton bean from a {@link org.springframework.beans.factory.BeanFactory}.\n *\n * <p>Useful when a proxy reference is needed on initialization but\n * the actual target object should not be initialized until first use.\n * When the target bean is defined in an\n * {@link org.springframework.context.ApplicationContext} (or a\n * {@code BeanFactory} that is eagerly pre-instantiating singleton beans)\n * it must be marked as \"lazy-init\" too, else it will be instantiated by said\n * {@code ApplicationContext} (or {@code BeanFactory}) on startup.\n * <p>For example:\n *\n * <pre class=\"code\">\n * &lt;bean id=\"serviceTarget\" class=\"example.MyService\" lazy-init=\"true\"&gt;\n *   ...\n * &lt;/bean&gt;\n *\n * &lt;bean id=\"service\" class=\"org.springframework.aop.framework.ProxyFactoryBean\"&gt;\n *   &lt;property name=\"targetSource\"&gt;\n *     &lt;bean class=\"org.springframework.aop.target.LazyInitTargetSource\"&gt;\n *       &lt;property name=\"targetBeanName\"&gt;&lt;idref local=\"serviceTarget\"/&gt;&lt;/property&gt;\n *     &lt;/bean&gt;\n *   &lt;/property&gt;\n * &lt;/bean&gt;</pre>\n *\n * The \"serviceTarget\" bean will not get initialized until a method on the\n * \"service\" proxy gets invoked.\n *\n * <p>Subclasses can extend this class and override the {@link #postProcessTargetObject(Object)} to\n * perform some additional processing with the target object when it is first loaded.\n *\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @since 1.1.4\n * @see org.springframework.beans.factory.BeanFactory#getBean\n * @see #postProcessTargetObject\n */",
            "/**\n * 从 {@link org.springframework.beans.factory.BeanFactory} 延迟访问单例 Bean 的\n * {@link org.springframework.aop.TargetSource}。\n *\n * <p>适用于初始化时需要代理引用、但目标对象应延迟到首次使用时才初始化的场景。\n * 当目标 Bean 定义在 {@link org.springframework.context.ApplicationContext}（或\n * 会 eagerly 预实例化单例的 {@code BeanFactory}）中时，\n * 也必须标记为 \"lazy-init\"，否则会在启动时被实例化。\n * <p>例如：\n *\n * <pre class=\"code\">\n * &lt;bean id=\"serviceTarget\" class=\"example.MyService\" lazy-init=\"true\"&gt;\n *   ...\n * &lt;/bean&gt;\n *\n * &lt;bean id=\"service\" class=\"org.springframework.aop.framework.ProxyFactoryBean\"&gt;\n *   &lt;property name=\"targetSource\"&gt;\n *     &lt;bean class=\"org.springframework.aop.target.LazyInitTargetSource\"&gt;\n *       &lt;property name=\"targetBeanName\"&gt;&lt;idref local=\"serviceTarget\"/&gt;&lt;/property&gt;\n *     &lt;/bean&gt;\n *   &lt;/property&gt;\n * &lt;/bean&gt;</pre>\n *\n * \"serviceTarget\" Bean 直到调用 \"service\" 代理上的方法时才会初始化。\n *\n * <p>子类可扩展本类并覆盖 {@link #postProcessTargetObject(Object)}，\n * 在目标对象首次加载时执行额外处理。\n *\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @since 1.1.4\n * @see org.springframework.beans.factory.BeanFactory#getBean\n * @see #postProcessTargetObject\n */",
        ),
        (
            "\t/**\n\t * Subclasses may override this method to perform additional processing on\n\t * the target object when it is first loaded.\n\t * @param targetObject the target object that has just been instantiated (and configured)\n\t */",
            "\t/**\n\t * 子类可覆盖本方法，在目标对象首次加载时执行额外处理。\n\t * @param targetObject 刚实例化（并完成配置）的目标对象\n\t */",
        ),
    ],
    "PoolingConfig.java": [
        (
            "/**\n * Config interface for a pooling target source.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 池化 TargetSource 的配置接口。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/**\n\t * Return the maximum size of the pool.\n\t */",
            "\t/**\n\t * 返回池的最大容量。\n\t */",
        ),
        (
            "\t/**\n\t * Return the number of active objects in the pool.\n\t * @throws UnsupportedOperationException if not supported by the pool\n\t */",
            "\t/**\n\t * 返回池中活跃对象数量。\n\t * @throws UnsupportedOperationException 若池不支持\n\t */",
        ),
        (
            "\t/**\n\t * Return the number of idle objects in the pool.\n\t * @throws UnsupportedOperationException if not supported by the pool\n\t */",
            "\t/**\n\t * 返回池中空闲对象数量。\n\t * @throws UnsupportedOperationException 若池不支持\n\t */",
        ),
    ],
}
