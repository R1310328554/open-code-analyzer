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
 * 队列 {@code move} 操作的参数对象，用于指定转移顺序策略。
 *
 * @author Nikita Koksharov
 *
 */
public interface QueueMoveElementsOrder extends QueueMoveElementsArgs {

    /**
     * 一次性批量追加全部元素，并保持相对顺序不变。
     * <p>
     * 此为默认策略。
     *
     * @return 参数对象
     */
    QueueMoveElementsArgs bulk();

    /**
     * 逐条转移元素：每追加一条到目标队列后，再从本队列移除下一条。
     *
     * @return 参数对象
     */
    QueueMoveElementsArgs oneByOne();

}
