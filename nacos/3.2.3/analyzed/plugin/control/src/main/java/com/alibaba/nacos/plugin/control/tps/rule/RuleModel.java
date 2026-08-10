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

package com.alibaba.nacos.plugin.control.tps.rule;

/**
 * TPS 限流规则计数模型。
 *
 * <p>定义监控键（如连接 ID、客户端 IP）如何映射到计数器：
 * FUZZY 模式下各键合并计数，PROTO 模式下各键独立计数。</p>
 *
 * @author shiyiyue
 */
public enum RuleModel {
    
    /**
     * 模糊模式：所有监控键共享同一计数器。
     *
     * <p>每个监控键的访问均累加到同一 TPS 计数。</p>
     */
    FUZZY("FUZZY", "every single monitor key will be counted as one counter"),
    
    /**
     * 精确模式：每个监控键使用独立计数器。
     *
     * <p>不同连接或客户端 IP 分别统计 TPS。</p>
     */
    PROTO("PROTO", "every single monitor key will be counted as different counter");
    
    /** 模型标识字符串。 */
    private String model;
    
    /** 模型英文描述（配置序列化保留）。 */
    private String desc;
    
    RuleModel(String model, String desc) {
        this.model = model;
        this.desc = desc;
    }
}
