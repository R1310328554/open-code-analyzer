"""Chinese JavaDoc replacements for springframework wave23a annotation support [16:18]."""

ANNOTATION_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AnnotationClassFilter.java": [
        (
            "/**\n * Simple ClassFilter that looks for a specific annotation being present on a class.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see AnnotationMatchingPointcut\n */",
            "/**\n * 简单的 ClassFilter，查找类上是否存在特定注解。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see AnnotationMatchingPointcut\n */",
        ),
        (
            "\t/**\n\t * Create a new AnnotationClassFilter for the given annotation type.\n\t * @param annotationType the annotation type to look for\n\t */",
            "\t/**\n\t * 为给定注解类型创建新的 AnnotationClassFilter。\n\t * @param annotationType 要查找的注解类型\n\t */",
        ),
        (
            "\t/**\n\t * Create a new AnnotationClassFilter for the given annotation type.\n\t * @param annotationType the annotation type to look for\n\t * @param checkInherited whether to also check the superclasses and\n\t * interfaces as well as meta-annotations for the annotation type\n\t * (i.e. whether to use {@link AnnotatedElementUtils#hasAnnotation}\n\t * semantics instead of standard Java {@link Class#isAnnotationPresent})\n\t */",
            "\t/**\n\t * 为给定注解类型创建新的 AnnotationClassFilter。\n\t * @param annotationType 要查找的注解类型\n\t * @param checkInherited 是否还检查超类、接口及元注解\n\t * （即是否使用 {@link AnnotatedElementUtils#hasAnnotation}\n\t * 语义而非标准 Java {@link Class#isAnnotationPresent}）\n\t */",
        ),
    ],
    "AnnotationMatchingPointcut.java": [
        (
            "/**\n * Simple {@link Pointcut} that looks for a specific annotation being present on a\n * {@linkplain #forClassAnnotation class} or {@linkplain #forMethodAnnotation method}.\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.0\n * @see AnnotationClassFilter\n * @see AnnotationMethodMatcher\n */",
            "/**\n * 简单的 {@link Pointcut}，查找\n * {@linkplain #forClassAnnotation 类}或{@linkplain #forMethodAnnotation 方法}上\n * 是否存在特定注解。\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.0\n * @see AnnotationClassFilter\n * @see AnnotationMethodMatcher\n */",
        ),
        (
            "\t/**\n\t * Create a new AnnotationMatchingPointcut for the given annotation type.\n\t * @param classAnnotationType the annotation type to look for at the class level\n\t */",
            "\t/**\n\t * 为给定注解类型创建新的 AnnotationMatchingPointcut。\n\t * @param classAnnotationType 类级别要查找的注解类型\n\t */",
        ),
        (
            "\t/**\n\t * Create a new AnnotationMatchingPointcut for the given annotation type.\n\t * @param classAnnotationType the annotation type to look for at the class level\n\t * @param checkInherited whether to also check the superclasses and interfaces\n\t * as well as meta-annotations for the annotation type\n\t * @see AnnotationClassFilter#AnnotationClassFilter(Class, boolean)\n\t */",
            "\t/**\n\t * 为给定注解类型创建新的 AnnotationMatchingPointcut。\n\t * @param classAnnotationType 类级别要查找的注解类型\n\t * @param checkInherited 是否还检查超类、接口及元注解\n\t * @see AnnotationClassFilter#AnnotationClassFilter(Class, boolean)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new AnnotationMatchingPointcut for the given annotation types.\n\t * @param classAnnotationType the annotation type to look for at the class level\n\t * (can be {@code null})\n\t * @param methodAnnotationType the annotation type to look for at the method level\n\t * (can be {@code null})\n\t */",
            "\t/**\n\t * 为给定注解类型创建新的 AnnotationMatchingPointcut。\n\t * @param classAnnotationType 类级别要查找的注解类型\n\t * （可为 {@code null}）\n\t * @param methodAnnotationType 方法级别要查找的注解类型\n\t * （可为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new AnnotationMatchingPointcut for the given annotation types.\n\t * @param classAnnotationType the annotation type to look for at the class level\n\t * (can be {@code null})\n\t * @param methodAnnotationType the annotation type to look for at the method level\n\t * (can be {@code null})\n\t * @param checkInherited whether to also check the superclasses and interfaces\n\t * as well as meta-annotations for the annotation type\n\t * @since 5.0\n\t * @see AnnotationClassFilter#AnnotationClassFilter(Class, boolean)\n\t * @see AnnotationMethodMatcher#AnnotationMethodMatcher(Class, boolean)\n\t */",
            "\t/**\n\t * 为给定注解类型创建新的 AnnotationMatchingPointcut。\n\t * @param classAnnotationType 类级别要查找的注解类型\n\t * （可为 {@code null}）\n\t * @param methodAnnotationType 方法级别要查找的注解类型\n\t * （可为 {@code null}）\n\t * @param checkInherited 是否还检查超类、接口及元注解\n\t * @since 5.0\n\t * @see AnnotationClassFilter#AnnotationClassFilter(Class, boolean)\n\t * @see AnnotationMethodMatcher#AnnotationMethodMatcher(Class, boolean)\n\t */",
        ),
        (
            "\t/**\n\t * Factory method for an AnnotationMatchingPointcut that matches\n\t * for the specified annotation at the class level.\n\t * @param annotationType the annotation type to look for at the class level\n\t * @return the corresponding AnnotationMatchingPointcut\n\t */",
            "\t/**\n\t * 创建在类级别匹配指定注解的 AnnotationMatchingPointcut 的工厂方法。\n\t * @param annotationType 类级别要查找的注解类型\n\t * @return 对应的 AnnotationMatchingPointcut\n\t */",
        ),
        (
            "\t/**\n\t * Factory method for an AnnotationMatchingPointcut that matches\n\t * for the specified annotation at the method level.\n\t * @param annotationType the annotation type to look for at the method level\n\t * @return the corresponding AnnotationMatchingPointcut\n\t */",
            "\t/**\n\t * 创建在方法级别匹配指定注解的 AnnotationMatchingPointcut 的工厂方法。\n\t * @param annotationType 方法级别要查找的注解类型\n\t * @return 对应的 AnnotationMatchingPointcut\n\t */",
        ),
        (
            "\t/**\n\t * {@link ClassFilter} that delegates to {@link AnnotationUtils#isCandidateClass}\n\t * for filtering classes whose methods are not worth searching to begin with.\n\t * @since 5.2\n\t */",
            "\t/**\n\t * 委托给 {@link AnnotationUtils#isCandidateClass} 的 {@link ClassFilter}，\n\t * 用于过滤其方法不值得搜索的类。\n\t * @since 5.2\n\t */",
        ),
    ],
    "AnnotationMethodMatcher.java": [
        (
            "/**\n * Simple {@link org.springframework.aop.MethodMatcher MethodMatcher} that looks for\n * a specific annotation being present on a method (checking both the method on the\n * invoked interface, if any, and the corresponding method on the target class).\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.0\n * @see AnnotationMatchingPointcut\n */",
            "/**\n * 简单的 {@link org.springframework.aop.MethodMatcher MethodMatcher}，\n * 查找方法上是否存在特定注解\n * （同时检查被调用接口上的方法（若有）及目标类上的对应方法）。\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.0\n * @see AnnotationMatchingPointcut\n */",
        ),
        (
            "\t/**\n\t * Create a new AnnotationClassFilter for the given annotation type.\n\t * @param annotationType the annotation type to look for\n\t */",
            "\t/**\n\t * 为给定注解类型创建新的 AnnotationMethodMatcher。\n\t * @param annotationType 要查找的注解类型\n\t */",
        ),
        (
            "\t/**\n\t * Create a new AnnotationClassFilter for the given annotation type.\n\t * @param annotationType the annotation type to look for\n\t * @param checkInherited whether to also check the superclasses and\n\t * interfaces as well as meta-annotations for the annotation type\n\t * (i.e. whether to use {@link AnnotatedElementUtils#hasAnnotation}\n\t * semantics instead of standard Java {@link Method#isAnnotationPresent})\n\t * @since 5.0\n\t */",
            "\t/**\n\t * 为给定注解类型创建新的 AnnotationMethodMatcher。\n\t * @param annotationType 要查找的注解类型\n\t * @param checkInherited 是否还检查超类、接口及元注解\n\t * （即是否使用 {@link AnnotatedElementUtils#hasAnnotation}\n\t * 语义而非标准 Java {@link Method#isAnnotationPresent}）\n\t * @since 5.0\n\t */",
        ),
        (
            "\t\t// Proxy classes never have annotations on their redeclared methods.",
            "\t\t// 代理类在其重新声明的方法上永无注解。",
        ),
        (
            "\t\t// The method may be on an interface, so let's check on the target class as well.",
            "\t\t// 方法可能在接口上，因此也检查目标类。",
        ),
    ],
}
