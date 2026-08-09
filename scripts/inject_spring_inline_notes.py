#!/usr/bin/env python3
"""在 analyzed 核心方法内插入高质量行内中文导读（仅复杂主路径）。"""
from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "springframework" / "7.0.8" / "analyzed"
MARK = "// [OCA]"


def inject_after(text: str, anchor: str, note: str, *, once: bool = True) -> str:
    if note in text:
        return text
    idx = text.find(anchor)
    if idx < 0:
        return text
    # 插在 anchor 行之后
    end = text.find("\n", idx)
    if end < 0:
        return text
    # 推断缩进
    line_start = text.rfind("\n", 0, idx) + 1
    indent = ""
    for ch in text[line_start:idx]:
        if ch in " \t":
            indent += ch
        else:
            break
    insertion = f"{indent}{MARK} {note}\n"
    return text[: end + 1] + insertion + text[end + 1 :]


def patch_file(rel: str, patches: list[tuple[str, str]]) -> None:
    path = ROOT / rel
    text = path.read_text(encoding="utf-8")
    original = text
    for anchor, note in patches:
        text = inject_after(text, anchor, note)
    if text != original:
        path.write_text(text, encoding="utf-8")
        print(f"patched {rel}")
    else:
        print(f"nochange {rel}")


def main() -> None:
    patch_file(
        "spring-beans/src/main/java/org/springframework/beans/factory/support/AbstractBeanFactory.java",
        [
            (
                "Object sharedInstance = getSingleton(beanName);",
                "先查单例缓存（含创建中的早期引用）。命中且无显式 args，通常直接复用，不再 createBean。",
            ),
            (
                "if (isPrototypeCurrentlyInCreation(beanName)) {",
                "原型不支持循环依赖：若创建栈里已有同名原型，直接失败（与单例策略不同）。",
            ),
            (
                "if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {",
                "本工厂没有定义则委派父工厂——层次化容器的关键路径。",
            ),
            (
                "String[] dependsOn = mbd.getDependsOn();",
                "depends-on 是显式依赖边，先于当前 Bean 初始化；与属性注入形成的隐式依赖不同。",
            ),
            (
                "if (mbd.isSingleton()) {",
                "单例：交由 getSingleton(beanName, ObjectFactory) 确保并发下只创建一次，并在失败时清理半成品。",
            ),
            (
                "else if (mbd.isPrototype()) {",
                "原型：每次 createBean；用 before/afterPrototypeCreation 做创建中标记。",
            ),
        ],
    )

    patch_file(
        "spring-beans/src/main/java/org/springframework/beans/factory/support/DefaultSingletonBeanRegistry.java",
        [
            (
                "Object singletonObject = this.singletonObjects.get(beanName);",
                "一级缓存：已完成的成品单例。",
            ),
            (
                "singletonObject = this.earlySingletonObjects.get(beanName);",
                "二级缓存：早期暴露对象（可能已是代理）。",
            ),
            (
                "ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);",
                "三级缓存：对象工厂。第一次取早期引用时 getObject()，常用于生成 AOP 代理，然后升到二级。",
            ),
            (
                "this.earlySingletonObjects.put(beanName, singletonObject);",
                "升舱：三级 → 二级，避免重复创建早期引用。",
            ),
        ],
    )

    patch_file(
        "spring-context/src/main/java/org/springframework/context/support/AbstractApplicationContext.java",
        [
            (
                "prepareRefresh();",
                "准备阶段：启动时间、状态、Environment 校验、早期事件集合初始化。",
            ),
            (
                "ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();",
                "子类在此刷新内部 BeanFactory（例如读取 XML/注解配置后的工厂）。",
            ),
            (
                "prepareBeanFactory(beanFactory);",
                "注册标准依赖（BeanFactory/ResourceLoader 等）与若干后置处理器/忽略依赖接口。",
            ),
            (
                "invokeBeanFactoryPostProcessors(beanFactory);",
                "工厂级后置处理：@Configuration 解析、属性占位符等在此发生，此时通常还没开始实例化业务 Bean。",
            ),
            (
                "registerBeanPostProcessors(beanFactory);",
                "注册 Bean 级后置处理器：后续 createBean 会走它们（Autowired/AOP 等）。",
            ),
            (
                "finishBeanFactoryInitialization(beanFactory);",
                "冻结定义并预实例化非懒加载单例——应用对象真正被创建的高峰期。",
            ),
            (
                "finishRefresh();",
                "发布 ContextRefreshedEvent、启动 Lifecyle 处理器等收尾工作。",
            ),
        ],
    )

    patch_file(
        "spring-webmvc/src/main/java/org/springframework/web/servlet/DispatcherServlet.java",
        [
            (
                "processedRequest = checkMultipart(request);",
                "若是 multipart 请求，先包装成 MultipartHttpServletRequest。",
            ),
            (
                "mappedHandler = getHandler(processedRequest);",
                "遍历 HandlerMapping，找到能处理该请求的 HandlerExecutionChain（含拦截器）。",
            ),
            (
                "HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());",
                "适配器模式：同一套分发逻辑可执行 Controller/@RequestMapping 方法等不同 Handler 类型。",
            ),
            (
                "if (!mappedHandler.applyPreHandle(processedRequest, response)) {",
                "拦截器 preHandle 返回 false：短路请求，不再调用 Controller。",
            ),
        ],
    )

    patch_file(
        "spring-tx/src/main/java/org/springframework/transaction/interceptor/TransactionAspectSupport.java",
        [
            (
                "TransactionInfo txInfo = createTransactionIfNecessary(ptm, txAttr, joinpointIdentification);",
                "按传播行为决定：新建 / 加入 / 挂起当前事务。理解 @Transactional 十有八九卡在这里。",
            ),
            (
                "completeTransactionAfterThrowing(txInfo, ex);",
                "异常路径：对照 rollbackFor/noRollbackFor 决定回滚还是提交。",
            ),
            (
                "commitTransactionAfterReturning(txInfo);",
                "正常返回：提交（或在参与型传播下延迟到外层事务提交）。",
            ),
        ],
    )


if __name__ == "__main__":
    main()
