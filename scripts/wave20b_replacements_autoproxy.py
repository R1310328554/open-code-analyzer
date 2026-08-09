"""Chinese JavaDoc replacements for springframework wave20b aspectj autoproxy [8:9]."""

AUTOPROXY_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AspectJAwareAdvisorAutoProxyCreator.java": [
        (
            "/**\n * {@link org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator}\n * subclass that exposes AspectJ's invocation context and understands AspectJ's rules\n * for advice precedence when multiple pieces of advice come from the same aspect.\n *\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @author Ramnivas Laddad\n * @since 2.0\n */",
            "/**\n * {@link org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator} 的子类，\n * 暴露 AspectJ 调用上下文，并理解同一切面中多条通知的 AspectJ 优先级规则。\n *\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @author Ramnivas Laddad\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Sort the supplied {@link Advisor} instances according to AspectJ precedence.\n\t * <p>If two pieces of advice come from the same aspect, they will have the same\n\t * order. Advice from the same aspect is then further ordered according to the\n\t * following rules:\n\t * <ul>\n\t * <li>If either of the pair is <em>after</em> advice, then the advice declared\n\t * last gets highest precedence (i.e., runs last).</li>\n\t * <li>Otherwise the advice declared first gets highest precedence (i.e., runs\n\t * first).</li>\n\t * </ul>\n\t * <p><b>Important:</b> Advisors are sorted in precedence order, from the highest\n\t * precedence to the lowest. \"On the way in\" to a join point, the highest precedence\n\t * advisor should run first. \"On the way out\" of a join point, the highest\n\t * precedence advisor should run last.\n\t */",
            "\t/**\n\t * 按 AspectJ 优先级对给定 {@link Advisor} 实例排序。\n\t * <p>若两条通知来自同一切面，它们具有相同 order。\n\t * 同一切面的通知再按以下规则进一步排序：\n\t * <ul>\n\t * <li>若其中一条为 <em>after</em> 通知，则后声明的通知优先级最高（即最后运行）。</li>\n\t * <li>否则先声明的通知优先级最高（即最先运行）。</li>\n\t * </ul>\n\t * <p><b>重要：</b>Advisor 按优先级从高到低排序。\n\t * 进入连接点时，最高优先级 Advisor 应最先运行；\n\t * 离开连接点时，最高优先级 Advisor 应最后运行。\n\t */",
        ),
        (
            "\t/**\n\t * Add an {@link ExposeInvocationInterceptor} to the beginning of the advice chain.\n\t * <p>This additional advice is needed when using AspectJ pointcut expressions\n\t * and when using AspectJ-style advice.\n\t */",
            "\t/**\n\t * 在通知链开头添加 {@link ExposeInvocationInterceptor}。\n\t * <p>使用 AspectJ 切入点表达式及 AspectJ 风格通知时需要此额外通知。\n\t */",
        ),
        (
            "\t/**\n\t * Implements AspectJ's {@link PartialComparable} interface for defining partial orderings.\n\t */",
            "\t/**\n\t * 实现 AspectJ 的 {@link PartialComparable} 接口，用于定义偏序关系。\n\t */",
        ),
    ],
    "AspectJPrecedenceComparator.java": [
        (
            "/**\n * Orders AspectJ advice/advisors by precedence (<i>not</i> invocation order).\n *\n * <p>Given two pieces of advice, {@code A} and {@code B}:\n * <ul>\n * <li>If {@code A} and {@code B} are defined in different aspects, then the advice\n * in the aspect with the lowest order value has the highest precedence.</li>\n * <li>If {@code A} and {@code B} are defined in the same aspect, if one of\n * {@code A} or {@code B} is a form of <em>after</em> advice, then the advice declared\n * last in the aspect has the highest precedence. If neither {@code A} nor {@code B}\n * is a form of <em>after</em> advice, then the advice declared first in the aspect\n * has the highest precedence.</li>\n * </ul>\n *\n * <p>Important: This comparator is used with AspectJ's\n * {@link org.aspectj.util.PartialOrder PartialOrder} sorting utility. Thus, unlike\n * a normal {@link Comparator}, a return value of {@code 0} from this comparator\n * means we don't care about the ordering, not that the two elements must be sorted\n * identically.\n *\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 按优先级（<i>非</i>调用顺序）对 AspectJ 通知/Advisor 排序。\n *\n * <p>给定两条通知 {@code A} 与 {@code B}：\n * <ul>\n * <li>若 {@code A} 与 {@code B} 定义在不同切面中，\n * 则 order 值最小的切面中的通知优先级最高。</li>\n * <li>若 {@code A} 与 {@code B} 定义在同一切面中，\n * 若其中一条为 <em>after</em> 通知，则切面中后声明的通知优先级最高；\n * 若两者均非 <em>after</em> 通知，则先声明的通知优先级最高。</li>\n * </ul>\n *\n * <p>重要：本比较器与 AspectJ 的\n * {@link org.aspectj.util.PartialOrder PartialOrder} 排序工具配合使用。\n * 因此与普通 {@link Comparator} 不同，返回 {@code 0} 表示不关心排序，\n * 而非两元素必须完全相同顺序。\n *\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Create a default {@code AspectJPrecedenceComparator}.\n\t */",
            "\t/**\n\t * 创建默认的 {@code AspectJPrecedenceComparator}。\n\t */",
        ),
        (
            "\t/**\n\t * Create an {@code AspectJPrecedenceComparator}, using the given {@link Comparator}\n\t * for comparing {@link org.springframework.aop.Advisor} instances.\n\t * @param advisorComparator the {@code Comparator} to use for advisors\n\t */",
            "\t/**\n\t * 创建 {@code AspectJPrecedenceComparator}，\n\t * 使用给定 {@link Comparator} 比较 {@link org.springframework.aop.Advisor} 实例。\n\t * @param advisorComparator 用于 Advisor 的 {@code Comparator}\n\t */",
        ),
        (
            "\t\t\t// the advice declared last has higher precedence",
            "\t\t\t// 后声明的通知优先级更高",
        ),
        (
            "\t\t\t\t// advice1 was declared before advice2\n\t\t\t\t// so advice1 has lower precedence",
            "\t\t\t\t// advice1 先于 advice2 声明，故 advice1 优先级更低",
        ),
        (
            "\t\t\t// the advice declared first has higher precedence",
            "\t\t\t// 先声明的通知优先级更高",
        ),
        (
            "\t\t\t\t// advice1 was declared before advice2\n\t\t\t\t// so advice1 has higher precedence",
            "\t\t\t\t// advice1 先于 advice2 声明，故 advice1 优先级更高",
        ),
        (
            "\t// pre-condition is that hasAspectName returned true",
            "\t// 前置条件：hasAspectName 已返回 true",
        ),
    ],
}
