/*
 * Copyright 2011 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lmax.disruptor;

/**
 * 由 {@link RingBuffer} 在初始化时调用，预填充环形缓冲区中的全部事件槽位。
 *
 * @param <T> 事件实现类型，在交换或并行协调过程中承载共享数据
 */
public interface EventFactory<T>
{
    /**
     * 创建一个新的事件实例；应尽可能在构造时完成内存分配，避免后续热路径分配。
     *
     * @return 新构造的事件实例
     */
    T newInstance();
}
