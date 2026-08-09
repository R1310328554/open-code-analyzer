#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-15a slice [0:6] (servlet registration)."""
from __future__ import annotations

import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springboot/4.1.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
BATCH_FILES = Path("/tmp/springboot_w15a.txt").read_text(encoding="utf-8").strip().splitlines()

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "FilterRegistration.java": [
        (
            "/**\n * Registers a {@link Filter} in a Servlet 3.0+ container. Can be used as an\n * annotation-based alternative to {@link FilterRegistrationBean}.\n *\n * @author Moritz Halbritter\n * @author Daeho Kwon\n * @since 3.5.0\n * @see FilterRegistrationBean\n */",
            "/**\n * 在 Servlet 3.0+ 容器中注册 {@link Filter}。\n * 可作为 {@link FilterRegistrationBean} 的基于注解替代方案。\n *\n * @author Moritz Halbritter\n * @author Daeho Kwon\n * @since 3.5.0\n * @see FilterRegistrationBean\n */",
        ),
        (
            "\t/**\n\t * Whether this registration is enabled.\n\t * @return whether this registration is enabled\n\t */",
            "\t/**\n\t * 此注册是否启用。\n\t *\n\t * @return whether this registration is enabled 是否启用此注册\n\t */",
        ),
        (
            "\t/**\n\t * Order of the registration bean.\n\t * @return the order of the registration bean\n\t */",
            "\t/**\n\t * 注册 Bean 的执行顺序。\n\t *\n\t * @return the order of the registration bean 注册 Bean 的顺序值\n\t */",
        ),
        (
            "\t/**\n\t * Name of this registration. If not specified the bean name will be used.\n\t * @return the name\n\t */",
            "\t/**\n\t * 此注册的名称。未指定时将使用 Bean 名称。\n\t *\n\t * @return the name 注册名称\n\t */",
        ),
        (
            "\t/**\n\t * Whether asynchronous operations are supported for this registration.\n\t * @return whether asynchronous operations are supported\n\t */",
            "\t/**\n\t * 此注册是否支持异步操作。\n\t *\n\t * @return whether asynchronous operations are supported 是否支持异步操作\n\t */",
        ),
        (
            "\t/**\n\t * Dispatcher types that should be used with the registration.\n\t * @return the dispatcher types\n\t */",
            "\t/**\n\t * 与此注册配合使用的分发器类型（DispatcherType）。\n\t *\n\t * @return the dispatcher types 分发器类型数组\n\t */",
        ),
        (
            "\t/**\n\t * Whether registration failures should be ignored. If set to true, a failure will be\n\t * logged. If set to false, an {@link IllegalStateException} will be thrown.\n\t * @return whether registration failures should be ignored\n\t */",
            "\t/**\n\t * 是否忽略注册失败。为 {@code true} 时仅记录日志；\n\t * 为 {@code false} 时抛出 {@link IllegalStateException}。\n\t *\n\t * @return whether registration failures should be ignored 是否忽略注册失败\n\t */",
        ),
        (
            "\t/**\n\t * Init parameters to be used with the filter.\n\t * @return the init parameters\n\t */",
            "\t/**\n\t * 过滤器使用的初始化参数。\n\t *\n\t * @return the init parameters 初始化参数\n\t */",
        ),
        (
            "\t/**\n\t * Whether the filter mappings should be matched after any declared Filter mappings of\n\t * the ServletContext.\n\t * @return whether the filter mappings should be matched after any declared Filter\n\t * mappings of the ServletContext\n\t */",
            "\t/**\n\t * 过滤器映射是否应在 ServletContext 中已声明的 Filter 映射之后匹配。\n\t *\n\t * @return whether the filter mappings should be matched after any declared Filter\n\t * mappings of the ServletContext 是否在 ServletContext 已声明映射之后匹配\n\t */",
        ),
        (
            "\t/**\n\t * Servlet names that the filter will be registered against.\n\t * @return the servlet names\n\t */",
            "\t/**\n\t * 过滤器将注册到的 Servlet 名称。\n\t *\n\t * @return the servlet names Servlet 名称数组\n\t */",
        ),
        (
            "\t/**\n\t * Servlet classes that the filter will be registered against.\n\t * @return the servlet classes\n\t */",
            "\t/**\n\t * 过滤器将注册到的 Servlet 类。\n\t *\n\t * @return the servlet classes Servlet 类数组\n\t */",
        ),
        (
            "\t/**\n\t * URL patterns, as defined in the Servlet specification, that the filter will be\n\t * registered against.\n\t * @return the url patterns\n\t */",
            "\t/**\n\t * 按 Servlet 规范定义的、过滤器将注册到的 URL 模式。\n\t *\n\t * @return the url patterns URL 模式数组\n\t */",
        ),
    ],
    "FilterRegistrationBean.java": [
        (
            "/**\n * A {@link ServletContextInitializer} to register {@link Filter}s in a Servlet 3.0+\n * container. Similar to the {@link ServletContext#addFilter(String, Filter) registration}\n * features provided by {@link ServletContext} but with a Spring Bean friendly design.\n * <p>\n * The {@link #setFilter(Filter) Filter} must be specified before calling\n * {@link #onStartup(ServletContext)}. Registrations can be associated with\n * {@link #setUrlPatterns URL patterns} and/or servlets (either by {@link #setServletNames\n * name} or through a {@link #setServletRegistrationBeans ServletRegistrationBean}s). When\n * no URL pattern or servlets are specified the filter will be associated to '/*'. The\n * filter name will be deduced if not specified.\n *\n * @param <T> the type of {@link Filter} to register\n * @author Phillip Webb\n * @since 1.4.0\n * @see ServletContextInitializer\n * @see ServletContext#addFilter(String, Filter)\n * @see DelegatingFilterProxyRegistrationBean\n * @see FilterRegistration\n */",
            "/**\n * 在 Servlet 3.0+ 容器中注册 {@link Filter} 的 {@link ServletContextInitializer}。\n * 功能类似 {@link ServletContext} 提供的 {@link ServletContext#addFilter(String, Filter) 注册}能力，\n * 但采用对 Spring Bean 更友好的设计。\n * <p>\n * 调用 {@link #onStartup(ServletContext)} 前必须先通过 {@link #setFilter(Filter) setFilter} 指定过滤器。\n * 注册可与 {@link #setUrlPatterns URL 模式} 和/或 Servlet 关联\n *（通过 {@link #setServletNames 名称} 或 {@link #setServletRegistrationBeans ServletRegistrationBean}）。\n * 未指定 URL 模式或 Servlet 时，过滤器将关联到 {@code /*}。\n * 未指定名称时将自动推断过滤器名称。\n *\n * @param <T> the type of {@link Filter} to register 待注册的 {@link Filter} 类型\n * @author Phillip Webb\n * @since 1.4.0\n * @see ServletContextInitializer\n * @see ServletContext#addFilter(String, Filter)\n * @see DelegatingFilterProxyRegistrationBean\n * @see FilterRegistration\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link FilterRegistrationBean} instance.\n\t */",
            "\t/**\n\t * 创建新的 {@link FilterRegistrationBean} 实例。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link FilterRegistrationBean} instance to be registered with the\n\t * specified {@link ServletRegistrationBean}s.\n\t * @param filter the filter to register\n\t * @param servletRegistrationBeans associate {@link ServletRegistrationBean}s\n\t */",
            "\t/**\n\t * 创建新的 {@link FilterRegistrationBean} 实例，并与指定 {@link ServletRegistrationBean} 关联注册。\n\t *\n\t * @param filter the filter to register 待注册的过滤器\n\t * @param servletRegistrationBeans associate {@link ServletRegistrationBean}s 关联的 {@link ServletRegistrationBean}\n\t */",
        ),
        (
            "\t/**\n\t * Set the filter to be registered.\n\t * @param filter the filter\n\t */",
            "\t/**\n\t * 设置待注册的过滤器。\n\t *\n\t * @param filter the filter 过滤器实例\n\t */",
        ),
    ],
    "RegistrationBean.java": [
        (
            "/**\n * Base class for Servlet 3.0+ based registration beans.\n *\n * @author Phillip Webb\n * @since 1.4.0\n * @see ServletRegistrationBean\n * @see FilterRegistrationBean\n * @see DelegatingFilterProxyRegistrationBean\n * @see ServletListenerRegistrationBean\n */",
            "/**\n * 基于 Servlet 3.0+ 的注册 Bean 抽象基类。\n * 实现 {@link ServletContextInitializer} 与 {@link Ordered}，\n * 在 {@link #onStartup(ServletContext)} 中按启用状态与顺序执行注册。\n *\n * @author Phillip Webb\n * @since 1.4.0\n * @see ServletRegistrationBean\n * @see FilterRegistrationBean\n * @see DelegatingFilterProxyRegistrationBean\n * @see ServletListenerRegistrationBean\n */",
        ),
        (
            "\t/**\n\t * Return a description of the registration. For example \"Servlet resourceServlet\"\n\t * @return a description of the registration\n\t */",
            "\t/**\n\t * 返回注册的描述信息，例如 \"Servlet resourceServlet\"。\n\t *\n\t * @return a description of the registration 注册描述\n\t */",
        ),
        (
            "\t/**\n\t * Register this bean with the servlet context.\n\t * @param description a description of the item being registered\n\t * @param servletContext the servlet context\n\t */",
            "\t/**\n\t * 将此 Bean 注册到 Servlet 上下文。\n\t *\n\t * @param description a description of the item being registered 待注册项的描述\n\t * @param servletContext the servlet context Servlet 上下文\n\t */",
        ),
        (
            "\t/**\n\t * Flag to indicate that the registration is enabled.\n\t * @param enabled the enabled to set\n\t */",
            "\t/**\n\t * 设置注册是否启用。\n\t *\n\t * @param enabled the enabled to set 是否启用\n\t */",
        ),
        (
            "\t/**\n\t * Return if the registration is enabled.\n\t * @return if enabled (default {@code true})\n\t */",
            "\t/**\n\t * 返回注册是否启用。\n\t *\n\t * @return if enabled (default {@code true}) 是否启用（默认 {@code true}）\n\t */",
        ),
        (
            "\t/**\n\t * Set the order of the registration bean.\n\t * @param order the order\n\t */",
            "\t/**\n\t * 设置注册 Bean 的执行顺序。\n\t *\n\t * @param order the order 顺序值\n\t */",
        ),
        (
            "\t/**\n\t * Get the order of the registration bean.\n\t * @return the order\n\t */",
            "\t/**\n\t * 获取注册 Bean 的执行顺序。\n\t *\n\t * @return the order 顺序值\n\t */",
        ),
    ],
    "ServletContextInitializer.java": [
        (
            "/**\n * Interface used to configure a Servlet 3.0+ {@link ServletContext context}\n * programmatically. Unlike {@link WebApplicationInitializer}, classes that implement this\n * interface (and do not implement {@link WebApplicationInitializer}) will <b>not</b> be\n * detected by {@link SpringServletContainerInitializer} and hence will not be\n * automatically bootstrapped by the Servlet container.\n * <p>\n * This interface is designed to act in a similar way to\n * {@link ServletContainerInitializer}, but have a lifecycle that's managed by Spring and\n * not the Servlet container.\n * <p>\n * For configuration examples see {@link WebApplicationInitializer}.\n *\n * @author Phillip Webb\n * @since 4.0.0\n * @see WebApplicationInitializer\n */",
            "/**\n * 以编程方式配置 Servlet 3.0+ {@link ServletContext context} 的接口。\n * 与 {@link WebApplicationInitializer} 不同，仅实现本接口（且未实现 {@link WebApplicationInitializer}）的类\n * <b>不会</b>被 {@link SpringServletContainerInitializer} 检测到，\n * 因此不会被 Servlet 容器自动引导。\n * <p>\n * 本接口的设计意图类似 {@link ServletContainerInitializer}，\n * 但生命周期由 Spring 管理而非 Servlet 容器。\n * <p>\n * 配置示例参见 {@link WebApplicationInitializer}。\n *\n * @author Phillip Webb\n * @since 4.0.0\n * @see WebApplicationInitializer\n */",
        ),
        (
            "\t/**\n\t * Configure the given {@link ServletContext} with any servlets, filters, listeners\n\t * context-params and attributes necessary for initialization.\n\t * @param servletContext the {@code ServletContext} to initialize\n\t * @throws ServletException if any call against the given {@code ServletContext}\n\t * throws a {@code ServletException}\n\t */",
            "\t/**\n\t * 为给定 {@link ServletContext} 配置初始化所需的 Servlet、过滤器、监听器、\n\t * 上下文参数及属性。\n\t *\n\t * @param servletContext the {@code ServletContext} to initialize 待初始化的 {@code ServletContext}\n\t * @throws ServletException if any call against the given {@code ServletContext}\n\t * throws a {@code ServletException} 对给定 {@code ServletContext} 的调用抛出 {@code ServletException} 时\n\t */",
        ),
    ],
    "ServletListenerRegistrationBean.java": [
        (
            "/**\n * A {@link ServletContextInitializer} to register {@link EventListener}s in a Servlet\n * 3.0+ container. Similar to the {@link ServletContext#addListener(EventListener)\n * registration} features provided by {@link ServletContext} but with a Spring Bean\n * friendly design.\n *\n * This bean can be used to register the following types of listener:\n * <ul>\n * <li>{@link ServletContextAttributeListener}</li>\n * <li>{@link ServletRequestListener}</li>\n * <li>{@link ServletRequestAttributeListener}</li>\n * <li>{@link HttpSessionAttributeListener}</li>\n * <li>{@link HttpSessionIdListener}</li>\n * <li>{@link HttpSessionListener}</li>\n * <li>{@link ServletContextListener}</li>\n * </ul>\n *\n * @param <T> the type of listener\n * @author Dave Syer\n * @author Phillip Webb\n * @since 1.4.0\n */",
            "/**\n * 在 Servlet 3.0+ 容器中注册 {@link EventListener} 的 {@link ServletContextInitializer}。\n * 功能类似 {@link ServletContext} 提供的 {@link ServletContext#addListener(EventListener) 注册}能力，\n * 但采用对 Spring Bean 更友好的设计。\n * <p>\n * 此 Bean 可用于注册以下类型的监听器：\n * <ul>\n * <li>{@link ServletContextAttributeListener}</li>\n * <li>{@link ServletRequestListener}</li>\n * <li>{@link ServletRequestAttributeListener}</li>\n * <li>{@link HttpSessionAttributeListener}</li>\n * <li>{@link HttpSessionIdListener}</li>\n * <li>{@link HttpSessionListener}</li>\n * <li>{@link ServletContextListener}</li>\n * </ul>\n *\n * @param <T> the type of listener 监听器类型\n * @author Dave Syer\n * @author Phillip Webb\n * @since 1.4.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link ServletListenerRegistrationBean} instance.\n\t */",
            "\t/**\n\t * 创建新的 {@link ServletListenerRegistrationBean} 实例。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link ServletListenerRegistrationBean} instance.\n\t * @param listener the listener to register\n\t */",
            "\t/**\n\t * 创建新的 {@link ServletListenerRegistrationBean} 实例。\n\t *\n\t * @param listener the listener to register 待注册的监听器\n\t */",
        ),
        (
            "\t/**\n\t * Set the listener that will be registered.\n\t * @param listener the listener to register\n\t */",
            "\t/**\n\t * 设置待注册的监听器。\n\t *\n\t * @param listener the listener to register 待注册的监听器\n\t */",
        ),
        (
            "\t/**\n\t * Return the listener to be registered.\n\t * @return the listener to be registered\n\t */",
            "\t/**\n\t * 返回待注册的监听器。\n\t *\n\t * @return the listener to be registered 待注册的监听器\n\t */",
        ),
        (
            "\t/**\n\t * Returns {@code true} if the specified listener is one of the supported types.\n\t * @param listener the listener to test\n\t * @return if the listener is of a supported type\n\t */",
            "\t/**\n\t * 若指定监听器属于支持的类型则返回 {@code true}。\n\t *\n\t * @param listener the listener to test 待检测的监听器\n\t * @return if the listener is of a supported type 是否为支持的类型\n\t */",
        ),
        (
            "\t/**\n\t * Return the supported types for this registration.\n\t * @return the supported types\n\t */",
            "\t/**\n\t * 返回此注册支持的监听器类型集合。\n\t *\n\t * @return the supported types 支持的类型集合\n\t */",
        ),
    ],
    "ServletRegistration.java": [
        (
            "/**\n * Registers a {@link Servlet} in a Servlet 3.0+ container. Can be used as an\n * annotation-based alternative to {@link ServletRegistrationBean}.\n *\n * @author Moritz Halbritter\n * @author Dmytro Danilenkov\n * @since 3.5.0\n * @see ServletRegistrationBean\n */",
            "/**\n * 在 Servlet 3.0+ 容器中注册 {@link Servlet}。\n * 可作为 {@link ServletRegistrationBean} 的基于注解替代方案。\n *\n * @author Moritz Halbritter\n * @author Dmytro Danilenkov\n * @since 3.5.0\n * @see ServletRegistrationBean\n */",
        ),
        (
            "\t/**\n\t * Whether this registration is enabled.\n\t * @return whether this registration is enabled\n\t */",
            "\t/**\n\t * 此注册是否启用。\n\t *\n\t * @return whether this registration is enabled 是否启用此注册\n\t */",
        ),
        (
            "\t/**\n\t * Order of the registration bean.\n\t * @return the order of the registration bean\n\t */",
            "\t/**\n\t * 注册 Bean 的执行顺序。\n\t *\n\t * @return the order of the registration bean 注册 Bean 的顺序值\n\t */",
        ),
        (
            "\t/**\n\t * Name of this registration. If not specified the bean name will be used.\n\t * @return the name\n\t */",
            "\t/**\n\t * 此注册的名称。未指定时将使用 Bean 名称。\n\t *\n\t * @return the name 注册名称\n\t */",
        ),
        (
            "\t/**\n\t * Whether asynchronous operations are supported for this registration.\n\t * @return whether asynchronous operations are supported\n\t */",
            "\t/**\n\t * 此注册是否支持异步操作。\n\t *\n\t * @return whether asynchronous operations are supported 是否支持异步操作\n\t */",
        ),
        (
            "\t/**\n\t * Whether registration failures should be ignored. If set to true, a failure will be\n\t * logged. If set to false, an {@link IllegalStateException} will be thrown.\n\t * @return whether registration failures should be ignored\n\t */",
            "\t/**\n\t * 是否忽略注册失败。为 {@code true} 时仅记录日志；\n\t * 为 {@code false} 时抛出 {@link IllegalStateException}。\n\t *\n\t * @return whether registration failures should be ignored 是否忽略注册失败\n\t */",
        ),
        (
            "\t/**\n\t * URL mappings for the servlet. If not specified the mapping will default to '/'.\n\t * @return the url mappings\n\t */",
            "\t/**\n\t * Servlet 的 URL 映射。未指定时默认为 {@code /}。\n\t *\n\t * @return the url mappings URL 映射数组\n\t */",
        ),
        (
            "\t/**\n\t * The {@code loadOnStartup} priority. See\n\t * {@link jakarta.servlet.ServletRegistration.Dynamic#setLoadOnStartup} for details.\n\t * @return the {@code loadOnStartup} priority\n\t */",
            "\t/**\n\t * {@code loadOnStartup} 优先级。\n\t * 详见 {@link jakarta.servlet.ServletRegistration.Dynamic#setLoadOnStartup}。\n\t *\n\t * @return the {@code loadOnStartup} priority {@code loadOnStartup} 优先级\n\t */",
        ),
        (
            "\t/**\n\t * Init parameters to be used with the servlet.\n\t * @return the init parameters\n\t */",
            "\t/**\n\t * Servlet 使用的初始化参数。\n\t *\n\t * @return the init parameters 初始化参数\n\t */",
        ),
        (
            "\t/**\n\t * The multipart configuration.\n\t * @return the multipart configuration\n\t */",
            "\t/**\n\t * 多部分（multipart）上传配置。\n\t *\n\t * @return the multipart configuration 多部分配置\n\t */",
        ),
    ],
}


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:160]}...")
        text = text.replace(old, new, 1)
    return text


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


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
            cn = len(re.findall(r"[\u4e00-\u9fff]", text))
            lic = "Licensed under the Apache License" in text
            if cn < 10 or not lic or not has_chinese(text):
                failures.append(f"VALIDATION cn={cn} lic={lic}: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK cn={cn} {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
