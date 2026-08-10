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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.copilot.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Copilot 模块自动配置入口，通过 {@code AutoConfiguration.imports} 加载。
 * <p>{@link ComponentScan} 确保 copilot 包下组件在独立控制台等模式下也能注册为 Spring Bean。</p>
 * <p>启用条件：</p>
 * <ul>
 *     <li>{@code nacos.copilot.enabled} 不为 {@code false}（默认 {@code true}）</li>
 *     <li>部署类型不为 {@code server}</li>
 * </ul>
 * Copilot auto-configuration entry point, loaded via {@code AutoConfiguration.imports}.
 *
 * @author nacos
 */
@Configuration
@ConditionalOnProperty(name = "nacos.copilot.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnExpression("'${nacos.deployment.type:merged}' != 'server'")
@ComponentScan(basePackages = "com.alibaba.nacos.copilot")
public class CopilotConfiguration {
}
