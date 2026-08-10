/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.model.form;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.api.model.NacosForm;

/**
 * 服务列表查询 HTTP 表单。
 *
 * <p>支持按 serviceNameParam、groupNameParam 模糊过滤；ignoreEmptyService 跳过无实例服务，withInstances 控制是否附带实例信息。</p>
 *
 * @author xiweng.yy
 */
public class ServiceListForm implements NacosForm {
    
    private static final long serialVersionUID = 541715462458894942L;
    
    private String namespaceId = Constants.DEFAULT_NAMESPACE_ID;
    
    private String serviceNameParam = StringUtils.EMPTY;
    
    private String groupNameParam = StringUtils.EMPTY;
    
    private boolean ignoreEmptyService;
    
    boolean withInstances;
    
    /** 确保 namespaceId 非空，缺省时使用默认命名空间。 */
    @Override
    public void validate() throws NacosApiException {
        if (StringUtils.isBlank(namespaceId)) {
            namespaceId = Constants.DEFAULT_NAMESPACE_ID;
        }
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getServiceNameParam() {
        return serviceNameParam;
    }
    
    public void setServiceNameParam(String serviceNameParam) {
        this.serviceNameParam = serviceNameParam;
    }
    
    public String getGroupNameParam() {
        return groupNameParam;
    }
    
    public void setGroupNameParam(String groupNameParam) {
        this.groupNameParam = groupNameParam;
    }
    
    public boolean isIgnoreEmptyService() {
        return ignoreEmptyService;
    }
    
    public void setIgnoreEmptyService(boolean ignoreEmptyService) {
        this.ignoreEmptyService = ignoreEmptyService;
    }
    
    public boolean isWithInstances() {
        return withInstances;
    }
    
    public void setWithInstances(boolean withInstances) {
        this.withInstances = withInstances;
    }
}
