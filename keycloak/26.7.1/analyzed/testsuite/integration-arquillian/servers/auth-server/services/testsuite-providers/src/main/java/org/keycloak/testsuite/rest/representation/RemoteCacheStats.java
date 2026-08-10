/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.testsuite.rest.representation;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.infinispan.client.hotrod.ServerStatistics;

/**
 * 远程 Infinispan 缓存统计信息表示，用于测试套件查询缓存指标。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class RemoteCacheStats {

    /** 本地存储操作次数。 */
    @JsonProperty(ServerStatistics.STORES)
    private Integer stores;

    /** 全局存储操作次数。 */
    @JsonProperty("globalStores")
    private Integer globalStores;

    /** 其他未显式映射的统计项。 */
    private Map<String, String> otherStats = new HashMap<>();


    /** 返回本地存储次数。 */
    public Integer getStores() {
        return stores;
    }

    /** 设置本地存储次数。 */
    public void setStores(Integer stores) {
        this.stores = stores;
    }

    /** 返回全局存储次数。 */
    public Integer getGlobalStores() {
        return globalStores;
    }

    /** 设置全局存储次数。 */
    public void setGlobalStores(Integer globalStores) {
        this.globalStores = globalStores;
    }

    /** 返回其他统计项映射。 */
    @JsonAnyGetter
    public Map<String, String> getOtherStats() {
        return otherStats;
    }

    /** 动态添加未映射的统计项。 */
    @JsonAnySetter
    public void setOtherStats(String name, String value) {
        otherStats.put(name, value);
    }
}
