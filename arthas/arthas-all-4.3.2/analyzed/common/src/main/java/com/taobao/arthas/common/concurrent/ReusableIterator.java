/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.taobao.arthas.common.concurrent;

import java.util.Iterator;

/**
 * 可重置的迭代器：遍历结束后可 {@link #rewind()} 从头再扫，避免重复分配 Iterator。
 *
 * @param <E> 元素类型
 */
public interface ReusableIterator<E> extends Iterator<E> {
    /** 重置迭代位置到起始 */
    void rewind();
}
