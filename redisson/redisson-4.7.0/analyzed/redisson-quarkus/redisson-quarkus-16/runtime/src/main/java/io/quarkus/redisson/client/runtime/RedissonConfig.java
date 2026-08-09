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
 * Quarkus 运行时 Redisson 配置映射（{@code quarkus.redisson.*}）。
 * <p>各方法返回对应部署模式（单机/集群/哨兵等）的配置键值对。
 *
 * @author Nikita Koksharov
 */
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus")
public interface RedissonConfig {

    /** 通用 Redisson 参数。
     * @return params 配置键值对
     */
    @WithName("redisson")
    Map<String, String> params();

    /** 单机模式参数。
     * @return params 配置键值对
     */
    @WithName("redisson.single-server-config")
    Map<String, String> singleServerConfig();

    /** 集群模式参数。
     * @return params 配置键值对
     */
    @WithName("redisson.cluster-servers-config")
    Map<String, String> clusterServersConfig();

    /** 哨兵模式参数。
     * @return params 配置键值对
     */
    @WithName("redisson.sentinel-servers-config")
    Map<String, String> sentinelServersConfig();

    /** 复制模式参数。
     * @return params 配置键值对
     */
    @WithName("redisson.replicated-servers-config")
    Map<String, String> replicatedServersConfig();

    /** 主从模式参数。
     * @return params 配置键值对
     */
    @WithName("redisson.master-slave-servers-config")
    Map<String, String> masterSlaveServersConfig();

}
