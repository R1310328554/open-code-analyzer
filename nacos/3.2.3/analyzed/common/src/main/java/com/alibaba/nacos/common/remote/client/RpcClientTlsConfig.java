/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.common.remote.client;

import com.alibaba.nacos.common.remote.TlsConfig;

/**
 * SDK 侧 RPC 客户端 TLS 配置：继承 {@link TlsConfig}，无额外字段，
 * 由 {@link RpcClientTlsConfigFactory} 从 Properties 填充。
 * 用于 {@link RpcClientFactory} 创建 {@link RpcClient} 时传入安全传输参数。
 * gRPC config for sdk.
 *
 * @author githubcheng2978
 */
/** SDK 客户端 TLS 配置占位类，复用父类全部证书与协议字段 */
public class RpcClientTlsConfig extends TlsConfig {
}
