/*
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
 */

package com.alibaba.nacos.plugin.auth.constant;

/**
 * 资源操作类型枚举，定义授权体系中的读/写动作。
 *
 * @author nkorange
 * @author mai.jh
 * @since 1.2.0
 */
public enum ActionTypes {
    
    /**
     * 读操作。
     */
    READ("r"),
    /**
     * 写操作。
     */
    WRITE("w");
    
    /**
     * 操作类型的字符串表示（"r" 或 "w"）。
     */
    private final String action;
    
    ActionTypes(String action) {
        this.action = action;
    }
    
    @Override
    public String toString() {
        return action;
    }
}
