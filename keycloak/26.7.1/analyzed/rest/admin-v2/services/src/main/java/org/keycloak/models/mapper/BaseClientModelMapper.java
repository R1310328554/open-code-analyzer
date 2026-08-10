package org.keycloak.models.mapper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.keycloak.models.ClientModel;
import org.keycloak.models.RoleModel;
import org.keycloak.representations.admin.v2.BaseClientRepresentation;

/**
 * 客户端表示与 {@link ClientModel} 之间字段映射的抽象基类。
 * <p>
 * 通过 {@link MappedField} 注册双向 getter/setter，支持按 {@code includeFields} 投影部分字段。
 */
public abstract class BaseClientModelMapper<T extends BaseClientRepresentation> implements ClientModelMapper {
    
    /** 单个字段在表示与模型间的双向映射描述。 */
    public static class MappedField<T> {
        
        Function<T, Object> repGetter;
        BiConsumer<T, Object> repSetter;
        Function<ClientModel, Object> modelGetter;
        BiConsumer<ClientModel, Object> modelSetter;
        
        /** 从持久化模型复制字段值到表示对象。 */
        void fromModel(ClientModel model, T rep) {
            if (repSetter != null && modelGetter != null) {
                repSetter.accept(rep, modelGetter.apply(model));
            }
        }
        
        /** 从表示对象写回持久化模型（仅当 setter 存在时）。 */
        void toModel(T rep, ClientModel model) {
            if (hasGetter() && modelSetter != null) {
                // TODO: exception handling to make things clearer when things fail
                modelSetter.accept(model, getValue(rep));
            }
        }
        
        public boolean hasGetter() {
            return repGetter != null;
        }
        
        public <V> V getValue(T rep) {
            if (repGetter != null) {
                return (V) repGetter.apply(rep);
            }
            return null;
        }
    }
 
    final Map<String, MappedField<BaseClientRepresentation>> fields = new LinkedHashMap<String, MappedField<BaseClientRepresentation>>();

    /** 返回已注册映射字段名集合（不可变）。 */
    public Set<String> getFieldNames() {
        return Collections.unmodifiableSet(fields.keySet());
    }

    protected <F> void addMapping(String name, Function<T, F> repGetter, BiConsumer<T, F> repSetter, Function<ClientModel, F> modelGetter, BiConsumer<ClientModel, F> modelSetter) {
        MappedField prop = new MappedField<>();
        prop.repGetter = repGetter;
        prop.repSetter = repSetter;
        prop.modelGetter = modelGetter;
        prop.modelSetter = modelSetter;
        this.fields.put(name, prop);
    }
        
    public BaseClientModelMapper() {
        this.addMapping("protocol", BaseClientRepresentation::getProtocol, BaseClientRepresentation::setProtocol, ClientModel::getProtocol, ClientModel::setProtocol);
        this.addMapping("uuid", BaseClientRepresentation::getUuid, BaseClientRepresentation::setUuid, ClientModel::getId, null);
        this.addMapping("enabled", BaseClientRepresentation::getEnabled, BaseClientRepresentation::setEnabled, ClientModel::isEnabled, (model, enabled) -> model.setEnabled(Boolean.TRUE.equals(enabled)));
        this.addMapping("clientId", BaseClientRepresentation::getClientId, BaseClientRepresentation::setClientId, ClientModel::getClientId, ClientModel::setClientId);
        this.addMapping("description", BaseClientRepresentation::getDescription, BaseClientRepresentation::setDescription, ClientModel::getDescription, ClientModel::setDescription);
        this.addMapping("displayName", BaseClientRepresentation::getDisplayName, BaseClientRepresentation::setDisplayName, ClientModel::getName, ClientModel::setName);
        this.addMapping("appUrl", BaseClientRepresentation::getAppUrl, BaseClientRepresentation::setAppUrl, ClientModel::getBaseUrl, ClientModel::setBaseUrl);
        // TODO: consider built-in logic for copying collections
        this.addMapping("redirectUris", BaseClientRepresentation::getRedirectUris, BaseClientRepresentation::setRedirectUris, model -> new LinkedHashSet<>(model.getRedirectUris()), (model, uris) -> model.setRedirectUris(new LinkedHashSet<>(uris)));
        this.addMapping("roles", BaseClientRepresentation::getRoles, BaseClientRepresentation::setRoles, model -> model.getRolesStream().map(RoleModel::getName).collect(Collectors.toSet()), null);
        this.addMapping("createdTimestamp", BaseClientRepresentation::getCreatedTimestamp, BaseClientRepresentation::setCreatedTimestamp, ClientModel::getCreatedTimestamp, null);
        this.addMapping("updatedTimestamp", BaseClientRepresentation::getUpdatedTimestamp, BaseClientRepresentation::setUpdatedTimestamp, ClientModel::getLastModifiedTimestamp, null);
    }
    
    @Override
    /** 将 {@link ClientModel} 转为表示；{@code includeFields} 非空时仅填充指定字段。 */
    public BaseClientRepresentation fromModel(ClientModel model, Set<String> includeFields) {
        // We don't want reps to depend on any unnecessary fields deps, hence no generated builder.

        T rep = createClientRepresentation();
        
        var stream = fields.entrySet().stream();
        if (includeFields != null && !includeFields.isEmpty()) {
            stream = stream.filter(e -> includeFields.contains(e.getKey()));
        }
        stream.forEach(e -> e.getValue().fromModel(model, rep));

        return rep;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void toModel(BaseClientRepresentation rep, ClientModel existingModel) {
        fields.values().forEach(m -> m.toModel(rep, existingModel));
    }

    @SuppressWarnings("unchecked")
    /** 将未包含在投影中的可写字段置为 null。 */
    public void applyProjection(BaseClientRepresentation rep, Set<String> includeFields) {
        if (includeFields == null || includeFields.isEmpty()) return;
        fields.entrySet().stream()
                .filter(e -> !includeFields.contains(e.getKey()))
                .filter(e -> e.getValue().repSetter != null)
                .forEach(e -> e.getValue().repSetter.accept(rep, null));
    }

    protected abstract T createClientRepresentation();

}
