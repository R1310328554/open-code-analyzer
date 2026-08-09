#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-10b batch files [20:40] (env/info/io/json)."""
from __future__ import annotations

import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springboot/4.1.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][20:40]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "RandomValuePropertySource.java": [
        (
            "/**\n * {@link PropertySource} that returns a random value for any property that starts with\n * {@literal \"random.\"}. Where the \"unqualified property name\" is the portion of the\n * requested property name beyond the \"random.\" prefix, this {@link PropertySource}\n * returns:\n * <ul>\n * <li>When {@literal \"int\"}, a random {@link Integer} value, restricted by an optionally\n * specified range.</li>\n * <li>When {@literal \"long\"}, a random {@link Long} value, restricted by an optionally\n * specified range.</li>\n * <li>When {@literal \"uuid\"}, a random {@link UUID} value.</li>\n * <li>Otherwise, a {@code byte[]}.</li>\n * </ul>\n * The {@literal \"random.int\"} and {@literal \"random.long\"} properties supports a range\n * suffix whose syntax is:\n * <p>\n * {@code OPEN value (,max) CLOSE} where the {@code OPEN,CLOSE} are any character and\n * {@code value,max} are integers. If {@code max} is not provided, then 0 is used as the\n * lower bound and {@code value} is the upper bound. If {@code max} is provided then\n * {@code value} is the minimum value and {@code max} is the maximum (exclusive).\n *\n * @author Dave Syer\n * @author Matt Benson\n * @author Madhura Bhave\n * @author Moritz Halbritter\n * @since 1.0.0\n */",
            "/**\n * 对以 {@literal \"random.\"} 开头的任意属性返回随机值的 {@link PropertySource}。\n * \"未限定属性名\" 指请求属性名中 {@literal \"random.\"} 前缀之后的部分，此 {@link PropertySource} 返回：\n * <ul>\n * <li>{@literal \"int\"} 时，返回随机 {@link Integer}，可由可选范围限制。</li>\n * <li>{@literal \"long\"} 时，返回随机 {@link Long}，可由可选范围限制。</li>\n * <li>{@literal \"uuid\"} 时，返回随机 {@link UUID}。</li>\n * <li>否则返回 {@code byte[]}。</li>\n * </ul>\n * {@literal \"random.int\"} 与 {@literal \"random.long\"} 支持范围后缀，语法为：\n * <p>\n * {@code OPEN value (,max) CLOSE}，其中 {@code OPEN,CLOSE} 为任意字符，\n * {@code value,max} 为整数。未提供 {@code max} 时，0 为下界、{@code value} 为上界；\n * 提供 {@code max} 时，{@code value} 为最小值、{@code max} 为最大值（不含）。\n *\n * @author Dave Syer\n * @author Matt Benson\n * @author Madhura Bhave\n * @author Moritz Halbritter\n * @since 1.0.0\n */",
        ),
        (
            "\t/**\n\t * Name of the random {@link PropertySource}.\n\t */",
            "\t/**\n\t * 随机 {@link PropertySource} 的名称。\n\t */",
        ),
        (
            "\t/**\n\t * Add a {@link RandomValuePropertySource} to the given {@link Environment}.\n\t * @param environment the environment to add the random property source to\n\t */",
            "\t/**\n\t * 向给定 {@link Environment} 添加 {@link RandomValuePropertySource}。\n\t *\n\t * @param environment 要添加随机属性源的环境\n\t */",
        ),
        (
            "\t/**\n\t * Add a {@link RandomValuePropertySource} to the given {@link Environment}.\n\t * @param environment the environment to add the random property source to\n\t * @param logger logger used for debug and trace information\n\t * @since 4.0.0\n\t */",
            "\t/**\n\t * 向给定 {@link Environment} 添加 {@link RandomValuePropertySource}。\n\t *\n\t * @param environment 要添加随机属性源的环境\n\t * @param logger 用于 debug 与 trace 信息的 logger\n\t * @since 4.0.0\n\t */",
        ),
    ],
    "YamlPropertySourceLoader.java": [
        (
            "/**\n * Strategy to load '.yml' (or '.yaml') files into a {@link PropertySource}.\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @since 1.0.0\n */",
            "/**\n * 将 '.yml'（或 '.yaml'）文件加载为 {@link PropertySource} 的策略。\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @since 1.0.0\n */",
        ),
    ],
    "BuildProperties.java": [
        (
            "/**\n * Provide build-related information such as group and artifact.\n *\n * @author Stephane Nicoll\n * @since 1.4.0\n */",
            "/**\n * 提供构建相关信息，例如 group 与 artifact。\n *\n * @author Stephane Nicoll\n * @since 1.4.0\n */",
        ),
        (
            "\t/**\n\t * Create an instance with the specified entries.\n\t * @param entries the information to expose\n\t */",
            "\t/**\n\t * 使用指定条目创建实例。\n\t *\n\t * @param entries 要暴露的信息\n\t */",
        ),
        (
            "\t/**\n\t * Return the groupId of the project or {@code null}.\n\t * @return the group\n\t */",
            "\t/**\n\t * 返回项目的 groupId 或 {@code null}。\n\t *\n\t * @return group\n\t */",
        ),
        (
            "\t/**\n\t * Return the artifactId of the project or {@code null}.\n\t * @return the artifact\n\t */",
            "\t/**\n\t * 返回项目的 artifactId 或 {@code null}。\n\t *\n\t * @return artifact\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of the project or {@code null}.\n\t * @return the name\n\t */",
            "\t/**\n\t * 返回项目名称或 {@code null}。\n\t *\n\t * @return 名称\n\t */",
        ),
        (
            "\t/**\n\t * Return the version of the project or {@code null}.\n\t * @return the version\n\t */",
            "\t/**\n\t * 返回项目版本或 {@code null}。\n\t *\n\t * @return 版本\n\t */",
        ),
        (
            "\t/**\n\t * Return the timestamp of the build or {@code null}.\n\t * <p>\n\t * If the original value could not be parsed properly, it is still available with the\n\t * {@code time} key.\n\t * @return the build time\n\t * @see #get(String)\n\t */",
            "\t/**\n\t * 返回构建时间戳或 {@code null}。\n\t * <p>\n\t * 若原始值无法正确解析，仍可通过 {@code time} 键获取。\n\t *\n\t * @return 构建时间\n\t * @see #get(String)\n\t */",
        ),
    ],
    "GitProperties.java": [
        (
            "/**\n * Provide git-related information such as commit id and time.\n *\n * @author Stephane Nicoll\n * @since 1.4.0\n */",
            "/**\n * 提供 Git 相关信息，例如 commit id 与时间。\n *\n * @author Stephane Nicoll\n * @since 1.4.0\n */",
        ),
        (
            "\t/**\n\t * Return the name of the branch or {@code null}.\n\t * @return the branch\n\t */",
            "\t/**\n\t * 返回分支名称或 {@code null}。\n\t *\n\t * @return 分支\n\t */",
        ),
        (
            "\t/**\n\t * Return the full id of the commit or {@code null}.\n\t * @return the full commit id\n\t */",
            "\t/**\n\t * 返回完整 commit id 或 {@code null}。\n\t *\n\t * @return 完整 commit id\n\t */",
        ),
        (
            "\t/**\n\t * Return the abbreviated id of the commit or {@code null}.\n\t * @return the short commit id\n\t */",
            "\t/**\n\t * 返回缩写 commit id 或 {@code null}。\n\t *\n\t * @return 短 commit id\n\t */",
        ),
        (
            "\t/**\n\t * Return the timestamp of the commit or {@code null}.\n\t * <p>\n\t * If the original value could not be parsed properly, it is still available with the\n\t * {@code commit.time} key.\n\t * @return the commit time\n\t * @see #get(String)\n\t */",
            "\t/**\n\t * 返回 commit 时间戳或 {@code null}。\n\t * <p>\n\t * 若原始值无法正确解析，仍可通过 {@code commit.time} 键获取。\n\t *\n\t * @return commit 时间\n\t * @see #get(String)\n\t */",
        ),
        (
            "\t/**\n\t * {@link RuntimeHintsRegistrar} for git properties.\n\t */",
            "\t/**\n\t * Git 属性的 {@link RuntimeHintsRegistrar}。\n\t */",
        ),
        (
            "\t/**\n\t * Coercer used to convert a source value to epoch time.\n\t */",
            "\t/**\n\t * 将源值转换为 epoch 时间的 Coercer。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Attempt to convert the specified value to epoch time.\n\t\t * @param value the value to coerce to\n\t\t * @return the epoch time in milliseconds or {@code null}\n\t\t */",
            "\t\t/**\n\t\t * 尝试将指定值转换为 epoch 时间。\n\t\t *\n\t\t * @param value 待转换的值\n\t\t * @return 毫秒 epoch 时间或 {@code null}\n\t\t */",
        ),
    ],
    "InfoProperties.java": [
        (
            "/**\n * Base class for components exposing unstructured data with dedicated methods for well\n * known keys.\n *\n * @author Stephane Nicoll\n * @since 1.4.0\n */",
            "/**\n * 暴露非结构化数据的组件基类，为常见键提供专用方法。\n *\n * @author Stephane Nicoll\n * @since 1.4.0\n */",
        ),
        (
            "\t/**\n\t * Create an instance with the specified entries.\n\t * @param entries the information to expose\n\t */",
            "\t/**\n\t * 使用指定条目创建实例。\n\t *\n\t * @param entries 要暴露的信息\n\t */",
        ),
        (
            "\t/**\n\t * Return the value of the specified property or {@code null}.\n\t * @param key the key of the property\n\t * @return the property value\n\t */",
            "\t/**\n\t * 返回指定属性的值或 {@code null}。\n\t *\n\t * @param key 属性键\n\t * @return 属性值\n\t */",
        ),
        (
            "\t/**\n\t * Return the value of the specified property as an {@link Instant} or {@code null} if\n\t * the value is not a valid {@link Long} representation of an epoch time.\n\t * @param key the key of the property\n\t * @return the property value\n\t */",
            "\t/**\n\t * 将指定属性值作为 {@link Instant} 返回；若值不是有效的 epoch {@link Long} 表示则返回 {@code null}。\n\t *\n\t * @param key 属性键\n\t * @return 属性值\n\t */",
        ),
        (
            "\t/**\n\t * Return a {@link PropertySource} of this instance.\n\t * @return a {@link PropertySource}\n\t */",
            "\t/**\n\t * 返回此实例对应的 {@link PropertySource}。\n\t *\n\t * @return {@link PropertySource}\n\t */",
        ),
        (
            "\t/**\n\t * Property entry.\n\t */",
            "\t/**\n\t * 属性条目。\n\t */",
        ),
    ],
    "JavaInfo.java": [
        (
            "/**\n * Information about the Java environment the application is running in.\n *\n * @author Jonatan Ivanov\n * @author Stephane Nicoll\n * @since 2.6.0\n */",
            "/**\n * 应用运行所在 Java 环境的信息。\n *\n * @author Jonatan Ivanov\n * @author Stephane Nicoll\n * @since 2.6.0\n */",
        ),
        (
            "\t/**\n\t * Information about the Java Vendor of the Java Runtime the application is running\n\t * in.\n\t *\n\t * @since 2.7.0\n\t */",
            "\t/**\n\t * 应用运行所在 Java Runtime 的 Java Vendor 信息。\n\t *\n\t * @since 2.7.0\n\t */",
        ),
        (
            "\t/**\n\t * Information about the Java Runtime Environment the application is running in.\n\t */",
            "\t/**\n\t * 应用运行所在 Java Runtime Environment 的信息。\n\t */",
        ),
        (
            "\t/**\n\t * Information about the Java Virtual Machine the application is running in.\n\t */",
            "\t/**\n\t * 应用运行所在 Java Virtual Machine 的信息。\n\t */",
        ),
    ],
    "OsInfo.java": [
        (
            "/**\n * Information about the Operating System the application is running on.\n *\n * @author Jonatan Ivanov\n * @since 2.7.0\n */",
            "/**\n * 应用运行所在操作系统（Operating System）的信息。\n *\n * @author Jonatan Ivanov\n * @since 2.7.0\n */",
        ),
    ],
    "ProcessInfo.java": [
        (
            "/**\n * Information about the process of the application.\n *\n * @author Jonatan Ivanov\n * @author Andrey Litvitski\n * @since 3.3.0\n */",
            "/**\n * 应用进程的信息。\n *\n * @author Jonatan Ivanov\n * @author Andrey Litvitski\n * @since 3.3.0\n */",
        ),
        (
            "\t/**\n\t * Number of processors available to the process. This value may change between\n\t * invocations especially in (containerized) environments where resource usage can be\n\t * isolated (for example using control groups).\n\t * @return result of {@link Runtime#availableProcessors()}\n\t * @see Runtime#availableProcessors()\n\t */",
            "\t/**\n\t * 进程可用的处理器数量。该值可能在多次调用间变化，\n\t * 尤其在可通过 cgroup 等隔离资源的（容器化）环境中。\n\t *\n\t * @return {@link Runtime#availableProcessors()} 的结果\n\t * @see Runtime#availableProcessors()\n\t */",
        ),
        (
            "\t/**\n\t * Memory information for the process. These values can provide details about the\n\t * current memory usage and limits selected by the user or JVM ergonomics (init, max,\n\t * committed, used for heap and non-heap). If limits not set explicitly, it might not\n\t * be trivial to know what these values are runtime; especially in (containerized)\n\t * environments where resource usage can be isolated (for example using control\n\t * groups) or not necessarily trivial to discover. Other than that, these values can\n\t * indicate if the JVM can resize the heap (stop-the-world).\n\t * @return heap and non-heap memory information\n\t * @since 3.4.0\n\t * @see MemoryMXBean#getHeapMemoryUsage()\n\t * @see MemoryMXBean#getNonHeapMemoryUsage()\n\t * @see MemoryUsage\n\t */",
            "\t/**\n\t * 进程的内存信息。可提供当前内存使用与用户或 JVM ergonomics 所选限制\n\t * （堆与非堆的 init、max、committed、used）的详情。若未显式设置限制，\n\t * 运行时可能难以确定这些值，尤其在容器化或 cgroup 隔离环境中。\n\t * 此外，这些值可指示 JVM 是否能调整堆大小（stop-the-world）。\n\t *\n\t * @return 堆与非堆内存信息\n\t * @since 3.4.0\n\t * @see MemoryMXBean#getHeapMemoryUsage()\n\t * @see MemoryMXBean#getNonHeapMemoryUsage()\n\t * @see MemoryUsage\n\t */",
        ),
        (
            "\t/**\n\t * Virtual threads information for the process. These values provide details about the\n\t * current state of virtual threads, including the number of mounted threads, queued\n\t * threads, the parallelism level, and the thread pool size.\n\t * @return an instance of {@link VirtualThreadsInfo} containing information about\n\t * virtual threads, or {@code null} if the VirtualThreadSchedulerMXBean is not\n\t * available\n\t * @since 3.5.0\n\t */",
            "\t/**\n\t * 进程的虚拟线程信息，包括 mounted 线程数、排队线程数、并行度与线程池大小。\n\t *\n\t * @return 包含虚拟线程信息的 {@link VirtualThreadsInfo}，\n\t * 若 VirtualThreadSchedulerMXBean 不可用则返回 {@code null}\n\t * @since 3.5.0\n\t */",
        ),
        (
            "\t/**\n\t * Uptime of the process. Can be useful to see how long the process has been running\n\t * and to check how long ago the last deployment or restart happened.\n\t * @return duration since the process started, if available, otherwise {@code null}\n\t * @since 4.1.0\n\t */",
            "\t/**\n\t * 进程运行时长。可用于查看进程已运行多久以及上次部署或重启距今多久。\n\t *\n\t * @return 自进程启动以来的时长，若不可用则返回 {@code null}\n\t * @since 4.1.0\n\t */",
        ),
        (
            "\t/**\n\t * Time at which the process started. Can be useful to see when the process was\n\t * started and to check when the last deployment or restart happened.\n\t * @return the time when the process started, if available, otherwise {@code null}\n\t * @since 4.1.0\n\t * @see Info#startInstant()\n\t */",
            "\t/**\n\t * 进程启动时刻。可用于查看进程何时启动以及上次部署或重启时间。\n\t *\n\t * @return 进程启动时间，若不可用则返回 {@code null}\n\t * @since 4.1.0\n\t * @see Info#startInstant()\n\t */",
        ),
        (
            "\t/**\n\t * Current time of the process. Can be useful to check if there is any clock-skew\n\t * issue and if the current time that the process knows is accurate enough.\n\t * @return the current time of the process\n\t * @since 4.1.0\n\t * @see Instant#now\n\t */",
            "\t/**\n\t * 进程当前时间。可用于检查是否存在时钟偏移以及进程所知当前时间是否足够准确。\n\t *\n\t * @return 进程当前时间\n\t * @since 4.1.0\n\t * @see Instant#now\n\t */",
        ),
        (
            "\t/**\n\t * Timezone of the process. Can help to detect time and timezone related issues.\n\t * @return the timezone of the process\n\t * @since 4.1.0\n\t * @see ZoneId#systemDefault()\n\t */",
            "\t/**\n\t * 进程时区。有助于检测与时间、时区相关的问题。\n\t *\n\t * @return 进程时区\n\t * @since 4.1.0\n\t * @see ZoneId#systemDefault()\n\t */",
        ),
        (
            "\t/**\n\t * Locale of the process. Can help to detect issues connected to language and country\n\t * settings.\n\t * @return the locale of the process\n\t * @since 4.1.0\n\t * @see Locale#getDefault()\n\t */",
            "\t/**\n\t * 进程 Locale。有助于检测与语言、国家/地区设置相关的问题。\n\t *\n\t * @return 进程 Locale\n\t * @since 4.1.0\n\t * @see Locale#getDefault()\n\t */",
        ),
        (
            "\t/**\n\t * Working directory of the process. Can help to locate files that the process uses.\n\t * @return the absolute path of the working directory of the process\n\t * @since 4.1.0\n\t */",
            "\t/**\n\t * 进程工作目录。有助于定位进程使用的文件。\n\t *\n\t * @return 进程工作目录的绝对路径\n\t * @since 4.1.0\n\t */",
        ),
        (
            "\t/**\n\t * Virtual threads information.\n\t *\n\t * @since 3.5.0\n\t */",
            "\t/**\n\t * 虚拟线程信息。\n\t *\n\t * @since 3.5.0\n\t */",
        ),
        (
            "\t/**\n\t * Memory information.\n\t *\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 内存信息。\n\t *\n\t * @since 3.4.0\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Garbage Collector information for the process. This list provides details about\n\t\t * the currently used GC algorithms selected by the user or JVM ergonomics. It\n\t\t * might not be trivial to know the used GC algorithms since that usually depends\n\t\t * on the {@link Runtime#availableProcessors()} (see:\n\t\t * {@link ProcessInfo#getCpus()}) and the available memory (see:\n\t\t * {@link MemoryUsageInfo}).\n\t\t * @return {@link List} of {@link GarbageCollectorInfo}.\n\t\t * @since 3.5.0\n\t\t */",
            "\t\t/**\n\t\t * 进程的垃圾收集器信息。列出用户或 JVM ergonomics 所选当前 GC 算法。\n\t\t * 所用 GC 算法通常取决于 {@link Runtime#availableProcessors()}（见 {@link ProcessInfo#getCpus()}）\n\t\t * 与可用内存（见 {@link MemoryUsageInfo}），可能不易直接确定。\n\t\t *\n\t\t * @return {@link GarbageCollectorInfo} 的 {@link List}\n\t\t * @since 3.5.0\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Garbage collection information.\n\t\t *\n\t\t * @since 3.5.0\n\t\t */",
            "\t\t/**\n\t\t * 垃圾收集信息。\n\t\t *\n\t\t * @since 3.5.0\n\t\t */",
        ),
    ],
    "SslInfo.java": [
        (
            "/**\n * Information about the certificates that the application uses.\n *\n * @author Jonatan Ivanov\n * @author Moritz Halbritter\n * @since 3.4.0\n */",
            "/**\n * 应用所用证书的信息。\n *\n * @author Jonatan Ivanov\n * @author Moritz Halbritter\n * @since 3.4.0\n */",
        ),
        (
            "\t/**\n\t * Creates a new instance.\n\t * @param sslBundles the {@link SslBundles} to extract the info from\n\t * @since 4.0.0\n\t */",
            "\t/**\n\t * 创建新实例。\n\t *\n\t * @param sslBundles 用于提取信息的 {@link SslBundles}\n\t * @since 4.0.0\n\t */",
        ),
        (
            "\t/**\n\t * Creates a new instance.\n\t * @param sslBundles the {@link SslBundles} to extract the info from\n\t * @param clock the {@link Clock} to use\n\t * @since 4.0.0\n\t */",
            "\t/**\n\t * 创建新实例。\n\t *\n\t * @param sslBundles 用于提取信息的 {@link SslBundles}\n\t * @param clock 使用的 {@link Clock}\n\t * @since 4.0.0\n\t */",
        ),
        (
            "\t/**\n\t * Returns information on all SSL bundles.\n\t * @return information on all SSL bundles\n\t */",
            "\t/**\n\t * 返回所有 SSL bundle 的信息。\n\t *\n\t * @return 所有 SSL bundle 的信息\n\t */",
        ),
        (
            "\t/**\n\t * Returns an SSL bundle by name.\n\t * @param name the name of the SSL bundle\n\t * @return the {@link BundleInfo} for the given SSL bundle\n\t * @throws NoSuchSslBundleException if a bundle with the provided name does not exist\n\t * @since 3.5.0\n\t */",
            "\t/**\n\t * 按名称返回 SSL bundle。\n\t *\n\t * @param name SSL bundle 名称\n\t * @return 给定 SSL bundle 的 {@link BundleInfo}\n\t * @throws NoSuchSslBundleException 若不存在该名称的 bundle\n\t * @since 3.5.0\n\t */",
        ),
        (
            "\t/**\n\t * Info about a single {@link SslBundle}.\n\t */",
            "\t/**\n\t * 单个 {@link SslBundle} 的信息。\n\t */",
        ),
        (
            "\t/**\n\t * Info about a single certificate chain.\n\t */",
            "\t/**\n\t * 单条证书链的信息。\n\t */",
        ),
        (
            "\t/**\n\t * Info about a certificate.\n\t */",
            "\t/**\n\t * 证书信息。\n\t */",
        ),
        (
            "\t/**\n\t * Certificate validity info.\n\t */",
            "\t/**\n\t * 证书有效性信息。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Validity Status.\n\t\t */",
            "\t\t/**\n\t\t * 有效性状态。\n\t\t */",
        ),
        (
            "\t\t\t/**\n\t\t\t * The certificate is valid.\n\t\t\t */",
            "\t\t\t/**\n\t\t\t * 证书有效。\n\t\t\t */",
        ),
        (
            "\t\t\t/**\n\t\t\t * The certificate's validity date range is in the future.\n\t\t\t */",
            "\t\t\t/**\n\t\t\t * 证书有效期尚未开始。\n\t\t\t */",
        ),
        (
            "\t\t\t/**\n\t\t\t * The certificate's validity date range is in the past.\n\t\t\t */",
            "\t\t\t/**\n\t\t\t * 证书已过期。\n\t\t\t */",
        ),
    ],
    "ApplicationResourceLoader.java": [
        (
            "/**\n * Class can be used to obtain {@link ResourceLoader ResourceLoaders} supporting\n * additional {@link ProtocolResolver ProtocolResolvers} registered in\n * {@code spring.factories}.\n * <p>\n * When not delegating to an existing resource loader, plain paths without a qualifier\n * will resolve to file system resources. This is different from\n * {@code DefaultResourceLoader}, which resolves unqualified paths to classpath resources.\n *\n * @author Scott Frederick\n * @author Moritz Halbritter\n * @author Phillip Webb\n * @since 3.3.0\n */",
            "/**\n * 可用于获取支持 {@code spring.factories} 中注册的额外 {@link ProtocolResolver ProtocolResolvers} 的\n * {@link ResourceLoader ResourceLoaders} 的类。\n * <p>\n * 未委托给现有 resource loader 时，无限定符的 plain path 将解析为文件系统资源，\n * 这与 {@code DefaultResourceLoader} 将无限定 path 解析为 classpath 资源不同。\n *\n * @author Scott Frederick\n * @author Moritz Halbritter\n * @author Phillip Webb\n * @since 3.3.0\n */",
        ),
        (
            "\t/**\n\t * Return a {@link ResourceLoader} supporting additional {@link ProtocolResolver\n\t * ProtocolResolvers} registered in {@code spring.factories}. The factories file will\n\t * be resolved using the default class loader at the time this call is made. Resources\n\t * will be resolved using the default class loader at the time they are resolved.\n\t * @return a {@link ResourceLoader} instance\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 返回支持 {@code spring.factories} 中额外 {@link ProtocolResolver ProtocolResolvers} 的\n\t * {@link ResourceLoader}。factories 文件在调用时使用默认 class loader 解析；\n\t * 资源在解析时使用当时的默认 class loader。\n\t *\n\t * @return {@link ResourceLoader} 实例\n\t * @since 3.4.0\n\t */",
        ),
        (
            "\t/**\n\t * Return a {@link ResourceLoader} supporting additional {@link ProtocolResolver\n\t * ProtocolResolvers} registered in {@code spring.factories}. The factories files and\n\t * resources will be resolved using the specified class loader.\n\t * @param classLoader the class loader to use or {@code null} to use the default class\n\t * loader\n\t * @return a {@link ResourceLoader} instance\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 返回支持 {@code spring.factories} 中额外 {@link ProtocolResolver ProtocolResolvers} 的\n\t * {@link ResourceLoader}。factories 文件与资源均使用指定 class loader 解析。\n\t *\n\t * @param classLoader 使用的 class loader，{@code null} 表示默认 class loader\n\t * @return {@link ResourceLoader} 实例\n\t * @since 3.4.0\n\t */",
        ),
        (
            "\t/**\n\t * Return a {@link ResourceLoader} supporting additional {@link ProtocolResolver\n\t * ProtocolResolvers} registered in {@code spring.factories}.\n\t * @param classLoader the class loader to use or {@code null} to use the default class\n\t * loader\n\t * @param springFactoriesLoader the {@link SpringFactoriesLoader} used to load\n\t * {@link ProtocolResolver ProtocolResolvers}\n\t * @return a {@link ResourceLoader} instance\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 返回支持 {@code spring.factories} 中额外 {@link ProtocolResolver ProtocolResolvers} 的\n\t * {@link ResourceLoader}。\n\t *\n\t * @param classLoader 使用的 class loader，{@code null} 表示默认 class loader\n\t * @param springFactoriesLoader 用于加载 {@link ProtocolResolver ProtocolResolvers} 的\n\t * {@link SpringFactoriesLoader}\n\t * @return {@link ResourceLoader} 实例\n\t * @since 3.4.0\n\t */",
        ),
        (
            "\t/**\n\t * Return a {@link ResourceLoader} supporting additional {@link ProtocolResolver\n\t * ProtocolResolvers} registered in {@code spring.factories}.\n\t * @param classLoader the class loader to use or {@code null} to use the default class\n\t * loader\n\t * @param springFactoriesLoader the {@link SpringFactoriesLoader} used to load\n\t * {@link ProtocolResolver ProtocolResolvers}\n\t * @param workingDirectory the working directory\n\t * @return a {@link ResourceLoader} instance\n\t * @since 3.5.0\n\t */",
            "\t/**\n\t * 返回支持 {@code spring.factories} 中额外 {@link ProtocolResolver ProtocolResolvers} 的\n\t * {@link ResourceLoader}。\n\t *\n\t * @param classLoader 使用的 class loader，{@code null} 表示默认 class loader\n\t * @param springFactoriesLoader 用于加载 {@link ProtocolResolver ProtocolResolvers} 的\n\t * {@link SpringFactoriesLoader}\n\t * @param workingDirectory 工作目录\n\t * @return {@link ResourceLoader} 实例\n\t * @since 3.5.0\n\t */",
        ),
        (
            "\t/**\n\t * Return a {@link ResourceLoader} delegating to the given resource loader and\n\t * supporting additional {@link ProtocolResolver ProtocolResolvers} registered in\n\t * {@code spring.factories}. The factories file will be resolved using the default\n\t * class loader at the time this call is made.\n\t * @param resourceLoader the delegate resource loader\n\t * @return a {@link ResourceLoader} instance\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 返回委托给给定 resource loader 并支持 {@code spring.factories} 中额外\n\t * {@link ProtocolResolver ProtocolResolvers} 的 {@link ResourceLoader}。\n\t * factories 文件在调用时使用默认 class loader 解析。\n\t *\n\t * @param resourceLoader 委托 resource loader\n\t * @return {@link ResourceLoader} 实例\n\t * @since 3.4.0\n\t */",
        ),
        (
            "\t/**\n\t * Return a {@link ResourceLoader} delegating to the given resource loader and\n\t * supporting additional {@link ProtocolResolver ProtocolResolvers} registered in\n\t * {@code spring.factories}. The factories file will be resolved using the default\n\t * class loader at the time this call is made.\n\t * @param resourceLoader the delegate resource loader\n\t * @param preferFileResolution if file based resolution is preferred when a suitable\n\t * {@link FilePathResolver} support the resource\n\t * @return a {@link ResourceLoader} instance\n\t * @since 3.4.1\n\t */",
            "\t/**\n\t * 返回委托给给定 resource loader 并支持 {@code spring.factories} 中额外\n\t * {@link ProtocolResolver ProtocolResolvers} 的 {@link ResourceLoader}。\n\t * factories 文件在调用时使用默认 class loader 解析。\n\t *\n\t * @param resourceLoader 委托 resource loader\n\t * @param preferFileResolution 当合适的 {@link FilePathResolver} 支持该资源时是否优先基于文件的解析\n\t * @return {@link ResourceLoader} 实例\n\t * @since 3.4.1\n\t */",
        ),
        (
            "\t/**\n\t * Return a {@link ResourceLoader} delegating to the given resource loader and\n\t * supporting additional {@link ProtocolResolver ProtocolResolvers} registered in\n\t * {@code spring.factories}.\n\t * @param resourceLoader the delegate resource loader\n\t * @param springFactoriesLoader the {@link SpringFactoriesLoader} used to load\n\t * {@link ProtocolResolver ProtocolResolvers}\n\t * @return a {@link ResourceLoader} instance\n\t * @since 3.4.0\n\t */",
            "\t/**\n\t * 返回委托给给定 resource loader 并支持 {@code spring.factories} 中额外\n\t * {@link ProtocolResolver ProtocolResolvers} 的 {@link ResourceLoader}。\n\t *\n\t * @param resourceLoader 委托 resource loader\n\t * @param springFactoriesLoader 用于加载 {@link ProtocolResolver ProtocolResolvers} 的\n\t * {@link SpringFactoriesLoader}\n\t * @return {@link ResourceLoader} 实例\n\t * @since 3.4.0\n\t */",
        ),
        (
            "\t/**\n\t * Internal {@link ResourceLoader} used to load {@link ApplicationResource}.\n\t */",
            "\t/**\n\t * 用于加载 {@link ApplicationResource} 的内部 {@link ResourceLoader}。\n\t */",
        ),
        (
            "\t/**\n\t * Strategy interface registered in {@code spring.factories} and used by\n\t * {@link ApplicationResourceLoader} to determine the file path of loaded resource\n\t * when it can also be represented as a {@link FileSystemResource}.\n\t *\n\t * @author Phillip Webb\n\t * @since 3.4.5\n\t */",
            "\t/**\n\t * 在 {@code spring.factories} 中注册、由 {@link ApplicationResourceLoader} 使用的策略接口，\n\t * 当已加载资源也可表示为 {@link FileSystemResource} 时确定其文件路径。\n\t *\n\t * @author Phillip Webb\n\t * @since 3.4.5\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Return the {@code path} of the given resource if it can also be represented as\n\t\t * a {@link FileSystemResource}.\n\t\t * @param location the location used to create the resource\n\t\t * @param resource the resource to check\n\t\t * @return the file path of the resource or {@code null} if the it is not possible\n\t\t * to represent the resource as a {@link FileSystemResource}.\n\t\t */",
            "\t\t/**\n\t\t * 若给定资源也可表示为 {@link FileSystemResource}，返回其 {@code path}。\n\t\t *\n\t\t * @param location 创建资源时使用的 location\n\t\t * @param resource 待检查的资源\n\t\t * @return 资源的文件路径；若无法表示为 {@link FileSystemResource} 则返回 {@code null}\n\t\t */",
        ),
        (
            "\t/**\n\t * An application {@link Resource}.\n\t */",
            "\t/**\n\t * 应用 {@link Resource}。\n\t */",
        ),
        (
            "\t/**\n\t * {@link ResourceLoader} decorator that adds support for additional\n\t * {@link ProtocolResolver ProtocolResolvers}.\n\t */",
            "\t/**\n\t * 为额外 {@link ProtocolResolver ProtocolResolvers} 提供支持的 {@link ResourceLoader} 装饰器。\n\t */",
        ),
    ],
    "Base64ProtocolResolver.java": [
        (
            "/**\n * {@link ProtocolResolver} for resources containing base 64 encoded text.\n *\n * @author Scott Frederick\n */",
            "/**\n * 用于包含 base64 编码文本的资源的 {@link ProtocolResolver}。\n *\n * @author Scott Frederick\n */",
        ),
    ],
    "ClassPathResourceFilePathResolver.java": [
        (
            "/**\n * {@link FilePathResolver} for {@link ClassPathResource}.\n *\n * @author Phillip Webb\n */",
            "/**\n * 针对 {@link ClassPathResource} 的 {@link FilePathResolver} 实现。\n * 当 location 非 classpath URL 前缀时，将 classpath 资源解析为其 location 路径。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "ProtocolResolverApplicationContextInitializer.java": [
        (
            "/**\n * {@link ApplicationContextInitializer} that adds all {@link ProtocolResolver\n * ProtocolResolvers} registered in a {@code spring.factories} file.\n *\n * @author Scott Frederick\n */",
            "/**\n * 将 {@code spring.factories} 文件中注册的所有 {@link ProtocolResolver ProtocolResolvers}\n * 添加到应用上下文的 {@link ApplicationContextInitializer}。\n *\n * @author Scott Frederick\n */",
        ),
    ],
    "AbstractJsonParser.java": [
        (
            "/**\n * Base class for parsers wrapped or implemented in this package.\n *\n * @author Anton Telechev\n * @author Phillip Webb\n * @since 2.0.1\n */",
            "/**\n * 本包中包装或实现的解析器基类。\n *\n * @author Anton Telechev\n * @author Phillip Webb\n * @since 2.0.1\n */",
        ),
    ],
    "AppendableByteArray.java": [
        (
            "/**\n * {@link Appendable} implementation that can be used to return a byte array. Designed to\n * reduce memory pressure for {@link WritableJson#toByteArray(Charset)} by using a single\n * cached buffer scoped to the thread.\n *\n * @author Phillip Webb\n */",
            "/**\n * 可用于返回 byte array 的 {@link Appendable} 实现。\n * 通过线程作用域的单一缓存缓冲区，降低 {@link WritableJson#toByteArray(Charset)} 的内存压力。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "BasicJsonParser.java": [
        (
            "/**\n * Really basic JSON parser for when you have nothing else available. Comes with some\n * limitations with respect to the JSON specification (e.g. only supports String values),\n * so users will probably prefer to have a library handle things instead (Jackson or Snake\n * YAML are supported).\n *\n * @author Dave Syer\n * @author Jean de Klerk\n * @author Stephane Nicoll\n * @since 1.2.0\n * @see JsonParserFactory\n */",
            "/**\n * 在无其他可用实现时使用的极简 JSON 解析器。\n * 相对 JSON 规范存在若干限制（例如仅支持 String 值），\n * 用户通常更倾向使用库（支持 Jackson 或 Snake YAML）。\n *\n * @author Dave Syer\n * @author Jean de Klerk\n * @author Stephane Nicoll\n * @since 1.2.0\n * @see JsonParserFactory\n */",
        ),
    ],
    "GsonJsonParser.java": [
        (
            "/**\n * Thin wrapper to adapt {@link Gson} to a {@link JsonParser}.\n *\n * @author Dave Syer\n * @author Jean de Klerk\n * @since 1.2.0\n * @see JsonParserFactory\n */",
            "/**\n * 将 {@link Gson} 适配为 {@link JsonParser} 的薄包装实现。\n * 通过 {@link JsonParserFactory} 在 classpath 存在 Gson 时选用。\n *\n * @author Dave Syer\n * @author Jean de Klerk\n * @since 1.2.0\n * @see JsonParserFactory\n */",
        ),
    ],
    "JacksonJsonParser.java": [
        (
            "/**\n * Thin wrapper to adapt Jackson 3 {@link JsonMapper} to {@link JsonParser}.\n *\n * @author Dave Syer\n * @since 1.0.0\n * @see JsonParserFactory\n */",
            "/**\n * 将 Jackson 3 {@link JsonMapper} 适配为 {@link JsonParser} 的薄包装。\n *\n * @author Dave Syer\n * @since 1.0.0\n * @see JsonParserFactory\n */",
        ),
        (
            "\t/**\n\t * Creates an instance with the specified {@link JsonMapper}.\n\t * @param jsonMapper the JSON mapper to use\n\t */",
            "\t/**\n\t * 使用指定 {@link JsonMapper} 创建实例。\n\t *\n\t * @param jsonMapper 使用的 JSON mapper\n\t */",
        ),
        (
            "\t/**\n\t * Creates an instance with a default {@link JsonMapper} that is created lazily.\n\t */",
            "\t/**\n\t * 创建实例，使用延迟创建的默认 {@link JsonMapper}。\n\t */",
        ),
    ],
    "JsonParseException.java": [
        (
            "/**\n * {@link IllegalArgumentException} thrown when source JSON is invalid.\n *\n * @author Anton Telechev\n * @author Phillip Webb\n * @since 2.0.1\n */",
            "/**\n * 源 JSON 无效或无法解析时抛出的 {@link IllegalArgumentException}。\n * 默认消息为 \"Cannot parse JSON\"。\n *\n * @author Anton Telechev\n * @author Phillip Webb\n * @since 2.0.1\n */",
        ),
    ],
    "JsonParser.java": [
        (
            "/**\n * Parser that can read JSON formatted strings into {@link Map}s or {@link List}s.\n *\n * @author Dave Syer\n * @since 1.0.0\n * @see JsonParserFactory\n * @see BasicJsonParser\n * @see JacksonJsonParser\n * @see GsonJsonParser\n */",
            "/**\n * 可将 JSON 格式字符串读入 {@link Map} 或 {@link List} 的解析器。\n *\n * @author Dave Syer\n * @since 1.0.0\n * @see JsonParserFactory\n * @see BasicJsonParser\n * @see JacksonJsonParser\n * @see GsonJsonParser\n */",
        ),
        (
            "\t/**\n\t * Parse the specified JSON string into a Map.\n\t * @param json the JSON to parse\n\t * @return the parsed JSON as a map\n\t * @throws JsonParseException if the JSON cannot be parsed\n\t */",
            "\t/**\n\t * 将指定 JSON 字符串解析为 Map。\n\t *\n\t * @param json 待解析的 JSON\n\t * @return 解析结果为 map\n\t * @throws JsonParseException 若 JSON 无法解析\n\t */",
        ),
        (
            "\t/**\n\t * Parse the specified JSON string into a List.\n\t * @param json the JSON to parse\n\t * @return the parsed JSON as a list\n\t * @throws JsonParseException if the JSON cannot be parsed\n\t */",
            "\t/**\n\t * 将指定 JSON 字符串解析为 List。\n\t *\n\t * @param json 待解析的 JSON\n\t * @return 解析结果为 list\n\t * @throws JsonParseException 若 JSON 无法解析\n\t */",
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
    if ok == len(BATCH_FILES) and not failures:
        subprocess.run(
            [
                sys.executable,
                str(ROOT / "scripts/mark_batch_done.py"),
                "--project",
                "springboot",
                "--version",
                "4.1.0",
                "--note",
                "wave10b env/info/io/json [20:40]",
                *BATCH_FILES,
            ],
            check=True,
        )
        batch_path = QUEUE / "batch.json"
        batch = json.loads(batch_path.read_text(encoding="utf-8"))
        done_path = QUEUE / "done.txt"
        pending_path = QUEUE / "pending.txt"
        batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
        batch["remaining_pending"] = len(
            [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
        )
        batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"Marked {ok} files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
