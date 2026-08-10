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

package com.alibaba.nacos.plugin.control.spi;

import com.alibaba.nacos.plugin.control.connection.ConnectionControlManager;
import com.alibaba.nacos.plugin.control.tps.TpsControlManager;

/**
 * Nacos 管控插件管理器构建 SPI。
 *
 * <p>各管控实现通过此接口向框架注册名称，并分别提供连接数管控与 TPS 限流管理器的构建能力。</p>
 *
 * @author xiweng.yy
 */
public interface ControlManagerBuilder {
    
    /**
     * 获取插件名称，用于与配置项 {@code controlManagerType} 匹配。
     *
     * @return 插件名称
     */
    String getName();
    
    /**
     * 构建当前插件的 {@link ConnectionControlManager} 实现。
     *
     * @return 连接数管控管理器实例
     */
    ConnectionControlManager buildConnectionControlManager();
    
    /**
     * 构建当前插件的 {@link TpsControlManager} 实现。
     *
     * @return TPS 限流管理器实例
     */
    TpsControlManager buildTpsControlManager();
}
