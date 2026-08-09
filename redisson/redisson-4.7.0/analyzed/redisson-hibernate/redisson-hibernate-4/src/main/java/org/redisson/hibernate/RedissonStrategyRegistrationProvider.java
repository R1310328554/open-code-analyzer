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
package org.redisson.hibernate;

import java.util.Collections;

import org.hibernate.boot.registry.selector.SimpleStrategyRegistrationImpl;
import org.hibernate.boot.registry.selector.StrategyRegistration;
import org.hibernate.boot.registry.selector.StrategyRegistrationProvider;
import org.hibernate.cache.spi.RegionFactory;

/**
 * Hibernate 启动时注册 Redisson {@link RegionFactory} 策略的 SPI 提供者。
 * 允许在配置中使用 {@code redisson} 短名或完整类名。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonStrategyRegistrationProvider implements StrategyRegistrationProvider {

    /** 向 Hibernate 注册 {@link RedissonRegionFactory} 作为 {@link RegionFactory} 实现。 */
    @Override
    public Iterable<StrategyRegistration> getStrategyRegistrations() {
        return Collections.<StrategyRegistration>singleton(new SimpleStrategyRegistrationImpl(
                        RegionFactory.class,
                        RedissonRegionFactory.class,
                        "redisson",
                        RedissonRegionFactory.class.getName(),
                        RedissonRegionFactory.class.getSimpleName()));
    }

}
