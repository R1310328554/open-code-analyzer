/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.config.listener;

import java.util.concurrent.Executor;

/**
 * 共享配置监听器抽象基类。
 *
 * <p>同一监听器实例可绑定多个 dataId/group，框架通过 {@link #fillContext} 注入当前上下文，
 * 再委托 {@link #innerReceive} 处理具体配置内容。</p>
 *
 * @author Nacos
 */
public abstract class AbstractSharedListener implements Listener {
    
    /** 当前回调对应的配置 dataId。 */
    private volatile String dataId;
    
    /** 当前回调对应的配置 group。 */
    private volatile String group;
    
    /** 由框架调用，填充当前配置标识上下文。 */
    public final void fillContext(String dataId, String group) {
        this.dataId = dataId;
        this.group = group;
    }
    
    @Override
    public final void receiveConfigInfo(String configInfo) {
        innerReceive(dataId, group, configInfo);
    }
    
    @Override
    public Executor getExecutor() {
        return null;
    }
    
    /**
     * 接收配置变更内容。
     *
     * @param dataId     配置 dataId
     * @param group      配置 group
     * @param configInfo 最新配置内容
     */
    public abstract void innerReceive(String dataId, String group, String configInfo);
}
