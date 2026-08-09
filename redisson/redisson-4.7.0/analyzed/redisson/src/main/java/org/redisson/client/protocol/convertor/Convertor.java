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
package org.redisson.client.protocol.convertor;

/**
 * Redis 回复后处理转换器接口。
 * <p>
 * 解码器得到原始对象后，由 {@link Convertor} 将其映射为命令期望的 Java 类型，
 * 例如整数转布尔、字节长度转位数等。
 *
 * @author Nikita Koksharov
 *
 * @param <R> type
 */
public interface Convertor<R> {

    /**
     * 将解码后的原始回复对象转换为目标类型。
     *
     * @param obj 解码器输出的原始对象
     * @return 转换后的目标类型实例
     */
    R convert(Object obj);

}
