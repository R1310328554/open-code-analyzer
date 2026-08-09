#!/usr/bin/env python3
"""Chinese-annotate Redisson 4.7.0 wave-1a helidon + hibernate-4 [0:15]."""
from __future__ import annotations

import json
import os
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "redisson/redisson-4.7.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
SCRIPTS = ROOT / "scripts"
WAVE1A_FILE = Path("/tmp/redisson_w1a.txt")
SCRIPT_NAME = "annotate_redisson_wave1a_batch0_15.py"
MARK_NOTE = "wave1a [0:15]"
BATCH_FILES = [
    ln.strip() for ln in WAVE1A_FILE.read_text(encoding="utf-8").splitlines() if ln.strip()
]

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["RedissonExtension.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n * Helidon CDI 扩展：收集 {@link RedissonClient} 注入点上的限定符，\n"
        " * 在 {@link AfterBeanDiscovery} 阶段为每个限定符注册对应的生产者 Bean。\n"
        " * <p>配置从 MicroProfile {@link Config} 读取，键前缀为 "
        "{@code org.redisson.Redisson.<instanceName>.}。</p>\n *\n * @author Nikita Koksharov\n *\n */",
    ),
    (
        "    private <T extends RedissonClient> void processRedissonInjectionPoint(@Observes ProcessInjectionPoint<?, T> point) {",
        "    /** 收集所有 {@link RedissonClient} 注入点使用的 CDI 限定符。 */\n"
        "    private <T extends RedissonClient> void processRedissonInjectionPoint(@Observes ProcessInjectionPoint<?, T> point) {",
    ),
    (
        "    private void addBeans(@Observes AfterBeanDiscovery discovery, BeanManager beanManager) {",
        "    /** 为每个已收集的限定符注册 {@link ApplicationScoped} 范围的 {@link RedissonClient} 生产者 Bean。 */\n"
        "    private void addBeans(@Observes AfterBeanDiscovery discovery, BeanManager beanManager) {",
    ),
]

R["RedissonRegionFactory.java"] = [
    (
        "/**\n * Hibernate Cache region factory based on Redisson. \n"
        " * Creates own Redisson instance during region start.\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n * 基于 Redisson 的 Hibernate 二级缓存 {@link RegionFactory} 实现。\n"
        " * 在 Region 启动时创建并持有独立的 {@link RedissonClient} 实例。\n *\n * @author Nikita Koksharov\n *\n */",
    ),
    (
        "    public static final String QUERY_DEF = \"query\";",
        "    /** 查询结果缓存区域的默认配置键后缀。 */\n    public static final String QUERY_DEF = \"query\";",
    ),
    (
        "    public static final String COLLECTION_DEF = \"collection\";",
        "    /** 集合缓存区域的默认配置键后缀。 */\n    public static final String COLLECTION_DEF = \"collection\";",
    ),
    (
        "    public static final String ENTITY_DEF = \"entity\";",
        "    /** 实体缓存区域的默认配置键后缀。 */\n    public static final String ENTITY_DEF = \"entity\";",
    ),
    (
        "    public static final String NATURAL_ID_DEF = \"naturalid\";",
        "    /** 自然 ID 缓存区域的默认配置键后缀。 */\n    public static final String NATURAL_ID_DEF = \"naturalid\";",
    ),
    (
        "    public static final String TIMESTAMPS_DEF = \"timestamps\";",
        "    /** 时间戳缓存区域的默认配置键后缀。 */\n    public static final String TIMESTAMPS_DEF = \"timestamps\";",
    ),
    (
        "    public static final String CONFIG_PREFIX = \"hibernate.cache.redisson.\";",
        "    /** Hibernate 属性中 Redisson 相关配置的前缀。 */\n    public static final String CONFIG_PREFIX = \"hibernate.cache.redisson.\";",
    ),
    (
        "    public static final String REDISSON_CONFIG_PATH = CONFIG_PREFIX + \"config\";",
        "    /** Redisson 配置文件路径对应的 Hibernate 属性键。 */\n    public static final String REDISSON_CONFIG_PATH = CONFIG_PREFIX + \"config\";",
    ),
    (
        "    public static final String FALLBACK = CONFIG_PREFIX + \"fallback\";",
        "    /** 是否在 Redis 不可用时启用本地降级模式的属性键。 */\n    public static final String FALLBACK = CONFIG_PREFIX + \"fallback\";",
    ),
    (
        "    @Override\n    public void start(Settings settings, Properties properties) throws CacheException {",
        "    /** 加载 Redisson 配置并初始化客户端，解析 fallback 开关。 */\n"
        "    @Override\n    public void start(Settings settings, Properties properties) throws CacheException {",
    ),
    (
        "    protected RedissonClient createRedissonClient(Properties properties) {",
        "    /** 从类路径或指定路径加载 YAML/JSON 配置并创建 {@link RedissonClient}。 */\n"
        "    protected RedissonClient createRedissonClient(Properties properties) {",
    ),
    (
        "    @Override\n    public void stop() {",
        "    /** 关闭 Redisson 客户端并释放连接。 */\n    @Override\n    public void stop() {",
    ),
    (
        "    @Override\n    public boolean isMinimalPutsEnabledByDefault() {",
        "    /** 默认启用最小化 put 策略以减少缓存写入。 */\n    @Override\n    public boolean isMinimalPutsEnabledByDefault() {",
    ),
    (
        "    @Override\n    public AccessType getDefaultAccessType() {",
        "    /** 默认缓存并发访问策略为 {@link AccessType#TRANSACTIONAL}。 */\n    @Override\n    public AccessType getDefaultAccessType() {",
    ),
    (
        "    @Override\n    public long nextTimestamp() {",
        "    /** 通过 Redis Lua 脚本生成全局递增时间戳；失败且启用 fallback 时使用本地 CAS 递增。 */\n"
        "    @Override\n    public long nextTimestamp() {",
    ),
    (
        "    protected RMapCache<Object, Object> getCache(String regionName, Properties properties, String defaultKey) {",
        "    /** 获取指定 Region 名称对应的 {@link RMapCache} 实例。 */\n"
        "    protected RMapCache<Object, Object> getCache(String regionName, Properties properties, String defaultKey) {",
    ),
]

R["JndiRedissonRegionFactory.java"] = [
    (
        "/**\n * Hibernate Cache region factory based on Redisson. \n"
        " * Uses Redisson instance located in JNDI.\n * \n * @author Nikita Koksharov \n *\n */",
        "/**\n * 基于 Redisson 的 Hibernate 二级缓存 {@link RegionFactory} 实现。\n"
        " * 通过 JNDI 查找已部署的 {@link RedissonClient}，而非自行创建实例。\n *\n * @author Nikita Koksharov\n *\n */",
    ),
    (
        "    public static final String JNDI_NAME = CONFIG_PREFIX + \"jndi_name\";",
        "    /** JNDI 中 {@link RedissonClient} 绑定名的 Hibernate 属性键。 */\n"
        "    public static final String JNDI_NAME = CONFIG_PREFIX + \"jndi_name\";",
    ),
    (
        "    @Override\n    protected RedissonClient createRedissonClient(Properties properties) {",
        "    /** 从 JNDI 查找 {@link RedissonClient}；未配置 {@link #JNDI_NAME} 时抛出 {@link CacheException}。 */\n"
        "    @Override\n    protected RedissonClient createRedissonClient(Properties properties) {",
    ),
    (
        "    @Override\n    public void stop() {",
        "    /** JNDI 模式下不关闭外部管理的 Redisson 实例。 */\n    @Override\n    public void stop() {",
    ),
]

R["JndiRedissonRegionNativeFactory.java"] = [
    (
        "/**\n * Hibernate Cache region factory based on Redisson. \n"
        " * Uses Redisson instance located in JNDI.\n * \n * @author Nikita Koksharov \n *\n */",
        "/**\n * 基于 Redisson 原生 Map 缓存的 Hibernate Region 工厂。\n"
        " * 通过 JNDI 查找已部署的 {@link RedissonClient}，配合 {@link RedissonRegionNativeFactory} 使用原生存储。\n *\n * @author Nikita Koksharov\n *\n */",
    ),
    (
        "    public static final String JNDI_NAME = CONFIG_PREFIX + \"jndi_name\";",
        "    /** JNDI 中 {@link RedissonClient} 绑定名的 Hibernate 属性键。 */\n"
        "    public static final String JNDI_NAME = CONFIG_PREFIX + \"jndi_name\";",
    ),
    (
        "    @Override\n    protected RedissonClient createRedissonClient(Properties properties) {",
        "    /** 从 JNDI 查找 {@link RedissonClient}；未配置 {@link #JNDI_NAME} 时抛出 {@link CacheException}。 */\n"
        "    @Override\n    protected RedissonClient createRedissonClient(Properties properties) {",
    ),
    (
        "    @Override\n    public void stop() {",
        "    /** JNDI 模式下不关闭外部管理的 Redisson 实例。 */\n    @Override\n    public void stop() {",
    ),
]

R["RedissonRegionNativeFactory.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n * 使用 Redisson 原生 Map 缓存（{@link RMapCacheNative}）的 Region 工厂。\n"
        " * 启动前校验 eviction 与 max_idle 配置必须为 0。\n *\n * @author Nikita Koksharov\n *\n */",
    ),
    (
        "    @Override\n    public void start(Settings settings, Properties properties) throws CacheException {",
        "    /** 校验原生模式下不允许非零的 max_entries 与 max_idle_time，再调用父类启动逻辑。 */\n"
        "    @Override\n    public void start(Settings settings, Properties properties) throws CacheException {",
    ),
    (
        "    @Override\n    protected RMapCache<Object, Object> getCache(String regionName, Properties properties, String defaultKey) {",
        "    /** 返回包装后的 {@link RMapCacheNative} 实例作为 Region 底层存储。 */\n"
        "    @Override\n    protected RMapCache<Object, Object> getCache(String regionName, Properties properties, String defaultKey) {",
    ),
]

R["RedissonStrategyRegistrationProvider.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n * Hibernate 启动时注册 Redisson {@link RegionFactory} 策略的 SPI 提供者。\n"
        " * 允许在配置中使用 {@code redisson} 短名或完整类名。\n *\n * @author Nikita Koksharov\n *\n */",
    ),
    (
        "    @Override\n    public Iterable<StrategyRegistration> getStrategyRegistrations() {",
        "    /** 向 Hibernate 注册 {@link RedissonRegionFactory} 作为 {@link RegionFactory} 实现。 */\n"
        "    @Override\n    public Iterable<StrategyRegistration> getStrategyRegistrations() {",
    ),
]

R["BaseRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n * Redisson 缓存 Region 的抽象基类，实现 {@link TransactionalDataRegion} 与 {@link GeneralDataRegion}。\n"
        " * 封装 {@link RMapCache} 的读写、驱逐、TTL 及 Redis 不可用时的 fallback 降级逻辑。\n *\n * @author Nikita Koksharov\n *\n */",
    ),
    (
        "    public BaseRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager, RegionFactory regionFactory, CacheDataDescription metadata, Properties properties, String defaultKey) {",
        "    /** 根据 Hibernate 属性初始化 TTL、maxIdle、maxSize 及 fallback 模式。 */\n"
        "    public BaseRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager, RegionFactory regionFactory, CacheDataDescription metadata, Properties properties, String defaultKey) {",
    ),
    (
        "    private void ping() {",
        "    /** 异步探测 Redis 连通性；恢复后退出 fallback 模式，否则继续定时重试。 */\n    private void ping() {",
    ),
    (
        "        // TODO Auto-generated method stub",
        "        // 当前实现不参与 Hibernate 事务同步",
    ),
    (
        "        // 60 seconds (normalized value)",
        "        // 60 秒（Hibernate 规范化后的超时值）",
    ),
    (
        "    @Override\n    public Object get(Object key) throws CacheException {",
        "    /** 从缓存读取条目；fallback 模式下返回 null 而不访问 Redis。 */\n    @Override\n    public Object get(Object key) throws CacheException {",
    ),
    (
        "    @Override\n    public void put(Object key, Object value) throws CacheException {",
        "    /** 写入缓存条目，应用 Region 配置的 TTL 与 maxIdle。 */\n    @Override\n    public void put(Object key, Object value) throws CacheException {",
    ),
    (
        "    @Override\n    public void evict(Object key) throws CacheException {",
        "    /** 移除指定键的缓存条目。 */\n    @Override\n    public void evict(Object key) throws CacheException {",
    ),
    (
        "    @Override\n    public void evictAll() throws CacheException {",
        "    /** 清空整个 Region 缓存。 */\n    @Override\n    public void evictAll() throws CacheException {",
    ),
]

R["RedissonCollectionRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n * Hibernate 集合（Collection）二级缓存 Region，基于 Redisson {@link RMapCache}。\n *\n * @author Nikita Koksharov\n *\n */",
    ),
    (
        "    @Override\n    public CollectionRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {",
        "    /** 按 {@link AccessType} 构建集合 Region 的并发访问策略。 */\n"
        "    @Override\n    public CollectionRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {",
    ),
]

R["RedissonEntityRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n * Hibernate 实体二级缓存 Region，基于 Redisson {@link RMapCache}。\n *\n * @author Nikita Koksharov\n *\n */",
    ),
    (
        "    @Override\n    public EntityRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {",
        "    /** 按 {@link AccessType} 构建实体 Region 的并发访问策略。 */\n"
        "    @Override\n    public EntityRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {",
    ),
]

R["RedissonNaturalIdRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n * Hibernate 自然 ID（NaturalId）二级缓存 Region，基于 Redisson {@link RMapCache}。\n *\n * @author Nikita Koksharov\n *\n */",
    ),
    (
        "    @Override\n    public NaturalIdRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {",
        "    /** 按 {@link AccessType} 构建自然 ID Region 的并发访问策略。 */\n"
        "    @Override\n    public NaturalIdRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {",
    ),
]

R["RedissonQueryRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n * Hibernate 查询结果二级缓存 Region，基于 Redisson {@link RMapCache}。\n *\n * @author Nikita Koksharov\n *\n */",
    ),
]

R["RedissonTimestampsRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n * Hibernate 查询失效时间戳 Region，基于 Redisson {@link RMapCache}。\n"
        " * 用于跟踪表/空间更新时间以支持查询缓存失效。\n *\n * @author Nikita Koksharov\n *\n */",
    ),
    (
        "    public RedissonTimestampsRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager,\n"
        "            RegionFactory regionFactory, Properties properties, String defaultKey) {",
        "    /** 构造时间戳 Region，metadata 为 null（非实体类缓存）。 */\n"
        "    public RedissonTimestampsRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager,\n"
        "            RegionFactory regionFactory, Properties properties, String defaultKey) {",
    ),
]

R["AbstractReadWriteAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n * 读写（READ_WRITE）缓存并发访问策略的抽象基类。\n"
        " * 委托 {@link GeneralDataRegion} 完成 get/put，解锁时驱逐条目。\n *\n * @author Nikita Koksharov\n *\n */",
    ),
    (
        "    @Override\n    public Object get(Object key, long txTimestamp) throws CacheException {",
        "    /** 从 Region 读取缓存条目（不校验版本）。 */\n    @Override\n    public Object get(Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "    @Override\n    public boolean putFromLoad(Object key, Object value, long txTimestamp, Object version, boolean minimalPutOverride)",
        "    /** 加载后写入缓存并始终返回 true。 */\n    @Override\n    public boolean putFromLoad(Object key, Object value, long txTimestamp, Object version, boolean minimalPutOverride)",
    ),
    (
        "    @Override\n    public SoftLock lockItem(Object key, Object version) throws CacheException {",
        "    /** 当前实现不使用软锁，直接返回 null。 */\n    @Override\n    public SoftLock lockItem(Object key, Object version) throws CacheException {",
    ),
    (
        "    @Override\n    public void unlockItem(Object key, SoftLock lock) throws CacheException {",
        "    /** 解锁时驱逐对应缓存条目。 */\n    @Override\n    public void unlockItem(Object key, SoftLock lock) throws CacheException {",
    ),
]


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def tree_guard(env: dict[str, str] | None = None) -> int:
    tracked = len(
        subprocess.check_output(["git", "-C", str(ROOT), "ls-files"], env=env).splitlines()
    )
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


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:160]}...")
        text = text.replace(old, new, 1)
    return text


def annotate_files() -> tuple[int, list[str]]:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        dst = ANALYZED / rel
        if not dst.exists():
            src = ORIGINAL / rel
            if not src.exists():
                failures.append(f"MISSING original: {rel}")
                continue
            dst.parent.mkdir(parents=True, exist_ok=True)
            dst.write_text(src.read_text(encoding="utf-8"), encoding="utf-8")
        reps = R.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            text = apply_replacements(dst.read_text(encoding="utf-8"), reps)
            cn = len(re.findall(r"[\u4e00-\u9fff]", text))
            lic = "Licensed under the Apache License" in text
            if cn < 10 or not lic:
                failures.append(f"VALIDATION cn={cn} lic={lic}: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK cn={cn} {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    return ok, failures


def isolated_index_commit(
    message: str, paths: list[str], base_ref: str = "origin/main"
) -> tuple[str, int]:
    index_file = Path("/tmp/git-index-redisson-w1a")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", base], env=env, check=True)
    subprocess.run(["git", "-C", str(ROOT), "add", "--", *paths], env=env, check=True)
    tree_count = tree_guard(env)
    tree = subprocess.check_output(
        ["git", "-C", str(ROOT), "write-tree"], env=env, text=True
    ).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True)
    index_file.unlink(missing_ok=True)
    return commit, tree_count


def confirm_chinese() -> dict[str, bool]:
    return {
        rel: has_chinese((ANALYZED / rel).read_text(encoding="utf-8")) for rel in BATCH_FILES
    }


def main() -> int:
    ok, failures = annotate_files()
    if failures or ok != len(BATCH_FILES):
        print(
            json.dumps(
                {"ok": ok, "failures": failures, "tree_count": 0},
                ensure_ascii=False,
                indent=2,
            )
        )
        return 1

    analyzed_paths = [f"redisson/redisson-4.7.0/analyzed/{rel}" for rel in BATCH_FILES]
    script_paths = [f"scripts/{SCRIPT_NAME}"]
    sha, tree_count = isolated_index_commit(
        "redisson redisson-4.7.0: Chinese-annotate wave 1a [0:15]",
        [*analyzed_paths, *script_paths],
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "-u", "origin", "main"], check=True)

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "redisson",
            "--version",
            "redisson-4.7.0",
            "--note",
            MARK_NOTE,
            *BATCH_FILES,
        ],
        check=True,
    )
    queue_paths = [
        "redisson/redisson-4.7.0/_reports/class-queue/done.txt",
        "redisson/redisson-4.7.0/_reports/class-queue/pending.txt",
        "redisson/redisson-4.7.0/_reports/class-queue/batch.json",
        "redisson/redisson-4.7.0/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        f"queue: mark redisson redisson-4.7.0 {MARK_NOTE} done",
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
