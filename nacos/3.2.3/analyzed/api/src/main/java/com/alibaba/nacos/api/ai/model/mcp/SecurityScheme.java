package com.alibaba.nacos.api.ai.model.mcp;

/**
 * SecurityScheme 表示安全认证方案的模型，包括类型、方案、位置、名称及默认凭证等信息.
 * 用于描述 API 的安全机制.
 *
 * @author xinluo
 */
public class SecurityScheme {
    
    /** 安全方案 ID，供工具引用。 */
    private String id;
    
    /** 安全方案类型，可取 'http'、'apiKey'、'localEnv' 或自定义扩展。 */
    private String type;
    
    /** HTTP 认证子方案；当 {@link #type} 为 `http` 时有效，可取 `basic` 或 `bearer`。 */
    private String scheme;
    
    /** 凭证传递位置，可取 `query` 或 `header`。 */
    private String in;
    
    /**
     * 方案名称；{@link #type} 为 `apiKey` 时表示 Header/Query 键名，
     * 为 `localEnv` 时表示环境变量名。
     */
    private String name;
    
    /** 未显式传入身份凭证时使用的默认凭证（可选）。 */
    private String defaultCredential;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getScheme() {
        return scheme;
    }
    
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
    
    public String getIn() {
        return in;
    }
    
    public void setIn(String in) {
        this.in = in;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDefaultCredential() {
        return defaultCredential;
    }
    
    public void setDefaultCredential(String defaultCredential) {
        this.defaultCredential = defaultCredential;
    }
}
