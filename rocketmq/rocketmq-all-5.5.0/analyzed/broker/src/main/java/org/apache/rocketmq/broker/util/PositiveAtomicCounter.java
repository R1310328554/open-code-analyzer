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
package org.apache.rocketmq.broker.util;

import java.util.concurrent.atomic.AtomicInteger;

/** 始终返回非负值的 {@link AtomicInteger} 包装，高位溢出后按 {@link #MASK} 截断。 */
public class PositiveAtomicCounter {
    private static final int MASK = 0x7FFFFFFF;
    private final AtomicInteger atom;


    /** 以 0 初始化内部计数器。 */
    public PositiveAtomicCounter() {
        atom = new AtomicInteger(0);
    }


    /** 自增并返回截断后的非负值。 */
    public final int incrementAndGet() {
        final int rt = atom.incrementAndGet();
        return rt & MASK;
    }


    /** 返回当前原始 int 值（可能为负）。 */
    public int intValue() {
        return atom.intValue();
    }
}
