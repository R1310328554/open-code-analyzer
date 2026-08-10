package org.keycloak.ssf;

/**
 * SSF 配置文件枚举，标识服务器所遵循的 SSF 规范版本。
 */
public enum SsfProfile {

    /** 标准 SSF 1.0 规范，定义见 https://openid.net/specs/openid-sharedsignals-framework-1_0-final.html */
    SSF_1_0,

    /**
     * 旧版 SSE CAEP 配置文件，定义见 https://openid.net/specs/openid-sse-framework-1_0.html。
     * <p>用于兼容 Apple Business Manager / Apple School Manager。</p>
     */
    SSE_CAEP

}
