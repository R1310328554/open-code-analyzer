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
 * SSL 上下文变更感知接口，供 RPC 服务端在证书热更新时回调。
 *
 * <p>实现类通常与 {@link RpcServerSslContextRefresher} 配合，在 {@link #onSslContextChange()} 中重建 gRPC SSL 上下文。</p>
 *
 * @author liuzunfei
 * @version $Id: RequestFilters.java, v 0.1 2023年03月17日 12:00 PM liuzunfei Exp $
 */
public interface SslContextChangeAware {
    
    /**
     * 初始化 RPC 服务端的 SSL 上下文。
     *
     * @param baseRpcServer 待绑定的 RPC 服务端实例
     */
    void init(BaseRpcServer baseRpcServer);
    
    /** SSL 上下文发生变更时触发，通常用于热重载证书。 */
    void onSslContextChange();
    
    /** 关闭并清理 SSL 上下文相关资源。 */
    void shutdown();
}
