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
import java.util.Map;

/**
 * 归并 {@link RReducer} 阶段输出的结果映射，生成单一结果对象。
 * <p>在整个 MapReduce 流程中仅执行一次。
 * 
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @param <R> 最终归并结果类型
 */
public interface RCollator<K, V, R> extends Serializable {

    /**
     * 归并 Reduce 阶段产生的键值映射。
     * 
     * @param resultMap Reduce 阶段输出的完整条目
     * @return 归并后的单一结果对象
     */
    R collate(Map<K, V> resultMap);
    
}
