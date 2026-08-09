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

/**
 * 责任链中的单个处理器。
 *
 * @param <T> 请求/上下文类型
 * @param <R> 处理结果类型
 */
public interface Handler<T, R> {

    /**
     * 处理当前节点逻辑，并可选择调用 {@link HandlerChain#handle} 传递至下一节点。
     *
     * @param t 输入上下文
     * @param chain 责任链实例
     * @return 处理结果
     */
    R handle(T t, HandlerChain<T, R> chain);
}
