/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.common.queue;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 固定容量的环形队列：新元素入队时若已满则淘汰最旧元素；非线程安全。
 *
 * @param <E> 元素类型
 */
public class RoundQueue<E> {

    /** 底层 FIFO 队列。 */
    private Queue<E> queue;
    /** 最大容量。 */
    private int capacity;

    /** 指定容量构造环形队列。 */
    public RoundQueue(int capacity) {
        this.capacity = capacity;
        queue = new LinkedList<>();
    }

    /**
     * 尝试加入元素：已存在则返回 false；满则 poll 队首后再 add。
     *
     * @return 实际新增元素返回 true，重复元素返回 false
     */
        boolean ok = false;
        if (!queue.contains(e)) {
            if (queue.size() >= capacity) {
                queue.poll();
            }
            queue.add(e);
            ok = true;
        }

        return ok;
    }
}
