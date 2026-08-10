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

package com.alibaba.nacos.config.server.model;

import java.io.Serializable;

/**
 * 配置监听器检查结果：是否仍有活跃监听、业务码及说明信息。
 * 删除配置前校验是否可安全移除时使用。
 * check config has listener.
 *
 * @author shiyiyue
 */
public class ListenerCheckResult implements Serializable {
    
    /** 是否存在活跃监听器 */
    private boolean hasListener;
    
    /** 检查结果业务码 */
    private int code;
    
    /** 结果说明或错误信息 */
    private String message;
    
    /** 是否存在监听器 */
    public boolean isHasListener() {
        return hasListener;
    }
    
    /** 设置是否存在监听器 */
    public void setHasListener(boolean hasListener) {
        this.hasListener = hasListener;
    }
    
    /** 获取结果码 */
    public int getCode() {
        return code;
    }
    
    /** 设置结果码 */
    public void setCode(int code) {
        this.code = code;
    }
    
    /** 获取说明信息 */
    public String getMessage() {
        return message;
    }
    
    /** 设置说明信息 */
    public void setMessage(String message) {
        this.message = message;
    }
}
