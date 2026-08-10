package org.keycloak.storage;

import org.keycloak.cluster.ClusterProvider;
import org.keycloak.cluster.ExecutionResult;
import org.keycloak.common.util.Time;
import org.keycloak.common.util.TriFunction;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ModelIllegalStateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.storage.UserStorageProviderModel.SyncMode;
import org.keycloak.storage.user.ImportSynchronization;
import org.keycloak.storage.user.SynchronizationResult;
import org.keycloak.timer.ScheduledTask;
import org.keycloak.timer.TimerProvider;
import org.keycloak.timer.TimerProvider.TimerTaskContext;

import org.jboss.logging.Logger;

/**
 * 用户存储同步定时任务：按配置周期执行全量或增量 LDAP/联邦用户导入同步。
 * <p>
 * 在支持主集群的部署中仅由主集群节点执行，避免并发锁冲突；通过集群锁防止重复同步。
 */
final class UserStorageSyncTask implements ScheduledTask {

    private static final Logger logger = Logger.getLogger(UserStorageSyncTask.class);
    /** 同步任务最小执行超时（秒）。 */
    private static final int TASK_EXECUTION_TIMEOUT = 30;

    private final String providerId;
    private final String realmId;
    private final SyncMode syncMode;
    private final int period;

    /** 根据 Provider 配置与同步模式构造定时任务。 */
    UserStorageSyncTask(UserStorageProviderModel provider, SyncMode syncMode) {
        this.providerId = provider.getId();
        this.realmId = provider.getParentId();
        this.syncMode = syncMode;
        this.period = SyncMode.FULL.equals(syncMode) ? provider.getFullSyncPeriod() : provider.getChangedSyncPeriod();
    }

    @Override
    public void run(KeycloakSession session) {
        ClusterProvider clusterProvider = session.getProvider(ClusterProvider.class);
        if (clusterProvider.isPrimaryClusterSupported() && !clusterProvider.isPrimaryCluster()) {
            // 确保 LDAP 同步仅在其中一个集群节点运行，避免冲突与锁争用
            return;
        }

        RealmModel realm = session.realms().getRealm(realmId);

        session.getContext().setRealm(realm);

        UserStorageProviderModel provider = getStorageModel(session);

        if (isSyncPeriod(provider)) {
            runWithResult(session);
            return;
        }

        logger.debugf("Ignored LDAP %s users-sync with storage provider %s due small time since last sync in realm %s", //
                syncMode, provider.getName(), realmId);
    }

    @Override
    public String getTaskName() {
        return UserStorageSyncTask.class.getSimpleName() + "-" + providerId + "-" + syncMode;
    }

    /** 执行同步并返回结果；异常时记录错误并返回空结果。 */
    SynchronizationResult runWithResult(KeycloakSession session) {
        try {
            return switch (syncMode) {
                case FULL -> runFullSync(session);
                case CHANGED -> runIncrementalSync(session);
            };
        } catch (Throwable t) {
            logger.errorf(t, "Error occurred during %s users-sync in realm %s and user provider %s",  syncMode, realmId, providerId);
        }

        return SynchronizationResult.empty();
    }

    /** 若 Provider 可调度则注册定时任务，否则返回 false。 */
    boolean schedule(KeycloakSession session) {
        UserStorageProviderModel provider = getStorageModel(session);

        if (isSchedulable(provider)) {
            TimerProvider timer = session.getProvider(TimerProvider.class);

            if (timer == null) {
                logger.debugf("Timer provider not available. Not scheduling periodic sync task for provider '%s' in realm '%s'", provider.getName(), realmId);
                return false;
            }

            logger.debugf("Scheduling user periodic sync task '%s' for user storage provider '%s' in realm '%s' with period %d seconds", getTaskName(), provider.getName(), realmId, period);
            timer.scheduleTask(this, period * 1000L);

            return true;
        }

        logger.debugf("Not scheduling periodic sync settings for provider '%s' in realm '%s'", provider.getName(), realmId);

        return false;
    }

    /** 取消已注册的同名定时同步任务。 */
    void cancel(KeycloakSession session) {
        TimerProvider timer = session.getProvider(TimerProvider.class);

        if (timer == null) {
            logger.debugf("Timer provider not available. Not cancelling periodic sync task for provider id '%s' in realm '%s'", providerId, realmId);
            return;
        }

        logger.debugf("Cancelling any running user periodic sync task '%s' for user storage provider provider '%s' in realm '%s'", getTaskName(), providerId, realmId);

        TimerTaskContext existingTask = timer.cancelTask(getTaskName());

        if (existingTask != null) {
            logger.debugf("Cancelled periodic sync task with task-name '%s' for provider with id '%s'",
                    getTaskName(), providerId);
        }
    }

    private UserStorageProviderModel getStorageModel(KeycloakSession session) {
        RealmModel realm = session.getContext().getRealm();

        if (realm == null) {
            throw new ModelIllegalStateException("Realm with id " + realmId + " not found");
        }

        ComponentModel component = realm.getComponent(providerId);

        if (component == null) {
            cancel(session);
            throw new ModelIllegalStateException("User storage provider with id " + providerId + " not found in realm " + realm.getName());
        }

        return new UserStorageProviderModel(component);
    }

    private SynchronizationResult runFullSync(KeycloakSession session) {
        return runSync(session,
                (sf, storage, model) -> storage.sync(sf, realmId, model));
    }

    private SynchronizationResult runIncrementalSync(KeycloakSession session) {
        return runSync(session, (sf, storage, model) -> {
            // 查看上次同步时间
            int oldLastSync = model.getLastSync();
            return storage.syncSince(Time.toDate(oldLastSync), sf, realmId, model);
        });
    }

    private SynchronizationResult runSync(KeycloakSession session, TriFunction<KeycloakSessionFactory, ImportSynchronization, UserStorageProviderModel, SynchronizationResult> syncFunction) {
        UserStorageProviderModel provider = getStorageModel(session);
        KeycloakSessionFactory sessionFactory = session.getKeycloakSessionFactory();
        ImportSynchronization factory = getProviderFactory(session, provider);

        if (factory == null) {
            return SynchronizationResult.ignored();
        }

        ClusterProvider clusterProvider = session.getProvider(ClusterProvider.class);
        // 全量与增量同步共用锁键；如有需要可进一步拆分
        String taskKey = provider.getId() + "::sync";
        // 当前最小超时 30 秒
        int timeout = Math.max(TASK_EXECUTION_TIMEOUT, period);

        ExecutionResult<SynchronizationResult> task = clusterProvider.executeIfNotExecuted(taskKey, timeout, () -> {
            // 需在本事务中重新加载组件以获取最新数据
            SynchronizationResult result = syncFunction.apply(sessionFactory, factory, provider);

            if (!result.isIgnored()) {
                KeycloakModelUtils.runJobInTransaction(sessionFactory, s -> {
                    RealmModel realm = s.realms().getRealm(realmId);
                    s.getContext().setRealm(realm);
                    updateLastSyncInterval(s);
                });
            }

            return result;
        });

        SynchronizationResult result = task.getResult();

        if (result == null || !task.isExecuted()) {
            logger.debugf("syncing users for federation provider %s was ignored as it's already in progress", provider.getName());
            return SynchronizationResult.ignored();
        }

        return result;
    }

    private ImportSynchronization getProviderFactory(KeycloakSession session, UserStorageProviderModel provider) {
        KeycloakSessionFactory sessionFactory = session.getKeycloakSessionFactory();
        UserStorageProviderFactory<?> factory = (UserStorageProviderFactory<?>) sessionFactory.getProviderFactory(UserStorageProvider.class, provider.getProviderId());

        if (factory instanceof ImportSynchronization f) {
            return f;
        }

        return null;
    }

    // 更新给定 UserFederationProviderModel 的上次同步时间戳，在独立事务中执行
    private void updateLastSyncInterval(KeycloakSession session) {
        UserStorageProviderModel provider = getStorageModel(session);

        // 更新数据库中的持久化 Provider 配置
        provider.setLastSync(Time.currentTime(), syncMode);

        RealmModel realm = session.getContext().getRealm();

        realm.updateComponent(provider);
    }

    // 若距上次同步时间过短则跳过本次同步
    private boolean isSyncPeriod(UserStorageProviderModel provider) {
        int lastSyncTime = provider.getLastSync(syncMode);

        if (lastSyncTime <= 0) {
            return true;
        }

        int currentTime = Time.currentTime();
        int timeSinceLastSync = currentTime - lastSyncTime;

        return timeSinceLastSync >= (period - 1);
    }

    private boolean isSchedulable(UserStorageProviderModel provider) {
        return provider.isImportEnabled() && provider.isEnabled() && period > 0;
    }
}
