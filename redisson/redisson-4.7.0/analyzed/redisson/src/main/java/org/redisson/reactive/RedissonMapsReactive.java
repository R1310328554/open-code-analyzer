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
package org.redisson.reactive;

import org.redisson.api.RMaps;
import org.redisson.api.RMapsImportReactive;
import org.redisson.api.map.MapsImportArgs;

/**
 * {@link RMaps} 的 Reactor 响应式辅助类：
 * 将批量 Map 导入等高级操作包装为 {@link RMapsImportReactive}。
 * <p>
 * 底层仍委托同步 {@link RMaps} 实例，通过 {@link ReactiveProxyBuilder}
 * 将异步调用适配为 Publisher/Mono。
 *
 * @author Nikita Koksharov
 *
 * @param <K> Map 键类型
 * @param <V> Map 值类型
 */
public class RedissonMapsReactive<K, V> {

    /** 底层同步 RMaps 实现。 */
    private final RMaps<K, V> instance;
    /** 响应式命令执行器，用于构建代理。 */
    private final CommandReactiveExecutor commandExecutor;

    /** @param instance 同步 RMaps 实例 @param commandExecutor 响应式执行器 */
    public RedissonMapsReactive(RMaps<K, V> instance, CommandReactiveExecutor commandExecutor) {
        this.instance = instance;
        this.commandExecutor = commandExecutor;
    }

    /** 创建响应式 Map 批量导入会话，参数见 {@link MapsImportArgs}。 */
    public RMapsImportReactive<K, V> createImport(MapsImportArgs<K> args) {
        return ReactiveProxyBuilder.create(commandExecutor, instance.createImport(args), RMapsImportReactive.class);
    }

}
