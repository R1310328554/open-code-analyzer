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
package org.redisson.cache;

/**
 * 缓存值引用接口，关联所属的 {@link CachedValue} 条目。
 * <p>
 * 用于软/弱引用实现中追踪引用所有者。
 *
 * @author Nikita Koksharov
 *
 */
public interface CachedValueReference {

    /** 返回引用所属的缓存条目。 */
    CachedValue<?, ?> getOwner();
    
}
