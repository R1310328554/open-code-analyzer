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
package com.alibaba.csp.sentinel.dashboard.datasource.entity.rule;

import java.util.Date;

import com.alibaba.csp.sentinel.slots.block.Rule;

/**
 * Dashboard 规则实体通用接口。
 * <p>所有规则类型实体均绑定 app/ip/port 元数据，并可转换为客户端 {@link Rule}。
 *
 * @author leyou
 */
public interface RuleEntity {

    /** @return Dashboard 侧规则 id */
    Long getId();

    /** @param id 规则主键 */
    void setId(Long id);

    /** @return 所属应用名 */
    String getApp();

    /** @return 绑定机器 IP */
    String getIp();

    /** @return 绑定机器端口 */
    Integer getPort();

    /** @return 创建时间 */
    Date getGmtCreate();
    
    /** 转换为 Sentinel 客户端 {@link Rule} 实例。 */
    Rule toRule();
}
