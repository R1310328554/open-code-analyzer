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

package com.alibaba.nacos.api.naming.pojo.healthcheck.impl;

import com.alibaba.nacos.api.naming.pojo.healthcheck.AbstractHealthChecker;
import com.alibaba.nacos.api.utils.StringUtils;

import java.util.Objects;

/**
 * MySQL 协议健康检查器实现，通过执行 SQL 命令探测数据库实例可用性。
 *
 * <p>配置数据库用户名、密码及探测 SQL，由 Nacos 服务端定期连接并执行以更新实例健康状态。</p>
 *
 * @author yangyi
 */
public class Mysql extends AbstractHealthChecker {
    
    /** 类型常量 {@code MYSQL}。 */
    public static final String TYPE = "MYSQL";
    
    private static final long serialVersionUID = 7928108094599401491L;
    
    /** 数据库用户名。 */
    private String user;
    
    /** 数据库密码。 */
    private String pwd;
    
    /** 健康探测 SQL 命令。 */
    private String cmd;
    
    /** 构造 MySQL 类型健康检查器。 */
    public Mysql() {
        super(Mysql.TYPE);
    }
    
    /** 获取探测 SQL 命令。 */
    public String getCmd() {
        return this.cmd;
    }
    
    /** 获取数据库密码。 */
    public String getPwd() {
        return this.pwd;
    }
    
    /** 获取数据库用户名。 */
    public String getUser() {
        return this.user;
    }
    
    /** 设置数据库用户名。 */
    public void setUser(final String user) {
        this.user = user;
    }
    
    /** 设置探测 SQL 命令。 */
    public void setCmd(final String cmd) {
        this.cmd = cmd;
    }
    
    /** 设置数据库密码。 */
    public void setPwd(final String pwd) {
        this.pwd = pwd;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(user, pwd, cmd);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Mysql)) {
            return false;
        }
        
        final Mysql other = (Mysql) obj;
        
        if (!StringUtils.equals(user, other.getUser())) {
            return false;
        }
        
        if (!StringUtils.equals(pwd, other.getPwd())) {
            return false;
        }
        
        return StringUtils.equals(cmd, other.getCmd());
    }
    
    @Override
    public Mysql clone() throws CloneNotSupportedException {
        final Mysql config = new Mysql();
        config.setUser(getUser());
        config.setPwd(getPwd());
        config.setCmd(getCmd());
        return config;
    }
}
