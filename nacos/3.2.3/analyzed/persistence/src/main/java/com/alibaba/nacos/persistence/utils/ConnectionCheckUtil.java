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

package com.alibaba.nacos.persistence.utils;

import com.zaxxer.hikari.HikariDataSource;

/**
 * HikariCP 数据源连通性校验工具。
 *
 * <p>启动或切换数据源时主动获取连接并探测，避免控制台显示 [no datasource set] 等误导信息。</p>
 *
 * @author Long Yu
 */
public class ConnectionCheckUtil {
    
    /**
     * 校验 Hikari 数据源能否正常建立连接。
     *
     * <p>连接失败时包装为 {@link RuntimeException} 向上抛出。</p>
     *
     * @param ds HikariDataSource object
     */
    public static void checkDataSourceConnection(HikariDataSource ds) {
        // 借连接后立即探测 isClosed，确保池配置有效
            connection.isClosed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
