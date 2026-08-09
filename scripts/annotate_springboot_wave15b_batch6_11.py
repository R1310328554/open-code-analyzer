#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-15b slice [6:11] (servlet registration, WAR support)."""
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
BATCH_FILES = Path("/tmp/springboot_w15b.txt").read_text(encoding="utf-8").strip().splitlines()

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ServletRegistrationBean.java": [
        (
            "/**\n * A {@link ServletContextInitializer} to register {@link Servlet}s in a Servlet 3.0+\n * container. Similar to the {@link ServletContext#addServlet(String, Servlet)\n * registration} features provided by {@link ServletContext} but with a Spring Bean\n * friendly design.\n * <p>\n * The {@link #setServlet(Servlet) servlet} must be specified before calling\n * {@link #onStartup}. URL mapping can be configured used {@link #setUrlMappings} or\n * omitted when mapping to '/*' (unless\n * {@link #ServletRegistrationBean(Servlet, boolean, String...) alwaysMapUrl} is set to\n * {@code false}). The servlet name will be deduced if not specified.\n *\n * @param <T> the type of the {@link Servlet} to register\n * @author Phillip Webb\n * @since 1.4.0\n * @see ServletContextInitializer\n * @see ServletContext#addServlet(String, Servlet)\n * @see org.springframework.boot.web.servlet.ServletRegistration\n */",
            "/**\n * 在 Servlet 3.0+ 容器中注册 {@link Servlet} 的 {@link ServletContextInitializer}。\n * 功能类似 {@link ServletContext} 提供的 {@link ServletContext#addServlet(String, Servlet)\n * 注册} 能力，但采用对 Spring Bean 更友好的设计。\n * <p>\n * 调用 {@link #onStartup} 前必须先通过 {@link #setServlet(Servlet) setServlet} 指定 Servlet。\n * 可通过 {@link #setUrlMappings} 配置 URL 映射；若省略映射则默认映射到 {@code /*}\n * （除非 {@link #ServletRegistrationBean(Servlet, boolean, String...) alwaysMapUrl}\n * 设为 {@code false}）。未指定名称时会自动推断 Servlet 名称。\n *\n * @param <T> the type of the {@link Servlet} to register 待注册 {@link Servlet} 的类型\n * @author Phillip Webb\n * @since 1.4.0\n * @see ServletContextInitializer\n * @see ServletContext#addServlet(String, Servlet)\n * @see org.springframework.boot.web.servlet.ServletRegistration\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link ServletRegistrationBean} instance.\n\t */",
            "\t/**\n\t * 创建新的 {@link ServletRegistrationBean} 实例。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link ServletRegistrationBean} instance with the specified\n\t * {@link Servlet} and URL mappings.\n\t * @param servlet the servlet being mapped\n\t * @param urlMappings the URLs being mapped\n\t */",
            "\t/**\n\t * 使用指定 {@link Servlet} 与 URL 映射创建新的 {@link ServletRegistrationBean} 实例。\n\t *\n\t * @param servlet the servlet being mapped 待映射的 Servlet\n\t * @param urlMappings the URLs being mapped 待映射的 URL\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link ServletRegistrationBean} instance with the specified\n\t * {@link Servlet} and URL mappings.\n\t * @param servlet the servlet being mapped\n\t * @param alwaysMapUrl if omitted URL mappings should be replaced with '/*'\n\t * @param urlMappings the URLs being mapped\n\t */",
            "\t/**\n\t * 使用指定 {@link Servlet} 与 URL 映射创建新的 {@link ServletRegistrationBean} 实例。\n\t *\n\t * @param servlet the servlet being mapped 待映射的 Servlet\n\t * @param alwaysMapUrl if omitted URL mappings should be replaced with '/*' 省略 URL 映射时是否替换为 {@code /*}\n\t * @param urlMappings the URLs being mapped 待映射的 URL\n\t */",
        ),
        (
            "\t/**\n\t * Sets the servlet to be registered.\n\t * @param servlet the servlet\n\t */",
            "\t/**\n\t * 设置待注册的 Servlet。\n\t *\n\t * @param servlet the servlet 待注册的 Servlet\n\t */",
        ),
        (
            "\t/**\n\t * Return the servlet being registered.\n\t * @return the servlet\n\t */",
            "\t/**\n\t * 返回待注册的 Servlet。\n\t *\n\t * @return the servlet 待注册的 Servlet\n\t */",
        ),
        (
            "\t/**\n\t * Set the URL mappings for the servlet. If not specified the mapping will default to\n\t * '/'. This will replace any previously specified mappings.\n\t * @param urlMappings the mappings to set\n\t * @see #addUrlMappings(String...)\n\t */",
            "\t/**\n\t * 设置 Servlet 的 URL 映射。未指定时默认映射到 {@code /}。\n\t * 调用此方法会替换此前已指定的所有映射。\n\t *\n\t * @param urlMappings the mappings to set 待设置的映射\n\t * @see #addUrlMappings(String...)\n\t */",
        ),
        (
            "\t/**\n\t * Return a mutable collection of the URL mappings, as defined in the Servlet\n\t * specification, for the servlet.\n\t * @return the urlMappings\n\t */",
            "\t/**\n\t * 返回 Servlet 规范定义的、用于此 Servlet 的可变 URL 映射集合。\n\t *\n\t * @return the urlMappings URL 映射\n\t */",
        ),
        (
            "\t/**\n\t * Add URL mappings, as defined in the Servlet specification, for the servlet.\n\t * @param urlMappings the mappings to add\n\t * @see #setUrlMappings(Collection)\n\t */",
            "\t/**\n\t * 按 Servlet 规范为 Servlet 追加 URL 映射。\n\t *\n\t * @param urlMappings the mappings to add 待追加的映射\n\t * @see #setUrlMappings(Collection)\n\t */",
        ),
        (
            "\t/**\n\t * Sets the {@code loadOnStartup} priority. See\n\t * {@link ServletRegistration.Dynamic#setLoadOnStartup} for details.\n\t * @param loadOnStartup if load on startup is enabled\n\t */",
            "\t/**\n\t * 设置 {@code loadOnStartup} 优先级。详见\n\t * {@link ServletRegistration.Dynamic#setLoadOnStartup}。\n\t *\n\t * @param loadOnStartup if load on startup is enabled 是否启用启动时加载\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link MultipartConfigElement multi-part configuration}.\n\t * @param multipartConfig the multipart configuration to set or {@code null}\n\t */",
            "\t/**\n\t * 设置 {@link MultipartConfigElement 多部分（multipart）配置}。\n\t *\n\t * @param multipartConfig the multipart configuration to set or {@code null} 待设置的多部分配置，或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Returns the {@link MultipartConfigElement multi-part configuration} to be applied\n\t * or {@code null}.\n\t * @return the multipart config\n\t */",
            "\t/**\n\t * 返回将要应用的 {@link MultipartConfigElement 多部分配置}，或 {@code null}。\n\t *\n\t * @return the multipart config 多部分配置\n\t */",
        ),
        (
            "\t/**\n\t * Configure registration settings. Subclasses can override this method to perform\n\t * additional configuration if required.\n\t * @param registration the registration\n\t */",
            "\t/**\n\t * 配置注册设置。子类可按需覆盖此方法以执行额外配置。\n\t *\n\t * @param registration the registration 注册对象\n\t */",
        ),
        (
            "\t/**\n\t * Returns the servlet name that will be registered.\n\t * @return the servlet name\n\t */",
            "\t/**\n\t * 返回将要注册的 Servlet 名称。\n\t *\n\t * @return the servlet name Servlet 名称\n\t */",
        ),
    ],
    "ErrorPageFilter.java": [
        (
            "/**\n * A Servlet {@link Filter} that provides an {@link ErrorPageRegistry} for non-embedded\n * applications (i.e. deployed WAR files). It registers error pages and handles\n * application errors by filtering requests and forwarding to the error pages instead of\n * letting the server handle them. Error pages are a feature of the servlet spec but there\n * is no Java API for registering them in the spec. This filter works around that by\n * accepting error page registrations from Spring Boot's {@link ErrorPageRegistrar} (any\n * beans of that type in the context will be applied to this server).\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @since 2.0.0\n */",
            "/**\n * 为非嵌入式应用（即部署的 WAR 包）提供 {@link ErrorPageRegistry} 的 Servlet {@link Filter}。\n * 它注册错误页，并通过过滤请求并转发到错误页来处理应用错误，而非交由服务器处理。\n * 错误页是 Servlet 规范的功能，但规范中并无注册错误页的 Java API。\n * 本过滤器通过接受 Spring Boot {@link ErrorPageRegistrar} 的错误页注册来绕过此限制\n * （上下文中所有该类型的 Bean 都会应用到此服务器）。\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @since 2.0.0\n */",
        ),
        (
            "\t// From RequestDispatcher but not referenced to remain compatible with Servlet 2.5",
            "\t// 来自 RequestDispatcher，但不直接引用以保持与 Servlet 2.5 的兼容性",
        ),
        (
            "\t/**\n\t * The name of the servlet attribute containing request URI.\n\t */",
            "\t/**\n\t * 包含请求 URI 的 Servlet 属性名。\n\t */",
        ),
        (
            "\t/**\n\t * Return the description for the given request. By default this method will return a\n\t * description based on the request {@code servletPath} and {@code pathInfo}.\n\t * @param request the source request\n\t * @return the description\n\t */",
            "\t/**\n\t * 返回给定请求的描述。默认基于请求的 {@code servletPath} 与 {@code pathInfo} 生成描述。\n\t *\n\t * @param request the source request 源请求\n\t * @return the description 描述信息\n\t */",
        ),
        (
            "\t\t\t// User might see the error page without all the data here but throwing the\n\t\t\t// exception isn't going to help anyone (we'll log it to be on the safe side)",
            "\t\t\t// 用户可能看到缺少部分数据的错误页，但抛出异常也无济于事（为稳妥起见仍记录日志）",
        ),
        (
            "\t\t\t// Ignore",
            "\t\t\t// 忽略",
        ),
        (
            "\t\t\t// Do not call super because the container may prevent us from handling the\n\t\t\t// error ourselves",
            "\t\t\t// 不调用 super，因为容器可能阻止我们自行处理错误",
        ),
        (
            "\t\t\t// If there was no error we need to trust the wrapped response",
            "\t\t\t// 若无错误则需信任被包装的响应",
        ),
    ],
    "ErrorPageFilterConfiguration.java": [
        (
            "/**\n * Configuration for {@link ErrorPageFilter}.\n *\n * @author Andy Wilkinson\n * @author Jay Choi\n */",
            "/**\n * {@link ErrorPageFilter} 的 Spring 配置类。\n * 注册 {@link ErrorPageFilter} Bean 及其 {@link FilterRegistrationBean}，\n * 并启用 {@link ErrorPageRegistrarBeanPostProcessor} 以应用错误页注册。\n *\n * @author Andy Wilkinson\n * @author Jay Choi\n */",
        ),
    ],
    "ServletContextApplicationContextInitializer.java": [
        (
            "/**\n * {@link ApplicationContextInitializer} for setting the servlet context.\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @since 2.0.0\n */",
            "/**\n * 用于设置 Servlet 上下文的 {@link ApplicationContextInitializer}。\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @since 2.0.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link ServletContextApplicationContextInitializer} instance.\n\t * @param servletContext the servlet that should be ultimately set.\n\t */",
            "\t/**\n\t * 创建新的 {@link ServletContextApplicationContextInitializer} 实例。\n\t *\n\t * @param servletContext the servlet that should be ultimately set 最终应设置的 Servlet 上下文\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link ServletContextApplicationContextInitializer} instance.\n\t * @param servletContext the servlet that should be ultimately set.\n\t * @param addApplicationContextAttribute if the {@link ApplicationContext} should be\n\t * stored as an attribute in the {@link ServletContext}\n\t * @since 1.3.4\n\t */",
            "\t/**\n\t * 创建新的 {@link ServletContextApplicationContextInitializer} 实例。\n\t *\n\t * @param servletContext the servlet that should be ultimately set 最终应设置的 Servlet 上下文\n\t * @param addApplicationContextAttribute if the {@link ApplicationContext} should be\n\t * stored as an attribute in the {@link ServletContext} 是否将 {@link ApplicationContext} 存为 {@link ServletContext} 属性\n\t * @since 1.3.4\n\t */",
        ),
    ],
    "SpringBootServletInitializer.java": [
        (
            "/**\n * An opinionated {@link WebApplicationInitializer} to run a {@link SpringApplication}\n * from a traditional WAR deployment. Binds {@link Servlet}, {@link Filter} and\n * {@link ServletContextInitializer} beans from the application context to the server.\n * <p>\n * To configure the application either override the\n * {@link #configure(SpringApplicationBuilder)} method (calling\n * {@link SpringApplicationBuilder#sources(Class...)}) or make the initializer itself a\n * {@code @Configuration}. If you are using {@link SpringBootServletInitializer} in\n * combination with other {@link WebApplicationInitializer WebApplicationInitializers} you\n * might also want to add an {@code @Ordered} annotation to configure a specific startup\n * order.\n * <p>\n * Note that a WebApplicationInitializer is only needed if you are building a war file and\n * deploying it. If you prefer to run an embedded web server then you won't need this at\n * all.\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @author Brian Clozel\n * @since 2.0.0\n * @see #configure(SpringApplicationBuilder)\n */",
            "/**\n * 用于在传统 WAR 部署中运行 {@link SpringApplication} 的约定式 {@link WebApplicationInitializer}。\n * 将应用上下文中的 {@link Servlet}、{@link Filter} 与\n * {@link ServletContextInitializer} Bean 绑定到服务器。\n * <p>\n * 配置应用时可覆盖 {@link #configure(SpringApplicationBuilder)} 方法\n * （调用 {@link SpringApplicationBuilder#sources(Class...)}），\n * 或将初始化器本身声明为 {@code @Configuration}。\n * 若将 {@link SpringBootServletInitializer} 与其他\n * {@link WebApplicationInitializer WebApplicationInitializers} 组合使用，\n * 还可添加 {@code @Ordered} 注解以指定启动顺序。\n * <p>\n * 注意：仅当构建 WAR 并部署时才需要 WebApplicationInitializer；\n * 若使用嵌入式 Web 服务器则完全不需要此类。\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @author Brian Clozel\n * @since 2.0.0\n * @see #configure(SpringApplicationBuilder)\n */",
        ),
        (
            "\tprotected @Nullable Log logger; // Don't initialize early",
            "\tprotected @Nullable Log logger; // 不要过早初始化",
        ),
        (
            "\t/**\n\t * Set if the {@link ErrorPageFilter} should be registered. Set to {@code false} if\n\t * error page mappings should be handled through the server and not Spring Boot.\n\t * @param registerErrorPageFilter if the {@link ErrorPageFilter} should be registered.\n\t */",
            "\t/**\n\t * 设置是否注册 {@link ErrorPageFilter}。\n\t * 若错误页映射应由服务器而非 Spring Boot 处理，则设为 {@code false}。\n\t *\n\t * @param registerErrorPageFilter if the {@link ErrorPageFilter} should be registered 是否注册 {@link ErrorPageFilter}\n\t */",
        ),
        (
            "\t\t// Logger initialization is deferred in case an ordered\n\t\t// LogServletContextInitializer is being used",
            "\t\t// 延迟初始化 Logger，以防使用了带顺序的 LogServletContextInitializer",
        ),
        (
            "\t/**\n\t * Deregisters the JDBC drivers that were registered by the application represented by\n\t * the given {@code servletContext}. The default implementation\n\t * {@link DriverManager#deregisterDriver(Driver) deregisters} every {@link Driver}\n\t * that was loaded by the {@link ServletContext#getClassLoader web application's class\n\t * loader}.\n\t * @param servletContext the web application's servlet context\n\t * @since 2.3.0\n\t */",
            "\t/**\n\t * 注销由给定 {@code servletContext} 所代表应用注册的 JDBC 驱动。\n\t * 默认实现会 {@link DriverManager#deregisterDriver(Driver) 注销}\n\t * 由 {@link ServletContext#getClassLoader Web 应用类加载器} 加载的每个 {@link Driver}。\n\t *\n\t * @param servletContext the web application's servlet context Web 应用的 Servlet 上下文\n\t * @since 2.3.0\n\t */",
        ),
        (
            "\t\t\t\t\t// Continue",
            "\t\t\t\t\t// 继续",
        ),
        (
            "\t/**\n\t * Shuts down the reactor {@link Schedulers} that were initialized by\n\t * {@code Schedulers.boundedElastic()} (or similar). The default implementation\n\t * {@link Schedulers#shutdownNow()} schedulers if they were initialized on this web\n\t * application's class loader.\n\t * @param servletContext the web application's servlet context\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 关闭由 {@code Schedulers.boundedElastic()}（或类似方法）初始化的 Reactor {@link Schedulers}。\n\t * 默认实现会在调度器由此 Web 应用类加载器初始化时调用 {@link Schedulers#shutdownNow()}。\n\t *\n\t * @param servletContext the web application's servlet context Web 应用的 Servlet 上下文\n\t * @since 3.4.0\n\t */",
        ),
        (
            "\t\t// Ensure error pages are registered",
            "\t\t// 确保注册错误页",
        ),
        (
            "\t/**\n\t * Returns the {@code SpringApplicationBuilder} that is used to configure and create\n\t * the {@link SpringApplication}. The default implementation returns a new\n\t * {@code SpringApplicationBuilder} in its default state.\n\t * @return the {@code SpringApplicationBuilder}.\n\t */",
            "\t/**\n\t * 返回用于配置并创建 {@link SpringApplication} 的 {@code SpringApplicationBuilder}。\n\t * 默认实现返回处于默认状态的新 {@code SpringApplicationBuilder}。\n\t *\n\t * @return the {@code SpringApplicationBuilder} SpringApplicationBuilder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Called to run a fully configured {@link SpringApplication}.\n\t * @param application the application to run\n\t * @return the {@link WebApplicationContext}\n\t */",
            "\t/**\n\t * 运行已完全配置的 {@link SpringApplication}。\n\t *\n\t * @param application the application to run 待运行的应用\n\t * @return the {@link WebApplicationContext} Web 应用上下文\n\t */",
        ),
        (
            "\t/**\n\t * Configure the application. Normally all you would need to do is to add sources\n\t * (e.g. config classes) because other settings have sensible defaults. You might\n\t * choose (for instance) to add default command line arguments, or set an active\n\t * Spring profile.\n\t * @param builder a builder for the application context\n\t * @return the application builder\n\t * @see SpringApplicationBuilder\n\t */",
            "\t/**\n\t * 配置应用。通常只需添加 sources（例如配置类），其他设置已有合理默认值。\n\t * 也可选择添加默认命令行参数或设置激活的 Spring profile 等。\n\t *\n\t * @param builder a builder for the application context 应用上下文构建器\n\t * @return the application builder 应用构建器\n\t * @see SpringApplicationBuilder\n\t */",
        ),
        (
            "\t/**\n\t * {@link ApplicationListener} to trigger\n\t * {@link ConfigurableWebEnvironment#initPropertySources(ServletContext, jakarta.servlet.ServletConfig)}.\n\t */",
            "\t/**\n\t * 触发 {@link ConfigurableWebEnvironment#initPropertySources(ServletContext, jakarta.servlet.ServletConfig)} 的\n\t * {@link ApplicationListener}。\n\t */",
        ),
        (
            "\t/**\n\t * {@link ContextLoaderListener} for the initialized context.\n\t */",
            "\t/**\n\t * 用于已初始化上下文的 {@link ContextLoaderListener}。\n\t */",
        ),
        (
            "\t\t\t// no-op because the application context is already initialized",
            "\t\t\t// 无操作，因为应用上下文已初始化",
        ),
        (
            "\t\t\t\t// Use original context so that the classloader can be accessed",
            "\t\t\t\t// 使用原始上下文以便访问类加载器",
        ),
        (
            "\t\t\t\t// Shut down shared reactor schedulers tied to this classloader",
            "\t\t\t\t// 关闭与此类加载器绑定的共享 Reactor 调度器",
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
