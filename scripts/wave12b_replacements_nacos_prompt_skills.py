"""Chinese annotation replacements for Nacos 3.2.3 wave12b [15:30] prompt/skills/remote."""

R: dict[str, list[tuple[str, str]]] = {}

# --- prompt ---

R["api/src/main/java/com/alibaba/nacos/api/ai/model/prompt/PromptVariable.java"] = [
    (
        "/**\n * Prompt variable definition with optional default value.\n *\n"
        " * <p>Represents a variable placeholder (e.g., {{variableName}}) in a prompt template,\n"
        " * along with its optional default value and description.</p>\n *\n"
        " * @author nacos\n */",
        "/**\n * Prompt 模板变量定义，可携带可选默认值与描述信息。\n *\n"
        " * <p>表示 Prompt 模板中的占位符（如 {{variableName}}），\n"
        " * 并记录该变量的默认值与用途说明。</p>\n *\n"
        " * @author nacos\n */",
    ),
    (
        "    /**\n     * Variable name (matches the placeholder name in template, e.g., \"question\" for {{question}}).\n     */",
        "    /** 变量名，与模板占位符一致（如 {{question}} 对应 \"question\"）。 */",
    ),
    (
        "    /**\n     * Default value for this variable. Null means the variable has no default (considered required).\n     */",
        "    /** 变量默认值；为 null 表示无默认值（视为必填）。 */",
    ),
    (
        "    /**\n     * Optional description explaining the purpose or expected content of this variable.\n     */",
        "    /** 可选描述，说明变量用途或期望填入的内容。 */",
    ),
]

R["api/src/main/java/com/alibaba/nacos/api/ai/model/prompt/PromptVersionInfo.java"] = [
    (
        "/**\n * Prompt version information.\n *\n * @author nacos\n */",
        "/**\n * Prompt 版本详情，在摘要信息基础上扩展模板正文与变量列表。\n *\n * @author nacos\n */",
    ),
    (
        "    private String template;",
        "    /** Prompt 模板正文内容。 */\n    private String template;",
    ),
    (
        "    private String md5;",
        "    /** 模板内容的 MD5 摘要，用于 CAS 乐观锁校验。 */\n    private String md5;",
    ),
    (
        "    private List<PromptVariable> variables;",
        "    /** 模板变量定义列表。 */\n    private List<PromptVariable> variables;",
    ),
]

R["api/src/main/java/com/alibaba/nacos/api/ai/model/prompt/PromptVersionSummary.java"] = [
    (
        "/**\n * Prompt version summary for prompt version list response.\n *\n * @author nacos\n */",
        "/**\n * Prompt 版本摘要，用于版本列表接口响应。\n *\n * @author nacos\n */",
    ),
    (
        "    private String promptKey;",
        "    /** Prompt 唯一标识键。 */\n    private String promptKey;",
    ),
    (
        "    private String version;",
        "    /** 版本号（如 major.minor.patch）。 */\n    private String version;",
    ),
    (
        "    private String status;",
        "    /** 版本状态（如草稿、审核中、已上线等）。 */\n    private String status;",
    ),
    (
        "    private String commitMsg;",
        "    /** 提交/发布说明信息。 */\n    private String commitMsg;",
    ),
    (
        "    private String srcUser;",
        "    /** 创建或最后修改该版本的用户标识。 */\n    private String srcUser;",
    ),
    (
        "    private Long gmtModified;",
        "    /** 最后修改时间戳（毫秒）。 */\n    private Long gmtModified;",
    ),
    (
        "    private String publishPipelineInfo;",
        "    /** 发布流水线相关信息（JSON 字符串）。 */\n    private String publishPipelineInfo;",
    ),
    (
        "    private Long downloadCount;",
        "    /** 该版本的累计下载次数。 */\n    private Long downloadCount;",
    ),
]

# --- skills ---

R["api/src/main/java/com/alibaba/nacos/api/ai/model/skills/BatchUploadResult.java"] = [
    (
        "/**\n * Result of batch skill upload from a multi-skill zip archive.\n *\n * @author nacos\n */",
        "/**\n * 多 Skill ZIP 批量上传的结果汇总。\n *\n * <p>记录成功上传的 Skill 名称列表，以及失败项及其原因。</p>\n *\n * @author nacos\n */",
    ),
    (
        "    private List<String> succeeded;",
        "    /** 上传成功的 Skill 名称列表。 */\n    private List<String> succeeded;",
    ),
    (
        "    private List<FailedItem> failed;",
        "    /** 上传失败的 Skill 条目列表。 */\n    private List<FailedItem> failed;",
    ),
    (
        "    /**\n     * Represents a skill that failed during batch upload.\n     */",
        "    /** 批量上传过程中失败的单个 Skill 条目。 */",
    ),
    (
        "        private String name;",
        "        /** 失败的 Skill 名称。 */\n        private String name;",
    ),
    (
        "        private String reason;",
        "        /** 失败原因描述。 */\n        private String reason;",
    ),
]

R["api/src/main/java/com/alibaba/nacos/api/ai/model/skills/Skill.java"] = [
    (
        "/**\n * Claude Skill entity for independent Skills management.\n * Simplified structure with core fields only.\n *\n * @author nacos\n */",
        "/**\n * Claude Skill 实体，用于独立 Skill 管理。\n *\n * <p>精简结构，仅包含 SKILL.md 正文与资源映射等核心字段。</p>\n *\n * @author nacos\n */",
    ),
    (
        "    /**\n     * Full SKILL.md content.\n     */",
        "    /** 完整 SKILL.md Markdown 正文。 */",
    ),
    (
        "    /**\n     * Resource map (note: singular resource, key is resource name).\n     */",
        "    /** 资源映射（字段名为 resource，键为资源名）。 */",
    ),
]

R["api/src/main/java/com/alibaba/nacos/api/ai/model/skills/SkillBase.java"] = [
    (
        "/**\n * Base class for Skill model objects. Contains common basic info fields shared across Skill-related models.\n *\n * @author nacos\n * @since 3.2.0\n */",
        "/**\n * Skill 模型基类，封装各 Skill 相关模型共用的基础信息字段。\n *\n * @author nacos\n * @since 3.2.0\n */",
    ),
    (
        "    private String namespaceId;",
        "    /** Nacos 命名空间 ID。 */\n    private String namespaceId;",
    ),
    (
        "    private String name;",
        "    /** Skill 名称（唯一标识）。 */\n    private String name;",
    ),
    (
        "    private String description;",
        "    /** Skill 的人类可读描述。 */\n    private String description;",
    ),
]

R["api/src/main/java/com/alibaba/nacos/api/ai/model/skills/SkillBasicInfo.java"] = [
    (
        "/**\n * Skill basic info for list response.\n *\n * @author nacos\n */",
        "/**\n * Skill 基础信息，用于列表接口响应。\n *\n * @author nacos\n */",
    ),
    (
        "    private Long updateTime;",
        "    /** 最后更新时间戳（毫秒）。 */\n    private Long updateTime;",
    ),
]

R["api/src/main/java/com/alibaba/nacos/api/ai/model/skills/SkillMeta.java"] = [
    (
        "/**\n * Skill metadata for admin API response.\n * Contains governance metadata and all version summaries.\n *\n * @author nacos\n */",
        "/**\n * Skill 管理端元数据响应，包含治理信息与全部版本摘要。\n *\n * @author nacos\n */",
    ),
    (
        "    /**\n     * All version summaries for this skill.\n     */",
        "    /** 该 Skill 的全部版本摘要列表。 */",
    ),
    (
        "    /**\n     * Summary of a single skill version for admin display.\n     */",
        "    /** 单个 Skill 版本摘要，供管理端展示。 */",
    ),
    (
        "        private String version;",
        "        /** 版本号。 */\n        private String version;",
    ),
    (
        "        private String status;",
        "        /** 版本状态。 */\n        private String status;",
    ),
    (
        "        private String author;",
        "        /** 版本作者。 */\n        private String author;",
    ),
    (
        "        private String commitMsg;",
        "        /** 提交/发布说明。 */\n        private String commitMsg;",
    ),
    (
        "        private Long createTime;",
        "        /** 创建时间戳（毫秒）。 */\n        private Long createTime;",
    ),
    (
        "        private Long updateTime;",
        "        /** 最后更新时间戳（毫秒）。 */\n        private Long updateTime;",
    ),
    (
        "        private String publishPipelineInfo;",
        "        /** 发布流水线信息。 */\n        private String publishPipelineInfo;",
    ),
    (
        "        /**\n         * Download count for this version.\n         */",
        "        /** 该版本累计下载次数。 */",
    ),
]

R["api/src/main/java/com/alibaba/nacos/api/ai/model/skills/SkillResource.java"] = [
    (
        "/**\n * Claude Skill Resource structure.\n *\n * @author nacos\n */",
        "/**\n * Claude Skill 资源结构，描述单个附属资源文件。\n *\n * @author nacos\n */",
    ),
    (
        "    /**\n     * Resource name (includes file extension, e.g., config_check_template.json).\n     */",
        "    /** 资源文件名（含扩展名，如 config_check_template.json）。 */",
    ),
    (
        "    /**\n     * Resource type: template, data, script, etc.\n     */",
        "    /** 资源类型：template、data、script 等。 */",
    ),
    (
        "    /**\n     * Resource content (string format, read from independent configuration).\n     */",
        "    /** 资源内容（字符串形式，来自独立配置项）。 */",
    ),
    (
        "    /**\n     * Resource metadata (optional).\n     */",
        "    /** 资源元数据（可选）。 */",
    ),
    (
        "    /**\n     * Get resource unique identifier.\n"
        "     * Format: \"type::name\" if type is not blank, otherwise \"name\".\n"
        "     * The separator \"::\" is used because it's not in the allowed character set for type and name.\n     *\n"
        "     * @return resource unique identifier\n     */",
        "    /**\n     * 获取资源唯一标识。\n"
        "     * <p>type 非空时格式为 \"type::name\"，否则为 \"name\"。\n"
        "     * 分隔符 \"::\" 不在 type 与 name 的合法字符集中，可安全拼接。</p>\n     *\n"
        "     * @return 资源唯一标识\n     */",
    ),
]

R["api/src/main/java/com/alibaba/nacos/api/ai/model/skills/SkillSummary.java"] = [
    (
        "/**\n * Skill summary for admin list response.\n * Contains skill basic info plus governance metadata.\n *\n * @author nacos\n */",
        "/**\n * Skill 管理端列表摘要，包含基础信息与治理元数据。\n *\n * @author nacos\n */",
    ),
    (
        "    /**\n     * Owner of the skill resource.\n     */",
        "    /** Skill 资源所有者。 */",
    ),
    (
        "    /**\n     * Whether the skill is globally enabled. true=enable, false=disable.\n     */",
        "    /** 是否全局启用；true 表示启用，false 表示禁用。 */",
    ),
    (
        "    /**\n     * Business tags (JSON string), e.g. [\"tag1\",\"tag2\"].\n     */",
        "    /** 业务标签（JSON 字符串），如 [\"tag1\",\"tag2\"]。 */",
    ),
    (
        "    /**\n     * Source marker for IP attribution (e.g. local/import/sync).\n     */",
        "    /** 来源标记，用于 IP 归属追踪（如 local/import/sync）。 */",
    ),
    (
        "    /**\n     * Visibility scope of skill metadata, e.g. PUBLIC or PRIVATE.\n     */",
        "    /** Skill 元数据可见范围，如 PUBLIC 或 PRIVATE。 */",
    ),
    (
        "    /**\n     * Label -> version mapping, e.g. {\"latest\":\"v3\",\"stable\":\"v2\"}.\n     */",
        "    /** 标签到版本的映射，如 {\"latest\":\"v3\",\"stable\":\"v2\"}。 */",
    ),
    (
        "    /**\n     * The version currently being edited (draft).\n     */",
        "    /** 当前正在编辑的草稿版本。 */",
    ),
    (
        "    /**\n     * The version currently under pipeline review.\n     */",
        "    /** 当前处于发布流水线审核中的版本。 */",
    ),
    (
        "    /**\n     * Number of online versions.\n     */",
        "    /** 已上线版本数量。 */",
    ),
    (
        "    /**\n     * Total download count across all versions.\n     */",
        "    /** 全部版本的累计下载次数。 */",
    ),
]

_SKILL_UTILS = "api/src/main/java/com/alibaba/nacos/api/ai/model/skills/SkillUtils.java"
R[_SKILL_UTILS] = [
    (
        "/**\n * Utility class for Skill operations.\n *\n * @author nacos\n */",
        "/**\n * Skill 操作工具类，提供 Markdown/ZIP 转换、本地同步与 Nacos Config 键构建等功能。\n *\n * @author nacos\n */",
    ),
    (
        "    /**\n     * ZIP local file header signature: PK\\x03\\x04.\n     */",
        "    /** ZIP 本地文件头魔数：PK\\x03\\x04。 */",
    ),
    (
        "    /**\n     * Minimum valid ZIP size (local file header = 30 bytes).\n     */",
        "    /** 合法 ZIP 的最小字节数（本地文件头为 30 字节）。 */",
    ),
    (
        "    /**\n     * Strategy for handling existing skill directories.\n     */",
        "    /** 本地同步时处理已存在 Skill 目录的策略。 */",
    ),
    (
        "        /**\n         * Overwrite existing directory (delete and recreate).\n         */",
        "        /** 覆盖已有目录（删除后重建）。 */",
    ),
    (
        "        /**\n         * Backup existing directory by renaming it with timestamp suffix.\n         */",
        "        /** 将已有目录重命名为带时间戳后缀的备份目录。 */",
    ),
    (
        "        /**\n         * Throw exception if directory already exists.\n         */",
        "        /** 目录已存在时抛出异常。 */",
    ),
    (
        "     * Get full SKILL.md markdown content from skill.\n     *\n     * @param skill the Skill object to convert\n     * @return SKILL.md markdown content\n     */",
        "     * 从 Skill 对象获取完整 SKILL.md Markdown 正文。\n     *\n     * @param skill 待转换的 Skill 对象\n     * @return SKILL.md Markdown 正文\n     */",
    ),
    (
        "     * Convert Skill object to a ZIP byte array containing all skill files.\n     *\n"
        "     * <p>The ZIP structure mirrors the upload format:\n"
        "     * {@code skillName/SKILL.md}, {@code skillName/type/resourceName}, etc.\n"
        "     * Binary resources (marked with metadata encoding=base64) are decoded back to raw bytes.</p>\n     *\n"
        "     * @param skill the Skill object to convert\n     * @return ZIP file as byte array\n     * @throws IOException if ZIP creation fails\n     * @throws IllegalArgumentException if skill is null or skill name is blank\n     */",
        "     * 将 Skill 对象转换为包含全部 Skill 文件的 ZIP 字节数组。\n     *\n"
        "     * <p>ZIP 目录结构与上传格式一致：\n"
        "     * {@code skillName/SKILL.md}、{@code skillName/type/resourceName} 等。\n"
        "     * 标记 metadata encoding=base64 的二进制资源会解码为原始字节。</p>\n     *\n"
        "     * @param skill 待转换的 Skill 对象\n     * @return ZIP 文件字节数组\n     * @throws IOException ZIP 创建失败时抛出\n     * @throws IllegalArgumentException skill 为 null 或名称为空时抛出\n     */",
    ),
    (
        "            // 1. SKILL.md",
        "            // 1. 写入 SKILL.md",
    ),
    (
        "            // 2. Resource files",
        "            // 2. 写入资源文件",
    ),
    (
        "     * Build ZIP entry path for a skill resource.\n     *\n"
        "     * @param skillName skill name (root directory)\n"
        "     * @param resource  skill resource\n"
        "     * @return ZIP entry path, e.g. \"skillName/type/resourceName\" or \"skillName/resourceName\"\n     */",
        "     * 构建 Skill 资源在 ZIP 中的条目路径。\n     *\n"
        "     * @param skillName Skill 名称（根目录）\n"
        "     * @param resource  Skill 资源\n"
        "     * @return ZIP 条目路径，如 \"skillName/type/resourceName\" 或 \"skillName/resourceName\"\n     */",
    ),
    (
        "     * Validate that a path does not contain path traversal sequences or absolute path indicators.\n     *\n"
        "     * @param path the path to validate\n     * @throws SecurityException if path contains unsafe sequences\n     */",
        "     * 校验路径不含目录穿越序列或绝对路径指示符。\n     *\n"
        "     * @param path 待校验路径\n     * @throws SecurityException 路径含不安全序列时抛出\n     */",
    ),
    (
        "     * Validate that a resolved path stays within the expected base directory.\n     *\n"
        "     * @param baseDir the base directory that must contain the target\n"
        "     * @param target  the resolved target path\n     * @throws SecurityException if target escapes baseDir\n     */",
        "     * 校验解析后的目标路径仍在指定基目录内。\n     *\n"
        "     * @param baseDir 必须包含目标的基目录\n"
        "     * @param target  解析后的目标路径\n     * @throws SecurityException 目标路径逃逸出基目录时抛出\n     */",
    ),
    (
        "     * Validate that byte array is a valid ZIP file by checking the magic number header.\n     *\n"
        "     * @param data the byte array to validate\n"
        "     * @throws IllegalArgumentException if data is null, too short, or does not have ZIP magic header\n     */",
        "     * 通过魔数头校验字节数组是否为合法 ZIP 文件。\n     *\n"
        "     * @param data 待校验字节数组\n"
        "     * @throws IllegalArgumentException 数据为 null、过短或缺少 ZIP 魔数头时抛出\n     */",
    ),
    (
        "     * Validate all ZIP entry paths for path traversal and absolute paths.\n     *\n"
        "     * <p>Scans entry names only without decompressing content, so it is lightweight\n"
        "     * and suitable for validating downloaded ZIP bytes on the client side.</p>\n     *\n"
        "     * @param data the ZIP byte array to validate\n     * @throws SecurityException if any entry contains path traversal or absolute path\n"
        "     * @throws IOException if ZIP cannot be read\n     */",
        "     * 校验 ZIP 内全部条目路径，防止目录穿越与绝对路径。\n     *\n"
        "     * <p>仅扫描条目名而不解压内容，开销小，适合客户端校验下载的 ZIP。</p>\n     *\n"
        "     * @param data 待校验 ZIP 字节数组\n"
        "     * @throws SecurityException 任一条目含目录穿越或绝对路径时抛出\n"
        "     * @throws IOException ZIP 无法读取时抛出\n     */",
    ),
    (
        "     * Resolve resource content to raw bytes.\n"
        "     * Base64-encoded binary resources (marked with metadata encoding=base64) are decoded;\n"
        "     * text resources are returned as UTF-8 bytes.\n     *\n     * @param resource the skill resource\n"
        "     * @return raw bytes of the resource content\n     */",
        "     * 将资源内容解析为原始字节。\n"
        "     * <p>metadata encoding=base64 的二进制资源会 Base64 解码；文本资源按 UTF-8 编码返回。</p>\n     *\n"
        "     * @param resource Skill 资源\n     * @return 资源内容的原始字节\n     */",
    ),
    (
        "     * Check if a resource is Base64-encoded binary content.\n     *\n"
        "     * @param resource the skill resource\n     * @return true if metadata contains encoding=base64\n     */",
        "     * 判断资源是否为 Base64 编码的二进制内容。\n     *\n"
        "     * @param resource Skill 资源\n     * @return metadata 含 encoding=base64 时返回 true\n     */",
    ),
    (
        "     * Sync Skill object to local directory.\n"
        "     * Creates the skill directory structure, SKILL.md file, and resource files.\n"
        "     * Uses OVERWRITE strategy by default.\n     *\n"
        "     * @param skill the Skill object to sync\n"
        "     * @param baseDir the base directory path where the skill directory will be created\n"
        "     * @throws IOException if file operations fail\n"
        "     * @throws IllegalArgumentException if skill is null or skill name is blank\n     */",
        "     * 将 Skill 对象同步到本地目录（默认 OVERWRITE 策略）。\n"
        "     * <p>创建 Skill 目录结构、SKILL.md 与资源文件。</p>\n     *\n"
        "     * @param skill 待同步的 Skill 对象\n"
        "     * @param baseDir Skill 目录所在的基路径\n"
        "     * @throws IOException 文件操作失败时抛出\n"
        "     * @throws IllegalArgumentException skill 为 null 或名称为空时抛出\n     */",
    ),
    (
        "     * Main config dataId for skill.\n     *\n"
        "     * @deprecated No longer used. Replaced by {@link #SKILL_INDEX_DATA_ID} for the manifest\n"
        "     *             and versioned resource files for content.\n     */",
        "     * Skill 主配置 dataId。\n     *\n"
        "     * @deprecated 已弃用。manifest 请使用 {@link #SKILL_INDEX_DATA_ID}，内容请使用版本化资源文件。\n     */",
    ),
    (
        "    /**\n     * Resource config dataId prefix.\n     */",
        "    /** 资源配置 dataId 前缀。 */",
    ),
    (
        "    /**\n     * Resource config dataId suffix.\n     */",
        "    /** 资源配置 dataId 后缀。 */",
    ),
    (
        "    /**\n     * Skill group prefix.\n     */",
        "    /** Skill 配置 group 前缀。 */",
    ),
    (
        "    /**\n     * Skill index config dataId for client-side config caching.\n"
        "     * Server writes a manifest config with this dataId at group {@code skill_{name}}\n"
        "     * containing the current online version and file list.\n     */",
        "    /**\n     * 客户端缓存用的 Skill 索引配置 dataId。\n"
        "     * <p>服务端在 group {@code skill_{name}} 下写入 manifest 配置，\n"
        "     * 包含当前在线版本与文件列表。</p>\n     */",
    ),
    (
        "     * Build the Nacos Config group for a skill (no version suffix).\n     *\n"
        "     * @param skillName name of skill\n     * @return config group string, e.g. \"skill_myskill\"\n     */",
        "     * 构建 Skill 的 Nacos Config group（无版本后缀）。\n     *\n"
        "     * @param skillName Skill 名称\n     * @return 配置 group 字符串，如 \"skill_myskill\"\n     */",
    ),
    (
        "     * Build the Nacos Config group for a specific skill version.\n     *\n"
        "     * @param skillName name of skill\n     * @param version   version string, e.g. \"v1\"\n"
        "     * @return config group string, e.g. \"skill_myskill__v1\"\n     */",
        "     * 构建指定 Skill 版本的 Nacos Config group。\n     *\n"
        "     * @param skillName Skill 名称\n     * @param version   版本字符串，如 \"v1\"\n"
        "     * @return 配置 group 字符串，如 \"skill_myskill__v1\"\n     */",
    ),
    (
        "     * Decode a Skill Nacos Config {@code group} (as stored) into logical skill name and optional version.\n     *\n"
        "     * @param group physical group, e.g. {@code skill_myagent} or {@code skill_name__v1}\n"
        "     * @return array of length 2: {@code [skillName, version]}; {@code version} is {@code null} for manifest group\n     */",
        "     * 将 Skill 的 Nacos Config {@code group} 解码为逻辑 Skill 名称与可选版本。\n     *\n"
        "     * @param group 物理 group，如 {@code skill_myagent} 或 {@code skill_name__v1}\n"
        "     * @return 长度为 2 的数组 {@code [skillName, version]}；manifest group 时 version 为 {@code null}\n     */",
    ),
    (
        "     * Sanitize a resource name for use in Nacos Config group names.\n     *\n"
        "     * @param name the raw resource name (e.g. skill name or agentspec name)\n"
        "     * @return value safe for use in Nacos config parameters\n"
        "     * @deprecated use {@link NacosAiConfigKeyCodec#encodeSegment(String)} for reversible encoding\n     */",
        "     * 将资源名清理为可用于 Nacos Config group 的安全值。\n     *\n"
        "     * @param name 原始资源名（如 skill 名或 agentspec 名）\n"
        "     * @return 可用于 Nacos 配置参数的安全值\n"
        "     * @deprecated 请使用 {@link NacosAiConfigKeyCodec#encodeSegment(String)} 进行可逆编码\n     */",
    ),
    (
        "     * Generate resource ID from resource type and name.\n"
        "     * Format: {type}_{resourcename}\n"
        "     * If resourcename ends with .xx, convert the last . to __\n"
        "     * Slashes in type are encoded as dots so that dataId (resource_{resourceId}.json) is valid in Nacos.\n     *\n"
        "     * @param type resource type (can be null or empty; may contain / for multi-level paths)\n"
        "     * @param resourceName resource name\n     * @return resource ID (safe for use in config dataId)\n     */",
        "     * 根据资源类型与名称生成资源 ID。\n"
        "     * <p>格式为 {type}_{resourcename}；若名称以 .xx 结尾，最后一个 . 转为 __。\n"
        "     * type 中的斜杠编码为点，以保证 dataId（resource_{resourceId}.json）在 Nacos 中合法。</p>\n     *\n"
        "     * @param type 资源类型（可为 null 或空；可含 / 表示多级路径）\n"
        "     * @param resourceName 资源名称\n     * @return 可用于 config dataId 的资源 ID\n     */",
    ),
    (
        "        // If resourcename ends with .xx, convert the last . to __",
        "        // 若资源名以 .xx 结尾，将最后一个 . 转为 __",
    ),
    (
        "            // Encode / as . so dataId has no slash (Nacos config key compatibility)",
        "            // 将 / 编码为 .，使 dataId 不含斜杠（兼容 Nacos 配置键）",
    ),
]

# --- remote ---

R["api/src/main/java/com/alibaba/nacos/api/ai/remote/AiRemoteConstants.java"] = [
    (
        "/**\n * Retain all ai module request type constants.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos AI 模块远程请求类型常量集合。\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    public static final String REGISTER_ENDPOINT = \"registerEndpoint\";",
        "    /** 注册端点请求类型。 */\n    public static final String REGISTER_ENDPOINT = \"registerEndpoint\";",
    ),
    (
        "    public static final String BATCH_REGISTER_ENDPOINT = \"batchRegisterEndpoint\";",
        "    /** 批量注册端点请求类型。 */\n    public static final String BATCH_REGISTER_ENDPOINT = \"batchRegisterEndpoint\";",
    ),
    (
        "    public static final String DE_REGISTER_ENDPOINT = \"deregisterEndpoint\";",
        "    /** 注销端点请求类型。 */\n    public static final String DE_REGISTER_ENDPOINT = \"deregisterEndpoint\";",
    ),
]

R["api/src/main/java/com/alibaba/nacos/api/ai/remote/request/AbstractAgentRequest.java"] = [
    (
        "/**\n * Nacos AI module agent request.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos AI 模块 Agent 远程请求基类，封装命名空间与 Agent 名称等公共字段。\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    private String namespaceId;",
        "    /** Nacos 命名空间 ID。 */\n    private String namespaceId;",
    ),
    (
        "    private String agentName;",
        "    /** Agent 名称。 */\n    private String agentName;",
    ),
]

R["api/src/main/java/com/alibaba/nacos/api/ai/remote/request/AbstractMcpRequest.java"] = [
    (
        "/**\n * Nacos AI module mcp request.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos AI 模块 MCP 远程请求抽象基类，封装命名空间与 MCP 标识等公共字段。\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    private String namespaceId;",
        "    /** Nacos 命名空间 ID。 */\n    private String namespaceId;",
    ),
    (
        "    private String mcpId;",
        "    /** MCP 服务 ID。 */\n    private String mcpId;",
    ),
    (
        "    private String mcpName;",
        "    /** MCP 服务名称。 */\n    private String mcpName;",
    ),
]

R["api/src/main/java/com/alibaba/nacos/api/ai/remote/request/AbstractPromptRequest.java"] = [
    (
        "/**\n * Nacos AI module prompt request.\n *\n * @author nacos\n */",
        "/**\n * Nacos AI 模块 Prompt 远程请求抽象基类，封装命名空间与 Prompt 键等公共字段。\n *\n * @author nacos\n */",
    ),
    (
        "    private String namespaceId;",
        "    /** Nacos 命名空间 ID。 */\n    private String namespaceId;",
    ),
    (
        "    private String promptKey;",
        "    /** Prompt 唯一标识键。 */\n    private String promptKey;",
    ),
]
