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

/**
 * RPC 客户端生命周期状态枚举：从等待初始化到运行、不健康、关闭的完整状态机。
 * status of rpc client.
 *
 * @author liuzunfei
 * @version $Id: RpcClientStatus.java, v 0.1 2020年07月14日 3:49 PM liuzunfei Exp $
 */
public enum RpcClientStatus {
    
    /** 等待注入 {@link ServerListFactory} */
    WAIT_INIT(0, "Wait to init server list factory..."),
    
    /** 服务器列表工厂已就绪，等待 start */
    INITIALIZED(1, "Server list factory is ready, wait to starting..."),
    
    /** 正在启动并尝试首次连接 */
    STARTING(2, "Client already staring, wait to connect with server..."),
    
    /** 连接不健康，可能已被服务端关闭，正在重连 */
    UNHEALTHY(3, "Client unhealthy, may closed by server, in reconnecting"),
    
    /** 正常运行，可收发 RPC 请求 */
    RUNNING(4, "Client is running"),
    
    /** 已关闭，不再重连 */
    SHUTDOWN(5, "Client is shutdown");
    
    /** 状态数值编码 */
    int status;
    
    /** 状态可读描述 */
    String desc;
    
    RpcClientStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
