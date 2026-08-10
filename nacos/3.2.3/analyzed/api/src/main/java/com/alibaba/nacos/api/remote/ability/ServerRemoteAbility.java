/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.remote.ability;

import java.io.Serializable;
import java.util.Objects;

/**
 * Nacos 服务端远程能力描述，用于连接协商与能力上报。
 *
 * <p>服务端在连接建立时向客户端声明 gRPC 连接支持与指标上报开关。</p>
 *
 * @author liuzunfei
 * @version $Id: ServerRemoteAbility.java, v 0.1 2021年01月24日 00:09 AM liuzunfei Exp $
 */
public class ServerRemoteAbility implements Serializable {
    
    private static final long serialVersionUID = -3069795759506428390L;
    
    /** 服务端是否支持 gRPC 远程连接。 */
    private boolean supportRemoteConnection;
    
    /** 是否启用 gRPC 指标上报（默认 {@code true}）。 */
    private boolean grpcReportEnabled = true;
    
    /** 返回是否支持远程连接。 */
    public boolean isSupportRemoteConnection() {
        return this.supportRemoteConnection;
    }
    
    /** 设置是否支持远程连接。 */
    public void setSupportRemoteConnection(boolean supportRemoteConnection) {
        this.supportRemoteConnection = supportRemoteConnection;
    }
    
    /** 返回 gRPC 指标上报是否开启。 */
    public boolean isGrpcReportEnabled() {
        return grpcReportEnabled;
    }
    
    /** 设置 gRPC 指标上报开关。 */
    public void setGrpcReportEnabled(boolean grpcReportEnabled) {
        this.grpcReportEnabled = grpcReportEnabled;
    }
    
    /** 按远程连接与 gRPC 上报能力比较相等性。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerRemoteAbility that = (ServerRemoteAbility) o;
        return supportRemoteConnection == that.supportRemoteConnection
            && grpcReportEnabled == that.grpcReportEnabled;
    }
    
    /** 基于能力字段计算哈希码。 */
    @Override
    public int hashCode() {
        return Objects.hash(supportRemoteConnection, grpcReportEnabled);
    }
}
