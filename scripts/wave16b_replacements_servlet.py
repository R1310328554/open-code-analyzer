"""Chinese JavaDoc replacements for ServletContextInitializerBeans."""

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ServletContextInitializerBeans.java": [
        (
            "/**\n * A collection {@link ServletContextInitializer}s obtained from a\n * {@link ListableBeanFactory}. Includes all {@link ServletContextInitializer} beans and\n * also adapts {@link Servlet}, {@link Filter} and certain {@link EventListener} beans.\n * <p>\n * Items are sorted so that adapted beans are top ({@link Servlet}, {@link Filter} then\n * {@link EventListener}) and direct {@link ServletContextInitializer} beans are at the\n * end. Further sorting is applied within these groups using the\n * {@link AnnotationAwareOrderComparator}.\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Brian Clozel\n * @author Moritz Halbritter\n * @author Daeho Kwon\n * @author Dmytro Danilenkov\n * @since 1.4.0\n */",
            "/**\n * 从 {@link ListableBeanFactory} 获取的 {@link ServletContextInitializer} 集合。\n * 包含所有 {@link ServletContextInitializer} Bean，并将 {@link Servlet}、{@link Filter}\n * 及特定 {@link EventListener} Bean 适配为初始化器。\n * <p>\n * 排序规则：适配 Bean 优先（{@link Servlet}、{@link Filter}、{@link EventListener}），\n * 直接的 {@link ServletContextInitializer} Bean 排在末尾；\n * 各组内再按 {@link AnnotationAwareOrderComparator} 排序。\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Brian Clozel\n * @author Moritz Halbritter\n * @author Daeho Kwon\n * @author Dmytro Danilenkov\n * @since 1.4.0\n */",
        ),
        (
            "\t/**\n\t * Seen bean instances or bean names.\n\t */",
            "\t/**\n\t * 已见过的 Bean 实例或 Bean 名称。\n\t */",
        ),
        (
            "\t\t\t// Mark the underlying source as seen in case it wraps an existing bean",
            "\t\t\t// 标记底层源为已见，以防其包装了已有 Bean",
        ),
        (
            "\t\t\t\t// One that we haven't already seen",
            "\t\t\t\t// 尚未处理过的新 Bean",
        ),
        (
            "\t/**\n\t * Adapter to convert a given Bean type into a {@link RegistrationBean} (and hence a\n\t * {@link ServletContextInitializer}).\n\t *\n\t * @param <T> the type of the Bean to adapt\n\t */",
            "\t/**\n\t * 将给定 Bean 类型转换为 {@link RegistrationBean}（进而成为 {@link ServletContextInitializer}）的适配器。\n\t *\n\t * @param <T> the type of the Bean to adapt 待适配的 Bean 类型\n\t */",
        ),
        (
            "\t/**\n\t * {@link RegistrationBeanAdapter} for {@link Servlet} beans.\n\t */",
            "\t/**\n\t * 针对 {@link Servlet} Bean 的 {@link RegistrationBeanAdapter}。\n\t */",
        ),
        (
            "\t\t\t\turl = \"/\"; // always map the main dispatcherServlet to \"/\"",
            "\t\t\t\turl = \"/\"; // 始终将主 dispatcherServlet 映射到 \"/\"",
        ),
        (
            "\t/**\n\t * {@link RegistrationBeanAdapter} implementation for {@link Filter} beans.\n\t * <p>\n\t * <b>NOTE:</b> A similar implementation is used in\n\t * {@code SpringBootMockMvcBuilderCustomizer} for registering\n\t * {@code @FilterRegistration} beans with {@code @MockMvc}. If you modify this class,\n\t * please also update {@code SpringBootMockMvcBuilderCustomizer} if needed.\n\t * </p>\n\t */",
            "\t/**\n\t * 针对 {@link Filter} Bean 的 {@link RegistrationBeanAdapter} 实现。\n\t * <p>\n\t * <b>注意：</b>{@code SpringBootMockMvcBuilderCustomizer} 中有类似实现，\n\t * 用于向 {@code @MockMvc} 注册 {@code @FilterRegistration} Bean。\n\t * 若修改本类，请同步检查 {@code SpringBootMockMvcBuilderCustomizer}。\n\t * </p>\n\t */",
        ),
        (
            "\t/**\n\t * {@link RegistrationBeanAdapter} for certain {@link EventListener} beans.\n\t */",
            "\t/**\n\t * 针对特定 {@link EventListener} Bean 的 {@link RegistrationBeanAdapter}。\n\t */",
        ),
        (
            "\t/**\n\t * Tracks seen initializers.\n\t */",
            "\t/**\n\t * 跟踪已处理的初始化器，避免重复注册。\n\t */",
        ),
        (
            "\t\t\t// If it has been directly seen, or the implemented ServletContextInitializer\n\t\t\t// has been seen already",
            "\t\t\t// 若已直接见过，或其实现的 ServletContextInitializer 已被处理",
        ),
    ],
}
