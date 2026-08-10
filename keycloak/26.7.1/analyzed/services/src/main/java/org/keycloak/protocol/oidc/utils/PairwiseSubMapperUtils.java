package org.keycloak.protocol.oidc.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.protocol.oidc.mappers.AbstractPairwiseSubMapper;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;

import org.jboss.logging.Logger;

/**
 * Pairwise Subject 映射器工具：解析 sector identifier、校验 redirect URI 与 sector 一致性。
 */
public class PairwiseSubMapperUtils {
    private static final Logger logger = Logger.getLogger(PairwiseSubMapperUtils.class);

    /**
     * 将客户端 root URL 与 redirect URI 列表解析为绝对 URI 集合。
     *
     * @param clientRootUrl 客户端 root URL
     * @param clientRedirectUris 注册的 redirect URI 集合
     * @return 有效的绝对 redirect URI 集合
     */
    public static Set<String> resolveValidRedirectUris(String clientRootUrl, Collection<String> clientRedirectUris) {
        if (clientRedirectUris == null) {
            return Collections.emptySet();
        }

        Set<String> validRedirects = new HashSet<String>();
        for (String redirectUri : clientRedirectUris) {
            if (redirectUri.startsWith("/")) {
                redirectUri = relativeToAbsoluteURI(clientRootUrl, redirectUri);
                logger.debugv("replacing relative valid redirect with: {0}", redirectUri);
            }
            if (redirectUri != null) {
                validRedirects.add(redirectUri);
            }
        }
        return validRedirects.stream()
                .filter(r -> r != null && !r.trim().isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * 从 sector identifier URI 解析有效 sector（主机名）。
     *
     * @param sectorIdentifierUri sector identifier URI
     * @return URI 合法且含 scheme 与 host 时返回 host，否则 null
     */
    public static String resolveValidSectorIdentifier(String sectorIdentifierUri) {
        URI uri;
        try {
            uri = new URI(sectorIdentifierUri);
        } catch (URISyntaxException e) {
            logger.debug("Invalid sector identifier URI", e);
            return null;
        }

        if (uri.getScheme() == null) {
            logger.debugv("Invalid sector identifier URI: {0}", sectorIdentifierUri);
            return null;
        }

        /*if (!uri.getScheme().equalsIgnoreCase("https")) {
            logger.debugv("The sector identifier URI scheme must be HTTPS. Was '{0}'", uri.getScheme());
            return null;
        }*/

        if (uri.getHost() == null) {
            logger.debug("The sector identifier URI must specify a host");
            return null;
        }

        return uri.getHost();
    }

    /**
     * 从客户端 redirect URI 推断 sector：所有 URI 须位于同一 host。
     *
     * @param clientRootUrl 客户端 root URL
     * @param clientRedirectUris 注册的 redirect URI
     * @return 单一 host 时返回该 host，否则 null
     */
    public static String resolveValidSectorIdentifier(String clientRootUrl, Set<String> clientRedirectUris) {
        Set<String> hosts = new HashSet<>();
        for (String redirectUri : resolveValidRedirectUris(clientRootUrl, clientRedirectUris)) {
            try {
                URI uri = new URI(redirectUri);
                hosts.add(uri.getHost());
            } catch (URISyntaxException e) {
                logger.debugv("client redirect uris contained an invalid uri: {0}", redirectUri);
            }
        }
        if (hosts.isEmpty()) {
            logger.debug("could not infer any valid sector_identifiers from client redirect uris");
            return null;
        }
        if (hosts.size() > 1) {
            logger.debug("the client redirect uris contained multiple hosts");
            return null;
        }
        return hosts.iterator().next();
    }

    /**
     * 校验客户端 redirect URI 是否为 sector identifier 返回列表的子集。
     *
     * @param clientRootUrl 客户端 root URL
     * @param clientRedirectUris 注册的 redirect URI
     * @param sectorRedirects sector identifier URI 拉取的 redirect URI 集合
     * @return 全部匹配则 true
     */
    public static boolean matchesRedirects(String clientRootUrl, Set<String> clientRedirectUris, Set<String> sectorRedirects) {
        Set<String> validRedirects = resolveValidRedirectUris(clientRootUrl, clientRedirectUris);
        for (String redirect : validRedirects) {
            if (!matchesRedirect(sectorRedirects, redirect)) return false;
        }
        return true;
    }

    private static boolean matchesRedirect(Set<String> validRedirects, String redirect) {
        for (String validRedirect : validRedirects) {
            if (validRedirect.endsWith("*") && !validRedirect.contains("?")) {
                // 通配符匹配时忽略 query 部分
                String r = redirect.contains("?") ? redirect.substring(0, redirect.indexOf("?")) : redirect;
                // 去除末尾通配符 *
                int length = validRedirect.length() - 1;
                validRedirect = validRedirect.substring(0, length);
                if (r.startsWith(validRedirect)) return true;
                // 去除末尾斜杠后再比对
                if (length - 1 > 0 && validRedirect.charAt(length - 1) == '/') length--;
                validRedirect = validRedirect.substring(0, length);
                if (validRedirect.equals(r)) return true;
            } else if (validRedirect.equals(redirect)) return true;
        }
        return false;
    }

    private static String relativeToAbsoluteURI(String rootUrl, String relative) {
        if (rootUrl == null || rootUrl.isEmpty()) {
            return null;
        }
        relative = rootUrl + relative;
        return relative;
    }

    /** 从客户端表示中提取所有 Pairwise Subject 协议映射器 */
    public static List<ProtocolMapperRepresentation> getPairwiseSubMappers(ClientRepresentation client) {
        List<ProtocolMapperRepresentation> pairwiseMappers = new LinkedList<>();
        List<ProtocolMapperRepresentation> mappers = client.getProtocolMappers();

        if (mappers != null) {
            client.getProtocolMappers().stream().filter((ProtocolMapperRepresentation mapping) -> {
                return mapping.getProtocolMapper().endsWith(AbstractPairwiseSubMapper.PROVIDER_ID_SUFFIX);
            }).forEach((ProtocolMapperRepresentation mapping) -> {
                pairwiseMappers.add(mapping);
            });
        }

        return pairwiseMappers;
    }
}
