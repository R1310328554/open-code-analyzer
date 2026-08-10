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
 * 采样结果封装：用于长轮询/监听诊断，返回各 groupKey 对应的监听状态快照。
 * SampleResult.
 *
 * @author Nacos
 */
public class SampleResult implements Serializable {
    
    private static final long serialVersionUID = 2587823382317389453L;
    
    /** groupKey → 监听状态描述 的映射（键为 dataId+group+tenant 组合） */
    private Map<String, String> lisentersGroupkeyStatus;
    
    /** @return 各 groupKey 的监听状态映射 */
    public Map<String, String> getLisentersGroupkeyStatus() {
        return lisentersGroupkeyStatus;
    }
    
    /** @param lisentersGroupkeyStatus 监听状态映射 */
    public void setLisentersGroupkeyStatus(Map<String, String> lisentersGroupkeyStatus) {
        this.lisentersGroupkeyStatus = lisentersGroupkeyStatus;
    }
    
}
