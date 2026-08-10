package org.keycloak.representations.docker;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Docker Registry 客户端可理解的认证成功响应，JSON 格式如下：
 * <pre>
 * {
 *   "token": "eyJh...nSQ",
 *   "expires_in": 300,
 *   "issued_at": "2016-09-02T10:56:33Z"
 * }
 * </pre>
 */
public class DockerResponse {

    /** Bearer 令牌字符串。 */
    @JsonProperty("token")
    private String token;
    /** 令牌有效期（秒）。 */
    @JsonProperty("expires_in")
    private Integer expires_in;
    /** 令牌签发时间（ISO 8601）。 */
    @JsonProperty("issued_at")
    private String issued_at;

    /** 默认无参构造器。 */
    public DockerResponse() {
    }

    /**
     * 构造 Docker 认证响应。
     *
     * @param token 令牌字符串
     * @param expires_in 有效期（秒）
     * @param issued_at 签发时间
     */
    public DockerResponse(final String token, final Integer expires_in, final String issued_at) {
        this.token = token;
        this.expires_in = expires_in;
        this.issued_at = issued_at;
    }

    public String getToken() {
        return token;
    }

    public DockerResponse setToken(final String token) {
        this.token = token;
        return this;
    }

    public Integer getExpires_in() {
        return expires_in;
    }

    public DockerResponse setExpires_in(final Integer expires_in) {
        this.expires_in = expires_in;
        return this;
    }

    public String getIssued_at() {
        return issued_at;
    }

    public DockerResponse setIssued_at(final String issued_at) {
        this.issued_at = issued_at;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DockerResponse)) return false;

        final DockerResponse that = (DockerResponse) o;

        if (token != null ? !token.equals(that.token) : that.token != null) return false;
        if (expires_in != null ? !expires_in.equals(that.expires_in) : that.expires_in != null) return false;
        return issued_at != null ? issued_at.equals(that.issued_at) : that.issued_at == null;

    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (expires_in != null ? expires_in.hashCode() : 0);
        result = 31 * result + (issued_at != null ? issued_at.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DockerResponse{" +
                "token='" + token + '\'' +
                ", expires_in='" + expires_in + '\'' +
                ", issued_at='" + issued_at + '\'' +
                '}';
    }
}
