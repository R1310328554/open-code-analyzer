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

package com.alibaba.nacos.config.server.model.gray;

import java.util.Map;

/**
 * 配置灰度规则 SPI 接口：定义匹配、有效性、类型版本及优先级等契约。
 * 各实现通过 {@link com.alibaba.nacos.common.spi.NacosServiceLoader} 注册至 {@link GrayRuleManager}。
 * gray rule.
 *
 * @author rong
 */
public interface GrayRule {
    
    /**
    * 判断连接标签是否命中灰度规则。
    *
    * @date 2024/3/14
    * @param labels 连接侧标签 Map
    * @return 命中返回 true，否则 false
    */
    boolean match(Map<String, String> labels);
    
    /**
    * 规则是否有效（表达式可解析且语义合法）。
    *
    * @date 2024/3/14
    * @return 有效返回 true
    */
    boolean isValid();
    
    /**
    * 获取规则类型标识。
    *
    * @date 2024/3/14
    * @return 灰度规则 type
    */
    String getType();
    
    /**
    * 获取规则版本号。
    *
    * @date 2024/3/14
    * @return 灰度规则 version
    */
    String getVersion();
    
    /**
    * 获取规则匹配优先级。
    *
    * @date 2024/3/14
    * @return 优先级数值
    */
    int getPriority();
    
    /**
    * 获取原始灰度表达式字符串（持久化用）。
    *
    * @date 2024/3/14
    * @return 原始表达式
    */
    String getRawGrayRuleExp();
}
