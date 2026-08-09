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
package org.apache.rocketmq.common.chain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 泛型责任链：按注册顺序依次调用 {@link Handler}，支持处理器内继续传递。
 *
 * @param <T> 请求/上下文类型
 * @param <R> 处理结果类型
 */
public class HandlerChain<T, R> {

    /** 已注册的处理器列表。 */
    private List<Handler<T, R>> handlers;
    /** 当前链路的迭代器（单次 handle 调用内复用）。 */
    private Iterator<Handler<T, R>> iterator;

    /** 创建空责任链。 */
    public static <T, R> HandlerChain<T, R> create() {
        return new HandlerChain<>();
    }

    /** 追加处理器并返回自身（链式调用）。 */
    public HandlerChain<T, R> addNext(Handler<T, R> handler) {
        if (this.handlers == null) {
            this.handlers = new ArrayList<>();
        }
        this.handlers.add(handler);
        return this;
    }

    /**
     * 驱动责任链：取下一个处理器执行，无剩余处理器时返回 null。
     *
     * @param t 输入上下文
     * @return 处理结果，链耗尽时为 null
     */
    public R handle(T t) {
        if (iterator == null) {
            iterator = handlers.iterator();
        }
        if (iterator.hasNext()) {
            Handler<T, R> handler = iterator.next();
            return handler.handle(t, this);
        }
        return null;
    }
}
