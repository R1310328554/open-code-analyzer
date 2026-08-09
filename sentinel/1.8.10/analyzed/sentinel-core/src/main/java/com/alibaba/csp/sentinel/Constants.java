/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.EntranceNode;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.util.VersionUtil;

/**
 * Sentinel 通用常量。
 *
 * @author qinan.qn
 * @author youji.zj
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public final class Constants {

    public static final String SENTINEL_VERSION = VersionUtil.getVersion("1.8.10");

    public final static int MAX_CONTEXT_NAME_SIZE = 2000;
    public final static int MAX_SLOT_CHAIN_SIZE = 6000;

    public final static String ROOT_ID = "machine-root";
    public final static String CONTEXT_DEFAULT_NAME = "sentinel_default_context";

    /**
     * 入站总流量的虚拟资源标识（自 1.5.0 起）。
     */
    public final static String TOTAL_IN_RESOURCE_NAME = "__total_inbound_traffic__";

    /**
     * CPU 使用率的虚拟资源标识（自 1.6.1 起）。
     */
    public final static String CPU_USAGE_RESOURCE_NAME = "__cpu_usage__";

    /**
     * 系统负载的虚拟资源标识（自 1.6.1 起）。
     */
    public final static String SYSTEM_LOAD_RESOURCE_NAME = "__system_load__";

    /**
     * 全局 ROOT 统计节点，表示通用父节点。
     */
    public final static DefaultNode ROOT = new EntranceNode(new StringResourceWrapper(ROOT_ID, EntryType.IN),
        new ClusterNode(ROOT_ID, ResourceTypeConstants.COMMON));

    /**
     * 入站流量的全局统计节点，通常用于 {@code SystemRule} 检查。
     */
    public final static ClusterNode ENTRY_NODE = new ClusterNode(TOTAL_IN_RESOURCE_NAME, ResourceTypeConstants.COMMON);

    /**
     * Sentinel 全局开关。
     */
    public static volatile boolean ON = true;

    /**
     * 默认 ProcessorSlot 顺序
     */
    public static final int ORDER_NODE_SELECTOR_SLOT = -10000;
    public static final int ORDER_CLUSTER_BUILDER_SLOT = -9000;
    public static final int ORDER_LOG_SLOT = -8000;
    public static final int ORDER_STATISTIC_SLOT = -7000;
    public static final int ORDER_AUTHORITY_SLOT = -6000;
    public static final int ORDER_SYSTEM_SLOT = -5000;
    public static final int ORDER_FLOW_SLOT = -2000;
    public static final int ORDER_DEFAULT_CIRCUIT_BREAKER_SLOT = -1500;
    public static final int ORDER_DEGRADE_SLOT = -1000;

    private Constants() {}
}
