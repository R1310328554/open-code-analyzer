/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.environment.spi;

import java.util.Map;
import java.util.Set;

/**
 * 自定义环境变量插件 SPI 接口。
 *
 * <p>实现类可声明关注的配置键，并在启动时对相应值进行转换或覆盖。</p>
 *
 * @author : huangtianhui
 */
public interface CustomEnvironmentPluginService {
    
    /**
     * 根据输入配置计算自定义值。
     *
     * @param property property key value
     * @return custom key value
     */
    Map<String, Object> customValue(Map<String, Object> property);
    
    /**
     * 返回本插件关注的配置键集合。
     *
     * @return propertyKey property Key
     */
    Set<String> propertyKey();
    
    /**
     * 返回插件执行优先级，数值越大优先级越高。
     *
     * @return order
     */
    Integer order();
    
    /**
     * 返回插件唯一名称，用于 SPI 注册与日志标识。
     *
     * @return 插件名称
     */
    String pluginName();
}
