/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.init;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注 {@link InitFunc} 执行顺序的注解，数值越小优先级越高。
 *
 * @author Eric Zhao
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface InitOrder {

    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    /**
     * 顺序值，默认最低优先级。
     *
     * @return 顺序值
     */
    int value() default LOWEST_PRECEDENCE;
}
