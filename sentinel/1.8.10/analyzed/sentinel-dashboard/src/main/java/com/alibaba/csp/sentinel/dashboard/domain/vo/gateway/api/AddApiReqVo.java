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
package com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.api;

import java.util.List;

/**
 * 新增网关 API 定义请求体，指定目标机器与 API 名称及 URL 匹配谓词列表。
 *
 * @author cdfive
 * @since 1.7.0
 */
public class AddApiReqVo {

    /** 目标应用名。 */
    private String app;

    /** 目标客户端机器 IP。 */
    private String ip;

    /** 目标客户端机器端口。 */
    private Integer port;

    /** 网关 API 名称（唯一标识）。 */
    private String apiName;

    /** URL 匹配谓词项列表。 */
    private List<ApiPredicateItemVo> predicateItems;

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public List<ApiPredicateItemVo> getPredicateItems() {
        return predicateItems;
    }

    public void setPredicateItems(List<ApiPredicateItemVo> predicateItems) {
        this.predicateItems = predicateItems;
    }
}

