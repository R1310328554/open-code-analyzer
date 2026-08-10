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
 * 扩展配置信息：在 {@link ConfigInfo} 基础上携带批量查询时的单条状态码与说明。
 * status 含义参见 {@code Constants} 中定义的配置状态常量。
 * ConfigInfoEx.
 *
 * @author leiwen.zh
 */
public class ConfigInfoEx extends ConfigInfo {
    
    private static final long serialVersionUID = 8905036592920606608L;
    
    /**
     * 批量查询时单条记录的状态码；具体取值见 Constants.java。
     * Single message status code, when querying for batch.
     * And details of message status code, you can see Constants.java.
     */
    private int status;
    
    /**
     * 批量查询时单条记录的说明信息。
     * Single message information, when querying for batch.
     */
    private String message;
    
    /** 无参构造 */
    public ConfigInfoEx() {
        super();
    }
    
    /**
     * 构造带内容的扩展配置。
     *
     * @param dataId  配置 dataId
     * @param group   配置 group
     * @param content 配置正文
     */
    public ConfigInfoEx(String dataId, String group, String content) {
        super(dataId, group, content);
    }
    
    /**
     * 构造带状态码与消息的扩展配置。
     *
     * @param dataId  配置 dataId
     * @param group   配置 group
     * @param content 配置正文
     * @param status  单条状态码
     * @param message 单条说明
     */
    public ConfigInfoEx(String dataId, String group, String content, int status, String message) {
        super(dataId, group, content);
        this.status = status;
        this.message = message;
    }
    
    /** 获取单条状态码 */
    public int getStatus() {
        return status;
    }
    
    /** 设置单条状态码 */
    public void setStatus(int status) {
        this.status = status;
    }
    
    /** 获取单条说明信息 */
    public String getMessage() {
        return message;
    }
    
    /** 设置单条说明信息 */
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
    @Override
    public String toString() {
        return "ConfigInfoEx [status=" + status + ", message=" + message + ", dataId=" + getDataId()
            + ", group="
            + getGroup() + ", appName=" + getAppName() + ", content=" + getContent() + "]";
    }
    
}
