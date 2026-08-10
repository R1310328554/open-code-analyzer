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

package com.alibaba.nacos.api.remote.request;

/**
 * 长连接重载/迁移请求。
 *
 * <p>集群扩缩容或节点下线时，通知客户端将连接迁移至 {@link #reloadServer}；{@link #reloadCount} 记录已重载次数。</p>
 *
 * @author liuzunfei
 * @version $Id: ServerReloadRequest.java, v 0.1 2020年11月09日 4:35 PM liuzunfei Exp $
 */
public class ServerReloadRequest extends InternalRequest {
    
    /** 已执行的重载次数。 */
    int reloadCount = 0;
    
    /** 目标重连服务端地址。 */
    String reloadServer;
    
    /** 返回已重载次数。 */
    public int getReloadCount() {
        return reloadCount;
    }
    
    /**
     * 设置已重载次数。
     *
     * @param reloadCount 重载计数
     */
    public void setReloadCount(int reloadCount) {
        this.reloadCount = reloadCount;
    }
    
    /** 返回目标重连服务端地址。 */
    public String getReloadServer() {
        return reloadServer;
    }
    
    /** 设置目标重连服务端地址。 */
    public void setReloadServer(String reloadServer) {
        this.reloadServer = reloadServer;
    }
}
