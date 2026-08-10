/*
 * Copyright 1999-$toady.year Alibaba Group Holding Ltd.
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
import com.alibaba.nacos.config.server.service.capacity.CapacityService;
import com.alibaba.nacos.api.model.NacosForm;
import org.springframework.http.HttpStatus;

/**
 * 更新配置容量表单：用于控制台或 OpenAPI 调整命名空间/分组的
 * 配额、单条大小及聚合上限，至少需提供一项容量参数。
 * This form is used to update capacity-related configurations.
 *
 * @author Nacos
 */
public class UpdateCapacityForm implements NacosForm {
    
    private static final long serialVersionUID = -1912905276914026856L;
    
    /** 目标配置分组，与 namespaceId 二选一或同时指定 */
    private String groupName;
    
    /** 目标命名空间 ID */
    private String namespaceId;
    
    /** 配置条目数量配额上限 */
    private Integer quota;
    
    /** 单条配置内容最大字节数 */
    private Integer maxSize;
    
    /** 单条配置最大聚合子项数 */
    private Integer maxAggrCount;
    
    /** 单条配置聚合内容总大小上限 */
    private Integer maxAggrSize;
    
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public Integer getQuota() {
        return quota;
    }
    
    public void setQuota(Integer quota) {
        this.quota = quota;
    }
    
    public Integer getMaxSize() {
        return maxSize;
    }
    
    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }
    
    public Integer getMaxAggrCount() {
        return maxAggrCount;
    }
    
    public void setMaxAggrCount(Integer maxAggrCount) {
        this.maxAggrCount = maxAggrCount;
    }
    
    public Integer getMaxAggrSize() {
        return maxAggrSize;
    }
    
    public void setMaxAggrSize(Integer maxAggrSize) {
        this.maxAggrSize = maxAggrSize;
    }
    
    @Override
    public void validate() throws NacosApiException {
        if (quota == null && maxSize == null && maxAggrCount == null && maxAggrSize == null) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(), ErrorCode.PARAMETER_MISSING,
                "The parameters quota, maxSize, maxAggrCount, maxAggrSize cannot be empty at the same time");
        }
    }
    
    /**
     * 校验 namespaceId 与 groupName：二者均为空时触发全量容量初始化并抛错。
     * Check namespaceId and groupName.
     *
     * @param capacityService capacity service
     * @throws NacosApiException NacosApiException
     */
    public void checkNamespaceIdAndGroupName(CapacityService capacityService)
        throws NacosApiException {
        if (StringUtils.isBlank(groupName) && StringUtils.isBlank(namespaceId)) {
            capacityService.initAllCapacity();
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(), ErrorCode.PARAMETER_MISSING,
                "At least one of the parameters (groupName or namespaceId) must be provided");
        }
    }
}
