/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.ai.pipeline.repository;

import com.alibaba.nacos.ai.pipeline.model.PipelineExecution;
import com.alibaba.nacos.ai.pipeline.model.PipelineExecutionStatus;
import com.alibaba.nacos.ai.pipeline.model.PipelineNodeResult;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.persistence.datasource.DynamicDataSource;
import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JDBC-based implementation of {@link PipelineExecutionRepository}.
 * <p>基于 JDBC 的流水线执行记录仓储实现，通过 {@link DynamicDataSource} 获取 {@link JdbcTemplate}，将执行记录持久化到 {@code pipeline_execution} 表；pipeline 字段（{@link PipelineNodeResult} 列表）以 JSON 序列化存储。</p>
 *
 * <p>Uses {@link DynamicDataSource} to obtain a {@link JdbcTemplate} and persists pipeline execution
 * records to the {@code pipeline_execution} table. The pipeline field (List of PipelineNodeResult)
 * is serialized/deserialized as JSON using {@link JacksonUtils}.</p>
 *
 * @author kiro
 * @since 3.2.0
 */
public class PipelineExecutionRepositoryImpl implements PipelineExecutionRepository {
    
    private static final Logger LOGGER =
        LoggerFactory.getLogger(PipelineExecutionRepositoryImpl.class);
    
    /** 插入流水线执行记录的 SQL。 */
    private static final String SQL_INSERT = "INSERT INTO pipeline_execution "
        + "(execution_id, resource_type, resource_name, namespace_id, version, status, pipeline, create_time, update_time) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    /** 按 execution_id 更新状态与 pipeline 的 SQL。 */
    private static final String SQL_UPDATE =
        "UPDATE pipeline_execution SET status=?, pipeline=?, update_time=? "
            + "WHERE execution_id=?";
    
    /** 按 execution_id 查询单条记录的 SQL。 */
    private static final String SQL_FIND_BY_ID =
        "SELECT * FROM pipeline_execution WHERE execution_id=?";
    
    /** 按资源维度查询执行记录（按创建时间倒序）的 SQL。 */
    private static final String SQL_FIND_BY_RESOURCE = "SELECT * FROM pipeline_execution "
        + "WHERE resource_type=? AND resource_name=? AND namespace_id=? AND version=? "
        + "ORDER BY create_time DESC";
    
    private static final PipelineExecutionRowMapper ROW_MAPPER = new PipelineExecutionRowMapper();
    
    /** 测试注入的 JdbcTemplate，非空时优先使用。 */
    private final JdbcTemplate injectedJdbcTemplate;
    
    /** 测试注入的数据源类型，用于方言相关 SQL 拼接。 */
    private final String injectedDataSourceType;
    
    /**
     * Default constructor. Uses {@link DynamicDataSource} to obtain the JdbcTemplate.
     * <p>默认构造，运行时从 {@link DynamicDataSource} 获取 JdbcTemplate。</p>
     */
    public PipelineExecutionRepositoryImpl() {
        this.injectedJdbcTemplate = null;
        this.injectedDataSourceType = null;
    }
    
    /**
     * Constructor for testing. Accepts a JdbcTemplate directly.
     * <p>测试构造，直接注入 JdbcTemplate。</p>
     *
     * @param jdbcTemplate the JdbcTemplate to use
     */
    public PipelineExecutionRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, null);
    }
    
    /**
     * Constructor for testing. Accepts a JdbcTemplate and datasource type directly.
     * <p>测试构造，同时注入 JdbcTemplate 与数据源类型以生成方言 SQL。</p>
     *
     * @param jdbcTemplate the JdbcTemplate to use
     * @param dataSourceType datasource type used to build dialect-specific SQL
     */
    public PipelineExecutionRepositoryImpl(JdbcTemplate jdbcTemplate, String dataSourceType) {
        this.injectedJdbcTemplate = jdbcTemplate;
        this.injectedDataSourceType = dataSourceType;
    }
    
    /** 获取 JdbcTemplate：优先使用注入实例，否则从动态数据源获取。 */
    private JdbcTemplate getJdbcTemplate() {
        if (injectedJdbcTemplate != null) {
            return injectedJdbcTemplate;
        }
        return DynamicDataSource.getInstance().getDataSource().getJdbcTemplate();
    }
    
    /** 获取当前数据源类型，用于 LIMIT/OFFSET 等方言差异。 */
    private String getDataSourceType() {
        if (StringUtils.isNotBlank(injectedDataSourceType)) {
            return injectedDataSourceType;
        }
        if (injectedJdbcTemplate != null) {
            return DataSourceConstant.MYSQL;
        }
        return DynamicDataSource.getInstance().getDataSource().getDataSourceType();
    }
    
    /** 构建查询资源最新一条执行记录的 SQL（含方言分页子句）。 */
    String buildSingleLatestSql() {
        return appendFirstRowClause("SELECT * FROM pipeline_execution "
            + "WHERE resource_type=? AND resource_name=? AND namespace_id=? AND version=? "
            + "ORDER BY create_time DESC");
    }
    
    /** 按数据源类型追加 OFFSET/LIMIT 或 FETCH NEXT 分页子句。 */
    String appendPageClause(String baseSql, int offset, int limit) {
        String dataSourceType = getDataSourceType();
        if (DataSourceConstant.DERBY.equalsIgnoreCase(dataSourceType)
            || DataSourceConstant.ORACLE.equalsIgnoreCase(dataSourceType)) {
            return baseSql + " OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY";
        }
        return baseSql + " LIMIT " + limit + " OFFSET " + offset;
    }
    
    /** 追加仅取首行的方言子句（LIMIT 1 或 FETCH FIRST 1 ROW ONLY）。 */
    private String appendFirstRowClause(String baseSql) {
        String dataSourceType = getDataSourceType();
        if (DataSourceConstant.DERBY.equalsIgnoreCase(dataSourceType)
            || DataSourceConstant.ORACLE.equalsIgnoreCase(dataSourceType)) {
            return baseSql + " FETCH FIRST 1 ROW ONLY";
        }
        return baseSql + " LIMIT 1";
    }
    
    @Override
    public void save(PipelineExecution execution) {
        String pipelineJson = JacksonUtils.toJson(execution.getPipeline());
        getJdbcTemplate().update(SQL_INSERT, execution.getExecutionId(),
            execution.getResourceType(),
            execution.getResourceName(), execution.getNamespaceId(), execution.getVersion(),
            execution.getStatus().name(), pipelineJson, execution.getCreateTime(),
            execution.getUpdateTime());
    }
    
    @Override
    public void update(PipelineExecution execution) {
        String pipelineJson = JacksonUtils.toJson(execution.getPipeline());
        getJdbcTemplate().update(SQL_UPDATE, execution.getStatus().name(), pipelineJson,
            execution.getUpdateTime(), execution.getExecutionId());
    }
    
    @Override
    public PipelineExecution findById(String executionId) {
        try {
            return getJdbcTemplate().queryForObject(SQL_FIND_BY_ID, ROW_MAPPER, executionId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to query pipeline_execution table (table may not exist): {}",
                e.getMessage());
            return null;
        }
    }
    
    @Override
    public PipelineExecution findByResource(String resourceType, String resourceName,
        String namespaceId,
        String version) {
        try {
            List<PipelineExecution> executions =
                getJdbcTemplate().query(SQL_FIND_BY_RESOURCE, ROW_MAPPER,
                    resourceType, resourceName, namespaceId, version);
            if (executions.isEmpty()) {
                return null;
            }
            return executions.get(0);
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to query pipeline_execution table (table may not exist): {}",
                e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<PipelineExecution> findByResourceWithPage(String resourceType, String resourceName,
        String namespaceId, String version, int offset, int limit) {
        try {
            StringBuilder sql =
                new StringBuilder("SELECT * FROM pipeline_execution WHERE resource_type = ?");
            List<Object> params = new ArrayList<>();
            params.add(resourceType);
            
            if (StringUtils.isNotBlank(resourceName)) {
                sql.append(" AND resource_name = ?");
                params.add(resourceName);
            }
            if (StringUtils.isNotBlank(namespaceId)) {
                sql.append(" AND namespace_id = ?");
                params.add(namespaceId);
            }
            if (StringUtils.isNotBlank(version)) {
                sql.append(" AND version = ?");
                params.add(version);
            }
            sql.append(" ORDER BY create_time DESC");
            
            List<PipelineExecution> executions =
                getJdbcTemplate().query(sql.toString(), ROW_MAPPER, params.toArray());
            if (executions.isEmpty() || offset >= executions.size()) {
                return Collections.emptyList();
            }
            int toIndex = Math.min(executions.size(), offset + limit);
            return new ArrayList<>(executions.subList(offset, toIndex));
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to query pipeline_execution table (table may not exist): {}",
                e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public int countByResource(String resourceType, String resourceName, String namespaceId,
        String version) {
        try {
            StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM pipeline_execution WHERE resource_type = ?");
            List<Object> params = new ArrayList<>();
            params.add(resourceType);
            
            if (StringUtils.isNotBlank(resourceName)) {
                sql.append(" AND resource_name = ?");
                params.add(resourceName);
            }
            if (StringUtils.isNotBlank(namespaceId)) {
                sql.append(" AND namespace_id = ?");
                params.add(namespaceId);
            }
            if (StringUtils.isNotBlank(version)) {
                sql.append(" AND version = ?");
                params.add(version);
            }
            
            Integer count =
                getJdbcTemplate().queryForObject(sql.toString(), Integer.class, params.toArray());
            return count != null ? count : 0;
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to query pipeline_execution table (table may not exist): {}",
                e.getMessage());
            return 0;
        }
    }
    
    /**
     * RowMapper for mapping ResultSet rows to PipelineExecution objects.
     * <p>将 ResultSet 行映射为 {@link PipelineExecution}，pipeline 列反序列化为节点结果列表。</p>
     */
    private static class PipelineExecutionRowMapper implements RowMapper<PipelineExecution> {
        
        @Override
        public PipelineExecution mapRow(ResultSet rs, int rowNum) throws SQLException {
            PipelineExecution execution = new PipelineExecution();
            execution.setExecutionId(rs.getString("execution_id"));
            execution.setResourceType(rs.getString("resource_type"));
            execution.setResourceName(rs.getString("resource_name"));
            execution.setNamespaceId(rs.getString("namespace_id"));
            execution.setVersion(rs.getString("version"));
            execution.setStatus(PipelineExecutionStatus.valueOf(rs.getString("status")));
            
            String pipelineJson = rs.getString("pipeline");
            List<PipelineNodeResult> pipeline = JacksonUtils.toObj(pipelineJson,
                new TypeReference<List<PipelineNodeResult>>() {
                });
            execution.setPipeline(pipeline);
            
            execution.setCreateTime(rs.getLong("create_time"));
            execution.setUpdateTime(rs.getLong("update_time"));
            return execution;
        }
    }
}
