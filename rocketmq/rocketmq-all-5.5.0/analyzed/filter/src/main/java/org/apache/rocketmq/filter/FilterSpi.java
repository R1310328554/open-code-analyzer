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

package org.apache.rocketmq.filter;

import org.apache.rocketmq.filter.expression.Expression;
import org.apache.rocketmq.filter.expression.MQFilterException;

/**
 * 消息过滤器 SPI 接口：编译表达式并声明过滤器类型。
 */
public interface FilterSpi {

    /**
     * 将字符串表达式编译为可执行的 {@link Expression}。
     * @param expr 过滤器表达式字符串
     */
    /** 编译入口，失败时抛出 {@link MQFilterException}。 */
    Expression compile(final String expr) throws MQFilterException;

    /**
     * 返回过滤器类型标识（如 SQL92）。
     */
    String ofType();
}
