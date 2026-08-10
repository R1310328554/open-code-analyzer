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

package com.alibaba.nacos.naming.controllers.v3;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.api.naming.pojo.healthcheck.AbstractHealthChecker;
import com.alibaba.nacos.api.naming.pojo.healthcheck.HealthCheckerFactory;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.naming.core.ClusterOperatorV2Impl;
import com.alibaba.nacos.naming.core.v2.metadata.ClusterMetadata;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.alibaba.nacos.naming.model.form.UpdateClusterForm;
import com.alibaba.nacos.naming.paramcheck.NamingDefaultHttpParamExtractor;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 命名集群管理 v3 控制器。
 *
 * <p>提供集群元数据（健康检查、扩展数据等）的更新 API。</p>
 *
 * @author Nacos
 */
@NacosApi
@RestController
@RequestMapping(UtilsAndCommons.CLUSTER_CONTROLLER_V3_ADMIN_PATH)
@ExtractorManager.Extractor(httpExtractor = NamingDefaultHttpParamExtractor.class)
public class ClusterControllerV3 {
    
    /** 集群元数据操作实现。 */
    private final ClusterOperatorV2Impl clusterOperatorV2;
    
    public ClusterControllerV3(ClusterOperatorV2Impl clusterOperatorV2) {
        this.clusterOperatorV2 = clusterOperatorV2;
    }
    
    /** 更新指定服务的集群元数据（健康检查器、端口等）。 */
    @Since("3.0.0")
    @PutMapping
    @Secured(action = ActionTypes.WRITE, apiType = ApiType.ADMIN_API)
    public Result<String> update(UpdateClusterForm updateClusterForm) throws Exception {
        updateClusterForm.validate();
        
        ClusterMetadata clusterMetadata = new ClusterMetadata();
        clusterMetadata.setHealthyCheckPort(updateClusterForm.getCheckPort());
        clusterMetadata.setUseInstancePortForCheck(updateClusterForm.isUseInstancePort4Check());
        AbstractHealthChecker healthChecker =
            HealthCheckerFactory.deserialize(updateClusterForm.getHealthChecker());
        clusterMetadata.setHealthChecker(healthChecker);
        clusterMetadata.setHealthyCheckType(healthChecker.getType());
        clusterMetadata
            .setExtendData(UtilsAndCommons.parseMetadata(updateClusterForm.getMetadata()));
        
        clusterOperatorV2.updateClusterMetadata(updateClusterForm.getNamespaceId(),
            updateClusterForm.getGroupName(),
            updateClusterForm.getServiceName(), updateClusterForm.getClusterName(),
            clusterMetadata);
        
        return Result.success("ok");
    }
}
