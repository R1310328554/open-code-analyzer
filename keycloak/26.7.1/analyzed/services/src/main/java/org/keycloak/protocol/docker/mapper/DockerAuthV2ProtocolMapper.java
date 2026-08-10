package org.keycloak.protocol.docker.mapper;

import java.util.Collections;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.docker.DockerAuthV2Protocol;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * Docker Auth v2 协议映射器抽象基类：绑定 {@link DockerAuthV2Protocol} 登录协议。
 * <p>提供统一的显示分类与空配置；子类通过 SPI 注册具体映射逻辑。</p>
 */
public abstract class DockerAuthV2ProtocolMapper implements ProtocolMapper {

    /** 管理控制台中 Docker 映射器的显示分类名称。 */
    public static final String DOCKER_AUTH_V2_CATEGORY = "Docker Auth Mapper";

    @Override
    /** @return Docker Auth v2 协议 ID */
    public String getProtocol() {
        return DockerAuthV2Protocol.LOGIN_PROTOCOL;
    }

    @Override
    /** @return 映射器在控制台中的分类 */
    public String getDisplayCategory() {
        return DOCKER_AUTH_V2_CATEGORY;
    }

    @Override
    /** Docker v2 映射器默认无额外配置项。 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    public void close() {
        // 无操作
    }

    @Override
    /** 映射器为单例式 SPI，不支持按会话创建实例。 */
    public final ProtocolMapper create(final KeycloakSession session) {
        throw new UnsupportedOperationException("The create method is not supported by this mapper");
    }

    @Override
    public void init(final Config.Scope config) {
        // no-op
    }

    @Override
    public void postInit(final KeycloakSessionFactory factory) {
        // no-op
    }
}
