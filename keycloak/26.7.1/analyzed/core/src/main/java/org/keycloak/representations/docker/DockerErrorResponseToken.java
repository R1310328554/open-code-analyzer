package org.keycloak.representations.docker;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Docker Registry 错误响应令牌，包装 {@link DockerError} 列表供客户端解析。
 */
public class DockerErrorResponseToken {


    /** 错误列表。 */
    @JsonProperty("errors")
    private final List<DockerError> errorList;

    /**
     * 构造错误响应令牌。
     *
     * @param errorList Docker 错误列表
     */
    public DockerErrorResponseToken(final List<DockerError> errorList) {
        this.errorList = errorList;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DockerErrorResponseToken)) return false;

        final DockerErrorResponseToken that = (DockerErrorResponseToken) o;

        return errorList != null ? errorList.equals(that.errorList) : that.errorList == null;
    }

    @Override
    public int hashCode() {
        return errorList != null ? errorList.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "DockerErrorResponseToken{" +
                "errorList=" + errorList +
                '}';
    }
}
