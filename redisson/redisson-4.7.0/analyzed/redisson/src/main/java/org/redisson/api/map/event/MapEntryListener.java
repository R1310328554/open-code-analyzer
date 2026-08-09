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
package org.redisson.api.map.event;

import java.util.EventListener;

/**
 * {@link org.redisson.api.RMapCache} 条目事件监听器的标记接口。
 * <p>
 * 具体监听创建、更新、移除或过期请实现对应的子接口。
 *
 * @author Nikita Koksharov
 *
 */
public interface MapEntryListener extends EventListener {

}
