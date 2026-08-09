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
package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.init.InitExecutor;

/**
 * Sentinel 环境类。本类触发 Sentinel 的全部初始化。
 *
 * <p>
 * 注意：为防止死锁，其他类的静态代码块或静态字段
 * 绝不应引用本类。
     * </p>
 *
 * @author jialiang.linjl
 */
public class Env {

    public static final Sph sph = new CtSph();

    static {
        // 若初始化失败，进程将退出。
        InitExecutor.doInit();
    }

}
