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

package com.alibaba.nacos.api.config.remote.response;

import com.alibaba.nacos.api.remote.response.Response;

/**
 * 配置查询响应。
 *
 * <p>服务端返回配置正文、MD5、内容类型、最后修改时间等完整查询结果。</p>
 *
 * @author liuzunfei
 * @version $Id: ConfigQueryResponse.java, v 0.1 2020年07月14日 2:47 PM liuzunfei Exp $
 */
public class ConfigQueryResponse extends Response {
    
    /** 配置不存在时的错误码。 */
    public static final int CONFIG_NOT_FOUND = 300;
    
    /** 配置查询冲突时的错误码。 */
    public static final int CONFIG_QUERY_CONFLICT = 400;
    
    /** 无权限访问时的错误码。 */
    public static final int NO_RIGHT = 403;
    
    /** 配置内容正文。 */
    String content;
    
    /** 加密数据密钥（启用加密时非空）。 */
    String encryptedDataKey;
    
    /** 配置内容类型（如 json、yaml）。 */
    String contentType;
    
    /** 配置内容的 MD5 摘要。 */
    String md5;
    
    /** 最后修改时间戳（毫秒）。 */
    long lastModified;
    
    /** 是否为 Beta（灰度）配置。 */
    boolean isBeta;
    
    /** 配置标签。 */
    String tag;
    
    /** 无参构造，供序列化或框架实例化使用。 */
    public ConfigQueryResponse() {
    }
    
    /**
     * 构建查询失败响应。
     *
     * @param errorCode 业务错误码
     * @param message   错误描述信息
     * @return 失败响应实例
     */
    public static ConfigQueryResponse buildFailResponse(int errorCode, String message) {
        ConfigQueryResponse response = new ConfigQueryResponse();
        response.setErrorInfo(errorCode, message);
        return response;
    }
    
    /**
     * 构建查询成功响应。
     *
     * @param content 配置内容正文
     * @return 成功响应实例
     */
    public static ConfigQueryResponse buildSuccessResponse(String content) {
        ConfigQueryResponse response = new ConfigQueryResponse();
        response.setContent(content);
        return response;
    }
    
    /** 获取配置标签。 */
    public String getTag() {
        return tag;
    }
    
    /** 设置配置标签。 */
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    /** 获取内容 MD5 摘要。 */
    public String getMd5() {
        return md5;
    }
    
    /** 设置内容 MD5 摘要。 */
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    
    /** 获取最后修改时间戳。 */
    public long getLastModified() {
        return lastModified;
    }
    
    /** 设置最后修改时间戳。 */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
    
    /** 是否为 Beta（灰度）配置。 */
    public boolean isBeta() {
        return isBeta;
    }
    
    /** 设置 Beta（灰度）标记。 */
    public void setBeta(boolean beta) {
        isBeta = beta;
    }
    
    /**
     * 获取配置内容正文。
     *
     * @return 配置内容
     */
    public String getContent() {
        return content;
    }
    
    /**
     * 设置配置内容正文。
     *
     * @param content 配置内容
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /** 设置加密数据密钥。 */
    public void setEncryptedDataKey(String encryptedDataKey) {
        this.encryptedDataKey = encryptedDataKey;
    }
    
    /** 获取加密数据密钥。 */
    public String getEncryptedDataKey() {
        return encryptedDataKey;
    }
    
    /**
     * 获取配置内容类型。
     *
     * @return 内容类型字符串
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * 设置配置内容类型。
     *
     * @param contentType 内容类型字符串
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
