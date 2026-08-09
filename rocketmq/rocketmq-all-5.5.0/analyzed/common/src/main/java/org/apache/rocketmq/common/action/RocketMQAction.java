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
package org.apache.rocketmq.common.action;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.apache.rocketmq.common.resource.ResourceType;

/**
 * 标注 RPC/管理接口所需的 ACL 动作与资源类型。
 * {@link #value()} 为请求码，{@link #action()} 列出允许的动作集合。
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RocketMQAction {

    /** 关联的请求码（RequestCode）。 */
    int value();

    /** 受控资源类型，默认 UNKNOWN。 */
    ResourceType resource() default ResourceType.UNKNOWN;

    /** 本接口允许的一个或多个 {@link Action}。 */
    Action[] action();
}
