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
package io.quarkus.redisson.client.deployment;

import io.quarkus.builder.item.SimpleBuildItem;

/**
 * Redisson 客户端扩展的 Quarkus 构建标记项（{@link SimpleBuildItem}）。
 * <p>表示 {@link QuarkusRedissonClientProcessor#build} 步骤已完成，
 * 供其他扩展在构建图中依赖 Redisson 客户端就绪状态。
 *
 * @author Nikita Koksharov
 */
public final class RedissonClientItemBuild extends SimpleBuildItem {
}
