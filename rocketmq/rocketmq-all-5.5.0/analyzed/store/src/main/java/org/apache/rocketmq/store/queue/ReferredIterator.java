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

package org.apache.rocketmq.store.queue;

import java.util.Iterator;

/**
 * 可释放资源的迭代器：遍历完成后须调用 {@link #release()} 释放底层引用。
 */
public interface ReferredIterator<T> extends Iterator<T> {

    /**
     * 释放迭代器所引用的底层资源。
     */
    /** 释放迭代器引用的底层资源。 */
    void release();

    /** 返回下一元素并释放资源。 */
    T nextAndRelease();
}
