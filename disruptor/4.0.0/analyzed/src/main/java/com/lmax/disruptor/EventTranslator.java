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
 * 将外部数据表示写入从 {@link RingBuffer} 申领到的事件槽位。
 *
 * <p>向 RingBuffer 发布时提供 EventTranslator：RingBuffer 按序号选出下一个可用事件，
 * 交由 EventTranslator 填充后再发布序号更新。
 *
 * @param <T> 事件实现类型，在交换或并行协调过程中承载共享数据
 */
public interface EventTranslator<T>
{
    /**
     * 将数据表示翻译并写入给定事件的字段。
     *
     * @param event    待写入数据的目标事件
     * @param sequence 分配给该事件的序号
     */
    void translateTo(T event, long sequence);
}
