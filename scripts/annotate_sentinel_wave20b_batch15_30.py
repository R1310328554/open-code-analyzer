#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-20b block [15:30] (dashboard cluster domain)."""
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
BATCH_LIST = Path("/tmp/sentinel_w20b.txt").read_text(encoding="utf-8").strip().split("\n")
SCRIPT_NAME = "annotate_sentinel_wave20b_batch15_30.py"
MARK_NOTE = "wave20b"

GUARD_FILES = [
    VER
    / "analyzed/sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/connection/ScanIdleConnectionTask.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/ClusterAppFullAssignRequest.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 应用级集群令牌服务端全量分配请求体。\n * <p>{@link #clusterMap} 为服务端机器与客户端/命名空间的映射列表；\n * {@link #remainingList} 为本次未参与分配的机器 ID 集合。</p>\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "    private List<ClusterAppAssignMap> clusterMap;",
        "    /** 集群分配映射列表，每项对应一台令牌服务端机器。 */\n    private List<ClusterAppAssignMap> clusterMap;",
    ),
    (
        "    private Set<String> remainingList;",
        "    /** 未分配机器 ID 集合。 */\n    private Set<String> remainingList;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/ClusterAppSingleServerAssignRequest.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 应用级单台集群令牌服务端分配请求体。\n * <p>{@link #clusterMap} 描述目标服务端机器及其客户端绑定；\n * {@link #remainingList} 记录其余未分配机器。</p>\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "    private ClusterAppAssignMap clusterMap;",
        "    /** 单台令牌服务端的分配映射。 */\n    private ClusterAppAssignMap clusterMap;",
    ),
    (
        "    private Set<String> remainingList;",
        "    /** 未分配机器 ID 集合。 */\n    private Set<String> remainingList;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/ClusterClientInfoVO.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 集群令牌客户端信息视图，展示所连服务端地址、客户端运行状态与超时配置。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "    private String serverHost;",
        "    /** 所连集群令牌服务端主机名或 IP。 */\n    private String serverHost;",
    ),
    (
        "    private Integer serverPort;",
        "    /** 所连集群令牌服务端端口。 */\n    private Integer serverPort;",
    ),
    (
        "    private Integer clientState;",
        "    /** 客户端运行状态码。 */\n    private Integer clientState;",
    ),
    (
        "    private Integer requestTimeout;",
        "    /** 令牌请求超时（毫秒）。 */\n    private Integer requestTimeout;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/ClusterGroupEntity.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 集群分组实体，描述一台令牌服务端机器及其关联的客户端集合。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "    private String machineId;",
        "    /** 机器唯一标识。 */\n    private String machineId;",
    ),
    (
        "    private Set<String> clientSet = new HashSet<>();",
        "    /** 绑定到该服务端的客户端机器 ID 集合。 */\n    private Set<String> clientSet = new HashSet<>();",
    ),
    (
        "    private Boolean belongToApp;",
        "    /** 该服务端机器是否归属当前应用。 */\n    private Boolean belongToApp;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/ClusterStateSingleVO.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 单台机器的集群状态视图，含地址、运行模式与关联目标。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "    private String address;",
        "    /** 机器地址，通常为 {@code ip:port}。 */\n    private String address;",
    ),
    (
        "    private Integer mode;",
        "    /** 集群运行模式（客户端/服务端/未启用等）。 */\n    private Integer mode;",
    ),
    (
        "    private String target;",
        "    /** 客户端模式下所连服务端地址，或服务端模式下的监听信息。 */\n    private String target;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/ConnectionDescriptorVO.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 连接描述视图，以 address 与 host 标识一条客户端连接。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    private String address;",
        "    /** 连接地址，通常为 {@code ip:port}。 */\n    private String address;",
    ),
    (
        "    private String host;",
        "    /** 连接主机名或 IP。 */\n    private String host;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/ConnectionGroupVO.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 命名空间维度的连接分组视图，汇总该命名空间下的连接列表与已连接数量。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    private String namespace;",
        "    /** 命名空间名称。 */\n    private String namespace;",
    ),
    (
        "    private List<ConnectionDescriptorVO> connectionSet;",
        "    /** 该命名空间下的连接描述列表。 */\n    private List<ConnectionDescriptorVO> connectionSet;",
    ),
    (
        "    private Integer connectedCount;",
        "    /** 当前已建立的连接数量。 */\n    private Integer connectedCount;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/config/ClusterClientConfig.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群令牌客户端配置，定义所连服务端地址与请求/连接超时。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    private String serverHost;",
        "    /** 集群令牌服务端主机名或 IP。 */\n    private String serverHost;",
    ),
    (
        "    private Integer serverPort;",
        "    /** 集群令牌服务端端口。 */\n    private Integer serverPort;",
    ),
    (
        "    private Integer requestTimeout;",
        "    /** 令牌请求超时（毫秒）。 */\n    private Integer requestTimeout;",
    ),
    (
        "    private Integer connectTimeout;",
        "    /** 与服务端建立连接的超时（毫秒）。 */\n    private Integer connectTimeout;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/config/ServerFlowConfig.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群令牌服务端流控配置，定义命名空间级 QPS 上限、滑动窗口参数与预占用比例。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    public static final double DEFAULT_EXCEED_COUNT = 1.0d;",
        "    /** 默认超出计数阈值。 */\n    public static final double DEFAULT_EXCEED_COUNT = 1.0d;",
    ),
    (
        "    public static final double DEFAULT_MAX_OCCUPY_RATIO = 1.0d;",
        "    /** 默认最大预占用比例。 */\n    public static final double DEFAULT_MAX_OCCUPY_RATIO = 1.0d;",
    ),
    (
        "    public static final int DEFAULT_INTERVAL_MS = 1000;",
        "    /** 默认统计窗口间隔（毫秒）。 */\n    public static final int DEFAULT_INTERVAL_MS = 1000;",
    ),
    (
        "    public static final int DEFAULT_SAMPLE_COUNT= 10;",
        "    /** 默认滑动窗口桶数量。 */\n    public static final int DEFAULT_SAMPLE_COUNT= 10;",
    ),
    (
        "    public static final double DEFAULT_MAX_ALLOWED_QPS= 30000;",
        "    /** 默认允许的最大 QPS。 */\n    public static final double DEFAULT_MAX_ALLOWED_QPS= 30000;",
    ),
    (
        "    private final String namespace;",
        "    /** 配置所属命名空间，构造时固定不可变。 */\n    private final String namespace;",
    ),
    (
        "    private Double maxAllowedQps = DEFAULT_MAX_ALLOWED_QPS;",
        "    /** 该命名空间允许的最大 QPS。 */\n    private Double maxAllowedQps = DEFAULT_MAX_ALLOWED_QPS;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/config/ServerTransportConfig.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群令牌服务端传输配置，定义 Netty 监听端口与连接空闲超时。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    public static final int DEFAULT_PORT = 18730;",
        "    /** 默认监听端口。 */\n    public static final int DEFAULT_PORT = 18730;",
    ),
    (
        "    public static final int DEFAULT_IDLE_SECONDS = 600;",
        "    /** 默认连接空闲超时（秒）。 */\n    public static final int DEFAULT_IDLE_SECONDS = 600;",
    ),
    (
        "    private Integer port;",
        "    /** 服务端监听端口。 */\n    private Integer port;",
    ),
    (
        "    private Integer idleSeconds;",
        "    /** 连接空闲超过该秒数将被关闭。 */\n    private Integer idleSeconds;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/request/ClusterAppAssignMap.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 集群分配映射条目，描述一台令牌服务端机器及其客户端与命名空间绑定。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "    private String machineId;",
        "    /** 令牌服务端机器 ID。 */\n    private String machineId;",
    ),
    (
        "    private Set<String> clientSet;",
        "    /** 绑定到该服务端的客户端机器 ID 集合。 */\n    private Set<String> clientSet;",
    ),
    (
        "    private Set<String> namespaceSet;",
        "    /** 该服务端负责的命名空间集合。 */\n    private Set<String> namespaceSet;",
    ),
    (
        "    private Boolean belongToApp;",
        "    /** 该机器是否归属当前应用。 */\n    private Boolean belongToApp;",
    ),
    (
        "    private Double maxAllowedQps;",
        "    /** 该服务端机器允许的最大 QPS。 */\n    private Double maxAllowedQps;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/request/ClusterClientModifyRequest.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群客户端配置修改请求，实现 {@link ClusterModifyRequest} 并携带 {@link ClusterClientConfig}。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    private Integer mode;",
        "    /** 集群运行模式。 */\n    private Integer mode;",
    ),
    (
        "    private ClusterClientConfig clientConfig;",
        "    /** 待下发的客户端配置。 */\n    private ClusterClientConfig clientConfig;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/request/ClusterModifyRequest.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群配置修改请求通用接口，提供目标机器标识与运行模式访问器。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    String getApp();",
        "    /** @return 目标应用名 */\n    String getApp();",
    ),
    (
        "    String getIp();",
        "    /** @return 目标机器 IP */\n    String getIp();",
    ),
    (
        "    Integer getPort();",
        "    /** @return 目标机器端口 */\n    Integer getPort();",
    ),
    (
        "    Integer getMode();",
        "    /** @return 集群运行模式 */\n    Integer getMode();",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/request/ClusterServerModifyRequest.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群令牌服务端配置修改请求，含流控、传输与命名空间配置。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    private ServerFlowConfig flowConfig;",
        "    /** 服务端流控配置。 */\n    private ServerFlowConfig flowConfig;",
    ),
    (
        "    private ServerTransportConfig transportConfig;",
        "    /** 服务端传输配置（端口与空闲超时）。 */\n    private ServerTransportConfig transportConfig;",
    ),
    (
        "    private Set<String> namespaceSet;",
        "    /** 该服务端负责的命名空间集合。 */\n    private Set<String> namespaceSet;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/state/AppClusterClientStateWrapVO.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 应用维度集群客户端状态包装视图，聚合机器标识与 {@link ClusterClientStateVO} 详情。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "    /**\n     * {ip}@{transport_command_port}.\n     */",
        "    /** 机器唯一标识，格式为 {@code ip@transport_command_port}。 */\n",
    ),
    (
        "    private Integer commandPort;",
        "    /** Sentinel 命令端口（transport command port）。 */\n    private Integer commandPort;",
    ),
    (
        "    private ClusterClientStateVO state;",
        "    /** 集群客户端运行状态详情。 */\n    private ClusterClientStateVO state;",
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
    index_file = Path("/tmp/git-index-sentinel-w20b")
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
        "sentinel 1.8.10: Chinese-annotate wave 20b [15:30]",
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
    update_batch_json()
    queue_paths = [
        "sentinel/1.8.10/_reports/class-queue/done.txt",
        "sentinel/1.8.10/_reports/class-queue/pending.txt",
        "sentinel/1.8.10/_reports/class-queue/batch.json",
        "sentinel/1.8.10/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark sentinel 1.8.10 wave20b done",
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
