/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.controller.metrics;

import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 控制器 OpenTelemetry 指标常量：定义 Meter 名称、标签键、指标名及枚举映射。
 */
public class ControllerMetricsConstant {

    /** 指标标签：控制器节点地址。 */
    public static final String LABEL_ADDRESS = "address";
    public static final String LABEL_GROUP = "group";
    public static final String LABEL_PEER_ID = "peer_id";
    public static final String LABEL_AGGREGATION = "aggregation";
    public static final String AGGREGATION_DELTA = "delta";

    /** OpenTelemetry Meter 名称，用于标识控制器指标域。 */
    public static final String OPEN_TELEMETRY_METER_NAME = "controller";

    /** Gauge 指标名：当前节点 Raft/DLedger 角色。 */
    public static final String GAUGE_ROLE = "role";

    /** Gauge 指标名：DLedger 存储目录磁盘占用（单位：字节）。 */
    public static final String GAUGE_DLEDGER_DISK_USAGE = "dledger_disk_usage";

    public static final String GAUGE_ACTIVE_BROKER_NUM = "active_broker_num";

    /** Counter 指标名：控制器 RPC 请求总数。 */
    public static final String COUNTER_REQUEST_TOTAL = "request_total";

    public static final String COUNTER_DLEDGER_OP_TOTAL = "dledger_op_total";

    public static final String COUNTER_ELECTION_TOTAL = "election_total";

    /** Histogram 指标名：请求处理延迟（单位：微秒）。 */
    public static final String HISTOGRAM_REQUEST_LATENCY = "request_latency";

    // unit: us
    public static final String HISTOGRAM_DLEDGER_OP_LATENCY = "dledger_op_latency";

    public static final String LABEL_CLUSTER_NAME = "cluster";

    public static final String LABEL_BROKER_SET = "broker_set";

    public static final String LABEL_REQUEST_TYPE = "request_type";

    public static final String LABEL_REQUEST_HANDLE_STATUS = "request_handle_status";

    public static final String LABEL_DLEDGER_OPERATION = "dledger_operation";

    public static final String LABEL_DLEDGER_OPERATION_STATUS = "dLedger_operation_status";

    public static final String LABEL_ELECTION_RESULT = "election_result";

    /** 控制器请求类型，与 {@link RequestCode} 一一对应。 */
    public enum RequestType {
        CONTROLLER_ALTER_SYNC_STATE_SET(RequestCode.CONTROLLER_ALTER_SYNC_STATE_SET),

        CONTROLLER_ELECT_MASTER(RequestCode.CONTROLLER_ELECT_MASTER),

        CONTROLLER_REGISTER_BROKER(RequestCode.CONTROLLER_REGISTER_BROKER),

        CONTROLLER_GET_REPLICA_INFO(RequestCode.CONTROLLER_GET_REPLICA_INFO),

        CONTROLLER_GET_METADATA_INFO(RequestCode.CONTROLLER_GET_METADATA_INFO),

        CONTROLLER_GET_SYNC_STATE_DATA(RequestCode.CONTROLLER_GET_SYNC_STATE_DATA),

        CONTROLLER_GET_BROKER_EPOCH_CACHE(RequestCode.GET_BROKER_EPOCH_CACHE),

        CONTROLLER_NOTIFY_BROKER_ROLE_CHANGED(RequestCode.NOTIFY_BROKER_ROLE_CHANGED),

        CONTROLLER_BROKER_HEARTBEAT(RequestCode.BROKER_HEARTBEAT),

        CONTROLLER_UPDATE_CONTROLLER_CONFIG(RequestCode.UPDATE_CONTROLLER_CONFIG),

        CONTROLLER_GET_CONTROLLER_CONFIG(RequestCode.GET_CONTROLLER_CONFIG),

        CONTROLLER_CLEAN_BROKER_DATA(RequestCode.CLEAN_BROKER_DATA),

        CONTROLLER_GET_NEXT_BROKER_ID(RequestCode.CONTROLLER_GET_NEXT_BROKER_ID),

        CONTROLLER_APPLY_BROKER_ID(RequestCode.CONTROLLER_APPLY_BROKER_ID);

        /** 对应的 Remoting 请求码。 */
        private final int code;

        /** 绑定请求码枚举常量。 */
        RequestType(int code) {
            this.code = code;
        }

        /** 按请求码返回枚举名的小写形式，未匹配时返回 null。 */
        public static String getLowerCaseNameByCode(int code) {
            for (RequestType requestType : RequestType.values()) {
                if (requestType.code == code) {
                    return requestType.name();
                }
            }
            return null;
        }
    }

    /** 控制器请求处理结果：成功、失败或超时。 */
    public enum RequestHandleStatus {
        SUCCESS,
        FAILED,
        TIMEOUT;

        public String getLowerCaseName() {
            return this.name().toLowerCase();
        }
    }

    public enum DLedgerOperation {
        APPEND;

        public String getLowerCaseName() {
            return this.name().toLowerCase();
        }
    }

    public enum DLedgerOperationStatus {
        SUCCESS,
        FAILED,
        TIMEOUT;

        public String getLowerCaseName() {
            return this.name().toLowerCase();
        }
    }

    /** 主节点选举结果枚举，用于选举指标标签。 */
    public enum ElectionResult {
        NEW_MASTER_ELECTED,
        KEEP_CURRENT_MASTER,
        NO_MASTER_ELECTED;

        public String getLowerCaseName() {
            return this.name().toLowerCase();
        }
    }

}
