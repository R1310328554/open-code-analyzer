package org.keycloak.services.util;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Vite 构建 manifest 中的单个 chunk 条目（参见 {@link ViteManifest}）。
 * <p>描述前端资源的文件名、入口标记、依赖导入及关联 CSS/静态资源。</p>
 */
public record Chunk (
    @JsonProperty(required = true)
    String file,

    @JsonProperty
    Optional<String> src,

    @JsonProperty
    Optional<String> name,

    @JsonProperty
    Optional<Boolean> isEntry,

    @JsonProperty
    Optional<Boolean> isDynamicEntry,

    @JsonProperty
    Optional<String[]> imports,

    @JsonProperty
    Optional<String[]> dynamicImports,

    @JsonProperty
    Optional<String[]> assets,

    @JsonProperty Optional<String[]> css
){}
