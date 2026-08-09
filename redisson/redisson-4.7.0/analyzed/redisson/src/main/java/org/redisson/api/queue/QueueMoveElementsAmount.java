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
 * 队列 {@code move} 操作的参数对象，用于指定转移数量。
 *
 * @author Nikita Koksharov
 *
 */
public interface QueueMoveElementsAmount extends QueueMoveElementsArgs {

    /**
     * 设置最多转移的元素条数。
     * 若本队列元素不足，则转移全部现有元素。
     *
     * @param value 最多转移条数
     * @return 参数对象
     */
    QueueMoveElementsOrder count(int value);

    /**
     * 设置必须精确转移的元素条数。
     * 若本队列元素不足，则不转移任何元素。
     *
     * @param value 精确转移条数
     * @return 参数对象
     */
    QueueMoveElementsOrder exactly(int value);

}
