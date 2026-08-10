/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.config.spi;

import com.alibaba.nacos.plugin.config.constants.ConfigChangeConstants;
import com.alibaba.nacos.plugin.config.constants.ConfigChangeExecuteTypes;
import com.alibaba.nacos.plugin.config.constants.ConfigChangePointCutTypes;
import com.alibaba.nacos.plugin.config.model.ConfigChangeRequest;
import com.alibaba.nacos.plugin.config.model.ConfigChangeResponse;

/**
 * 配置变更插件 SPI 接口。
 *
 * <p>插件实现需声明服务类型、执行时机、切点范围及执行顺序，
 * 在配置发布、删除或导入等变更流程中被框架按序回调。</p>
 *
 * @author liyunfei
 */
public interface ConfigChangePluginService {
    
    /**
     * 执行配置变更插件逻辑。
     *
     * @param configChangeRequest  配置变更请求上下文
     * @param configChangeResponse 配置变更响应，用于回写执行结果
     */
    void execute(ConfigChangeRequest configChangeRequest,
        ConfigChangeResponse configChangeResponse);
    
    /**
     * 返回插件执行时机，参见 {@link ConfigChangeExecuteTypes}。
     *
     * @return 执行类型（切点前或切点后）
     */
    ConfigChangeExecuteTypes executeType();
    
    /**
     * 返回插件服务类型标识，如 webhook、whiteList 等，
     * 需与 {@link ConfigChangeConstants} 中对应配置项保持一致。
     *
     * @return 服务类型字符串
     */
    String getServiceType();
    
    /**
     * 同一切点下多个插件按 order 升序执行，数值越小优先级越高。
     *
     * @return 执行顺序值
     */
    int getOrder();
    
    /**
     * 返回本插件需要拦截的配置变更切点类型数组，参见 {@link ConfigChangePointCutTypes}。
     *
     * <p>
     * 切点类型即对应的配置变更入口方法。
     * </p>
     *
     * @return 需要拦截的切点类型数组
     */
    ConfigChangePointCutTypes[] pointcutMethodNames();
    
}
