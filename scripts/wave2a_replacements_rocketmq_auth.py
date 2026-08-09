"""RocketMQ 5.5.0 wave2a auth/authorization package replacements [0:15]."""

R: dict[str, list[tuple[str, str]]] = {}

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/enums/PolicyType.java"] = [
    (
        "public enum PolicyType {",
        "/**\n * ACL 策略类型：区分用户自定义策略与系统默认策略。\n */\npublic enum PolicyType {",
    ),
    (
        "    CUSTOM((byte) 1, \"Custom\"),",
        "    /** 用户自定义策略。 */\n    CUSTOM((byte) 1, \"Custom\"),",
    ),
    (
        "    DEFAULT((byte) 2, \"Default\");",
        "    /** 系统默认策略。 */\n    DEFAULT((byte) 2, \"Default\");",
    ),
    (
        "    public static PolicyType getByName(String name) {",
        "    /** 按名称（忽略大小写）查找策略类型；未匹配时返回 null。 */\n    public static PolicyType getByName(String name) {",
    ),
    (
        "    public byte getCode() {",
        "    /** 返回持久化用的数值编码。 */\n    public byte getCode() {",
    ),
    (
        "    public String getName() {",
        "    /** 返回可读名称。 */\n    public String getName() {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/exception/AuthorizationException.java"] = [
    (
        "public class AuthorizationException extends RuntimeException {",
        "/**\n * 授权异常：ACL 校验失败或元数据操作出错时抛出。\n */\npublic class AuthorizationException extends RuntimeException {",
    ),
    (
        "    public AuthorizationException(String message) {",
        "    /** 使用固定消息构造异常。 */\n    public AuthorizationException(String message) {",
    ),
    (
        "    public AuthorizationException(String message, Throwable cause) {",
        "    /** 使用消息与根因构造异常。 */\n    public AuthorizationException(String message, Throwable cause) {",
    ),
    (
        "    public AuthorizationException(String messagePattern, Object... argArray) {",
        "    /** 使用 SLF4J 风格占位符模板构造异常消息。 */\n    public AuthorizationException(String messagePattern, Object... argArray) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/factory/AuthorizationFactory.java"] = [
    (
        "public class AuthorizationFactory {",
        "/**\n * 授权组件工厂：按 {@link AuthConfig} 缓存并装配 Provider、元数据提供者、\n * {@link AuthorizationEvaluator} 与 {@link AuthorizationStrategy} 实例。\n */\npublic class AuthorizationFactory {",
    ),
    (
        "    public static AuthorizationProvider<AuthorizationContext> getProvider(AuthConfig config) {",
        "    /** 获取或创建 {@link AuthorizationProvider}；config 为 null 时返回 null。 */\n    public static AuthorizationProvider<AuthorizationContext> getProvider(AuthConfig config) {",
    ),
    (
        "    public static AuthorizationMetadataProvider getMetadataProvider(AuthConfig config) {",
        "    /** 获取授权元数据提供者（无外部元数据服务）。 */\n    public static AuthorizationMetadataProvider getMetadataProvider(AuthConfig config) {",
    ),
    (
        "    public static AuthorizationMetadataManager getMetadataManager(AuthConfig config) {",
        "    /** 创建 {@link AuthorizationMetadataManagerImpl} 管理 ACL 生命周期。 */\n    public static AuthorizationMetadataManager getMetadataManager(AuthConfig config) {",
    ),
    (
        "    public static AuthorizationMetadataProvider getMetadataProvider(AuthConfig config, Supplier<?> metadataService) {",
        "    /** 获取授权元数据提供者并调用 {@link AuthorizationMetadataProvider#initialize}。 */\n    public static AuthorizationMetadataProvider getMetadataProvider(AuthConfig config, Supplier<?> metadataService) {",
    ),
    (
        "    public static AuthorizationEvaluator getEvaluator(AuthConfig config) {",
        "    /** 获取或创建 {@link AuthorizationEvaluator}。 */\n    public static AuthorizationEvaluator getEvaluator(AuthConfig config) {",
    ),
    (
        "    public static AuthorizationEvaluator getEvaluator(AuthConfig config, Supplier<?> metadataService) {",
        "    /** 获取带元数据服务的 {@link AuthorizationEvaluator}。 */\n    public static AuthorizationEvaluator getEvaluator(AuthConfig config, Supplier<?> metadataService) {",
    ),
    (
        "    public static AuthorizationStrategy getStrategy(AuthConfig config, Supplier<?> metadataService) {",
        "    /** 反射实例化 {@link AuthorizationStrategy}，默认 {@link StatelessAuthorizationStrategy}。 */\n    public static AuthorizationStrategy getStrategy(AuthConfig config, Supplier<?> metadataService) {",
    ),
    (
        "    public static List<AuthorizationContext> newContexts(AuthConfig config, Metadata metadata,\n        GeneratedMessageV3 message) {",
        "    /** 从 gRPC 请求构建授权上下文列表。 */\n    public static List<AuthorizationContext> newContexts(AuthConfig config, Metadata metadata,\n        GeneratedMessageV3 message) {",
    ),
    (
        "    public static List<AuthorizationContext> newContexts(AuthConfig config, ChannelHandlerContext context,\n        RemotingCommand command) {",
        "    /** 从 Remoting 请求构建授权上下文列表。 */\n    public static List<AuthorizationContext> newContexts(AuthConfig config, ChannelHandlerContext context,\n        RemotingCommand command) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/manager/AuthorizationMetadataManager.java"] = [
    (
        "public interface AuthorizationMetadataManager {",
        "/**\n * 授权元数据管理器：对外暴露 ACL 的 CRUD 与查询接口，供管理 API 调用。\n */\npublic interface AuthorizationMetadataManager {",
    ),
    (
        "    void shutdown();",
        "    /** 关闭底层认证与授权元数据提供者。 */\n    void shutdown();",
    ),
    (
        "    CompletableFuture<Void> createAcl(Acl acl);",
        "    /** 创建 ACL；主体已存在时合并策略。 */\n    CompletableFuture<Void> createAcl(Acl acl);",
    ),
    (
        "    CompletableFuture<Void> updateAcl(Acl acl);",
        "    /** 更新 ACL；不存在时等同创建。 */\n    CompletableFuture<Void> updateAcl(Acl acl);",
    ),
    (
        "    CompletableFuture<Void> deleteAcl(Subject subject);",
        "    /** 删除主体的全部 ACL。 */\n    CompletableFuture<Void> deleteAcl(Subject subject);",
    ),
    (
        "    CompletableFuture<Void> deleteAcl(Subject subject, PolicyType policyType, Resource resource);",
        "    /** 删除指定策略类型下某资源的策略条目；无剩余条目时删除整个 ACL。 */\n    CompletableFuture<Void> deleteAcl(Subject subject, PolicyType policyType, Resource resource);",
    ),
    (
        "    CompletableFuture<Acl> getAcl(Subject subject);",
        "    /** 按主体查询 ACL。 */\n    CompletableFuture<Acl> getAcl(Subject subject);",
    ),
    (
        "    CompletableFuture<List<Acl>> listAcl(String subjectFilter, String resourceFilter);",
        "    /** 列出 ACL；支持按主体键与资源键子串过滤。 */\n    CompletableFuture<List<Acl>> listAcl(String subjectFilter, String resourceFilter);",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/manager/AuthorizationMetadataManagerImpl.java"] = [
    (
        "public class AuthorizationMetadataManagerImpl implements AuthorizationMetadataManager {",
        "/**\n * {@link AuthorizationMetadataManager} 默认实现：校验 ACL 结构、\n * 确认主体存在后委托 {@link AuthorizationMetadataProvider} 持久化。\n */\npublic class AuthorizationMetadataManagerImpl implements AuthorizationMetadataManager {",
    ),
    (
        "    public AuthorizationMetadataManagerImpl(AuthConfig authConfig) {",
        "    /** 从 {@link AuthorizationFactory} 获取认证与授权元数据提供者。 */\n    public AuthorizationMetadataManagerImpl(AuthConfig authConfig) {",
    ),
    (
        "    @Override\n    public void shutdown() {",
        "    /** 依次关闭认证与授权元数据提供者。 */\n    @Override\n    public void shutdown() {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> createAcl(Acl acl) {",
        "    /** 校验并初始化 ACL，存在则合并策略否则新建。 */\n    @Override\n    public CompletableFuture<Void> createAcl(Acl acl) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> updateAcl(Acl acl) {",
        "    /** 校验后更新 ACL；不存在时创建。 */\n    @Override\n    public CompletableFuture<Void> updateAcl(Acl acl) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> deleteAcl(Subject subject) {",
        "    /** 删除主体的全部 ACL（委托带 null 过滤的重载）。 */\n    @Override\n    public CompletableFuture<Void> deleteAcl(Subject subject) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> deleteAcl(Subject subject, PolicyType policyType, Resource resource) {",
        "    /** 删除指定资源条目；策略为空时删除整个 ACL。 */\n    @Override\n    public CompletableFuture<Void> deleteAcl(Subject subject, PolicyType policyType, Resource resource) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Acl> getAcl(Subject subject) {",
        "    /** 校验主体存在后查询 ACL。 */\n    @Override\n    public CompletableFuture<Acl> getAcl(Subject subject) {",
    ),
    (
        "    @Override\n    public CompletableFuture<List<Acl>> listAcl(String subjectFilter, String resourceFilter) {",
        "    /** 委托元数据提供者列出 ACL。 */\n    @Override\n    public CompletableFuture<List<Acl>> listAcl(String subjectFilter, String resourceFilter) {",
    ),
    (
        "    private static void initAcl(Acl acl) {",
        "    /** 为未指定类型的策略设置 {@link PolicyType#CUSTOM}。 */\n    private static void initAcl(Acl acl) {",
    ),
    (
        "    private void validate(Acl acl) {",
        "    /** 校验 ACL 主体类型与策略列表非空。 */\n    private void validate(Acl acl) {",
    ),
    (
        "    private void validate(Policy policy) {",
        "    /** 校验策略条目列表非空。 */\n    private void validate(Policy policy) {",
    ),
    (
        "    private void validate(PolicyEntry entry) {",
        "    /** 校验资源、动作、来源 IP 与决策字段合法。 */\n    private void validate(PolicyEntry entry) {",
    ),
    (
        "    private <T> CompletableFuture<T> handleException(Exception e) {",
        "    /** 将异常包装为 exceptionally 完成的 Future。 */\n    private <T> CompletableFuture<T> handleException(Exception e) {",
    ),
    (
        "    private AuthenticationMetadataProvider getAuthenticationMetadataProvider() {",
        "    /** 返回已配置的认证元数据提供者。 */\n    private AuthenticationMetadataProvider getAuthenticationMetadataProvider() {",
    ),
    (
        "    private AuthorizationMetadataProvider getAuthorizationMetadataProvider() {",
        "    /** 返回已配置的授权元数据提供者。 */\n    private AuthorizationMetadataProvider getAuthorizationMetadataProvider() {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/model/Acl.java"] = [
    (
        "public class Acl {",
        "/**\n * 访问控制列表：绑定 {@link Subject} 与其 {@link Policy} 集合。\n */\npublic class Acl {",
    ),
    (
        "    public static Acl of(Subject subject, Policy policy) {",
        "    /** 以单条策略创建 ACL。 */\n    public static Acl of(Subject subject, Policy policy) {",
    ),
    (
        "    public static Acl of(Subject subject, List<Policy> policies) {",
        "    /** 以策略列表创建 ACL。 */\n    public static Acl of(Subject subject, List<Policy> policies) {",
    ),
    (
        "    public static Acl of(Subject subject, List<Resource> resources, List<Action> actions, Environment environment,\n        Decision decision) {",
        "    /** 从资源、动作、环境与决策快速构建单策略 ACL。 */\n    public static Acl of(Subject subject, List<Resource> resources, List<Action> actions, Environment environment,\n        Decision decision) {",
    ),
    (
        "    public void updatePolicy(Policy policy) {",
        "    /** 合并单条策略到现有 ACL。 */\n    public void updatePolicy(Policy policy) {",
    ),
    (
        "    public void updatePolicy(List<Policy> policies) {",
        "    /** 按策略类型合并多条策略；同类型则更新条目。 */\n    public void updatePolicy(List<Policy> policies) {",
    ),
    (
        "    public void deletePolicy(PolicyType policyType, Resource resource) {",
        "    /** 删除指定类型下某资源的策略条目；策略为空时移除该策略。 */\n    public void deletePolicy(PolicyType policyType, Resource resource) {",
    ),
    (
        "    public Policy getPolicy(PolicyType policyType) {",
        "    /** 按类型查找策略；不存在时返回 null。 */\n    public Policy getPolicy(PolicyType policyType) {",
    ),
    (
        "    public Subject getSubject() {",
        "    /** 返回 ACL 所属主体。 */\n    public Subject getSubject() {",
    ),
    (
        "    public void setSubject(Subject subject) {",
        "    /** 设置 ACL 所属主体。 */\n    public void setSubject(Subject subject) {",
    ),
    (
        "    public List<Policy> getPolicies() {",
        "    /** 返回策略列表。 */\n    public List<Policy> getPolicies() {",
    ),
    (
        "    public void setPolicies(List<Policy> policies) {",
        "    /** 设置策略列表。 */\n    public void setPolicies(List<Policy> policies) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/model/Environment.java"] = [
    (
        "public class Environment {",
        "/**\n * 授权环境约束：当前用于限制请求来源 IP/CIDR。\n */\npublic class Environment {",
    ),
    (
        "    public static Environment of(String sourceIp) {",
        "    /** 以单个来源 IP 创建环境；空字符串时返回 null。 */\n    public static Environment of(String sourceIp) {",
    ),
    (
        "    public static Environment of(List<String> sourceIps) {",
        "    /** 以来源 IP 列表创建环境；列表为空时返回 null。 */\n    public static Environment of(List<String> sourceIps) {",
    ),
    (
        "    public boolean isMatch(Environment environment) {",
        "    /** 判断请求环境是否匹配本策略环境；未配置来源 IP 时视为匹配。 */\n    public boolean isMatch(Environment environment) {",
    ),
    (
        "    public List<String> getSourceIps() {",
        "    /** 返回允许的来源 IP/CIDR 列表。 */\n    public List<String> getSourceIps() {",
    ),
    (
        "    public void setSourceIps(List<String> sourceIps) {",
        "    /** 设置允许的来源 IP/CIDR 列表。 */\n    public void setSourceIps(List<String> sourceIps) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/model/Policy.java"] = [
    (
        "public class Policy {",
        "/**\n * ACL 策略：按 {@link PolicyType} 分组的一组 {@link PolicyEntry}。\n */\npublic class Policy {",
    ),
    (
        "    public static Policy of(List<Resource> resources, List<Action> actions, Environment environment,\n        Decision decision) {",
        "    /** 以 {@link PolicyType#CUSTOM} 从资源与动作列表构建策略。 */\n    public static Policy of(List<Resource> resources, List<Action> actions, Environment environment,\n        Decision decision) {",
    ),
    (
        "    public static Policy of(PolicyType policyType, List<Resource> resources, List<Action> actions,\n        Environment environment,\n        Decision decision) {",
        "    /** 指定策略类型，为每个资源生成一条 {@link PolicyEntry}。 */\n    public static Policy of(PolicyType policyType, List<Resource> resources, List<Action> actions,\n        Environment environment,\n        Decision decision) {",
    ),
    (
        "    public static Policy of(PolicyType type, List<PolicyEntry> entries) {",
        "    /** 直接以条目列表构建策略。 */\n    public static Policy of(PolicyType type, List<PolicyEntry> entries) {",
    ),
    (
        "    public void updateEntry(List<PolicyEntry> newEntries) {",
        "    /** 合并新条目；同资源则更新动作、环境与决策。 */\n    public void updateEntry(List<PolicyEntry> newEntries) {",
    ),
    (
        "    public void deleteEntry(Resource resources) {",
        "    /** 删除指定资源对应的策略条目。 */\n    public void deleteEntry(Resource resources) {",
    ),
    (
        "    public PolicyType getPolicyType() {",
        "    /** 返回策略类型。 */\n    public PolicyType getPolicyType() {",
    ),
    (
        "    public void setPolicyType(PolicyType policyType) {",
        "    /** 设置策略类型。 */\n    public void setPolicyType(PolicyType policyType) {",
    ),
    (
        "    public List<PolicyEntry> getEntries() {",
        "    /** 返回策略条目列表。 */\n    public List<PolicyEntry> getEntries() {",
    ),
    (
        "    public void setEntries(List<PolicyEntry> entries) {",
        "    /** 设置策略条目列表。 */\n    public void setEntries(List<PolicyEntry> entries) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/model/PolicyEntry.java"] = [
    (
        "public class PolicyEntry {",
        "/**\n * 单条策略条目：描述对某 {@link Resource} 在特定 {@link Environment} 下\n * 执行 {@link Action} 的 {@link Decision}。\n */\npublic class PolicyEntry {",
    ),
    (
        "    public static PolicyEntry of(Resource resource, List<Action> actions, Environment environment, Decision decision) {",
        "    /** 构建包含资源、动作、环境与决策的策略条目。 */\n    public static PolicyEntry of(Resource resource, List<Action> actions, Environment environment, Decision decision) {",
    ),
    (
        "    public void updateEntry(List<Action> actions, Environment environment,\n        Decision decision) {",
        "    /** 更新动作、环境与决策字段。 */\n    public void updateEntry(List<Action> actions, Environment environment,\n        Decision decision) {",
    ),
    (
        "    public boolean isMatchResource(Resource resource) {",
        "    /** 判断请求资源是否匹配本条目资源模式。 */\n    public boolean isMatchResource(Resource resource) {",
    ),
    (
        "    public boolean isMatchAction(List<Action> actions) {",
        "    /** 判断请求动作是否被本条目允许；请求含 {@link Action#ANY} 时视为匹配。 */\n    public boolean isMatchAction(List<Action> actions) {",
    ),
    (
        "    public boolean isMatchEnvironment(Environment environment) {",
        "    /** 判断请求环境是否满足本条目约束；未配置环境时视为匹配。 */\n    public boolean isMatchEnvironment(Environment environment) {",
    ),
    (
        "    public String toResourceStr() {",
        "    /** 返回资源键字符串；资源为 null 时返回 null。 */\n    public String toResourceStr() {",
    ),
    (
        "    public List<String> toActionsStr() {",
        "    /** 将动作列表转为名称字符串列表。 */\n    public List<String> toActionsStr() {",
    ),
    (
        "    public Resource getResource() {",
        "    /** 返回策略条目绑定的资源。 */\n    public Resource getResource() {",
    ),
    (
        "    public void setResource(Resource resource) {",
        "    /** 设置策略条目绑定的资源。 */\n    public void setResource(Resource resource) {",
    ),
    (
        "    public List<Action> getActions() {",
        "    /** 返回允许的动作列表。 */\n    public List<Action> getActions() {",
    ),
    (
        "    public void setActions(List<Action> actions) {",
        "    /** 设置允许的动作列表。 */\n    public void setActions(List<Action> actions) {",
    ),
    (
        "    public Environment getEnvironment() {",
        "    /** 返回环境约束。 */\n    public Environment getEnvironment() {",
    ),
    (
        "    public void setEnvironment(Environment environment) {",
        "    /** 设置环境约束。 */\n    public void setEnvironment(Environment environment) {",
    ),
    (
        "    public Decision getDecision() {",
        "    /** 返回授权决策（允许或拒绝）。 */\n    public Decision getDecision() {",
    ),
    (
        "    public void setDecision(Decision decision) {",
        "    /** 设置授权决策。 */\n    public void setDecision(Decision decision) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/model/RequestContext.java"] = [
    (
        "public class RequestContext {",
        "/**\n * 授权请求上下文：携带主体、资源、动作与来源 IP，供策略匹配使用。\n */\npublic class RequestContext {",
    ),
    (
        "    public Subject getSubject() {",
        "    /** 返回请求主体。 */\n    public Subject getSubject() {",
    ),
    (
        "    public void setSubject(Subject subject) {",
        "    /** 设置请求主体。 */\n    public void setSubject(Subject subject) {",
    ),
    (
        "    public Resource getResource() {",
        "    /** 返回请求目标资源。 */\n    public Resource getResource() {",
    ),
    (
        "    public void setResource(Resource resource) {",
        "    /** 设置请求目标资源。 */\n    public void setResource(Resource resource) {",
    ),
    (
        "    public Action getAction() {",
        "    /** 返回请求动作。 */\n    public Action getAction() {",
    ),
    (
        "    public void setAction(Action action) {",
        "    /** 设置请求动作。 */\n    public void setAction(Action action) {",
    ),
    (
        "    public String getSourceIp() {",
        "    /** 返回客户端来源 IP。 */\n    public String getSourceIp() {",
    ),
    (
        "    public void setSourceIp(String sourceIp) {",
        "    /** 设置客户端来源 IP。 */\n    public void setSourceIp(String sourceIp) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/model/Resource.java"] = [
    (
        "public class Resource {",
        "/**\n * 授权资源：由 {@link ResourceType}、名称与 {@link ResourcePattern} 组成，\n * 支持字面量、前缀与通配匹配。\n */\npublic class Resource {",
    ),
    (
        "    public static Resource ofCluster(String clusterName) {",
        "    /** 创建集群字面量资源。 */\n    public static Resource ofCluster(String clusterName) {",
    ),
    (
        "    public static Resource ofTopic(String topicName) {",
        "    /** 创建 Topic 字面量资源。 */\n    public static Resource ofTopic(String topicName) {",
    ),
    (
        "    public static Resource ofGroup(String groupName) {",
        "    /** 创建消费组字面量资源；自动剥离重试/DLQ 前缀。 */\n    public static Resource ofGroup(String groupName) {",
    ),
    (
        "    public static Resource of(ResourceType resourceType, String resourceName, ResourcePattern resourcePattern) {",
        "    /** 按类型、名称与匹配模式构建资源。 */\n    public static Resource of(ResourceType resourceType, String resourceName, ResourcePattern resourcePattern) {",
    ),
    (
        "    public static List<Resource> of(List<String> resourceKeys) {",
        "    /** 批量解析资源键字符串为 {@link Resource} 列表。 */\n    public static List<Resource> of(List<String> resourceKeys) {",
    ),
    (
        "    public static Resource of(String resourceKey) {",
        "    /** 解析 {@code type:name} 格式资源键，支持 * 与前缀通配。 */\n    public static Resource of(String resourceKey) {",
    ),
    (
        "    @JSONField(serialize = false)\n    public String getResourceKey() {",
        "    /** 序列化为 {@code type:name} 资源键；不参与 JSON 序列化。 */\n    @JSONField(serialize = false)\n    public String getResourceKey() {",
    ),
    (
        "    public boolean isMatch(Resource resource) {",
        "    /** 判断请求资源是否被本资源模式覆盖。 */\n    public boolean isMatch(Resource resource) {",
    ),
    (
        "    public ResourceType getResourceType() {",
        "    /** 返回资源类型。 */\n    public ResourceType getResourceType() {",
    ),
    (
        "    public void setResourceType(ResourceType resourceType) {",
        "    /** 设置资源类型。 */\n    public void setResourceType(ResourceType resourceType) {",
    ),
    (
        "    public String getResourceName() {",
        "    /** 返回资源名称。 */\n    public String getResourceName() {",
    ),
    (
        "    public void setResourceName(String resourceName) {",
        "    /** 设置资源名称。 */\n    public void setResourceName(String resourceName) {",
    ),
    (
        "    public ResourcePattern getResourcePattern() {",
        "    /** 返回资源匹配模式。 */\n    public ResourcePattern getResourcePattern() {",
    ),
    (
        "    public void setResourcePattern(ResourcePattern resourcePattern) {",
        "    /** 设置资源匹配模式。 */\n    public void setResourcePattern(ResourcePattern resourcePattern) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/provider/AuthorizationMetadataProvider.java"] = [
    (
        "public interface AuthorizationMetadataProvider {",
        "/**\n * 授权元数据提供者：负责 ACL 的持久化 CRUD 与查询。\n */\npublic interface AuthorizationMetadataProvider {",
    ),
    (
        "    void initialize(AuthConfig authConfig, Supplier<?> metadataService);",
        "    /** 初始化存储后端，绑定 {@link AuthConfig} 与可选元数据服务。 */\n    void initialize(AuthConfig authConfig, Supplier<?> metadataService);",
    ),
    (
        "    void shutdown();",
        "    /** 关闭 RocksDB 与缓存线程等资源。 */\n    void shutdown();",
    ),
    (
        "    CompletableFuture<Void> createAcl(Acl acl);",
        "    /** 持久化新建 ACL。 */\n    CompletableFuture<Void> createAcl(Acl acl);",
    ),
    (
        "    CompletableFuture<Void> deleteAcl(Subject subject);",
        "    /** 删除主体的 ACL 记录。 */\n    CompletableFuture<Void> deleteAcl(Subject subject);",
    ),
    (
        "    CompletableFuture<Void> updateAcl(Acl acl);",
        "    /** 更新已有 ACL 记录。 */\n    CompletableFuture<Void> updateAcl(Acl acl);",
    ),
    (
        "    CompletableFuture<Acl> getAcl(Subject subject);",
        "    /** 按主体查询 ACL。 */\n    CompletableFuture<Acl> getAcl(Subject subject);",
    ),
    (
        "    CompletableFuture<List<Acl>> listAcl(String subjectFilter, String resourceFilter);",
        "    /** 列出 ACL，支持主体与资源子串过滤。 */\n    CompletableFuture<List<Acl>> listAcl(String subjectFilter, String resourceFilter);",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/provider/AuthorizationProvider.java"] = [
    (
        "public interface AuthorizationProvider<AuthorizationContext> {",
        "/**\n * 授权提供者 SPI：封装上下文构建与授权责任链执行。\n *\n * @param <AuthorizationContext> 授权上下文类型\n */\npublic interface AuthorizationProvider<AuthorizationContext> {",
    ),
    (
        "    void initialize(AuthConfig config);",
        "    /** 仅注入配置初始化提供者。 */\n    void initialize(AuthConfig config);",
    ),
    (
        "    void initialize(AuthConfig config, Supplier<?> metadataService);",
        "    /** 注入配置与元数据服务初始化提供者。 */\n    void initialize(AuthConfig config, Supplier<?> metadataService);",
    ),
    (
        "    CompletableFuture<Void> authorize(AuthorizationContext context);",
        "    /** 对给定上下文执行授权责任链。 */\n    CompletableFuture<Void> authorize(AuthorizationContext context);",
    ),
    (
        "    List<AuthorizationContext> newContexts(Metadata metadata, GeneratedMessageV3 message);",
        "    /** 从 gRPC Metadata 与 Protobuf 请求构建授权上下文列表。 */\n    List<AuthorizationContext> newContexts(Metadata metadata, GeneratedMessageV3 message);",
    ),
    (
        "    List<AuthorizationContext> newContexts(ChannelHandlerContext context, RemotingCommand command);",
        "    /** 从 Netty 通道与 Remoting 命令构建授权上下文列表。 */\n    List<AuthorizationContext> newContexts(ChannelHandlerContext context, RemotingCommand command);",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/provider/DefaultAuthorizationProvider.java"] = [
    (
        "public class DefaultAuthorizationProvider implements AuthorizationProvider<DefaultAuthorizationContext> {",
        "/**\n * 默认授权提供者：组装 {@link DefaultAuthorizationContextBuilder} 与\n * User/Acl 责任链，并在完成后写审计日志。\n */\npublic class DefaultAuthorizationProvider implements AuthorizationProvider<DefaultAuthorizationContext> {",
    ),
    (
        "    @Override\n    public void initialize(AuthConfig config) {",
        "    /** 委托双参数 initialize，元数据服务为 null。 */\n    @Override\n    public void initialize(AuthConfig config) {",
    ),
    (
        "    @Override\n    public void initialize(AuthConfig config, Supplier<?> metadataService) {",
        "    /** 保存配置并创建 {@link DefaultAuthorizationContextBuilder}。 */\n    @Override\n    public void initialize(AuthConfig config, Supplier<?> metadataService) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> authorize(DefaultAuthorizationContext context) {",
        "    /** 执行责任链并在完成时写审计日志。 */\n    @Override\n    public CompletableFuture<Void> authorize(DefaultAuthorizationContext context) {",
    ),
    (
        "    @Override\n    public List<DefaultAuthorizationContext> newContexts(Metadata metadata, GeneratedMessageV3 message) {",
        "    /** 从 gRPC 请求构建授权上下文。 */\n    @Override\n    public List<DefaultAuthorizationContext> newContexts(Metadata metadata, GeneratedMessageV3 message) {",
    ),
    (
        "    @Override\n    public List<DefaultAuthorizationContext> newContexts(ChannelHandlerContext context, RemotingCommand command) {",
        "    /** 从 Remoting 请求构建授权上下文。 */\n    @Override\n    public List<DefaultAuthorizationContext> newContexts(ChannelHandlerContext context, RemotingCommand command) {",
    ),
    (
        "    protected HandlerChain<DefaultAuthorizationContext, CompletableFuture<Void>> newHandlerChain() {",
        "    /** 构建 User → Acl 授权责任链。 */\n    protected HandlerChain<DefaultAuthorizationContext, CompletableFuture<Void>> newHandlerChain() {",
    ),
    (
        "    protected void doAuditLog(DefaultAuthorizationContext context, Throwable ex) {",
        "    /** 记录授权审计日志；拒绝时 INFO，允许时 DEBUG。 */\n    protected void doAuditLog(DefaultAuthorizationContext context, Throwable ex) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authorization/provider/LocalAuthorizationMetadataProvider.java"] = [
    (
        "public class LocalAuthorizationMetadataProvider implements AuthorizationMetadataProvider {",
        "/**\n * 本地 ACL 元数据提供者：使用 RocksDB 持久化并以 Caffeine 缓存加速读取。\n */\npublic class LocalAuthorizationMetadataProvider implements AuthorizationMetadataProvider {",
    ),
    (
        "    @Override\n    public void initialize(AuthConfig authConfig, Supplier<?> metadataService) {",
        "    /** 打开 RocksDB 存储并配置 ACL 本地缓存。 */\n    @Override\n    public void initialize(AuthConfig authConfig, Supplier<?> metadataService) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> createAcl(Acl acl) {",
        "    /** 写入 ACL 到 RocksDB 并失效缓存。 */\n    @Override\n    public CompletableFuture<Void> createAcl(Acl acl) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> deleteAcl(Subject subject) {",
        "    /** 从 RocksDB 删除 ACL 并失效缓存。 */\n    @Override\n    public CompletableFuture<Void> deleteAcl(Subject subject) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> updateAcl(Acl acl) {",
        "    /** 更新 RocksDB 中的 ACL 并失效缓存。 */\n    @Override\n    public CompletableFuture<Void> updateAcl(Acl acl) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Acl> getAcl(Subject subject) {",
        "    /** 从 Caffeine 缓存读取 ACL；空占位符映射为 null。 */\n    @Override\n    public CompletableFuture<Acl> getAcl(Subject subject) {",
    ),
    (
        "    @Override\n    public CompletableFuture<List<Acl>> listAcl(String subjectFilter, String resourceFilter) {",
        "    /** 遍历 RocksDB 并按主体/资源子串过滤后返回 ACL 列表。 */\n    @Override\n    public CompletableFuture<List<Acl>> listAcl(String subjectFilter, String resourceFilter) {",
    ),
    (
        "    @Override\n    public void shutdown() {",
        "    /** 关闭 RocksDB 与缓存刷新线程池。 */\n    @Override\n    public void shutdown() {",
    ),
    (
        "    private static class AclCacheLoader implements CacheLoader<String, Acl> {",
        "    /** Caffeine 缓存加载器：从 RocksDB 反序列化 ACL。 */\n    private static class AclCacheLoader implements CacheLoader<String, Acl> {",
    ),
    (
        "        public AclCacheLoader(ConfigRocksDBStorage storage) {",
        "        /** 绑定 RocksDB 存储实例。 */\n        public AclCacheLoader(ConfigRocksDBStorage storage) {",
    ),
    (
        "        @Override\n        public Acl load(String subjectKey) {",
        "        /** 按主体键从 RocksDB 加载 ACL；不存在时返回 {@link #EMPTY_ACL}。 */\n        @Override\n        public Acl load(String subjectKey) {",
    ),
]
