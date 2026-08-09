/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.spring.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code spring.redis.redisson.*} 扩展属性：内联 YAML 或外部配置文件路径。
 * <p>与 Spring Boot 标准 {@code spring.redis.*} 并存；二者同时存在时 YAML/文件优先。
 *
 * @author Nikita Koksharov
 * @author AnJia (https://anjia0532.github.io/)
 */
@ConfigurationProperties(prefix = "spring.redis.redisson")
public class RedissonProperties {

    /** 内联 Redisson YAML 配置字符串。 */
    private String config;

    /** Redisson 配置文件路径（Spring {@link org.springframework.core.io.Resource} 格式）。 */
    private String file;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
