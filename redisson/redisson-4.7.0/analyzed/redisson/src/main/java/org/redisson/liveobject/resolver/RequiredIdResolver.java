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
package org.redisson.liveobject.resolver;

import org.redisson.api.annotation.RId;
import org.redisson.command.CommandAsyncExecutor;

/**
 * 要求调用方显式提供 id 的 {@link RIdResolver}：未提供则抛异常。
 * <p>
 * 对应 {@code @RId} 未指定 generator 且创建时未传入 id 的场景。
 *
 * @author Nikita Koksharov
 *
 */
public class RequiredIdResolver implements RIdResolver<Object> {
    
    /** 单例，作为必须手动指定 id 时的默认解析器。 */
    public static final RequiredIdResolver INSTANCE = new RequiredIdResolver();

    /** 始终抛出 {@link IllegalArgumentException}，提示未定义 id。 */
    @Override
    public Object resolve(Class<?> cls, RId annotation, String idFieldName, CommandAsyncExecutor commandAsyncExecutor) {
        throw new IllegalArgumentException("id value is not defined for instance of " + cls);
    }

}
