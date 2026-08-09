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
 * 向只读副本节点执行写命令（READONLY）时抛出。
 * <p>
 * 客户端应改向主节点或启用读写分离的正确路由。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisReadonlyException extends RedisException {

    private static final long serialVersionUID = -2565335188503354660L;

    /** 使用错误消息构造只读异常。 */
    public RedisReadonlyException(String message) {
        super(message);
    }

}
