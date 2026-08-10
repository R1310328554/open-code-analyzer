/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.console.config;

import com.alibaba.nacos.naming.selector.SelectorManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 按 {@code functionMode} 裁剪模块时的补偿配置：例如仅 config 模式仍需 {@link SelectorManager}。
 * Do some config and bean initialize when `functionMode` set. Such as `config` and `naming`.
 *
 * @author xiweng.yy
 */
@Configuration
public class ConsoleFunctionEnabledConfig {
    
    /**
     * 当 functionMode 为 config 时命名模块 Bean 不会加载，但控制台 API 仍需要 {@link SelectorManager} 解析选择器。
     * If `functionMode` set as `config`,
     * the naming module bean will not be loaded, but console api required {@link SelectorManager} to do selector parser.
     *
     * @return {@link SelectorManager} bean
     */
    @Bean
    @ConditionalOnMissingBean
    public SelectorManager selectorManager() {
        return new SelectorManager();
    }
}
