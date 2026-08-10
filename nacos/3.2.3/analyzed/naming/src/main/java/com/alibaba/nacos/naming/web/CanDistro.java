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

package com.alibaba.nacos.naming.web;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 标记 Controller 方法是否参与 Distro 分区路由。
 *
 * <p>标注后由 {@link DistroFilter} 判断本节点是否负责该请求，否则代理到负责节点。</p>
 *
 * @author nkorange
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CanDistro {
    
}
