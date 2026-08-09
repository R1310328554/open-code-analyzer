#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-28a block [0:15] (zuul2 demo, aspectj/cdi, apollo)."""
from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
SCRIPTS = ROOT / "scripts"
QUEUE = VER / "_reports/class-queue"
BATCH_LIST = [
    ln.strip()
    for ln in Path("/tmp/sentinel_w28a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_sentinel_wave28a_batch0_15.py"
MARK_NOTE = "wave28a [0:15]"

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-demo/sentinel-demo-zuul2-gateway/src/main/java/com/alibaba/csp/sentinel/demo/zuul2/gateway/ZuulBootstrap.java"] = [
    (
        "/**\n * <p>The Zuul 2.x demo with Sentinel gateway flow control.</p>\n * <p>Run with {@code -Dcsp.sentinel.api.type=1} to mark the demo as API gateway.</p>\n *\n * @author wavesZh\n */",
        "/**\n * <p>集成 Sentinel 网关流控的 Zuul 2.x 演示入口。</p>\n * <p>启动时添加 {@code -Dcsp.sentinel.api.type=1} 将应用标记为 API 网关类型。</p>\n *\n * @author wavesZh\n */",
    ),
    (
        "            // Load sample rules. You may also manage rules in Sentinel dashboard.",
        "            // 加载示例规则；亦可在 Sentinel Dashboard 中动态管理",
    ),
    (
        "            //DataCenterInfo",
        "            // Eureka 实例配置（DataCenterInfo）",
    ),
]

R["sentinel-demo/sentinel-demo-zuul2-gateway/src/main/java/com/alibaba/csp/sentinel/demo/zuul2/gateway/ZuulClasspathFiltersModule.java"] = [
    (
        "public class ZuulClasspathFiltersModule extends AbstractModule {",
        "/** Guice 模块：注册 Sentinel Zuul 2 过滤器与演示路由。 */\npublic class ZuulClasspathFiltersModule extends AbstractModule {",
    ),
    (
        "        filterMultibinder.addBinding().toInstance(new SentinelZuulInboundFilter(500));",
        "        // 入站 Sentinel 过滤器（order=500）\n        filterMultibinder.addBinding().toInstance(new SentinelZuulInboundFilter(500));",
    ),
    (
        "        filterMultibinder.addBinding().toInstance(new SentinelZuulOutboundFilter(500));",
        "        // 出站 Sentinel 过滤器（order=500）\n        filterMultibinder.addBinding().toInstance(new SentinelZuulOutboundFilter(500));",
    ),
    (
        "        filterMultibinder.addBinding().toInstance(new SentinelZuulEndpoint());",
        "        // Sentinel 阻塞异常 Endpoint\n        filterMultibinder.addBinding().toInstance(new SentinelZuulEndpoint());",
    ),
    (
        "        filterMultibinder.addBinding().toInstance(new Route());",
        "        // 演示用路径路由过滤器\n        filterMultibinder.addBinding().toInstance(new Route());",
    ),
]

R["sentinel-demo/sentinel-demo-zuul2-gateway/src/main/java/com/alibaba/csp/sentinel/demo/zuul2/gateway/ZuulSampleModule.java"] = [
    (
        "/**\n * Zuul Sample Module\n *\n * Author: Arthur Gonigberg\n * Date: November 20, 2017\n */",
        "/**\n * Zuul 2 示例 Guice 模块：绑定 Netty 服务器、过滤器链与监控组件。\n *\n * Author: Arthur Gonigberg\n * Date: November 20, 2017\n */",
    ),
    (
        "        // sample specific bindings",
        "        // 示例专用绑定",
    ),
    (
        "        // use provided basic netty origin manager",
        "        // 使用 BasicNettyOriginManager 作为 Origin 管理器",
    ),
    (
        "        // zuul filter loading",
        "        // Zuul 核心过滤器加载",
    ),
    (
        "        // general server bindings",
        "        // 通用服务器组件绑定",
    ),
    (
        "        // health/discovery status",
        "        // 健康检查与服务发现状态",
    ),
    (
        "        // decorate new sessions when requests come in",
        "        // 请求到达时装饰 SessionContext",
    ),
    (
        "        // atlas metrics registry",
        "        // Spectator/Atlas 指标注册表",
    ),
    (
        "        // metrics post-request completion",
        "        // 请求完成后的指标回调",
    ),
    (
        "        // discovery client",
        "        // Eureka 发现客户端可选参数",
    ),
    (
        "        // timings publisher",
        "        // 请求耗时指标发布器",
    ),
    (
        "        // access logger, including request ID generator",
        "        // 访问日志（含请求 UUID）",
    ),
]

R["sentinel-demo/sentinel-demo-zuul2-gateway/src/main/java/com/alibaba/csp/sentinel/demo/zuul2/gateway/filters/NotFoundEndpoint.java"] = [
    (
        "public class NotFoundEndpoint extends HttpSyncEndpoint {",
        "/** 404 同步 Endpoint：路径未匹配时返回 HTTP NOT FOUND。 */\npublic class NotFoundEndpoint extends HttpSyncEndpoint {",
    ),
    (
        "    @Override\n    public HttpResponseMessage apply(HttpRequestMessage request) {",
        "    /** 构造 404 响应并缓冲响应体。 */\n    @Override\n    public HttpResponseMessage apply(HttpRequestMessage request) {",
    ),
]

R["sentinel-demo/sentinel-demo-zuul2-gateway/src/main/java/com/alibaba/csp/sentinel/demo/zuul2/gateway/filters/Route.java"] = [
    (
        "public class Route extends HttpInboundSyncFilter {",
        "/** 入站路由过滤器：按请求路径设置 Endpoint 与 routeVIP。 */\npublic class Route extends HttpInboundSyncFilter {",
    ),
    (
        "\t\t\tcase \"/images\":",
        "\t\t\t// /images -> 代理到 images VIP\n\t\t\tcase \"/images\":",
    ),
    (
        "\t\t\tcase \"/comments\":",
        "\t\t\t// /comments -> 代理到 comments VIP\n\t\t\tcase \"/comments\":",
    ),
    (
        "\t\t\tdefault:",
        "\t\t\t// 未知路径 -> 404 Endpoint\n\t\t\tdefault:",
    ),
]

R["sentinel-extension/sentinel-annotation-aspectj/src/main/java/com/alibaba/csp/sentinel/annotation/aspectj/AbstractSentinelAspectSupport.java"] = [
    (
        "/**\n * Some common functions for Sentinel annotation aspect.\n *\n * @author Eric Zhao\n * @author zhaoyuguang\n * @author dowenliu-xyz(hawkdowen@hotmail.com)\n */",
        "/**\n * {@link SentinelResourceAspect} 的公共支撑逻辑：资源名解析、fallback/blockHandler 反射调用与异常追踪。\n *\n * @author Eric Zhao\n * @author zhaoyuguang\n * @author dowenliu-xyz(hawkdowen@hotmail.com)\n */",
    ),
    (
        "        // The ignore list will be checked first.",
        "        // 优先检查 exceptionsToIgnore 列表",
    ),
    (
        "    /**\n     * Check whether the exception is in provided list of exception classes.\n     *\n     * @param ex         provided throwable\n     * @param exceptions list of exceptions\n     * @return true if it is in the list, otherwise false\n     */",
        "    /**\n     * 判断异常是否属于给定异常类型列表。\n     *\n     * @param ex         provided throwable\n     * @param exceptions list of exceptions\n     * @return true if it is in the list, otherwise false\n     */",
    ),
    (
        "        // If resource name is present in annotation, use this value.",
        "        // 注解 value 非空时直接使用",
    ),
    (
        "        // Parse name of target method.",
        "        // 否则按方法签名生成资源名",
    ),
    (
        "        // Execute fallback function if configured.",
        "        // 若配置了 fallback 则优先执行",
    ),
    (
        "            // Construct args.",
        "            // 构造 fallback 参数（可追加 Throwable）",
    ),
    (
        "        // If fallback is absent, we'll try the defaultFallback if provided.",
        "        // fallback 缺失时尝试 defaultFallback",
    ),
    (
        "        // Execute the default fallback function if configured.",
        "        // 执行 defaultFallback（无参或仅 Throwable 参数）",
    ),
    (
        "        // If no any fallback is present, then directly throw the exception.",
        "        // 无任何 fallback 时原样抛出异常",
    ),
    (
        "        // Execute block handler if configured.",
        "        // 若配置了 blockHandler 则调用",
    ),
    (
        "        // If no block handler is present, then go to fallback.",
        "        // 无 blockHandler 时降级到 fallback 链",
    ),
    (
        "            // throw the actual exception",
        "            // 解包 InvocationTargetException 抛出真实异常",
    ),
    (
        "     * Make the given method accessible, explicitly setting it accessible if\n     * necessary. The {@code setAccessible(true)} method is only called\n     * when actually necessary, to avoid unnecessary conflicts with a JVM\n     * SecurityManager (if active).\n     * @param method the method to make accessible\n     * @see java.lang.reflect.Method#setAccessible",
        "     * 在必要时将方法设为可访问，仅在确实需要时调用 {@code setAccessible(true)}，\n     * 以避免与 JVM SecurityManager 冲突。\n     * @param method the method to make accessible\n     * @see java.lang.reflect.Method#setAccessible",
    ),
    (
        "            // First time, resolve the fallback.",
        "            // 首次解析 fallback 并写入缓存",
    ),
    (
        "            // Cache the method instance.",
        "            // 缓存解析结果",
    ),
    (
        "            // First time, resolve the default fallback.",
        "            // 首次解析 defaultFallback",
    ),
    (
        "            // Default fallback allows two kinds of parameter list.\n            // One is empty parameter list.",
        "            // defaultFallback 支持两种签名：无参",
    ),
    (
        "            // The other is a single parameter {@link Throwable} to get relevant exception info.",
        "            // 或单参数 {@link Throwable}",
    ),
    (
        "            // We first find the default fallback with empty parameter list.",
        "            // 先查找无参 defaultFallback",
    ),
    (
        "            // If default fallback with empty params is absent, we then try to find the other one.",
        "            // 未找到时再查找带 Throwable 的版本",
    ),
    (
        "        // Fallback function allows two kinds of parameter list.",
        "        // fallback 支持与原方法相同签名，或末尾追加 Throwable",
    ),
    (
        "        // We first find the fallback matching the signature of origin method.",
        "        // 优先匹配与原方法相同参数列表",
    ),
    (
        "        // If fallback matching the origin method is absent, we then try to find the other one.",
        "        // 未找到时再匹配带 Throwable 的版本",
    ),
    (
        "            // By default current class.",
        "            // 默认在当前目标类中查找",
    ),
    (
        "            // First time, resolve the block handler.",
        "            // 首次解析 blockHandler",
    ),
    (
        "        // Current class not found, find in the super classes recursively.",
        "        // 当前类未找到则递归查找父类",
    ),
    (
        "     * Get declared method with provided name and parameterTypes in given class and its super classes.\n     * All parameters should be valid.\n     *\n     * @param clazz          class where the method is located\n     * @param name           method name\n     * @param parameterTypes method parameter type list\n     * @return resolved method, null if not found",
        "     * 在类及其父类中按名称与参数类型查找 declared 方法。\n     *\n     * @param clazz          class where the method is located\n     * @param name           method name\n     * @param parameterTypes method parameter type list\n     * @return resolved method, null if not found",
    ),
]

R["sentinel-extension/sentinel-annotation-aspectj/src/main/java/com/alibaba/csp/sentinel/annotation/aspectj/MethodWrapper.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 反射 {@link Method} 的可选包装：区分「已解析」与「不存在」两种缓存状态。\n *\n * @author Eric Zhao\n */",
    ),
]

R["sentinel-extension/sentinel-annotation-aspectj/src/main/java/com/alibaba/csp/sentinel/annotation/aspectj/ResourceMetadataRegistry.java"] = [
    (
        "/**\n * Registry for resource configuration metadata (e.g. fallback method)\n *\n * @author Eric Zhao\n * @author dowenliu-xyz(hawkdowen@hotmail.com)\n */",
        "/**\n * 注解资源元数据注册表：缓存 fallback、defaultFallback 与 blockHandler 的 {@link MethodWrapper}。\n *\n * @author Eric Zhao\n * @author dowenliu-xyz(hawkdowen@hotmail.com)\n */",
    ),
    (
        "     * Only for internal test.",
        "     * 仅供内部测试清理缓存。",
    ),
]

R["sentinel-extension/sentinel-annotation-aspectj/src/main/java/com/alibaba/csp/sentinel/annotation/aspectj/SentinelResourceAspect.java"] = [
    (
        "/**\n * Aspect for methods with {@link SentinelResource} annotation.\n *\n * @author Eric Zhao\n */",
        "/**\n * 拦截带 {@link SentinelResource} 注解的方法，自动创建 Sentinel 资源入口并处理阻塞/异常。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "            // Should not go through here.",
        "            // 不应进入此分支",
    ),
    (
        "            // The ignore list will be checked first.",
        "            // 优先检查 exceptionsToIgnore",
    ),
    (
        "            // No fallback function can handle the exception, so throw it out.",
        "            // 无可用 fallback，原样抛出",
    ),
]

R["sentinel-extension/sentinel-annotation-cdi-interceptor/src/main/java/com/alibaba/csp/sentinel/annotation/cdi/interceptor/AbstractSentinelInterceptorSupport.java"] = [
    (
        "/**\n * Some common functions for Sentinel annotation CDI extension.\n *\n * @author Eric Zhao\n * @author seasidesky\n * @author dowenliu-xyz(hawkdowen@hotmail.com)\n */",
        "/**\n * CDI {@link SentinelResourceInterceptor} 的公共支撑：资源名、fallback/blockHandler 解析与异常追踪。\n *\n * @author Eric Zhao\n * @author seasidesky\n * @author dowenliu-xyz(hawkdowen@hotmail.com)\n */",
    ),
    (
        "        // The ignore list will be checked first.",
        "        // 优先检查 exceptionsToIgnore 列表",
    ),
    (
        "    /**\n     * Check whether the exception is in provided list of exception classes.\n     *\n     * @param ex         provided throwable\n     * @param exceptions list of exceptions\n     * @return true if it is in the list, otherwise false\n     */",
        "    /**\n     * 判断异常是否属于给定异常类型列表。\n     *\n     * @param ex         provided throwable\n     * @param exceptions list of exceptions\n     * @return true if it is in the list, otherwise false\n     */",
    ),
    (
        "        // If resource name is present in annotation, use this value.",
        "        // 注解 value 非空时直接使用",
    ),
    (
        "        // Parse name of target method.",
        "        // 否则按方法签名生成资源名",
    ),
    (
        "        // Execute fallback function if configured.",
        "        // 若配置了 fallback 则优先执行",
    ),
    (
        "            // Construct args.",
        "            // 构造 fallback 参数",
    ),
    (
        "                // throw the actual exception",
        "                // 解包 InvocationTargetException",
    ),
    (
        "        // If fallback is absent, we'll try the defaultFallback if provided.",
        "        // fallback 缺失时尝试 defaultFallback",
    ),
    (
        "        // Execute the default fallback function if configured.",
        "        // 执行 defaultFallback",
    ),
    (
        "        // If no any fallback is present, then directly throw the exception.",
        "        // 无任何 fallback 时原样抛出",
    ),
    (
        "        // Execute block handler if configured.",
        "        // 若配置了 blockHandler 则调用",
    ),
    (
        "        // If no block handler is present, then go to fallback.",
        "        // 无 blockHandler 时降级到 fallback",
    ),
    (
        "            // First time, resolve the fallback.",
        "            // 首次解析 fallback",
    ),
    (
        "            // Cache the method instance.",
        "            // 缓存解析结果",
    ),
    (
        "            // Default fallback allows two kinds of parameter list.\n            // One is empty parameter list.",
        "            // defaultFallback 支持无参",
    ),
    (
        "            // The other is a single parameter {@link Throwable} to get relevant exception info.",
        "            // 或单参数 {@link Throwable}",
    ),
    (
        "            // We first find the default fallback with empty parameter list.",
        "            // 先查找无参版本",
    ),
    (
        "            // If default fallback with empty params is absent, we then try to find the other one.",
        "            // 未找到时再查找带 Throwable 的版本",
    ),
    (
        "        // Fallback function allows two kinds of parameter list.",
        "        // fallback 支持与原方法相同签名或末尾追加 Throwable",
    ),
    (
        "        // We first find the fallback matching the signature of origin method.",
        "        // 优先匹配原方法签名",
    ),
    (
        "        // If fallback matching the origin method is absent, we then try to find the other one.",
        "        // 未找到时再匹配带 Throwable 的版本",
    ),
    (
        "            // By default current class.",
        "            // 默认在当前目标类中查找",
    ),
    (
        "        // Current class not found, find in the super classes recursively.",
        "        // 当前类未找到则递归查找父类",
    ),
    (
        "     * Get declared method with provided name and parameterTypes in given class and its super classes.\n     * All parameters should be valid.\n     *\n     * @param clazz          class where the method is located\n     * @param name           method name\n     * @param parameterTypes method parameter type list\n     * @return resolved method, null if not found",
        "     * 在类及其父类中按名称与参数类型查找 declared 方法。\n     *\n     * @param clazz          class where the method is located\n     * @param name           method name\n     * @param parameterTypes method parameter type list\n     * @return resolved method, null if not found",
    ),
]

R["sentinel-extension/sentinel-annotation-cdi-interceptor/src/main/java/com/alibaba/csp/sentinel/annotation/cdi/interceptor/MethodWrapper.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 反射 {@link Method} 的可选包装，用于缓存 fallback/blockHandler 解析结果。\n *\n * @author Eric Zhao\n */",
    ),
]

R["sentinel-extension/sentinel-annotation-cdi-interceptor/src/main/java/com/alibaba/csp/sentinel/annotation/cdi/interceptor/ResourceMetadataRegistry.java"] = [
    (
        "/**\n * Registry for resource configuration metadata (e.g. fallback method)\n *\n * @author Eric Zhao\n * @author dowenliu-xyz(hawkdowen@hotmail.com)\n */",
        "/**\n * CDI 注解资源元数据注册表：缓存 fallback、defaultFallback 与 blockHandler。\n *\n * @author Eric Zhao\n * @author dowenliu-xyz(hawkdowen@hotmail.com)\n */",
    ),
    (
        "     * Only for internal test.",
        "     * 仅供内部测试清理缓存。",
    ),
]

R["sentinel-extension/sentinel-annotation-cdi-interceptor/src/main/java/com/alibaba/csp/sentinel/annotation/cdi/interceptor/SentinelResourceBinding.java"] = [
    (
        "/**\n * The annotation indicates a definition of Sentinel resource.\n *\n * @author Eric Zhao\n * @author seasidesky\n * @since 1.8.0\n */",
        "/**\n * CDI 拦截器绑定注解：声明 Sentinel 资源名、流控方向及 fallback/blockHandler 配置。\n *\n * @author Eric Zhao\n * @author seasidesky\n * @since 1.8.0\n */",
    ),
    (
        "     * @return name of the Sentinel resource",
        "     * @return Sentinel 资源名称",
    ),
    (
        "     * @return the entry type (inbound or outbound), outbound by default",
        "     * @return 入口类型（IN/OUT），默认 OUT",
    ),
    (
        "     * @return the classification (type) of the resource",
        "     * @return 资源分类类型编号",
    ),
    (
        "     * @return name of the block exception function, empty by default",
        "     * @return blockHandler 方法名，默认空",
    ),
    (
        "     * The {@code blockHandler} is located in the same class with the original method by default.\n     * However, if some methods share the same signature and intend to set the same block handler,\n     * then users can set the class where the block handler exists. Note that the block handler method\n     * must be static.\n     *\n     * @return the class where the block handler exists, should not provide more than one classes",
        "     * 默认 {@code blockHandler} 与原方法同处一类；多方法共享时可指定外部类，且 handler 必须为 static。\n     *\n     * @return blockHandler 所在类（最多一个）",
    ),
    (
        "     * @return name of the fallback function, empty by default",
        "     * @return fallback 方法名，默认空",
    ),
    (
        "     * The {@code defaultFallback} is used as the default universal fallback method.\n     * It should not accept any parameters, and the return type should be compatible\n     * with the original method.\n     *\n     * @return name of the default fallback method, empty by default",
        "     * {@code defaultFallback} 为通用降级方法，应无参且返回类型与原方法兼容。\n     *\n     * @return defaultFallback 方法名，默认空",
    ),
    (
        "     * The {@code fallback} is located in the same class with the original method by default.\n     * However, if some methods share the same signature and intend to set the same fallback,\n     * then users can set the class where the fallback function exists. Note that the shared fallback method\n     * must be static.\n     *\n     * @return the class where the fallback method is located (only single class)",
        "     * 默认 {@code fallback} 与原方法同处一类；共享时可指定外部类，且 fallback 必须为 static。\n     *\n     * @return fallback 所在类（仅一个）",
    ),
    (
        "     * @return the list of exception classes to trace, {@link Throwable} by default",
        "     * @return 需追踪的异常类型列表，默认 {@link Throwable}",
    ),
    (
        "     * Indicates the exceptions to be ignored. Note that {@code exceptionsToTrace} should\n     * not appear with {@code exceptionsToIgnore} at the same time, or {@code exceptionsToIgnore}\n     * will be of higher precedence.\n     *\n     * @return the list of exception classes to ignore, empty by default",
        "     * 指定忽略的异常类型；与 {@code exceptionsToTrace} 同时出现时以 {@code exceptionsToIgnore} 为准。\n     *\n     * @return 忽略的异常类型列表，默认空",
    ),
]

R["sentinel-extension/sentinel-annotation-cdi-interceptor/src/main/java/com/alibaba/csp/sentinel/annotation/cdi/interceptor/SentinelResourceInterceptor.java"] = [
    (
        "/**\n * @author sea\n * @since 1.8.0\n */",
        "/**\n * CDI 拦截器：对 {@link SentinelResourceBinding} 标注的方法执行 Sentinel 入口/出口与降级逻辑。\n *\n * @author sea\n * @since 1.8.0\n */",
    ),
    (
        "            // Should not go through here.",
        "            // 不应进入此分支",
    ),
    (
        "            // The ignore list will be checked first.",
        "            // 优先检查 exceptionsToIgnore",
    ),
    (
        "            // No fallback function can handle the exception, so throw it out.",
        "            // 无可用 fallback，原样抛出",
    ),
]

R["sentinel-extension/sentinel-datasource-apollo/src/main/java/com/alibaba/csp/sentinel/datasource/apollo/ApolloDataSource.java"] = [
    (
        "/**\n * A read-only {@code DataSource} with <a href=\"http://github.com/ctripcorp/apollo\">Apollo</a> as its configuration\n * source.\n * <br />\n * When the rule is changed in Apollo, it will take effect in real time.\n *\n * @author Jason Song\n * @author Haojun Ren\n */",
        "/**\n * 只读 {@code DataSource}，以 <a href=\"http://github.com/ctripcorp/apollo\">Apollo</a> 为配置源。\n * <br />\n * Apollo 中规则变更后通过监听器实时刷新 Sentinel 规则。\n *\n * @author Jason Song\n * @author Haojun Ren\n */",
    ),
    (
        "     * Constructs the Apollo data source\n     *\n     * @param namespaceName        the namespace name in Apollo, should not be null or empty\n     * @param ruleKey              the rule key in the namespace, should not be null or empty\n     * @param defaultRuleValue     the default rule value when the ruleKey is not found or any error\n     *                             occurred\n     * @param parser               the parser to transform string configuration to actual flow rules",
        "     * 构造 Apollo 数据源。\n     *\n     * @param namespaceName        the namespace name in Apollo, should not be null or empty\n     * @param ruleKey              the rule key in the namespace, should not be null or empty\n     * @param defaultRuleValue     the default rule value when the ruleKey is not found or any error\n     *                             occurred\n     * @param parser               the parser to transform string configuration to actual flow rules",
    ),
    (
        "                //change is never null because the listener will only notify for this key",
        "                // 监听器仅订阅 ruleKey，change 通常非 null",
    ),
]


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def apply_replacements(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise SystemExit(f"Missing original: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    src_text = src.read_text(encoding="utf-8")
    for old, new in R.get(rel, []):
        if old not in text:
            raise SystemExit(f"MISSING pattern in {rel}: {old[:80]!r}...")
        text = text.replace(old, new, 1)
    if not has_chinese(text):
        raise SystemExit(f"No Chinese in {rel} after annotation")
    if "Licensed under the Apache License" in src_text and "Licensed under the Apache License" not in text:
        raise SystemExit(f"License missing in {rel}")
    dst.write_text(text, encoding="utf-8")


def tree_guard(env: dict[str, str] | None = None) -> int:
    tracked = len(subprocess.check_output(["git", "-C", str(ROOT), "ls-files"], env=env).splitlines())
    if tracked < 50000:
        raise RuntimeError(f"tree guard failed: tracked={tracked} (expected >=50000)")
    for path in GUARD_FILES:
        if env is None:
            if not path.exists():
                raise RuntimeError(f"guard file missing: {path}")
            blob = path.read_text(encoding="utf-8")
        else:
            rel = path.relative_to(ROOT)
            blob = subprocess.check_output(
                ["git", "-C", str(ROOT), "show", f":{rel}"], env=env, text=True
            )
        if not has_chinese(blob):
            raise RuntimeError(f"guard file lacks Chinese: {path}")
    return tracked


def isolated_index_commit(message: str, paths: list[str], base_ref: str = "origin/main") -> tuple[str, int]:
    index_file = Path("/tmp/git-index-sentinel-w28a")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", base], env=env, check=True)
    normal = [p for p in paths if not p.endswith("worker.log")]
    forced = [p for p in paths if p.endswith("worker.log")]
    if normal:
        subprocess.run(["git", "-C", str(ROOT), "add", "--", *normal], env=env, check=True)
    if forced:
        subprocess.run(["git", "-C", str(ROOT), "add", "-f", "--", *forced], env=env, check=True)
    tree_count = tree_guard(env)
    tree = subprocess.check_output(["git", "-C", str(ROOT), "write-tree"], env=env, text=True).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True)
    index_file.unlink(missing_ok=True)
    return commit, tree_count


def push_main(retries: int = 4) -> None:
    for attempt in range(retries):
        r = subprocess.run(
            ["git", "-C", str(ROOT), "push", "-u", "origin", "main"],
            capture_output=True,
            text=True,
        )
        if r.returncode == 0:
            return
        if attempt + 1 < retries:
            subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
            subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "origin/main"], check=True)
            time.sleep(4 * (2**attempt))
    raise subprocess.CalledProcessError(r.returncode, r.args, r.stdout, r.stderr)


def confirm_chinese() -> dict[str, bool]:
    return {rel: has_chinese((ANALYZED / rel).read_text(encoding="utf-8")) for rel in BATCH_LIST}


def verify_origin_main() -> dict[str, bool]:
    result: dict[str, bool] = {}
    for rel in BATCH_LIST:
        path = f"sentinel/1.8.10/analyzed/{rel}"
        blob = subprocess.check_output(
            ["git", "-C", str(ROOT), "show", f"origin/main:{path}"],
            text=True,
        )
        result[rel] = has_chinese(blob)
    return result


def main() -> int:
    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    failures: list[str] = []
    for rel in BATCH_LIST:
        try:
            apply_replacements(rel)
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if failures:
        return 1

    analyzed_paths = [f"sentinel/1.8.10/analyzed/{rel}" for rel in BATCH_LIST]
    script_path = f"scripts/{SCRIPT_NAME}"
    sha, tree_count = isolated_index_commit(
        "sentinel 1.8.10: Chinese-annotate wave 28a [0:15]",
        [*analyzed_paths, script_path],
    )
    push_main()

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "sentinel",
            "--version",
            "1.8.10",
            "--note",
            MARK_NOTE,
            *BATCH_LIST,
        ],
        check=True,
    )
    queue_paths = [
        "sentinel/1.8.10/_reports/class-queue/done.txt",
        "sentinel/1.8.10/_reports/class-queue/pending.txt",
        "sentinel/1.8.10/_reports/class-queue/batch.json",
        "sentinel/1.8.10/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        f"queue: mark sentinel 1.8.10 {MARK_NOTE} done",
        queue_paths,
        base_ref="HEAD",
    )
    push_main()

    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    origin_chinese = verify_origin_main()
    done_total = len(
        [ln for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    pending_total = len(
        [ln for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    chinese = confirm_chinese()
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "done": done_total,
                "pending": pending_total,
                "chinese_confirmed": chinese,
                "origin_main_chinese": origin_chinese,
                "all_chinese": all(origin_chinese.values()),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0 if all(origin_chinese.values()) else 1


if __name__ == "__main__":
    raise SystemExit(main())
