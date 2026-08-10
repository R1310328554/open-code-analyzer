/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.ai.utils;

import com.alibaba.nacos.common.utils.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Resource content encoder shared by Skill and AgentSpec zip parsers.
 * <p>Skill/AgentSpec ZIP 解析共用的资源内容编码器：白名单内按 UTF-8 明文存储，其余 Base64 编码并写入 metadata.encoding=base64。</p>
 *
 * <p>Decision policy: a file is treated as plain text only when its name (or extension)
 * matches the text whitelist; everything else is encoded as Base64 with metadata
 * {@code encoding=base64}. The detection contract used at download time
 * ({@code metadata.encoding == "base64"}) is unchanged, so legacy resources written
 * without metadata continue to be decoded as UTF-8 text.
 *
 * @author nacos
 */
public final class ResourceContentEncoder {
    
    /** 元数据键：标记资源内容编码方式。 */
    public static final String METADATA_ENCODING = "encoding";
    
    /** 元数据值：内容为 Base64 编码的二进制。 */
    public static final String METADATA_ENCODING_BASE64 = "base64";
    
    /** 可安全按 UTF-8 文本存储的扩展名白名单。 */
    private static final Set<String> TEXT_EXTENSIONS;
    
    /** 无扩展名或点文件名的文本白名单（小写匹配）。 */
    private static final Set<String> TEXT_FILE_NAMES;
    
    static {
        Set<String> exts = new HashSet<>();
        Collections.addAll(exts,
            // 标记/文档类
            "md", "markdown", "mdx", "txt", "rst", "adoc", "asciidoc",
            // 结构化数据/配置
            "json", "json5", "yaml", "yml", "xml", "html", "htm", "css", "scss", "sass", "less",
            "properties", "conf", "cfg", "ini", "toml", "env", "tpl", "tmpl", "j2", "mustache",
            "hbs",
            // 常见脚本/源码
            "js", "mjs", "cjs", "ts", "tsx", "jsx", "vue", "svelte",
            "py", "java", "kt", "kts", "scala", "groovy", "go", "rs", "rb", "php",
            "swift", "m", "mm", "c", "h", "cpp", "cc", "cxx", "hpp", "hh", "hxx",
            "cs", "fs", "fsx", "vb", "lua", "r", "pl", "pm", "ex", "exs", "erl",
            "dart", "zig", "nim", "jl", "clj", "cljs", "edn", "elm",
            // Shell/构建脚本
            "sh", "bash", "zsh", "fish", "ps1", "psm1", "bat", "cmd",
            "gradle", "sbt", "make", "mk",
            // 数据/日志
            "sql", "graphql", "gql", "csv", "tsv", "log", "diff", "patch",
            // 其他文本
            "proto", "thrift", "ipynb");
        TEXT_EXTENSIONS = Collections.unmodifiableSet(exts);
        
        Set<String> names = new HashSet<>();
        Collections.addAll(names,
            // 常见无扩展名文本文件
            "dockerfile", "containerfile", "makefile", "rakefile", "gemfile", "gemfile.lock",
            "jenkinsfile", "vagrantfile", "procfile", "brewfile",
            "license", "license.txt", "notice", "readme", "changelog", "authors",
            "contributors", "maintainers", "codeowners", "version", "manifest",
            // 点文件
            ".gitignore", ".gitattributes", ".gitmodules", ".gitkeep",
            ".dockerignore", ".editorconfig", ".env", ".envrc",
            ".npmrc", ".nvmrc", ".yarnrc", ".prettierrc", ".eslintrc",
            ".babelrc", ".browserslistrc", ".stylelintrc");
        TEXT_FILE_NAMES = Collections.unmodifiableSet(names);
    }
    
    private ResourceContentEncoder() {
    }
    
    /**
     * Decide whether the given file should be persisted as UTF-8 text.
     * <p>按文件名与扩展名白名单判定是否按 UTF-8 文本持久化。</p>
     * Text whitelist matches by exact filename (for files with no extension or leading dot)
     * and by lower-cased extension. Anything not matched is treated as binary.
     *
     * @param fileName file name including extension (path prefix is allowed)
     * @return {@code true} if the file is recognized as plain text
     */
    public static boolean isText(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return false;
        }
        String pureName = stripDirectory(fileName).trim();
        if (pureName.isEmpty()) {
            return false;
        }
        String lower = pureName.toLowerCase();
        if (TEXT_FILE_NAMES.contains(lower)) {
            return true;
        }
        int dot = lower.lastIndexOf('.');
        // 无扩展名或尾随点：仅依赖上方文件名白名单
        if (dot <= 0 || dot == lower.length() - 1) {
            return false;
        }
        String ext = lower.substring(dot + 1);
        return TEXT_EXTENSIONS.contains(ext);
    }
    
    /**
     * Convenience inverse of {@link #isText(String)}.
     * <p>{@link #isText(String)} 的逆判断，即是否为二进制资源。</p>
     *
     * @param fileName file name including extension
     * @return {@code true} when the file is not in the text whitelist
     */
    public static boolean isBinary(String fileName) {
        return !isText(fileName);
    }
    
    /**
     * Encode raw resource bytes for storage.
     * <p>编码原始字节：文本直接 UTF-8 字符串；二进制 Base64 并附带 encoding 元数据。</p>
     * Text files are stored as UTF-8 strings with no encoding metadata; binary files are
     * stored as Base64 strings with {@code metadata.encoding=base64} so download paths
     * can restore the original bytes via {@code SkillUtils.resolveResourceBytes}.
     *
     * @param data     raw bytes; {@code null} is treated as empty content
     * @param fileName file name used to look up the text whitelist
     * @return immutable encoded content holder
     */
    public static EncodedContent encode(byte[] data, String fileName) {
        if (data == null || data.length == 0) {
            return new EncodedContent("", null);
        }
        if (isText(fileName)) {
            return new EncodedContent(new String(data, StandardCharsets.UTF_8), null);
        }
        Map<String, Object> metadata = new HashMap<>(2);
        metadata.put(METADATA_ENCODING, METADATA_ENCODING_BASE64);
        return new EncodedContent(Base64.getEncoder().encodeToString(data), metadata);
    }
    
    /**
     * Build a metadata map flagging Base64 encoding. Used by storage-side reconstruction
     * <p>构造仅含 Base64 编码标记的可变 metadata，供存储侧重建路径使用。</p>
     * paths that already hold a content string and only need to attach the encoding hint.
     *
     * @return mutable metadata map containing only the Base64 encoding flag
     */
    public static Map<String, Object> base64Metadata() {
        Map<String, Object> metadata = new HashMap<>(2);
        metadata.put(METADATA_ENCODING, METADATA_ENCODING_BASE64);
        return metadata;
    }
    
    private static String stripDirectory(String name) {
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            return name.substring(slash + 1);
        }
        int back = name.lastIndexOf('\\');
        return back >= 0 ? name.substring(back + 1) : name;
    }
    
    /**
     * Immutable holder describing an encoded resource: the textual content and optional metadata.
     * <p>编码结果不可变容器：content 与可选 metadata；纯文本时 metadata 为 null。</p>
     * Metadata is {@code null} when no special encoding hint is needed (plain text).
     */
    public static final class EncodedContent {
        
        private final String content;
        
        private final Map<String, Object> metadata;
        
        EncodedContent(String content, Map<String, Object> metadata) {
            this.content = content;
            this.metadata = metadata;
        }
        
        public String getContent() {
            return content;
        }
        
        public Map<String, Object> getMetadata() {
            return metadata;
        }
    }
}
