"""Chinese JavaDoc replacements for springframework wave20b aop config [10:20]."""

CONFIG_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractInterceptorDrivenBeanDefinitionDecorator.java": [
        (
            "/**\n * Base implementation for\n * {@link org.springframework.beans.factory.xml.BeanDefinitionDecorator BeanDefinitionDecorators}\n * wishing to add an {@link org.aopalliance.intercept.MethodInterceptor interceptor}\n * to the resulting bean.\n *\n * <p>This base class controls the creation of the {@link ProxyFactoryBean} bean definition\n * and wraps the original as an inner-bean definition for the {@code target} property\n * of {@link ProxyFactoryBean}.\n *\n * <p>Chaining is correctly handled, ensuring that only one {@link ProxyFactoryBean} definition\n * is created. If a previous {@link org.springframework.beans.factory.xml.BeanDefinitionDecorator}\n * already created the {@link org.springframework.aop.framework.ProxyFactoryBean} then the\n * interceptor is simply added to the existing definition.\n *\n * <p>Subclasses have only to create the {@code BeanDefinition} to the interceptor that\n * they wish to add.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.aopalliance.intercept.MethodInterceptor\n */",
            "/**\n * 希望向结果 Bean 添加 {@link org.aopalliance.intercept.MethodInterceptor 拦截器} 的\n * {@link org.springframework.beans.factory.xml.BeanDefinitionDecorator BeanDefinitionDecorator}\n * 基类实现。\n *\n * <p>本基类控制 {@link ProxyFactoryBean} Bean 定义的创建，\n * 并将原始定义包装为 {@link ProxyFactoryBean} {@code target} 属性的内部 Bean 定义。\n *\n * <p>正确处理链式装饰，确保仅创建一个 {@link ProxyFactoryBean} 定义。\n * 若先前的 {@link org.springframework.beans.factory.xml.BeanDefinitionDecorator}\n * 已创建 {@link org.springframework.aop.framework.ProxyFactoryBean}，\n * 则仅将拦截器添加到现有定义。\n *\n * <p>子类只需创建要添加的拦截器 {@code BeanDefinition}。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.aopalliance.intercept.MethodInterceptor\n */",
        ),
        (
            "\t\t// get the root bean name - will be the name of the generated proxy factory bean",
            "\t\t// 获取根 Bean 名称——将作为生成的代理工厂 Bean 名称",
        ),
        (
            "\t\t// delegate to subclass for interceptor definition",
            "\t\t// 委托子类创建拦截器定义",
        ),
        (
            "\t\t// generate name and register the interceptor",
            "\t\t// 生成名称并注册拦截器",
        ),
        (
            "\t\t\t// create the proxy definition",
            "\t\t\t// 创建代理定义",
        ),
        (
            "\t\t\t// create proxy factory bean definition",
            "\t\t\t// 创建 ProxyFactoryBean 定义",
        ),
        (
            "\t\t\t// set the target",
            "\t\t\t// 设置 target",
        ),
        (
            "\t\t\t// create the interceptor names list",
            "\t\t\t// 创建 interceptorNames 列表",
        ),
        (
            "\t\t\t// copy autowire settings from original bean definition.",
            "\t\t\t// 从原始 Bean 定义复制 autowire 设置。",
        ),
        (
            "\t\t\t// wrap it in a BeanDefinitionHolder with bean name",
            "\t\t\t// 用 BeanDefinitionHolder 包装并指定 Bean 名称",
        ),
        (
            "\t/**\n\t * Subclasses should implement this method to return the {@code BeanDefinition}\n\t * for the interceptor they wish to apply to the bean being decorated.\n\t */",
            "\t/**\n\t * 子类应实现本方法，返回要应用于被装饰 Bean 的拦截器 {@code BeanDefinition}。\n\t */",
        ),
    ],
    "AdviceEntry.java": [
        (
            "/**\n * {@link ParseState} entry representing an advice element.\n *\n * @author Mark Fisher\n * @since 2.0\n */",
            "/**\n * 表示 advice 元素的 {@link ParseState} 条目。\n *\n * @author Mark Fisher\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code AdviceEntry} instance.\n\t * @param kind the kind of advice represented by this entry (before, after, around)\n\t */",
            "\t/**\n\t * 创建新的 {@code AdviceEntry} 实例。\n\t * @param kind 本条目表示的通知类型（before、after、around）\n\t */",
        ),
    ],
    "AdvisorComponentDefinition.java": [
        (
            "/**\n * {@link org.springframework.beans.factory.parsing.ComponentDefinition}\n * that bridges the gap between the advisor bean definition configured\n * by the {@code <aop:advisor>} tag and the component definition\n * infrastructure.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 桥接 {@code <aop:advisor>} 标签配置的 Advisor Bean 定义\n * 与组件定义基础设施的\n * {@link org.springframework.beans.factory.parsing.ComponentDefinition}。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
    ],
    "AdvisorEntry.java": [
        (
            "/**\n * {@link ParseState} entry representing an advisor.\n *\n * @author Mark Fisher\n * @since 2.0\n */",
            "/**\n * 表示 advisor 的 {@link ParseState} 条目。\n *\n * @author Mark Fisher\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code AdvisorEntry} instance.\n\t * @param name the bean name of the advisor\n\t */",
            "\t/**\n\t * 创建新的 {@code AdvisorEntry} 实例。\n\t * @param name Advisor 的 Bean 名称\n\t */",
        ),
    ],
    "AopConfigUtils.java": [
        (
            "/**\n * Utility class for handling registration of AOP auto-proxy creators.\n *\n * <p>Only a single auto-proxy creator should be registered yet multiple concrete\n * implementations are available. This class provides a simple escalation protocol,\n * allowing a caller to request a particular auto-proxy creator and know that creator,\n * <i>or a more capable variant thereof</i>, will be registered as a post-processor.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 2.5\n * @see AopNamespaceUtils\n */",
            "/**\n * 处理 AOP 自动代理创建器注册的工具类。\n *\n * <p>应仅注册一个自动代理创建器，但存在多种具体实现。\n * 本类提供简单升级协议：调用者可请求特定自动代理创建器，\n * 并确知该创建器<i>或其更强变体</i>将作为后处理器注册。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 2.5\n * @see AopNamespaceUtils\n */",
        ),
        (
            "\t/**\n\t * The bean name of the internally managed auto-proxy creator.\n\t */",
            "\t/**\n\t * 内部管理的自动代理创建器的 Bean 名称。\n\t */",
        ),
        (
            "\t/**\n\t * Stores the auto proxy creator classes in escalation order.\n\t */",
            "\t/**\n\t * 按升级顺序存储自动代理创建器类。\n\t */",
        ),
        (
            "\t\t// Set up the escalation list...",
            "\t\t// 设置升级列表...",
        ),
    ],
    "AopNamespaceHandler.java": [
        (
            "/**\n * {@code NamespaceHandler} for the {@code aop} namespace.\n *\n * <p>Provides a {@link org.springframework.beans.factory.xml.BeanDefinitionParser} for the\n * {@code <aop:config>} tag. A {@code config} tag can include nested\n * {@code pointcut}, {@code advisor} and {@code aspect} tags.\n *\n * <p>The {@code pointcut} tag allows for creation of named\n * {@link AspectJExpressionPointcut} beans using a simple syntax:\n * <pre class=\"code\">\n * &lt;aop:pointcut id=&quot;getNameCalls&quot; expression=&quot;execution(* *..ITestBean.getName(..))&quot;/&gt;\n * </pre>\n *\n * <p>Using the {@code advisor} tag you can configure an {@link org.springframework.aop.Advisor}\n * and have it applied to all relevant beans in you {@link org.springframework.beans.factory.BeanFactory}\n * automatically. The {@code advisor} tag supports both in-line and referenced\n * {@link org.springframework.aop.Pointcut Pointcuts}:\n *\n * <pre class=\"code\">\n * &lt;aop:advisor id=&quot;getAgeAdvisor&quot;\n *     pointcut=&quot;execution(* *..ITestBean.getAge(..))&quot;\n *     advice-ref=&quot;getAgeCounter&quot;/&gt;\n *\n * &lt;aop:advisor id=&quot;getNameAdvisor&quot;\n *     pointcut-ref=&quot;getNameCalls&quot;\n *     advice-ref=&quot;getNameCounter&quot;/&gt;</pre>\n *\n * @author Rob Harrop\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * {@code aop} 命名空间的 {@code NamespaceHandler}。\n *\n * <p>为 {@code <aop:config>} 标签提供\n * {@link org.springframework.beans.factory.xml.BeanDefinitionParser}。\n * {@code config} 标签可包含嵌套的 {@code pointcut}、{@code advisor} 与 {@code aspect} 标签。\n *\n * <p>{@code pointcut} 标签可用简单语法创建命名的\n * {@link AspectJExpressionPointcut} Bean：\n * <pre class=\"code\">\n * &lt;aop:pointcut id=&quot;getNameCalls&quot; expression=&quot;execution(* *..ITestBean.getName(..))&quot;/&gt;\n * </pre>\n *\n * <p>使用 {@code advisor} 标签可配置 {@link org.springframework.aop.Advisor}，\n * 并自动应用于 {@link org.springframework.beans.factory.BeanFactory} 中所有相关 Bean。\n * {@code advisor} 标签支持内联与引用的 {@link org.springframework.aop.Pointcut 切入点}：\n *\n * <pre class=\"code\">\n * &lt;aop:advisor id=&quot;getAgeAdvisor&quot;\n *     pointcut=&quot;execution(* *..ITestBean.getAge(..))&quot;\n *     advice-ref=&quot;getAgeCounter&quot;/&gt;\n *\n * &lt;aop:advisor id=&quot;getNameAdvisor&quot;\n *     pointcut-ref=&quot;getNameCalls&quot;\n *     advice-ref=&quot;getNameCounter&quot;/&gt;</pre>\n *\n * @author Rob Harrop\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Register the {@link BeanDefinitionParser BeanDefinitionParsers} for the\n\t * '{@code config}', '{@code spring-configured}', '{@code aspectj-autoproxy}'\n\t * and '{@code scoped-proxy}' tags.\n\t */",
            "\t/**\n\t * 注册 '{@code config}'、'{@code spring-configured}'、\n\t * '{@code aspectj-autoproxy}' 与 '{@code scoped-proxy}' 标签的\n\t * {@link BeanDefinitionParser BeanDefinitionParser}。\n\t */",
        ),
        (
            "\t\t// In 2.0 XSD as well as in 2.5+ XSDs",
            "\t\t// 2.0 XSD 及 2.5+ XSD 均包含",
        ),
        (
            "\t\t// Only in 2.0 XSD: moved to context namespace in 2.5+",
            "\t\t// 仅 2.0 XSD 包含：2.5+ 已移至 context 命名空间",
        ),
    ],
    "AopNamespaceUtils.java": [
        (
            "/**\n * Utility class for handling registration of auto-proxy creators used internally\n * by the '{@code aop}' namespace tags.\n *\n * <p>Only a single auto-proxy creator should be registered and multiple configuration\n * elements may wish to register different concrete implementations. As such this class\n * delegates to {@link AopConfigUtils} which provides a simple escalation protocol.\n * Callers may request a particular auto-proxy creator and know that creator,\n * <i>or a more capable variant thereof</i>, will be registered as a post-processor.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 2.0\n * @see AopConfigUtils\n */",
            "/**\n * 处理 '{@code aop}' 命名空间标签内部使用的自动代理创建器注册的工具类。\n *\n * <p>应仅注册一个自动代理创建器，多个配置元素可能希望注册不同具体实现。\n * 因此本类委托 {@link AopConfigUtils}，其提供简单升级协议。\n * 调用者可请求特定自动代理创建器，并确知该创建器\n * <i>或其更强变体</i>将作为后处理器注册。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 2.0\n * @see AopConfigUtils\n */",
        ),
        (
            "\t/**\n\t * The {@code proxy-target-class} attribute as found on AOP-related XML tags.\n\t */",
            "\t/**\n\t * AOP 相关 XML 标签上的 {@code proxy-target-class} 属性。\n\t */",
        ),
        (
            "\t/**\n\t * The {@code expose-proxy} attribute as found on AOP-related XML tags.\n\t */",
            "\t/**\n\t * AOP 相关 XML 标签上的 {@code expose-proxy} 属性。\n\t */",
        ),
    ],
    "AspectComponentDefinition.java": [
        (
            "/**\n * {@link org.springframework.beans.factory.parsing.ComponentDefinition}\n * that holds an aspect definition, including its nested pointcuts.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n * @see #getNestedComponents()\n * @see PointcutComponentDefinition\n */",
            "/**\n * 持有切面定义（含嵌套切入点）的\n * {@link org.springframework.beans.factory.parsing.ComponentDefinition}。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n * @see #getNestedComponents()\n * @see PointcutComponentDefinition\n */",
        ),
    ],
    "AspectEntry.java": [
        (
            "/**\n * {@link ParseState} entry representing an aspect.\n *\n * @author Mark Fisher\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 表示 aspect 的 {@link ParseState} 条目。\n *\n * @author Mark Fisher\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code AspectEntry} instance.\n\t * @param id the id of the aspect element\n\t * @param ref the bean name referenced by this aspect element\n\t */",
            "\t/**\n\t * 创建新的 {@code AspectEntry} 实例。\n\t * @param id aspect 元素的 id\n\t * @param ref 本 aspect 元素引用的 Bean 名称\n\t */",
        ),
    ],
    "AspectJAutoProxyBeanDefinitionParser.java": [
        (
            "/**\n * {@link BeanDefinitionParser} for the {@code aspectj-autoproxy} tag,\n * enabling the automatic application of @AspectJ-style aspects found in\n * the {@link org.springframework.beans.factory.BeanFactory}.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * {@code aspectj-autoproxy} 标签的 {@link BeanDefinitionParser}，\n * 启用对 {@link org.springframework.beans.factory.BeanFactory} 中\n * @AspectJ 风格切面的自动应用。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
    ],
    "MethodLocatingFactoryBean.java": [
        (
            "/**\n * {@link FactoryBean} implementation that locates a {@link Method} on a specified bean.\n *\n * @author Rob Harrop\n * @since 2.0\n */",
            "/**\n * 在指定 Bean 上定位 {@link Method} 的 {@link FactoryBean} 实现。\n *\n * @author Rob Harrop\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Set the name of the bean to locate the {@link Method} on.\n\t * <p>This property is required.\n\t * @param targetBeanName the name of the bean to locate the {@link Method} on\n\t */",
            "\t/**\n\t * 设置要定位 {@link Method} 的目标 Bean 名称。\n\t * <p>此属性必填。\n\t * @param targetBeanName 要定位 {@link Method} 的 Bean 名称\n\t */",
        ),
        (
            "\t/**\n\t * Set the name of the {@link Method} to locate.\n\t * <p>This property is required.\n\t * @param methodName the name of the {@link Method} to locate\n\t */",
            "\t/**\n\t * 设置要定位的 {@link Method} 名称。\n\t * <p>此属性必填。\n\t * @param methodName 要定位的 {@link Method} 名称\n\t */",
        ),
    ],
}
