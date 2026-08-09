#!/usr/bin/env python3
"""Chinese-annotate RocketMQ rocketmq-all-5.5.0 wave-1b auth providers/strategies/authorization [15:30]."""
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
VER = ROOT / "rocketmq/rocketmq-all-5.5.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
SCRIPTS = ROOT / "scripts"
QUEUE = VER / "_reports/class-queue"
BATCH_LIST = [
    ln.strip()
    for ln in Path("/tmp/rocketmq_w1b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_rocketmq_wave1b_batch15_30.py"
MARK_NOTE = "wave1b [15:30]"

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/provider/AuthenticationMetadataProvider.java"] = [
    (
        "public interface AuthenticationMetadataProvider {",
        "/**\n * 认证元数据提供者：负责用户 CRUD 与查询，供管理接口与鉴权链使用。\n */\npublic interface AuthenticationMetadataProvider {",
    ),
    (
        "    void initialize(AuthConfig authConfig, Supplier<?> metadataService);",
        "    /** 初始化存储后端与缓存，绑定 {@link AuthConfig} 与可选元数据服务。 */\n    void initialize(AuthConfig authConfig, Supplier<?> metadataService);",
    ),
    (
        "    void shutdown();",
        "    /** 关闭 RocksDB 与后台线程等资源。 */\n    void shutdown();",
    ),
    (
        "    CompletableFuture<Void> createUser(User user);",
        "    /** 创建用户并持久化。 */\n    CompletableFuture<Void> createUser(User user);",
    ),
    (
        "    CompletableFuture<Void> deleteUser(String username);",
        "    /** 按用户名删除用户。 */\n    CompletableFuture<Void> deleteUser(String username);",
    ),
    (
        "    CompletableFuture<Void> updateUser(User user);",
        "    /** 更新已有用户信息。 */\n    CompletableFuture<Void> updateUser(User user);",
    ),
    (
        "    CompletableFuture<User> getUser(String username);",
        "    /** 按用户名查询单个用户。 */\n    CompletableFuture<User> getUser(String username);",
    ),
    (
        "    CompletableFuture<List<User>> listUser(String filter);",
        "    /** 列出用户；filter 非空时按用户名子串过滤。 */\n    CompletableFuture<List<User>> listUser(String filter);",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/provider/AuthenticationProvider.java"] = [
    (
        "public interface AuthenticationProvider<AuthenticationContext> {",
        "/**\n * 认证提供者 SPI：封装上下文构建与认证责任链执行，支持 gRPC 与 Remoting 两种入口。\n */\npublic interface AuthenticationProvider<AuthenticationContext> {",
    ),
    (
        "    void initialize(AuthConfig config, Supplier<?> metadataService);",
        "    /** 注入配置与元数据服务，初始化上下文构建器。 */\n    void initialize(AuthConfig config, Supplier<?> metadataService);",
    ),
    (
        "    CompletableFuture<Void> authenticate(AuthenticationContext context);",
        "    /** 对给定上下文执行认证责任链。 */\n    CompletableFuture<Void> authenticate(AuthenticationContext context);",
    ),
    (
        "    AuthenticationContext newContext(Metadata metadata, GeneratedMessageV3 request);",
        "    /** 从 gRPC Metadata 与 Protobuf 请求构建认证上下文。 */\n    AuthenticationContext newContext(Metadata metadata, GeneratedMessageV3 request);",
    ),
    (
        "    AuthenticationContext newContext(ChannelHandlerContext context, RemotingCommand command);",
        "    /** 从 Netty 通道与 Remoting 命令构建认证上下文。 */\n    AuthenticationContext newContext(ChannelHandlerContext context, RemotingCommand command);",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/provider/DefaultAuthenticationProvider.java"] = [
    (
        "public class DefaultAuthenticationProvider implements AuthenticationProvider<DefaultAuthenticationContext> {",
        "/**\n * 默认认证提供者：组装 {@link DefaultAuthenticationContextBuilder} 与\n * {@link DefaultAuthenticationHandler} 责任链，并在完成后写审计日志。\n */\npublic class DefaultAuthenticationProvider implements AuthenticationProvider<DefaultAuthenticationContext> {",
    ),
    (
        "    protected final Logger log = LoggerFactory.getLogger(LoggerName.ROCKETMQ_AUTH_AUDIT_LOGGER_NAME);",
        "    /** 认证审计专用 Logger。 */\n    protected final Logger log = LoggerFactory.getLogger(LoggerName.ROCKETMQ_AUTH_AUDIT_LOGGER_NAME);",
    ),
    (
        "    @Override\n    public void initialize(AuthConfig config, Supplier<?> metadataService) {",
        "    /** 保存配置并创建默认上下文构建器。 */\n    @Override\n    public void initialize(AuthConfig config, Supplier<?> metadataService) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> authenticate(DefaultAuthenticationContext context) {",
        "    /** 执行认证链，无论成功失败均触发审计日志。 */\n    @Override\n    public CompletableFuture<Void> authenticate(DefaultAuthenticationContext context) {",
    ),
    (
        "    @Override\n    public DefaultAuthenticationContext newContext(Metadata metadata, GeneratedMessageV3 request) {",
        "    /** 构建 gRPC 场景的 {@link DefaultAuthenticationContext}。 */\n    @Override\n    public DefaultAuthenticationContext newContext(Metadata metadata, GeneratedMessageV3 request) {",
    ),
    (
        "    @Override\n    public DefaultAuthenticationContext newContext(ChannelHandlerContext context, RemotingCommand command) {",
        "    /** 构建 Remoting 场景的 {@link DefaultAuthenticationContext}。 */\n    @Override\n    public DefaultAuthenticationContext newContext(ChannelHandlerContext context, RemotingCommand command) {",
    ),
    (
        "    protected HandlerChain<DefaultAuthenticationContext, CompletableFuture<Void>> newHandlerChain() {",
        "    /** 创建仅含 {@link DefaultAuthenticationHandler} 的单节点责任链。 */\n    protected HandlerChain<DefaultAuthenticationContext, CompletableFuture<Void>> newHandlerChain() {",
    ),
    (
        "    protected void doAuditLog(DefaultAuthenticationContext context, Throwable ex) {",
        "    /** 用户名非空时记录认证成功（debug）或失败（info）审计。 */\n    protected void doAuditLog(DefaultAuthenticationContext context, Throwable ex) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/provider/LocalAuthenticationMetadataProvider.java"] = [
    (
        "public class LocalAuthenticationMetadataProvider implements AuthenticationMetadataProvider {",
        "/**\n * 本地 RocksDB 用户元数据提供者：JSON 序列化用户记录，Caffeine 缓存加速读路径。\n */\npublic class LocalAuthenticationMetadataProvider implements AuthenticationMetadataProvider {",
    ),
    (
        "    private final static String AUTH_METADATA_COLUMN_FAMILY = new String(RocksDB.DEFAULT_COLUMN_FAMILY,\n        StandardCharsets.UTF_8);",
        "    /** RocksDB 默认列族名，用于存储用户键值。 */\n    private final static String AUTH_METADATA_COLUMN_FAMILY = new String(RocksDB.DEFAULT_COLUMN_FAMILY,\n        StandardCharsets.UTF_8);",
    ),
    (
        "    @Override\n    public void initialize(AuthConfig authConfig, Supplier<?> metadataService) {",
        "    /** 打开 users 目录下 RocksDB，并按配置初始化 Caffeine 用户缓存。 */\n    @Override\n    public void initialize(AuthConfig authConfig, Supplier<?> metadataService) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> createUser(User user) {",
        "    /** 写入用户 JSON 到 RocksDB，刷 WAL 并失效缓存。 */\n    @Override\n    public CompletableFuture<Void> createUser(User user) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> deleteUser(String username) {",
        "    /** 从 RocksDB 删除用户并失效缓存。 */\n    @Override\n    public CompletableFuture<Void> deleteUser(String username) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> updateUser(User user) {",
        "    /** 覆盖写入用户信息并失效缓存。 */\n    @Override\n    public CompletableFuture<Void> updateUser(User user) {",
    ),
    (
        "    @Override\n    public CompletableFuture<User> getUser(String username) {",
        "    /** 经 Caffeine 缓存读取用户；未命中时返回 null。 */\n    @Override\n    public CompletableFuture<User> getUser(String username) {",
    ),
    (
        "    @Override\n    public CompletableFuture<List<User>> listUser(String filter) {",
        "    /** 全表扫描用户，可选按用户名子串过滤。 */\n    @Override\n    public CompletableFuture<List<User>> listUser(String filter) {",
    ),
    (
        "    @Override\n    public void shutdown() {",
        "    /** 关闭 RocksDB 存储与缓存刷新线程池。 */\n    @Override\n    public void shutdown() {",
    ),
    (
        "    private static class UserCacheLoader implements CacheLoader<String, User> {",
        "    /** Caffeine {@link CacheLoader}：从 RocksDB 加载用户，空值用 {@link #EMPTY_USER} 占位。 */\n    private static class UserCacheLoader implements CacheLoader<String, User> {",
    ),
    (
        "        public static final User EMPTY_USER = new User();",
        "        /** 表示用户不存在的哨兵对象，避免缓存穿透。 */\n        public static final User EMPTY_USER = new User();",
    ),
    (
        "        @Override\n        public User load(String username) {",
        "        /** 按用户名从 RocksDB 反序列化 {@link User}。 */\n        @Override\n        public User load(String username) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/strategy/AbstractAuthenticationStrategy.java"] = [
    (
        "public abstract class AbstractAuthenticationStrategy implements AuthenticationStrategy {",
        "/**\n * 认证策略抽象基类：解析白名单 RPC、获取 {@link AuthenticationProvider} 并同步执行认证。\n */\npublic abstract class AbstractAuthenticationStrategy implements AuthenticationStrategy {",
    ),
    (
        "    protected final Set<String> authenticationWhiteSet = new HashSet<>();",
        "    /** 免认证 RPC 代码集合，来自配置的逗号分隔白名单。 */\n    protected final Set<String> authenticationWhiteSet = new HashSet<>();",
    ),
    (
        "    public AbstractAuthenticationStrategy(AuthConfig authConfig, Supplier<?> metadataService) {",
        "    /** 初始化 Provider 并解析 {@code authenticationWhitelist} 配置。 */\n    public AbstractAuthenticationStrategy(AuthConfig authConfig, Supplier<?> metadataService) {",
    ),
    (
        "    protected void doEvaluate(AuthenticationContext context) {",
        "    /** 认证开关关闭、Provider 缺失或 RPC 在白名单内时跳过；否则阻塞等待认证完成。 */\n    protected void doEvaluate(AuthenticationContext context) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/strategy/AuthenticationStrategy.java"] = [
    (
        "public interface AuthenticationStrategy {",
        "/**\n * 认证策略 SPI：对 {@link AuthenticationContext} 执行一次认证评估。\n */\npublic interface AuthenticationStrategy {",
    ),
    (
        "    void evaluate(AuthenticationContext context);",
        "    /** 评估并可能抛出 {@link org.apache.rocketmq.auth.authentication.exception.AuthenticationException}。 */\n    void evaluate(AuthenticationContext context);",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/strategy/StatefulAuthenticationStrategy.java"] = [
    (
        "public class StatefulAuthenticationStrategy extends AbstractAuthenticationStrategy {",
        "/**\n * 有状态认证策略：按 channelId（及用户名）缓存认证结果，减少重复校验开销。\n */\npublic class StatefulAuthenticationStrategy extends AbstractAuthenticationStrategy {",
    ),
    (
        "    protected Cache<String, Pair<Boolean, AuthenticationException>> authCache;",
        "    /** 缓存键为 channelId 或 channelId#username，值为成功标志与异常。 */\n    protected Cache<String, Pair<Boolean, AuthenticationException>> authCache;",
    ),
    (
        "    public StatefulAuthenticationStrategy(AuthConfig authConfig, Supplier<?> metadataService) {",
        "    /** 按配置创建带过期与容量上限的 Caffeine 缓存。 */\n    public StatefulAuthenticationStrategy(AuthConfig authConfig, Supplier<?> metadataService) {",
    ),
    (
        "    @Override\n    public void evaluate(AuthenticationContext context) {",
        "    /** 无 channelId 时直接认证；否则命中缓存或执行后缓存结果。 */\n    @Override\n    public void evaluate(AuthenticationContext context) {",
    ),
    (
        "    private String buildKey(AuthenticationContext context) {",
        "    /** 构建缓存键：channelId 或 channelId#username。 */\n    private String buildKey(AuthenticationContext context) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/strategy/StatelessAuthenticationStrategy.java"] = [
    (
        "public class StatelessAuthenticationStrategy extends AbstractAuthenticationStrategy {",
        "/**\n * 无状态认证策略：每次请求均完整执行 {@link #doEvaluate}，不做结果缓存。\n */\npublic class StatelessAuthenticationStrategy extends AbstractAuthenticationStrategy {",
    ),
    (
        "    public StatelessAuthenticationStrategy(AuthConfig authConfig, Supplier<?> metadataService) {",
        "    /** 委托基类初始化 Provider 与白名单。 */\n    public StatelessAuthenticationStrategy(AuthConfig authConfig, Supplier<?> metadataService) {",
    ),
    (
        "    @Override\n    public void evaluate(AuthenticationContext context) {",
        "    /** 直接调用基类认证逻辑。 */\n    @Override\n    public void evaluate(AuthenticationContext context) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/AuthorizationEvaluator.java"] = [
    (
        "public class AuthorizationEvaluator {",
        "/**\n * 授权评估入口：通过 {@link AuthorizationFactory} 获取策略并对上下文列表逐条评估。\n */\npublic class AuthorizationEvaluator {",
    ),
    (
        "    public AuthorizationEvaluator(AuthConfig authConfig) {",
        "    /** 使用默认元数据服务（null）构造。 */\n    public AuthorizationEvaluator(AuthConfig authConfig) {",
    ),
    (
        "    public AuthorizationEvaluator(AuthConfig authConfig, Supplier<?> metadataService) {",
        "    /** 指定元数据服务 Supplier 构造授权策略。 */\n    public AuthorizationEvaluator(AuthConfig authConfig, Supplier<?> metadataService) {",
    ),
    (
        "    public void evaluate(List<AuthorizationContext> contexts) {",
        "    /** 对非空上下文列表依次调用策略 {@code evaluate}。 */\n    public void evaluate(List<AuthorizationContext> contexts) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/builder/AuthorizationContextBuilder.java"] = [
    (
        "public interface AuthorizationContextBuilder {",
        "/**\n * 授权上下文构建器：将 gRPC 或 Remoting 请求解析为 {@link DefaultAuthorizationContext} 列表。\n */\npublic interface AuthorizationContextBuilder {",
    ),
    (
        "    List<DefaultAuthorizationContext> build(Metadata metadata, GeneratedMessageV3 message);",
        "    /** 从 gRPC Metadata 与 Protobuf 消息提取待授权资源与动作。 */\n    List<DefaultAuthorizationContext> build(Metadata metadata, GeneratedMessageV3 message);",
    ),
    (
        "    List<DefaultAuthorizationContext> build(ChannelHandlerContext context, RemotingCommand command);",
        "    /** 从 Netty 通道与 Remoting 命令构建授权上下文。 */\n    List<DefaultAuthorizationContext> build(ChannelHandlerContext context, RemotingCommand command);",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/chain/AclAuthorizationHandler.java"] = [
    (
        "public class AclAuthorizationHandler implements Handler<DefaultAuthorizationContext, CompletableFuture<Void>> {",
        "/**\n * ACL 授权处理器：加载主体 ACL，匹配 CUSTOM/DEFAULT 策略条目并判定 ALLOW/DENY。\n */\npublic class AclAuthorizationHandler implements Handler<DefaultAuthorizationContext, CompletableFuture<Void>> {",
    ),
    (
        "    public AclAuthorizationHandler(AuthConfig config) {",
        "    /** 使用默认元数据 Provider 构造。 */\n    public AclAuthorizationHandler(AuthConfig config) {",
    ),
    (
        "    public AclAuthorizationHandler(AuthConfig config, Supplier<?> metadataService) {",
        "    /** 指定元数据服务 Supplier 获取 ACL 数据。 */\n    public AclAuthorizationHandler(AuthConfig config, Supplier<?> metadataService) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> handle(DefaultAuthorizationContext context,\n        HandlerChain<DefaultAuthorizationContext, CompletableFuture<Void>> chain) {",
        "    /** 异步加载 ACL，匹配策略条目；无匹配或 DENY 时抛出 {@link AuthorizationException}。 */\n    @Override\n    public CompletableFuture<Void> handle(DefaultAuthorizationContext context,\n        HandlerChain<DefaultAuthorizationContext, CompletableFuture<Void>> chain) {",
    ),
    (
        "            // 1. get the defined acl entries which match the request.",
        "            // 1. 查找与请求资源、动作、来源 IP 匹配的 ACL 条目",
    ),
    (
        "            // 2. if no matched acl entries, return deny",
        "            // 2. 无匹配条目则拒绝",
    ),
    (
        "            // 3. judge is the entries has denied decision.",
        "            // 3. 匹配条目为 DENY 则拒绝",
    ),
    (
        "    private PolicyEntry matchPolicyEntries(DefaultAuthorizationContext context, Acl acl) {",
        "    /** 优先 CUSTOM 策略，否则回退 DEFAULT；返回排序后最高优先级条目。 */\n    private PolicyEntry matchPolicyEntries(DefaultAuthorizationContext context, Acl acl) {",
    ),
    (
        "    private List<PolicyEntry> matchPolicyEntries(DefaultAuthorizationContext context, List<PolicyEntry> entries) {",
        "    /** 按资源、动作与环境（来源 IP）过滤策略条目。 */\n    private List<PolicyEntry> matchPolicyEntries(DefaultAuthorizationContext context, List<PolicyEntry> entries) {",
    ),
    (
        "    private int comparePolicyEntries(PolicyEntry o1, PolicyEntry o2) {",
        "    /** 比较优先级：LITERAL > PREFIXED > ANY，同模式 PREFIX 越长越优先，DENY 优于 ALLOW。 */\n    private int comparePolicyEntries(PolicyEntry o1, PolicyEntry o2) {",
    ),
    (
        "        // the decision deny has higher priority",
        "        // DENY 决策优先级高于 ALLOW",
    ),
    (
        "    private static void throwException(DefaultAuthorizationContext context, String detail) {",
        "    /** 构造并抛出带主体、资源、来源 IP 的授权失败异常。 */\n    private static void throwException(DefaultAuthorizationContext context, String detail) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/chain/UserAuthorizationHandler.java"] = [
    (
        "public class UserAuthorizationHandler implements Handler<DefaultAuthorizationContext, CompletableFuture<Void>> {",
        "/**\n * 用户授权前置处理器：校验用户存在且未禁用；SUPER 用户跳过下游 ACL 检查。\n */\npublic class UserAuthorizationHandler implements Handler<DefaultAuthorizationContext, CompletableFuture<Void>> {",
    ),
    (
        "    public UserAuthorizationHandler(AuthConfig config, Supplier<?> metadataService) {",
        "    /** 绑定认证元数据 Provider 以查询用户状态。 */\n    public UserAuthorizationHandler(AuthConfig config, Supplier<?> metadataService) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> handle(DefaultAuthorizationContext context, HandlerChain<DefaultAuthorizationContext, CompletableFuture<Void>> chain) {",
        "    /** 非 USER 主体直接传递；SUPER 用户短路；否则继续责任链。 */\n    @Override\n    public CompletableFuture<Void> handle(DefaultAuthorizationContext context, HandlerChain<DefaultAuthorizationContext, CompletableFuture<Void>> chain) {",
    ),
    (
        "    private CompletableFuture<User> getUser(Subject subject) {",
        "    /** 加载用户并校验存在性与 {@link UserStatus#DISABLE} 状态。 */\n    private CompletableFuture<User> getUser(Subject subject) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/context/AuthorizationContext.java"] = [
    (
        "public abstract class AuthorizationContext {",
        "/**\n * 授权上下文基类：承载通道 ID、RPC 代码与可扩展键值信息。\n */\npublic abstract class AuthorizationContext {",
    ),
    (
        "    @SuppressWarnings(\"unchecked\")\n    public <T> T getExtInfo(String key) {",
        "    /** 按 key 读取扩展属性，key 为空或不存在时返回 null。 */\n    @SuppressWarnings(\"unchecked\")\n    public <T> T getExtInfo(String key) {",
    ),
    (
        "    public void setExtInfo(String key, Object value) {",
        "    /** 写入扩展属性；key 或 value 为空时忽略。 */\n    public void setExtInfo(String key, Object value) {",
    ),
    (
        "    public boolean hasExtInfo(String key) {",
        "    /** 判断扩展属性是否存在且非 null。 */\n    public boolean hasExtInfo(String key) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/context/DefaultAuthorizationContext.java"] = [
    (
        "public class DefaultAuthorizationContext extends AuthorizationContext {",
        "/**\n * 默认授权上下文：包含主体、资源、动作列表与来源 IP。\n */\npublic class DefaultAuthorizationContext extends AuthorizationContext {",
    ),
    (
        "    public static DefaultAuthorizationContext of(Subject subject, Resource resource, Action action, String sourceIp) {",
        "    /** 单动作工厂方法。 */\n    public static DefaultAuthorizationContext of(Subject subject, Resource resource, Action action, String sourceIp) {",
    ),
    (
        "    public static DefaultAuthorizationContext of(Subject subject, Resource resource, List<Action> actions, String sourceIp) {",
        "    /** 多动作工厂方法。 */\n    public static DefaultAuthorizationContext of(Subject subject, Resource resource, List<Action> actions, String sourceIp) {",
    ),
    (
        "    public String getSubjectKey() {",
        "    /** 返回主体标识键，主体为空时返回 null。 */\n    public String getSubjectKey() {",
    ),
    (
        "    public String getResourceKey() {",
        "    /** 返回资源标识键，资源为空时返回 null。 */\n    public String getResourceKey() {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/enums/Decision.java"] = [
    (
        "public enum Decision {",
        "/**\n * ACL 策略决策：允许或拒绝访问。\n */\npublic enum Decision {",
    ),
    (
        "    ALLOW((byte) 1, \"Allow\"),",
        "    /** 允许访问。 */\n    ALLOW((byte) 1, \"Allow\"),",
    ),
    (
        "    DENY((byte) 2, \"Deny\");",
        "    /** 拒绝访问。 */\n    DENY((byte) 2, \"Deny\");",
    ),
    (
        "    public static Decision getByName(String name) {",
        "    /** 按名称（忽略大小写）解析决策枚举。 */\n    public static Decision getByName(String name) {",
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
    index_file = Path("/tmp/git-index-rocketmq-w1b")
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
        path = f"rocketmq/rocketmq-all-5.5.0/analyzed/{rel}"
        blob = subprocess.check_output(
            ["git", "-C", str(ROOT), "show", f"origin/main:{path}"],
            text=True,
        )
        result[rel] = has_chinese(blob)
    return result


def main() -> int:
    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    if len(BATCH_LIST) != 15:
        raise SystemExit(f"Expected 15 batch files, got {len(BATCH_LIST)}")
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

    analyzed_paths = [f"rocketmq/rocketmq-all-5.5.0/analyzed/{rel}" for rel in BATCH_LIST]
    script_path = f"scripts/{SCRIPT_NAME}"
    sha, tree_count = isolated_index_commit(
        "rocketmq rocketmq-all-5.5.0: Chinese-annotate wave 1b [15:30]",
        [*analyzed_paths, script_path],
    )
    push_main()

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "rocketmq",
            "--version",
            "rocketmq-all-5.5.0",
            "--note",
            MARK_NOTE,
            *BATCH_LIST,
        ],
        check=True,
    )
    queue_paths = [
        "rocketmq/rocketmq-all-5.5.0/_reports/class-queue/done.txt",
        "rocketmq/rocketmq-all-5.5.0/_reports/class-queue/pending.txt",
        "rocketmq/rocketmq-all-5.5.0/_reports/class-queue/batch.json",
        "rocketmq/rocketmq-all-5.5.0/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        f"queue: mark rocketmq rocketmq-all-5.5.0 {MARK_NOTE} done",
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
