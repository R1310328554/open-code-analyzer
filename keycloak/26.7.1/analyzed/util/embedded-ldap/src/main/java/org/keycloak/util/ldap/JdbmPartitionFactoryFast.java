/*
 * Copyright 2022. Red Hat, Inc. and/or its affiliates
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

package org.keycloak.util.ldap;

import java.io.File;

import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.server.core.api.DnFactory;
import org.apache.directory.server.core.factory.JdbmPartitionFactory;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;

/**
 * 测试用 JDBM 分区工厂：关闭每次写入时的磁盘同步以加速嵌入式 LDAP 测试。
 *
 * @author Alexander Schwartz
 */
public class JdbmPartitionFactoryFast extends JdbmPartitionFactory {
    /**
     * 创建 JDBM 分区并禁用 syncOnWrite，减少测试中的磁盘 I/O。
     *
     * @param schemaManager 模式管理器
     * @param dnFactory DN 工厂
     * @param id 分区标识
     * @param suffix 分区后缀 DN
     * @param cacheSize 缓存大小
     * @param workingDirectory 工作目录
     * @return 配置后的 {@link JdbmPartition} 实例
     * @throws Exception 创建分区失败时
     */
    @Override
    public JdbmPartition createPartition(SchemaManager schemaManager, DnFactory dnFactory, String id, String suffix, int cacheSize, File workingDirectory) throws Exception {
        JdbmPartition partition = super.createPartition(schemaManager, dnFactory, id, suffix, cacheSize, workingDirectory);
        // 不在每次更新时写盘，略微加快测试执行
        partition.setSyncOnWrite(false);
        return partition;
    }
}
