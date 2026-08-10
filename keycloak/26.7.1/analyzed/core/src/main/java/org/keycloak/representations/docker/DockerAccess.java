package org.keycloak.representations.docker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Docker Registry v2 认证规范中的访问权限（access）条目。
 * <p>
 * JSON 格式示例：
 * <pre>
 * {
 *   "type": "repository",
 *   "name": "samalba/my-app",
 *   "actions": ["push", "pull"]
 * }
 * </pre>
 * 亦可通过 {@code scope} 查询参数（{@code type:name:actions}）构造。
 */
public class DockerAccess {

    /** scope 参数中类型字段的索引。 */
    public static final int ACCESS_TYPE = 0;
    /** scope 参数中仓库名称字段的索引。 */
    public static final int REPOSITORY_NAME = 1;
    /** scope 参数中权限列表字段的索引。 */
    public static final int PERMISSIONS = 2;
    /** URL 解码使用的字符编码。 */
    public static final String DECODE_ENCODING = "UTF-8";

    /** 访问类型（如 repository）。 */
    @JsonProperty("type")
    protected String type;
    /** 资源名称（如镜像仓库路径）。 */
    @JsonProperty("name")
    protected String name;
    /** 允许的操作列表（如 push、pull）。 */
    @JsonProperty("actions")
    protected List<String> actions;

    /** 默认无参构造器。 */
    public DockerAccess() {
    }

    /**
     * 从 Docker scope 查询参数解析访问权限。
     * <p>
     * 格式为 {@code type:name:action1,action2,...}，经 URL 解码后按 {@code :} 分割。
     *
     * @param scopeParam scope 查询参数字符串
     */
    public DockerAccess(final String scopeParam) {
        if (scopeParam != null) {
            try {
                final String unencoded = URLDecoder.decode(scopeParam, DECODE_ENCODING);
                final String[] parts = unencoded.split(":");
                if (parts.length != 3) {
                    throw new IllegalArgumentException(String.format("Expecting input string to have %d parts delineated by a ':' character.  " +
                            "Found %d parts: %s", 3, parts.length, unencoded));
                }

                type = parts[ACCESS_TYPE];
                name = parts[REPOSITORY_NAME];
                if (parts[PERMISSIONS] != null) {
                    actions = Arrays.asList(parts[PERMISSIONS].split(","));
                }
            } catch (final UnsupportedEncodingException e) {
                throw new IllegalStateException("Error attempting to decode scope parameter using encoding: " + DECODE_ENCODING);
            }
        }
    }

    public String getType() {
        return type;
    }

    public DockerAccess setType(final String type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public DockerAccess setName(final String name) {
        this.name = name;
        return this;
    }

    public List<String> getActions() {
        return actions;
    }

    public DockerAccess setActions(final List<String> actions) {
        this.actions = actions;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DockerAccess)) return false;

        final DockerAccess that = (DockerAccess) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return actions != null ? actions.equals(that.actions) : that.actions == null;

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (actions != null ? actions.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DockerAccess{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", actions=" + actions +
                '}';
    }
}
