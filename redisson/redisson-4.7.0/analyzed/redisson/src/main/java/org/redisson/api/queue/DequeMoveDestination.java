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
package org.redisson.api.queue;

/**
 * 双端队列元素移动操作的目标端参数。
 * <p>
 * 在选定源端移除元素后，指定目标队列及插入位置。
 *
 * @author Nikita Koksharov
 *
 */
public interface DequeMoveDestination extends DequeMoveArgs {

    /**
     * 将移除的元素作为目标队列的头部元素插入。
     *
     * @param name 目标队列名称
     * @return 参数对象
     */
    DequeMoveArgs addFirstTo(String name);

    /**
     * 将移除的元素作为目标队列的尾部元素插入。
     *
     * @param name 目标队列名称
     * @return 参数对象
     */
    DequeMoveArgs addLastTo(String name);

}
