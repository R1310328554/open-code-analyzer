/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.remote.request;

import java.util.Map;

import static com.alibaba.nacos.api.common.Constants.Remote.INTERNAL_MODULE;

/**
 * 连接建立确认请求（Setup ACK）。
 *
 * <p>服务端在收到 {@link com.alibaba.nacos.api.remote.request.ConnectionSetupRequest} 后回复，携带 {@link #abilityTable} 声明服务端能力，通知客户端连接已就绪。</p>
 *
 * @author Daydreamer.
 * @date 2022/7/12 19:21
 **/
public class SetupAckRequest extends ServerRequest {
    
    /** 服务端能力表（特性名 → 是否支持）。 */
    private Map<String, Boolean> abilityTable;
    
    /** 无参构造，供序列化框架使用。 */
    public SetupAckRequest() {
    }
    
    /**
     * 指定服务端能力表构造 ACK。
     *
     * @param abilityTable 能力映射
     */
        this.abilityTable = abilityTable;
    }
    
    /** 返回服务端能力表。 */
    public Map<String, Boolean> getAbilityTable() {
        return abilityTable;
    }
    
    /** 设置服务端能力表。 */
    public void setAbilityTable(Map<String, Boolean> abilityTable) {
        this.abilityTable = abilityTable;
    }
    
    /** {@inheritDoc} 返回内部模块标识。 */
    @Override
    public String getModule() {
        return INTERNAL_MODULE;
    }
}
