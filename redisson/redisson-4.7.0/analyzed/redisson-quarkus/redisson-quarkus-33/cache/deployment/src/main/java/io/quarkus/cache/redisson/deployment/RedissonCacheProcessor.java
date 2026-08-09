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
package io.quarkus.cache.redisson.deployment;

import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.cache.CompositeCacheKey;
import io.quarkus.cache.deployment.CacheManagerInfoBuildItem;
import io.quarkus.cache.redisson.runtime.RedissonCacheBuildRecorder;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import org.redisson.api.RedissonClient;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

/**
 * Quarkus Cache 扩展部署处理器：注册 Redisson 缓存管理器与 Native 反射。
 * <p>构建阶段通过 {@link RedissonCacheBuildRecorder} 提供 {@link CacheManagerInfo}，
 * 并确保 {@link RedissonClient} 为不可移除 Bean。
 */
public class RedissonCacheProcessor {

    /** 运行时初始化：向 Quarkus Cache SPI 注册 Redisson 缓存管理器供应商。 */
    @BuildStep
    @Record(RUNTIME_INIT)
    CacheManagerInfoBuildItem cacheManagerInfo(RedissonCacheBuildRecorder recorder) {
        return new CacheManagerInfoBuildItem(recorder.getCacheManagerSupplier());
    }

    /** 标记 {@link RedissonClient} 为不可移除 Bean，避免 Arc 优化剔除依赖。 */
    @BuildStep
    UnremovableBeanBuildItem redissonClientUnremoveable() {
        return UnremovableBeanBuildItem.beanTypes(RedissonClient.class);
    }

    /** 为 {@link CompositeCacheKey} 注册 Native Image 方法反射。 */
    @BuildStep
    void nativeImage(BuildProducer<ReflectiveClassBuildItem> producer) {
        producer.produce(ReflectiveClassBuildItem.builder(CompositeCacheKey.class).methods(true).build());
    }

}