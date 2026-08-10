/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.visibility.constant;

/**
 * 可见性插件常量定义。
 *
 * <p>包含资源可见范围（公开/私有）与读写操作标识。</p>
 *
 * @author xiweng.yy
 */
public class VisibilityConstants {
    
    /** 公开可见范围标识。 */
    public static final String SCOPE_PUBLIC = "PUBLIC";
    
    /** 私有可见范围标识。 */
    public static final String SCOPE_PRIVATE = "PRIVATE";
    
    /** 读操作标识。 */
    public static final String ACTION_READ = "r";
    
    /** 写操作标识。 */
    public static final String ACTION_WRITE = "w";
    
    private VisibilityConstants() {
    }
}
