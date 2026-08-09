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
package org.redisson.liveobject;

import org.redisson.api.RMap;

/**
 * Live Object 代理类的 ByteBuddy 模板基类。
 * <p>
 * 代理实例继承此类，持有 {@code liveObjectId} 与 {@code liveObjectLiveMap} 两个
 * 由 {@link LiveObjectInterceptor} 注入的字段，供拦截器读写 Redis 映射数据。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 */
public class LiveObjectTemplate {

    /** Live Object 主键（{@link org.redisson.api.annotation.RId} 标注字段）。 */
    private Object liveObjectId;
    /** 存储实体字段序列化值的 Redis Hash 映射。 */
    private RMap liveObjectLiveMap;

}
