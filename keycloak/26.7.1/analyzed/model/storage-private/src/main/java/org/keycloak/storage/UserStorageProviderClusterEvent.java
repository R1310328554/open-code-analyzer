package org.keycloak.storage;

import java.util.Objects;

import org.keycloak.cluster.ClusterEvent;

import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 用户存储 Provider 集群事件：在联邦 Provider 配置变更或移除时通知集群各节点更新同步定时任务。
 * <p>
 * 每次更新或删除联邦 Provider 时向集群广播，使各节点刷新同步周期。
 */
@ProtoTypeId(65540)
public class UserStorageProviderClusterEvent implements ClusterEvent {

    private boolean removed;
    private String realmId;
    private UserStorageProviderModel storageProvider;

    /** 是否表示 Provider 已被移除。 */
    @ProtoField(1)
    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    /** 目标领域 ID。 */
    @ProtoField(2)
    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    /** 关联的用户存储 Provider 配置模型。 */
    @ProtoField(3)
    public UserStorageProviderModel getStorageProvider() {
        return storageProvider;
    }

    public void setStorageProvider(UserStorageProviderModel federationProvider) {
        this.storageProvider = federationProvider;
    }

    /** 创建集群事件实例。 */
    public static UserStorageProviderClusterEvent createEvent(boolean removed, String realmId, UserStorageProviderModel provider) {
        UserStorageProviderClusterEvent notification = new UserStorageProviderClusterEvent();
        notification.setRemoved(removed);
        notification.setRealmId(realmId);
        notification.setStorageProvider(provider);
        return notification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserStorageProviderClusterEvent that = (UserStorageProviderClusterEvent) o;
        return removed == that.removed && Objects.equals(realmId, that.realmId) && Objects.equals(storageProvider.getId(), that.storageProvider.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(removed, realmId, storageProvider.getId());
    }
}
