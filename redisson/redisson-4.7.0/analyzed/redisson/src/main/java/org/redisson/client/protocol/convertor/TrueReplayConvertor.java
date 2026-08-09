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
 * 恒真回复转换器：忽略 Redis 回复内容，始终返回 {@code true}。
 * <p>
 * 适用于只关心命令是否成功、无需解析具体回复体的场景。
 *
 * @author Nikita Koksharov
 *
 */
public class TrueReplayConvertor implements Convertor<Boolean> {

    /** 无论 {@code obj} 为何值，均返回 {@code true}。 */
    @Override
    public Boolean convert(Object obj) {
        return true;
    }


}
