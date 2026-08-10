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

package com.alibaba.nacos.naming.core.v2.client.factory.impl;

import com.alibaba.nacos.naming.constants.ClientConstants;
import com.alibaba.nacos.naming.core.v2.client.ClientAttributes;
import com.alibaba.nacos.naming.core.v2.client.factory.ClientFactory;
import com.alibaba.nacos.naming.core.v2.client.impl.IpPortBasedClient;

import static com.alibaba.nacos.naming.constants.ClientConstants.REVISION;

/**
 * 临时（ephemeral）{@link IpPortBasedClient} 工厂。
 *
 * <p>客户端 ID 以 IP:Port#true 形式标识，依赖心跳续期。</p>
 *
 * @author xiweng.yy
 */
public class EphemeralIpPortClientFactory implements ClientFactory<IpPortBasedClient> {
    
    @Override
    public String getType() {
        return ClientConstants.EPHEMERAL_IP_PORT;
    }
    
    /** 创建本机负责的临时 IP:Port 客户端。 */
    @Override
    public IpPortBasedClient newClient(String clientId, ClientAttributes attributes) {
        long revision = attributes.getClientAttribute(REVISION, 0);
        IpPortBasedClient ipPortBasedClient = new IpPortBasedClient(clientId, true, revision);
        ipPortBasedClient.setAttributes(attributes);
        return ipPortBasedClient;
    }
    
    /** 创建从 Distro 同步的临时 IP:Port 客户端。 */
    @Override
    public IpPortBasedClient newSyncedClient(String clientId, ClientAttributes attributes) {
        long revision = attributes.getClientAttribute(REVISION, 0);
        IpPortBasedClient ipPortBasedClient = new IpPortBasedClient(clientId, true, revision);
        ipPortBasedClient.setAttributes(attributes);
        return ipPortBasedClient;
    }
}
