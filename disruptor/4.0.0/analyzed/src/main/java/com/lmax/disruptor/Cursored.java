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
 * 实现此接口的类型必须提供一个 long 值，表示其当前游标位置。
 * 在运行时向 {@link SequenceGroups#addSequences(Object, java.util.concurrent.atomic.AtomicReferenceFieldUpdater, Cursored, Sequence...)}
 * 动态增删 {@link Sequence} 时会用到。
 */
public interface Cursored
{
    /**
     * 获取当前游标值。
     *
     * @return 当前游标值
     */
    long getCursor();
}
