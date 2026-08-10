package org.keycloak.models.sessions.infinispan.changes;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.sessions.infinispan.entities.SessionEntity;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 基于版本 UUID 的 Infinispan 条件替换函数。
 * <p>
 * 仅当缓存中当前 {@link SessionEntityWrapper} 的版本与期望值一致时才写入新值，避免覆盖并发修改。
 *
 * @param <K> Infinispan 键类型
 * @param <T> Keycloak 会话实体类型
 */
@ProtoTypeId(Marshalling.REPLACE_FUNCTION)
public class ReplaceFunction<K, T extends SessionEntity> implements BiFunction<K, SessionEntityWrapper<T>, SessionEntityWrapper<T>> {

    private final UUID expectedVersion;
    private final SessionEntityWrapper<T> newValue;

    @ProtoFactory
    public ReplaceFunction(UUID expectedVersion, SessionEntityWrapper<T> newValue) {
        this.expectedVersion = Objects.requireNonNull(expectedVersion);
        this.newValue = Objects.requireNonNull(newValue);
    }

    @Override
    public SessionEntityWrapper<T> apply(K key, SessionEntityWrapper<T> currentValue) {
        assert currentValue != null;
        // 版本匹配则替换，否则保留原值以实现乐观并发控制
        return expectedVersion.equals(currentValue.getVersion()) ? newValue : currentValue;
    }

    @ProtoField(1)
    UUID getExpectedVersion() {
        return expectedVersion;
    }

    @ProtoField(2)
    SessionEntityWrapper<T> getNewValue() {
        return newValue;
    }
}
