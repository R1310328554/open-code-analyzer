/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.jgroups.certificates;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.spi.infinispan.JGroupsCertificateProvider;

import org.infinispan.commons.api.Lifecycle;
import org.infinispan.factories.KnownComponentNames;
import org.infinispan.factories.annotations.ComponentName;
import org.infinispan.factories.annotations.Inject;
import org.infinispan.factories.annotations.Start;
import org.infinispan.factories.annotations.Stop;
import org.infinispan.factories.scopes.Scope;
import org.infinispan.factories.scopes.Scopes;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.CacheManagerNotifier;
import org.infinispan.notifications.cachemanagerlistener.annotation.Merged;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.remoting.transport.Address;
import org.infinispan.util.concurrent.BlockingManager;
import org.jboss.logging.Logger;

/**
 * JGroups 加密通信（mTLS）证书轮换与重载管理器。
 * <p>
 * 绑定 Infinispan 生命周期，与 {@link EmbeddedCacheManager} 同步启停。
 * <p>
 * 提供 {@link #rotateCertificate()} 立即轮换证书，以及 {@link #reloadCertificate()} 从存储重载并调度下次轮换。
 * <p>
 * 定时器到期时仅由集群协调者生成新证书，并通知其他成员从存储加载；密钥库与信任库同时保留新旧证书以实现平滑切换。
 * <p>
 * 监听拓扑变更：协调者故障后，新选出的协调者继续执行轮换职责。
 */
@Scope(Scopes.GLOBAL)
@Listener
public class CertificateReloadManager implements Lifecycle {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
    /** 失败后重试前的等待间隔。 */
    private static final Duration RETRY_WAIT_TIME = Duration.ofMinutes(1);
    /** JGroups 视图就绪前的高频启动重载周期。 */
    private static final Duration BOOT_PERIOD = Duration.ofMillis(500);

    /** 用于在事务中访问 JGroups 证书 SPI 的 Keycloak 会话工厂。 */
    private final KeycloakSessionFactory sessionFactory;
    /** 保护轮换/重载/调度逻辑的互斥锁（AutoCloseable 包装）。 */
    private final AutoCloseableLock lock;
    /** 协调者安排的下次证书轮换定时任务。 */
    private ScheduledFuture<?> scheduledFuture;
    /** JGroups 启动前的快速启动重载任务，收到视图后取消。 */
    private ScheduledFuture<?> bootFuture;

    @Inject EmbeddedCacheManager cacheManager;
    @Inject CacheManagerNotifier notifier;
    @Inject BlockingManager blockingManager;
    @ComponentName(KnownComponentNames.EXPIRATION_SCHEDULED_EXECUTOR)
    @Inject ScheduledExecutorService scheduledExecutorService;

    /**
     * @param sessionFactory Keycloak 会话工厂
     */
    public CertificateReloadManager(KeycloakSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        lock = new AutoCloseableLock(new ReentrantLock());
    }

    /** {@inheritDoc} 注册拓扑监听器并启动启动期快速重载与首次轮换调度。 */
    @Override
    @Start
    public void start() {
        logger.info("Starting JGroups certificate reload manager");
        notifier.addListener(this);
        scheduleNextRotation();

        lock.lock();
        try(lock) {
            // 在 JGroups 启动前以短周期重载证书；收到 JGroups 视图后取消，避免节点在轮换过程中加入导致状态不一致
            bootFuture = scheduledExecutorService.scheduleAtFixedRate(() -> blockingManager.runBlocking(this::bootReload, "boot-reload"), BOOT_PERIOD.toMillis(), BOOT_PERIOD.toMillis(), TimeUnit.MILLISECONDS);
        }

    }

    /** {@inheritDoc} 移除监听器并取消已安排的轮换任务。 */
    @Override
    @Stop
    public void stop() {
        logger.info("Stopping JGroups certificate reload manager");
        notifier.removeListener(this);
        lock.lock();
        try (lock) {
            if (scheduledFuture == null) {
                return;
            }
            scheduledFuture.cancel(true);
        }
    }

    /**
     * 生成新证书并重载到 JGroups 密钥库，随后广播重载通知。
     */
    public void rotateCertificate() {
        logger.info("Rotating JGroups certificate");
        lock.lock();
        try (lock) {
            KeycloakModelUtils.runJobInTransaction(sessionFactory, CertificateReloadManager::replaceCertificateInTransaction);
            sendReloadNotification();
        } catch (RuntimeException e) {
            logger.warn("Failed to rotate JGroups certificate", e);
            retry(this::rotateCertificate, "retry-rotate");
        }
    }

    /**
     * 从持久化存储重载证书，并重新调度下次轮换。
     */
    public void reloadCertificate() {
        logger.info("Reloading JGroups Certificate");
        lock.lock();
        try (lock) {
            if (bootFuture != null) {
                bootFuture.cancel(true);
                bootFuture = null;
            }
            KeycloakModelUtils.runJobInTransaction(sessionFactory, CertificateReloadManager::loadCertificateInTransaction);
        } catch (RuntimeException e) {
            logger.warn("Failed to reload JGroups certificate", e);
            retry(this::reloadCertificate, "retry-reload");
        } finally {
            scheduleNextRotation();
        }
    }

    /** 集群视图合并或变更时重载最新存储中的证书（应对分区后的证书同步）。 */
    @ViewChanged
    @Merged
    public void onViewChanged(ViewChangedEvent event) {
        logger.debug("On view changed");
        // 分区场景下重载存储中最新的证书
        reloadCertificate();
    }

    /** 测试用：当前节点是否为集群协调者。 */
    public boolean isCoordinator() {
        return cacheManager.isCoordinator();
    }

    /** 测试用：是否已安排轮换定时任务。 */
    public boolean hasRotationTask() {
        lock.lock();
        try (lock) {
            return scheduledFuture != null;
        }
    }

    /** 启动阶段短周期重载，忽略单次失败。 */
    private void bootReload() {
        logger.debug("[Boot] reloading certificate.");
        lock.lock();
        try (lock) {
            KeycloakModelUtils.runJobInTransaction(sessionFactory, CertificateReloadManager::loadCertificateInTransaction);
        } catch (RuntimeException e) {
            logger.warn("Exception on boot reload cycle. Ignoring it.", e);
        }
    }

    /** 证书无效时的回调：异步触发完整重载流程。 */
    private void onInvalidCertificate() {
        logger.info("On certificate exception");
        blockingManager.runBlocking(this::reloadCertificate, "invalid-certificate");
    }

    /** 处理集群节点对重载通知的响应；失败时向该节点重试通知。 */
    private void onCertificateReloadResponse(Address address, Void unused, Throwable throwable) {
        if (throwable != null) {
            logger.warnf(throwable, "Node %s failed to handle JGroups certificate reload notification.", address);
            retry(() -> sendReloadNotification(address), "retry-notification");
        }
    }

    /** 协调者根据 SPI 返回的间隔安排下次轮换；间隔为零则立即轮换。 */
    private void scheduleNextRotation() {
        lock.lock();
        try (lock) {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(false);
            }
            if (!isCoordinator()) {
                return;
            }

            var delay = KeycloakModelUtils.runJobInTransactionWithResult(sessionFactory, CertificateReloadManager::nextRotationDelay);
            logger.debugf("Next rotation in %s", delay);
            if (delay.isZero()) {
                blockingManager.runBlocking(this::rotateCertificate, "rotate");
                return;
            }
            scheduledFuture = scheduledExecutorService.schedule(() -> blockingManager.runBlocking(this::rotateCertificate, "rotate"), delay.toSeconds(), TimeUnit.SECONDS);
        }
    }

    /** 在事务中调用 SPI 生成并替换证书。 */
    private static void replaceCertificateInTransaction(KeycloakSession session) {
        session.getProvider(JGroupsCertificateProvider.class).rotateCertificate();
    }

    /** 在事务中从存储加载证书到 JGroups 运行时。 */
    private static void loadCertificateInTransaction(KeycloakSession session) {
        session.getProvider(JGroupsCertificateProvider.class).reloadCertificate();
    }

    /** 在事务中查询距离下次轮换的等待时间。 */
    private static Duration nextRotationDelay(KeycloakSession session) {
        return session.getProvider(JGroupsCertificateProvider.class).nextRotation();
    }

    /** 向所有集群节点广播证书重载通知。 */
    private void sendReloadNotification() {
        cacheManager.executor()
                .allNodeSubmission()
                .submitConsumer(ReloadCertificateFunction.getInstance(), this::onCertificateReloadResponse);
    }

    /** 向指定节点发送证书重载通知。 */
    private void sendReloadNotification(Address destination) {
        cacheManager.executor()
                .filterTargets(destination::equals)
                .submitConsumer(ReloadCertificateFunction.getInstance(), this::onCertificateReloadResponse);
    }

    /** 在 RETRY_WAIT_TIME 后通过 BlockingManager 重试指定任务。 */
    private void retry(Runnable runnable, String traceId) {
        scheduledExecutorService.schedule(() -> blockingManager.runBlocking(runnable, traceId), RETRY_WAIT_TIME.toSeconds(), TimeUnit.SECONDS);
    }

    /** 将 {@link ReentrantLock} 包装为 try-with-resources 可用的 AutoCloseable 锁。 */
    private record AutoCloseableLock(ReentrantLock innerLock) implements AutoCloseable {

        public void lock() {
            innerLock.lock();
        }

        @Override
        public void close() {
            innerLock.unlock();
        }
    }

}
