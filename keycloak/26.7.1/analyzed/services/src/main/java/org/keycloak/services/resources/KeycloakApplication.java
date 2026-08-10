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
package org.keycloak.services.resources;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.core.Application;

import org.keycloak.common.Profile;
import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.common.util.Time;
import org.keycloak.exportimport.ExportImportConfig;
import org.keycloak.exportimport.ExportImportManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.dblock.DBLockManager;
import org.keycloak.models.dblock.DBLockProvider;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.PostMigrationEvent;
import org.keycloak.services.DefaultKeycloakSessionFactory;
import org.keycloak.services.managers.ApplianceBootstrap;

import io.quarkus.runtime.Quarkus;
import org.jboss.logging.Logger;

/**
 * Keycloak 应用抽象基类。
 * <p>负责临时目录初始化、加密集成、会话工厂创建、领域引导（master realm、导入、临时管理员）及优雅关闭。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class KeycloakApplication extends Application {

    /** 系统属性键：Keycloak 临时目录路径 */
    private static final String KC_TMPDIR = "kc.io.tmpdir";

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(KeycloakApplication.class);

    /** 全局会话工厂（启动完成后可用） */
    private static volatile DefaultKeycloakSessionFactory sessionFactory;

    /**
     * 获取当前应用初始化的临时目录路径。
     * <br>
     * 目录不保证已存在
     */
    public static String getTmpDirectory() {
        return Optional.ofNullable(System.getProperty(KC_TMPDIR)).orElseThrow(() -> new RuntimeException("No temporary directory was configured."));
    }

    /** 根据数据目录设置 {@link #KC_TMPDIR} 系统属性 */
    protected void initTmpDirectory() {
        String dataDir = getDataDir();
        if (dataDir != null) {
            File tmpDir = new File(dataDir, "tmp");
            System.setProperty(KC_TMPDIR, tmpDir.getAbsolutePath());
        }
    }

    /** @return 数据目录路径，子类实现 */
    protected abstract String getDataDir();

    /** 启动应用：初始化临时目录、加密、会话工厂并执行引导 */
    protected synchronized void startup() {
        logger.debugv("Application: {0}", this.getClass().getName());
        initTmpDirectory();
        Profile.getInstance().logUnsupportedFeatures();
        CryptoIntegration.init(KeycloakApplication.class.getClassLoader());
        KeycloakApplication.sessionFactory = createSessionFactory();
        runBootstrap(KeycloakApplication.sessionFactory);
    }

    /** 在 DB 锁保护下执行引导并可选运行导出 */
    private void runBootstrap(DefaultKeycloakSessionFactory keycloakSessionFactory) {
        var startTime = System.nanoTime();

        keycloakSessionFactory.init();

        if ("exit_before_bootstrap".equals(System.getProperty("kc.launch.mode"))) {
            Quarkus.asyncExit(0);
            return;
        }

        setTransactionTimeout(keycloakSessionFactory);
        var exportImportManager = KeycloakModelUtils.runJobInTransactionWithResult(keycloakSessionFactory, session -> {
            DBLockManager dbLockManager = new DBLockManager(session);
            dbLockManager.checkForcedUnlock();
            DBLockProvider dbLock = dbLockManager.getDBLock();
            dbLock.waitForLock(DBLockProvider.Namespace.KEYCLOAK_BOOT);
            try {
                return bootstrap(session);
            } finally {
                dbLock.releaseLock();
            }
        });

        if (exportImportManager.isRunExport()) {
            // the transaction timeout is stored in a thread-local, when exports creates a new transaction, it should fetch it.
            exportImportManager.runExport();
        }

        resetTransactionTimeout(keycloakSessionFactory);
        keycloakSessionFactory.publish(new PostMigrationEvent(keycloakSessionFactory));
        keycloakSessionFactory.setBootstrapCompleted();

        var duration = Duration.ofNanos(System.nanoTime() - startTime);
        logger.infof("Bootstrap completed in %f seconds", (double) duration.toMillis() / 1000);
    }

    /** @return 引导阶段事务超时（秒），默认 5 分钟 */
    protected int getTransactionTimeout(DefaultKeycloakSessionFactory sessionFactory) {
        return Math.toIntExact(TimeUnit.MINUTES.toSeconds(5));
    }

    /** 关闭会话工厂并释放资源 */
    protected synchronized void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }

    /** 发布 {@link ShutdownDelayInitiatedEvent}，标记进入关闭延迟阶段 */
    protected synchronized void shutdownDelayInitiated() {
        if (sessionFactory == null) {
            return;
        }
        sessionFactory.publish(new ShutdownDelayInitiatedEvent(Instant.ofEpochMilli(Time.currentTimeMillis())));
    }

    /** 引导 master 领域、导入配置并创建管理员用户 */
    protected ExportImportManager bootstrap(KeycloakSession session) {
        logger.debug("bootstrap");
        boolean existing = ExportImportConfig.isSingleTransaction();
        ExportImportConfig.setSingleTransaction(true);
        try {
            ApplianceBootstrap applianceBootstrap = new ApplianceBootstrap(session);
            var exportImportManager = new ExportImportManager(session);
            var newInstall = applianceBootstrap.isNewInstall();
            if (newInstall) {
                if (!exportImportManager.isImportMasterIncluded()) {
                    applianceBootstrap.createMasterRealm();
                }
                // 以下操作也在初始引导事务中执行，失败则服务器无法启动 - if there is a problem, the server won't be initialized at all
                exportImportManager.runImport();
                createTemporaryAdmin(session);
            } else {
                exportImportManager.runImport();
            }
            return exportImportManager;
        } finally {
            ExportImportConfig.setSingleTransaction(existing);
        }
    }

    /** 创建临时管理员账户，由 Quarkus 等子类实现 */
    protected abstract void createTemporaryAdmin(KeycloakSession session);

    /** 创建会话工厂，由运行时子类实现 */
    protected abstract DefaultKeycloakSessionFactory createSessionFactory();

    /**
     * 警告：仅供测试逻辑使用。 Will return null if there is no current KeycloakApplication, or if the
     * startup has not yet reached the point of setting this value.
     */
    /** @return 当前会话工厂，未启动完成时可能为 null */
    public static DefaultKeycloakSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /** 引导前设置事务超时 */
    private void setTransactionTimeout(DefaultKeycloakSessionFactory keycloakSessionFactory) {
        try {
            var transactionTimeoutSeconds = getTransactionTimeout(keycloakSessionFactory);
            KeycloakModelUtils.setTransactionLimit(keycloakSessionFactory, transactionTimeoutSeconds);
        } catch (Exception e) {
            logger.debug("Failed to set the transaction timeout, using the default value");
        }
    }

    /** 引导后重置事务超时为默认值 */
    private void resetTransactionTimeout(DefaultKeycloakSessionFactory keycloakSessionFactory) {
        try {
            KeycloakModelUtils.setTransactionLimit(keycloakSessionFactory, 0);
        } catch (Exception e) {
            logger.debug("Failed to reset the transaction timeout");
        }
    }

}
