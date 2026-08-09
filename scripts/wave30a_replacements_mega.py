"""Chinese JavaDoc replacements for Spring Framework 7.0.8 wave30a mega batch [0:10]."""

MEGA_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractApplicationEventMulticaster.java": [
        (
            '/**\n * Abstract implementation of the {@link ApplicationEventMulticaster} interface,\n * providing the basic listener registration facility.\n *\n * <p>Doesn\'t permit multiple instances of the same listener by default,\n * as it keeps listeners in a linked Set. The collection class used to hold\n * ApplicationListener objects can be overridden through the "collectionClass"\n * bean property.\n *\n * <p>Implementing ApplicationEventMulticaster\'s actual {@link #multicastEvent} method\n * is left to subclasses. {@link SimpleApplicationEventMulticaster} simply multicasts\n * all events to all registered listeners, invoking them in the calling thread by\n * default. Alternative implementations could be more sophisticated in those respects.\n *\n * @author Juergen Hoeller\n * @author Stephane Nicoll\n * @since 1.2.3\n * @see #getApplicationListeners(ApplicationEvent, ResolvableType)\n * @see SimpleApplicationEventMulticaster\n */',
            '/**\n * {@link ApplicationEventMulticaster} 的抽象实现，提供基本监听器注册能力。\n *\n * <p>默认使用 LinkedHashSet 存储监听器，不允许同一监听器多次注册；\n * 可通过 "collectionClass" 属性覆盖集合类型。\n *\n * <p>{@link #multicastEvent} 的具体分发策略由子类实现。\n * {@link SimpleApplicationEventMulticaster} 默认在调用线程向所有监听器广播。\n *\n * @author Juergen Hoeller\n * @author Stephane Nicoll\n * @since 1.2.3\n * @see #getApplicationListeners(ApplicationEvent, ResolvableType)\n * @see SimpleApplicationEventMulticaster\n */',
        ),
        (
            'class `AbstractApplicationEventMulticaster`：请结合所属模块与调用方理解其在整体架构中的职责。',
            'class `AbstractApplicationEventMulticaster`：应用事件广播器的抽象基类，管理监听器注册与按事件类型检索监听器。',
        ),
    ],
    "ApplicationListenerMethodAdapter.java": [
        (
            '/**\n * {@link GenericApplicationListener} adapter that delegates the processing of\n * an event to an {@link EventListener} annotated method.\n *\n * <p>Delegates to {@link #processEvent(ApplicationEvent)} to give subclasses\n * a chance to deviate from the default. Unwraps the content of a\n * {@link PayloadApplicationEvent} if necessary to allow a method declaration\n * to define any arbitrary event type. If a condition is defined, it is\n * evaluated prior to invoking the underlying method.\n *\n * @author Stephane Nicoll\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @author Sebastien Deleuze\n * @author Yanming Zhou\n * @since 4.2\n */',
            '/**\n * 将事件处理委托给 {@link EventListener} 标注方法的 {@link GenericApplicationListener} 适配器。\n *\n * <p>通过 {@link #processEvent(ApplicationEvent)} 委托，便于子类覆盖默认行为。\n * 必要时解包 {@link PayloadApplicationEvent} 内容，使方法可声明任意事件类型。\n * 若定义了 condition，则在调用目标方法前评估。\n *\n * @author Stephane Nicoll\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @author Sebastien Deleuze\n * @author Yanming Zhou\n * @since 4.2\n */',
        ),
        (
            '/**\n\t * Construct a new ApplicationListenerMethodAdapter.\n\t * @param beanName the name of the bean to invoke the listener method on\n\t * @param targetClass the target class that the method is declared on\n\t * @param method the listener method to invoke\n\t */',
            '/**\n * 构造 ApplicationListenerMethodAdapter。\n * @param beanName 要调用监听器方法的 Bean 名称\n * @param targetClass 声明该方法的目标类\n * @param method 要调用的监听器方法\n */',
        ),
        (
            '/**\n\t * Return the target listener method.\n\t * @since 7.0.7 in public form (with protected visibility since 5.3)\n\t */',
            '/**\n * 返回目标监听器方法。\n * @since 7.0.7 in public form (with protected visibility since 5.3)\n */',
        ),
        (
            '/**\n\t * Return the condition to use.\n\t * <p>Matches the {@code condition} attribute of the {@link EventListener}\n\t * annotation or any matching attribute on a composed annotation that\n\t * is meta-annotated with {@code @EventListener}.\n\t */',
            '/**\n * 返回要使用的 condition 表达式。\n * <p>对应 {@link EventListener} 的 {@code condition} 属性，\n * 或 meta-annotated {@code @EventListener} 组合注解中的匹配属性。\n */',
        ),
        (
            '/**\n\t * Return whether default execution is applicable for the target listener.\n\t * @since 6.2\n\t * @see #onApplicationEvent\n\t * @see EventListener#defaultExecution()\n\t */',
            '/**\n * 返回目标监听器是否适用默认执行路径。\n * @since 6.2\n * @see #onApplicationEvent\n * @see EventListener#defaultExecution()\n */',
        ),
        (
            '/**\n\t * Initialize this instance.\n\t */',
            '/** 初始化此实例。 */',
        ),
        (
            '/**\n\t * Determine the default id for the target listener, to be applied in case of\n\t * no {@link EventListener#id() annotation-specified id value}.\n\t * <p>The default implementation builds a method name with parameter types.\n\t * @since 5.3.5\n\t * @see #getListenerId()\n\t */',
            '/**\n * 确定目标监听器的默认 id（未指定 {@link EventListener#id()} 时使用）。\n * <p>默认实现使用方法名加参数类型构建。\n * @since 5.3.5\n * @see #getListenerId()\n */',
        ),
        (
            '/**\n\t * Process the specified {@link ApplicationEvent}, checking if the condition\n\t * matches and handling a non-null result, if any.\n\t * @param event the event to process through the listener method\n\t */',
            '/**\n * 处理指定 {@link ApplicationEvent}：检查 condition 是否匹配并处理非空返回值。\n * @param event 要通过监听器方法处理的事件\n */',
        ),
        (
            '/**\n\t * Determine whether the listener method would actually handle the given\n\t * event, checking if the condition matches.\n\t * @param event the event to process through the listener method\n\t * @since 6.1\n\t */',
            '/**\n * 判断监听器方法是否真的会处理给定事件（检查 condition）。\n * @param event 要通过监听器方法处理的事件\n * @since 6.1\n */',
        ),
        (
            '/**\n\t * Resolve the method arguments to use for the specified {@link ApplicationEvent}.\n\t * <p>These arguments will be used to invoke the method handled by this instance.\n\t * Can return {@code null} to indicate that no suitable arguments could be resolved\n\t * and therefore the method should not be invoked at all for the specified event.\n\t */',
            '/**\n * 解析指定 {@link ApplicationEvent} 的方法参数。\n * <p>这些参数用于调用本实例包装的方法。\n * 若无法解析合适参数可返回 {@code null}，表示不应调用该方法。\n */',
        ),
        (
            '/**\n\t * Invoke the event listener method with the given argument values.\n\t */',
            '/** 使用给定参数值调用事件监听器方法。 */',
        ),
        (
            '/**\n\t * Return the target bean instance to use.\n\t */',
            '/** 返回要使用的目标 Bean 实例。 */',
        ),
        (
            '/**\n\t * Add additional details such as the bean type and method signature to\n\t * the given error message.\n\t * @param message error message to append the HandlerMethod details to\n\t */',
            '/**\n * 向给定错误消息追加 Bean 类型、方法签名等详细信息。\n * @param message 要追加 HandlerMethod 详情的错误消息\n */',
        ),
        (
            '/**\n\t * Assert that the target bean class is an instance of the class where the given\n\t * method is declared. In some cases the actual bean instance at event-\n\t * processing time may be a JDK dynamic proxy (lazy initialization, prototype\n\t * beans, and others). Event listener beans that require proxying should prefer\n\t * class-based proxy mechanisms.\n\t */',
            '/**\n * 断言目标 Bean 类是声明该方法的类的实例。\n * <p>事件处理时实际 Bean 可能是 JDK 动态代理（lazy、prototype 等），\n * 需要代理的监听器 Bean 应优先使用基于类的代理机制。\n */',
        ),
        (
            '/**\n\t * Reactive Streams Subscriber for publishing follow-up events.\n\t */',
            '/**\n * Reactive Streams Subscriber，用于发布后续事件。\n */',
        ),
    ],
    "CacheAspectSupport.java": [
        (
            '/**\n * Base class for caching aspects, such as the {@link CacheInterceptor} or an\n * AspectJ aspect.\n *\n * <p>This enables the underlying Spring caching infrastructure to be used easily\n * to implement an aspect for any aspect system.\n *\n * <p>Subclasses are responsible for calling relevant methods in the correct order.\n *\n * <p>Uses the <b>Strategy</b> design pattern. A {@link CacheOperationSource} is\n * used for determining caching operations, a {@link KeyGenerator} will build the\n * cache keys, and a {@link CacheResolver} will resolve the actual cache(s) to use.\n *\n * <p>Note: A cache aspect is serializable but does not perform any actual caching\n * after deserialization.\n *\n * @author Costin Leau\n * @author Juergen Hoeller\n * @author Chris Beams\n * @author Phillip Webb\n * @author Sam Brannen\n * @author Stephane Nicoll\n * @author Sebastien Deleuze\n * @since 3.1\n */',
            '/**\n * 缓存切面的基类，例如 {@link CacheInterceptor} 或 AspectJ 切面。\n *\n * <p>便于底层 Spring 缓存基础设施为任意切面系统实现缓存切面。\n *\n * <p>子类负责按正确顺序调用相关方法。\n *\n * <p>采用<b>策略</b>设计模式：{@link CacheOperationSource} 决定缓存操作，\n * {@link KeyGenerator} 构建缓存键，{@link CacheResolver} 解析实际使用的 Cache。\n *\n * <p>注意：缓存切面可序列化，但反序列化后不再执行实际缓存。\n *\n * @author Costin Leau\n * @author Juergen Hoeller\n * @author Chris Beams\n * @author Phillip Webb\n * @author Sam Brannen\n * @author Stephane Nicoll\n * @author Sebastien Deleuze\n * @since 3.1\n */',
        ),
        (
            '/**\n\t * Inner class to avoid a hard dependency on the Reactive Streams API at runtime.\n\t */',
            '/**\n * 内部类，避免在运行时硬依赖 Reactive Streams API。\n */',
        ),
        (
            '/**\n\t * Reactive Streams Subscriber for exhausting the Flux and collecting a List\n\t * to cache.\n\t */',
            '/**\n * Reactive Streams Subscriber，用于消费 Flux 并收集为 List 以写入缓存。\n */',
        ),
        (
            '/**\n\t * Metadata of a cache operation that does not depend on a particular invocation\n\t * which makes it a good candidate for caching.\n\t */',
            '/**\n * 不依赖特定调用的缓存操作元数据，适合缓存复用。\n */',
        ),
        (
            '/**\n\t * A {@link CacheOperationInvocationContext} context for a {@link CacheOperation}.\n\t */',
            '/**\n * {@link CacheOperation} 对应的 {@link CacheOperationInvocationContext} 上下文。\n */',
        ),
        (
            'class `CacheAspectSupport`：请结合所属模块与调用方理解其在整体架构中的职责。',
            'class `CacheAspectSupport`：Spring 缓存 AOP 的核心基类，协调 CacheOperationSource、KeyGenerator、CacheResolver 完成 @Cacheable/@CachePut/@CacheEvict 拦截。',
        ),
    ],
    "CommonAnnotationBeanPostProcessor.java": [
        (
            '/**\n * {@link org.springframework.beans.factory.config.BeanPostProcessor} implementation\n * that supports common Java annotations out of the box, in particular the common\n * annotations in the {@code jakarta.annotation} package. These common Java\n * annotations are supported in many Jakarta EE technologies (for example, JSF and JAX-RS).\n *\n * <p>This post-processor includes support for the {@link jakarta.annotation.PostConstruct}\n * and {@link jakarta.annotation.PreDestroy} annotations - as init annotation\n * and destroy annotation, respectively - through inheriting from\n * {@link InitDestroyAnnotationBeanPostProcessor} with pre-configured annotation types.\n *\n * <p>The central element is the {@link jakarta.annotation.Resource} annotation\n * for annotation-driven injection of named beans, by default from the containing\n * Spring BeanFactory, with only {@code mappedName} references resolved in JNDI.\n * The {@link #setAlwaysUseJndiLookup "alwaysUseJndiLookup" flag} enforces JNDI lookups\n * equivalent to standard Jakarta EE resource injection for {@code name} references\n * and default names as well. The target beans can be simple POJOs, with no special\n * requirements other than the type having to match.\n *\n * <p>This post-processor also supports the EJB {@link jakarta.ejb.EJB} annotation,\n * analogous to {@link jakarta.annotation.Resource}, with the capability to\n * specify both a local bean name and a global JNDI name for fallback retrieval.\n * The target beans can be plain POJOs as well as EJB Session Beans in this case.\n *\n * <p>For default usage, resolving resource names as Spring bean names,\n * simply define the following in your application context:\n *\n * <pre class="code">\n * &lt;bean class="org.springframework.context.annotation.CommonAnnotationBeanPostProcessor"/&gt;</pre>\n *\n * For direct JNDI access, resolving resource names as JNDI resource references\n * within the Jakarta EE application\'s "java:comp/env/" namespace, use the following:\n *\n * <pre class="code">\n * &lt;bean class="org.springframework.context.annotation.CommonAnnotationBeanPostProcessor"&gt;\n *   &lt;property name="alwaysUseJndiLookup" value="true"/&gt;\n * &lt;/bean&gt;</pre>\n *\n * {@code mappedName} references will always be resolved in JNDI,\n * allowing for global JNDI names (including "java:" prefix) as well. The\n * "alwaysUseJndiLookup" flag just affects {@code name} references and\n * default names (inferred from the field name / property name).\n *\n * <p><b>NOTE:</b> A default CommonAnnotationBeanPostProcessor will be registered\n * by the "context:annotation-config" and "context:component-scan" XML tags.\n * Remove or turn off the default annotation configuration there if you intend\n * to specify a custom CommonAnnotationBeanPostProcessor bean definition!\n * <p><b>NOTE:</b> Annotation injection will be performed <i>before</i> XML injection;\n * thus the latter configuration will override the former for properties wired through\n * both approaches.\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.5\n * @see #setAlwaysUseJndiLookup\n * @see #setResourceFactory\n * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor\n * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor\n */',
            '/**\n * 开箱即用支持常见 Java 注解的\n * {@link org.springframework.beans.factory.config.BeanPostProcessor} 实现，\n * 尤其是 {@code jakarta.annotation} 包中的通用注解。\n * 这些注解在多种 Jakarta EE 技术（如 JSF、JAX-RS）中广泛使用。\n *\n * <p>本后处理器通过继承 {@link InitDestroyAnnotationBeanPostProcessor}\n * 并预配置注解类型，支持 {@link jakarta.annotation.PostConstruct} 与\n * {@link jakarta.annotation.PreDestroy} 分别作为 init/destroy 注解。\n *\n * <p>核心是对 {@link jakarta.annotation.Resource} 的支持：按名称注入 Bean，\n * 默认从当前 Spring BeanFactory 解析，仅 {@code mappedName} 走 JNDI。\n * {@link #setAlwaysUseJndiLookup "alwaysUseJndiLookup" 标志} 可强制对 {@code name}\n * 及默认名称也执行 JNDI 查找，等效于标准 Jakarta EE 资源注入。\n *\n * <p>同样支持 EJB {@link jakarta.ejb.EJB} 注解，可指定本地 Bean 名与全局 JNDI 名作为回退。\n *\n * <p>默认用法（资源名解析为 Spring Bean 名）：\n *\n * <pre class="code">\n * &lt;bean class="org.springframework.context.annotation.CommonAnnotationBeanPostProcessor"/&gt;</pre>\n *\n * 直接 JNDI 访问时：\n *\n * <pre class="code">\n * &lt;bean class="org.springframework.context.annotation.CommonAnnotationBeanPostProcessor"&gt;\n *   &lt;property name="alwaysUseJndiLookup" value="true"/&gt;\n * &lt;/bean&gt;</pre>\n *\n * <p><b>注意：</b>{@code context:annotation-config} 与 {@code context:component-scan}\n * 会注册默认实例；若需自定义定义请移除默认配置。\n * 注解注入<b>先于</b> XML 注入，后者可覆盖前者。\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.5\n * @see #setAlwaysUseJndiLookup\n * @see #setResourceFactory\n * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor\n */',
        ),
        (
            '/**\n\t * Obtain a resource object for the given name and type through autowiring\n\t * based on the given factory.\n\t * @param factory the factory to autowire against\n\t * @param element the descriptor for the annotated field/method\n\t * @param requestingBeanName the name of the requesting bean\n\t * @return the resource object (never {@code null})\n\t * @throws NoSuchBeanDefinitionException if no corresponding target resource found\n\t */',
            '/**\n * 通过给定工厂的自动装配获取指定名称与类型的资源对象。\n * @param factory 用于自动装配的工厂\n * @param element 标注字段/方法的描述符\n * @param requestingBeanName 请求 Bean 的名称\n */',
        ),
        (
            '/**\n\t * Class representing generic injection information about an annotated field\n\t * or setter method, supporting @Resource and related annotations.\n\t */',
            '/**\n * 表示标注字段或 setter 方法上通用注入信息的类，支持 @Resource 及相关注解。\n */',
        ),
        (
            '/**\n\t * Class representing injection information about an annotated field\n\t * or setter method, supporting the @Resource annotation.\n\t */',
            '/**\n * 表示 @Resource 标注字段或 setter 方法注入信息的类。\n */',
        ),
        (
            '/**\n\t * Class representing injection information about an annotated field\n\t * or setter method, supporting the @EJB annotation.\n\t */',
            '/**\n * 表示 @EJB 标注字段或 setter 方法注入信息的类。\n */',
        ),
        (
            '/**\n\t * {@link BeanRegistrationAotContribution} to inject resources on fields and methods.\n\t */',
            '/**\n * 在字段与方法上注入资源的 {@link BeanRegistrationAotContribution}。\n */',
        ),
        (
            '处理器：容器生命周期中的扩展钩子',
            'Bean 后处理器：解析 @Resource/@EJB/@PostConstruct/@PreDestroy 等 Jakarta 通用注解并完成注入或生命周期回调',
        ),
    ],
    "ConfigurationClassBeanDefinitionReader.java": [
        (
            '/**\n * Reads a given fully-populated set of ConfigurationClass instances, registering bean\n * definitions with the given {@link BeanDefinitionRegistry} based on its contents.\n *\n * <p>This class was modeled after the {@link BeanDefinitionReader} hierarchy, but does\n * not implement/extend any of its artifacts as a set of configuration classes is not a\n * {@link Resource}.\n *\n * @author Chris Beams\n * @author Juergen Hoeller\n * @author Phillip Webb\n * @author Sam Brannen\n * @author Sebastien Deleuze\n * @since 3.0\n * @see ConfigurationClassParser\n */',
            '/**\n * 读取已完整解析的 {@link ConfigurationClass} 集合，\n * 并根据其内容向 {@link BeanDefinitionRegistry} 注册 Bean 定义。\n *\n * <p>建模自 {@link BeanDefinitionReader} 层次，但配置类集合并非 {@link Resource}，\n * 故不实现/继承该层次中的类型。\n *\n * @author Chris Beams\n * @author Juergen Hoeller\n * @author Phillip Webb\n * @author Sam Brannen\n * @author Sebastien Deleuze\n * @since 3.0\n * @see ConfigurationClassParser\n */',
        ),
        (
            "/**\n\t * Evaluate {@code @Conditional} annotations, tracking results and taking into\n\t * account 'imported by'.\n\t */",
            '/**\n * 评估 {@code @Conditional} 注解，跟踪结果并考虑 "imported by" 关系。\n */',
        ),
        (
            'Bean 定义元数据：描述如何创建与装配一个 Bean；源文件: `spring-context/src/main/java/org/springframework/context/annotation/ConfigurationClassBeanDefinitionReader.java`',
            '将已解析的 @Configuration 类（含 @Bean、@Import 等）转化为 BeanDefinition 并注册到容器；源文件: `spring-context/src/main/java/org/springframework/context/annotation/ConfigurationClassBeanDefinitionReader.java`',
        ),
        (
            'Bean 定义元数据：描述如何创建与装配一个 Bean；源文件: `spring-context/src/main/java/org/springframework/context/annotation/ConfigurationClassBeanDefinitionReader.java`',
            '标记自 @Configuration 创建的 RootBeanDefinition，用于 Bean 覆盖场景下区分配置源；源文件: `spring-context/src/main/java/org/springframework/context/annotation/ConfigurationClassBeanDefinitionReader.java`',
        ),
    ],
    "ConfigurationClassEnhancer.java": [
        (
            '/**\n * Enhances {@link Configuration} classes by generating a CGLIB subclass which\n * interacts with the Spring container to respect bean scoping semantics for\n * {@code @Bean} methods. Each such {@code @Bean} method will be overridden in\n * the generated subclass, only delegating to the actual {@code @Bean} method\n * implementation if the container actually requests the construction of a new\n * instance. Otherwise, a call to such an {@code @Bean} method serves as a\n * reference back to the container, obtaining the corresponding bean by name.\n *\n * @author Chris Beams\n * @author Juergen Hoeller\n * @since 3.0\n * @see #enhance\n * @see ConfigurationClassPostProcessor\n */',
            '/**\n * 通过生成 CGLIB 子类增强 {@link Configuration} 类，\n * 使其与 Spring 容器协作以遵守 {@code @Bean} 方法的作用域语义。\n * 生成的子类会覆盖每个 {@code @Bean} 方法：仅当容器确实需要新实例时才\n * 委托给真实实现；否则调用返回容器中同名 Bean 的引用。\n *\n * @author Chris Beams\n * @author Juergen Hoeller\n * @since 3.0\n * @see #enhance\n * @see ConfigurationClassPostProcessor\n */',
        ),
        (
            '/**\n\t * Loads the specified class and generates a CGLIB subclass of it equipped with\n\t * container-aware callbacks capable of respecting scoping and other bean semantics.\n\t * @return the enhanced subclass\n\t */',
            '/**\n * 加载指定类并生成带容器感知回调的 CGLIB 子类，以尊重作用域等 Bean 语义。\n * @return 增强后的子类\n */',
        ),
        (
            '/**\n\t * Checks whether the given config class relies on package visibility, either for\n\t * the class and any of its constructors or for any of its {@code @Bean} methods.\n\t */',
            '/**\n * 检查给定配置类是否依赖包可见性（类/构造器或 {@code @Bean} 方法）。\n */',
        ),
        (
            '/**\n\t * Marker interface to be implemented by all @Configuration CGLIB subclasses.\n\t * Facilitates idempotent behavior for {@link ConfigurationClassEnhancer#enhance}\n\t * through checking to see if candidate classes are already assignable to it.\n\t * <p>Also extends {@link BeanFactoryAware}, as all enhanced {@code @Configuration}\n\t * classes require access to the {@link BeanFactory} that created them.\n\t * <p>Note that this interface is intended for framework-internal use only, however\n\t * must remain public in order to allow access to subclasses generated from other\n\t * packages (i.e. user code).\n\t */',
            '/**\n * 所有 @Configuration CGLIB 子类应实现的标记接口，\n * 便于 {@link ConfigurationClassEnhancer#enhance} 幂等检测。\n * <p>同时扩展 {@link BeanFactoryAware}，增强的配置类需要访问创建它的 {@link BeanFactory}。\n */',
        ),
        (
            '/**\n\t * Conditional {@link Callback}.\n\t * @see ConditionalCallbackFilter\n\t */',
            '/**\n * 条件 {@link Callback}。\n * @see ConditionalCallbackFilter\n */',
        ),
        (
            '/**\n\t * A {@link CallbackFilter} that works by interrogating {@link Callback Callbacks} in the order\n\t * that they are defined via {@link ConditionalCallback}.\n\t */',
            '/**\n * 按 {@link ConditionalCallback} 定义顺序询问各 {@link Callback} 的 {@link CallbackFilter}。\n */',
        ),
        (
            "/**\n\t * Custom extension of CGLIB's DefaultGeneratorStrategy, introducing a {@link BeanFactory} field.\n\t * Also exposes the application ClassLoader as thread context ClassLoader for the time of\n\t * class generation (in order for ASM to pick it up when doing common superclass resolution).\n\t */",
            '/**\n * CGLIB DefaultGeneratorStrategy 的扩展，引入 {@link BeanFactory} 字段；\n * 生成类期间将应用 ClassLoader 设为线程上下文 ClassLoader（供 ASM 解析公共超类）。\n */',
        ),
        (
            '/**\n\t * Intercepts the invocation of any {@link BeanFactoryAware#setBeanFactory(BeanFactory)} on\n\t * {@code @Configuration} class instances for the purpose of recording the {@link BeanFactory}.\n\t * @see EnhancedConfiguration\n\t */',
            '/**\n * 拦截 {@code @Configuration} 实例上对 {@link BeanFactoryAware#setBeanFactory(BeanFactory)} 的调用以记录 {@link BeanFactory}。\n * @see EnhancedConfiguration\n */',
        ),
        (
            '/**\n\t * Intercepts the invocation of any {@link Bean}-annotated methods in order to ensure proper\n\t * handling of bean semantics such as scoping and AOP proxying.\n\t * @see Bean\n\t * @see ConfigurationClassEnhancer\n\t */',
            '/**\n * 拦截所有 {@link Bean} 标注方法的调用，确保正确处理作用域与 AOP 代理等 Bean 语义。\n * @see Bean\n * @see ConfigurationClassEnhancer\n */',
        ),
        (
            'class `ConfigurationClassEnhancer`：请结合所属模块与调用方理解其在整体架构中的职责。',
            'class `ConfigurationClassEnhancer`：用 CGLIB 增强 @Configuration 类，使 @Bean 方法调用走容器单例/作用域语义而非直接 new。',
        ),
    ],
    "MBeanClientInterceptor.java": [
        (
            '/**\n * {@link org.aopalliance.intercept.MethodInterceptor} that routes calls to an\n * MBean running on the supplied {@code MBeanServerConnection}.\n * Works for both local and remote {@code MBeanServerConnection}s.\n *\n * <p>By default, the {@code MBeanClientInterceptor} will connect to the\n * {@code MBeanServer} and cache MBean metadata at startup. This can\n * be undesirable when running against a remote {@code MBeanServer}\n * that may not be running when the application starts. Through setting the\n * {@link #setConnectOnStartup(boolean) connectOnStartup} property to "false",\n * you can defer this process until the first invocation against the proxy.\n *\n * <p>This functionality is usually used through {@link MBeanProxyFactoryBean}.\n * See the javadoc of that class for more information.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 1.2\n * @see MBeanProxyFactoryBean\n * @see #setConnectOnStartup\n */',
            '/**\n * 将调用路由到给定 {@code MBeanServerConnection} 上 MBean 的\n * {@link org.aopalliance.intercept.MethodInterceptor}，支持本地与远程连接。\n *\n * <p>默认启动时连接 {@code MBeanServer} 并缓存 MBean 元数据；\n * 远程服务器可能未启动时可设 {@link #setConnectOnStartup(boolean)} 为 false 延迟连接。\n *\n * <p>通常通过 {@link MBeanProxyFactoryBean} 使用。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 1.2\n * @see MBeanProxyFactoryBean\n * @see #setConnectOnStartup\n */',
        ),
        (
            '/**\n\t * Simple wrapper class around a method name and its signature.\n\t * Used as the key when caching methods.\n\t */',
            '/**\n * 方法名与其签名的简单包装类，用作方法缓存的键。\n */',
        ),
        (
            '/**\n\t\t * Create a new instance of {@code MethodCacheKey} with the supplied\n\t\t * method name and parameter list.\n\t\t * @param name the name of the method\n\t\t * @param parameterTypes the arguments in the method signature\n\t\t */',
            '/**\n * 用给定方法名与参数列表创建 {@code MethodCacheKey}。\n * @param name 方法名\n * @param parameterTypes 方法签名中的参数类型\n */',
        ),
        (
            '拦截器：调用链中的前置/后置逻辑',
            'AOP 拦截器：将本地方法调用转发到远程或本地 MBeanServerConnection 上的 MBean 操作',
        ),
    ],
    "MBeanExporter.java": [
        (
            '/**\n * JMX exporter that allows for exposing any <i>Spring-managed bean</i> to a\n * JMX {@link javax.management.MBeanServer}, without the need to define any\n * JMX-specific information in the bean classes.\n *\n * <p>If a bean implements one of the JMX management interfaces, MBeanExporter can\n * simply register the MBean with the server through its auto-detection process.\n *\n * <p>If a bean does not implement one of the JMX management interfaces, MBeanExporter\n * will create the management information using the supplied {@link MBeanInfoAssembler}.\n *\n * <p>A list of {@link MBeanExporterListener MBeanExporterListeners} can be registered\n * via the {@link #setListeners(MBeanExporterListener[]) listeners} property, allowing\n * application code to be notified of MBean registration and unregistration events.\n *\n * <p>This exporter is compatible with MBeans as well as MXBeans.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Rick Evans\n * @author Mark Fisher\n * @author Stephane Nicoll\n * @author Sam Brannen\n * @since 1.2\n * @see #setBeans\n * @see #setAutodetect\n * @see #setAssembler\n * @see #setListeners\n * @see org.springframework.jmx.export.assembler.MBeanInfoAssembler\n * @see MBeanExporterListener\n */',
            '/**\n * JMX 导出器，可将任意<i>Spring 管理的 Bean</i>暴露到 JMX {@link javax.management.MBeanServer}，\n * 无需在 Bean 类中定义 JMX 专用信息。\n *\n * <p>若 Bean 已实现 JMX 管理接口，可通过自动检测直接注册。\n * <p>否则由 {@link MBeanInfoAssembler} 生成管理信息。\n * <p>可通过 {@link #setListeners(MBeanExporterListener[]) listeners} 注册\n * {@link MBeanExporterListener} 以接收注册/注销事件。\n * <p>兼容 MBean 与 MXBean。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Rick Evans\n * @author Mark Fisher\n * @author Stephane Nicoll\n * @since 1.2\n * @see #setBeans\n * @see #setAutodetect\n * @see #setAssembler\n * @see #setNamingStrategy\n * @see org.springframework.jmx.export.assembler.MBeanInfoAssembler\n * @see org.springframework.jmx.export.naming.ObjectNamingStrategy\n */',
        ),
        (
            '/**\n\t * Extension of {@link LazyInitTargetSource} that will inject a\n\t * {@link org.springframework.jmx.export.notification.NotificationPublisher}\n\t * into the lazy resource as it is created if required.\n\t */',
            '/**\n * {@link LazyInitTargetSource} 的扩展，在创建 lazy 资源时按需注入\n * {@link org.springframework.jmx.export.notification.NotificationPublisher}。\n */',
        ),
        (
            'class `MBeanExporter`：请结合所属模块与调用方理解其在整体架构中的职责。',
            'class `MBeanExporter`：Spring JMX 导出核心，将容器 Bean 注册为 MBean/MXBean 并管理 ObjectName 与生命周期。',
        ),
    ],
    "PostProcessorRegistrationDelegate.java": [
        (
            "/**\n * Delegate for AbstractApplicationContext's post-processor handling.\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @author Stephane Nicoll\n * @since 4.0\n */",
            '/**\n * {@link AbstractApplicationContext} 后处理器处理的委托类。\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @author Stephane Nicoll\n * @since 4.0\n */',
        ),
        (
            '/**\n\t * Load and sort the post-processors of the specified type.\n\t * @param beanFactory the bean factory to use\n\t * @param beanPostProcessorType the post-processor type\n\t * @param <T> the post-processor type\n\t * @return a list of sorted post-processors for the specified type\n\t */',
            '/**\n * 加载并排序指定类型的后处理器。\n * @param beanFactory Bean 工厂\n * @param beanFactoryPostProcessors 已知的 BFPP 实例\n */',
        ),
        (
            '处理器：容器生命周期中的扩展钩子',
            '内部委托类：按 PriorityOrdered/Ordered 契约有序调用 BeanFactoryPostProcessor 与 BeanPostProcessor',
        ),
    ],
    "ReloadableResourceBundleMessageSource.java": [
        (
            '/**\n * Spring-specific {@link org.springframework.context.MessageSource} implementation\n * that accesses resource bundles using specified basenames, participating in the\n * Spring {@link org.springframework.context.ApplicationContext}\'s resource loading.\n *\n * <p>In contrast to the JDK-based {@link ResourceBundleMessageSource}, this class uses\n * {@link java.util.Properties} instances as its custom data structure for messages,\n * loading them via a {@link org.springframework.util.PropertiesPersister} strategy\n * from Spring {@link Resource} handles. This strategy is not only capable of\n * reloading files based on timestamp changes, but also of loading properties files\n * with a specific character encoding. It will detect XML property files as well.\n *\n * <p>Note that the basenames set as {@link #setBasenames "basenames"} property\n * are treated in a slightly different fashion than the "basenames" property of\n * {@link ResourceBundleMessageSource}. It follows the basic ResourceBundle rule of not\n * specifying file extension or language codes, but can refer to any Spring resource\n * location (instead of being restricted to classpath resources). With a "classpath:"\n * prefix, resources can still be loaded from the classpath, but "cacheSeconds" values\n * other than "-1" (caching forever) might not work reliably in this case.\n *\n * <p>For a typical web application, message files could be placed in {@code WEB-INF}:\n * for example, a "WEB-INF/messages" basename would find a "WEB-INF/messages.properties",\n * "WEB-INF/messages_en.properties" etc arrangement as well as "WEB-INF/messages.xml",\n * "WEB-INF/messages_en.xml" etc. Note that message definitions in a <i>previous</i>\n * resource bundle will override ones in a later bundle, due to sequential lookup.\n\n * <p>This MessageSource can easily be used outside an\n * {@link org.springframework.context.ApplicationContext}: it will use a\n * {@link org.springframework.core.io.DefaultResourceLoader} as default,\n * simply getting overridden with the ApplicationContext\'s resource loader\n * if running in a context. It does not have any other specific dependencies.\n *\n * <p>Thanks to Thomas Achleitner for providing the initial implementation of\n * this message source!\n *\n * @author Juergen Hoeller\n * @author Sebastien Deleuze\n * @author Sam Brannen\n * @see #setCacheSeconds\n * @see #setBasenames\n * @see #setDefaultCharset\n * @see #setFileEncodings\n * @see #setPropertiesPersister\n * @see #setResourceLoader\n * @see org.springframework.core.io.DefaultResourceLoader\n * @see ResourceBundleMessageSource\n * @see java.util.ResourceBundle\n */',
            '/**\n * Spring 专用的 {@link org.springframework.context.MessageSource} 实现，\n * 通过 basename 访问资源 bundle，并参与 Spring {@link org.springframework.context.ResourceLoaderAware} 资源加载。\n *\n * <p>与 {@link ResourceBundleMessageSource} 的 "basenames" 类似但不完全相同：\n * 遵循 ResourceBundle 不写扩展名/语言码的规则，但可指向任意 Spring 资源位置。\n *\n * <p>典型 Web 应用可将消息文件放在 {@code WEB-INF} 下，例如 basename "WEB-INF/messages"。\n * 先找到的 bundle 中定义会覆盖后找到的（顺序查找）。\n *\n * <p>可在 {@link org.springframework.context.ApplicationContext} 外使用；\n * 默认 {@link org.springframework.core.io.DefaultResourceLoader}，在上下文中会被覆盖。\n *\n * @author Juergen Hoeller\n * @author Sebastien Deleuze\n * @author Sam Brannen\n * @see #setCacheSeconds\n * @see #setBasenames\n * @see #setDefaultCharset\n * @see #setFileEncodings\n * @see #setPropertiesPersister\n * @see #setResourceLoader\n * @see org.springframework.core.io.DefaultResourceLoader\n * @see ResourceBundleMessageSource\n * @see java.util.ResourceBundle\n */',
        ),
        (
            '/**\n\t * PropertiesHolder for caching.\n\t * Stores the last-modified timestamp of the source file for efficient\n\t * change detection, and the timestamp of the last refresh attempt\n\t * (updated every time the cache entry gets re-validated).\n\t */',
            '/**\n * 用于缓存的 PropertiesHolder：记录源文件最后修改时间以高效检测变更，\n * 以及上次刷新尝试的时间戳（每次重新验证缓存条目时更新）。\n */',
        ),
        (
            '/** Cache to hold already generated MessageFormats per message code. */',
            '/** 缓存已生成的 MessageFormat（按消息代码）。 */',
        ),
        (
            'class `ReloadableResourceBundleMessageSource`：请结合所属模块与调用方理解其在整体架构中的职责。',
            'class `ReloadableResourceBundleMessageSource`：可热刷新的国际化 MessageSource，按 basename 加载 properties/XML 并缓存。',
        ),
    ],
}
