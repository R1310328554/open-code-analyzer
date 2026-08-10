/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.remote.tls;

import com.alibaba.nacos.core.remote.BaseRpcServer;

/**
 * RPC 服务端 SSL 上下文刷新 SPI：监听 {@link BaseRpcServer} 并在证书/配置变更时触发协商器重载。
 * ssl context refresher spi holder.
 *
 * @author liuzunfei
 * @version $Id: RequestFilters.java, v 0.1 2023年03月17日 12:00 PM liuzunfei Exp $
 */
public interface RpcServerSslContextRefresher {
    
    /**
     * 注册对 RPC 服务的 SSL 上下文变更监听并返回感知回调。
     * listener current rpc server and do something on ssl context change.
     *
     * @param baseRpcServer rpc server.
     * @return SSL 上下文变更感知器
     */
    SslContextChangeAware refresh(BaseRpcServer baseRpcServer);
    
    /**
     * 返回刷新器名称，用于 SPI 区分 SDK/Cluster 等实现。
     * refresher name.
     *
     * @return 刷新器名称
     */
    String getName();
}
