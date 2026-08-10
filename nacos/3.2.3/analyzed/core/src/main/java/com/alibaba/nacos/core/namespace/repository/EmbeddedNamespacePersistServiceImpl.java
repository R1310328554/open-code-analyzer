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

package com.alibaba.nacos.core.namespace.repository;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.core.namespace.model.TenantInfo;
import com.alibaba.nacos.persistence.configuration.condition.ConditionOnEmbeddedStorage;
import com.alibaba.nacos.persistence.datasource.DataSourceService;
import com.alibaba.nacos.persistence.datasource.DynamicDataSource;
import com.alibaba.nacos.persistence.model.event.DerbyImportEvent;
import com.alibaba.nacos.persistence.repository.embedded.EmbeddedStorageContextHolder;
import com.alibaba.nacos.persistence.repository.embedded.operate.DatabaseOperate;
import com.alibaba.nacos.plugin.datasource.MapperManager;
import com.alibaba.nacos.plugin.datasource.constants.CommonConstant;
import com.alibaba.nacos.plugin.datasource.constants.TableConstant;
import com.alibaba.nacos.plugin.datasource.mapper.TenantInfoMapper;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.alibaba.nacos.core.namespace.repository.NamespaceRowMapperInjector.TENANT_INFO_ROW_MAPPER;

/**
 * 内嵌存储（Derby）下的命名空间持久化实现：通过 {@link DatabaseOperate} 与 {@link EmbeddedStorageContextHolder} 执行 tenant_info 表 CRUD。
 * EmbeddedOtherPersistServiceImpl.
 *
 * @author lixiaoshuang
 */
@SuppressWarnings("checkstyle:linelength")
@Conditional(value = ConditionOnEmbeddedStorage.class)
@Service("embeddedOtherPersistServiceImpl")
public class EmbeddedNamespacePersistServiceImpl implements NamespacePersistService {
    
    /** 数据源服务，提供数据库类型等信息。 */
    private DataSourceService dataSourceService;
    
    /** 内嵌数据库操作门面。 */
    private final DatabaseOperate databaseOperate;
    
    /** SQL Mapper 管理器，按数据源类型生成 tenant_info 语句。 */
    private MapperManager mapperManager;
    
    /**
     * 构造并初始化数据源、Mapper 及 Derby 导入事件订阅。
     *
     * @param databaseOperate databaseOperate
     */
    public EmbeddedNamespacePersistServiceImpl(DatabaseOperate databaseOperate) {
        this.databaseOperate = databaseOperate;
        this.dataSourceService = DynamicDataSource.getInstance().getDataSource();
        Boolean isDataSourceLogEnable = EnvUtil
            .getProperty(CommonConstant.NACOS_PLUGIN_DATASOURCE_LOG, Boolean.class, false);
        this.mapperManager = MapperManager.instance(isDataSourceLogEnable);
        NotifyCenter.registerToSharePublisher(DerbyImportEvent.class);
    }
    
    /** {@inheritDoc} — 通过 EmbeddedStorageContextHolder 批量提交插入 SQL。 */
    @Override
    public void insertTenantInfoAtomic(String kp, String tenantId, String tenantName,
        String tenantDesc,
        String createResource, final long time) {
        
        TenantInfoMapper tenantInfoMapper = mapperManager
            .findMapper(dataSourceService.getDataSourceType(), TableConstant.TENANT_INFO);
        final String sql = tenantInfoMapper.insert(Arrays
            .asList("kp", "tenant_id", "tenant_name", "tenant_desc", "create_source", "gmt_create",
                "gmt_modified"));
        final Object[] args =
            new Object[] {kp, tenantId, tenantName, tenantDesc, createResource, time, time};
        
        EmbeddedStorageContextHolder.addSqlContext(sql, args);
        
        try {
            boolean result =
                databaseOperate.update(EmbeddedStorageContextHolder.getCurrentSqlContext());
            if (!result) {
                throw new NacosRuntimeException(NacosException.SERVER_ERROR,
                    "Namespace creation failed");
            }
        } finally {
            EmbeddedStorageContextHolder.cleanAllContext();
        }
    }
    
    /** {@inheritDoc} — 按 kp + tenantId 删除 tenant_info 行。 */
    @Override
    public void removeTenantInfoAtomic(final String kp, final String tenantId) {
        TenantInfoMapper tenantInfoMapper = mapperManager
            .findMapper(dataSourceService.getDataSourceType(), TableConstant.TENANT_INFO);
        
        EmbeddedStorageContextHolder
            .addSqlContext(tenantInfoMapper.delete(Arrays.asList("kp", "tenant_id")), kp, tenantId);
        try {
            databaseOperate.update(EmbeddedStorageContextHolder.getCurrentSqlContext());
        } finally {
            EmbeddedStorageContextHolder.cleanAllContext();
        }
    }
    
    /** {@inheritDoc} — 更新命名空间名称、描述及 gmt_modified。 */
    @Override
    public void updateTenantNameAtomic(String kp, String tenantId, String tenantName,
        String tenantDesc) {
        
        TenantInfoMapper tenantInfoMapper = mapperManager
            .findMapper(dataSourceService.getDataSourceType(), TableConstant.TENANT_INFO);
        final String sql = tenantInfoMapper
            .update(Arrays.asList("tenant_name", "tenant_desc", "gmt_modified"),
                Arrays.asList("kp", "tenant_id"));
        final Object[] args =
            new Object[] {tenantName, tenantDesc, System.currentTimeMillis(), kp, tenantId};
        
        EmbeddedStorageContextHolder.addSqlContext(sql, args);
        
        try {
            boolean result =
                databaseOperate.update(EmbeddedStorageContextHolder.getCurrentSqlContext());
            if (!result) {
                throw new NacosRuntimeException(NacosException.SERVER_ERROR,
                    "Namespace update failed");
            }
        } finally {
            EmbeddedStorageContextHolder.cleanAllContext();
        }
    }
    
    /** {@inheritDoc} — 按 kp 查询该分区下全部命名空间。 */
    @Override
    public List<TenantInfo> findTenantByKp(String kp) {
        TenantInfoMapper tenantInfoMapper = mapperManager
            .findMapper(dataSourceService.getDataSourceType(), TableConstant.TENANT_INFO);
        String sql = tenantInfoMapper
            .select(Arrays.asList("tenant_id", "tenant_name", "tenant_desc"),
                Collections.singletonList("kp"));
        return databaseOperate.queryMany(sql, new Object[] {kp}, TENANT_INFO_ROW_MAPPER);
        
    }
    
    /** {@inheritDoc} — 按 kp + tenantId 查询单个命名空间。 */
    @Override
    public TenantInfo findTenantByKp(String kp, String tenantId) {
        TenantInfoMapper tenantInfoMapper = mapperManager
            .findMapper(dataSourceService.getDataSourceType(), TableConstant.TENANT_INFO);
        String sql = tenantInfoMapper
            .select(Arrays.asList("tenant_id", "tenant_name", "tenant_desc"),
                Arrays.asList("kp", "tenant_id"));
        return databaseOperate.queryOne(sql, new Object[] {kp, tenantId}, TENANT_INFO_ROW_MAPPER);
        
    }
    
    /** {@inheritDoc} — 将 {@code *} 转为 SQL LIKE {@code %}，并转义下划线。 */
    @Override
    public String generateLikeArgument(String s) {
        String underscore = "_";
        if (s.contains(underscore)) {
            s = s.replaceAll(underscore, "\\\\_");
        }
        String fuzzySearchSign = "\\*";
        String sqlLikePercentSign = "%";
        if (s.contains(PATTERN_STR)) {
            return s.replaceAll(fuzzySearchSign, sqlLikePercentSign);
        } else {
            return s;
        }
    }
    
    /** {@inheritDoc} — 尝试 COUNT 探测表是否存在。 */
    @Override
    public boolean isExistTable(String tableName) {
        String sql = String.format("SELECT COUNT(*) FROM %s ", tableName);
        try {
            databaseOperate.queryOne(sql, Integer.class);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
    
    /** {@inheritDoc} — 统计指定 tenantId 的记录数，用于存在性判断。 */
    @Override
    public int tenantInfoCountByTenantId(String tenantId) {
        if (Objects.isNull(tenantId)) {
            throw new IllegalArgumentException("tenantId can not be null");
        }
        TenantInfoMapper tenantInfoMapper = mapperManager
            .findMapper(dataSourceService.getDataSourceType(), TableConstant.TENANT_INFO);
        String sql = tenantInfoMapper.count(Collections.singletonList("tenant_id"));
        Integer result = databaseOperate.queryOne(sql, new String[] {tenantId}, Integer.class);
        if (result == null) {
            return 0;
        }
        return result;
    }
}
