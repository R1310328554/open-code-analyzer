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

/**
 * 可销毁对象标记接口。
 * <p>实现类在不再使用时须调用 {@link #destroy()} 释放 Redis 侧资源。
 *
 * @author Nikita Koksharov
 */
public interface RDestroyable {

    /**
     * 在对象不再需要时销毁并释放资源。
     */
    void destroy();
    
}
