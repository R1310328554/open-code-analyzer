/*
 * Copyright 2016 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.netty.handler.codec.redis;

import io.netty.util.internal.UnstableApi;

/**
 * Errors of <a href="https://redis.io/topics/protocol">RESP</a>.
 * <p>RESP Error 类型（前缀 {@code -}），表示命令执行失败。正文通常为 {@code ERR ...} 等人可读
 * 字符串；常见固定文案可通过 {@link FixedRedisMessagePool#getError} 复用单例。</p>
 */
@UnstableApi
public final class ErrorRedisMessage extends AbstractStringRedisMessage {

    /**
     * Creates a {@link ErrorRedisMessage} from {@code content}.
     *
     * @param content the message content, must not be {@code null}.
     */
    public ErrorRedisMessage(String content) {
        super(content);
    }

}
