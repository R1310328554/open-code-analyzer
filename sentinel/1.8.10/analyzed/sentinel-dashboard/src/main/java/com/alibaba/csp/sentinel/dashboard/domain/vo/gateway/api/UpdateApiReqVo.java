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
 * 更新网关 API 定义请求体，按 id 定位规则并替换谓词列表。
 *
 * @author cdfive
 * @since 1.7.0
 */
public class UpdateApiReqVo {

    /** Dashboard 侧 API 定义主键 id。 */
    private Long id;

    /** 所属应用名。 */
    private String app;

    /** 更新后的 URL 匹配谓词项列表。 */
    private List<ApiPredicateItemVo> predicateItems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public List<ApiPredicateItemVo> getPredicateItems() {
        return predicateItems;
    }

    public void setPredicateItems(List<ApiPredicateItemVo> predicateItems) {
        this.predicateItems = predicateItems;
    }
}
