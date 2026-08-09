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
package org.redisson.spring.cache;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.cache.metrics.CacheMeterBinderProvider;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 4.x 缓存指标自动配置（{@code org.springframework.boot.cache.metrics.CacheMeterBinderProvider}）。
 * <p>条件与 {@link RedissonCacheStatisticsAutoConfiguration} 相同，绑定器实现为
 * {@link RedissonCacheMeterBinderProviderV4}。
 *
 * @author Craig Andrews
 * @author Nikita Koksharov
 *
 * {@link EnableAutoConfiguration Auto-configuration} for {@link RedissonCacheMeterBinderProvider}
 *
 */
@Configuration
@AutoConfigureAfter(CacheAutoConfiguration.class)
@ConditionalOnBean(CacheManager.class)
@ConditionalOnClass({CacheMeterBinderProvider.class, RedissonCache.class})
public class RedissonCacheStatisticsAutoConfigurationV4 {
    
    /** 注册 Boot 4.x 版 Redisson 缓存 Micrometer 绑定器。 */
    @Bean
    public RedissonCacheMeterBinderProviderV4 redissonCacheMeterBinderProvider(){
        return new RedissonCacheMeterBinderProviderV4();
    }
    
}
