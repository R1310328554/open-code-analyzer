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

package com.alibaba.nacos.api.remote.response;

/**
 * 服务端连接校验响应。
 *
 * <p>客户端发起连接探测后，服务端返回分配的 {@link #connectionId} 及是否支持能力协商（{@link #supportAbilityNegotiation}）。</p>
 *
 * @author liuzunfei
 * @version $Id: ServerCheckResponse.java, v 0.1 2020年07月22日 8:37 PM liuzunfei Exp $
 */
public class ServerCheckResponse extends Response {
    
    /** 服务端为该连接分配的唯一标识。 */
    private String connectionId;
    
    /** 服务端是否支持客户端能力协商。 */
    private boolean supportAbilityNegotiation;
    
    /** 无参构造，供序列化框架使用。 */
    public ServerCheckResponse() {
        
    }
    
    /**
     * 构造连接校验响应。
     *
     * @param connectionId               连接 ID
     * @param supportAbilityNegotiation  是否支持能力协商
     */
        this.connectionId = connectionId;
        this.supportAbilityNegotiation = supportAbilityNegotiation;
    }
    
    /** 返回连接 ID。 */
    public String getConnectionId() {
        return connectionId;
    }
    
    /** 设置连接 ID。 */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
    
    /** 返回服务端是否支持能力协商。 */
    public boolean isSupportAbilityNegotiation() {
        return supportAbilityNegotiation;
    }
    
    /** 设置是否支持能力协商。 */
    public void setSupportAbilityNegotiation(boolean supportAbilityNegotiation) {
        this.supportAbilityNegotiation = supportAbilityNegotiation;
    }
}
