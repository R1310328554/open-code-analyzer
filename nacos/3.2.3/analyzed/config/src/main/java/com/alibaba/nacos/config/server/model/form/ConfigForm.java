/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.model.form;

import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.api.model.NacosForm;
import org.springframework.http.HttpStatus;

/**
 * 配置 HTTP 请求表单基类：封装发布/更新配置所需的 dataId、group、命名空间、
 * 正文、标签、灰度规则及元数据字段，实现 {@link NacosForm} 参数校验。
 * ConfigForm.
 *
 * @author dongyafei
 * @author xiweng.yy
 */
public class ConfigForm implements NacosForm, Cloneable {
    
    private static final long serialVersionUID = 4124932564086863921L;
    
    /** 配置 dataId，全局唯一标识配置名 */
    private String dataId;
    
    /**
     * 配置分组（已废弃，请改用 {@link ConfigFormV3#groupName}）。
     * Deprecated, please use {@link ConfigFormV3#groupName} replaced.
     */
    @Deprecated
    private String group;
    
    /** 命名空间 ID，默认为空字符串表示 public 命名空间 */
    private String namespaceId = StringUtils.EMPTY;
    
    /** 配置正文内容 */
    private String content;
    
    /** 标签维度值，用于标签路由场景 */
    private String tag;
    
    /** 关联应用名称，便于控制台检索 */
    private String appName;
    
    /** 操作来源用户 */
    private String srcUser;
    
    /** 配置标签，逗号分隔 */
    private String configTags;
    
    /** 加密配置的数据密钥 */
    private String encryptedDataKey;
    
    /** 灰度配置名称 */
    private String grayName;
    
    /** 灰度匹配规则表达式 */
    private String grayRuleExp;
    
    /** 灰度版本号 */
    private String grayVersion;
    
    /** 灰度优先级，数值越大优先级越高 */
    private int grayPriority;
    
    /** 配置描述说明 */
    private String desc;
    
    /** 配置用途说明 */
    private String use;
    
    /** 配置生效范围或影响说明 */
    private String effect;
    
    /** 配置内容类型（text、json、yaml 等） */
    private String type;
    
    /** 配置内容 Schema 定义 */
    private String schema;
    
    public ConfigForm() {
    }
    
    public ConfigForm(String dataId, String group, String namespaceId, String content, String tag,
        String appName,
        String srcUser, String configTags, String desc, String use, String effect, String type,
        String schema) {
        this.dataId = dataId;
        this.group = group;
        this.namespaceId = namespaceId;
        this.content = content;
        this.tag = tag;
        this.appName = appName;
        this.srcUser = srcUser;
        this.configTags = configTags;
        this.desc = desc;
        this.use = use;
        this.effect = effect;
        this.type = type;
        this.schema = schema;
    }
    
    @Override
    public ConfigForm clone() {
        try {
            // Object.clone() 是浅拷贝，但对于 String 和基本类型已经足够
            return (ConfigForm) super.clone();
        } catch (CloneNotSupportedException e) {
            // 理论上不会发生，因为实现了接口Cloneable
            throw new AssertionError(e);
        }
    }
    
    public String getDataId() {
        return dataId;
    }
    
    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getTag() {
        return tag;
    }
    
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    public String getAppName() {
        return appName;
    }
    
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    public String getSrcUser() {
        return srcUser;
    }
    
    public void setSrcUser(String srcUser) {
        this.srcUser = srcUser;
    }
    
    public String getConfigTags() {
        return configTags;
    }
    
    public void setConfigTags(String configTags) {
        this.configTags = configTags;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public String getUse() {
        return use;
    }
    
    public void setUse(String use) {
        this.use = use;
    }
    
    public String getEffect() {
        return effect;
    }
    
    public void setEffect(String effect) {
        this.effect = effect;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSchema() {
        return schema;
    }
    
    public void setSchema(String schema) {
        this.schema = schema;
    }
    
    public String getEncryptedDataKey() {
        return encryptedDataKey;
    }
    
    public void setEncryptedDataKey(String encryptedDataKey) {
        this.encryptedDataKey = encryptedDataKey;
    }
    
    public String getGrayName() {
        return grayName;
    }
    
    public void setGrayName(String grayName) {
        this.grayName = grayName;
    }
    
    public String getGrayRuleExp() {
        return grayRuleExp;
    }
    
    public void setGrayRuleExp(String grayRuleExp) {
        this.grayRuleExp = grayRuleExp;
    }
    
    public String getGrayVersion() {
        return grayVersion;
    }
    
    public void setGrayVersion(String grayVersion) {
        this.grayVersion = grayVersion;
    }
    
    public int getGrayPriority() {
        return grayPriority;
    }
    
    public void setGrayPriority(int grayPriority) {
        this.grayPriority = grayPriority;
    }
    
    @Override
    public void validate() throws NacosApiException {
        if (StringUtils.isBlank(dataId)) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(), ErrorCode.PARAMETER_MISSING,
                "Required parameter 'dataId' type String is not present");
        } else if (StringUtils.isBlank(group)) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(), ErrorCode.PARAMETER_MISSING,
                "Required parameter 'group' type String is not present");
        }
    }
    
    /**
     * 校验表单参数，并额外要求 content 非空。
     * Validate form parameter and include validate `content` parameters.
     *
     * @throws NacosApiException NacosApiException
     */
    public void validateWithContent() throws NacosApiException {
        validate();
        if (StringUtils.isBlank(content)) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(), ErrorCode.PARAMETER_MISSING,
                "Required parameter 'content' type String is not present");
        }
    }
}
