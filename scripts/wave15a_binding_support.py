"""Chinese JavaDoc replacements for springframework wave15a binding support [0:2]."""

BINDING_SUPPORT_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "BindingAwareConcurrentModel.java": [
        (
            "/**\n * Subclass of {@link ConcurrentModel} that automatically removes\n * the {@link BindingResult} object when its corresponding\n * target attribute is replaced through regular {@link Map} operations.\n *\n * <p>This is the class exposed to handler methods by Spring WebFlux,\n * typically consumed through a declaration of the\n * {@link org.springframework.ui.Model} interface as a parameter type.\n * There is typically no need to create it within user code.\n * If necessary a handler method can return a regular {@code java.util.Map},\n * likely a {@code java.util.ConcurrentMap}, for a pre-determined model.\n *\n * @author Rossen Stoyanchev\n * @since 5.0\n * @see BindingResult\n * @see BindingAwareModelMap\n */",
            "/**\n * {@link ConcurrentModel} 的子类，当对应目标属性通过常规 {@link Map} 操作被替换时，\n * 自动移除关联的 {@link BindingResult} 对象。\n *\n * <p>Spring WebFlux 向处理器方法暴露的 model 类，\n * 通常通过将 {@link org.springframework.ui.Model} 接口声明为参数类型来使用。\n * 用户代码一般无需自行创建；若确有需要，处理器方法也可返回\n * 普通 {@code java.util.Map}（通常为 {@code java.util.ConcurrentMap}）作为预定 model。\n *\n * @author Rossen Stoyanchev\n * @since 5.0\n * @see BindingResult\n * @see BindingAwareModelMap\n */",
        ),
    ],
    "BindingAwareModelMap.java": [
        (
            "/**\n * Subclass of {@link org.springframework.ui.ExtendedModelMap} that automatically removes\n * a {@link org.springframework.validation.BindingResult} object if the corresponding\n * target attribute gets replaced through regular {@link Map} operations.\n *\n * <p>This is the class exposed to handler methods by Spring MVC, typically consumed through\n * a declaration of the {@link org.springframework.ui.Model} interface. There is no need to\n * build it within user code; a plain {@link org.springframework.ui.ModelMap} or even a just\n * a regular {@link Map} with String keys will be good enough to return a user model.\n *\n * @author Juergen Hoeller\n * @since 2.5.6\n * @see org.springframework.validation.BindingResult\n */",
            "/**\n * {@link org.springframework.ui.ExtendedModelMap} 的子类，当对应目标属性通过常规 {@link Map} 操作被替换时，\n * 自动移除关联的 {@link org.springframework.validation.BindingResult} 对象。\n *\n * <p>Spring MVC 向处理器方法暴露的 model 类，\n * 通常通过声明 {@link org.springframework.ui.Model} 接口来使用。\n * 用户代码无需自行构建；普通 {@link org.springframework.ui.ModelMap}，\n * 甚至仅含 String 键的常规 {@link Map} 也足以返回用户 model。\n *\n * @author Juergen Hoeller\n * @since 2.5.6\n * @see org.springframework.validation.BindingResult\n */",
        ),
    ],
}