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
 * 空回复转换器：丢弃 Redis 回复内容，始终返回 {@code null}。
 * <p>
 * 用于无需消费回复体、仅等待命令完成的命令。
 *
 * @author Nikita Koksharov
 *
 */
public class VoidReplayConvertor implements Convertor<Void> {

    /** 忽略 {@code obj}，返回 {@code null}。 */
    @Override
    public Void convert(Object obj) {
        return null;
    }


}
