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
package org.apache.rocketmq.remoting.metrics;

/** Remoting 层 OpenTelemetry 指标名与标签常量定义。 */
public class RemotingMetricsConstant {
    /** RPC 延迟直方图指标名。 */
    public static final String HISTOGRAM_RPC_LATENCY = "rocketmq_rpc_latency";
    /** 协议类型标签键。 */
    public static final String LABEL_PROTOCOL_TYPE = "protocol_type";
    /** Remoting 请求码标签键。 */
    public static final String LABEL_REQUEST_CODE = "request_code";
    /** Remoting 响应码标签键。 */
    public static final String LABEL_RESPONSE_CODE = "response_code";
    /** 是否为长轮询请求的标签键。 */
    public static final String LABEL_IS_LONG_POLLING = "is_long_polling";
    /** RPC 结果标签键（成功/取消/写失败等）。 */
    public static final String LABEL_RESULT = "result";

    /** 协议类型标签值：Remoting 协议。 */
    public static final String PROTOCOL_TYPE_REMOTING = "remoting";

    /** 结果标签值：oneway 调用。 */
    public static final String RESULT_ONEWAY = "oneway";
    /** 结果标签值：成功。 */
    public static final String RESULT_SUCCESS = "success";
    /** 结果标签值：写通道任务被取消。 */
    public static final String RESULT_CANCELED = "cancelled";
    /** 结果标签值：服务端处理请求失败。 */
    public static final String RESULT_PROCESS_REQUEST_FAILED = "process_request_failed";
    /** 结果标签值：向 Netty 通道写数据失败。 */
    public static final String RESULT_WRITE_CHANNEL_FAILED = "write_channel_failed";

}
