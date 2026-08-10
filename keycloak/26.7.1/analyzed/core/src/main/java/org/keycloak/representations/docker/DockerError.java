package org.keycloak.representations.docker;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Docker Registry 错误响应的 JSON 表示，格式如下：
 * <pre>
 * {
 *   "code": "UNAUTHORIZED",
 *   "message": "access to the requested resource is not authorized",
 *   "detail": [
 *     { "Type": "repository", "Name": "samalba/my-app", "Action": "pull" },
 *     { "Type": "repository", "Name": "samalba/my-app", "Action": "push" }
 *   ]
 * }
 * </pre>
 */
public class DockerError {


    /** 错误代码（如 UNAUTHORIZED）。 */
    @JsonProperty("code")
    private final String errorCode;
    /** 人类可读的错误消息。 */
    @JsonProperty("message")
    private final String message;
    /** 错误详情列表，每项描述被拒绝的访问权限。 */
    @JsonProperty("detail")
    private final List<DockerAccess> dockerErrorDetails;

    /**
     * 构造 Docker 错误对象。
     *
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param dockerErrorDetails 访问权限详情列表
     */
    public DockerError(final String errorCode, final String message, final List<DockerAccess> dockerErrorDetails) {
        this.errorCode = errorCode;
        this.message = message;
        this.dockerErrorDetails = dockerErrorDetails;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public List<DockerAccess> getDockerErrorDetails() {
        return dockerErrorDetails;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DockerError)) return false;

        final DockerError that = (DockerError) o;

        if (!Objects.equals(errorCode, that.errorCode)) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        return dockerErrorDetails != null ? dockerErrorDetails.equals(that.dockerErrorDetails) : that.dockerErrorDetails == null;
    }

    @Override
    public int hashCode() {
        int result = errorCode != null ? errorCode.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (dockerErrorDetails != null ? dockerErrorDetails.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DockerError{" +
                "errorCode=" + errorCode +
                ", message='" + message + '\'' +
                ", dockerErrorDetails=" + dockerErrorDetails +
                '}';
    }
}
