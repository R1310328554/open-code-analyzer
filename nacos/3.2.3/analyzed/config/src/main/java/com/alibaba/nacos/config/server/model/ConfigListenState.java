/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.model;

/**
 * 配置监听比对状态：持有客户端已知 MD5 及是否发生命名空间迁移标志。
 * 长轮询 listen 流程中用于判断是否需要推送变更。
 * The type Config listen state.
 *
 * @author Sunrisea
 */
public class ConfigListenState {
    
    /** 客户端当前持有的配置内容 MD5 */
    private String md5;
    
    /** 是否因命名空间迁移需强制刷新 */
    private boolean namespaceTransfer;
    
    /**
     * 以已知 MD5 构造监听状态。
     *
     * @param md5 客户端 MD5
     */
    public ConfigListenState(String md5) {
        this.md5 = md5;
    }
    
    /**
     * 是否发生命名空间迁移。
     * Is namespace transfer boolean.
     *
     * @return the boolean
     */
    public boolean isNamespaceTransfer() {
        return namespaceTransfer;
    }
    
    /**
     * 设置命名空间迁移标志。
     * Sets namespace transfer.
     *
     * @param namespaceTransfer the namespace transfer
     */
    public void setNamespaceTransfer(boolean namespaceTransfer) {
        this.namespaceTransfer = namespaceTransfer;
    }
    
    /**
     * 获取客户端 MD5。
     * Gets md 5.
     *
     * @return the md 5
     */
    public String getMd5() {
        return md5;
    }
    
    /**
     * 设置客户端 MD5。
     * Sets md 5.
     *
     * @param md5 the md 5
     */
    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
