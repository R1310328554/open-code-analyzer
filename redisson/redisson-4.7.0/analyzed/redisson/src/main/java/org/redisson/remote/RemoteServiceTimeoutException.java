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
package org.redisson.remote;

/**
 * 远程服务调用超时异常：
 * 当 {@link RemoteInvocationOptions} 指定的执行超时到期
 * 仍未收到 {@link RemoteServiceResponse} 时抛出。
 *
 * @author Nikita Koksharov
 *
 */
public class RemoteServiceTimeoutException extends RuntimeException {

    private static final long serialVersionUID = -1749266931994840256L;

    /** @param message 超时描述（通常含请求 ID 与超时毫秒数） */
    public RemoteServiceTimeoutException(String message) {
        super(message);
    }
    
}
