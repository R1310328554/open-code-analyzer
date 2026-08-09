"""Chinese JavaDoc replacements for Sentinel 1.8.10 wave30b command center classes."""

COMMAND_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "CommandHandler.java": [
        (
            "/**\n * Represent a handler that handles a {@link CommandRequest}.\n *\n * @author Eric Zhao\n */",
            "/**\n * 命令处理器接口：将 {@link CommandRequest} 转换为 {@link CommandResponse}。\n * 各具体命令通过 SPI 注册并由 {@link CommandHandlerProvider} 统一发现。\n *\n * @author Eric Zhao\n */",
        ),
        (
            "    /**\n     * Handle the given Courier command request.\n     *\n     * @param request the request to handle\n     * @return the response\n     */",
            "    /**\n     * 处理命令中心传入的请求并返回响应。\n     *\n     * @param request 待处理的命令请求\n     * @return 成功或失败的命令响应\n     */",
        ),
    ],
    "CommandHandlerInterceptor.java": [
        (
            "/**\n * Intercepts specified command, and can be extended using SPI.\n *\n * @author icodening\n * @since 1.8.4\n * @see com.alibaba.csp.sentinel.spi.SpiLoader\n * @see com.alibaba.csp.sentinel.spi.Spi\n */",
            "/**\n * 命令处理器拦截器：在指定命令执行前后插入自定义逻辑，可通过 SPI 扩展。\n * 多个拦截器按 SPI 排序后组成责任链，由 {@link InterceptingCommandHandler} 驱动。\n *\n * @author icodening\n * @since 1.8.4\n * @see com.alibaba.csp.sentinel.spi.SpiLoader\n * @see com.alibaba.csp.sentinel.spi.Spi\n */",
        ),
        (
            "    /**\n     * whether to intercept the specified command\n     *\n     * @param commandName command name, eg. getRules\n     * @return \"true\" means intercept, \"false\" means skip\n     */",
            "    /**\n     * 判断是否拦截指定命令。\n     *\n     * @param commandName 命令名称，例如 getRules\n     * @return true 表示参与拦截，false 表示跳过\n     */",
        ),
        (
            "    /**\n     * intercept the given command request, and return a command response\n     *\n     * @param request   commandRequest\n     * @param execution interceptor chain execution\n     * @return command response\n     */",
            "    /**\n     * 拦截命令请求：可调用 {@code execution.execute(request)} 继续责任链，或直接返回响应。\n     *\n     * @param request   命令请求\n     * @param execution 拦截器链执行器\n     * @return 命令响应\n     */",
        ),
    ],
    "CommandHandlerProvider.java": [
        (
            "/**\n * Provides and filters command handlers registered via SPI.\n *\n * @author Eric Zhao\n */",
            "/**\n * 命令处理器提供者：通过 SPI 加载 {@link CommandHandler} 实现，\n * 解析 {@link CommandMapping} 注解获取命令名，并按需包装拦截器链。\n *\n * @author Eric Zhao\n */",
        ),
        (
            "    /**\n     * Get all command handlers annotated with {@link CommandMapping} with command name.\n     *\n     * @return list of all named command handlers\n     */",
            "    /**\n     * 获取所有带 {@link CommandMapping} 注解的命令处理器，键为命令名。\n     * 若存在 {@link CommandHandlerInterceptor}，则自动包装为 {@link InterceptingCommandHandler}。\n     *\n     * @return 命令名到处理器的映射\n     */",
        ),
        (
            "            if (!commandHandlerInterceptors.isEmpty()) {",
            "            // 收集对该命令生效的拦截器并包装为责任链\n            if (!commandHandlerInterceptors.isEmpty()) {",
        ),
        (
            "    private String parseCommandName(CommandHandler handler) {",
            "    /** 从处理器类上的 {@link CommandMapping} 注解解析命令名。 */\n    private String parseCommandName(CommandHandler handler) {",
        ),
    ],
    "CommandRequest.java": [
        (
            "/**\n * Command request representation of command center.\n *\n * @author Eric Zhao\n */",
            "/**\n * 命令中心请求模型：封装 HTTP 查询参数、元数据与可选请求体。\n * 参数与元数据均使用字符串键值对，便于传输层透传。\n *\n * @author Eric Zhao\n */",
        ),
        (
            "    private final Map<String, String> metadata = new HashMap<String, String>();",
            "    /** 请求元数据（如客户端地址、协议信息等）。 */\n    private final Map<String, String> metadata = new HashMap<String, String>();",
        ),
        (
            "    private final Map<String, String> parameters = new HashMap<String, String>();",
            "    /** URL 查询参数或表单参数。 */\n    private final Map<String, String> parameters = new HashMap<String, String>();",
        ),
        (
            "    private byte[] body;",
            "    /** 可选请求体（如 POST 规则变更时的 JSON 载荷）。 */\n    private byte[] body;",
        ),
        (
            "    public String getParam(String key, String defaultValue) {",
            "    /** 获取参数，空白时返回默认值。 */\n    public String getParam(String key, String defaultValue) {",
        ),
        (
            "            throw new IllegalArgumentException(\"Parameter key cannot be empty\");",
            "            throw new IllegalArgumentException(\"参数键不能为空\");",
        ),
        (
            "            throw new IllegalArgumentException(\"Metadata key cannot be empty\");",
            "            throw new IllegalArgumentException(\"元数据键不能为空\");",
        ),
    ],
    "CommandRequestExecution.java": [
        (
            "/**\n * @author icodening\n * @since 1.8.4\n */",
            "/**\n * 命令请求执行器：拦截器链中用于将控制权传递给下一环或最终处理器。\n *\n * @author icodening\n * @since 1.8.4\n */",
        ),
        (
            "    /**\n     * execute the command request and return the command response.\n     *\n     * @param request command request\n     * @return command response\n     */",
            "    /**\n     * 继续执行拦截器链或底层 {@link CommandHandler}，返回最终响应。\n     *\n     * @param request 命令请求\n     * @return 命令响应\n     */",
        ),
    ],
    "CommandResponse.java": [
        (
            "/**\n * Command response representation of command center.\n *\n * @param <R> type of the result\n * @author Eric Zhao\n */",
            "/**\n * 命令中心响应模型：封装成功标志、结果对象与可选异常。\n * 工厂方法 {@link #ofSuccess} 与 {@link #ofFailure} 区分正常与失败路径。\n *\n * @param <R> 结果类型\n * @author Eric Zhao\n */",
        ),
        (
            "    /**\n     * Construct a successful response with given object.\n     *\n     * @param result result object\n     * @param <T>    type of the result\n     * @return constructed server response\n     */",
            "    /**\n     * 构造成功响应。\n     *\n     * @param result 结果对象\n     * @param <T>    结果类型\n     * @return 成功响应\n     */",
        ),
        (
            "    /**\n     * Construct a failed response with given exception.\n     *\n     * @param ex cause of the failure\n     * @return constructed server response\n     */",
            "    /**\n     * 构造失败响应（无附加结果）。\n     *\n     * @param ex 失败原因\n     * @return 失败响应\n     */",
        ),
        (
            "    /**\n     * Construct a failed response with given exception.\n     *\n     * @param ex     cause of the failure\n     * @param result additional message of the failure\n     * @return constructed server response\n     */",
            "    /**\n     * 构造失败响应，并附带额外结果（如错误提示文本）。\n     *\n     * @param ex     失败原因\n     * @param result 附加结果\n     * @return 失败响应\n     */",
        ),
    ],
    "CommandMapping.java": [
        (
            "/**\n * @author Eric Zhao\n */",
            "/**\n * 命令映射注解：标注 {@link CommandHandler} 实现类对应的命令名与简要描述。\n * 由 {@link CommandHandlerProvider} 在启动时扫描并注册到命令中心路由表。\n *\n * @author Eric Zhao\n */",
        ),
        (
            "    /**\n     * Get brief description of the command.\n     *\n     * @return brief description of the command\n     * @since 1.5.0\n     */",
            "    /**\n     * 命令简要说明，供 {@code /api} 接口列出可用命令时使用。\n     *\n     * @return 命令描述\n     * @since 1.5.0\n     */",
        ),
    ],
    "ApiCommandHandler.java": [
        (
            "/**\n * <p>\n * List all available command handlers by request: </br>\n * {@code curl http://ip:commandPort/api}\n * </p>\n *\n * @author houyi\n * @since 1.5.0\n */",
            "/**\n * <p>\n * 列出所有已注册命令处理器，返回 JSON 数组（url + desc）。\n * 示例：{@code curl http://ip:commandPort/api}\n * </p>\n *\n * @author houyi\n * @since 1.5.0\n */",
        ),
        (
            '@CommandMapping(name = "api", desc = "get all available command handlers")',
            '@CommandMapping(name = "api", desc = "获取全部可用命令处理器列表")',
        ),
        (
            "            if (commandMapping == null) {\n                continue;\n            }",
            "            // 跳过未标注 CommandMapping 的处理器\n            if (commandMapping == null) {\n                continue;\n            }",
        ),
    ],
    "BasicInfoCommandHandler.java": [
        (
            "/**\n * The basic info command returns the runtime properties.\n *\n * @author Eric Zhao\n */",
            "/**\n * 基础信息命令：返回 Sentinel 运行时配置（主机名、端口、应用名等）。\n *\n * @author Eric Zhao\n */",
        ),
        (
            '@CommandMapping(name = "basicInfo", desc = "get sentinel config info")',
            '@CommandMapping(name = "basicInfo", desc = "获取 Sentinel 运行时配置信息")',
        ),
    ],
    "FetchActiveRuleCommandHandler.java": [
        (
            "/**\n * @author jialiang.linjl\n */",
            "/**\n * 按类型查询当前生效的规则列表（流控/降级/授权/系统），返回 JSON。\n * 请求参数 {@code type} 取值：flow、degrade、authority、system。\n *\n * @author jialiang.linjl\n */",
        ),
        (
            '@CommandMapping(name = "getRules", desc = "get all active rules by type, request param: type={ruleType}")',
            '@CommandMapping(name = "getRules", desc = "按类型获取生效规则，参数 type={ruleType}")',
        ),
        (
            '        if ("flow".equalsIgnoreCase(type)) {',
            '        // 流控规则\n        if ("flow".equalsIgnoreCase(type)) {',
        ),
        (
            '        } else if ("degrade".equalsIgnoreCase(type)) {',
            '        // 降级规则\n        } else if ("degrade".equalsIgnoreCase(type)) {',
        ),
        (
            '        } else if ("authority".equalsIgnoreCase(type)) {',
            '        // 授权规则\n        } else if ("authority".equalsIgnoreCase(type)) {',
        ),
        (
            '        } else if ("system".equalsIgnoreCase(type)) {',
            '        // 系统规则\n        } else if ("system".equalsIgnoreCase(type)) {',
        ),
        (
            '            return CommandResponse.ofFailure(new IllegalArgumentException("invalid type"));',
            '            return CommandResponse.ofFailure(new IllegalArgumentException("无效的规则类型 type"));',
        ),
    ],
    "FetchClusterNodeByIdCommandHandler.java": [
        (
            "/**\n * @author qinan.qn\n */",
            "/**\n * 按资源 ID 查询单个 {@link ClusterNode} 的 VO 快照，返回 JSON。\n * 请求参数 {@code id} 为资源名称；未找到时返回空对象 {@code {}}。\n *\n * @author qinan.qn\n */",
        ),
        (
            '@CommandMapping(name = "clusterNodeById", desc = "get clusterNode VO by id, request param: id={resourceName}")',
            '@CommandMapping(name = "clusterNodeById", desc = "按 id 获取 ClusterNode VO，参数 id={resourceName}")',
        ),
        (
            '            return CommandResponse.ofFailure(new IllegalArgumentException("Invalid parameter: empty clusterNode name"));',
            '            return CommandResponse.ofFailure(new IllegalArgumentException("无效参数：clusterNode 名称为空"));',
        ),
    ],
    "FetchClusterNodeHumanCommandHandler.java": [
        (
            "/**\n * @author qinan.qn\n */",
            "/**\n * 以人类可读表格形式输出 ClusterNode 指标，支持按资源名模糊匹配。\n * 最多展示 30 条匹配记录，列含线程数、QPS、RT 等运行时统计。\n *\n * @author qinan.qn\n */",
        ),
        (
            '@CommandMapping(name = "cnode", desc = "get clusterNode metrics by id, request param: id={resourceName}")',
            '@CommandMapping(name = "cnode", desc = "按 id 获取 ClusterNode 指标表格，参数 id={resourceName}")',
        ),
        (
            "        if (StringUtil.isEmpty(name)) {",
            "        // id 为空则拒绝请求\n        if (StringUtil.isEmpty(name)) {",
        ),
        (
            "                if (++i == 30) {\n                    break;\n                }",
            "                // 预扫描阶段最多统计 30 条以确定列宽\n                if (++i == 30) {\n                    break;\n                }",
        ),
        (
            "                if (++i == 30) {\n                    break;\n                }\n            }\n        }\n\n        return CommandResponse.ofSuccess(sb.toString());",
            "                // 输出阶段同样限制 30 条\n                if (++i == 30) {\n                    break;\n                }\n            }\n        }\n\n        return CommandResponse.ofSuccess(sb.toString());",
        ),
    ],
    "FetchJsonTreeCommandHandler.java": [
        (
            "/**\n * @author leyou\n */",
            "/**\n * 以 JSON 数组返回从根节点开始的调用树 {@link NodeVo} 列表（前序遍历）。\n * 用于 Dashboard 或客户端可视化资源层级与实时指标。\n *\n * @author leyou\n */",
        ),
        (
            '@CommandMapping(name = "jsonTree", desc = "get tree node VO start from root node")',
            '@CommandMapping(name = "jsonTree", desc = "从根节点获取调用树 NodeVo JSON")',
        ),
        (
            "    /**\n     * Preorder traversal.\n     */",
            "    /**\n     * 前序遍历调用树，将每个 {@link DefaultNode} 转为 {@link NodeVo} 并递归子节点。\n     */",
        ),
    ],
    "FetchOriginCommandHandler.java": [
        (
            "/**\n * @author qinan.qn\n */",
            "/**\n * 查询指定 ClusterNode 下各调用来源（origin）的统计指标，以表格文本返回。\n * 先精确匹配资源名，否则尝试子串匹配；最多输出 30 条 origin 记录。\n *\n * @author qinan.qn\n */",
        ),
        (
            '@CommandMapping(name = "origin", desc = "get origin clusterNode by id, request param: id={resourceName}")',
            '@CommandMapping(name = "origin", desc = "按 id 获取 origin 统计表格，参数 id={resourceName}")',
        ),
        (
            "        if (!exactly) {",
            "        // 精确匹配失败时退化为子串匹配\n        if (!exactly) {",
        ),
        (
            '            return CommandResponse.ofSuccess("Not find cNode with id " + name);',
            '            return CommandResponse.ofSuccess("未找到 id 为 " + name + " 的 ClusterNode");',
        ),
        (
            "            if (++i == 30) {\n                break;\n            }\n\n        }\n\n        return CommandResponse.ofSuccess(sb.toString());",
            "            // 最多展示 30 条 origin\n            if (++i == 30) {\n                break;\n            }\n\n        }\n\n        return CommandResponse.ofSuccess(sb.toString());",
        ),
    ],
    "FetchSimpleClusterNodeCommandHandler.java": [
        (
            "/**\n * @author jialiang.linjl\n */",
            "/**\n * 返回全部 ClusterNode 的 {@link NodeVo} 列表（JSON）。\n * 参数 {@code type=notZero} 时可过滤 totalRequest &lt;= 0 的节点。\n *\n * @author jialiang.linjl\n */",
        ),
        (
            '@CommandMapping(name = "clusterNode", desc = "get all clusterNode VO, use type=notZero to ignore those nodes with totalRequest <=0")',
            '@CommandMapping(name = "clusterNode", desc = "获取全部 ClusterNode VO，type=notZero 可忽略无流量节点")',
        ),
        (
            "        /*\n         * type==notZero means nodes whose totalRequest <= 0 will be ignored.\n         */",
            "        /* type=notZero 时忽略 totalRequest <= 0 的节点 */",
        ),
    ],
}
