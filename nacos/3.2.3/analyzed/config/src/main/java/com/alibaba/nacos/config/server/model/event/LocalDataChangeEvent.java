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

package com.alibaba.nacos.config.server.model.event;

import com.alibaba.nacos.common.notify.Event;

/**
 * 本地内存数据变更事件：单机模式下配置缓存更新后发布，
 * 仅携带 groupKey，供本节点长轮询推送线程唤醒等待客户端。
 * LocalDataChangeEvent.
 *
 * @author Nacos
 */
public class LocalDataChangeEvent extends Event {
    
    /** 发生变更的配置 groupKey（dataId+group+tenant） */
    public final String groupKey;
    
    /**
     * @param groupKey 变更配置的 groupKey
     */
    public LocalDataChangeEvent(String groupKey) {
        this.groupKey = groupKey;
    }
    
}
