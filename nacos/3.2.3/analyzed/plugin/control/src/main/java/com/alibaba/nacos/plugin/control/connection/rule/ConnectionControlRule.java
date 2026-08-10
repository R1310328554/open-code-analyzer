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

package com.alibaba.nacos.plugin.control.connection.rule;

import java.util.HashSet;
import java.util.Set;

/**
 * 连接数管控规则模型，定义全局连接上限与监控 IP 白名单。
 *
 * <p>由 {@link com.alibaba.nacos.plugin.control.rule.parser.ConnectionControlRuleParser}
 * 从持久化 JSON 解析，并由连接管控管理器热更新生效。</p>
 *
 * @author shiyiyue
 */
public class ConnectionControlRule {
    
    /** 仅监控不限流的 IP 集合。 */
    private Set<String> monitorIpList = new HashSet<>();
    
    /** 允许的最大连接总数，-1 表示不限制。 */
    private int countLimit = -1;
    
    public int getCountLimit() {
        return countLimit;
    }
    
    public void setCountLimit(int countLimit) {
        this.countLimit = countLimit;
    }
    
    public Set<String> getMonitorIpList() {
        return monitorIpList;
    }
    
    public void setMonitorIpList(Set<String> monitorIpList) {
        this.monitorIpList = monitorIpList;
    }
}
