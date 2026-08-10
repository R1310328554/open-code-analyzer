/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.core.v2.cleaner;

/**
 * 命名模块清理任务接口。
 *
 * <p>各清理器通过 {@link #getType()} 标识类型，在 {@link #doClean()} 中执行具体清理逻辑。</p>
 *
 * @author xiweng.yy
 */
public interface NamingCleaner {
    
    /**
     * 返回本清理器处理的资源类型标识。
     *
     * @return 清理类型字符串
     */
    String getType();
    
    /** 执行一次清理操作。 */
    void doClean();
}
