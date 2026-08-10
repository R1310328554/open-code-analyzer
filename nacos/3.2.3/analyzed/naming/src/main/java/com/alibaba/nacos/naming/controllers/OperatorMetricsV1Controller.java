/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.controllers;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.core.controller.compatibility.Compatibility;
import com.alibaba.nacos.naming.cluster.ServerStatus;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * v1 运维指标接口的最小兼容控制器（仅返回 status）。
 *
 * <p>仅当 classpath 中不存在 api-legacy-adapter 时加载；否则由旧版 {@code OperatorController} 提供完整指标。完整指标请使用 v3 API。</p>
 *
 * @author xiweng.yy
 */
@RestController
@RequestMapping({
    UtilsAndCommons.NACOS_NAMING_CONTEXT + UtilsAndCommons.NACOS_NAMING_OPERATOR_CONTEXT,
    UtilsAndCommons.NACOS_NAMING_CONTEXT + "/ops"})
@ConditionalOnMissingClass("com.alibaba.nacos.legacy.adapter.naming.OperatorController")
public class OperatorMetricsV1Controller {
    
    public OperatorMetricsV1Controller() {
    }
    
    /**
     * 获取运维指标（仅返回 status 字段）。
     *
     * <p>供旧版客户端兼容；完整指标见 v3 管理 API。</p>
     */
    @Since("3.2.0")
    @GetMapping("/metrics")
    @Compatibility(apiType = ApiType.OPEN_API,
        alternatives = "GET ${contextPath:nacos}/v3/admin/ns/ops/metrics")
    public ObjectNode metrics() {
        ObjectNode result = JacksonUtils.createEmptyJsonNode();
        result.put("status", ServerStatus.UP.name());
        return result;
    }
}
