/*
 * Copyright 2012 LMAX Ltd.
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
 * 通常用于将业务代码与 {@link RingBuffer} 解耦，便于单元测试。
 *
 * @param <T> 实现类所提供的数据类型
 */
public interface DataProvider<T>
{
    /**
     * 按序号读取对应槽位的事件数据。
     *
     * @param sequence 要读取的序号
     * @return 该序号对应的数据项
     */
    T get(long sequence);
}
