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

package com.alibaba.nacos.core.distributed.raft;

/**
 * JRaft 系统常量：默认参数值与 {@code nacos.core.protocol.raft} 配置键名。
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SuppressWarnings("all")
public final class RaftSysConstants {
    
    // ========= 默认参数值 ========= //
    
    /**
     * {@link RaftSysConstants#RAFT_ELECTION_TIMEOUT_MS}
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    /** 默认选举超时（毫秒）。 */
    public static final int DEFAULT_ELECTION_TIMEOUT = 5000;
    
    /**
     * {@link RaftSysConstants#RAFT_SNAPSHOT_INTERVAL_SECS}
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    /** 默认快照间隔（秒），30 分钟。 */
    public static final int DEFAULT_RAFT_SNAPSHOT_INTERVAL_SECS = 30 * 60;
    
    /**
     * {@link RaftSysConstants#RAFT_CLI_SERVICE_THREAD_NUM}
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    /** 默认 CLI 业务处理线程数。 */
    public static final int DEFAULT_RAFT_CLI_SERVICE_THREAD_NUM = 4;
    
    /**
     * {@link RaftSysConstants#RAFT_READ_INDEX_TYPE}
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    /** 默认线性读策略：ReadOnlySafe（ReadIndex）。 */
    public static final String DEFAULT_READ_INDEX_TYPE = "ReadOnlySafe";
    
    /**
     * {@link RaftSysConstants#RAFT_RPC_REQUEST_TIMEOUT_MS}
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    /** 默认 RPC 请求超时（毫秒）。 */
    public static final int DEFAULT_RAFT_RPC_REQUEST_TIMEOUT_MS = 5000;
    
    /**
     * The maximum size of each file RPC (snapshot copy) request between nodes is 128 K by default 节点之间每次文件 RPC
     * (snapshot拷贝）请求的最大大小，默认为 128 K
     */
    public static final int DEFAULT_MAX_BYTE_COUNT_PER_RPC = 128 * 1024;
    
    /**
     * The maximum number of logs sent from the leader to the followers is 1024 by default 从 leader 往 follower
     * 发送的最大日志个数，默认 1024
     */
    public static final int DEFAULT_MAX_ENTRIES_SIZE = 1024;
    
    /**
     * The maximum body size of the log sent from the leader to the followers is 512K by default 从 leader 往 follower
     * 发送日志的最大 body 大小，默认 512K
     */
    public static final int DEFAULT_MAX_BODY_SIZE = 512 * 1024;
    
    /**
     * The maximum size of the log storage buffer is 256K by default 日志存储缓冲区最大大小，默认256K
     */
    public static final int DEFAULT_MAX_APPEND_BUFFER_SIZE = 256 * 1024;
    
    /**
     * The election timer interval will be a random maximum outside the specified time, 1 second by default
     * 选举定时器间隔会在指定时间之外随机的最大范围，默认1秒
     */
    public static final int DEFAULT_MAX_ELECTION_DELAY_MS = 1000;
    
    /**
     * Specifies the ratio of the election timeout to the heartbeat interval. Heartbeat interval is equal to the
     * electionTimeoutMs/electionHeartbeatFactor, default one of 10 points. 指定选举超时时间和心跳间隔时间之间的比值。心跳间隔等于electionTimeoutMs/electionHeartbeatFactor，默认10分之一。
     */
    public static final int DEFAULT_ELECTION_HEARTBEAT_FACTOR = 10;
    
    /**
     * The tasks submitted to the leader will accumulate one batch into the maximum batch size stored in the log, and 32
     * tasks will be assigned by default 向 leader 提交的任务累积一个批次刷入日志存储的最大批次大小，默认 32 个任务
     */
    public static final int DEFAULT_APPLY_BATCH = 32;
    
    /**
     * Call fsync when necessary when writing log, meta information, and it should always be true 写入日志、元信息的时候必要的时候调用
     * fsync，通常都应该为 true
     */
    public static final boolean DEFAULT_SYNC = true;
    
    /**
     * If fsync is called by writing snapshot/raft information, the default is false. If sync is true, it is better to
     * respect sync 写入 snapshot/raft 元信息是否调用 fsync，默认为 false，在 sync 为 true 的情况下，优选尊重 sync
     */
    public static final boolean DEFAULT_SYNC_META = false;
    
    /**
     * Internal disruptor buffer size, need to be appropriately adjusted for high write throughput applications, default
     * 16384 内部 disruptor buffer 大小，如果是写入吞吐量较高的应用，需要适当调高该值，默认 16384
     */
    public static final int DEFAULT_DISRUPTOR_BUFFER_SIZE = 16384;
    
    /**
     * Whether to enable replicated pipeline request optimization by default 是否启用复制的 pipeline 请求优化，默认打开
     */
    public static final boolean DEFAULT_REPLICATOR_PIPELINE = true;
    
    /**
     * Maximum in-flight requests with pipeline requests enabled, 256 by default 在启用 pipeline 请求情况下，最大 in-flight
     * 请求数，默认256
     */
    public static final int DEFAULT_MAX_REPLICATOR_INFLIGHT_MSGS = 256;
    
    /**
     * Whether LogEntry checksum is enabled 是否启用 LogEntry checksum
     */
    public static final boolean DEFAULT_ENABLE_LOG_ENTRY_CHECKSUM = false;
    
    // ========= 配置键名 ========= //
    
    /** 协议状态标识。 */
    public static final String RAFT_STATE = "raft";
    
    /** Spring 配置前缀。 */
    public static final String RAFT_CONFIG_PREFIX = "nacos.core.protocol.raft";
    
    /**
     * 选举超时配置键（毫秒）。
     * Election timeout in milliseconds
     */
    public static final String RAFT_ELECTION_TIMEOUT_MS = "election_timeout_ms";
    
    /**
     * 快照间隔配置键（秒）。
     * Snapshot interval in seconds
     */
    public static final String RAFT_SNAPSHOT_INTERVAL_SECS = "snapshot_interval_secs";
    
    /**
     * 请求转发失败重试次数配置键。
     * Requested retries
     */
    public static final String REQUEST_FAILOVER_RETRIES = "request_failoverRetries";
    
    /**
     * Raft 内部工作线程数配置键。
     * raft internal worker threads
     */
    public static final String RAFT_CORE_THREAD_NUM = "core_thread_num";
    
    /**
     * CLI 业务请求处理线程数配置键。
     * Number of threads required for raft business request processing
     */
    public static final String RAFT_CLI_SERVICE_THREAD_NUM = "cli_service_thread_num";
    
    /**
     * 线性读策略配置键，默认 read_index。
     * raft linear read strategy, defaults to read_index read
     */
    public static final String RAFT_READ_INDEX_TYPE = "read_index_type";
    
    /**
     * RPC 请求超时配置键（毫秒）。
     * rpc request timeout, default 5 seconds
     */
    public static final String RAFT_RPC_REQUEST_TIMEOUT_MS = "rpc_request_timeout_ms";
    
    /**
     * Maximum size of each file RPC (snapshot copy) request between nodes, default is 128 K
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    public static final String MAX_BYTE_COUNT_PER_RPC = "max_byte_count_per_rpc";
    
    /**
     * Maximum number of logs sent from leader to follower, default is 1024
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    public static final String MAX_ENTRIES_SIZE = "max_entries_size";
    
    /**
     * Maximum body size for sending logs from leader to follower, default is 512K
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    public static final String MAX_BODY_SIZE = "max_body_size";
    
    /**
     * Maximum log storage buffer size, default 256K
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    public static final String MAX_APPEND_BUFFER_SIZE = "max_append_buffer_size";
    
    /**
     * Election timer interval will be a random maximum outside the specified time, default is 1 second
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    public static final String MAX_ELECTION_DELAY_MS = "max_election_delay_ms";
    
    /**
     * Specify the ratio between election timeout and heartbeat interval. Heartbeat interval is equal to
     * electionTimeoutMs/electionHeartbeatFactor，One tenth by default.
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    public static final String ELECTION_HEARTBEAT_FACTOR = "election_heartbeat_factor";
    
    /**
     * The tasks submitted to the leader accumulate the maximum batch size of a batch flush log storage. The default is
     * 32 tasks.
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    public static final String APPLY_BATCH = "apply_batch";
    
    /**
     * Call fsync when necessary when writing logs and meta information, usually should be true
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    public static final String SYNC = "sync";
    
    /**
     * Whether to write snapshot / raft meta-information to call fsync. The default is false. When sync is true, it is
     * preferred to respect sync.
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    public static final String SYNC_META = "sync_meta";
    
    /**
     * Internal disruptor buffer size. For applications with high write throughput, you need to increase this value. The
     * default value is 16384.
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    public static final String DISRUPTOR_BUFFER_SIZE = "disruptor_buffer_size";
    
    /**
     * Whether to enable replication of pipeline request optimization, which is enabled by default
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    public static final String REPLICATOR_PIPELINE = "replicator_pipeline";
    
    /**
     * Maximum number of in-flight requests with pipeline requests enabled, default is 256
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    public static final String MAX_REPLICATOR_INFLIGHT_MSGS = "max_replicator_inflight_msgs";
    
    /**
     * Whether to enable LogEntry checksum
      * <p>JRaft 系统常量；详见类级说明。</p>
     */
    public static final String ENABLE_LOG_ENTRY_CHECKSUM = "enable_log_entry_checksum";
}
