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

package com.alibaba.nacos.client.config.listener.impl;

import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.api.config.listener.AbstractListener;

/**
 * 配置细粒度变更监听器抽象基类。
 *
 * <p>子类实现 {@link #receiveConfigChange(ConfigChangeEvent)} 接收按 key 解析后的变更事件；
 * 本类对 {@link #receiveConfigInfo(String)} 留空，由 {@link com.alibaba.nacos.client.config.impl.CacheData}
 * 在比对 MD5 后调用 {@code receiveConfigChange}。</p>
 *
 * @author rushsky518
 */
public abstract class AbstractConfigChangeListener extends AbstractListener {
    
    /**
     * 处理配置细粒度变更事件。
     *
     * @param event 包含变更项映射的 {@link ConfigChangeEvent}
     */
    public abstract void receiveConfigChange(final ConfigChangeEvent event);
    
    /**
     * 整段配置回调留空；细粒度监听走 {@link #receiveConfigChange(ConfigChangeEvent)}。
     *
     * @param configInfo 完整配置内容（未使用）
     */
    @Override
    public void receiveConfigInfo(final String configInfo) {
    }
}
