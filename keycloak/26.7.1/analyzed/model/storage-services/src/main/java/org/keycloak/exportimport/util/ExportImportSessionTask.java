/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.exportimport.util;

import java.io.IOException;

import org.keycloak.connections.jpa.support.EntityManagers;
import org.keycloak.exportimport.ExportImportConfig;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.utils.KeycloakSessionUtil;

/**
 * 导出/导入会话任务抽象基类：在事务上下文中执行 {@link #runExportImportTask}，并将 {@link IOException} 包装为运行时异常。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class ExportImportSessionTask {

    /** 任务执行模式：批处理优化或普通模式。 */
    public enum Mode {
        /** 启用批处理优化（只读/批量插入），完成后 flush/clear。 */
        BATCHED,
        /** 普通事务模式。 */
        NORMAL
    }

    /** 以 {@link Mode#NORMAL} 模式在事务中运行任务。 */
    public void runTask(KeycloakSessionFactory factory) {
        runTask(factory, Mode.NORMAL);
    }

    /** 以指定 {@link Mode} 在事务中运行任务。 */
    public void runTask(KeycloakSessionFactory factory, Mode mode) {
        boolean useExistingSession = ExportImportConfig.isSingleTransaction();
        KeycloakSession existing = KeycloakSessionUtil.getKeycloakSession();
        if (useExistingSession && existing != null && existing.getTransactionManager().isActive()) {
            run(mode, existing);
        } else {
            KeycloakModelUtils.runJobInTransaction(factory, session -> this.run(mode, session));
        }
    }

    /** 在指定模式下执行导出/导入任务（批处理或普通）。 */
    private void run(Mode mode, KeycloakSession session) {
        Runnable task = () -> {
            try {
                runExportImportTask(session);
            } catch (IOException ioe) {
                throw new RuntimeException("Error during export/import: " + ioe.getMessage(), ioe);
            }
        };
        if (mode == Mode.BATCHED) {
            EntityManagers.runInBatch(session, task, true);
        } else {
            task.run();
        }
    }

    /** 子类实现的导出/导入核心逻辑。 */
    protected abstract void runExportImportTask(KeycloakSession session) throws IOException;
}
