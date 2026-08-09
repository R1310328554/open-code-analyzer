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
package com.alibaba.csp.sentinel.transport.util;

import java.util.List;

import com.alibaba.csp.sentinel.datasource.WritableDataSource;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

/**
 * 可写数据源注册表：供 HTTP 命令修改流控/降级/授权/系统规则时持久化。
 * 各规则类型对应一个 {@link WritableDataSource}，由应用启动时注册。
 *
 * @author Eric Zhao
 */
public final class WritableDataSourceRegistry {

    /** 流控规则可写数据源。 */
    private static WritableDataSource<List<FlowRule>> flowDataSource = null;
    private static WritableDataSource<List<AuthorityRule>> authorityDataSource = null;
    private static WritableDataSource<List<DegradeRule>> degradeDataSource = null;
    private static WritableDataSource<List<SystemRule>> systemSource = null;

    /** 注册流控规则可写数据源。 */
    public static synchronized void registerFlowDataSource(WritableDataSource<List<FlowRule>> datasource) {
        flowDataSource = datasource;
    }

    public static synchronized void registerAuthorityDataSource(WritableDataSource<List<AuthorityRule>> dataSource) {
        authorityDataSource = dataSource;
    }

    public static synchronized void registerDegradeDataSource(WritableDataSource<List<DegradeRule>> dataSource) {
        degradeDataSource = dataSource;
    }

    public static synchronized void registerSystemDataSource(WritableDataSource<List<SystemRule>> dataSource) {
        systemSource = dataSource;
    }

    /** @return 已注册的流控规则数据源，可能为 null。 */
    public static WritableDataSource<List<FlowRule>> getFlowDataSource() {
        return flowDataSource;
    }

    public static WritableDataSource<List<AuthorityRule>> getAuthorityDataSource() {
        return authorityDataSource;
    }

    public static WritableDataSource<List<DegradeRule>> getDegradeDataSource() {
        return degradeDataSource;
    }

    /** @return 已注册的系统规则数据源，可能为 null。 */
    public static WritableDataSource<List<SystemRule>> getSystemSource() {
        return systemSource;
    }

    private WritableDataSourceRegistry() {}
}
