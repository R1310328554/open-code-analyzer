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
package org.redisson.rx;

import org.redisson.api.RMaps;
import org.redisson.api.RMapsImportRx;
import org.redisson.api.map.MapsImportArgs;

/**
 * 多 map 批量导入（{@link RMaps}）的 Rx 门面。
 * <p>
 * 将 {@link RMaps#createImport} 返回的同步导入句柄包装为 {@link RMapsImportRx}，
 * 使大批量 map 数据迁移可走 RxJava 异步链。
 *
 * @author Nikita Koksharov
 *
 * @param <K> field type
 * @param <V> value type
 */
public class RedissonMapsRx<K, V> {

    /** 底层 RMaps 实例。 */
    private final RMaps<K, V> instance;
    /** Rx 命令执行器，供 {@link RxProxyBuilder} 调度异步命令。 */
    private final CommandRxExecutor commandExecutor;

    public RedissonMapsRx(RMaps<K, V> instance, CommandRxExecutor commandExecutor) {
        this.instance = instance;
        this.commandExecutor = commandExecutor;
    }

    /** 按 {@link MapsImportArgs} 创建响应式 map 导入会话。 */
    public RMapsImportRx<K, V> createImport(MapsImportArgs<K> args) {
        return RxProxyBuilder.create(commandExecutor, instance.createImport(args), RMapsImportRx.class);
    }

}
