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
package org.redisson.api.mapreduce;

import java.io.Serializable;
import java.util.Iterator;

/**
 * 将同一键下 Map 阶段产生的多个值归约为单个结果。
 * 
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RReducer<K, V> extends Serializable {

    /**
     * 对每个键及其关联的值集合执行归约。
     * 
     * @param reducedKey 待归约的键
     * @param iter 该键对应的值迭代器
     * @return 归约后的单个值
     */
    V reduce(K reducedKey, Iterator<V> iter);
    
}
