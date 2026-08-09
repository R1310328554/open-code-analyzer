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
 * Live Object（实时对象）接口，代表已持久化到 Redis 的实体代理。
 * <p>实现类通过 {@link RLiveObjectService} 创建，getter/setter 自动映射到 Redis。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 */
public interface RLiveObject extends RExpirable {

    /**
     * 返回标注 {@code @RId} 的主键字段值。
     * @return Live Object 主键
     */
    Object getLiveObjectId();

    /**
     * 修改 {@code @RId} 主键字段值。由于 liveObjectId 编码在底层 {@link RMap} 键名中，
     * liveObjectId 编码在底层 {@link RMap} 键名中，
     * 此操作将按实例类 {@code @REntity} 命名规则重命名底层 {@link RMap}。
     * 
     * @param liveObjectId Live Object 主键
     * @see org.redisson.api.RMap
     */
    void setLiveObjectId(Object liveObjectId);

    RMap getLiveObjectLiveMap();

}
