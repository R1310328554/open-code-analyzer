package org.keycloak.testframework.oauth;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

import org.keycloak.OAuth2Constants;
import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.common.util.KeyUtils;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.ECDSASignatureSignerContext;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.def.DefaultCryptoProvider;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKBuilder;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.protocol.oidc.representations.OIDCConfigurationRepresentation;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.util.JsonSerialization;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import static org.keycloak.common.crypto.CryptoConstants.EC_KEY_SECP256R1;

/**
 * 用于身份联合与代理登录测试的模拟 OAuth/OIDC 身份提供者。
 * <p>
 * 在嵌入式 HTTP 服务器上暴露 OpenID 配置与 JWKS 端点，并提供 JWT 签名辅助方法。
 */
public class OAuthIdentityProvider {

    private final HttpServer httpServer;

    private final OAuthIdentityProviderKeys keys;
    private final OAuthIdentityProviderConfigBuilder.OAuthIdentityProviderConfiguration config;

    private int keysRequestCount = 0;

    /**
     * 注册 well-known 与 JWKS 端点并初始化签名密钥。
     *
     * @param httpServer 嵌入式 HTTP 服务器
     * @param config 身份提供者运行配置（模式、JWK use 等）
     */
    public OAuthIdentityProvider(HttpServer httpServer, OAuthIdentityProviderConfigBuilder.OAuthIdentityProviderConfiguration config) {
        this.config = config;
        if (!CryptoIntegration.isInitialised()) {
            CryptoIntegration.setProvider(new DefaultCryptoProvider());
        }

        this.httpServer = httpServer;
        httpServer.createContext("/idp/.well-known/openid-configuration", new WellKnownHandler());
        httpServer.createContext("/idp/jwks", new JwksHttpHandler());

        keys = new OAuthIdentityProviderKeys(config);
    }

    /** 使用默认密钥对 {@link JsonWebToken} 进行 ES256 签名。 */
    public String encodeToken(JsonWebToken token) {
        return encodeToken(token, keys);
    }

    /**
     * 使用指定密钥对令牌签名。
     *
     * @param token 待签名的 JWT 内容
     * @param keys 签名密钥材料
     * @return 紧凑 JWS 字符串
     */
    public String encodeToken(JsonWebToken token, OAuthIdentityProviderKeys keys) {
        return new JWSBuilder().type("JWT").jsonContent(token).sign(new ECDSASignatureSignerContext(keys.getKeyWrapper()));
    }

    /** 签发身份断言 JWT（ID-JAG），使用 {@link OAuth2Constants#IDENTITY_ASSERTION_JWT_HEADER_TYPE} 类型头。 */
    public String encodeIDJAG(JsonWebToken token) {
        return new JWSBuilder().type(OAuth2Constants.IDENTITY_ASSERTION_JWT_HEADER_TYPE).jsonContent(token).sign(new ECDSASignatureSignerContext(keys.getKeyWrapper()));
    }

    /** 根据当前配置新建一组独立的签名密钥。 */
    public OAuthIdentityProviderKeys createKeys() {
        return new OAuthIdentityProviderKeys(config);
    }

    /** @return 本实例初始化时创建的默认密钥 */
    public OAuthIdentityProviderKeys getKeys() {
        return keys;
    }

    /** @return JWKS 端点被请求的次数 */
    public int getKeysRequestCount() {
        return keysRequestCount;
    }

    /** 从 HTTP 服务器移除 OpenID 配置与 JWKS 上下文。 */
    public void close() {
        httpServer.removeContext("/idp/.well-known/openid-configuration");
        httpServer.removeContext("/idp/jwks");
    }

    /** 返回最小 OIDC 发现文档，指向本地 JWKS URI。 */
    public class WellKnownHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            OIDCConfigurationRepresentation oidcConfig = new OIDCConfigurationRepresentation();
            oidcConfig.setJwksUri("http://127.0.0.1:8500/idp/jwks");
            String oidcConfigString = JsonSerialization.writeValueAsString(oidcConfig);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, oidcConfigString.length());
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(oidcConfigString.getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        }

    }

    /** 返回 JWK Set；Kubernetes 模式下使用 {@code application/jwk-set+json} 内容类型。 */
    public class JwksHttpHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            boolean kubernetes = OAuthIdentityProviderConfigBuilder.Mode.KUBERNETES.equals(config.mode());

            if (kubernetes) {
                exchange.getResponseHeaders().add("Content-Type", "application/jwk-set+json");
            } else {
                exchange.getResponseHeaders().add("Content-Type", "application/json");
            }
            exchange.sendResponseHeaders(200, keys.getJwksString().length());
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(keys.getJwksString().getBytes(StandardCharsets.UTF_8));
            outputStream.close();

            keysRequestCount++;
        }

    }

    /** 模拟 IdP 的 EC 签名密钥与 JWKS JSON 表示。 */
    public static class OAuthIdentityProviderKeys {

        private final KeyWrapper keyWrapper;

        private final String jwksString;

        /**
         * 按配置模式生成 EC P-256 密钥对并序列化 JWKS。
         *
         * @param config 身份提供者配置
         */
        public OAuthIdentityProviderKeys(OAuthIdentityProviderConfigBuilder.OAuthIdentityProviderConfiguration config) {
            try {
                boolean spiffe = OAuthIdentityProviderConfigBuilder.Mode.SPIFFE.equals(config.mode());

                KeyUse keyUse = spiffe ? KeyUse.JWT_SVID : KeyUse.SIG;

                KeyPair keyPair = KeyUtils.generateEcKeyPair(EC_KEY_SECP256R1);

                JWK jwk = JWKBuilder.create().ec(keyPair.getPublic());
                if (!spiffe) {
                    jwk.setAlgorithm("ES256");
                }
                if (config.jwkUse()) {
                    jwk.setPublicKeyUse(keyUse.getSpecName());
                } else {
                    jwk.setPublicKeyUse(null);
                }

                Map<String, Object> jwks = new HashMap<>();
                jwks.put("keys", new JWK[] { jwk });

                if (spiffe) {
                    jwks.put("spiffe_sequence", 1);
                    jwks.put("spiffe_refresh_hint", 300);
                }

                jwksString = JsonSerialization.writeValueAsString(jwks);

                keyWrapper = new KeyWrapper();
                keyWrapper.setKid(jwk.getKeyId());
                keyWrapper.setPublicKey(keyPair.getPublic());
                keyWrapper.setPrivateKey(keyPair.getPrivate());
                keyWrapper.setUse(KeyUse.SIG);
                keyWrapper.setAlgorithm(Algorithm.ES256);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /** @return 用于 JWS 签名的 {@link KeyWrapper} */
        public KeyWrapper getKeyWrapper() {
            return keyWrapper;
        }

        /** @return JWKS 文档的 JSON 字符串 */
        public String getJwksString() {
            return jwksString;
        }
    }

}
