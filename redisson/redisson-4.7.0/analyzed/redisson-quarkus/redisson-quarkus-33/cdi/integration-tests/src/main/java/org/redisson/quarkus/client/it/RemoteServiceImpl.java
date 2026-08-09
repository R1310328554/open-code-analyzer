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
package org.redisson.quarkus.client.it;

/**
 * {@link RemService} 本地实现，注册到 {@link RRemoteService} 供远程代理调用。
 *
 * @author Nikita Koksharov
 */
public class RemoteServiceImpl implements RemService {


    /** 返回固定字符串 {@code "executed"} 表示调用成功。 */
    @Override
    public String executeMe() {
        return "executed";
    }
}
