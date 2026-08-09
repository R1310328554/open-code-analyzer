#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-18b block [15:30] (envoy-rls + dashboard auth)."""
from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
SCRIPTS = ROOT / "scripts"
QUEUE = VER / "_reports/class-queue"
BATCH_LIST = Path("/tmp/sentinel_w18b.txt").read_text(encoding="utf-8").strip().split("\n")
SCRIPT_NAME = "annotate_sentinel_wave18b_batch15_30.py"

GUARD_FILES = [
    VER
    / "analyzed/sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/connection/NettyConnection.java",
    VER
    / "analyzed/sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/codec/DefaultResponseEntityWriter.java",
    VER
    / "analyzed/sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/ClusterFlowChecker.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-cluster/sentinel-cluster-server-envoy-rls/src/main/java/com/alibaba/csp/sentinel/cluster/server/envoy/rls/log/RlsAccessLogger.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Envoy RLS 访问日志工具类。\n * <p>通过环境变量 {@link com.alibaba.csp.sentinel.cluster.server.envoy.rls.SentinelEnvoyRlsConstants#ENABLE_ACCESS_LOG_ENV_KEY}\n * 控制是否输出限流检查访问日志。</p>\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    public static boolean isEnabled() {",
        "    /** 返回访问日志是否已启用。 */\n    public static boolean isEnabled() {",
    ),
    (
        "    public static void log(String info) {",
        "    /**\n     * 在启用且 info 非空时输出访问日志。\n     *\n     * @param info 日志内容\n     */\n    public static void log(String info) {",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-envoy-rls/src/main/java/com/alibaba/csp/sentinel/cluster/server/envoy/rls/rule/EnvoyRlsRule.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.7.0\n */",
        "/**\n * Envoy RLS 限流规则模型，包含 domain 与资源描述符列表。\n *\n * @author Eric Zhao\n * @since 1.7.0\n */",
    ),
    (
        "    public static class ResourceDescriptor {",
        "    /** 资源描述符，绑定一组键值资源与限流阈值 count。 */\n    public static class ResourceDescriptor {",
    ),
    (
        "    public static class KeyValueResource {",
        "    /** 键值型限流维度，用于标识 descriptor 中的单个匹配条目。 */\n    public static class KeyValueResource {",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-envoy-rls/src/main/java/com/alibaba/csp/sentinel/cluster/server/envoy/rls/rule/EnvoyRlsRuleManager.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.7.0\n */",
        "/**\n * Envoy RLS 规则管理器，维护 domain 到 {@link EnvoyRlsRule} 的映射并同步为 Sentinel 集群流控规则。\n *\n * @author Eric Zhao\n * @since 1.7.0\n */",
    ),
    (
        "    /**\n     * Listen to the {@link SentinelProperty} for Envoy RLS rules. The property is the source of {@link EnvoyRlsRule}.\n     *\n     * @param property the property to listen\n     */",
        "    /**\n     * 注册 {@link SentinelProperty} 以监听 Envoy RLS 规则变更；该属性为 {@link EnvoyRlsRule} 的数据源。\n     *\n     * @param property 待监听的动态属性\n     */",
    ),
    (
        "    /**\n     * Load Envoy RLS rules, while former rules will be replaced.\n     *\n     * @param rules new rules to load\n     * @return true if there are actual changes, otherwise false\n     */",
        "    /**\n     * 加载 Envoy RLS 规则，原有规则将被整体替换。\n     *\n     * @param rules 待加载的新规则列表\n     * @return 若配置实际发生变更则返回 true，否则 false\n     */",
    ),
    (
        "    public static List<EnvoyRlsRule> getRules() {",
        "    /** 返回当前已加载的 Envoy RLS 规则副本列表。 */\n    public static List<EnvoyRlsRule> getRules() {",
    ),
    (
        "            // Use the \"default\" namespace.",
        "            // 使用 \"default\" 命名空间加载集群流控规则。",
    ),
    (
        "    /**\n     * Check whether the given Envoy RLS rule is valid.\n     *\n     * @param rule the rule to check\n     * @return true if the rule is valid, otherwise false\n     */",
        "    /**\n     * 校验给定 Envoy RLS 规则是否合法。\n     *\n     * @param rule 待校验规则\n     * @return 合法返回 true，否则 false\n     */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-envoy-rls/src/main/java/com/alibaba/csp/sentinel/cluster/server/envoy/rls/rule/EnvoySentinelRuleConverter.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.7.0\n */",
        "/**\n * Envoy RLS 规则与 Sentinel {@link com.alibaba.csp.sentinel.slots.block.flow.FlowRule} 之间的转换器。\n *\n * @author Eric Zhao\n * @since 1.7.0\n */",
    ),
    (
        "    /**\n     * Currently we use \"|\" to separate each key/value entries.\n     */",
        "    /** 键值条目分隔符，当前使用 \"|\"。 */\n",
    ),
    (
        "    /**\n     * Convert the {@link EnvoyRlsRule} to a list of Sentinel flow rules.\n     *\n     * @param rule a valid Envoy RLS rule\n     * @return converted rules\n     */",
        "    /**\n     * 将合法 {@link EnvoyRlsRule} 转换为 Sentinel 流控规则列表。\n     *\n     * @param rule 合法的 Envoy RLS 规则\n     * @return 转换后的流控规则列表\n     */",
    ),
    (
        "    public static FlowRule toSentinelFlowRule(String domain, EnvoyRlsRule.ResourceDescriptor descriptor) {",
        "    /**\n     * 将单个资源描述符转换为集群模式 {@link com.alibaba.csp.sentinel.slots.block.flow.FlowRule}。\n     *\n     * @param domain 限流 domain\n     * @param descriptor 资源描述符\n     * @return 对应的流控规则\n     */\n    public static FlowRule toSentinelFlowRule(String domain, EnvoyRlsRule.ResourceDescriptor descriptor) {",
    ),
    (
        "        // One descriptor could have only one rule.",
        "        // 每个 descriptor 仅对应一条流控规则。",
    ),
    (
        "    public static long generateFlowId(String key) {",
        "    /**\n     * 根据资源键生成集群流控规则 ID。\n     *\n     * @param key 资源标识键\n     * @return 流控规则 ID，key 为空时返回 -1\n     */\n    public static long generateFlowId(String key) {",
    ),
    (
        "        // Add offset to avoid negative ID.",
        "        // 加偏移量以避免生成负 ID。",
    ),
    (
        "    public static String generateKey(String domain, EnvoyRlsRule.ResourceDescriptor descriptor) {",
        "    /**\n     * 根据 domain 与 descriptor 中的键值资源生成 Sentinel 资源名。\n     *\n     * @param domain 限流 domain\n     * @param descriptor 资源描述符\n     * @return 拼接后的资源键\n     */\n    public static String generateKey(String domain, EnvoyRlsRule.ResourceDescriptor descriptor) {",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-envoy-rls/src/main/java/com/alibaba/csp/sentinel/cluster/server/envoy/rls/service/v3/SentinelEnvoyRlsServiceImpl.java"] = [
    (
        "/**\n * gRPC限流入口，实现envoy rls v3 api\n *\n * @author Winjay chan\n * @date 2021/8/4\n */",
        "/**\n * Sentinel Envoy RLS v3 gRPC 限流服务实现。\n * <p>接收 {@link io.envoyproxy.envoy.service.ratelimit.v3.RateLimitRequest}，\n * 按 descriptor 向集群令牌服务端申请配额并返回 {@link io.envoyproxy.envoy.service.ratelimit.v3.RateLimitResponse}。</p>\n *\n * @author Winjay chan\n * @date 2021/8/4\n */",
    ),
    (
        "            // Not present, use the default \"1\" by default.",
        "            // 未指定 hitsAddend 时默认按 1 次请求计数。",
    ),
    (
        "                // If the rule of the descriptor is absent, the request will pass directly.",
        "                // 若 descriptor 无对应规则，则直接放行。",
    ),
    (
        "            // Pass if the target rule is absent.",
        "            // 目标规则不存在时直接放行。",
    ),
    (
        "        // If the rule is present, it should be valid.",
        "        // 规则存在时应已通过合法性校验。",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/DashboardApplication.java"] = [
    (
        "/**\n * Sentinel dashboard application.\n *\n * @author Carpenter Lee\n */",
        "/**\n * Sentinel 控制台 Spring Boot 启动入口。\n * <p>启动前在独立线程中触发 {@link com.alibaba.csp.sentinel.init.InitExecutor} 完成 Sentinel 初始化。</p>\n *\n * @author Carpenter Lee\n */",
    ),
    (
        "    public static void main(String[] args) {",
        "    /** 应用主入口：先异步初始化 Sentinel，再启动 Spring Boot。 */\n    public static void main(String[] args) {",
    ),
    (
        "    private static void triggerSentinelInit() {",
        "    /** 在后台线程执行 Sentinel 初始化，避免阻塞 Spring 启动。 */\n    private static void triggerSentinelInit() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/auth/AuthAction.java"] = [
    (
        "/**\n * @author lkxiaolou\n * @since 1.7.1\n */",
        "/**\n * 标注控制器方法所需权限的注解，由 {@link AuthorizationInterceptor} 在请求前校验。\n *\n * @author lkxiaolou\n * @since 1.7.1\n */",
    ),
    (
        "    /**\n     * @return the privilege type\n     */",
        "    /**\n     * @return 所需权限类型\n     */",
    ),
    (
        "    /**\n     * @return the target name to control\n     */",
        "    /**\n     * @return 请求参数名，用于提取鉴权目标（如应用名）\n     */",
    ),
    (
        "    /**\n     * @return the message when permission is denied\n     */",
        "    /**\n     * @return 权限不足时返回给客户端的提示信息\n     */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/auth/AuthService.java"] = [
    (
        "/**\n * Interface for authentication and authorization.\n *\n * @author Carpenter Lee\n * @since 1.5.0\n */",
        "/**\n * 控制台认证与授权服务接口。\n *\n * @author Carpenter Lee\n * @since 1.5.0\n */",
    ),
    (
        "    /**\n     * Get the authentication user.\n     *\n     * @param request the request contains the user information\n     * @return the auth user represent the current user, when the user is illegal, a null value will return.\n     */",
        "    /**\n     * 从请求中解析当前认证用户。\n     *\n     * @param request 携带用户信息的请求对象\n     * @return 当前用户；若未认证或非法则返回 null\n     */",
    ),
    (
        "    /**\n     * Privilege type.\n     */",
        "    /** 权限类型枚举。 */\n",
    ),
    (
        "        /**\n         * Read rule\n         */",
        "        /** 读取规则。 */\n",
    ),
    (
        "        /**\n         * Create or modify rule\n         */",
        "        /** 创建或修改规则。 */\n",
    ),
    (
        "        /**\n         * Delete rule\n         */",
        "        /** 删除规则。 */\n",
    ),
    (
        "        /**\n         * Read metrics\n         */",
        "        /** 读取监控指标。 */\n",
    ),
    (
        "        /**\n         * Add machine\n         */",
        "        /** 添加机器。 */\n",
    ),
    (
        "        /**\n         * All privileges above are granted.\n         */",
        "        /** 授予上述全部权限。 */\n",
    ),
    (
        "    /**\n     * Represents the current user.\n     */",
        "    /** 当前认证用户抽象。 */\n",
    ),
    (
        "        /**\n         * Query whether current user has the specific privilege to the target, the target\n         * may be an app name or an ip address, or other destination.\n         * <p>\n         * This method will use return value to represent  whether user has the specific\n         * privileges to the target, but to throw a RuntimeException to represent no auth\n         * is also a good way.\n         * </p>\n         *\n         * @param target        the target to check\n         * @param privilegeType the privilege type to check\n         * @return if current user has the specific privileges to the target, return true,\n         * otherwise return false.\n         */",
        "        /**\n         * 判断当前用户对指定目标是否拥有给定权限；目标可为应用名、IP 等。\n         * <p>\n         * 通常通过返回值表示是否有权限；抛出 {@link RuntimeException} 表示未授权也是可接受的做法。\n         * </p>\n         *\n         * @param target        待校验目标\n         * @param privilegeType 权限类型\n         * @return 有权限返回 true，否则 false\n         */",
    ),
    (
        "        /**\n         * Check whether current user is a super-user.\n         *\n         * @return if current user is super user return true, else return false.\n         */",
        "        /**\n         * 判断当前用户是否为超级用户。\n         *\n         * @return 超级用户返回 true，否则 false\n         */",
    ),
    (
        "        /**\n         * Get current user's nick name.\n         *\n         * @return current user's nick name.\n         */",
        "        /**\n         * 返回当前用户昵称。\n         *\n         * @return 用户昵称\n         */",
    ),
    (
        "        /**\n         * Get current user's login name.\n         *\n         * @return current user's login name.\n         */",
        "        /**\n         * 返回当前用户登录名。\n         *\n         * @return 登录名\n         */",
    ),
    (
        "        /**\n         * Get current user's ID.\n         *\n         * @return ID of current user\n         */",
        "        /**\n         * 返回当前用户 ID。\n         *\n         * @return 用户 ID\n         */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/auth/AuthorizationInterceptor.java"] = [
    (
        "/**\n * The web interceptor for privilege-based authorization.\n *\n * @author lkxiaolou\n * @author wxq\n * @since 1.7.1\n */",
        "/**\n * 基于权限的 Web 授权拦截器接口。\n *\n * @author lkxiaolou\n * @author wxq\n * @since 1.7.1\n */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/auth/DefaultAuthorizationInterceptor.java"] = [
    (
        "/**\n * The web interceptor for privilege-based authorization.\n * <p>\n * move from old {@link AuthorizationInterceptor}.\n *\n * @author lkxiaolou\n * @author wxq\n * @since 1.7.1\n */",
        "/**\n * 默认授权拦截器实现，在控制器方法执行前校验 {@link AuthAction} 标注的权限。\n * <p>\n * 自旧版 {@link AuthorizationInterceptor} 迁移而来。\n * </p>\n *\n * @author lkxiaolou\n * @author wxq\n * @since 1.7.1\n */",
    ),
    (
        "    public DefaultAuthorizationInterceptor(AuthService<HttpServletRequest> authService) {",
        "    /** 构造拦截器并注入 {@link AuthService}。 */\n    public DefaultAuthorizationInterceptor(AuthService<HttpServletRequest> authService) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/auth/DefaultLoginAuthenticationFilter.java"] = [
    (
        "/**\n * <p>The Servlet filter for authentication.</p>\n *\n * <p>Note: some urls are excluded as they needn't auth, such as:</p>\n * <ul>\n * <li>index url: {@code /}</li>\n * <li>authentication request url: {@code /login}, {@code /logout}</li>\n * <li>machine registry: {@code /registry/machine}</li>\n * <li>static resources</li>\n * </ul>\n * <p>\n * The excluded urls and urlSuffixes could be configured in {@code application.properties} file.\n *\n * @author cdfive\n * @since 1.6.0\n */",
        "/**\n * <p>默认登录认证 Servlet 过滤器。</p>\n *\n * <p>以下 URL 通常无需认证，例如：</p>\n * <ul>\n * <li>首页：{@code /}</li>\n * <li>登录/登出：{@code /login}、{@code /logout}</li>\n * <li>机器注册：{@code /registry/machine}</li>\n * <li>静态资源</li>\n * </ul>\n * <p>\n * 排除 URL 与后缀可在 {@code application.properties} 中配置。\n * </p>\n *\n * @author cdfive\n * @since 1.6.0\n */",
    ),
    (
        "    /**\n     * Some urls which needn't auth, such as /auth/login, /registry/machine and so on.\n     */",
        "    /** 无需认证的 URL 列表（如 /auth/login、/registry/machine 等）。 */\n",
    ),
    (
        "    /**\n     * Some urls with suffixes which needn't auth, such as htm, html, js and so on.\n     */",
        "    /** 无需认证的 URL 后缀列表（如 htm、html、js 等）。 */\n",
    ),
    (
        "    /**\n     * Authentication using AuthService interface.\n     */",
        "    /** 基于 {@link AuthService} 的认证实现。 */\n",
    ),
    (
        "        // Exclude the urls which needn't auth",
        "        // 跳过无需认证的 URL",
    ),
    (
        "        // Exclude the urls with suffixes which needn't auth",
        "        // 跳过匹配排除后缀的 URL",
    ),
    (
        "            // Add . for url suffix so that we needn't add . in property file",
        "            // 自动补点前缀，配置文件中后缀可不带 \".\"",
    ),
    (
        "            // If auth fail, set response status code to 401",
        "            // 认证失败时返回 401",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/auth/FakeAuthServiceImpl.java"] = [
    (
        "/**\n * A fake AuthService implementation, which will pass all user auth checking.\n *\n * @author Carpenter Lee\n * @since 1.5.0\n */",
        "/**\n * 假认证服务实现，所有权限校验均直接通过，用于关闭鉴权的开发/测试场景。\n *\n * @author Carpenter Lee\n * @since 1.5.0\n */",
    ),
    (
        "        public boolean authTarget(String target, PrivilegeType privilegeType) {\n            // fake implementation, always return true",
        "        public boolean authTarget(String target, PrivilegeType privilegeType) {\n            // 假实现，恒返回 true",
    ),
    (
        "        public boolean isSuperUser() {\n            // fake implementation, always return true",
        "        public boolean isSuperUser() {\n            // 假实现，恒返回 true",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/auth/LoginAuthenticationFilter.java"] = [
    (
        "/**\n * <p>The Servlet filter for authentication.</p>\n *\n * <p>Note: some urls are excluded as they needn't auth, such as:</p>\n * <ul>\n * <li>index url: {@code /}</li>\n * <li>authentication request url: {@code /login}, {@code /logout}</li>\n * <li>machine registry: {@code /registry/machine}</li>\n * <li>static resources</li>\n * </ul>\n * <p>\n * The excluded urls and urlSuffixes could be configured in {@code application.properties} file.\n *\n * @author cdfive\n * @author wxq\n * @since 1.6.0\n */",
        "/**\n * <p>登录认证 Servlet 过滤器接口。</p>\n *\n * <p>部分 URL 无需认证，例如：</p>\n * <ul>\n * <li>首页：{@code /}</li>\n * <li>登录/登出：{@code /login}、{@code /logout}</li>\n * <li>机器注册：{@code /registry/machine}</li>\n * <li>静态资源</li>\n * </ul>\n * <p>\n * 排除 URL 与后缀可在 {@code application.properties} 中配置。\n * </p>\n *\n * @author cdfive\n * @author wxq\n * @since 1.6.0\n */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/auth/SimpleWebAuthServiceImpl.java"] = [
    (
        "/**\n * @author cdfive\n * @since 1.6.0\n */",
        "/**\n * 基于 HttpSession 的简单 Web 认证服务，从会话中读取 {@link AuthUser}。\n *\n * @author cdfive\n * @since 1.6.0\n */",
    ),
    (
        "    public static final String WEB_SESSION_KEY = \"session_sentinel_admin\";",
        "    /** HttpSession 中存储 Sentinel 管理员的键名。 */\n    public static final String WEB_SESSION_KEY = \"session_sentinel_admin\";",
    ),
    (
        "    public static final class SimpleWebAuthUserImpl implements AuthUser {",
        "    /** 简单 Web 认证用户实现，默认拥有全部权限。 */\n    public static final class SimpleWebAuthUserImpl implements AuthUser {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/client/CommandFailedException.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 控制台向 Sentinel 客户端发送命令失败时抛出的运行时异常。\n * <p>重写 {@link #fillInStackTrace()} 以抑制堆栈填充，降低高频失败时的开销。</p>\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    public CommandFailedException() {}",
        "    /** 构造无消息的命令失败异常。 */\n    public CommandFailedException() {}",
    ),
    (
        "    public CommandFailedException(String message) {",
        "    /**\n     * 构造带消息的命令失败异常。\n     *\n     * @param message 失败原因描述\n     */\n    public CommandFailedException(String message) {",
    ),
]


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


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
    index_file = Path("/tmp/git-index-sentinel-w18b")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", base], env=env, check=True)
    subprocess.run(["git", "-C", str(ROOT), "add", "--", *paths], env=env, check=True)
    tree_count = tree_guard(env)
    tree = subprocess.check_output(["git", "-C", str(ROOT), "write-tree"], env=env, text=True).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True)
    index_file.unlink(missing_ok=True)
    return commit, tree_count


def apply_replacements(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise FileNotFoundError(f"Missing original: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    orig = text
    for old, new in R.get(rel, []):
        if old not in text:
            raise ValueError(f"MISSING pattern in {rel}: {old[:80]!r}...")
        text = text.replace(old, new, 1)
    if not has_chinese(text):
        raise ValueError(f"No Chinese in {rel} after annotation")
    if "Licensed under the Apache License" in orig and "Licensed under the Apache License" not in text:
        raise ValueError(f"License missing in {rel}")
    dst.write_text(text, encoding="utf-8")


def update_batch_json() -> None:
    batch_path = QUEUE / "batch.json"
    if not batch_path.exists():
        return
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    if pending_path.exists():
        batch["remaining_pending"] = len(
            [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
        )
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def confirm_chinese() -> dict[str, bool]:
    return {rel: has_chinese((ANALYZED / rel).read_text(encoding="utf-8")) for rel in BATCH_LIST}


def main() -> int:
    failures: list[str] = []
    for rel in BATCH_LIST:
        try:
            apply_replacements(rel)
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if failures:
        print(json.dumps({"failures": failures}, ensure_ascii=False, indent=2))
        return 1

    analyzed_paths = [f"sentinel/1.8.10/analyzed/{rel}" for rel in BATCH_LIST]
    script_path = f"scripts/{SCRIPT_NAME}"
    sha, tree_count = isolated_index_commit(
        "sentinel 1.8.10: Chinese-annotate wave 18b [15:30]",
        [*analyzed_paths, script_path],
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "-u", "origin", "main"], check=True)

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "sentinel",
            "--version",
            "1.8.10",
            "--note",
            "wave18b",
            *BATCH_LIST,
        ],
        check=True,
    )
    update_batch_json()
    queue_paths = [
        "sentinel/1.8.10/_reports/class-queue/done.txt",
        "sentinel/1.8.10/_reports/class-queue/pending.txt",
        "sentinel/1.8.10/_reports/class-queue/batch.json",
        "sentinel/1.8.10/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark sentinel 1.8.10 wave18b done",
        queue_paths,
        base_ref="HEAD",
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "origin", "main"], check=True)

    done_total = len([ln for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()])
    pending_total = len([ln for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines() if ln.strip()])
    chinese_confirmed = confirm_chinese()
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "done": done_total,
                "pending": pending_total,
                "chinese_confirmed": chinese_confirmed,
                "all_15_chinese": all(chinese_confirmed.values()),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
