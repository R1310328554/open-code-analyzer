package org.keycloak.operator.crds.v2beta1.deployment.spec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.sundr.builder.annotations.Buildable;

/**
 * 集群首次创建时的引导管理员（Bootstrap Admin）配置。
 *
 * <p>仅在初始集群 bootstrap 阶段使用，用于创建临时管理员用户或服务账号凭证。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
public class BootstrapAdminSpec {

    /** 引导管理员用户凭证配置。 */
    public static class User {
        /** 包含 username 与 password 键的 Secret 名称。 */
        @JsonPropertyDescription("Name of the Secret that contains the username and password keys")
    	private String secret;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    /** 引导管理员服务账号（客户端）凭证配置。 */
    public static class Service {
        /** 包含 client-id 与 client-secret 键的 Secret 名称。 */
        @JsonPropertyDescription("Name of the Secret that contains the client-id and client-secret keys")
    	private String secret;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

    }

    //private Integer expiration;
    /** 引导管理员用户配置。 */
    @JsonPropertyDescription("Configures the bootstrap admin user")
    private User user;
    /** 引导管理员服务账号配置。 */
    @JsonPropertyDescription("Configures the bootstrap admin service account")
    private Service service;

    /*public Integer getExpiration() {
        return expiration;
    }

    public void setExpiration(Integer expiration) {
        this.expiration = expiration;
    }*/

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

}