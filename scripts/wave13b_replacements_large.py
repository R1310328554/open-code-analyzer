"""Wave 13b [20:40] Chinese JavaDoc replacements — large files."""

LARGE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "GroovyScriptFactory.java": [
        (
            "/**\n * {@link org.springframework.scripting.ScriptFactory} implementation\n * for a Groovy script.\n *\n * <p>Typically used in combination with a\n * {@link org.springframework.scripting.support.ScriptFactoryPostProcessor};\n * see the latter's javadoc for a configuration example.\n *\n * <p>Note: Spring 4.0 supports Groovy 1.8 and higher.\n *\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @author Rod Johnson\n * @since 2.0\n * @see groovy.lang.GroovyClassLoader\n * @see org.springframework.scripting.support.ScriptFactoryPostProcessor\n */",
            "/**\n * Groovy 脚本的 {@link org.springframework.scripting.ScriptFactory} 实现。\n *\n * <p>通常与 {@link org.springframework.scripting.support.ScriptFactoryPostProcessor} 配合使用；\n * 配置示例见后者的 javadoc。\n *\n * <p>注意：Spring 4.0 支持 Groovy 1.8 及更高版本。\n *\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @author Rod Johnson\n * @since 2.0\n * @see groovy.lang.GroovyClassLoader\n * @see org.springframework.scripting.support.ScriptFactoryPostProcessor\n */",
        ),
        (
            "\t/**\n\t * Create a new GroovyScriptFactory for the given script source.\n\t * <p>We don't need to specify script interfaces here, since\n\t * a Groovy script defines its Java interfaces itself.\n\t * @param scriptSourceLocator a locator that points to the source of the script.\n\t * Interpreted by the post-processor that actually creates the script.\n\t */",
            "\t/**\n\t * 为给定脚本源创建新的 GroovyScriptFactory。\n\t * <p>此处无需指定脚本接口，因为 Groovy 脚本会自行定义 Java 接口。\n\t * @param scriptSourceLocator 指向脚本源的定位器，\n\t * 由实际创建脚本的后处理器解释。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new GroovyScriptFactory for the given script source,\n\t * specifying a strategy interface that can create a custom MetaClass\n\t * to supply missing methods and otherwise change the behavior of the object.\n\t * @param scriptSourceLocator a locator that points to the source of the script.\n\t * Interpreted by the post-processor that actually creates the script.\n\t * @param groovyObjectCustomizer a customizer that can set a custom metaclass\n\t * or make other changes to the GroovyObject created by this factory\n\t * (may be {@code null})\n\t * @see GroovyObjectCustomizer#customize\n\t */",
            "\t/**\n\t * 为给定脚本源创建新的 GroovyScriptFactory，\n\t * 指定可创建自定义 MetaClass 以提供缺失方法并改变对象行为的策略接口。\n\t * @param scriptSourceLocator 指向脚本源的定位器，\n\t * 由实际创建脚本的后处理器解释。\n\t * @param groovyObjectCustomizer 可为所创建 GroovyObject 设置自定义元类\n\t * 或进行其他更改的定制器（可为 {@code null}）\n\t * @see GroovyObjectCustomizer#customize\n\t */",
        ),
        (
            "\t/**\n\t * Create a new GroovyScriptFactory for the given script source,\n\t * specifying a strategy interface that can create a custom MetaClass\n\t * to supply missing methods and otherwise change the behavior of the object.\n\t * @param scriptSourceLocator a locator that points to the source of the script.\n\t * Interpreted by the post-processor that actually creates the script.\n\t * @param compilerConfiguration a custom compiler configuration to be applied\n\t * to the GroovyClassLoader (may be {@code null})\n\t * @since 4.3.3\n\t * @see GroovyClassLoader#GroovyClassLoader(ClassLoader, CompilerConfiguration)\n\t */",
            "\t/**\n\t * 为给定脚本源创建新的 GroovyScriptFactory，\n\t * 指定可创建自定义 MetaClass 以提供缺失方法并改变对象行为的策略接口。\n\t * @param scriptSourceLocator 指向脚本源的定位器，\n\t * 由实际创建脚本的后处理器解释。\n\t * @param compilerConfiguration 应用于 GroovyClassLoader 的自定义编译器配置\n\t * （可为 {@code null}）\n\t * @since 4.3.3\n\t * @see GroovyClassLoader#GroovyClassLoader(ClassLoader, CompilerConfiguration)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new GroovyScriptFactory for the given script source,\n\t * specifying a strategy interface that can customize Groovy's compilation\n\t * process within the underlying GroovyClassLoader.\n\t * @param scriptSourceLocator a locator that points to the source of the script.\n\t * Interpreted by the post-processor that actually creates the script.\n\t * @param compilationCustomizers one or more customizers to be applied to the\n\t * GroovyClassLoader compiler configuration\n\t * @since 4.3.3\n\t * @see CompilerConfiguration#addCompilationCustomizers\n\t * @see org.codehaus.groovy.control.customizers.ImportCustomizer\n\t */",
            "\t/**\n\t * 为给定脚本源创建新的 GroovyScriptFactory，\n\t * 指定可在底层 GroovyClassLoader 内定制 Groovy 编译过程的策略接口。\n\t * @param scriptSourceLocator 指向脚本源的定位器，\n\t * 由实际创建脚本的后处理器解释。\n\t * @param compilationCustomizers 应用于 GroovyClassLoader 编译器配置的\n\t * 一个或多个定制器\n\t * @since 4.3.3\n\t * @see CompilerConfiguration#addCompilationCustomizers\n\t * @see org.codehaus.groovy.control.customizers.ImportCustomizer\n\t */",
        ),
        (
            "\t/**\n\t * Return the GroovyClassLoader used by this script factory.\n\t */",
            "\t/**\n\t * 返回本脚本工厂使用的 GroovyClassLoader。\n\t */",
        ),
        (
            "\t/**\n\t * Build a {@link GroovyClassLoader} for the given {@code ClassLoader}.\n\t * @param classLoader the ClassLoader to build a GroovyClassLoader for\n\t * @since 4.3.3\n\t */",
            "\t/**\n\t * 为给定 {@code ClassLoader} 构建 {@link GroovyClassLoader}。\n\t * @param classLoader 要为其构建 GroovyClassLoader 的 ClassLoader\n\t * @since 4.3.3\n\t */",
        ),
        (
            "\t/**\n\t * Groovy scripts determine their interfaces themselves,\n\t * hence we don't need to explicitly expose interfaces here.\n\t * @return {@code null} always\n\t */",
            "\t/**\n\t * Groovy 脚本自行确定其接口，\n\t * 因此此处无需显式暴露接口。\n\t * @return 始终为 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Groovy scripts do not need a config interface,\n\t * since they expose their setters as public methods.\n\t */",
            "\t/**\n\t * Groovy 脚本不需要配置接口，\n\t * 因为它们将 setter 暴露为 public 方法。\n\t */",
        ),
        (
            "\t/**\n\t * Loads and parses the Groovy script via the GroovyClassLoader.\n\t * @see groovy.lang.GroovyClassLoader\n\t */",
            "\t/**\n\t * 通过 GroovyClassLoader 加载并解析 Groovy 脚本。\n\t * @see groovy.lang.GroovyClassLoader\n\t */",
        ),
        (
            "\t/**\n\t * Instantiate the given Groovy script class and run it if necessary.\n\t * @param scriptSource the source for the underlying script\n\t * @param scriptClass the Groovy script class\n\t * @return the result object (either an instance of the script class\n\t * or the result of running the script instance)\n\t * @throws ScriptCompilationException in case of instantiation failure\n\t */",
            "\t/**\n\t * 实例化给定 Groovy 脚本类并在必要时运行。\n\t * @param scriptSource 底层脚本的源\n\t * @param scriptClass Groovy 脚本类\n\t * @return 结果对象（脚本类实例或运行脚本实例的结果）\n\t * @throws ScriptCompilationException 实例化失败时\n\t */",
        ),
        (
            "\t/**\n\t * Wrapper that holds a temporarily cached result object.\n\t */",
            "\t/**\n\t * 持有临时缓存结果对象的包装器。\n\t */",
        ),
    ],
    "Component.java": [
        (
            "/**\n * Indicates that the annotated class is a <em>component</em>.\n *\n * <p>Such classes are considered as candidates for auto-detection\n * when using annotation-based configuration and classpath scanning.\n *\n * <p>A component may optionally specify a logical component name via the\n * {@link #value value} attribute of this annotation.\n *\n * <p>Other class-level annotations may be considered as identifying\n * a component as well, typically a special kind of component &mdash;\n * for example, the {@link Repository @Repository} annotation or AspectJ's\n * {@link org.aspectj.lang.annotation.Aspect @Aspect} annotation. Note, however,\n * that the {@code @Aspect} annotation does not automatically make a class\n * eligible for classpath scanning.\n *\n * <p>Any annotation meta-annotated with {@code @Component} is considered a\n * <em>stereotype</em> annotation which makes the annotated class eligible for\n * classpath scanning. For example, {@link Service @Service},\n * {@link Controller @Controller}, and {@link Repository @Repository} are\n * stereotype annotations. Stereotype annotations may also support configuration\n * of a logical component name by overriding the {@link #value} attribute of this\n * annotation via {@link org.springframework.core.annotation.AliasFor @AliasFor}.\n *\n * <p>As of Spring Framework 6.1, support for configuring the name of a stereotype\n * component by convention (i.e., via a {@code String value()} attribute without\n * {@code @AliasFor}) is deprecated and will be removed in a future version of the\n * framework. Consequently, custom stereotype annotations must use {@code @AliasFor}\n * to declare an explicit alias for this annotation's {@link #value} attribute.\n * See the source code declaration of {@link Repository#value()} and\n * {@link org.springframework.web.bind.annotation.ControllerAdvice#name()\n * ControllerAdvice.name()} for concrete examples.\n *\n * @author Mark Fisher\n * @author Sam Brannen\n * @since 2.5\n * @see Repository\n * @see Service\n * @see Controller\n * @see org.springframework.context.annotation.ClassPathBeanDefinitionScanner\n */",
            "/**\n * 表示被注解的类是<em>组件</em>。\n *\n * <p>使用基于注解的配置和类路径扫描时，\n * 此类被视为自动检测的候选。\n *\n * <p>组件可通过本注解的 {@link #value value} 属性\n * 可选地指定逻辑组件名。\n *\n * <p>其他类级别注解也可视为标识组件，\n * 通常是特殊类型的组件——例如 {@link Repository @Repository} 注解\n * 或 AspectJ 的 {@link org.aspectj.lang.annotation.Aspect @Aspect} 注解。\n * 但注意 {@code @Aspect} 注解不会自动使类具备类路径扫描资格。\n *\n * <p>任何以 {@code @Component} 元注解的注解均视为<em>构造型</em>注解，\n * 使被注解类具备类路径扫描资格。例如 {@link Service @Service}、\n * {@link Controller @Controller} 和 {@link Repository @Repository} 均为构造型注解。\n * 构造型注解也可通过 {@link org.springframework.core.annotation.AliasFor @AliasFor}\n * 覆盖本注解 {@link #value} 属性来配置逻辑组件名。\n *\n * <p>自 Spring Framework 6.1 起，通过约定（即无 {@code @AliasFor} 的\n * {@code String value()} 属性）配置构造型组件名称的支持已弃用，\n * 并将在未来版本中移除。因此，自定义构造型注解必须使用 {@code @AliasFor}\n * 为本注解 {@link #value} 属性声明显式别名。\n * 具体示例见 {@link Repository#value()} 和\n * {@link org.springframework.web.bind.annotation.ControllerAdvice#name()\n * ControllerAdvice.name()} 的源码声明。\n *\n * @author Mark Fisher\n * @author Sam Brannen\n * @since 2.5\n * @see Repository\n * @see Service\n * @see Controller\n * @see org.springframework.context.annotation.ClassPathBeanDefinitionScanner\n */",
        ),
        (
            "\t/**\n\t * The value may indicate a suggestion for a logical component name,\n\t * to be turned into a Spring bean name in case of an autodetected component.\n\t * @return the suggested component name, if any (or empty String otherwise)\n\t */",
            "\t/**\n\t * value 可表示逻辑组件名的建议，\n\t * 在自动检测组件时转为 Spring Bean 名称。\n\t * @return 建议的组件名（若有），否则为空 String\n\t */",
        ),
    ],
    "Indexed.java": [
        (
            "/**\n * Indicate that the annotated element represents a stereotype for the index.\n *\n * <p>The {@code CandidateComponentsIndex} is an alternative to classpath\n * scanning that uses a metadata file generated at compilation time. The\n * index allows retrieving the candidate components (i.e. fully qualified\n * name) based on a stereotype. This annotation instructs the generator to\n * index the element on which the annotated element is present or if it\n * implements or extends from the annotated element. The stereotype is the\n * fully qualified name of the annotated element.\n *\n * <p>Consider the default {@link Component} annotation that is meta-annotated\n * with this annotation. If a component is annotated with {@link Component},\n * an entry for that component will be added to the index using the\n * {@code org.springframework.stereotype.Component} stereotype.\n *\n * <p>This annotation is also honored on meta-annotations. Consider this\n * custom annotation:\n * <pre class=\"code\">\n * package com.example;\n *\n * &#064;Target(ElementType.TYPE)\n * &#064;Retention(RetentionPolicy.RUNTIME)\n * &#064;Documented\n * &#064;Indexed\n * &#064;Service\n * public @interface PrivilegedService { ... }\n * </pre>\n *\n * If the above annotation is present on a type, it will be indexed with two\n * stereotypes: {@code org.springframework.stereotype.Component} and\n * {@code com.example.PrivilegedService}. While {@link Service} isn't directly\n * annotated with {@code Indexed}, it is meta-annotated with {@link Component}.\n *\n * <p>It is also possible to index all implementations of a certain interface or\n * all the subclasses of a given class by adding {@code @Indexed} on it.\n *\n * Consider this base interface:\n * <pre class=\"code\">\n * package com.example;\n *\n * &#064;Indexed\n * public interface AdminService { ... }\n * </pre>\n *\n * Now, consider an implementation of this {@code AdminService} somewhere:\n * <pre class=\"code\">\n * package com.example.foo;\n *\n * import com.example.AdminService;\n *\n * public class ConfigurationAdminService implements AdminService { ... }\n * </pre>\n *\n * Because this class implements an interface that is indexed, it will be\n * automatically included with the {@code com.example.AdminService} stereotype.\n * If there are more {@code @Indexed} interfaces and/or superclasses in the\n * hierarchy, the class will map to all their stereotypes.\n *\n * @author Stephane Nicoll\n * @since 5.0\n */",
            "/**\n * 表示被注解元素是索引的构造型。\n *\n * <p>{@code CandidateComponentsIndex} 是类路径扫描的替代方案，\n * 使用编译时生成的元数据文件。索引允许基于构造型检索候选组件\n * （即完全限定名）。本注解指示生成器为存在被注解元素的元素建立索引，\n * 或为其所实现/继承的被注解元素建立索引。构造型即被注解元素的完全限定名。\n *\n * <p>考虑默认以本注解元注解的 {@link Component} 注解。\n * 若组件以 {@link Component} 注解，\n * 将使用 {@code org.springframework.stereotype.Component} 构造型\n * 为该组件向索引添加条目。\n *\n * <p>本注解在元注解上同样生效。考虑以下自定义注解：\n * <pre class=\"code\">\n * package com.example;\n *\n * &#064;Target(ElementType.TYPE)\n * &#064;Retention(RetentionPolicy.RUNTIME)\n * &#064;Documented\n * &#064;Indexed\n * &#064;Service\n * public @interface PrivilegedService { ... }\n * </pre>\n *\n * 若上述注解出现在某类型上，将以两种构造型建立索引：\n * {@code org.springframework.stereotype.Component} 和\n * {@code com.example.PrivilegedService}。虽然 {@link Service} 未直接以\n * {@code Indexed} 注解，但它以 {@link Component} 元注解。\n *\n * <p>也可通过在接口或类上添加 {@code @Indexed}\n * 为某接口的所有实现或某类的所有子类建立索引。\n *\n * 考虑以下基接口：\n * <pre class=\"code\">\n * package com.example;\n *\n * &#064;Indexed\n * public interface AdminService { ... }\n * </pre>\n *\n * 再考虑某处 {@code AdminService} 的实现：\n * <pre class=\"code\">\n * package com.example.foo;\n *\n * import com.example.AdminService;\n *\n * public class ConfigurationAdminService implements AdminService { ... }\n * </pre>\n *\n * 由于该类实现了已建立索引的接口，\n * 将自动以 {@code com.example.AdminService} 构造型纳入索引。\n * 若层次结构中还有更多 {@code @Indexed} 接口和/或超类，\n * 该类将映射到所有相关构造型。\n *\n * @author Stephane Nicoll\n * @since 5.0\n */",
        ),
    ],
    "ConcurrentModel.java": [
        (
            "/**\n * Implementation of the {@link Model} interface based on a {@link ConcurrentHashMap}\n * for use in concurrent scenarios.\n *\n * <p>Exposed to handler methods by Spring WebFlux, typically via a declaration of the\n * {@link Model} interface. There is typically no need to create it within user code.\n * If necessary a handler method can return a regular {@code java.util.Map},\n * likely a {@code java.util.ConcurrentMap}, for a pre-determined model.\n *\n * @author Rossen Stoyanchev\n * @since 5.0\n */",
            "/**\n * 基于 {@link ConcurrentHashMap} 的 {@link Model} 接口实现，\n * 用于并发场景。\n *\n * <p>由 Spring WebFlux 暴露给处理器方法，通常通过声明 {@link Model} 接口。\n * 用户代码通常无需创建。必要时处理器方法可返回常规 {@code java.util.Map}，\n * 可能是 {@code java.util.ConcurrentMap}，作为预定模型。\n *\n * @author Rossen Stoyanchev\n * @since 5.0\n */",
        ),
        (
            "\t/**\n\t * Construct a new, empty {@code ConcurrentModel}.\n\t */",
            "\t/**\n\t * 构造新的空 {@code ConcurrentModel}。\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new {@code ConcurrentModel} containing the supplied attribute\n\t * under the supplied name.\n\t * @see #addAttribute(String, Object)\n\t */",
            "\t/**\n\t * 构造包含所提供属性（以所提供名称）的新 {@code ConcurrentModel}。\n\t * @see #addAttribute(String, Object)\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new {@code ConcurrentModel} containing the supplied attribute.\n\t * <p>Uses attribute name generation to generate the key for the supplied model\n\t * object.\n\t * @see #addAttribute(Object)\n\t */",
            "\t/**\n\t * 构造包含所提供属性的新 {@code ConcurrentModel}。\n\t * <p>使用属性名生成器为所提供模型对象生成键。\n\t * @see #addAttribute(Object)\n\t */",
        ),
        (
            "\t/**\n\t * Add the supplied attribute under the supplied name.\n\t * @param attributeName the name of the model attribute (never {@code null})\n\t * @param attributeValue the model attribute value (ignored if {@code null},\n\t * just removing an existing entry if any)\n\t */",
            "\t/**\n\t * 以给定名称添加所提供的属性。\n\t * @param attributeName 模型属性名称（永不为 {@code null}）\n\t * @param attributeValue 模型属性值（若为 {@code null} 则忽略，\n\t * 仅移除已有条目（若有））\n\t */",
        ),
        (
            "\t/**\n\t * Add the supplied attribute to this {@code Map} using a\n\t * {@link org.springframework.core.Conventions#getVariableName generated name}.\n\t * <p><i>Note: Empty {@link Collection Collections} are not added to\n\t * the model when using this method because we cannot correctly determine\n\t * the true convention name. View code should check for {@code null} rather\n\t * than for empty collections as is already done by JSTL tags.</i>\n\t * @param attributeValue the model attribute value (never {@code null})\n\t */",
            "\t/**\n\t * 使用 {@link org.springframework.core.Conventions#getVariableName 生成的名称}\n\t * 将所提供的属性添加到本 {@code Map}。\n\t * <p><i>注意：使用本方法时，空的 {@link Collection Collection}\n\t * 不会添加到模型，因为我们无法正确确定真正的约定名称。\n\t * 视图代码应检查 {@code null} 而非空集合，与 JSTL 标签的做法一致。</i>\n\t * @param attributeValue 模型属性值（永不为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Copy all attributes in the supplied {@code Collection} into this\n\t * {@code Map}, using attribute name generation for each element.\n\t * @see #addAttribute(Object)\n\t */",
            "\t/**\n\t * 将所提供 {@code Collection} 中的所有属性复制到本 {@code Map}，\n\t * 为每个元素生成属性名。\n\t * @see #addAttribute(Object)\n\t */",
        ),
        (
            "\t/**\n\t * Copy all attributes in the supplied {@code Map} into this {@code Map}.\n\t * @see #addAttribute(String, Object)\n\t */",
            "\t/**\n\t * 将所提供 {@code Map} 中的所有属性复制到本 {@code Map}。\n\t * @see #addAttribute(String, Object)\n\t */",
        ),
        (
            "\t/**\n\t * Copy all attributes in the supplied {@code Map} into this {@code Map},\n\t * with existing objects of the same name taking precedence (i.e. not getting\n\t * replaced).\n\t */",
            "\t/**\n\t * 将所提供 {@code Map} 中的所有属性复制到本 {@code Map}，\n\t * 同名已有对象优先（即不会被替换）。\n\t */",
        ),
        (
            "\t/**\n\t * Does this model contain an attribute of the given name?\n\t * @param attributeName the name of the model attribute (never {@code null})\n\t * @return whether this model contains a corresponding attribute\n\t */",
            "\t/**\n\t * 本模型是否包含给定名称的属性？\n\t * @param attributeName 模型属性名称（永不为 {@code null}）\n\t * @return 本模型是否包含对应属性\n\t */",
        ),
    ],
    "ModelMap.java": [
        (
            "/**\n * Implementation of {@link java.util.Map} for use when building model data for use\n * with UI tools. Supports chained calls and generation of model attribute names.\n *\n * <p>This class serves as generic model holder for Servlet MVC but is not tied to it.\n * Check out the {@link Model} interface for an interface variant.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n * @see Conventions#getVariableName\n * @see org.springframework.web.servlet.ModelAndView\n */",
            "/**\n * 用于构建 UI 工具所用模型数据的 {@link java.util.Map} 实现。\n * 支持链式调用和模型属性名生成。\n *\n * <p>本类作为 Servlet MVC 的通用模型持有者，但不与其绑定。\n * 接口变体见 {@link Model} 接口。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n * @see Conventions#getVariableName\n * @see org.springframework.web.servlet.ModelAndView\n */",
        ),
        (
            "\t/**\n\t * Construct a new, empty {@code ModelMap}.\n\t */",
            "\t/**\n\t * 构造新的空 {@code ModelMap}。\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new {@code ModelMap} containing the supplied attribute\n\t * under the supplied name.\n\t * @see #addAttribute(String, Object)\n\t */",
            "\t/**\n\t * 构造包含所提供属性（以所提供名称）的新 {@code ModelMap}。\n\t * @see #addAttribute(String, Object)\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new {@code ModelMap} containing the supplied attribute.\n\t * Uses attribute name generation to generate the key for the supplied model\n\t * object.\n\t * @see #addAttribute(Object)\n\t */",
            "\t/**\n\t * 构造包含所提供属性的新 {@code ModelMap}。\n\t * 使用属性名生成器为所提供模型对象生成键。\n\t * @see #addAttribute(Object)\n\t */",
        ),
        (
            "\t/**\n\t * Add the supplied attribute under the supplied name.\n\t * @param attributeName the name of the model attribute (never {@code null})\n\t * @param attributeValue the model attribute value (can be {@code null})\n\t */",
            "\t/**\n\t * 以给定名称添加所提供的属性。\n\t * @param attributeName 模型属性名称（永不为 {@code null}）\n\t * @param attributeValue 模型属性值（可为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Add the supplied attribute to this {@code Map} using a\n\t * {@link org.springframework.core.Conventions#getVariableName generated name}.\n\t * <p><i>Note: Empty {@link Collection Collections} are not added to\n\t * the model when using this method because we cannot correctly determine\n\t * the true convention name. View code should check for {@code null} rather\n\t * than for empty collections as is already done by JSTL tags.</i>\n\t * @param attributeValue the model attribute value (never {@code null})\n\t */",
            "\t/**\n\t * 使用 {@link org.springframework.core.Conventions#getVariableName 生成的名称}\n\t * 将所提供的属性添加到本 {@code Map}。\n\t * <p><i>注意：使用本方法时，空的 {@link Collection Collection}\n\t * 不会添加到模型，因为我们无法正确确定真正的约定名称。\n\t * 视图代码应检查 {@code null} 而非空集合，与 JSTL 标签的做法一致。</i>\n\t * @param attributeValue 模型属性值（永不为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Copy all attributes in the supplied {@code Collection} into this\n\t * {@code Map}, using attribute name generation for each element.\n\t * @see #addAttribute(Object)\n\t */",
            "\t/**\n\t * 将所提供 {@code Collection} 中的所有属性复制到本 {@code Map}，\n\t * 为每个元素生成属性名。\n\t * @see #addAttribute(Object)\n\t */",
        ),
        (
            "\t/**\n\t * Copy all attributes in the supplied {@code Map} into this {@code Map}.\n\t * @see #addAttribute(String, Object)\n\t */",
            "\t/**\n\t * 将所提供 {@code Map} 中的所有属性复制到本 {@code Map}。\n\t * @see #addAttribute(String, Object)\n\t */",
        ),
        (
            "\t/**\n\t * Copy all attributes in the supplied {@code Map} into this {@code Map},\n\t * with existing objects of the same name taking precedence (i.e. not getting\n\t * replaced).\n\t */",
            "\t/**\n\t * 将所提供 {@code Map} 中的所有属性复制到本 {@code Map}，\n\t * 同名已有对象优先（即不会被替换）。\n\t */",
        ),
        (
            "\t/**\n\t * Does this model contain an attribute of the given name?\n\t * @param attributeName the name of the model attribute (never {@code null})\n\t * @return whether this model contains a corresponding attribute\n\t */",
            "\t/**\n\t * 本模型是否包含给定名称的属性？\n\t * @param attributeName 模型属性名称（永不为 {@code null}）\n\t * @return 本模型是否包含对应属性\n\t */",
        ),
        (
            "\t/**\n\t * Return the attribute value for the given name, if any.\n\t * @param attributeName the name of the model attribute (never {@code null})\n\t * @return the corresponding attribute value, or {@code null} if none\n\t * @since 5.2\n\t */",
            "\t/**\n\t * 返回给定名称的属性值（若有）。\n\t * @param attributeName 模型属性名称（永不为 {@code null}）\n\t * @return 对应的属性值，若无则返回 {@code null}\n\t * @since 5.2\n\t */",
        ),
    ],
    "AbstractBindingResult.java": [
        (
            "/**\n * Abstract implementation of the {@link BindingResult} interface and\n * its super-interface {@link Errors}. Encapsulates common management of\n * {@link ObjectError ObjectErrors} and {@link FieldError FieldErrors}.\n *\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @since 2.0\n * @see Errors\n */",
            "/**\n * {@link BindingResult} 接口及其超接口 {@link Errors} 的抽象实现。\n * 封装 {@link ObjectError ObjectErrors} 与 {@link FieldError FieldErrors} 的通用管理。\n *\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @since 2.0\n * @see Errors\n */",
        ),
        (
            "\t/**\n\t * Create a new AbstractBindingResult instance.\n\t * @param objectName the name of the target object\n\t * @see DefaultMessageCodesResolver\n\t */",
            "\t/**\n\t * 创建新的 AbstractBindingResult 实例。\n\t * @param objectName 目标对象名称\n\t * @see DefaultMessageCodesResolver\n\t */",
        ),
        (
            "\t/**\n\t * Set the strategy to use for resolving errors into message codes.\n\t * Default is DefaultMessageCodesResolver.\n\t * @see DefaultMessageCodesResolver\n\t */",
            "\t/**\n\t * 设置将错误解析为消息代码的策略。\n\t * 默认为 DefaultMessageCodesResolver。\n\t * @see DefaultMessageCodesResolver\n\t */",
        ),
        (
            "\t/**\n\t * Return the strategy to use for resolving errors into message codes.\n\t */",
            "\t/**\n\t * 返回将错误解析为消息代码的策略。\n\t */",
        ),
        (
            "\t/**\n\t * This default implementation determines the type based on the actual\n\t * field value, if any. Subclasses should override this to determine\n\t * the type from a descriptor, even for {@code null} values.\n\t * @see #getActualFieldValue\n\t */",
            "\t/**\n\t * 本默认实现基于实际字段值（若有）确定类型。\n\t * 子类应覆盖此方法，从描述符确定类型，即使值为 {@code null}。\n\t * @see #getActualFieldValue\n\t */",
        ),
        (
            "\t/**\n\t * Return a model Map for the obtained state, exposing an Errors\n\t * instance as '{@link #MODEL_KEY_PREFIX MODEL_KEY_PREFIX} + objectName'\n\t * and the object itself.\n\t * <p>Note that the Map is constructed every time you're calling this method.\n\t * Adding things to the map and then re-calling this method will not work.\n\t * <p>The attributes in the model Map returned by this method are usually\n\t * included in the ModelAndView for a form view that uses Spring's bind tag,\n\t * which needs access to the Errors instance.\n\t * @see #getObjectName\n\t * @see #MODEL_KEY_PREFIX\n\t */",
            "\t/**\n\t * 返回所获状态的模型 Map，将 Errors 实例暴露为\n\t * '{@link #MODEL_KEY_PREFIX MODEL_KEY_PREFIX} + objectName' 以及对象本身。\n\t * <p>注意，每次调用本方法都会构造 Map。\n\t * 向 Map 添加内容后再重新调用本方法无效。\n\t * <p>本方法返回的模型 Map 中的属性通常包含在使用 Spring bind 标签的\n\t * 表单视图的 ModelAndView 中，该标签需要访问 Errors 实例。\n\t * @see #getObjectName\n\t * @see #MODEL_KEY_PREFIX\n\t */",
        ),
        (
            "\t/**\n\t * This implementation delegates to the\n\t * {@link #getPropertyEditorRegistry() PropertyEditorRegistry}'s\n\t * editor lookup facility, if available.\n\t */",
            "\t/**\n\t * 本实现委托 {@link #getPropertyEditorRegistry() PropertyEditorRegistry}\n\t * 的编辑器查找功能（若可用）。\n\t */",
        ),
        (
            "\t/**\n\t * This implementation returns {@code null}.\n\t */",
            "\t/**\n\t * 本实现返回 {@code null}。\n\t */",
        ),
        (
            "\t/**\n\t * Mark the specified disallowed field as suppressed.\n\t * <p>The data binder invokes this for each field value that was\n\t * detected to target a disallowed field.\n\t * @see DataBinder#setAllowedFields\n\t */",
            "\t/**\n\t * 将指定不允许的字段标记为已抑制。\n\t * <p>数据绑定器对每个检测到指向不允许字段的字段值调用此方法。\n\t * @see DataBinder#setAllowedFields\n\t */",
        ),
        (
            "\t/**\n\t * Return the list of fields that were suppressed during the bind process.\n\t * <p>Can be used to determine whether any field values were targeting\n\t * disallowed fields.\n\t * @see DataBinder#setAllowedFields\n\t */",
            "\t/**\n\t * 返回绑定过程中被抑制的字段列表。\n\t * <p>可用于判断是否有字段值指向不允许的字段。\n\t * @see DataBinder#setAllowedFields\n\t */",
        ),
        (
            "\t/**\n\t * Return the wrapped target object.\n\t */",
            "\t/**\n\t * 返回包装的目标对象。\n\t */",
        ),
        (
            "\t/**\n\t * Extract the actual field value for the given field.\n\t * @param field the field to check\n\t * @return the current value of the field\n\t */",
            "\t/**\n\t * 提取给定字段的实际字段值。\n\t * @param field 要检查的字段\n\t * @return 字段的当前值\n\t */",
        ),
        (
            "\t/**\n\t * Format the given value for the specified field.\n\t * <p>The default implementation simply returns the field value as-is.\n\t * @param field the field to check\n\t * @param value the value of the field (either a rejected value\n\t * other than from a binding error, or an actual field value)\n\t * @return the formatted value\n\t */",
            "\t/**\n\t * 格式化给定字段的给定值。\n\t * <p>默认实现直接返回字段值。\n\t * @param field 要检查的字段\n\t * @param value 字段值（非绑定错误的拒绝值或实际字段值）\n\t * @return 格式化后的值\n\t */",
        ),
    ],
}
