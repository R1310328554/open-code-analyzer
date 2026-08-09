#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-19a block [0:15] (dashboard client/config/controllers)."""
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
SCRIPT_NAME = "annotate_sentinel_wave19a_batch0_15.py"
BATCH_LIST = Path("/tmp/sentinel_w19a.txt").read_text(encoding="utf-8").strip().split("\n")
MARK_NOTE = "wave19a"

GUARD_FILES = [
    VER
    / "analyzed/sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/connection/ScanIdleConnectionTask.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/client/CommandNotFoundException.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 0.2.1\n */",
        "/**\n * 客户端命令未找到异常。\n * <p>当 Sentinel 客户端不支持或未注册某 API 命令时抛出；\n * {@link #fillInStackTrace()} 被重写为空操作以降低热路径开销。\n *\n * @author Eric Zhao\n * @since 0.2.1\n */",
    ),
    (
        "    public CommandNotFoundException() { }",
        "    /** 无参构造。 */\n    public CommandNotFoundException() { }",
    ),
    (
        "    public CommandNotFoundException(String message) {",
        "    /** @param message 异常描述 */\n    public CommandNotFoundException(String message) {",
    ),
    (
        "    @Override\n    public synchronized Throwable fillInStackTrace() {",
        "    /** 不填充堆栈，避免频繁创建异常时的性能损耗。 */\n    @Override\n    public synchronized Throwable fillInStackTrace() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/config/AuthConfiguration.java"] = [
    (
        "@Configuration\n@EnableConfigurationProperties(AuthProperties.class)\npublic class AuthConfiguration {",
        "/**\n * Dashboard 认证相关 Spring 配置，按 {@link AuthProperties#isEnabled()} 注册认证 Bean。\n */\n@Configuration\n@EnableConfigurationProperties(AuthProperties.class)\npublic class AuthConfiguration {",
    ),
    (
        "    public AuthConfiguration(AuthProperties authProperties) {",
        "    /** @param authProperties 认证开关与属性配置 */\n    public AuthConfiguration(AuthProperties authProperties) {",
    ),
    (
        "    @Bean\n    @ConditionalOnMissingBean\n    public AuthService<HttpServletRequest> httpServletRequestAuthService() {",
        "    /** 注册 HTTP 请求认证服务；启用认证时用 {@link SimpleWebAuthServiceImpl}，否则用 {@link FakeAuthServiceImpl}。 */\n    @Bean\n    @ConditionalOnMissingBean\n    public AuthService<HttpServletRequest> httpServletRequestAuthService() {",
    ),
    (
        "        if (this.authProperties.isEnabled()) {",
        "        // 开启认证时使用真实 Web 认证实现。\n        if (this.authProperties.isEnabled()) {",
    ),
    (
        "    @Bean\n    @ConditionalOnMissingBean\n    public LoginAuthenticationFilter loginAuthenticationFilter(AuthService<HttpServletRequest> httpServletRequestAuthService) {",
        "    /** 注册登录认证 Servlet 过滤器。 */\n    @Bean\n    @ConditionalOnMissingBean\n    public LoginAuthenticationFilter loginAuthenticationFilter(AuthService<HttpServletRequest> httpServletRequestAuthService) {",
    ),
    (
        "    @Bean\n    @ConditionalOnMissingBean\n    public AuthorizationInterceptor authorizationInterceptor(AuthService<HttpServletRequest> httpServletRequestAuthService) {",
        "    /** 注册 MVC 授权拦截器，校验接口访问权限。 */\n    @Bean\n    @ConditionalOnMissingBean\n    public AuthorizationInterceptor authorizationInterceptor(AuthService<HttpServletRequest> httpServletRequestAuthService) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/config/AuthProperties.java"] = [
    (
        "@ConfigurationProperties(prefix = \"auth\")\npublic class AuthProperties {",
        "/**\n * Dashboard 认证配置属性，绑定 {@code auth.*} 前缀。\n */\n@ConfigurationProperties(prefix = \"auth\")\npublic class AuthProperties {",
    ),
    (
        "    private boolean enabled = true;",
        "    /** 是否启用 Dashboard 登录认证，默认 true。 */\n    private boolean enabled = true;",
    ),
    (
        "    public boolean isEnabled() {",
        "    /** @return 认证是否启用 */\n    public boolean isEnabled() {",
    ),
    (
        "    public void setEnabled(boolean enabled) {",
        "    /** @param enabled 是否启用认证 */\n    public void setEnabled(boolean enabled) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/config/DashboardConfig.java"] = [
    (
        "/**\n * <p>Dashboard local config support.</p>\n * <p>\n * Dashboard supports configuration loading by several ways by order:<br>\n * 1. System.properties<br>\n * 2. Env\n * </p>\n *\n * @author jason\n * @since 1.5.0\n */",
        "/**\n * Dashboard 本地配置读取工具。\n * <p>按优先级从环境变量、系统属性加载配置并缓存：</p>\n * <ol>\n *   <li>环境变量</li>\n *   <li>System.getProperty</li>\n * </ol>\n *\n * @author jason\n * @since 1.5.0\n */",
    ),
    (
        "    public static final int DEFAULT_MACHINE_HEALTHY_TIMEOUT_MS = 60_000;",
        "    /** 机器健康判定默认超时（毫秒）。 */\n    public static final int DEFAULT_MACHINE_HEALTHY_TIMEOUT_MS = 60_000;",
    ),
    (
        "    /**\n     * Login username\n     */",
        "    /** 登录用户名配置键。 */",
    ),
    (
        "    /**\n     * Login password\n     */",
        "    /** 登录密码配置键。 */",
    ),
    (
        "    /**\n     * Hide application name in sidebar when it has no healthy machines after specific period in millisecond.\n     */",
        "    /** 无健康机器超过指定毫秒后在侧栏隐藏应用名的配置键。 */",
    ),
    (
        "    /**\n     * Remove application when it has no healthy machines after specific period in millisecond.\n     */",
        "    /** 无健康机器超过指定毫秒后移除应用的配置键。 */",
    ),
    (
        "    /**\n     * Timeout\n     */",
        "    /** 机器不健康判定超时配置键。 */",
    ),
    (
        "    /**\n     * Auto remove unhealthy machine after specific period in millisecond.\n     */",
        "    /** 不健康机器自动移除超时配置键。 */",
    ),
    (
        "        // env",
        "        // 优先读取环境变量。",
    ),
    (
        "        // properties",
        "        // 其次读取 JVM 系统属性。",
    ),
    (
        "    public static String getAuthUsername() {",
        "    /** @return 配置的登录用户名，未配置时返回 null */\n    public static String getAuthUsername() {",
    ),
    (
        "    public static String getAuthPassword() {",
        "    /** @return 配置的登录密码，未配置时返回 null */\n    public static String getAuthPassword() {",
    ),
    (
        "    public static int getHideAppNoMachineMillis() {",
        "    /** @return 侧栏隐藏无机器应用的超时毫秒数 */\n    public static int getHideAppNoMachineMillis() {",
    ),
    (
        "    public static int getRemoveAppNoMachineMillis() {",
        "    /** @return 自动移除无机器应用的超时毫秒数 */\n    public static int getRemoveAppNoMachineMillis() {",
    ),
    (
        "    public static int getAutoRemoveMachineMillis() {",
        "    /** @return 自动移除不健康机器的超时毫秒数 */\n    public static int getAutoRemoveMachineMillis() {",
    ),
    (
        "    public static int getUnhealthyMachineMillis() {",
        "    /** @return 机器不健康判定超时毫秒数 */\n    public static int getUnhealthyMachineMillis() {",
    ),
    (
        "    public static void clearCache() {",
        "    /** 清空配置缓存，便于测试或热更新后重新加载。 */\n    public static void clearCache() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/config/WebConfig.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * Dashboard Web MVC 与 Servlet 过滤器配置。\n * <p>注册认证过滤器、Sentinel {@link CommonFilter} 及静态资源与首页路由。\n *\n * @author leyou\n */",
    ),
    (
        "    @Override\n    public void addInterceptors(InterceptorRegistry registry) {",
        "    /** 全局注册授权拦截器。 */\n    @Override\n    public void addInterceptors(InterceptorRegistry registry) {",
    ),
    (
        "    @Override\n    public void addResourceHandlers(ResourceHandlerRegistry registry) {",
        "    /** 映射 classpath 静态资源。 */\n    @Override\n    public void addResourceHandlers(ResourceHandlerRegistry registry) {",
    ),
    (
        "    @Override\n    public void addViewControllers(ViewControllerRegistry registry) {",
        "    /** 将根路径转发到 index.htm。 */\n    @Override\n    public void addViewControllers(ViewControllerRegistry registry) {",
    ),
    (
        "    /**\n     * Add {@link CommonFilter} to the server, this is the simplest way to use Sentinel\n     * for Web application.\n     */",
        "    /**\n     * 注册 Sentinel {@link CommonFilter}，为 Web 应用提供统一入口流控。\n     */",
    ),
    (
        "        // If this is enabled, the entrance of all Web URL resources will be unified as a single context name.\n        // In most scenarios that's enough, and it could reduce the memory footprint.",
        "        // 统一 Web 入口上下文名，多数场景下可减小内存占用。",
    ),
    (
        "        // Example: register a UrlCleaner to exclude URLs of common static resources.",
        "        // 注册 UrlCleaner，排除常见静态资源 URL 以免产生无意义资源统计。",
    ),
    (
        "    @Bean\n    public FilterRegistrationBean authenticationFilterRegistration() {",
        "    /** 注册登录认证过滤器，优先级高于 Sentinel 过滤器。 */\n    @Bean\n    public FilterRegistrationBean authenticationFilterRegistration() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/AppController.java"] = [
    (
        "/**\n * @author Carpenter Lee\n */",
        "/**\n * 应用与机器管理 REST API。\n * <p>提供应用列表、机器查询及手动移除机器等接口。\n *\n * @author Carpenter Lee\n */",
    ),
    (
        "    @GetMapping(\"/names.json\")\n    public Result<List<String>> queryApps(HttpServletRequest request) {",
        "    /** 返回所有已注册应用名称。 */\n    @GetMapping(\"/names.json\")\n    public Result<List<String>> queryApps(HttpServletRequest request) {",
    ),
    (
        "    @GetMapping(\"/briefinfos.json\")\n    public Result<List<AppInfo>> queryAppInfos(HttpServletRequest request) {",
        "    /** 返回应用摘要信息列表，按应用名排序。 */\n    @GetMapping(\"/briefinfos.json\")\n    public Result<List<AppInfo>> queryAppInfos(HttpServletRequest request) {",
    ),
    (
        "    @GetMapping(value = \"/{app}/machines.json\")\n    public Result<List<MachineInfoVo>> getMachinesByApp(@PathVariable(\"app\") String app) {",
        "    /** 查询指定应用下的机器列表。 */\n    @GetMapping(value = \"/{app}/machines.json\")\n    public Result<List<MachineInfoVo>> getMachinesByApp(@PathVariable(\"app\") String app) {",
    ),
    (
        "    @RequestMapping(value = \"/{app}/machine/remove.json\")\n    public Result<String> removeMachineById(",
        "    /** 按 IP 与端口从应用中移除机器。 */\n    @RequestMapping(value = \"/{app}/machine/remove.json\")\n    public Result<String> removeMachineById(",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/AuthController.java"] = [
    (
        "/**\n * @author cdfive\n * @since 1.6.0\n */",
        "/**\n * Dashboard 登录、登出与会话校验 API。\n * <p>支持通过 {@link DashboardConfig} 或配置文件覆盖默认凭据。\n *\n * @author cdfive\n * @since 1.6.0\n */",
    ),
    (
        "    @PostMapping(\"/login\")\n    public Result<AuthService.AuthUser> login(HttpServletRequest request, String username, String password) {",
        "    /** 校验用户名密码并在 Session 中写入登录用户。 */\n    @PostMapping(\"/login\")\n    public Result<AuthService.AuthUser> login(HttpServletRequest request, String username, String password) {",
    ),
    (
        "        /*\n         * If auth.username or auth.password is blank(set in application.properties or VM arguments),\n         * auth will pass, as the front side validate the input which can't be blank,\n         * so user can input any username or password(both are not blank) to login in that case.\n         */",
        "        /*\n         * 若 auth.username 或 auth.password 未配置（留空），则跳过凭据校验；\n         * 前端仍要求非空输入，因此任意非空用户名密码均可登录。\n         */",
    ),
    (
        "    @PostMapping(value = \"/logout\")\n    public Result<?> logout(HttpServletRequest request) {",
        "    /** 使当前 Session 失效，完成登出。 */\n    @PostMapping(value = \"/logout\")\n    public Result<?> logout(HttpServletRequest request) {",
    ),
    (
        "    @PostMapping(value = \"/check\")\n    public Result<?> check(HttpServletRequest request) {",
        "    /** 检查当前请求是否已登录。 */\n    @PostMapping(value = \"/check\")\n    public Result<?> check(HttpServletRequest request) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/AuthorityRuleController.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 0.2.1\n */",
        "/**\n * 授权规则（黑白名单）REST API。\n * <p>从客户端拉取规则、持久化到 {@link RuleRepository} 并在变更后推送到目标机器。\n *\n * @author Eric Zhao\n * @since 0.2.1\n */",
    ),
    (
        "    @GetMapping(\"/rules\")\n    @AuthAction(PrivilegeType.READ_RULE)\n    public Result<List<AuthorityRuleEntity>> apiQueryAllRulesForMachine(@RequestParam String app,",
        "    /** 查询指定机器上的全部授权规则并同步到本地仓库。 */\n    @GetMapping(\"/rules\")\n    @AuthAction(PrivilegeType.READ_RULE)\n    public Result<List<AuthorityRuleEntity>> apiQueryAllRulesForMachine(@RequestParam String app,",
    ),
    (
        "    private <R> Result<R> checkEntityInternal(AuthorityRuleEntity entity) {",
        "    /** 校验授权规则实体字段合法性。 */\n    private <R> Result<R> checkEntityInternal(AuthorityRuleEntity entity) {",
    ),
    (
        "    @PostMapping(\"/rule\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<AuthorityRuleEntity> apiAddAuthorityRule(@RequestBody AuthorityRuleEntity entity) {",
        "    /** 新增授权规则并推送到客户端。 */\n    @PostMapping(\"/rule\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<AuthorityRuleEntity> apiAddAuthorityRule(@RequestBody AuthorityRuleEntity entity) {",
    ),
    (
        "    @PutMapping(\"/rule/{id}\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<AuthorityRuleEntity> apiUpdateParamFlowRule(@PathVariable(\"id\") Long id,",
        "    /** 按 id 更新授权规则并重新发布。 */\n    @PutMapping(\"/rule/{id}\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<AuthorityRuleEntity> apiUpdateParamFlowRule(@PathVariable(\"id\") Long id,",
    ),
    (
        "    @DeleteMapping(\"/rule/{id}\")\n    @AuthAction(PrivilegeType.DELETE_RULE)\n    public Result<Long> apiDeleteRule(@PathVariable(\"id\") Long id) {",
        "    /** 删除授权规则并同步到客户端。 */\n    @DeleteMapping(\"/rule/{id}\")\n    @AuthAction(PrivilegeType.DELETE_RULE)\n    public Result<Long> apiDeleteRule(@PathVariable(\"id\") Long id) {",
    ),
    (
        "    private boolean publishRules(String app, String ip, Integer port) {",
        "    /** 将机器上全部授权规则推送到 Sentinel 客户端。 */\n    private boolean publishRules(String app, String ip, Integer port) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/DegradeController.java"] = [
    (
        "/**\n * Controller regarding APIs of degrade rules. Refactored since 1.8.0.\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
        "/**\n * 熔断降级规则 REST API（1.8.0 起重构）。\n * <p>支持查询、增删改及向客户端发布 {@link DegradeRuleEntity}。\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
    ),
    (
        "    @GetMapping(\"/rules.json\")\n    @AuthAction(PrivilegeType.READ_RULE)\n    public Result<List<DegradeRuleEntity>> apiQueryMachineRules(String app, String ip, Integer port) {",
        "    /** 查询指定机器的降级规则并同步到本地仓库。 */\n    @GetMapping(\"/rules.json\")\n    @AuthAction(PrivilegeType.READ_RULE)\n    public Result<List<DegradeRuleEntity>> apiQueryMachineRules(String app, String ip, Integer port) {",
    ),
    (
        "    @PostMapping(\"/rule\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<DegradeRuleEntity> apiAddRule(@RequestBody DegradeRuleEntity entity) {",
        "    /** 新增降级规则并发布到客户端。 */\n    @PostMapping(\"/rule\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<DegradeRuleEntity> apiAddRule(@RequestBody DegradeRuleEntity entity) {",
    ),
    (
        "    @PutMapping(\"/rule/{id}\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<DegradeRuleEntity> apiUpdateRule(@PathVariable(\"id\") Long id,",
        "    /** 更新降级规则，保留原 app/ip/port 绑定。 */\n    @PutMapping(\"/rule/{id}\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<DegradeRuleEntity> apiUpdateRule(@PathVariable(\"id\") Long id,",
    ),
    (
        "    @DeleteMapping(\"/rule/{id}\")\n    @AuthAction(PrivilegeType.DELETE_RULE)\n    public Result<Long> delete(@PathVariable(\"id\") Long id) {",
        "    /** 删除降级规则并同步到客户端。 */\n    @DeleteMapping(\"/rule/{id}\")\n    @AuthAction(PrivilegeType.DELETE_RULE)\n    public Result<Long> delete(@PathVariable(\"id\") Long id) {",
    ),
    (
        "    private boolean publishRules(String app, String ip, Integer port) {",
        "    /** 将机器上全部降级规则推送到 Sentinel 客户端。 */\n    private boolean publishRules(String app, String ip, Integer port) {",
    ),
    (
        "    private <R> Result<R> checkEntityInternal(DegradeRuleEntity entity) {",
        "    /** 校验降级规则字段，含熔断策略与阈值约束。 */\n    private <R> Result<R> checkEntityInternal(DegradeRuleEntity entity) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/DemoController.java"] = [
    (
        "@Controller\n@RequestMapping(value = \"/demo\", produces = MediaType.APPLICATION_JSON_VALUE)\npublic class DemoController {",
        "/**\n * Sentinel 功能演示控制器，用于构造调用链、循环与慢请求等测试场景。\n */\n@Controller\n@RequestMapping(value = \"/demo\", produces = MediaType.APPLICATION_JSON_VALUE)\npublic class DemoController {",
    ),
    (
        "    @RequestMapping(\"/greeting\")\n    public String greeting() {",
        "    /** 返回演示首页视图名。 */\n    @RequestMapping(\"/greeting\")\n    public String greeting() {",
    ),
    (
        "    @RequestMapping(\"/link\")\n    @ResponseBody\n    public String link() throws BlockException {",
        "    /** 构造嵌套 {@link SphU#entry} 调用链以演示链路流控。 */\n    @RequestMapping(\"/link\")\n    @ResponseBody\n    public String link() throws BlockException {",
    ),
    (
        "    @RequestMapping(\"/loop\")\n    @ResponseBody\n    public String loop(String name, int time) throws BlockException {",
        "    /** 启动多线程循环访问资源，用于压测与规则验证。 */\n    @RequestMapping(\"/loop\")\n    @ResponseBody\n    public String loop(String name, int time) throws BlockException {",
    ),
    (
        "    @RequestMapping(\"/slow\")\n    @ResponseBody\n    public String slow(String name, int time) throws BlockException {",
        "    /** 启动带慢调用（sleep）的循环线程，用于 RT 熔断演示。 */\n    @RequestMapping(\"/slow\")\n    @ResponseBody\n    public String slow(String name, int time) throws BlockException {",
    ),
    (
        "    static class RunTask implements Runnable {",
        "    /** 后台循环执行 {@link SphU#entry} 的任务。 */\n    static class RunTask implements Runnable {",
    ),
    (
        "        @Override\n        public void run() {",
        "        /** 在独立上下文中循环 entry/exit，可选模拟慢请求。 */\n        @Override\n        public void run() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/FlowControllerV1.java"] = [
    (
        "/**\n * Flow rule controller.\n *\n * @author leyou\n * @author Eric Zhao\n */",
        "/**\n * 流控规则 v1 REST API。\n * <p>提供规则的查询、增删改，并通过 {@link SentinelApiClient} 异步推送到客户端。\n *\n * @author leyou\n * @author Eric Zhao\n */",
    ),
    (
        "    @GetMapping(\"/rules\")\n    @AuthAction(PrivilegeType.READ_RULE)\n    public Result<List<FlowRuleEntity>> apiQueryMachineRules(@RequestParam String app,",
        "    /** 查询指定机器的流控规则并同步到内存仓库。 */\n    @GetMapping(\"/rules\")\n    @AuthAction(PrivilegeType.READ_RULE)\n    public Result<List<FlowRuleEntity>> apiQueryMachineRules(@RequestParam String app,",
    ),
    (
        "    private <R> Result<R> checkEntityInternal(FlowRuleEntity entity) {",
        "    /** 校验流控规则字段，含 grade、strategy、controlBehavior 与集群模式。 */\n    private <R> Result<R> checkEntityInternal(FlowRuleEntity entity) {",
    ),
    (
        "    @PostMapping(\"/rule\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<FlowRuleEntity> apiAddFlowRule(@RequestBody FlowRuleEntity entity) {",
        "    /** 新增流控规则并等待异步发布完成（最多 5 秒）。 */\n    @PostMapping(\"/rule\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<FlowRuleEntity> apiAddFlowRule(@RequestBody FlowRuleEntity entity) {",
    ),
    (
        "    @PutMapping(\"/save.json\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<FlowRuleEntity> apiUpdateFlowRule(Long id, String app,",
        "    /** 按 id 部分更新流控规则字段并重新发布。 */\n    @PutMapping(\"/save.json\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<FlowRuleEntity> apiUpdateFlowRule(Long id, String app,",
    ),
    (
        "    @DeleteMapping(\"/delete.json\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<Long> apiDeleteFlowRule(Long id) {",
        "    /** 删除流控规则并同步到客户端。 */\n    @DeleteMapping(\"/delete.json\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<Long> apiDeleteFlowRule(Long id) {",
    ),
    (
        "    private CompletableFuture<Void> publishRules(String app, String ip, Integer port) {",
        "    /** 异步将机器上全部流控规则推送到 Sentinel 客户端。 */\n    private CompletableFuture<Void> publishRules(String app, String ip, Integer port) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/MachineRegistryController.java"] = [
    (
        "@Controller\n@RequestMapping(value = \"/registry\", produces = MediaType.APPLICATION_JSON_VALUE)\npublic class MachineRegistryController {",
        "/**\n * 客户端机器心跳注册 API。\n * <p>接收 Sentinel 客户端上报的机器信息并写入 {@link AppManagement}。\n */\n@Controller\n@RequestMapping(value = \"/registry\", produces = MediaType.APPLICATION_JSON_VALUE)\npublic class MachineRegistryController {",
    ),
    (
        "    @ResponseBody\n    @RequestMapping(\"/machine\")\n    public Result<?> receiveHeartBeat(String app,",
        "    /** 处理客户端心跳，校验 app/ip/port 并更新机器最后心跳时间。 */\n    @ResponseBody\n    @RequestMapping(\"/machine\")\n    public Result<?> receiveHeartBeat(String app,",
    ),
    (
        "        if (port == -1) {",
        "        // 端口尚未就绪时拒绝注册。\n        if (port == -1) {",
    ),
    (
        "        String sentinelVersion = StringUtil.isBlank(v) ? \"unknown\" : v;",
        "        // 客户端版本号缺失时使用 unknown。\n        String sentinelVersion = StringUtil.isBlank(v) ? \"unknown\" : v;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/MetricController.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * 监控指标查询 API。\n * <p>支持按应用分页查询 Top 资源指标及单资源时序数据。\n *\n * @author leyou\n */",
    ),
    (
        "    private static final long maxQueryIntervalMs = 1000 * 60 * 60;",
        "    /** 单次查询最大时间跨度（1 小时）。 */\n    private static final long maxQueryIntervalMs = 1000 * 60 * 60;",
    ),
    (
        "    @ResponseBody\n    @RequestMapping(\"/queryTopResourceMetric.json\")\n    public Result<?> queryTopResourceMetric(final String app,",
        "    /** 分页查询应用下 Top 资源的时序指标，支持关键字过滤与升降序。 */\n    @ResponseBody\n    @RequestMapping(\"/queryTopResourceMetric.json\")\n    public Result<?> queryTopResourceMetric(final String app,",
    ),
    (
        "        // order matters.",
        "        // 保持与 topResource 分页顺序一致。",
    ),
    (
        "    @ResponseBody\n    @RequestMapping(\"/queryByAppAndResource.json\")\n    public Result<?> queryByAppAndResource(String app, String identity, Long startTime, Long endTime) {",
        "    /** 查询单个资源在指定时间范围内的指标序列。 */\n    @ResponseBody\n    @RequestMapping(\"/queryByAppAndResource.json\")\n    public Result<?> queryByAppAndResource(String app, String identity, Long startTime, Long endTime) {",
    ),
    (
        "    private Iterable<MetricVo> sortMetricVoAndDistinct(List<MetricVo> vos) {",
        "    /** 按时间戳排序并去重，同一时刻保留 gmtCreate 较新的记录。 */\n    private Iterable<MetricVo> sortMetricVoAndDistinct(List<MetricVo> vos) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/ParamFlowRuleController.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 0.2.1\n */",
        "/**\n * 热点参数流控规则 REST API。\n * <p>依赖客户端 0.2.0+ 及 param-flow 扩展；不支持时返回 4041 错误码。\n *\n * @author Eric Zhao\n * @since 0.2.1\n */",
    ),
    (
        "    private boolean checkIfSupported(String app, String ip, int port) {",
        "    /** 检查目标机器 Sentinel 版本是否支持热点参数流控。 */\n    private boolean checkIfSupported(String app, String ip, int port) {",
    ),
    (
        "            // If error occurred or cannot retrieve machine info, return true.",
        "            // 无法获取机器信息时默认可用，由客户端调用时再判定。",
    ),
    (
        "    @GetMapping(\"/rules\")\n    @AuthAction(PrivilegeType.READ_RULE)\n    public Result<List<ParamFlowRuleEntity>> apiQueryAllRulesForMachine(@RequestParam String app,",
        "    /** 查询指定机器的热点参数流控规则。 */\n    @GetMapping(\"/rules\")\n    @AuthAction(PrivilegeType.READ_RULE)\n    public Result<List<ParamFlowRuleEntity>> apiQueryAllRulesForMachine(@RequestParam String app,",
    ),
    (
        "    private boolean isNotSupported(Throwable ex) {",
        "    /** 判断异常是否因客户端命令不存在（版本不支持）。 */\n    private boolean isNotSupported(Throwable ex) {",
    ),
    (
        "    @PostMapping(\"/rule\")\n    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)\n    public Result<ParamFlowRuleEntity> apiAddParamFlowRule(@RequestBody ParamFlowRuleEntity entity) {",
        "    /** 新增热点参数流控规则并发布。 */\n    @PostMapping(\"/rule\")\n    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)\n    public Result<ParamFlowRuleEntity> apiAddParamFlowRule(@RequestBody ParamFlowRuleEntity entity) {",
    ),
    (
        "    private <R> Result<R> checkEntityInternal(ParamFlowRuleEntity entity) {",
        "    /** 校验热点参数流控规则实体字段。 */\n    private <R> Result<R> checkEntityInternal(ParamFlowRuleEntity entity) {",
    ),
    (
        "    @PutMapping(\"/rule/{id}\")\n    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)\n    public Result<ParamFlowRuleEntity> apiUpdateParamFlowRule(@PathVariable(\"id\") Long id,",
        "    /** 更新热点参数流控规则。 */\n    @PutMapping(\"/rule/{id}\")\n    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)\n    public Result<ParamFlowRuleEntity> apiUpdateParamFlowRule(@PathVariable(\"id\") Long id,",
    ),
    (
        "    @DeleteMapping(\"/rule/{id}\")\n    @AuthAction(PrivilegeType.DELETE_RULE)\n    public Result<Long> apiDeleteRule(@PathVariable(\"id\") Long id) {",
        "    /** 删除热点参数流控规则。 */\n    @DeleteMapping(\"/rule/{id}\")\n    @AuthAction(PrivilegeType.DELETE_RULE)\n    public Result<Long> apiDeleteRule(@PathVariable(\"id\") Long id) {",
    ),
    (
        "    private CompletableFuture<Void> publishRules(String app, String ip, Integer port) {",
        "    /** 异步将机器上全部热点参数规则推送到客户端。 */\n    private CompletableFuture<Void> publishRules(String app, String ip, Integer port) {",
    ),
    (
        "    private <R> Result<R> unsupportedVersion() {",
        "    /** 返回客户端不支持热点参数流控的标准错误响应。 */\n    private <R> Result<R> unsupportedVersion() {",
    ),
    (
        "    private final SentinelVersion version020 = new SentinelVersion().setMinorVersion(2);",
        "    /** 支持热点参数流控的最低 minor 版本（0.2.x）。 */\n    private final SentinelVersion version020 = new SentinelVersion().setMinorVersion(2);",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/ResourceController.java"] = [
    (
        "/**\n * @author Carpenter Lee\n */",
        "/**\n * 资源调用链实时统计 API。\n * <p>从客户端拉取资源树或集群节点视图并转换为 {@link ResourceVo}。\n *\n * @author Carpenter Lee\n */",
    ),
    (
        "    /**\n     * Fetch real time statistics info of the machine.\n     *\n     * @param ip        ip to fetch\n     * @param port      port of the ip\n     * @param type      one of [root, default, cluster], 'root' means fetching from tree root node, 'default' means\n     *                  fetching from tree default node, 'cluster' means fetching from cluster node.\n     * @param searchKey key to search\n     * @return node statistics info.\n     */",
        "    /**\n     * 拉取指定机器的资源实时统计信息。\n     *\n     * @param ip        目标机器 IP\n     * @param port      目标机器端口\n     * @param type      取值 root、default 或 cluster：root/default 返回调用树，cluster 返回集群节点列表\n     * @param searchKey 资源名过滤关键字\n     * @return 资源统计视图列表\n     */",
    ),
    (
        "            // Normal (cluster node).",
        "            // cluster 类型：拉取集群节点列表。",
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
    index_file = Path("/tmp/git-index-sentinel-w19a")
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
        return 1

    analyzed_paths = [f"sentinel/1.8.10/analyzed/{rel}" for rel in BATCH_LIST]
    script_path = f"scripts/{SCRIPT_NAME}"
    sha, tree_count = isolated_index_commit(
        "sentinel 1.8.10: Chinese-annotate wave 19a [0:15]",
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
        "queue: mark sentinel 1.8.10 wave19a done",
        queue_paths,
        base_ref="HEAD",
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "origin", "main"], check=True)

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
                "all_chinese": all(chinese.values()),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
