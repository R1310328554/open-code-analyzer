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
 * 空安全 {@link Double} 回复转换器，继承 {@link DoubleReplayConvertor}。
 * <p>
 * 父类转换结果为 {@code null} 时返回 {@code 0.0}，避免下游 NPE。
 *
 * @author Nikita Koksharov
 *
 */
public class DoubleNullSafeReplayConvertor extends DoubleReplayConvertor {

    /** 委托父类转换，{@code null} 结果替换为 {@code 0.0}。 */
    @Override
    public Double convert(Object obj) {
        Double r = super.convert(obj);
        if (r == null) {
            return 0.0;
        }
        return r;
    }
    
}
