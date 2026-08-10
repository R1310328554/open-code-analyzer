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

package com.alibaba.nacos.config.server.service.dump.task;

import com.alibaba.nacos.common.task.AbstractDelayTask;

/**
 * 单条配置增量 Dump 延迟任务：按 groupKey、灰度名与最后修改时间
 * 将一条配置从持久层同步到本地缓存；失败时按 1 秒间隔重试。
 * Dump data task.
 *
 * @author Nacos
 */
public class DumpTask extends AbstractDelayTask {
    
    /**
     * 构造单条 Dump 任务。
     *
     * @param groupKey     配置 groupKey（dataId+group+tenant）
     * @param grayName     灰度规则名，正式配置为空
     * @param lastModified 配置最后修改时间戳
     * @param handleIp     触发 Dump 的节点 IP
     */
    public DumpTask(String groupKey, String grayName, long lastModified, String handleIp) {
        this.groupKey = groupKey;
        this.lastModified = lastModified;
        this.handleIp = handleIp;
        this.grayName = grayName;
        // 重试间隔 1 秒，避免持久层短暂不可用时频繁打满
        setTaskInterval(1000L);
    }
    
    @Override
    public void merge(AbstractDelayTask task) {
    }
    
    /** 目标配置的 groupKey，Dump 路由主键。 */
    final String groupKey;
    
    final long lastModified;
    
    final String handleIp;
    
    /** 灰度规则标识；正式配置该字段为空字符串。 */
    final String grayName;
    
    public String getGroupKey() {
        return groupKey;
    }
    
    public long getLastModified() {
        return lastModified;
    }
    
    /** 返回发起 Dump 请求的节点 IP，用于链路追踪。 */
    public String getHandleIp() {
        return handleIp;
    }
    
    public String getGrayName() {
        return grayName;
    }
}
