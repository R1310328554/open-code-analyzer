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
 * Live Object 级联操作类型。
 * <p>控制 {@link RLiveObjectService} 持久化、合并、分离、删除时的关联对象传播行为。
 *
 * @author Nikita Koksharov
 */
public enum RCascadeType {

    /**
     * 包含所有级联类型。
     */
    ALL,
    
    /**
     * 在调用 {@link RLiveObjectService#persist} 时级联持久化关联对象。
     */
    PERSIST,
    
    /**
     * 在调用 {@link RLiveObjectService#detach} 时级联分离关联对象。
     */
    DETACH,
    
    /**
     * 在调用 {@link RLiveObjectService#merge} 时级联合并关联对象。
     */
    MERGE,
    
    /**
     * 在调用 {@link RLiveObjectService#delete} 时级联删除关联对象。
     */
    DELETE
    
    
}
