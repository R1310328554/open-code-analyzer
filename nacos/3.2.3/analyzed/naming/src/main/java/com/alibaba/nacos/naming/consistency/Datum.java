/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.naming.consistency;

import com.alibaba.nacos.naming.pojo.Record;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 命名一致性数据单元，封装键值对与版本时间戳。
 *
 * <p>用于 Distro/Raft 等协议在节点间同步 {@link Record} 类型业务数据。</p>
 *
 * @author nacos
 */
public class Datum<T extends Record> implements Serializable {
    
    private static final long serialVersionUID = -2525482315889753720L;
    
    /** 数据键（通常为 {@link KeyBuilder} 构建的字符串）。 */
    public String key;
    
    /** 业务记录值。 */
    public T value;
    
    /** 数据版本时间戳（原子更新）。 */
    public AtomicLong timestamp = new AtomicLong(0L);
    
    /**
     * 工厂方法：创建并填充键值的 {@link Datum} 实例。
     *
     * @param key   数据键
     * @param value 数据值
     * @param <T>   值类型
     * @return 新 Datum
     */
    public static <T extends Record> Datum createDatum(final String key, final T value) {
        Datum datum = new Datum();
        datum.key = key;
        datum.value = value;
        return datum;
    }
}
