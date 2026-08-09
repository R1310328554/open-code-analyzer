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
package org.redisson.api;

import io.netty.buffer.ByteBufUtil;
import org.redisson.misc.RandomXoshiro256PlusPlus;

import java.util.Random;

/**
 * 基于 Xoshiro256++ 的随机 ID 生成器；生成 32 字符十六进制字符串。
 *
 * @author Nikita Koksharov
 *
 */
public class RandomIdGenerator implements IdGenerator {

    private static final Random RANDOM = RandomXoshiro256PlusPlus.create();

    @Override
    public String generateId() {
        byte[] id = new byte[16];
        RANDOM.nextBytes(id);
        return ByteBufUtil.hexDump(id);
    }

}
