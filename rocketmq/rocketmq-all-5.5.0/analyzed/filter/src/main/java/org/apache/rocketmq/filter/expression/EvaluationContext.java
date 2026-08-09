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

package org.apache.rocketmq.filter.expression;

import java.util.Map;

/**
 * 表达式求值上下文：提供按名称读取变量与全量键值映射。
 *
 * 对应 ActiveMQ 的 MessageEvaluationContext，此处抽象为接口。
 */
public interface EvaluationContext {

    /**
     * 按变量名从上下文取值。
     * @param name 属性或用户属性名
     */
    Object get(String name);

    /**
     * 返回上下文全部变量键值对。
     */
    Map<String, Object> keyValues();
}
