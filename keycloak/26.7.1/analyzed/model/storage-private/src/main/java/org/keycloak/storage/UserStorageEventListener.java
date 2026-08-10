package org.keycloak.storage;

import java.util.stream.Stream;

import org.keycloak.cluster.ClusterEvent;
import org.keycloak.cluster.ClusterListener;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.StorageProviderRealmModel;
import org.keycloak.models.utils.PostMigrationEvent;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.provider.ProviderEventListener;
import org.keycloak.storage.UserStorageProviderModel.SyncMode;
import org.keycloak.storage.user.ImportSynchronization;

import org.jboss.logging.Logger;

import static org.keycloak.models.utils.KeycloakModelUtils.runJobInTransaction;

/**
 * 用户存储事件监听器：响应集群事件与 Provider 事件，维护用户联邦存储的周期性同步定时任务。
 * <p>
 * 监听 {@link PostMigrationEvent} 完成迁移后重新调度同步任务，并注册集群监听器；
 * 监听 {@link StoreSyncEvent} 在存储配置变更时刷新或取消定时任务并通知集群各节点。
 */
public final class UserStorageEventListener implements ClusterListener, ProviderEventListener {

    private static final Logger logger = Logger.getLogger(UserStorageEventListener.class);
    /** 集群通知通道键，用于用户存储同步任务协调。 */
    private static final String USER_STORAGE_TASK_KEY = "user-storage";

    private final KeycloakSessionFactory sessionFactory;

    /** 构造监听器并绑定 {@link KeycloakSessionFactory}。 */
    public UserStorageEventListener(KeycloakSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /** 处理来自集群的用户存储 Provider 变更事件。 */
    @Override
    public void eventReceived(ClusterEvent event) {
        UserStorageProviderClusterEvent fedEvent = (UserStorageProviderClusterEvent) event;
        String realmId = fedEvent.getRealmId();

        runJobInTransaction(sessionFactory, session -> {
            RealmModel realm = session.realms().getRealm(realmId);

            if (realm == null) {
                if (fedEvent.isRemoved()) {
                    logger.debugf("Realm with id %s not found when handling user storage removal event, it may have been deleted already", realmId);
                    return;
                }
                throw new RuntimeException("Failed to execute session task. Realm with id " + realmId + " not found.");
            }

            session.getContext().setRealm(realm);
            refreshScheduledTasks(session, fedEvent.getStorageProvider(), fedEvent.isRemoved());
        });
    }

    /** 处理迁移完成与存储同步配置变更等 Provider 事件。 */
    @Override
    public void onEvent(ProviderEvent event) {
        if (event instanceof PostMigrationEvent) {
            runJobInTransaction(sessionFactory, session -> {
                session.realms().getRealmsWithProviderTypeStream(UserStorageProvider.class)
                        .forEach(realm -> {
                            try {
                                session.getContext().setRealm(realm);
                                getUserStorageProvidersStream(realm).forEachOrdered(provider -> reScheduleTasks(session, provider));
                            } finally {
                                session.getContext().setRealm(null);
                            }
                });

                ClusterProvider clusterProvider = session.getProvider(ClusterProvider.class);

                if (clusterProvider != null) {
                    clusterProvider.registerListener(USER_STORAGE_TASK_KEY, this);
                }
            });
        } else if (event instanceof StoreSyncEvent ev) {
            UserStorageProviderModel model = ev.getModel() == null ? null: new UserStorageProviderModel(ev.getModel());
            boolean removed = ev.getRemoved();
            String realmId = ev.getRealm().getId();

            runJobInTransaction(sessionFactory, session -> {
                RealmModel realm = session.realms().getRealm(realmId);
                if (realm == null) {
                    return;
                }
                session.getContext().setRealm(realm);

                if (model != null) {
                    refreshScheduledTasks(session, model, removed);
                    notifyStoreSyncClusterUpdate(session, realm, model, removed);
                } else {
                    getUserStorageProvidersStream(realm).forEachOrdered(fedProvider -> {
                        refreshScheduledTasks(session, fedProvider, removed);
                        notifyStoreSyncClusterUpdate(session, realm, fedProvider, removed);
                    });
                }
            });
        }
    }

    /** 为支持 {@link ImportSynchronization} 的 Provider 重新调度全量与增量同步任务。 */
    private void reScheduleTasks(KeycloakSession session, UserStorageProviderModel provider) {
        KeycloakSessionFactory sessionFactory = session.getKeycloakSessionFactory();
        UserStorageProviderFactory<?> factory = (UserStorageProviderFactory<?>) sessionFactory.getProviderFactory(UserStorageProvider.class, provider.getProviderId());
        RealmModel realm = session.getContext().getRealm();

        if (!(factory instanceof ImportSynchronization)) {
            logger.debugf("Not refreshing periodic sync settings for provider '%s' in realm '%s'", provider.getName(), realm.getName());
            return;
        }

        logger.debugf("Going to refresh periodic sync settings for provider '%s' in realm '%s' with realmId '%s'. Full sync period: %d , changed users sync period: %d",
                provider.getName(), realm.getName(), realm.getId(), provider.getFullSyncPeriod(), provider.getChangedSyncPeriod());
        scheduleTask(session, provider, SyncMode.FULL);
        scheduleTask(session, provider, SyncMode.CHANGED);
    }

    /** 调度指定同步模式的定时任务；若调度失败则取消可能残留的旧任务。 */
    private void scheduleTask(KeycloakSession session, UserStorageProviderModel provider, SyncMode mode) {
        UserStorageSyncTask task = new UserStorageSyncTask(provider, mode);

        if (!task.schedule(session)) {
            // 取消可能残留的悬空任务
            task.cancel(session);
        }
    }

    // 确保集群各节点均收到通知
    private void notifyStoreSyncClusterUpdate(KeycloakSession session, RealmModel realm, UserStorageProviderModel provider, boolean removed) {
        KeycloakSessionFactory sessionFactory = session.getKeycloakSessionFactory();
        UserStorageProviderFactory<?> factory = (UserStorageProviderFactory<?>) sessionFactory.getProviderFactory(UserStorageProvider.class, provider.getProviderId());

        if (!(factory instanceof ImportSynchronization)) {
            return;
        }

        ClusterProvider cp = session.getProvider(ClusterProvider.class);

        if (cp != null) {
            UserStorageProviderClusterEvent event = UserStorageProviderClusterEvent.createEvent(removed, realm.getId(), provider);
            cp.notify(USER_STORAGE_TASK_KEY, event, true);
        }
    }

    /** 根据是否移除 Provider 刷新或取消已调度的同步任务。 */
    private void refreshScheduledTasks(KeycloakSession session, UserStorageProviderModel model, boolean removed) {
        if (removed) {
            new UserStorageSyncTask(model, SyncMode.FULL).cancel(session);
            new UserStorageSyncTask(model, SyncMode.CHANGED).cancel(session);
        } else {
            reScheduleTasks(session, model);
        }
    }

    /** 返回领域下配置的用户存储 Provider 模型流。 */
    private Stream<UserStorageProviderModel> getUserStorageProvidersStream(RealmModel realm) {
        if (realm instanceof StorageProviderRealmModel s) {
            return s.getUserStorageProvidersStream();
        }

        return Stream.empty();
    }
}
