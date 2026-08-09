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
package org.redisson.api.map;

import java.util.Arrays;
import java.util.List;

/**
 * 共享相同字段名的 Map 对象批量导入参数接口。
 *
 * @author Nikita Koksharov
 *
 * @param <K> field type
 */
public interface MapsImportArgs<K> {

    /**
     * 定义所有待导入 Map 对象共用的字段名（变参形式）。
     * <p>
     * 导入时传入的值按位置与这些字段名一一对应。
     *
     * @param fields 字段名
     * @return 参数对象
     */
    @SafeVarargs
    static <K> MapsImportArgs<K> fields(K... fields) {
        return new MapsImportParams<>(Arrays.asList(fields));
    }

    /**
     * 定义所有待导入 Map 对象共用的字段名（List 形式）。
     * <p>
     * 导入时传入的值按位置与这些字段名一一对应。
     *
     * @param fields 字段名列表
     * @return 参数对象
     */
    static <K> MapsImportArgs<K> fields(List<K> fields) {
        return new MapsImportParams<>(fields);
    }

    /**
     * 设置缓冲 Map 对象数量达到该阈值时自动 flush 写入 Redis。
     * <p>
     * 默认值为 {@code 500}。
     *
     * @param batchSize 缓冲 Map 对象数量
     * @return 参数对象
     */
    MapsImportArgs<K> batchSize(int batchSize);

}
