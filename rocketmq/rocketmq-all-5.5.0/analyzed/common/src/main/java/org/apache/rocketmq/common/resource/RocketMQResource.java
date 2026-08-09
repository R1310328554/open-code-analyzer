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
package org.apache.rocketmq.common.resource;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 标记 RocketMQ 资源字段或参数的类型注解，
 * 配合 {@link ResourceType} 用于资源解析与注入。
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RocketMQResource {

    /** 资源类型。 */
    ResourceType value();

    /** 多值资源的分隔符，默认空串表示不分隔。 */
    String splitter() default "";
}
