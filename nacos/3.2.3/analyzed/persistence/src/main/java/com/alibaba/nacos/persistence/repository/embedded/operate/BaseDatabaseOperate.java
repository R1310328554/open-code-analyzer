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

package com.alibaba.nacos.persistence.repository.embedded.operate;

import com.alibaba.nacos.common.utils.ExceptionUtil;
import com.alibaba.nacos.common.utils.LoggerUtils;
import com.alibaba.nacos.persistence.repository.embedded.sql.ModifyRequest;
import com.alibaba.nacos.persistence.utils.DerbyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

/**
 * Derby 嵌入式数据库通用操作接口。
 *
 * <p>封装 {@link JdbcTemplate} 查询/更新、事务批量提交及数据导入等默认实现，供集群与单机 {@link DatabaseOperate} 实现类复用。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public interface BaseDatabaseOperate extends DatabaseOperate {
    
    Logger LOGGER = LoggerFactory.getLogger(BaseDatabaseOperate.class);
    
    /**
     * 无参数单条查询，结果映射为目标类型。
     *
     * <p>无匹配行时返回 null 而非抛异常。</p>
     *
     * @param jdbcTemplate {@link JdbcTemplate}
     * @param sql          sql
     * @param cls          target type
     * @param <R>          target type
     * @return R
     */
    default <R> R queryOne(JdbcTemplate jdbcTemplate, String sql, Class<R> cls) {
        try {
            return jdbcTemplate.queryForObject(sql, cls);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] can't get connection : {}",
                ExceptionUtil.getAllExceptionMsg(e));
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("[db-error] DataAccessException : {}",
                ExceptionUtil.getAllExceptionMsg(e));
            throw e;
        }
    }
    
    /** 带占位符参数的单条查询。 */
    default <R> R queryOne(JdbcTemplate jdbcTemplate, String sql, Object[] args, Class<R> cls) {
        try {
            return jdbcTemplate.queryForObject(sql, args, cls);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] {}", e.toString());
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}", sql,
                args,
                ExceptionUtil.getAllExceptionMsg(e));
            throw e;
        }
    }
    
    /** 使用 {@link RowMapper} 映射单条查询结果。 */
    default <R> R queryOne(JdbcTemplate jdbcTemplate, String sql, Object[] args,
        RowMapper<R> mapper) {
        try {
            return jdbcTemplate.queryForObject(sql, args, mapper);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] {}", e.toString());
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}", sql,
                args,
                ExceptionUtil.getAllExceptionMsg(e));
            throw e;
        }
    }
    
    /** 使用 RowMapper 查询多条记录。 */
    default <R> List<R> queryMany(JdbcTemplate jdbcTemplate, String sql, Object[] args,
        RowMapper<R> mapper) {
        try {
            return jdbcTemplate.query(sql, args, mapper);
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] {}", e.toString());
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}", sql,
                args,
                ExceptionUtil.getAllExceptionMsg(e));
            throw e;
        }
    }
    
    /** 按目标 Class 查询列表（如 Integer、String）。 */
    default <R> List<R> queryMany(JdbcTemplate jdbcTemplate, String sql, Object[] args,
        Class<R> rClass) {
        try {
            return jdbcTemplate.queryForList(sql, args, rClass);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] {}", e.toString());
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}", sql,
                args,
                ExceptionUtil.getAllExceptionMsg(e));
            throw e;
        }
    }
    
    /** 查询多行并返回列名到值的 Map 列表。 */
    default List<Map<String, Object>> queryMany(JdbcTemplate jdbcTemplate, String sql,
        Object[] args) {
        try {
            return jdbcTemplate.queryForList(sql, args);
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] {}", e.toString());
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}", sql,
                args,
                ExceptionUtil.getAllExceptionMsg(e));
            throw e;
        }
    }
    
    /**
     * 在事务中顺序执行多条 {@link ModifyRequest}。
     *
     * @param transactionTemplate {@link TransactionTemplate}
     * @param jdbcTemplate        {@link JdbcTemplate}
     * @param contexts            {@link List} ModifyRequest list
     * @return {@link Boolean}
     */
    default Boolean update(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate,
        List<ModifyRequest> contexts) {
        return update(transactionTemplate, jdbcTemplate, contexts, null);
    }
    
    /**
     * 带成功/失败回调的事务更新（修复 #3617）。
     *
     * <p>单条 SQL 更新影响行数为 0 且标记回滚时，整笔事务回滚。</p>
     *
     * @param transactionTemplate {@link TransactionTemplate}
     * @param jdbcTemplate        {@link JdbcTemplate}
     * @param contexts            {@link List} ModifyRequest list
     * @return {@link Boolean}
     */
    default Boolean update(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate,
        List<ModifyRequest> contexts, BiConsumer<Boolean, Throwable> consumer) {
        boolean updateResult = Boolean.FALSE;
        try {
            updateResult = transactionTemplate.execute(status -> {
                String[] errSql = new String[] {null};
                Object[][] args = new Object[][] {null};
                try {
                    contexts.forEach(pair -> {
                        errSql[0] = pair.getSql();
                        args[0] = pair.getArgs();
                        boolean rollBackOnUpdateFail = pair.isRollBackOnUpdateFail();
                        // 调试模式下打印当前执行的 SQL 与参数
                        LoggerUtils.printIfDebugEnabled(LOGGER, "current sql : {}", errSql[0]);
                        LoggerUtils.printIfDebugEnabled(LOGGER, "current args : {}", args[0]);
                        int row = jdbcTemplate.update(pair.getSql(), pair.getArgs());
                        if (rollBackOnUpdateFail && row < 1) {
                            LoggerUtils.printIfDebugEnabled(LOGGER, "SQL update affected {} rows ",
                                row);
                            throw new IllegalTransactionStateException("Illegal transaction");
                        }
                    });
                    if (consumer != null) {
                        consumer.accept(Boolean.TRUE, null);
                    }
                    return Boolean.TRUE;
                } catch (BadSqlGrammarException | DataIntegrityViolationException e) {
                    LOGGER.error("[db-error] sql : {}, args : {}, error : {}", errSql[0], args[0],
                        e.toString());
                    if (consumer != null) {
                        consumer.accept(Boolean.FALSE, e);
                    }
                    return Boolean.FALSE;
                } catch (CannotGetJdbcConnectionException e) {
                    LOGGER.error("[db-error] sql : {}, args : {}, error : {}", errSql[0], args[0],
                        e.toString());
                    throw e;
                } catch (DataAccessException e) {
                    LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}",
                        errSql[0], args[0],
                        ExceptionUtil.getAllExceptionMsg(e));
                    throw e;
                }
            });
        } catch (IllegalTransactionStateException e) {
            LoggerUtils.printIfDebugEnabled(LOGGER, "Roll back transaction for {} ",
                e.getMessage());
            if (consumer != null) {
                consumer.accept(Boolean.FALSE, e);
            }
        }
        return updateResult;
    }
    
    /**
     * 批量导入外部 SQL 到 Derby。
     *
     * <p>对 INSERT 语句做 Derby 方言修正后使用 batchUpdate 提交。</p>
     *
     * @param template {@link JdbcTemplate}
     * @param requests {@link List} ModifyRequest list
     * @return {@link Boolean}
     */
    default Boolean doDataImport(JdbcTemplate template, List<ModifyRequest> requests) {
        final String[] sql =
            requests.stream().map(ModifyRequest::getSql).map(DerbyUtils::insertStatementCorrection)
                .toArray(String[]::new);
        int[] affect = template.batchUpdate(sql);
        return IntStream.of(affect).count() == requests.size();
    }
    
}
