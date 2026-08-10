/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.console.controller.v3.config;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.config.model.ConfigBasicInfo;
import com.alibaba.nacos.api.config.model.ConfigHistoryBasicInfo;
import com.alibaba.nacos.api.config.model.ConfigHistoryDetailInfo;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.utils.NamespaceUtil;
import com.alibaba.nacos.config.server.model.form.ConfigFormV3;
import com.alibaba.nacos.config.server.paramcheck.ConfigDefaultHttpParamExtractor;
import com.alibaba.nacos.console.proxy.config.HistoryProxy;
import com.alibaba.nacos.core.model.form.PageForm;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 控制台 v3 配置变更历史 REST 控制器，提供历史详情、分页列表、上一版本及按命名空间列举配置等接口。
 * 映射路径 {@code /v3/console/cs/history}。
 *
 * Controller for handling HTTP requests related to history operations.
 *
 * @author zhangyukun on:2024/8/16
 */
@NacosApi
@RestController
@RequestMapping("/v3/console/cs/history")
@ExtractorManager.Extractor(httpExtractor = ConfigDefaultHttpParamExtractor.class)
public class ConsoleHistoryController {
    
    /** 配置历史业务代理。 */
    private final HistoryProxy historyProxy;
    
    /**
     * 构造配置历史控制器。
     *
     * @param historyProxy 历史业务代理
     */
    @Autowired
    public ConsoleHistoryController(HistoryProxy historyProxy) {
        this.historyProxy = historyProxy;
    }
    
    /**
      * 查询指定 nid 的配置变更历史详情。
     * Query the detailed configuration history information. notes:
     *
     * @param nid        history_config_info nid
     * @param configForm config form
     * @return history config info
     */
    @Since("3.0.0")
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.CONFIG, apiType = ApiType.CONSOLE_API)
    public Result<ConfigHistoryDetailInfo> getConfigHistoryInfo(ConfigFormV3 configForm,
        @RequestParam("nid") Long nid)
        throws NacosException {
        configForm.validate();
        String dataId = configForm.getDataId();
        String groupName = configForm.getGroupName();
        String namespaceId = NamespaceUtil.processNamespaceParameter(configForm.getNamespaceId());
        return Result
            .success(historyProxy.getConfigHistoryInfo(dataId, groupName, namespaceId, nid));
    }
    
    /**
      * 分页查询配置变更历史列表。
     * Query the list history config. notes:
     *
     * @param configForm config form
     * @param pageForm   page form
     * @return the page of history config.
     */
    @Since("3.0.0")
    @GetMapping("/list")
    @Secured(action = ActionTypes.READ, signType = SignType.CONFIG, apiType = ApiType.CONSOLE_API)
    public Result<Page<ConfigHistoryBasicInfo>> listConfigHistory(ConfigFormV3 configForm,
        PageForm pageForm)
        throws NacosException {
        configForm.validate();
        pageForm.validate();
        int pageSize = Math.min(500, pageForm.getPageSize());
        int pageNo = pageForm.getPageNo();
        String dataId = configForm.getDataId();
        String groupName = configForm.getGroupName();
        String namespaceId = NamespaceUtil.processNamespaceParameter(configForm.getNamespaceId());
        return Result.success(
            historyProxy.listConfigHistory(dataId, groupName, namespaceId, pageNo, pageSize));
    }
    
    /**
      * 查询上一版本配置历史详情。
     * Query previous config history information. notes:
     *
     * @param id         config_info id
     * @param configForm config form
     * @return history config info
     */
    @Since("3.0.0")
    @GetMapping(value = "/previous")
    @Secured(action = ActionTypes.READ, signType = SignType.CONFIG, apiType = ApiType.CONSOLE_API)
    public Result<ConfigHistoryDetailInfo> getPreviousConfigHistoryInfo(ConfigFormV3 configForm,
        @RequestParam("id") Long id) throws NacosException {
        configForm.validate();
        String dataId = configForm.getDataId();
        String groupName = configForm.getGroupName();
        String namespaceId = NamespaceUtil.processNamespaceParameter(configForm.getNamespaceId());
        return Result
            .success(historyProxy.getPreviousConfigHistoryInfo(dataId, groupName, namespaceId, id));
    }
    
    /**
      * 按命名空间列举全部配置摘要。
     * Query configs list by namespace.
     *
     * @param namespaceId config_info namespace
     * @return list
     */
    @Since("3.0.0")
    @GetMapping(value = "/configs")
    @Secured(action = ActionTypes.READ, signType = SignType.CONFIG, apiType = ApiType.CONSOLE_API)
    public Result<List<ConfigBasicInfo>> getConfigsByTenant(
        @RequestParam("namespaceId") String namespaceId)
        throws NacosException {
        namespaceId = NamespaceUtil.processNamespaceParameter(namespaceId);
        return Result.success(historyProxy.getConfigsByTenant(namespaceId));
    }
    
}
