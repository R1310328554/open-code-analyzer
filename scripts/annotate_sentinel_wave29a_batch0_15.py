#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-29a block [0:15] (redis/spring-cloud-config/zookeeper/metric-exporter/param-flow)."""
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
    for ln in Path("/tmp/sentinel_w29a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_sentinel_wave29a_batch0_15.py"
MARK_NOTE = "wave29a [0:15]"

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-extension/sentinel-datasource-redis/src/main/java/com/alibaba/csp/sentinel/datasource/redis/config/RedisHostAndPort.java"] = [
    (
        "/**\n * An immutable representation of a host and port.\n *\n * @author tiger\n */",
        "/**\n * 主机与端口的不可变值对象，用于 Redis 连接地址解析。\n *\n * @author tiger\n */",
    ),
    (
        "    /**\n     * @param host must not be empty or {@literal null}.\n     * @param port\n     */",
        "    /**\n     * @param host must not be empty or {@literal null}.\n     * @param port 端口号，{@link #NO_PORT} 表示未指定\n     */",
    ),
    (
        "    /**\n     * Create a {@link RedisHostAndPort} of {@code host} and {@code port}\n     *\n     * @param host the hostname\n     * @param port a valid port\n     * @return the {@link RedisHostAndPort} of {@code host} and {@code port}\n     */",
        "    /**\n     * 根据 {@code host} 与 {@code port} 创建 {@link RedisHostAndPort} 实例。\n     *\n     * @param host the hostname\n     * @param port a valid port\n     * @return the {@link RedisHostAndPort} of {@code host} and {@code port}\n     */",
    ),
    (
        "    /**\n     * @return {@literal true} if has a port.\n     */",
        "    /**\n     * @return 是否已指定有效端口号。\n     */",
    ),
    (
        "    /**\n     * @return the host text.\n     */",
        "    /**\n     * @return 主机名文本。\n     */",
    ),
    (
        "    /**\n     * @return the port.\n     */",
        "    /**\n     * @return 端口号；未指定时抛出 {@link IllegalStateException}。\n     */",
    ),
    (
        "    /**\n     * @param port the port number\n     * @return {@literal true} for valid port numbers.\n     */",
        "    /**\n     * @param port the port number\n     * @return 端口是否在 0–65535 合法范围内。\n     */",
    ),
]

R["sentinel-extension/sentinel-datasource-spring-cloud-config/src/main/java/com/alibaba/csp/sentinel/datasource/spring/cloud/config/SentinelRuleLocator.java"] = [
    (
        "/**\n * <p>\n * {@link SentinelRuleLocator} which pulls Sentinel rules from remote server.\n * It retrieves configurations of spring-cloud-config client configurations from\n * {@link org.springframework.core.env.Environment}, such as {@code spring.cloud.config.uri=uri},\n * {@code spring.cloud.config.profile=profile}, and so on.\n * When rules are pulled successfully, it will be stored to {@link SentinelRuleStorage}.\n * </p>\n *\n * @author lianglin\n * @since 1.7.0\n */",
        "/**\n * <p>\n * 从 Spring Cloud Config 远程服务器拉取 Sentinel 规则的 {@link PropertySourceLocator}。\n * 从 {@link org.springframework.core.env.Environment} 读取客户端配置（如 {@code spring.cloud.config.uri}、\n * {@code spring.cloud.config.profile} 等），拉取成功后写入 {@link SentinelRuleStorage}。\n * </p>\n *\n * @author lianglin\n * @since 1.7.0\n */",
    ),
    (
        "    /**\n     * Responsible for pull data from remote server\n     *\n     * @param environment\n     * @return correct data if success else a empty propertySource or null\n     */",
        "    /**\n     * 从 Config Server 拉取配置并组装为 {@link CompositePropertySource}。\n     *\n     * @param environment\n     * @return 成功时返回属性源，否则返回空属性源或 null\n     */",
    ),
    (
        "            // Try all the labels until one works",
        "            // 依次尝试各 label 直至拉取成功",
    ),
    (
        "                    // result.getPropertySources() can be null if using xml",
        "                    // 使用 XML 配置时 propertySources 可能为 null",
    ),
    (
        "            if (MediaType.APPLICATION_JSON.includes(e.getResponseHeaders().getContentType())) {",
        "            // 服务端错误时尝试解析 JSON 响应体\n            if (MediaType.APPLICATION_JSON.includes(e.getResponseHeaders().getContentType())) {",
    ),
    (
        "            // To avoid redundant addition of header",
        "            // 避免重复添加 Authorization 头",
    ),
]

R["sentinel-extension/sentinel-datasource-spring-cloud-config/src/main/java/com/alibaba/csp/sentinel/datasource/spring/cloud/config/SentinelRuleStorage.java"] = [
    (
        "/**\n * Storage data pull from spring-config-cloud server\n * And notice ${@link SpringCloudConfigDataSource} update latest values\n *\n * @author lianglin\n * @since 1.7.0\n */",
        "/**\n * 缓存从 Spring Cloud Config 拉取的规则属性源，并通知 {@link SpringCloudConfigDataSource} 刷新。\n *\n * @author lianglin\n * @since 1.7.0\n */",
    ),
]

R["sentinel-extension/sentinel-datasource-spring-cloud-config/src/main/java/com/alibaba/csp/sentinel/datasource/spring/cloud/config/SpringCloudConfigDataSource.java"] = [
    (
        "/**\n * <p>A read-only {@code DataSource} with Spring Cloud Config backend.</p>\n * <p>\n * It retrieves the Spring Cloud Config data stored in {@link SentinelRuleStorage}.\n * When the data in the backend has been modified, {@link SentinelRuleStorage} will\n * invoke {@link SpringCloudConfigDataSource#updateValues()} to update values dynamically.\n * </p>\n * <p>\n * To notify the client that the remote config has changed, users could bind a git\n * webhook callback with the {@link SentinelRuleLocator#refresh()} API.\n * </p>\n *\n * @author lianglin\n * @since 1.7.0\n */",
        "/**\n * <p>只读 {@code DataSource}，以 Spring Cloud Config 为后端存储 Sentinel 规则。</p>\n * <p>\n * 从 {@link SentinelRuleStorage} 读取配置；后端变更时由 {@link SentinelRuleStorage}\n * 调用 {@link SpringCloudConfigDataSource#updateValues()} 动态刷新。\n * </p>\n * <p>\n * 可通过 Git Webhook 回调 {@link SentinelRuleLocator#refresh()} 通知客户端配置已更新。\n * </p>\n *\n * @author lianglin\n * @since 1.7.0\n */",
    ),
]

R["sentinel-extension/sentinel-datasource-spring-cloud-config/src/main/java/com/alibaba/csp/sentinel/datasource/spring/cloud/config/config/DataSourceBootstrapConfiguration.java"] = [
    (
        "/**\n * <p>\n * Define the configuration Loaded when spring application start.\n * Put it in META-INF/spring.factories, it will be auto loaded by Spring\n * </p>\n *\n * @author lianglin\n * @since 1.7.0\n */",
        "/**\n * <p>\n * Spring Boot 启动阶段加载 Sentinel Config 数据源相关 Bean。\n * 在 META-INF/spring.factories 中注册后可被 Spring 自动装配。\n * </p>\n *\n * @author lianglin\n * @since 1.7.0\n */",
    ),
]

R["sentinel-extension/sentinel-datasource-zookeeper/src/main/java/com/alibaba/csp/sentinel/datasource/zookeeper/ZookeeperDataSource.java"] = [
    (
        "/**\n * A read-only {@code DataSource} with ZooKeeper backend.\n *\n * @author guonanjun\n */",
        "/**\n * 只读 {@code DataSource}，以 ZooKeeper 为后端存储 Sentinel 规则。\n * 通过 Curator {@link CuratorCache} 监听节点变更并实时刷新规则。\n *\n * @author guonanjun\n */",
    ),
    (
        "    /**\n     * This constructor is Nacos-style.\n     */",
        "    /**\n     * Nacos 风格构造：{@code groupId/dataId} 映射为 ZK 路径 {@code /groupId/dataId}。\n     */",
    ),
    (
        "    /**\n     * This constructor adds authentication information.\n     */",
        "    /**\n     * 带 ZK 认证信息的 Nacos 风格构造。\n     */",
    ),
    (
        "                    // Update the new value to the property.",
        "                    // 将新配置写入 Sentinel 属性",
    ),
]

R["sentinel-extension/sentinel-metric-exporter/src/main/java/com/alibaba/csp/sentinel/metric/MetricExporterInit.java"] = [
    (
        "/**\n * The{@link MetricExporterInit} work on load Metric exporters.\n *\n * @author chenglu\n * @date 2021-07-01 19:58\n * @since 1.8.3\n */",
        "/**\n * {@link InitFunc} 实现：加载并启动 {@link MetricExporter}，注册 JVM 关闭钩子。\n *\n * @author chenglu\n * @date 2021-07-01 19:58\n * @since 1.8.3\n */",
    ),
    (
        "    /**\n     * the list of metric exporters.\n     */",
        "    /** 已注册的指标导出器列表。 */",
    ),
    (
        "    /*\n      load metric exporters.\n     */",
        "    /* 静态加载指标导出器 */",
    ),
    (
        "        // now we use this simple way to load MetricExporter.",
        "        // 当前以硬编码方式注册 JMX 导出器",
    ),
    (
        "        // start the metric exporters.",
        "        // 启动各 MetricExporter",
    ),
    (
        "        // add shutdown hook.",
        "        // 注册关闭钩子以优雅停止导出器",
    ),
]

R["sentinel-extension/sentinel-metric-exporter/src/main/java/com/alibaba/csp/sentinel/metric/collector/MetricCollector.java"] = [
    (
        "/**\n * The {@link MetricCollector} work on collecting metrics in {@link MetricNode}.\n *\n * @author chenglu\n * @date 2021-07-01 20:01\n * @since 1.8.3\n */",
        "/**\n * 从各 {@link ClusterNode} 收集上一秒 {@link MetricNode} 指标并按资源名聚合。\n *\n * @author chenglu\n * @date 2021-07-01 20:01\n * @since 1.8.3\n */",
    ),
    (
        "    /**\n     * collect the metrics in {@link MetricNode}.\n     *\n     * @return the metric grouped by resource name.\n     */",
        "    /**\n     * 收集上一秒各资源的 {@link MetricNode} 快照。\n     *\n     * @return the metric grouped by resource name.\n     */",
    ),
    (
        "    /**\n     * Get the last second {@link MetricNode} of {@link ClusterNode}\n     * @param node {@link ClusterNode}\n     * @param minTime the min time.\n     * @param maxTime the max time.\n     * @return the list of {@link MetricNode}\n     */",
        "    /**\n     * 获取 {@link ClusterNode} 在 [minTime, maxTime) 区间内的 {@link MetricNode} 列表。\n     * @param node {@link ClusterNode}\n     * @param minTime the min time.\n     * @param maxTime the max time.\n     * @return the list of {@link MetricNode}\n     */",
    ),
    (
        "    /**\n     * aggregate the metrics, the metrics under the same resource will left the lasted value\n     * @param metricNodeMap metrics map\n     * @param metrics metrics info group by timestamp\n     * @param node the node\n     */",
        "    /**\n     * 将 metrics 按资源名聚合，同一资源保留时间戳最新的一条。\n     * @param metricNodeMap metrics map\n     * @param metrics metrics info group by timestamp\n     * @param node the node\n     */",
    ),
    (
        "            // always keep the MetricNode is the last",
        "            // 同一资源仅保留时间戳较新的 MetricNode",
    ),
]

R["sentinel-extension/sentinel-metric-exporter/src/main/java/com/alibaba/csp/sentinel/metric/exporter/MetricExporter.java"] = [
    (
        "/**\n * {@link MetricExporter} work on export metric to target monitor.\n * you can implement your export ways by this class.\n *\n * @author chenglu\n * @date 2021-07-01 21:16\n */",
        "/**\n * 指标导出 SPI：将 Sentinel 运行时指标导出到目标监控系统。\n * 可实现此接口自定义导出方式（如 JMX、Prometheus 等）。\n *\n * @author chenglu\n * @date 2021-07-01 21:16\n */",
    ),
    (
        "    /**\n     * start the {@link MetricExporter}.\n     *\n     * @throws Exception start exception.\n     */",
        "    /**\n     * 启动导出器（如注册定时任务）。\n     *\n     * @throws Exception start exception.\n     */",
    ),
    (
        "    /**\n     * export the data to target monitor by the implement.\n     *\n     * @throws Exception export exception.\n     */",
        "    /**\n     * 执行一次指标导出。\n     *\n     * @throws Exception export exception.\n     */",
    ),
    (
        "    /**\n     * shutdown the {@link MetricExporter}.\n     *\n     * @throws Exception shutdown exception.\n     */",
        "    /**\n     * 关闭导出器并释放资源。\n     *\n     * @throws Exception shutdown exception.\n     */",
    ),
]

R["sentinel-extension/sentinel-metric-exporter/src/main/java/com/alibaba/csp/sentinel/metric/exporter/jmx/JMXMetricExporter.java"] = [
    (
        "/**\n * The JMX metric exporter, mainly for write metric datas to JMX bean. It implement {@link MetricExporter}, provide method\n * start, export and shutdown. The mainly design for the jmx is refresh the JMX bean data scheduled.\n * {@link JMXExportTask} work on export data to {@link MetricBean}.\n *\n * @author chenglu\n * @date 2021-07-01 20:02\n * @since 1.8.3\n */",
        "/**\n * JMX 指标导出器：定时将 {@link MetricCollector} 采集的数据写入 {@link MetricBean}。\n * 实现 {@link MetricExporter} 的 start/export/shutdown 生命周期；\n * 内部 {@link JMXExportTask} 每秒调度一次导出。\n *\n * @author chenglu\n * @date 2021-07-01 20:02\n * @since 1.8.3\n */",
    ),
    (
        "    /**\n     * schedule executor.\n     */",
        "    /** 定时调度线程池。 */",
    ),
    (
        "    /**\n     * JMX metric writer, write metric datas to {@link MetricBean}.\n     */",
        "    /** JMX 指标写入器，负责注册/更新 {@link MetricBean}。 */",
    ),
    (
        "    /**\n     * global metrics collector.\n     */",
        "    /** 全局指标采集器。 */",
    ),
    (
        "    /**\n     * JMXExportTask mainly work on execute the JMX metric export.\n     */",
        "    /** 定时任务：调用 {@link #export()} 刷新 JMX MBean 数据。 */",
    ),
]

R["sentinel-extension/sentinel-metric-exporter/src/main/java/com/alibaba/csp/sentinel/metric/exporter/jmx/MBeanRegistry.java"] = [
    (
        "/**\n * This class provides a unified interface for registering/unregistering of Metric MBean.\n *\n * @author chenglu\n * @date 2021-07-01 20:02\n * @since 1.8.3\n */",
        "/**\n * Sentinel 指标 MBean 统一注册中心：管理 {@link MetricBean} 的注册、注销与查找。\n *\n * @author chenglu\n * @date 2021-07-01 20:02\n * @since 1.8.3\n */",
    ),
    (
        "            // Account for running within IKVM and create a new MBeanServer",
        "            // IKVM 等环境无 PlatformMBeanServer 时自行创建",
    ),
    (
        "    /**\n     * Registers a new MBean with the platform MBean server.\n     * @param bean the bean being registered\n     * @param mBeanName the mBeanName\n     * @throws JMException MBean can not register exception\n     */",
        "    /**\n     * 向平台 MBeanServer 注册 {@link MetricBean}。\n     * @param bean the bean being registered\n     * @param mBeanName the mBeanName\n     * @throws JMException MBean can not register exception\n     */",
    ),
    (
        "    /**\n     * unregister the MetricBean\n     * @param bean MetricBean\n     */",
        "    /**\n     * 注销已注册的 {@link MetricBean}。\n     * @param bean MetricBean\n     */",
    ),
    (
        "    /**\n     * find the MBean by BeanName\n     * @param mBeanName mBeanName\n     * @return MetricMBean\n     */",
        "    /**\n     * 按 ObjectName 查找已注册的 MBean。\n     * @param mBeanName mBeanName\n     * @return MetricMBean\n     */",
    ),
    (
        "    /**\n     * list all MBeans which is registered into MBeanRegistry\n     * @return MetricBeans\n     */",
        "    /**\n     * 列出当前注册表内全部 {@link MetricBean}。\n     * @return MetricBeans\n     */",
    ),
]

R["sentinel-extension/sentinel-metric-exporter/src/main/java/com/alibaba/csp/sentinel/metric/exporter/jmx/MetricBean.java"] = [
    (
        "/**\n * the MetricBean for JMX expose.\n *\n * @author chenglu\n * @date 2021-07-01 20:02\n * @since 1.8.3\n */",
        "/**\n * JMX 暴露的 Sentinel 指标 MBean 实现，字段对应 {@link MetricNode} 各维度。\n *\n * @author chenglu\n * @date 2021-07-01 20:02\n * @since 1.8.3\n */",
    ),
    (
        "    /**\n     * Resource classification (e.g. SQL or RPC)\n     */",
        "    /** 资源分类（如 SQL、RPC 等）。 */",
    ),
    (
        "    /**\n     * set the version to current Mbean.\n     *\n     * @param version current version.\n     */",
        "    /**\n     * 设置当前导出批次版本号，用于识别过期 MBean。\n     *\n     * @param version current version.\n     */",
    ),
    (
        "    /**\n     * reset the MBean value to the initialized value.\n     */",
        "    /** 将各计数器重置为初始零值。 */",
    ),
    (
        "    /**\n     * set the MetricBean's value which from MetricNode.\n     *\n     * @param metricNode metric Node for write file\n     */",
        "    /**\n     * 从 {@link MetricNode} 拷贝指标值到当前 MBean。\n     *\n     * @param metricNode metric Node for write file\n     */",
    ),
]

R["sentinel-extension/sentinel-metric-exporter/src/main/java/com/alibaba/csp/sentinel/metric/exporter/jmx/MetricBeanWriter.java"] = [
    (
        "/**\n * the metric bean writer, it provides {@link MetricBeanWriter#write} method for register the\n * MetricBean in {@link MBeanRegistry} or update the value of MetricBean\n *\n * @author chenglu\n * @date 2021-07-01 20:02\n * @since 1.8.3\n */",
        "/**\n * 指标 MBean 写入器：通过 {@link MetricBeanWriter#write} 注册或更新 {@link MBeanRegistry} 中的 {@link MetricBean}。\n *\n * @author chenglu\n * @date 2021-07-01 20:02\n * @since 1.8.3\n */",
    ),
    (
        "    /**\n     * write the MetricNode value to MetricBean\n     * if the MetricBean is not registered into {@link MBeanRegistry},\n     * it will be created and registered into {@link MBeanRegistry}.\n     * else it will update the value of MetricBean.\n     * Notes. if the MetricNode is null, then {@link MetricBean} will be reset.\n     * @param map metricNode value group by resource\n     * @throws Exception write failed exception\n     */",
        "    /**\n     * 将按资源分组的 {@link MetricNode} 写入对应 {@link MetricBean}。\n     * 未注册则创建并注册；已存在则更新数值。\n     * 若 map 为空则重置全部已注册 MBean。\n     * @param map metricNode value group by resource\n     * @throws Exception write failed exception\n     */",
    ),
    (
        "        // set or update the new metric value",
        "        // 注册或更新本轮指标值",
    ),
    (
        "            // Fix JMX Metrics export error: https://github.com/alibaba/Sentinel/issues/2989",
        "            // 转义资源名中的 JMX 特殊字符，见 issue #2989",
    ),
    (
        "            // Without escape, it will throw \"cannot add mbean for pattern name\" or \"invalid character in value part of property\" exception",
        "            // 未转义会导致 ObjectName 非法",
    ),
    (
        "        // reset the old value",
        "        // 重置/注销本轮未更新的旧 MBean",
    ),
    (
        "     * escape only when arg has special character",
        "     * 仅当资源名含 JMX 特殊字符（*、?、=、:、\"、换行等）时进行转义",
    ),
]

R["sentinel-extension/sentinel-metric-exporter/src/main/java/com/alibaba/csp/sentinel/metric/exporter/jmx/MetricMXBean.java"] = [
    (
        "/**\n * the Metric JMX Bean interface.\n *\n * @author chenglu\n * @date 2021-07-01 20:02\n * @since 1.8.3\n */",
        "/**\n * Sentinel 指标 JMX MBean 接口，暴露 QPS、RT、并发度等运行时统计。\n *\n * @author chenglu\n * @date 2021-07-01 20:02\n * @since 1.8.3\n */",
    ),
]

R["sentinel-extension/sentinel-parameter-flow-control/src/main/java/com/alibaba/csp/sentinel/command/handler/GetParamFlowRulesCommandHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 0.2.0\n */",
        "/**\n * 命令处理器：返回当前全部热点参数流控规则（JSON）。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
    ),
    (
        '@CommandMapping(name = "getParamFlowRules", desc = "Get all parameter flow rules")',
        '@CommandMapping(name = "getParamFlowRules", desc = "获取全部热点参数流控规则")',
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
    index_file = Path("/tmp/git-index-sentinel-w29a")
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
        "sentinel 1.8.10: Chinese-annotate wave 29a [0:15]",
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
