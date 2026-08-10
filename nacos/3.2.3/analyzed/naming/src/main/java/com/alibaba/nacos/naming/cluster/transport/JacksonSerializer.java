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

package com.alibaba.nacos.naming.cluster.transport;

import com.alibaba.nacos.common.utils.JacksonUtils;
import org.springframework.stereotype.Component;

/**
 * 基于 Jackson 的 {@link Serializer} 实现。
 *
 * <p>委托 {@link com.alibaba.nacos.common.utils.JacksonUtils} 完成 Distro 与客户端同步数据的 JSON 编解码。</p>
 *
 * @author yangyi
 */
@Component
public class JacksonSerializer implements Serializer {
    
    /** JSON 字段名：时间戳。 */
    private static final String TIMESTAMP_KEY = "timestamp";
    
    /** JSON 字段名：键。 */
    private static final String KEY = "key";
    
    /** JSON 字段名：值。 */
    private static final String VALUE = "value";
    
    /** 将对象序列化为 JSON 字节数组。 */
    @Override
    public <T> byte[] serialize(T data) {
        return JacksonUtils.toJsonBytes(data);
    }
    
    /** 将 JSON 字节数组反序列化为指定类型。 */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return JacksonUtils.toObj(data, clazz);
    }
}
