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
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.api.model.NacosForm;
import org.springframework.http.HttpStatus;

/**
 * 集群配置更新 HTTP 表单。
 *
 * <p>用于修改健康检查端口、是否复用实例端口、healthChecker 类型及集群 metadata；serviceName 与 clusterName 为必填项。</p>
 *
 * @author xiweng.yy
 */
public class UpdateClusterForm implements NacosForm {
    
    private static final long serialVersionUID = 4724672496526879919L;
    
    private String namespaceId = Constants.DEFAULT_NAMESPACE_ID;
    
    private String groupName;
    
    private String serviceName;
    
    private String clusterName;
    
    private Integer checkPort;
    
    private Boolean useInstancePort4Check;
    
    private String healthChecker;
    
    private String metadata;
    
    /** 校验必填字段并在通过后填充 namespace/group/metadata 默认值。 */
    @Override
    public void validate() throws NacosApiException {
        if (StringUtils.isBlank(serviceName)) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(), ErrorCode.PARAMETER_MISSING,
                "Required parameter 'serviceName' type String is not present");
        }
        if (StringUtils.isBlank(clusterName)) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(), ErrorCode.PARAMETER_MISSING,
                "Required parameter 'clusterName' type String is not present");
        }
        if (null == checkPort) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(), ErrorCode.PARAMETER_MISSING,
                "Required parameter 'checkPort' type Integer is not present");
        }
        if (null == useInstancePort4Check) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(), ErrorCode.PARAMETER_MISSING,
                "Required parameter 'useInstancePort4Check' type Boolean is not present");
        }
        if (StringUtils.isEmpty(healthChecker)) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(), ErrorCode.PARAMETER_MISSING,
                "Required parameter 'healthChecker' type String is not present");
        }
        fillDefaultValue();
    }
    
    /** 填充缺省 namespace、group，metadata 缺省为空串。 */
    private void fillDefaultValue() {
        if (StringUtils.isEmpty(namespaceId)) {
            namespaceId = Constants.DEFAULT_NAMESPACE_ID;
        }
        if (StringUtils.isEmpty(groupName)) {
            groupName = Constants.DEFAULT_GROUP;
        }
        if (null == metadata) {
            metadata = StringUtils.EMPTY;
        }
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getClusterName() {
        return clusterName;
    }
    
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
    
    public Integer getCheckPort() {
        return checkPort;
    }
    
    public void setCheckPort(Integer checkPort) {
        this.checkPort = checkPort;
    }
    
    public Boolean isUseInstancePort4Check() {
        return useInstancePort4Check;
    }
    
    public void setUseInstancePort4Check(Boolean useInstancePort4Check) {
        this.useInstancePort4Check = useInstancePort4Check;
    }
    
    public String getHealthChecker() {
        return healthChecker;
    }
    
    public void setHealthChecker(String healthChecker) {
        this.healthChecker = healthChecker;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
