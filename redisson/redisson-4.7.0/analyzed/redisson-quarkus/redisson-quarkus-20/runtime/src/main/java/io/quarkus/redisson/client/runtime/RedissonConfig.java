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
package io.quarkus.redisson.client.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.Map;

/**
 * Quarkus {@code quarkus.redisson.*} 运行时配置映射。
 * <p>各方法返回扁平化属性 Map，供 {@link PropertiesConvertor} 转为 Redisson YAML。
 *
 * @author Nikita Koksharov
 */
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus")
public interface RedissonConfig {

    /** 通用 Redisson 参数（{@code quarkus.redisson.*}）。 */
    /**
     * Common params
     *
     * @return params
     */
    @WithName("redisson")
    Map<String, String> params();

    /** 单节点模式配置（{@code quarkus.redisson.single-server-config.*}）。 */
    /**
     * Single server params
     *
     * @return params
     */
    @WithName("redisson.single-server-config")
    Map<String, String> singleServerConfig();

    /** 集群模式配置（{@code quarkus.redisson.cluster-servers-config.*}）。 */
    /**
     * Cluster servers params
     *
     * @return params
     */
    @WithName("redisson.cluster-servers-config")
    Map<String, String> clusterServersConfig();

    /** 哨兵模式配置（{@code quarkus.redisson.sentinel-servers-config.*}）。 */
    /**
     * Sentinel servers params
     *
     * @return params
     */
    @WithName("redisson.sentinel-servers-config")
    Map<String, String> sentinelServersConfig();

    /** 复制模式配置（{@code quarkus.redisson.replicated-servers-config.*}）。 */
    /**
     * Replicated servers params
     *
     * @return params
     */
    @WithName("redisson.replicated-servers-config")
    Map<String, String> replicatedServersConfig();

    /** 主从模式配置（{@code quarkus.redisson.master-slave-servers-config.*}）。 */
    /**
     * Master and slave servers params
     *
     * @return params
     */
    @WithName("redisson.master-slave-servers-config")
    Map<String, String> masterSlaveServersConfig();

}
