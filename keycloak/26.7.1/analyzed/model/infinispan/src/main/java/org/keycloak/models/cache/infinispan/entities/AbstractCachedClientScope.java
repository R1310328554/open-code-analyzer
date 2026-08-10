package org.keycloak.models.cache.infinispan.entities;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.cache.infinispan.DefaultLazyLoader;
import org.keycloak.models.cache.infinispan.LazyLoader;

/**
 * Client/ClientScope 缓存条目中协议映射器（Protocol Mapper）查询的抽象基类。
 * <p>
 * 通过 {@link LazyLoader} 按 ID、名称与类型三类索引懒加载映射器，
 * 供 {@link CachedClientScope} 与 {@link CachedClient} 复用，避免缓存条目过大。
 */
abstract class AbstractCachedClientScope<D extends ClientScopeModel> extends AbstractRevisioned implements InRealm {

    /** 按映射器 ID 索引的懒加载器。 */
    private final LazyLoader<D, Map<String, ProtocolMapperModel>> mappersById;
    /** 按 protocol.name 复合键索引的懒加载器。 */
    private final LazyLoader<D, Map<String, String>> mappersByName;
    /** 按映射器类型分组索引的懒加载器。 */
    private final LazyLoader<D, Map<String, List<String>>> mappersByType;

    /** 从 ClientScopeModel 初始化三类协议映射器懒加载索引。 */
    public AbstractCachedClientScope(long revision, ClientScopeModel model) {
        super(revision, model.getId());
        mappersById = new DefaultLazyLoader<>(scope -> scope.getProtocolMappersStream()
                .collect(Collectors.toMap(ProtocolMapperModel::getId, ProtocolMapperModel::new)),
                Collections::emptyMap);
        mappersByName = new DefaultLazyLoader<>(scope -> scope.getProtocolMappersStream()
                .collect(Collectors.toMap(mapper -> mapper.getProtocol() + "." + mapper.getName(),
                        ProtocolMapperModel::getId)),
                Collections::emptyMap);
        mappersByType = new DefaultLazyLoader<>(scope ->
                scope.getProtocolMappersStream()
                        .collect(Collectors.groupingBy(ProtocolMapperModel::getProtocolMapper,
                                Collectors.mapping(ProtocolMapperModel::getId, Collectors.toList()))),
                Collections::emptyMap);
    }

    /** 返回所有协议映射器的流。 */
    public Stream<ProtocolMapperModel> getProtocolMappers(KeycloakSession session, Supplier<D> model) {
        return mappersById.get(session, model).values().stream();
    }

    /** 按 ID 查找协议映射器；id 为 null 时返回 null。 */
    public ProtocolMapperModel getProtocolMapperById(KeycloakSession session, Supplier<D> model, String id) {
        if (id == null) {
            return null;
        }
        return mappersById.get(session, model).get(id);
    }

    /** 按映射器类型查找协议映射器列表。 */
    public List<ProtocolMapperModel> getProtocolMapperByType(KeycloakSession session, Supplier<D> model, String type) {
        return mappersByType.get(session, model).getOrDefault(type, List.of()).stream()
                .map(id -> getProtocolMapperById(session, model, id))
                .collect(Collectors.toList());
    }

    /** 按协议与名称查找协议映射器。 */
    public ProtocolMapperModel getProtocolMapperByName(KeycloakSession session, Supplier<D> model, String protocol, String name) {
        String id = mappersByName.get(session, model).get(protocol + "." + name);
        return getProtocolMapperById(session, model, id);
    }
}
