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
package org.redisson.liveobject.core;

import net.bytebuddy.implementation.bind.annotation.*;
import org.redisson.api.RMap;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.liveobject.resolver.MapResolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Live Object 的 {@link RMap} 方法拦截器。
 * <p>
 * ByteBuddy 将实体上对 {@link RMap} 接口方法的调用委托到此拦截器；
 * 通过 {@link MapResolver} 解析或创建与实体 id 绑定的 Redis Map，再反射调用目标方法。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 * @author Nikita Koksharov
 */
public class RMapInterceptor {

    /** 异步命令执行器，用于创建底层 {@link RedissonMap}。 */
    private final CommandAsyncExecutor commandAsyncExecutor;
    /** 解析/缓存实体对应的 Live Map 实例。 */
    private final MapResolver mapResolver;
    /** 被代理的 {@code @REntity} 实体类。 */
    private final Class<?> entityClass;

    /** 构造 Map 方法拦截器。 */
    public RMapInterceptor(CommandAsyncExecutor commandAsyncExecutor, Class<?> entityClass, MapResolver mapResolver) {
        this.commandAsyncExecutor = commandAsyncExecutor;
        this.mapResolver = mapResolver;
        this.entityClass = entityClass;
    }

    /**
     * 拦截对 Live Map 的方法调用：先 resolve 出 {@link RMap}，再 invoke 原方法。
     * <p>
     * {@link InvocationTargetException} 的 cause 会被重新抛出，便于调用方看到真实异常。
     */
    @RuntimeType
    public Object intercept(
            @Origin Method method,
            @AllArguments Object[] args,
            @FieldValue("liveObjectId") Object id,
            @FieldProxy("liveObjectLiveMap") LiveObjectInterceptor.Setter mapSetter,
            @FieldProxy("liveObjectLiveMap") LiveObjectInterceptor.Getter mapGetter
    ) throws Throwable {
        try {
            RMap map = mapResolver.resolve(commandAsyncExecutor, entityClass, id, mapSetter, mapGetter);
            return method.invoke(map, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
