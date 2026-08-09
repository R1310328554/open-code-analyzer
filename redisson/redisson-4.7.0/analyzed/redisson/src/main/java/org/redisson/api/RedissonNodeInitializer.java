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

import org.redisson.RedissonNode;

/**
 * Redisson Node 启动回调接口；用于在节点启动时执行自定义初始化逻辑。
 * 
 * @author Nikita Koksharov
 *
 */
public interface RedissonNodeInitializer {

    /**
     * Redisson Node 启动时调用。
     * 
     * @param redissonNode Redisson Node 实例
     */
    void onStartup(RedissonNode redissonNode);
    
}
