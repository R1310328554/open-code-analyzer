package org.keycloak.representations.docker;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.representations.JsonWebToken;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Docker Registry 认证 JWT 令牌，继承 {@link JsonWebToken} 并携带 {@link DockerAccess} 访问权限列表。
 * <p>
 * JSON 载荷示例：
 * <pre>
 * {
 *   "iss": "auth.docker.com",
 *   "sub": "jlhawn",
 *   "aud": "registry.docker.com",
 *   "exp": 1415387315,
 *   "access": [
 *     { "type": "repository", "name": "samalba/my-app", "actions": ["push"] }
 *   ]
 * }
 * </pre>
 */
public class DockerResponseToken extends JsonWebToken {

    /** Docker 访问权限条目列表。 */
    @JsonProperty("access")
    protected List<DockerAccess> accessItems = new ArrayList<>();

    public List<DockerAccess> getAccessItems() {
        return accessItems;
    }

    @Override
    public DockerResponseToken id(final String id) {
        super.id(id);
        return this;
    }

    @Override
    public DockerResponseToken exp(final Long expiration) {
        super.exp(expiration);
        return this;
    }

    @Override
    public DockerResponseToken nbf(final Long notBefore) {
        super.nbf(notBefore);
        return this;
    }

    @Override
    public DockerResponseToken issuedNow() {
        super.issuedNow();
        return this;
    }

    @Override
    public DockerResponseToken iat(final Long issuedAt) {
        super.iat(issuedAt);
        return this;
    }

    @Override
    public DockerResponseToken issuer(final String issuer) {
        super.issuer(issuer);
        return this;
    }

    @Override
    public DockerResponseToken audience(final String... audience) {
        super.audience(audience);
        return this;
    }

    @Override
    public DockerResponseToken subject(final String subject) {
        super.subject(subject);
        return this;
    }

    @Override
    public DockerResponseToken type(final String type) {
        super.type(type);
        return this;
    }

    @Override
    public DockerResponseToken issuedFor(final String issuedFor) {
        super.issuedFor(issuedFor);
        return this;
    }
}
