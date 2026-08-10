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

package com.alibaba.nacos.config.server.model;

import java.io.Serializable;
import java.util.Map;

/**
 * 按 groupKey 聚合的监听器采集状态：含采集结果码与各键监听状态 Map。
 * 管理端查询客户端订阅分布时使用（类名保留历史拼写 Listenser）。
 * GroupkeyListenserStatus.
 *
 * @author Nacos
 */
public class GroupkeyListenserStatus implements Serializable {
    
    private static final long serialVersionUID = -2094829323598842474L;
    
    /** 采集任务状态码（成功/部分失败等） */
    private int collectStatus;
    
    /** groupKey 到监听状态字符串的映射 */
    private Map<String, String> lisentersGroupkeyStatus;
    
    /** 获取采集状态码 */
    public int getCollectStatus() {
        return collectStatus;
    }
    
    /** 设置采集状态码 */
    public void setCollectStatus(int collectStatus) {
        this.collectStatus = collectStatus;
    }
    
    /** 获取各 groupKey 监听状态 Map */
    public Map<String, String> getLisentersGroupkeyStatus() {
        return lisentersGroupkeyStatus;
    }
    
    /** 设置 groupKey 监听状态 Map */
    public void setLisentersGroupkeyStatus(Map<String, String> lisentersGroupkeyStatus) {
        this.lisentersGroupkeyStatus = lisentersGroupkeyStatus;
    }
}
