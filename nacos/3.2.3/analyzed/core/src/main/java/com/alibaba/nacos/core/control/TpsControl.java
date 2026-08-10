/*
 *
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.core.control;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TPS 管控注解：标注在 Controller 方法上，声明限流控制点名称与别名，由 {@link HttpTpsPointRegistry} 在容器刷新时注册到管控中心。
 * tps control manager.
 *
 * @author liuzunfei
 * @version $Id: TpsControlManager.java, v 0.1 2021年01月09日 12:38 PM liuzunfei Exp $
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TpsControl {
    
    /**
     * 控制点别名，用于解析器注册与查找。
     *
     * @return 别名，默认可为空字符串
     */
    String name() default "";
    
    /**
     * 要应用的 TPS 控制点名称（必填）。
     *
     * @return 控制点标识
     */
    String pointName();
    
}
