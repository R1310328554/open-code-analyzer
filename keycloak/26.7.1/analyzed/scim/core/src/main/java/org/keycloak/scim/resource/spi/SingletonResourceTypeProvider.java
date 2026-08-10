package org.keycloak.scim.resource.spi;

import java.util.stream.Stream;

import org.keycloak.models.ModelValidationException;
import org.keycloak.scim.protocol.request.SearchRequest;
import org.keycloak.scim.resource.ResourceTypeRepresentation;

/**
 * 单例 SCIM 资源类型的 {@link ScimResourceTypeProvider} 特化接口。
 *
 * <p>单例资源类型表示 SCIM 服务提供者中仅存在唯一实例的资源，
 * 例如 {@link org.keycloak.scim.resource.config.ServiceProviderConfig}。</p>
 *
 * <p>本接口不新增方法，仅作为标记接口表明实现者管理单例资源。
 * 实现类应确保检索操作返回唯一实例，并正确处理创建与删除语义。</p>
 */
public interface SingletonResourceTypeProvider<R extends ResourceTypeRepresentation> extends ScimResourceTypeProvider<R> {

    /** 返回唯一的单例资源实例。 */
    R getSingleton();

    @Override
    default R create(R resource) {
        throw unsupportedOperation();
    }

    @Override
    default R update(R user) {
        throw unsupportedOperation();
    }

    @Override
    default R get(String id) {
        throw unsupportedOperation();
    }

    @Override
    default Stream<R> getAll(SearchRequest searchRequest) {
        return Stream.of(getSingleton());
    }

    @Override
    default Long count(SearchRequest searchRequest) {
        return 1L;
    }

    @Override
    default boolean delete(String id) {
        throw unsupportedOperation();
    }

    /** 构造不支持操作的校验异常。 */
    private ModelValidationException unsupportedOperation() {
        return new ModelValidationException("Unsupported operation for resource type " + getResourceType());
    }

    @Override
    default void close() {
    }
}
