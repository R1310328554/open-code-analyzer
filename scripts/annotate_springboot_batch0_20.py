#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 batch files [0:20]."""
from __future__ import annotations

import json
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springboot/4.1.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
BATCH_FILES = json.loads((VER / "_reports/class-queue/batch.json").read_text())["files"][:20]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ConditionalOnMissingBean.java": [
        (
            "/**\n * {@link Conditional @Conditional} that only matches when no beans meeting the specified\n * requirements are already contained in the {@link BeanFactory}. None of the requirements\n * must be met for the condition to match and the requirements do not have to be met by\n * the same bean.\n * <p>\n * When placed on a {@link Bean @Bean} method and none of {@link #value}, {@link #type},\n * {@link #name}, or {@link #annotation} has been specified, the bean type to match\n * defaults to the return type of the {@code @Bean} method:\n *\n * <pre class=\"code\">\n * &#064;Configuration\n * public class MyAutoConfiguration {\n *\n *     &#064;ConditionalOnMissingBean\n *     &#064;Bean\n *     public MyService myService() {\n *         ...\n *     }\n *\n * }</pre>\n * <p>\n * In the sample above the condition will match if no bean of type {@code MyService} is\n * already contained in the {@link BeanFactory}.\n * <p>\n * The condition can only match the bean definitions that have been processed by the\n * application context so far and, as such, it is strongly recommended to use this\n * condition on auto-configuration classes only. If a candidate bean may be created by\n * another auto-configuration, make sure that the one using this condition runs after.\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @since 1.0.0\n */",
            "/**\n * 仅当 {@link BeanFactory} 中尚不存在满足所有指定要求的 Bean 时才匹配的\n * {@link Conditional @Conditional}。条件匹配时所有要求均不得满足，且不必由同一个 Bean 满足。\n * <p>\n * 当标注在 {@link Bean @Bean} 方法上且未指定 {@link #value}、{@link #type}、\n * {@link #name} 或 {@link #annotation} 时，要匹配的 Bean 类型默认为\n * {@code @Bean} 方法的返回类型：\n *\n * <pre class=\"code\">\n * &#064;Configuration\n * public class MyAutoConfiguration {\n *\n *     &#064;ConditionalOnMissingBean\n *     &#064;Bean\n *     public MyService myService() {\n *         ...\n *     }\n *\n * }</pre>\n * <p>\n * 上例中，若 {@link BeanFactory} 中尚不存在类型为 {@code MyService} 的 Bean，条件即匹配。\n * <p>\n * 该条件只能匹配应用上下文迄今已处理的 Bean 定义，因此强烈建议仅在自动配置类上使用。\n * 若候选 Bean 可能由其他自动配置创建，请确保使用此条件的配置在其之后运行。\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @since 1.0.0\n */",
        ),
        (
            "/**\n\t * The class types of beans that should be checked. The condition matches when no bean\n\t * of each class specified is contained in the {@link BeanFactory}. Beans that are not\n\t * autowire candidates or that are not default candidates are ignored.\n\t * <p>\n\t * Since this annotation is parsed by loading class bytecode, it is safe to specify\n\t * classes here that may ultimately not be on the classpath, but only if this\n\t * annotation is directly on the affected component and <b>not</b> if this annotation\n\t * is used as a composed, meta-annotation. In order to use this annotation as a\n\t * meta-annotation, only use the {@link #type} attribute.\n\t * @return the class types of beans to check\n\t * @see Bean#autowireCandidate()\n\t * @see BeanDefinition#isAutowireCandidate\n\t * @see Bean#defaultCandidate()\n\t * @see AbstractBeanDefinition#isDefaultCandidate\n\t */",
            "/**\n\t * 要检查的 Bean 类类型。当 {@link BeanFactory} 中不包含任何指定类的 Bean 时条件匹配。\n\t * 非自动装配候选或非默认候选的 Bean 会被忽略。\n\t * <p>\n\t * 由于该注解通过加载类字节码解析，在此指定最终可能不在类路径上的类是安全的，\n\t * 但前提是注解直接标注在受影响的组件上，<b>而非</b>作为组合元注解使用。\n\t * 若要将该注解作为元注解使用，请仅使用 {@link #type} 属性。\n\t * @return 要检查的 Bean 类类型\n\t * @see Bean#autowireCandidate()\n\t * @see BeanDefinition#isAutowireCandidate\n\t * @see Bean#defaultCandidate()\n\t * @see AbstractBeanDefinition#isDefaultCandidate\n\t */",
        ),
        (
            "/**\n\t * The class type names of beans that should be checked. The condition matches when no\n\t * bean of each class specified is contained in the {@link BeanFactory}. Beans that\n\t * are not autowire candidates or that are not default candidates are ignored.\n\t * @return the class type names of beans to check\n\t * @see Bean#autowireCandidate()\n\t * @see BeanDefinition#isAutowireCandidate\n\t * @see Bean#defaultCandidate()\n\t * @see AbstractBeanDefinition#isDefaultCandidate\n\t */",
            "/**\n\t * 要检查的 Bean 类类型名。当 {@link BeanFactory} 中不包含任何指定类的 Bean 时条件匹配。\n\t * 非自动装配候选或非默认候选的 Bean 会被忽略。\n\t * @return 要检查的 Bean 类类型名\n\t * @see Bean#autowireCandidate()\n\t * @see BeanDefinition#isAutowireCandidate\n\t * @see Bean#defaultCandidate()\n\t * @see AbstractBeanDefinition#isDefaultCandidate\n\t */",
        ),
        (
            "/**\n\t * The class types of beans that should be ignored when identifying matching beans.\n\t * <p>\n\t * Since this annotation is parsed by loading class bytecode, it is safe to specify\n\t * classes here that may ultimately not be on the classpath, but only if this\n\t * annotation is directly on the affected component and <b>not</b> if this annotation\n\t * is used as a composed, meta-annotation. In order to use this annotation as a\n\t * meta-annotation, only use the {@link #ignoredType} attribute.\n\t * @return the class types of beans to ignore\n\t * @since 1.2.5\n\t */",
            "/**\n\t * 在识别匹配 Bean 时应忽略的 Bean 类类型。\n\t * <p>\n\t * 由于该注解通过加载类字节码解析，在此指定最终可能不在类路径上的类是安全的，\n\t * 但前提是注解直接标注在受影响的组件上，<b>而非</b>作为组合元注解使用。\n\t * 若要将该注解作为元注解使用，请仅使用 {@link #ignoredType} 属性。\n\t * @return 要忽略的 Bean 类类型\n\t * @since 1.2.5\n\t */",
        ),
        (
            "/**\n\t * The class type names of beans that should be ignored when identifying matching\n\t * beans.\n\t * @return the class type names of beans to ignore\n\t * @since 1.2.5\n\t */",
            "/**\n\t * 在识别匹配 Bean 时应忽略的 Bean 类类型名。\n\t * @return 要忽略的 Bean 类类型名\n\t * @since 1.2.5\n\t */",
        ),
        (
            "/**\n\t * The annotation type decorating a bean that should be checked. The condition matches\n\t * when each annotation specified is missing from all beans in the\n\t * {@link BeanFactory}. Beans that are not autowire candidates or that are not default\n\t * candidates are ignored.\n\t * <p>\n\t * Since this annotation is parsed by loading class bytecode, it is safe to specify\n\t * classes here that may ultimately not be on the classpath, but only if this\n\t * annotation is directly on the affected component and <b>not</b> if this annotation\n\t * is used as a composed, meta-annotation.\n\t * @return the class-level annotation types to check\n\t * @see Bean#autowireCandidate()\n\t * @see BeanDefinition#isAutowireCandidate\n\t * @see Bean#defaultCandidate()\n\t * @see AbstractBeanDefinition#isDefaultCandidate\n\t */",
            "/**\n\t * 要检查的、标注在 Bean 上的注解类型。当 {@link BeanFactory} 中所有 Bean 均缺少\n\t * 每个指定注解时条件匹配。非自动装配候选或非默认候选的 Bean 会被忽略。\n\t * <p>\n\t * 由于该注解通过加载类字节码解析，在此指定最终可能不在类路径上的类是安全的，\n\t * 但前提是注解直接标注在受影响的组件上，<b>而非</b>作为组合元注解使用。\n\t * @return 要检查的类级别注解类型\n\t * @see Bean#autowireCandidate()\n\t * @see BeanDefinition#isAutowireCandidate\n\t * @see Bean#defaultCandidate()\n\t * @see AbstractBeanDefinition#isDefaultCandidate\n\t */",
        ),
        (
            "/**\n\t * The names of beans to check. The condition matches when each bean name specified is\n\t * missing in the {@link BeanFactory}.\n\t * @return the names of beans to check\n\t */",
            "/**\n\t * 要检查的 Bean 名称。当 {@link BeanFactory} 中缺少每个指定名称时条件匹配。\n\t * @return 要检查的 Bean 名称\n\t */",
        ),
        (
            "/**\n\t * Strategy to decide if the application context hierarchy (parent contexts) should be\n\t * considered.\n\t * @return the search strategy\n\t */",
            "/**\n\t * 决定是否应考虑应用上下文层次结构（父上下文）的策略。\n\t * @return 搜索策略\n\t */",
        ),
        (
            "/**\n\t * Additional classes that may contain the specified bean types within their generic\n\t * parameters. For example, an annotation declaring {@code value=Name.class} and\n\t * {@code parameterizedContainer=NameRegistration.class} would detect both\n\t * {@code Name} and {@code NameRegistration<Name>}.\n\t * <p>\n\t * Since this annotation is parsed by loading class bytecode, it is safe to specify\n\t * classes here that may ultimately not be on the classpath, but only if this\n\t * annotation is directly on the affected component and <b>not</b> if this annotation\n\t * is used as a composed, meta-annotation.\n\t * @return the container types\n\t * @since 2.1.0\n\t */",
            "/**\n\t * 其泛型参数中可能包含指定 Bean 类型的附加类。例如，注解声明\n\t * {@code value=Name.class} 和 {@code parameterizedContainer=NameRegistration.class}\n\t * 将同时检测 {@code Name} 和 {@code NameRegistration<Name>}。\n\t * <p>\n\t * 由于该注解通过加载类字节码解析，在此指定最终可能不在类路径上的类是安全的，\n\t * 但前提是注解直接标注在受影响的组件上，<b>而非</b>作为组合元注解使用。\n\t * @return 容器类型\n\t * @since 2.1.0\n\t */",
        ),
    ],
    "ConditionalOnMissingClass.java": [
        (
            "/**\n * {@link Conditional @Conditional} that only matches when the specified classes are not\n * on the classpath.\n *\n * @author Dave Syer\n * @since 1.0.0\n */",
            "/**\n * 仅当指定类不在类路径上时才匹配的 {@link Conditional @Conditional}。\n *\n * @author Dave Syer\n * @since 1.0.0\n */",
        ),
        (
            "/**\n\t * The names of the classes that must not be present.\n\t * @return the names of the classes that must not be present\n\t */",
            "/**\n\t * 必须不存在的类名。\n\t * @return 必须不存在的类名\n\t */",
        ),
    ],
    "ConditionalOnMissingFilterBean.java": [
        (
            "/**\n * {@link Conditional @Conditional} that only matches when no {@link Filter} beans of the\n * specified type are contained in the {@link BeanFactory}. This condition will detect\n * both directly registered {@link Filter} beans as well as those registered through a\n * {@link FilterRegistrationBean}.\n * <p>\n * When placed on a {@code @Bean} method, the bean class defaults to the return type of\n * the factory method or the type of the {@link Filter} if the bean is a\n * {@link FilterRegistrationBean}:\n *\n * <pre class=\"code\">\n * &#064;Configuration\n * public class MyAutoConfiguration {\n *\n *     &#064;ConditionalOnMissingFilterBean\n *     &#064;Bean\n *     public MyFilter myFilter() {\n *         ...\n *     }\n *\n * }</pre>\n * <p>\n * In the sample above the condition will match if no bean of type {@code MyFilter} or\n * {@code FilterRegistrationBean<MyFilter>} is already contained in the\n * {@link BeanFactory}.\n *\n * @author Phillip Webb\n * @since 2.1.0\n */",
            "/**\n * 仅当 {@link BeanFactory} 中不包含指定类型的 {@link Filter} Bean 时才匹配的\n * {@link Conditional @Conditional}。该条件会检测直接注册的 {@link Filter} Bean，\n * 以及通过 {@link FilterRegistrationBean} 注册的 Bean。\n * <p>\n * 当标注在 {@code @Bean} 方法上时，Bean 类默认为工厂方法的返回类型，\n * 若 Bean 为 {@link FilterRegistrationBean} 则默认为其中的 {@link Filter} 类型：\n *\n * <pre class=\"code\">\n * &#064;Configuration\n * public class MyAutoConfiguration {\n *\n *     &#064;ConditionalOnMissingFilterBean\n *     &#064;Bean\n *     public MyFilter myFilter() {\n *         ...\n *     }\n *\n * }</pre>\n * <p>\n * 上例中，若 {@link BeanFactory} 中尚不存在类型为 {@code MyFilter} 或\n * {@code FilterRegistrationBean<MyFilter>} 的 Bean，条件即匹配。\n *\n * @author Phillip Webb\n * @since 2.1.0\n */",
        ),
        (
            "/**\n\t * The filter bean type that must not be present.\n\t * @return the bean type\n\t */",
            "/**\n\t * 必须不存在的过滤器 Bean 类型。\n\t * @return Bean 类型\n\t */",
        ),
    ],
    "ConditionalOnNotWarDeployment.java": [
        (
            "/**\n * {@link Conditional @Conditional} that only matches when the application is not a\n * traditional WAR deployment. For applications with embedded servers, this condition will\n * return true.\n *\n * @author Guirong Hu\n * @since 2.7.10\n */",
            "/**\n * 仅当应用程序不是传统 WAR 部署时才匹配的 {@link Conditional @Conditional}。\n * 对于使用嵌入式服务器的应用程序，该条件将返回 {@code true}。\n *\n * @author Guirong Hu\n * @since 2.7.10\n */",
        ),
    ],
    "ConditionalOnNotWebApplication.java": [
        (
            "/**\n * {@link Conditional @Conditional} that only matches when the application context is a\n * not a web application context.\n *\n * @author Dave Syer\n * @since 1.0.0\n */",
            "/**\n * 仅当应用程序上下文不是 Web 应用程序上下文时才匹配的 {@link Conditional @Conditional}。\n *\n * @author Dave Syer\n * @since 1.0.0\n */",
        ),
    ],
    "ConditionalOnProperties.java": [
        (
            "/**\n * Container annotation that aggregates several\n * {@link ConditionalOnProperty @ConditionalOnProperty} annotations.\n *\n * @author Phillip Webb\n * @since 3.5.0\n * @see ConditionalOnProperty\n */",
            "/**\n * 聚合多个 {@link ConditionalOnProperty @ConditionalOnProperty} 注解的容器注解。\n *\n * @author Phillip Webb\n * @since 3.5.0\n * @see ConditionalOnProperty\n */",
        ),
        (
            "/**\n\t * Return the contained {@link ConditionalOnProperty @ConditionalOnProperty}\n\t * annotations.\n\t * @return the contained annotations\n\t */",
            "/**\n\t * 返回所包含的 {@link ConditionalOnProperty @ConditionalOnProperty} 注解。\n\t * @return 所包含的注解\n\t */",
        ),
    ],
    "ConditionalOnProperty.java": [
        (
            "/**\n * {@link Conditional @Conditional} that checks if the specified properties have a\n * specific value. By default the properties must be present in the {@link Environment}\n * and <strong>not</strong> equal to {@code false}. The {@link #havingValue()} and\n * {@link #matchIfMissing()} attributes allow further customizations.\n * <p>\n * The {@link #havingValue} attribute can be used to specify the value that the property\n * should have. The table below shows when a condition matches according to the property\n * value and the {@link #havingValue()} attribute:\n *\n * <table border=\"1\">\n * <caption>Having values</caption>\n * <tr>\n * <th>Property Value</th>\n * <th>{@code havingValue=\"\"}</th>\n * <th>{@code havingValue=\"true\"}</th>\n * <th>{@code havingValue=\"false\"}</th>\n * <th>{@code havingValue=\"foo\"}</th>\n * </tr>\n * <tr>\n * <td>{@code \"true\"}</td>\n * <td>yes</td>\n * <td>yes</td>\n * <td>no</td>\n * <td>no</td>\n * </tr>\n * <tr>\n * <td>{@code \"false\"}</td>\n * <td>no</td>\n * <td>no</td>\n * <td>yes</td>\n * <td>no</td>\n * </tr>\n * <tr>\n * <td>{@code \"foo\"}</td>\n * <td>yes</td>\n * <td>no</td>\n * <td>no</td>\n * <td>yes</td>\n * </tr>\n * </table>\n * <p>\n * If the property is not contained in the {@link Environment} at all, the\n * {@link #matchIfMissing()} attribute is consulted. By default missing attributes do not\n * match.\n * <p>\n * This condition cannot be reliably used for matching collection properties. For example,\n * in the following configuration, the condition matches if {@code spring.example.values}\n * is present in the {@link Environment} but does not match if\n * {@code spring.example.values[0]} is present.\n *\n * <pre class=\"code\">\n * &#064;ConditionalOnProperty(prefix = \"spring\", name = \"example.values\")\n * class ExampleAutoConfiguration {\n * }\n * </pre>\n *\n * It is better to use a custom condition for such cases.\n *\n * @author Maciej Walkowiak\n * @author Stephane Nicoll\n * @author Phillip Webb\n * @since 1.1.0\n * @see ConditionalOnBooleanProperty\n */",
            "/**\n * 检查指定属性是否具有特定值的 {@link Conditional @Conditional}。\n * 默认情况下，属性必须存在于 {@link Environment} 中且<strong>不</strong>等于 {@code false}。\n * 可通过 {@link #havingValue()} 和 {@link #matchIfMissing()} 属性进一步定制。\n * <p>\n * {@link #havingValue} 属性可用于指定属性应具有的值。下表根据属性值和\n * {@link #havingValue()} 属性显示条件何时匹配：\n *\n * <table border=\"1\">\n * <caption>期望值</caption>\n * <tr>\n * <th>属性值</th>\n * <th>{@code havingValue=\"\"}</th>\n * <th>{@code havingValue=\"true\"}</th>\n * <th>{@code havingValue=\"false\"}</th>\n * <th>{@code havingValue=\"foo\"}</th>\n * </tr>\n * <tr>\n * <td>{@code \"true\"}</td>\n * <td>是</td>\n * <td>是</td>\n * <td>否</td>\n * <td>否</td>\n * </tr>\n * <tr>\n * <td>{@code \"false\"}</td>\n * <td>否</td>\n * <td>否</td>\n * <td>是</td>\n * <td>否</td>\n * </tr>\n * <tr>\n * <td>{@code \"foo\"}</td>\n * <td>是</td>\n * <td>否</td>\n * <td>否</td>\n * <td>是</td>\n * </tr>\n * </table>\n * <p>\n * 若属性完全不在 {@link Environment} 中，则参考 {@link #matchIfMissing()} 属性。\n * 默认情况下缺失的属性不匹配。\n * <p>\n * 该条件无法可靠地用于匹配集合属性。例如，在以下配置中，若\n * {@code spring.example.values} 存在于 {@link Environment} 中则条件匹配，\n * 但若仅存在 {@code spring.example.values[0]} 则不匹配。\n *\n * <pre class=\"code\">\n * &#064;ConditionalOnProperty(prefix = \"spring\", name = \"example.values\")\n * class ExampleAutoConfiguration {\n * }\n * </pre>\n *\n * 此类情况最好使用自定义条件。\n *\n * @author Maciej Walkowiak\n * @author Stephane Nicoll\n * @author Phillip Webb\n * @since 1.1.0\n * @see ConditionalOnBooleanProperty\n */",
        ),
        (
            "/**\n\t * Alias for {@link #name()}.\n\t * @return the names\n\t */",
            "/**\n\t * {@link #name()} 的别名。\n\t * @return 属性名\n\t */",
        ),
        (
            "/**\n\t * A prefix that should be applied to each property. The prefix automatically ends\n\t * with a dot if not specified. A valid prefix is defined by one or more words\n\t * separated with dots (e.g. {@code \"acme.system.feature\"}).\n\t * @return the prefix\n\t */",
            "/**\n\t * 应用于每个属性的前缀。若未指定，前缀会自动以点号结尾。\n\t * 有效前缀由一个或多个以点号分隔的单词组成（例如 {@code \"acme.system.feature\"}）。\n\t * @return 前缀\n\t */",
        ),
        (
            "/**\n\t * The name of the properties to test. If a prefix has been defined, it is applied to\n\t * compute the full key of each property. For instance if the prefix is\n\t * {@code app.config} and one value is {@code my-value}, the full key would be\n\t * {@code app.config.my-value}\n\t * <p>\n\t * Use the dashed notation to specify each property, that is all lower case with a \"-\"\n\t * to separate words (e.g. {@code my-long-property}).\n\t * <p>\n\t * If multiple names are specified, all of the properties have to pass the test for\n\t * the condition to match.\n\t * @return the names\n\t */",
            "/**\n\t * 要测试的属性名。若已定义前缀，则应用于计算每个属性的完整键。\n\t * 例如前缀为 {@code app.config} 且值为 {@code my-value} 时，完整键为\n\t * {@code app.config.my-value}。\n\t * <p>\n\t * 使用短横线命名法指定每个属性，即全小写并以 {@code -} 分隔单词\n\t * （例如 {@code my-long-property}）。\n\t * <p>\n\t * 若指定了多个名称，所有属性都必须通过测试条件才匹配。\n\t * @return 属性名\n\t */",
        ),
        (
            "/**\n\t * The string representation of the expected value for the properties. If not\n\t * specified, the property must <strong>not</strong> be equal to {@code false}.\n\t * @return the expected value\n\t */",
            "/**\n\t * 属性期望值的字符串表示。若未指定，属性必须<strong>不</strong>等于 {@code false}。\n\t * @return 期望值\n\t */",
        ),
        (
            "/**\n\t * Specify if the condition should match if the property is not set. Defaults to\n\t * {@code false}.\n\t * @return if the condition should match if the property is missing\n\t */",
            "/**\n\t * 指定属性未设置时条件是否应匹配。默认为 {@code false}。\n\t * @return 属性缺失时条件是否应匹配\n\t */",
        ),
    ],
    "ConditionalOnResource.java": [
        (
            "/**\n * {@link Conditional @Conditional} that only matches when the specified resources are on\n * the classpath.\n *\n * @author Dave Syer\n * @since 1.0.0\n */",
            "/**\n * 仅当指定资源存在于类路径上时才匹配的 {@link Conditional @Conditional}。\n *\n * @author Dave Syer\n * @since 1.0.0\n */",
        ),
        (
            "/**\n\t * The resources that must be present.\n\t * @return the resource paths that must be present.\n\t */",
            "/**\n\t * 必须存在的资源。\n\t * @return 必须存在的资源路径\n\t */",
        ),
    ],
    "ConditionalOnSingleCandidate.java": [
        (
            "/**\n * {@link Conditional @Conditional} that only matches when a bean of the specified class\n * is already contained in the {@link BeanFactory} and a single candidate can be\n * determined.\n * <p>\n * The condition will also match if multiple matching bean instances are already contained\n * in the {@link BeanFactory} but a primary candidate has been defined; essentially, the\n * condition match if auto-wiring a bean with the defined type will succeed.\n * <p>\n * The condition can only match the bean definitions that have been processed by the\n * application context so far and, as such, it is strongly recommended to use this\n * condition on auto-configuration classes only. If a candidate bean may be created by\n * another auto-configuration, make sure that the one using this condition runs after.\n *\n * @author Stephane Nicoll\n * @since 1.3.0\n */",
            "/**\n * 仅当 {@link BeanFactory} 中已包含指定类的 Bean 且能确定唯一候选时才匹配的\n * {@link Conditional @Conditional}。\n * <p>\n * 若 {@link BeanFactory} 中已包含多个匹配实例但已定义主候选，条件也会匹配；\n * 本质上，当按定义类型自动装配 Bean 能够成功时条件即匹配。\n * <p>\n * 该条件只能匹配应用上下文迄今已处理的 Bean 定义，因此强烈建议仅在自动配置类上使用。\n * 若候选 Bean 可能由其他自动配置创建，请确保使用此条件的配置在其之后运行。\n *\n * @author Stephane Nicoll\n * @since 1.3.0\n */",
        ),
        (
            "/**\n\t * The class type of bean that should be checked. The condition matches if a bean of\n\t * the class specified is contained in the {@link BeanFactory} and a primary candidate\n\t * exists in case of multiple instances. Beans that are not autowire candidates, that\n\t * are not default candidates, or that are fallback candidates are ignored.\n\t * <p>\n\t * Since this annotation is parsed by loading class bytecode, it is safe to specify\n\t * classes here that may ultimately not be on the classpath, but only if this\n\t * annotation is directly on the affected component and <b>not</b> if this annotation\n\t * is used as a composed, meta-annotation. In order to use this annotation as a\n\t * meta-annotation, only use the {@link #type} attribute.\n\t * <p>\n\t * This attribute may <strong>not</strong> be used in conjunction with\n\t * {@link #type()}, but it may be used instead of {@link #type()}.\n\t * @return the class type of the bean to check\n\t * @see Bean#autowireCandidate()\n\t * @see BeanDefinition#isAutowireCandidate\n\t * @see Bean#defaultCandidate()\n\t * @see AbstractBeanDefinition#isDefaultCandidate\n\t */",
            "/**\n\t * 要检查的 Bean 类类型。当 {@link BeanFactory} 中包含指定类的 Bean 且\n\t * 存在多个实例时有主候选，条件即匹配。非自动装配候选、非默认候选或回退候选的 Bean 会被忽略。\n\t * <p>\n\t * 由于该注解通过加载类字节码解析，在此指定最终可能不在类路径上的类是安全的，\n\t * 但前提是注解直接标注在受影响的组件上，<b>而非</b>作为组合元注解使用。\n\t * 若要将该注解作为元注解使用，请仅使用 {@link #type} 属性。\n\t * <p>\n\t * 该属性<strong>不得</strong>与 {@link #type()} 同时使用，但可作为 {@link #type()} 的替代。\n\t * @return 要检查的 Bean 类类型\n\t * @see Bean#autowireCandidate()\n\t * @see BeanDefinition#isAutowireCandidate\n\t * @see Bean#defaultCandidate()\n\t * @see AbstractBeanDefinition#isDefaultCandidate\n\t */",
        ),
        (
            "/**\n\t * The class type name of bean that should be checked. The condition matches if a bean\n\t * of the class specified is contained in the {@link BeanFactory} and a primary\n\t * candidate exists in case of multiple instances. Beans that are not autowire\n\t * candidates, that are not default candidates, or that are fallback candidates are\n\t * ignored.\n\t * <p>\n\t * This attribute may <strong>not</strong> be used in conjunction with\n\t * {@link #value()}, but it may be used instead of {@link #value()}.\n\t * @return the class type name of the bean to check\n\t * @see Bean#autowireCandidate()\n\t * @see BeanDefinition#isAutowireCandidate\n\t * @see Bean#defaultCandidate()\n\t * @see AbstractBeanDefinition#isDefaultCandidate\n\t */",
            "/**\n\t * 要检查的 Bean 类类型名。当 {@link BeanFactory} 中包含指定类的 Bean 且\n\t * 存在多个实例时有主候选，条件即匹配。非自动装配候选、非默认候选或回退候选的 Bean 会被忽略。\n\t * <p>\n\t * 该属性<strong>不得</strong>与 {@link #value()} 同时使用，但可作为 {@link #value()} 的替代。\n\t * @return 要检查的 Bean 类类型名\n\t * @see Bean#autowireCandidate()\n\t * @see BeanDefinition#isAutowireCandidate\n\t * @see Bean#defaultCandidate()\n\t * @see AbstractBeanDefinition#isDefaultCandidate\n\t */",
        ),
        (
            "/**\n\t * Strategy to decide if the application context hierarchy (parent contexts) should be\n\t * considered.\n\t * @return the search strategy\n\t */",
            "/**\n\t * 决定是否应考虑应用上下文层次结构（父上下文）的策略。\n\t * @return 搜索策略\n\t */",
        ),
    ],
    "ConditionalOnThreading.java": [
        (
            "/**\n * {@link Conditional @Conditional} that matches when the specified threading is active.\n *\n * @author Moritz Halbritter\n * @since 3.2.0\n */",
            "/**\n * 当指定线程模型处于活跃状态时匹配的 {@link Conditional @Conditional}。\n *\n * @author Moritz Halbritter\n * @since 3.2.0\n */",
        ),
        (
            "/**\n\t * The {@link Threading threading} that must be active.\n\t * @return the expected threading\n\t */",
            "/**\n\t * 必须处于活跃状态的 {@link Threading 线程模型}。\n\t * @return 期望的线程模型\n\t */",
        ),
    ],
    "ConditionalOnWarDeployment.java": [
        (
            "/**\n * {@link Conditional @Conditional} that matches when the application is a traditional WAR\n * deployment. For applications with embedded servers, this condition will return false.\n *\n * @author Madhura Bhave\n * @since 2.3.0\n */",
            "/**\n * 仅当应用程序为传统 WAR 部署时才匹配的 {@link Conditional @Conditional}。\n * 对于使用嵌入式服务器的应用程序，该条件将返回 {@code false}。\n *\n * @author Madhura Bhave\n * @since 2.3.0\n */",
        ),
    ],
    "ConditionalOnWebApplication.java": [
        (
            "/**\n * {@link Conditional @Conditional} that matches when the application is a web\n * application. By default, any web application will match but it can be narrowed using\n * the {@link #type()} attribute.\n *\n * @author Dave Syer\n * @author Stephane Nicoll\n * @since 1.0.0\n */",
            "/**\n * 当应用程序为 Web 应用程序时匹配的 {@link Conditional @Conditional}。\n * 默认情况下任何 Web 应用程序均匹配，但可通过 {@link #type()} 属性缩小范围。\n *\n * @author Dave Syer\n * @author Stephane Nicoll\n * @since 1.0.0\n */",
        ),
        (
            "/**\n\t * The required type of the web application.\n\t * @return the required web application type\n\t */",
            "/**\n\t * 所需的 Web 应用程序类型。\n\t * @return 所需的 Web 应用程序类型\n\t */",
        ),
        (
            "/**\n\t * Available application types.\n\t */",
            "/**\n\t * 可用的应用程序类型。\n\t */",
        ),
        (
            "/**\n\t\t * Any web application will match.\n\t\t */",
            "/**\n\t\t * 任何 Web 应用程序均匹配。\n\t\t */",
        ),
        (
            "/**\n\t\t * Only servlet-based web application will match.\n\t\t */",
            "/**\n\t\t * 仅基于 Servlet 的 Web 应用程序匹配。\n\t\t */",
        ),
        (
            "/**\n\t\t * Only reactive-based web application will match.\n\t\t */",
            "/**\n\t\t * 仅基于响应式的 Web 应用程序匹配。\n\t\t */",
        ),
    ],
    "FilteringSpringBootCondition.java": [
        (
            "/**\n * Abstract base class for a {@link SpringBootCondition} that also implements\n * {@link AutoConfigurationImportFilter}.\n *\n * @author Phillip Webb\n */",
            "/**\n * 同时实现 {@link AutoConfigurationImportFilter} 的 {@link SpringBootCondition} 抽象基类。\n *\n * @author Phillip Webb\n */",
        ),
        (
            "/**\n\t * Slightly faster variant of {@link ClassUtils#forName(String, ClassLoader)} that\n\t * doesn't deal with primitives, arrays or inner types.\n\t * @param className the class name to resolve\n\t * @param classLoader the class loader to use\n\t * @return a resolved class\n\t * @throws ClassNotFoundException if the class cannot be found\n\t */",
            "/**\n\t * {@link ClassUtils#forName(String, ClassLoader)} 的略快变体，\n\t * 不处理基本类型、数组或内部类型。\n\t * @param className 要解析的类名\n\t * @param classLoader 要使用的类加载器\n\t * @return 解析后的类\n\t * @throws ClassNotFoundException 若找不到类\n\t */",
        ),
    ],
    "NoneNestedConditions.java": [
        (
            "/**\n * {@link Condition} that will match when none of the nested class conditions match. Can\n * be used to create composite conditions, for example:\n *\n * <pre class=\"code\">\n * static class OnNeitherJndiNorProperty extends NoneNestedConditions {\n *\n *    OnNeitherJndiNorProperty() {\n *        super(ConfigurationPhase.PARSE_CONFIGURATION);\n *    }\n *\n *    &#064;ConditionalOnJndi()\n *    static class OnJndi {\n *    }\n *\n *    &#064;ConditionalOnProperty(\"something\")\n *    static class OnProperty {\n *    }\n *\n * }\n * </pre>\n * <p>\n * The\n * {@link org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase\n * ConfigurationPhase} should be specified according to the conditions that are defined.\n * In the example above, all conditions are static and can be evaluated early so\n * {@code PARSE_CONFIGURATION} is a right fit.\n *\n * @author Phillip Webb\n * @since 1.3.0\n */",
            "/**\n * 当所有嵌套类条件均不匹配时才匹配的 {@link Condition}。可用于创建组合条件，例如：\n *\n * <pre class=\"code\">\n * static class OnNeitherJndiNorProperty extends NoneNestedConditions {\n *\n *    OnNeitherJndiNorProperty() {\n *        super(ConfigurationPhase.PARSE_CONFIGURATION);\n *    }\n *\n *    &#064;ConditionalOnJndi()\n *    static class OnJndi {\n *    }\n *\n *    &#064;ConditionalOnProperty(\"something\")\n *    static class OnProperty {\n *    }\n *\n * }\n * </pre>\n * <p>\n * 应根据所定义的条件指定\n * {@link org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase\n * ConfigurationPhase}。上例中所有条件均为静态且可尽早评估，因此\n * {@code PARSE_CONFIGURATION} 是合适的选择。\n *\n * @author Phillip Webb\n * @since 1.3.0\n */",
        ),
    ],
    "OnClassCondition.java": [
        (
            "/**\n * {@link Condition} and {@link AutoConfigurationImportFilter} that checks for the\n * presence or absence of specific classes.\n *\n * @author Phillip Webb\n * @see ConditionalOnClass\n * @see ConditionalOnMissingClass\n */",
            "/**\n * 检查特定类是否存在或不存在的 {@link Condition} 和 {@link AutoConfigurationImportFilter}。\n *\n * @author Phillip Webb\n * @see ConditionalOnClass\n * @see ConditionalOnMissingClass\n */",
        ),
    ],
    "OnCloudPlatformCondition.java": [
        (
            "/**\n * {@link Condition} that checks for a required {@link CloudPlatform}.\n *\n * @author Madhura Bhave\n * @see ConditionalOnCloudPlatform\n */",
            "/**\n * 检查所需 {@link CloudPlatform} 的 {@link Condition}。\n *\n * @author Madhura Bhave\n * @see ConditionalOnCloudPlatform\n */",
        ),
    ],
    "OnExpressionCondition.java": [
        (
            "/**\n * A Condition that evaluates a SpEL expression.\n *\n * @author Dave Syer\n * @author Stephane Nicoll\n * @see ConditionalOnExpression\n */",
            "/**\n * 评估 SpEL 表达式的 {@link Condition}。\n *\n * @author Dave Syer\n * @author Stephane Nicoll\n * @see ConditionalOnExpression\n */",
        ),
        (
            "/**\n\t * Allow user to provide bare expression with no '#{}' wrapper.\n\t * @param expression source expression\n\t * @return wrapped expression\n\t */",
            "/**\n\t * 允许用户提供不带 {@code #{}} 包装器的裸表达式。\n\t * @param expression 源表达式\n\t * @return 包装后的表达式\n\t */",
        ),
    ],
    "OnJavaCondition.java": [
        (
            "/**\n * {@link Condition} that checks for a required version of Java.\n *\n * @author Oliver Gierke\n * @author Phillip Webb\n * @see ConditionalOnJava\n */",
            "/**\n * 检查所需 Java 版本的 {@link Condition}。\n *\n * @author Oliver Gierke\n * @author Phillip Webb\n * @see ConditionalOnJava\n */",
        ),
        (
            "/**\n\t * Determines if the {@code runningVersion} is within the specified range of versions.\n\t * @param runningVersion the current version.\n\t * @param range the range\n\t * @param version the bounds of the range\n\t * @return if this version is within the specified range\n\t */",
            "/**\n\t * 判断 {@code runningVersion} 是否在指定版本范围内。\n\t * @param runningVersion 当前版本\n\t * @param range 范围\n\t * @param version 范围的边界\n\t * @return 该版本是否在指定范围内\n\t */",
        ),
    ],
    "OnJndiCondition.java": [
        (
            "/**\n * {@link Condition} that checks for JNDI locations.\n *\n * @author Phillip Webb\n * @see ConditionalOnJndi\n */",
            "/**\n * 检查 JNDI 位置的 {@link Condition}。\n *\n * @author Phillip Webb\n * @see ConditionalOnJndi\n */",
        ),
    ],
    "OnPropertyCondition.java": [
        (
            "/**\n * {@link Condition} that checks if properties are defined in the environment.\n *\n * @author Maciej Walkowiak\n * @author Phillip Webb\n * @author Stephane Nicoll\n * @author Andy Wilkinson\n * @see ConditionalOnProperty\n * @see ConditionalOnBooleanProperty\n */",
            "/**\n * 检查环境中是否定义了指定属性的 {@link Condition}。\n *\n * @author Maciej Walkowiak\n * @author Phillip Webb\n * @author Stephane Nicoll\n * @author Andy Wilkinson\n * @see ConditionalOnProperty\n * @see ConditionalOnBooleanProperty\n */",
        ),
    ],
}


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src = ORIGINAL / rel
        dst = ANALYZED / rel
        if not src.exists():
            failures.append(f"MISSING original: {rel}")
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            text = dst.read_text(encoding="utf-8")
            text = apply_replacements(text, reps)
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
