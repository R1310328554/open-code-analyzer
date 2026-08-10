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

package com.alibaba.nacos.persistence.repository.embedded.sql;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 封装一条数据库写操作（INSERT/UPDATE/DELETE）。
 *
 * <p>携带执行序号、SQL 文本、占位符参数及“更新失败是否回滚”标志，供嵌入式存储批量事务提交使用。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ModifyRequest implements Serializable {
    
    private static final long serialVersionUID = 4548851816596520564L;
    
    /** 在同一批次中的执行顺序号。 */
    private int executeNo;
    
    /** 待执行的 SQL 语句。 */
    private String sql;
    
    /** 更新影响行数小于 1 时是否触发事务回滚。 */
    private boolean rollBackOnUpdateFail = Boolean.FALSE;
    
    /** SQL 占位符参数数组。 */
    private Object[] args;
    
    public ModifyRequest() {
    }
    
    public ModifyRequest(String sql) {
        this.sql = sql;
    }
    
    public int getExecuteNo() {
        return executeNo;
    }
    
    public void setExecuteNo(int executeNo) {
        this.executeNo = executeNo;
    }
    
    public String getSql() {
        return sql;
    }
    
    public void setSql(String sql) {
        this.sql = sql;
    }
    
    public Object[] getArgs() {
        return args;
    }
    
    public void setArgs(Object[] args) {
        this.args = args;
    }
    
    public boolean isRollBackOnUpdateFail() {
        return rollBackOnUpdateFail;
    }
    
    public void setRollBackOnUpdateFail(boolean rollBackOnUpdateFail) {
        this.rollBackOnUpdateFail = rollBackOnUpdateFail;
    }
    
    @Override
    public String toString() {
        return "SQL{" + "executeNo=" + executeNo + ", sql='" + sql + '\'' + ", args="
            + Arrays.toString(args) + '}';
    }
}
