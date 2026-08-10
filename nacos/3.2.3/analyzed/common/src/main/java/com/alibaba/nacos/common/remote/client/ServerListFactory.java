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

import java.util.List;

/**
 * 服务端地址列表工厂：为内部 RPC 客户端提供候选节点及切换策略，配合 {@link RpcClient} 实现故障转移与负载均衡。
 * server list factory . use to inner client to connecte and switch servers.
 * @author liuzunfei
 * @version $Id: ServerListFactory.java, v 0.1 2020年07月14日 1:11 PM liuzunfei Exp $
 */
public interface ServerListFactory {
    
    /**
     * 切换到下一个可用服务端并返回其地址（格式 {@code ip:port}）。
     * switch to a new server and get it.
     *
     * @return server " ip:port".
     */
    String genNextServer();
    
    /**
     * 获取当前正在连接或最近使用的服务端地址。
     * get current server.
     * @return server " ip:port".
     */
    String getCurrentServer();
    
    /**
     * 返回完整的服务端地址列表快照。
     * get current server.
     *
     * @return servers.
     */
    List<String> getServerList();
    
}
