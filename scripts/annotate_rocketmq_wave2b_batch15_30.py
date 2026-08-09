#!/usr/bin/env python3
"""Chinese-annotate RocketMQ rocketmq-all-5.5.0 wave-2b authorization/migration/broker [15:30]."""
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
    for ln in Path("/tmp/rocketmq_w2b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_rocketmq_wave2b_batch15_30.py"
MARK_NOTE = "wave2b [15:30]"

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/strategy/AbstractAuthorizationStrategy.java"] = [
    (
        "public abstract class AbstractAuthorizationStrategy implements AuthorizationStrategy {",
        "/**\n * 授权策略抽象基类：解析白名单 RPC、获取 {@link AuthorizationProvider} 并同步执行授权。\n */\npublic abstract class AbstractAuthorizationStrategy implements AuthorizationStrategy {",
    ),
    (
        "    protected final Set<String> authorizationWhiteSet = new HashSet<>();",
        "    /** 免授权 RPC 代码集合，来自配置的逗号分隔白名单。 */\n    protected final Set<String> authorizationWhiteSet = new HashSet<>();",
    ),
    (
        "    public AbstractAuthorizationStrategy(AuthConfig authConfig, Supplier<?> metadataService) {",
        "    /** 初始化 Provider 并解析 {@code authorizationWhitelist} 配置。 */\n    public AbstractAuthorizationStrategy(AuthConfig authConfig, Supplier<?> metadataService) {",
    ),
    (
        "    public void doEvaluate(AuthorizationContext context) {",
        "    /** 授权开关关闭、Provider 缺失或 RPC 在白名单内时跳过；否则阻塞等待授权完成。 */\n    public void doEvaluate(AuthorizationContext context) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/strategy/AuthorizationStrategy.java"] = [
    (
        "public interface AuthorizationStrategy {",
        "/**\n * 授权策略 SPI：对 {@link AuthorizationContext} 执行一次授权评估。\n */\npublic interface AuthorizationStrategy {",
    ),
    (
        "    void evaluate(AuthorizationContext context);",
        "    /** 评估并可能抛出 {@link org.apache.rocketmq.auth.authorization.exception.AuthorizationException}。 */\n    void evaluate(AuthorizationContext context);",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/strategy/StatefulAuthorizationStrategy.java"] = [
    (
        "public class StatefulAuthorizationStrategy extends AbstractAuthorizationStrategy {",
        "/**\n * 有状态授权策略：按 channelId、主体、资源、动作与来源 IP 缓存授权结果，减少重复校验。\n */\npublic class StatefulAuthorizationStrategy extends AbstractAuthorizationStrategy {",
    ),
    (
        "    protected Cache<String, Pair<Boolean, AuthorizationException>> authCache;",
        "    /** 缓存键为复合维度字符串，值为成功标志与异常。 */\n    protected Cache<String, Pair<Boolean, AuthorizationException>> authCache;",
    ),
    (
        "    public StatefulAuthorizationStrategy(AuthConfig authConfig, Supplier<?> metadataService) {",
        "    /** 按配置创建带过期与容量上限的 Caffeine 缓存。 */\n    public StatefulAuthorizationStrategy(AuthConfig authConfig, Supplier<?> metadataService) {",
    ),
    (
        "    @Override\n    public void evaluate(AuthorizationContext context) {",
        "    /** 无 channelId 时直接授权；否则命中缓存或执行后缓存结果。 */\n    @Override\n    public void evaluate(AuthorizationContext context) {",
    ),
    (
        "    private String buildKey(AuthorizationContext context) {",
        "    /** 构建缓存键：channelId#subject#resource#actions#sourceIp。 */\n    private String buildKey(AuthorizationContext context) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/strategy/StatelessAuthorizationStrategy.java"] = [
    (
        "public class StatelessAuthorizationStrategy extends AbstractAuthorizationStrategy {",
        "/**\n * 无状态授权策略：每次请求均完整执行 {@link #doEvaluate}，不做结果缓存。\n */\npublic class StatelessAuthorizationStrategy extends AbstractAuthorizationStrategy {",
    ),
    (
        "    public StatelessAuthorizationStrategy(AuthConfig authConfig, Supplier<?> metadataService) {",
        "    /** 委托基类初始化 Provider 与白名单。 */\n    public StatelessAuthorizationStrategy(AuthConfig authConfig, Supplier<?> metadataService) {",
    ),
    (
        "    @Override\n    public void evaluate(AuthorizationContext context) {",
        "    /** 直接调用基类授权逻辑。 */\n    @Override\n    public void evaluate(AuthorizationContext context) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/config/AuthConfig.java"] = [
    (
        "public class AuthConfig implements Cloneable {",
        "/**\n * RocketMQ 认证与授权统一配置：开关、Provider/Strategy 类名、白名单及各类缓存参数。\n */\npublic class AuthConfig implements Cloneable {",
    ),
    (
        "    private boolean authenticationEnabled = false;",
        "    /** 是否启用认证。 */\n    private boolean authenticationEnabled = false;",
    ),
    (
        "    private boolean authorizationEnabled = false;",
        "    /** 是否启用授权。 */\n    private boolean authorizationEnabled = false;",
    ),
    (
        "    private boolean migrateAuthFromV1Enabled = false;",
        "    /** 是否从 v1 Plain ACL 配置迁移至新认证授权模型。 */\n    private boolean migrateAuthFromV1Enabled = false;",
    ),
    (
        "    private int statefulAuthenticationCacheMaxNum = 10000;",
        "    /** 有状态认证缓存最大条目数。 */\n    private int statefulAuthenticationCacheMaxNum = 10000;",
    ),
    (
        "    private int statefulAuthorizationCacheMaxNum = 10000;",
        "    /** 有状态授权缓存最大条目数。 */\n    private int statefulAuthorizationCacheMaxNum = 10000;",
    ),
    (
        "    @Override\n    public AuthConfig clone() {",
        "    /** 浅拷贝当前配置实例。 */\n    @Override\n    public AuthConfig clone() {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/migration/AuthMigrator.java"] = [
    (
        "public class AuthMigrator {",
        "/**\n * ACL v1 至 v2 迁移器：读取 Plain ACL YAML，将 AccessKey 转为 {@link User} 并生成 {@link Acl}。\n */\npublic class AuthMigrator {",
    ),
    (
        "    public AuthMigrator(AuthConfig authConfig) {",
        "    /** 绑定配置并初始化 Plain 权限管理器与元数据 Manager。 */\n    public AuthMigrator(AuthConfig authConfig) {",
    ),
    (
        "    public void migrate() {",
        "    /** 迁移开关开启时遍历所有 PlainAccessConfig 并逐条迁移。 */\n    public void migrate() {",
    ),
    (
        "    private void doMigrate(PlainAccessConfig accessConfig) {",
        "    /** 用户已存在则跳过，否则创建用户与 ACL。 */\n    private void doMigrate(PlainAccessConfig accessConfig) {",
    ),
    (
        "    private CompletableFuture<Void> createUser(PlainAccessConfig accessConfig) {",
        "    /** 将 AccessKey/SecretKey 映射为 {@link User}，admin 标记转为 SUPER 类型。 */\n    private CompletableFuture<Void> createUser(PlainAccessConfig accessConfig) {",
    ),
    (
        "    private CompletableFuture<Void> createAcl(PlainAccessConfig config) {",
        "    /** 解析 topic/group 权限字符串，组装 CUSTOM 与 DEFAULT 策略并创建 ACL。 */\n    private CompletableFuture<Void> createAcl(PlainAccessConfig config) {",
    ),
    (
        "    private Decision parseDecision(String str) {",
        "    /** 将 v1 权限字符串解析为 {@link Decision}，空或 deny 为 DENY。 */\n    private Decision parseDecision(String str) {",
    ),
    (
        "    private List<Action> parseActions(String str) {",
        "    /** 将 pub/sub/deny 等 v1 权限码映射为 {@link Action} 列表。 */\n    private List<Action> parseActions(String str) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/migration/v1/AccessResource.java"] = [
    (
        "public interface AccessResource {",
        "/**\n * v1 ACL 访问资源标记接口，供 Plain 权限模型实现类继承。\n */\npublic interface AccessResource {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/migration/v1/AclConfig.java"] = [
    (
        "public class AclConfig {",
        "/**\n * v1 Plain ACL 聚合配置：全局白名单地址与账号列表。\n */\npublic class AclConfig {",
    ),
    (
        "    private List<String> globalWhiteAddrs;",
        "    /** 全局 IP 白名单地址列表。 */\n    private List<String> globalWhiteAddrs;",
    ),
    (
        "    private List<PlainAccessConfig> plainAccessConfigs;",
        "    /** Plain 账号及其 topic/group 权限配置。 */\n    private List<PlainAccessConfig> plainAccessConfigs;",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/migration/v1/PlainAccessConfig.java"] = [
    (
        "public class PlainAccessConfig  implements Serializable {",
        "/**\n * v1 Plain ACL 单账号配置：AccessKey、SecretKey、默认权限及 topic/group 细粒度权限。\n */\npublic class PlainAccessConfig  implements Serializable {",
    ),
    (
        "    private String accessKey;",
        "    /** 访问密钥标识，对应 v2 用户名。 */\n    private String accessKey;",
    ),
    (
        "    private String secretKey;",
        "    /** 访问密钥，对应 v2 用户密码。 */\n    private String secretKey;",
    ),
    (
        "    private boolean admin;",
        "    /** 是否为超级管理员账号。 */\n    private boolean admin;",
    ),
    (
        "    private List<String> topicPerms;",
        "    /** Topic 级权限列表，格式 topicName=perm。 */\n    private List<String> topicPerms;",
    ),
    (
        "    private List<String> groupPerms;",
        "    /** Consumer Group 级权限列表，格式 groupName=perm。 */\n    private List<String> groupPerms;",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/migration/v1/PlainAccessData.java"] = [
    (
        "public class PlainAccessData implements Serializable {",
        "/**\n * v1 Plain ACL YAML 根对象：全局白名单、账号列表与数据版本。\n */\npublic class PlainAccessData implements Serializable {",
    ),
    (
        "    private List<String> globalWhiteRemoteAddresses = new ArrayList<>();",
        "    /** 全局远程地址白名单。 */\n    private List<String> globalWhiteRemoteAddresses = new ArrayList<>();",
    ),
    (
        "    private List<PlainAccessConfig> accounts = new ArrayList<>();",
        "    /** Plain 账号配置列表。 */\n    private List<PlainAccessConfig> accounts = new ArrayList<>();",
    ),
    (
        "    public static class DataVersion implements Serializable {",
        "    /** ACL 配置数据版本：时间戳与递增计数器。 */\n    public static class DataVersion implements Serializable {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/migration/v1/PlainAccessResource.java"] = [
    (
        "public class PlainAccessResource implements AccessResource {",
        "/**\n * v1 Plain 访问资源：承载 AccessKey、资源权限映射及签名校验相关字段。\n */\npublic class PlainAccessResource implements AccessResource {",
    ),
    (
        "    // Identify the user",
        "    // 用户身份标识字段",
    ),
    (
        "    private Map<String, Byte> resourcePermMap;",
        "    /** 资源名到权限字节的映射表。 */\n    private Map<String, Byte> resourcePermMap;",
    ),
    (
        "    public static String getGroupFromRetryTopic(String retryTopic) {",
        "    /** 从重试 Topic 名解析 Consumer Group。 */\n    public static String getGroupFromRetryTopic(String retryTopic) {",
    ),
    (
        "    public static String getRetryTopic(String group) {",
        "    /** 由 Consumer Group 生成重试 Topic 名。 */\n    public static String getRetryTopic(String group) {",
    ),
    (
        "    public void addResourceAndPerm(String resource, byte perm) {",
        "    /** 向资源权限映射表添加一条记录。 */\n    public void addResourceAndPerm(String resource, byte perm) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/migration/v1/PlainPermissionManager.java"] = [
    (
        "public class PlainPermissionManager {",
        "/**\n * v1 Plain ACL 文件管理器：扫描 conf/acl 目录下 YAML 并聚合为 {@link AclConfig}。\n */\npublic class PlainPermissionManager {",
    ),
    (
        "    public PlainPermissionManager() {",
        "    /** 初始化默认 ACL 目录与 plain_acl.yml 路径并加载配置。 */\n    public PlainPermissionManager() {",
    ),
    (
        "    public List<String> getAllAclFiles(String path) {",
        "    /** 递归收集目录下所有 .yml/.yaml ACL 文件路径。 */\n    public List<String> getAllAclFiles(String path) {",
    ),
    (
        "    public void load() {",
        "    /** 确保默认 ACL 文件存在并刷新 fileList。 */\n    public void load() {",
    ),
    (
        "    /**\n     * Currently GlobalWhiteAddress is defined in {@link #defaultAclFile}, so make sure it exists.\n     */",
        "    /** 确保默认 plain_acl.yml 存在（全局白名单定义于此文件）。 */",
    ),
    (
        "    public AclConfig getAllAclConfig() {",
        "    /** 合并所有 YAML 中的账号与白名单，按 AccessKey 去重后返回。 */\n    public AclConfig getAllAclConfig() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/BrokerPathConfigHelper.java"] = [
    (
        "public class BrokerPathConfigHelper {",
        "/**\n * Broker 持久化配置文件路径辅助类：topic、消费位点、订阅组等 JSON 路径。\n */\npublic class BrokerPathConfigHelper {",
    ),
    (
        "    private static String brokerConfigPath = System.getProperty(\"user.home\") + File.separator + \"store\"",
        "    /** Broker 主配置文件路径，可通过 {@link #setBrokerConfigPath} 覆盖。 */\n    private static String brokerConfigPath = System.getProperty(\"user.home\") + File.separator + \"store\"",
    ),
    (
        "    public static String getTopicConfigPath(final String rootDir) {",
        "    /** 返回 topic 配置 JSON 路径。 */\n    public static String getTopicConfigPath(final String rootDir) {",
    ),
    (
        "    public static String getConsumerOffsetPath(final String rootDir) {",
        "    /** 返回消费位点持久化 JSON 路径。 */\n    public static String getConsumerOffsetPath(final String rootDir) {",
    ),
    (
        "    public static String getSubscriptionGroupPath(final String rootDir) {",
        "    /** 返回订阅组配置 JSON 路径。 */\n    public static String getSubscriptionGroupPath(final String rootDir) {",
    ),
    (
        "    private static String getConfigDir(final String rootDir) {",
        "    /** 返回 rootDir 下 config 子目录路径（含尾部分隔符）。 */\n    private static String getConfigDir(final String rootDir) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/BrokerPreOnlineService.java"] = [
    (
        "public class BrokerPreOnlineService extends ServiceThread {",
        "/**\n * Broker 上线前准备服务：隔离模式下等待 HA 握手完成并同步元数据后再对外提供服务。\n */\npublic class BrokerPreOnlineService extends ServiceThread {",
    ),
    (
        "    public BrokerPreOnlineService(BrokerController brokerController) {",
        "    /** 绑定 Broker 控制器。 */\n    public BrokerPreOnlineService(BrokerController brokerController) {",
    ),
    (
        "    @Override\n    public void run() {",
        "    /** 循环执行上线准备，非隔离或成功后退出。 */\n    @Override\n    public void run() {",
    ),
    (
        "    CompletableFuture<Boolean> waitForHaHandshakeComplete(String brokerAddr) {",
        "    /** 向 HA 服务注册握手完成通知并返回 Future。 */\n    CompletableFuture<Boolean> waitForHaHandshakeComplete(String brokerAddr) {",
    ),
    (
        "    private boolean prepareForMasterOnline(BrokerMemberGroup brokerMemberGroup) {",
        "    /** Master 按 brokerId 顺序等待各副本 HA 握手并反向同步元数据。 */\n    private boolean prepareForMasterOnline(BrokerMemberGroup brokerMemberGroup) {",
    ),
    (
        "    private boolean syncMetadataReverse(String brokerAddr) {",
        "    /** 从对端拉取较新的消费位点、延迟队列偏移与定时器检查点并持久化。 */\n    private boolean syncMetadataReverse(String brokerAddr) {",
    ),
    (
        "    private boolean prepareForSlaveOnline(BrokerMemberGroup brokerMemberGroup) {",
        "    /** Slave 获取 Master HA 地址，完成握手后启动服务。 */\n    private boolean prepareForSlaveOnline(BrokerMemberGroup brokerMemberGroup) {",
    ),
    (
        "    private boolean prepareForBrokerOnline() {",
        "    /** 同步 Broker 成员组并按 Master/Slave/无 Master 分支处理上线流程。 */\n    private boolean prepareForBrokerOnline() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/BrokerStartup.java"] = [
    (
        "public class BrokerStartup {",
        "/**\n * Broker 进程启动入口：解析命令行、加载配置、构建 {@link BrokerController} 并注册关闭钩子。\n */\npublic class BrokerStartup {",
    ),
    (
        "    public static void main(String[] args) {",
        "    /** 创建控制器并启动 Broker。 */\n    public static void main(String[] args) {",
    ),
    (
        "    public static BrokerController start(BrokerController controller) {",
        "    /** 启动控制器并打印 boot success 提示。 */\n    public static BrokerController start(BrokerController controller) {",
    ),
    (
        "    public static ConfigContext parseCmdLine(String[] args) throws Exception {",
        "    /** 解析 -c/-p/-m 等命令行选项并填充 {@link ConfigContext}。 */\n    public static ConfigContext parseCmdLine(String[] args) throws Exception {",
    ),
    (
        "    public static ConfigContext configFileToConfigContext(String filePath) throws Exception {",
        "    /** 从 properties 文件加载 Broker/Netty/Store/Auth 配置。 */\n    public static ConfigContext configFileToConfigContext(String filePath) throws Exception {",
    ),
    (
        "    public static BrokerController buildBrokerController(ConfigContext configContext) {",
        "    /** 校验环境、设置 HA 端口与 Broker 角色，创建并初始化 {@link BrokerController}。 */\n    public static BrokerController buildBrokerController(ConfigContext configContext) {",
    ),
    (
        "        // Set broker role according to ha config",
        "        // 根据 HA 角色（ASYNC/SYNC_MASTER 或 SLAVE）设置 brokerId",
    ),
    (
        "    public static BrokerController createBrokerController(String[] args) {",
        "    /** 完整创建流程：解析参数、构建控制器、initialize 并注册 shutdown hook。 */\n    public static BrokerController createBrokerController(String[] args) {",
    ),
    (
        "    public static class SystemConfigFileHelper {",
        "    /** Broker 配置文件加载辅助类。 */\n    public static class SystemConfigFileHelper {",
    ),
    (
        "        public Properties loadConfig() throws Exception {",
        "        /** 从已设置的 file 路径读取 properties。 */\n        public Properties loadConfig() throws Exception {",
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
    index_file = Path("/tmp/git-index-rocketmq-w2b")
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


def apply_all() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)


def push_with_recommit(
    message: str,
    paths: list[str],
    *,
    reapply: bool = False,
    retries: int = 6,
) -> tuple[str, int]:
    sha = ""
    tree_count = 0
    for attempt in range(retries):
        subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
        subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "origin/main"], check=True)
        if reapply:
            apply_all()
        sha, tree_count = isolated_index_commit(message, paths, base_ref="origin/main")
        r = subprocess.run(
            ["git", "-C", str(ROOT), "push", "-u", "origin", "main"],
            capture_output=True,
            text=True,
        )
        if r.returncode == 0:
            return sha, tree_count
        if attempt + 1 < retries:
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
    subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "origin/main"], check=True)
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
    sha, tree_count = push_with_recommit(
        "rocketmq rocketmq-all-5.5.0: Chinese-annotate wave 2b [15:30]",
        [*analyzed_paths, script_path],
        reapply=True,
    )

    done_set = set(
        ln.strip()
        for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines()
        if ln.strip()
    )
    if not all(rel in done_set for rel in BATCH_LIST):
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
        queue_sha, _ = push_with_recommit(
            f"queue: mark rocketmq rocketmq-all-5.5.0 {MARK_NOTE} done",
            queue_paths,
        )
    else:
        queue_sha = sha

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
