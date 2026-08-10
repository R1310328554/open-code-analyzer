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

package com.alibaba.nacos.plugin.control.impl;

import com.alibaba.nacos.plugin.control.connection.ConnectionControlManager;
import com.alibaba.nacos.plugin.control.spi.ControlManagerBuilder;
import com.alibaba.nacos.plugin.control.tps.TpsControlManager;

/**
 * Nacos 默认管控插件构建器。
 *
 * <p>实现 {@link ControlManagerBuilder} SPI， 分别创建连接管控与 TPS 管控管理器实例。</p>
 *
 * @author xiweng.yy
 */
public class NacosControlManagerBuilder implements ControlManagerBuilder {
    
    /** 返回构建器名称 {@code nacos}。 */
    @Override
    public String getName() {
        return "nacos";
    }
    
    /** 构建 {@link NacosConnectionControlManager} 实例。 */
    @Override
    public ConnectionControlManager buildConnectionControlManager() {
        return new NacosConnectionControlManager();
    }
    
    /** 构建 {@link NacosTpsControlManager} 实例。 */
    @Override
    public TpsControlManager buildTpsControlManager() {
        return new NacosTpsControlManager();
    }
}
