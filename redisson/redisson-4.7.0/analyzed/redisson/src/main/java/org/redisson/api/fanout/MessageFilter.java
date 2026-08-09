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
package org.redisson.api.fanout;

import java.io.Serializable;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * 可靠扇出（ReliableFanout）消息过滤接口。
 * <p>
 * 实现本接口可基于自定义逻辑选择性投递消息：{@link java.util.function.BiPredicate#test}
 * 返回 {@code true} 表示应投递，{@code false} 表示过滤掉。
 * <p>
 * 作为可序列化的 {@link java.util.function.BiPredicate}，实例可：
 * <ul>
 *   <li>跨网络传输到各节点</li>
 *   <li>在所有 ReliableFanout 副本间复制</li>
 *   <li>在发布流程的每个节点上执行过滤</li>
 * </ul>
 * 第二个参数为消息头键值对，可用于基于元数据的过滤决策。
 *
 * @param <V> 消息体类型
 * @author Nikita Koksharov
 */
public interface MessageFilter<V> extends BiPredicate<V, Map<String, Object>>, Serializable {
}
