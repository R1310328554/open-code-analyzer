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

import org.redisson.RedissonMap;
import org.redisson.api.RMap;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.connection.ServiceManager;
import org.redisson.liveobject.core.LiveObjectInterceptor;

/**
 * Live Object 实体级 {@link RMap} 的解析与懒创建。
 * <p>
 * 拦截器通过 Getter/Setter 缓存已创建的 Map；首次访问时按 {@link NamingScheme} 构造 {@link RedissonMap}。
 *
 * @author Nikita Koksharov
 *
 */
public final class MapResolver {

    /** 连接与服务管理（remove/destroy 预留扩展点）。 */
    private final ServiceManager serviceManager;

    /** @param serviceManager Redisson 服务管理器 */
    public MapResolver(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    /** 按实体类型清理缓存（当前为空实现）。 */
    public void remove(Class<?> entityClass) {
    }

    /** 按实体类型与 id 清理（当前为空实现）。 */
    public void remove(Class<?> entityClass, Object id) {
    }

    /** 销毁指定 Live Object 的 Map（当前为空实现）。 */
    public void destroy(Class<?> entityClass, Object id) {
    }

    /**
     * 返回实体 id 对应的 Live Map：若 Getter 已有值则直接返回，否则创建并 Setter 缓存。
     */
    public RMap resolve(CommandAsyncExecutor commandExecutor, Class<?> entityClass, Object id,
                        LiveObjectInterceptor.Setter mapSetter, LiveObjectInterceptor.Getter mapGetter) {
        if (mapGetter.getValue() != null) {
            return (RMap) mapGetter.getValue();
        }

        NamingScheme namingScheme = commandExecutor.getObjectBuilder().getNamingScheme(entityClass);
        String idKey = namingScheme.getName(entityClass, id);

        RMap<Object, Object> lm = new RedissonMap<>(namingScheme.getCodec(), commandExecutor,
                    idKey, null, null, null);

        mapSetter.setValue(lm);
        return lm;
    }

}
