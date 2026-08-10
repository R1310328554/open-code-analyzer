/*
 *
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
 *
 */

package com.alibaba.nacos.core.remote;

/**
 * 运行时连接踢出器抽象基类，定义心跳超时阈值与负载均衡踢出接口。
 * runtime connection ejector.
 *
 * @author shiyiyue
 */
public abstract class RuntimeConnectionEjector {
    
    /**
     * 连接保活超时阈值（约为客户端 keep-alive 的 4 倍）。
     * 4 times of client keep alive.
     */
    public static final long KEEP_ALIVE_TIME = 20000L;
    
    /**
     * 一次性负载调整目标连接数，用于集群 rebalance。
     * current loader adjust count,only effective once,use to re balance.
     */
    private int loadClient = -1;
    
    /** 负载踢出时的重定向目标地址。 */
    String redirectAddress = null;
    
    /** 关联的连接管理器。 */
    protected ConnectionManager connectionManager;
    
    public RuntimeConnectionEjector() {
    }
    
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }
    
    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    /**
     * 执行运行时连接踢出逻辑。
     * eject runtime connection.
     */
    public abstract void doEject();
    
    public int getLoadClient() {
        return loadClient;
    }
    
    public void setLoadClient(int loadClient) {
        this.loadClient = loadClient;
    }
    
    public String getRedirectAddress() {
        return redirectAddress;
    }
    
    public void setRedirectAddress(String redirectAddress) {
        this.redirectAddress = redirectAddress;
    }
    
    /**
     * 返回踢出器实现名称。
     * get name.
     *
     * @return 踢出器名称
     */
    public abstract String getName();
}
