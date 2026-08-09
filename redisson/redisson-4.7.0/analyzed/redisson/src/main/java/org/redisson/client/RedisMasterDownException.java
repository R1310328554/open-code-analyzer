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
package org.redisson.client;

/**
 * 从节点与主节点失联（MASTERDOWN）时抛出的可重试异常。
 * <p>
 * 常见于主从复制中断，客户端可稍后重试或切换节点。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisMasterDownException extends RedisRetryException {

    private static final long serialVersionUID = -2565335188503354660L;

    /** 使用错误消息构造 MASTERDOWN 异常。 */
    public RedisMasterDownException(String message) {
        super(message);
    }

}
