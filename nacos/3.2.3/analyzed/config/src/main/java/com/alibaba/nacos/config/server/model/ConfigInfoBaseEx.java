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

/**
 * 带状态码的配置基础扩展：在 {@link ConfigInfoBase} 上附加单条批量操作的结果
 * status 与 message，字段集合固定以兼容旧版批量 API。
 * ConfigInfoBaseEx.
 * And can't add field, to compatible with old interface(If adding a field, then it will occur compatibility problems).
 *
 * @author Nacos
 */
public class ConfigInfoBaseEx extends ConfigInfoBase {
    
    private static final long serialVersionUID = 5802322506486922169L;
    
    /**
     * 单条批量操作结果状态码，详见 {@link com.alibaba.nacos.config.server.constant.Constants}。
     * Single message status code, when querying for batch.
     * And details of message status code, you can see Constants.java.
     */
    private int status;
    
    /**
     * 单条批量操作的说明信息（成功或失败原因）。
     * Single message information, when querying for batch.
     */
    private String message;
    
    public ConfigInfoBaseEx() {
        super();
    }
    
    public ConfigInfoBaseEx(String dataId, String group, String content) {
        super(dataId, group, content);
    }
    
    /** 构造带状态码与消息的扩展配置实体 */
    public ConfigInfoBaseEx(String dataId, String group, String content, int status,
        String message) {
        super(dataId, group, content);
        this.status = status;
        this.message = message;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public String toString() {
        return "ConfigInfoBaseEx [status=" + status + ", message=" + message + ", dataId="
            + getDataId() + ", group()="
            + getGroup() + ", content()=" + getContent() + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
