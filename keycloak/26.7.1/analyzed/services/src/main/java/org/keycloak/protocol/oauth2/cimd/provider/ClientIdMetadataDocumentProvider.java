package org.keycloak.protocol.oauth2.cimd.provider;

import org.keycloak.models.ClientModel;
import org.keycloak.protocol.oauth2.cimd.clientpolicy.executor.AbstractClientIdMetadataDocumentExecutor;
import org.keycloak.protocol.oauth2.cimd.clientpolicy.executor.AbstractClientIdMetadataDocumentExecutor.FetchOperation;
import org.keycloak.protocol.oauth2.cimd.clientpolicy.executor.AbstractClientIdMetadataDocumentExecutor.OIDCClientRepresentationWithCacheControl;
import org.keycloak.provider.Provider;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyException;

/**
 * Client ID 元数据文档（CIMD）Provider 接口。
 * <ul>
 *     <li>判断是否需要（重新）拉取客户端元数据</li>
 *     <li>创建与更新元数据缓存</li>
 *     <li>设置缓存过期时间</li>
 *     <li>在 {@code ClientRepresentation} 上增强元数据</li>
 * </ul>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public interface ClientIdMetadataDocumentProvider<CONFIG extends AbstractClientIdMetadataDocumentExecutor.Configuration> extends Provider {

    /**
     * 获取 CIMD 执行器配置。
     * @return {@code CONFIG extends AbstractClientIdMetadataDocumentExecutor.Configuration}
     */
    CONFIG getConfiguration();

    /**
     * 设置 CIMD 执行器配置。
     * @param configuration CIMD 执行器配置，非 {@code null}
     */
    void setConfiguration(CONFIG configuration);

    /**
     * 将缓存过期时间（秒）写入 {@code ClientRepresentation} 元数据。
     * @param clientRep 客户端表示，非 {@code null}
     * @param cacheExpiryTimeInSec 缓存过期 Unix 时间（秒）
     */
    void setCacheExpiryTimeToClientMetadata(ClientRepresentation clientRep, int cacheExpiryTimeInSec);

    /**
     * 将缓存过期时间（秒）写入 {@code ClientModel} 元数据。
     * @param clientModel 客户端模型，非 {@code null}
     * @param cacheExpiryTimeInSec 缓存过期 Unix 时间（秒）
     */
    void setCacheExpiryTimeToClientMetadata(ClientModel clientModel, int cacheExpiryTimeInSec);

    /**
     * 根据 {@code client_id} 与缓存状态决定拉取操作：新建、更新或无需更新。
     * @param clientId 授权请求中的 {@code client_id}，非 {@code null}
     * @return {@link FetchOperation}
     */
    FetchOperation determineFetchOperation(String clientId);

    /**
     * 根据拉取的 OIDC 客户端元数据及 Cache-Control 创建客户端。
     * @param clientOIDCWithCacheControl 元数据与缓存控制信息，非 {@code null}
     * @return 新建 {@link ClientModel}
     * @throws ClientPolicyException 创建失败时
     */
    ClientModel createClientMetadata(OIDCClientRepresentationWithCacheControl clientOIDCWithCacheControl) throws ClientPolicyException;

    /**
     * 根据重新拉取的元数据更新已有客户端。
     * @param clientOIDCWithCacheControl 元数据与缓存控制信息，非 {@code null}
     * @return 更新后的 {@link ClientModel}
     * @throws ClientPolicyException 更新失败时
     */
    ClientModel updateClientMetadata(OIDCClientRepresentationWithCacheControl clientOIDCWithCacheControl) throws ClientPolicyException;

    /**
     * 按 Provider 策略增强客户端元数据。
     * @param clientRep {@link ClientRepresentation}，非 {@code null}
     */
    void augmentClientMetadata(ClientRepresentation clientRep);

    @Override
    default void close() {
    }
}
