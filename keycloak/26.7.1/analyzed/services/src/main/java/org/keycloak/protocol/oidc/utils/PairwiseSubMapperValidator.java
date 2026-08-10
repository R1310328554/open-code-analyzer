package org.keycloak.protocol.oidc.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.protocol.ProtocolMapperConfigException;
import org.keycloak.protocol.oidc.mappers.PairwiseSubMapperHelper;
import org.keycloak.util.JsonSerialization;

/**
 * Pairwise Subject 映射器配置校验器：验证 sector identifier 与客户端 redirect URI 一致性。
 * @author <a href="mailto:martin.hardselius@gmail.com">Martin Hardselius</a>
 */
public class PairwiseSubMapperValidator {

    /** 错误键：客户端 redirect URI 格式非法 */
    public static final String PAIRWISE_MALFORMED_CLIENT_REDIRECT_URI = "pairwiseMalformedClientRedirectURI";
    /** 错误键：redirect URI 缺少 host */
    public static final String PAIRWISE_CLIENT_REDIRECT_URIS_MISSING_HOST = "pairwiseClientRedirectURIsMissingHost";
    /** 错误键：无 sector URI 时 redirect URI 含多个 host */
    public static final String PAIRWISE_CLIENT_REDIRECT_URIS_MULTIPLE_HOSTS = "pairwiseClientRedirectURIsMultipleHosts";
    /** 错误键：sector identifier URI 格式非法 */
    public static final String PAIRWISE_MALFORMED_SECTOR_IDENTIFIER_URI = "pairwiseMalformedSectorIdentifierURI";
    /** 错误键：无法从 sector URI 获取 redirect 列表 */
    public static final String PAIRWISE_FAILED_TO_GET_REDIRECT_URIS = "pairwiseFailedToGetRedirectURIs";
    /** 错误键：客户端 redirect 与 sector URI 列表不匹配 */
    public static final String PAIRWISE_REDIRECT_URIS_MISMATCH = "pairwiseRedirectURIsMismatch";

    /**
     * 校验客户端上 Pairwise 映射器配置（从 mapper 读取 sector identifier URI）。
     * @param session Keycloak 会话
     * @param client 客户端模型
     * @param mapperModel 协议映射器模型
     */
        String sectorIdentifierUri = PairwiseSubMapperHelper.getSectorIdentifierUri(mapperModel);
        String rootUrl = client.getRootUrl();
        Set<String> redirectUris = client.getRedirectUris();
        validate(session, rootUrl, redirectUris, sectorIdentifierUri);
    }

    /**
     * 校验 redirect URI 与 sector identifier URI 配置。
     * @param session Keycloak 会话（拉取 sector URI 时需要）
     * @param rootUrl 客户端 root URL
     * @param redirectUris 注册的 redirect URI
     * @param sectorIdentifierUri sector identifier URI（可为空）
     */
        if (sectorIdentifierUri == null || sectorIdentifierUri.isEmpty()) {
            validateClientRedirectUris(rootUrl, redirectUris);
            return;
        }
        validateSectorIdentifierUri(sectorIdentifierUri);
        validateSectorIdentifierUri(session, rootUrl, redirectUris, sectorIdentifierUri);
    }

    private static void validateClientRedirectUris(String rootUrl, Set<String> redirectUris) throws ProtocolMapperConfigException {
        Set<String> hosts = new HashSet<>();
        for (String redirectUri : PairwiseSubMapperUtils.resolveValidRedirectUris(rootUrl, redirectUris)) {
            try {
                URI uri = new URI(redirectUri);
                hosts.add(uri.getHost());
            } catch (URISyntaxException e) {
                throw new ProtocolMapperConfigException("Client contained an invalid redirect URI.",
                        PAIRWISE_MALFORMED_CLIENT_REDIRECT_URI, e);
            }
        }

        if (hosts.isEmpty()) {
            throw new ProtocolMapperConfigException("Client redirect URIs must contain a valid host component.",
                    PAIRWISE_CLIENT_REDIRECT_URIS_MISSING_HOST);
        }

        if (hosts.size() > 1) {
            throw new ProtocolMapperConfigException("Without a configured Sector Identifier URI, client redirect URIs must not contain multiple host components.", PAIRWISE_CLIENT_REDIRECT_URIS_MULTIPLE_HOSTS);
        }
    }

    private static void validateSectorIdentifierUri(String sectorIdentifierUri) throws ProtocolMapperConfigException {
        URI uri;
        try {
            uri = new URI(sectorIdentifierUri);
        } catch (URISyntaxException e) {
            throw new ProtocolMapperConfigException("Invalid Sector Identifier URI.",
                    PAIRWISE_MALFORMED_SECTOR_IDENTIFIER_URI, e);
        }
        if (uri.getScheme() == null || uri.getHost() == null) {
            throw new ProtocolMapperConfigException("Invalid Sector Identifier URI.",
                    PAIRWISE_MALFORMED_SECTOR_IDENTIFIER_URI);
        }
    }

    private static void validateSectorIdentifierUri(KeycloakSession session, String rootUrl, Set<String> redirectUris, String sectorIdentifierUri) throws ProtocolMapperConfigException {
        Set<String> sectorRedirects = getSectorRedirects(session, sectorIdentifierUri);
        if (!PairwiseSubMapperUtils.matchesRedirects(rootUrl, redirectUris, sectorRedirects)) {
            throw new ProtocolMapperConfigException("Client redirect URIs does not match redirect URIs fetched from the Sector Identifier URI.",
                    PAIRWISE_REDIRECT_URIS_MISMATCH);
        }
    }

    private static Set<String> getSectorRedirects(KeycloakSession session, String sectorIdentifierUri) throws ProtocolMapperConfigException {
        try {
            List<String> sectorRedirects = JsonSerialization.readValue(
                    session.getProvider(HttpClientProvider.class).getString(sectorIdentifierUri),
                    TypedList.class
            );
            return new HashSet<>(sectorRedirects);
        } catch (IOException e) {
            throw new ProtocolMapperConfigException("Failed to get redirect URIs from the Sector Identifier URI.",
                    PAIRWISE_FAILED_TO_GET_REDIRECT_URIS, e);
        }
    }

    /** 用于 JSON 反序列化 sector redirect URI 字符串列表 */
    public static class TypedList extends ArrayList<String> {}

}
