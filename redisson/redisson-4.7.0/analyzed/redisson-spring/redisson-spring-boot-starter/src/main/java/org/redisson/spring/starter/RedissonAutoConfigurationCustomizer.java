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

import org.redisson.config.Config;

/**
 * 回调接口：允许 Bean 在自动配置阶段定制 {@link org.redisson.api.RedissonClient}。
 * <p>实现类注册为 Spring Bean 后，会在 {@code RedissonClient} 创建前依次调用 {@link #customize(Config)}。
 *
 * @author Nikos Kakavas (https://github.com/nikakis)
 */
@FunctionalInterface
public interface RedissonAutoConfigurationCustomizer {

    /**
     * 定制 Redisson 客户端配置。
     * @param configuration 待修改的 {@link Config}
     */
    void customize(Config configuration);

}
