"""RocketMQ 5.5.0 wave1a auth/authentication package replacements [0:15]."""

R: dict[str, list[tuple[str, str]]] = {}

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/AuthenticationEvaluator.java"] = [
    (
        "public class AuthenticationEvaluator {",
        "/**\n * 认证评估器：根据 {@link AuthConfig} 装配 {@link AuthenticationStrategy}，\n * 对 {@link AuthenticationContext} 执行认证流程。\n */\npublic class AuthenticationEvaluator {",
    ),
    (
        "    public AuthenticationEvaluator(AuthConfig authConfig) {",
        "    /** 使用默认元数据服务创建评估器。 */\n    public AuthenticationEvaluator(AuthConfig authConfig) {",
    ),
    (
        "    public AuthenticationEvaluator(AuthConfig authConfig, Supplier<?> metadataService) {",
        "    /** 指定元数据服务 Supplier 创建评估器。 */\n    public AuthenticationEvaluator(AuthConfig authConfig, Supplier<?> metadataService) {",
    ),
    (
        "    public void evaluate(AuthenticationContext context) {",
        "    /** 对给定上下文执行认证；context 为 null 时直接返回。 */\n    public void evaluate(AuthenticationContext context) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/builder/AuthenticationContextBuilder.java"] = [
    (
        "public interface AuthenticationContextBuilder<AuthenticationContext> {",
        "/**\n * 认证上下文构建器：从 gRPC 或 Remoting 请求中提取认证所需字段。\n *\n * @param <AuthenticationContext> 构建的目标上下文类型\n */\npublic interface AuthenticationContextBuilder<AuthenticationContext> {",
    ),
    (
        "    AuthenticationContext build(Metadata metadata, GeneratedMessageV3 request);",
        "    /** 从 gRPC {@link Metadata} 与 Protobuf 请求构建认证上下文。 */\n    AuthenticationContext build(Metadata metadata, GeneratedMessageV3 request);",
    ),
    (
        "    AuthenticationContext build(ChannelHandlerContext context, RemotingCommand request);",
        "    /** 从 Netty 通道上下文与 {@link RemotingCommand} 构建认证上下文。 */\n    AuthenticationContext build(ChannelHandlerContext context, RemotingCommand request);",
    ),
]

R[
    "auth/src/main/java/org/apache/rocketmq/auth/authentication/builder/DefaultAuthenticationContextBuilder.java"
] = [
    (
        "public class DefaultAuthenticationContextBuilder implements AuthenticationContextBuilder<DefaultAuthenticationContext> {",
        "/**\n * {@link AuthenticationContextBuilder} 默认实现：解析 gRPC Authorization 头\n * 或 Remoting 扩展字段中的 ACL 凭证与签名。\n */\npublic class DefaultAuthenticationContextBuilder implements AuthenticationContextBuilder<DefaultAuthenticationContext> {",
    ),
    (
        "    private static final String CREDENTIAL = \"Credential\";",
        "    /** Authorization 头中 Credential 键名。 */\n    private static final String CREDENTIAL = \"Credential\";",
    ),
    (
        "    private static final String SIGNATURE = \"Signature\";",
        "    /** Authorization 头中 Signature 键名。 */\n    private static final String SIGNATURE = \"Signature\";",
    ),
    (
        "    @Override\n    public DefaultAuthenticationContext build(Metadata metadata, GeneratedMessageV3 request) {",
        "    /** 解析 gRPC Authorization 头中的 Credential/Signature 与 datetime 作为签名内容。 */\n    @Override\n    public DefaultAuthenticationContext build(Metadata metadata, GeneratedMessageV3 request) {",
    ),
    (
        "    @Override\n    public DefaultAuthenticationContext build(ChannelHandlerContext context, RemotingCommand request) {",
        "    /** 从 Remoting 扩展字段提取 accessKey/signature，并组合请求体作为签名内容。 */\n    @Override\n    public DefaultAuthenticationContext build(ChannelHandlerContext context, RemotingCommand request) {",
    ),
    (
        "        // Content",
        "        // 组装待签名字节：排除 signature 字段，旧版本跳过 UNIQUE_MSG_QUERY_FLAG",
    ),
    (
        "    public String hexToBase64(String input) throws DecoderException {",
        "    /** 将十六进制签名字符串解码后转为 Base64。 */\n    public String hexToBase64(String input) throws DecoderException {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/chain/DefaultAuthenticationHandler.java"] = [
    (
        "public class DefaultAuthenticationHandler implements Handler<DefaultAuthenticationContext, CompletableFuture<Void>> {",
        "/**\n * 默认认证责任链处理器：加载用户、校验状态并用 {@link AclSigner} 比对签名。\n */\npublic class DefaultAuthenticationHandler implements Handler<DefaultAuthenticationContext, CompletableFuture<Void>> {",
    ),
    (
        "    public DefaultAuthenticationHandler(AuthConfig config, Supplier<?> metadataService) {",
        "    /** 根据配置初始化 {@link AuthenticationMetadataProvider}。 */\n    public DefaultAuthenticationHandler(AuthConfig config, Supplier<?> metadataService) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> handle(DefaultAuthenticationContext context,\n        HandlerChain<DefaultAuthenticationContext, CompletableFuture<Void>> chain) {",
        "    /** 异步获取用户后执行 {@link #doAuthenticate}。 */\n    @Override\n    public CompletableFuture<Void> handle(DefaultAuthenticationContext context,\n        HandlerChain<DefaultAuthenticationContext, CompletableFuture<Void>> chain) {",
    ),
    (
        "    protected CompletableFuture<User> getUser(DefaultAuthenticationContext context) {",
        "    /** 校验 username 非空后从元数据提供者加载 {@link User}。 */\n    protected CompletableFuture<User> getUser(DefaultAuthenticationContext context) {",
    ),
    (
        "    protected void doAuthenticate(DefaultAuthenticationContext context, User user) {",
        "    /** 校验用户存在、未禁用，并用 {@link MessageDigest#isEqual} 比对签名。 */\n    protected void doAuthenticate(DefaultAuthenticationContext context, User user) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/context/AuthenticationContext.java"] = [
    (
        "public abstract class AuthenticationContext {",
        "/**\n * 认证上下文抽象基类：承载通道 ID、RPC 标识及扩展信息。\n */\npublic abstract class AuthenticationContext {",
    ),
    (
        "    public String getChannelId() {",
        "    /** 返回 Netty 通道 ID。 */\n    public String getChannelId() {",
    ),
    (
        "    public void setChannelId(String channelId) {",
        "    /** 设置 Netty 通道 ID。 */\n    public void setChannelId(String channelId) {",
    ),
    (
        "    public String getRpcCode() {",
        "    /** 返回 RPC 方法标识（gRPC 全名或 Remoting code）。 */\n    public String getRpcCode() {",
    ),
    (
        "    public void setRpcCode(String rpcCode) {",
        "    /** 设置 RPC 方法标识。 */\n    public void setRpcCode(String rpcCode) {",
    ),
    (
        "    @SuppressWarnings(\"unchecked\")\n    public <T> T getExtInfo(String key) {",
        "    /** 按 key 读取扩展信息；key 为空或不存在时返回 null。 */\n    @SuppressWarnings(\"unchecked\")\n    public <T> T getExtInfo(String key) {",
    ),
    (
        "    public void setExtInfo(String key, Object value) {",
        "    /** 写入单条扩展信息；key 或 value 无效时忽略。 */\n    public void setExtInfo(String key, Object value) {",
    ),
    (
        "    public boolean hasExtInfo(String key) {",
        "    /** 判断指定 key 的扩展信息是否存在且非 null。 */\n    public boolean hasExtInfo(String key) {",
    ),
    (
        "    public Map<String, Object> getExtInfo() {",
        "    /** 返回扩展信息映射（可能为 null）。 */\n    public Map<String, Object> getExtInfo() {",
    ),
    (
        "    public void setExtInfo(Map<String, Object> extInfo) {",
        "    /** 批量设置扩展信息映射。 */\n    public void setExtInfo(Map<String, Object> extInfo) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/context/DefaultAuthenticationContext.java"] = [
    (
        "public class DefaultAuthenticationContext extends AuthenticationContext {",
        "/**\n * 默认认证上下文：包含用户名、待签名内容与客户端提交的签名。\n */\npublic class DefaultAuthenticationContext extends AuthenticationContext {",
    ),
    (
        "    public String getUsername() {",
        "    /** 返回认证用户名（accessKey）。 */\n    public String getUsername() {",
    ),
    (
        "    public void setUsername(String username) {",
        "    /** 设置认证用户名。 */\n    public void setUsername(String username) {",
    ),
    (
        "    public byte[] getContent() {",
        "    /** 返回参与签名的原始字节内容。 */\n    public byte[] getContent() {",
    ),
    (
        "    public void setContent(byte[] content) {",
        "    /** 设置参与签名的原始字节内容。 */\n    public void setContent(byte[] content) {",
    ),
    (
        "    public String getSignature() {",
        "    /** 返回客户端提交的签名字符串。 */\n    public String getSignature() {",
    ),
    (
        "    public void setSignature(String signature) {",
        "    /** 设置客户端提交的签名字符串。 */\n    public void setSignature(String signature) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/enums/SubjectType.java"] = [
    (
        "public enum SubjectType {",
        "/** 授权主体类型枚举，用于解析 subjectKey 前缀。 */\npublic enum SubjectType {",
    ),
    (
        "    USER((byte) 1, \"User\");",
        "    /** 普通用户主体。 */\n    USER((byte) 1, \"User\");",
    ),
    (
        "    public static SubjectType getByName(String name) {",
        "    /** 按名称（忽略大小写）查找主体类型。 */\n    public static SubjectType getByName(String name) {",
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

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/enums/UserStatus.java"] = [
    (
        "public enum UserStatus {",
        "/** 用户账号启用/禁用状态。 */\npublic enum UserStatus {",
    ),
    (
        "    ENABLE((byte) 1, \"enable\"),",
        "    /** 账号已启用。 */\n    ENABLE((byte) 1, \"enable\"),",
    ),
    (
        "    DISABLE((byte) 2, \"disable\");",
        "    /** 账号已禁用，认证将被拒绝。 */\n    DISABLE((byte) 2, \"disable\");",
    ),
    (
        "    public static UserStatus getByName(String name) {",
        "    /** 按名称（忽略大小写）查找用户状态。 */\n    public static UserStatus getByName(String name) {",
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

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/enums/UserType.java"] = [
    (
        "public enum UserType {",
        "/** 用户类型：超级用户拥有更高权限。 */\npublic enum UserType {",
    ),
    (
        "    SUPER((byte) 1, \"Super\"),",
        "    /** 超级用户，可绕过部分 ACL 限制。 */\n    SUPER((byte) 1, \"Super\"),",
    ),
    (
        "    NORMAL((byte) 2, \"Normal\");",
        "    /** 普通用户。 */\n    NORMAL((byte) 2, \"Normal\");",
    ),
    (
        "    public static UserType getByName(String name) {",
        "    /** 按名称（忽略大小写）查找用户类型。 */\n    public static UserType getByName(String name) {",
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

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/exception/AuthenticationException.java"] = [
    (
        "public class AuthenticationException extends RuntimeException {",
        "/** 认证失败时抛出的运行时异常，支持 SLF4J 风格占位符消息。 */\npublic class AuthenticationException extends RuntimeException {",
    ),
    (
        "    public AuthenticationException(String message) {",
        "    /** 使用固定消息构造异常。 */\n    public AuthenticationException(String message) {",
    ),
    (
        "    public AuthenticationException(String message, Throwable cause) {",
        "    /** 使用消息与根因构造异常。 */\n    public AuthenticationException(String message, Throwable cause) {",
    ),
    (
        "    public AuthenticationException(String messagePattern, Object... argArray) {",
        "    /** 使用 {@link MessageFormatter} 格式化占位符消息后构造异常。 */\n    public AuthenticationException(String messagePattern, Object... argArray) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/factory/AuthenticationFactory.java"] = [
    (
        "public class AuthenticationFactory {",
        "/**\n * 认证组件工厂：按 {@link AuthConfig} 缓存并创建 Provider、MetadataProvider、\n * Evaluator 与 {@link AuthenticationStrategy} 实例。\n */\npublic class AuthenticationFactory {",
    ),
    (
        "    public static AuthenticationProvider<AuthenticationContext> getProvider(AuthConfig config) {",
        "    /** 获取（或创建并缓存）{@link AuthenticationProvider} 实例。 */\n    public static AuthenticationProvider<AuthenticationContext> getProvider(AuthConfig config) {",
    ),
    (
        "    public static AuthenticationMetadataProvider getMetadataProvider(AuthConfig config) {",
        "    /** 获取元数据提供者（无外部 metadataService）。 */\n    public static AuthenticationMetadataProvider getMetadataProvider(AuthConfig config) {",
    ),
    (
        "    public static AuthenticationMetadataManager getMetadataManager(AuthConfig config) {",
        "    /** 创建 {@link AuthenticationMetadataManagerImpl} 管理用户元数据。 */\n    public static AuthenticationMetadataManager getMetadataManager(AuthConfig config) {",
    ),
    (
        "    public static AuthenticationMetadataProvider getMetadataProvider(AuthConfig config, Supplier<?> metadataService) {",
        "    /** 获取元数据提供者并传入 metadataService 完成 initialize。 */\n    public static AuthenticationMetadataProvider getMetadataProvider(AuthConfig config, Supplier<?> metadataService) {",
    ),
    (
        "    public static AuthenticationEvaluator getEvaluator(AuthConfig config) {",
        "    /** 获取（或缓存）{@link AuthenticationEvaluator}。 */\n    public static AuthenticationEvaluator getEvaluator(AuthConfig config) {",
    ),
    (
        "    public static AuthenticationEvaluator getEvaluator(AuthConfig config, Supplier<?> metadataService) {",
        "    /** 获取带 metadataService 的 {@link AuthenticationEvaluator}。 */\n    public static AuthenticationEvaluator getEvaluator(AuthConfig config, Supplier<?> metadataService) {",
    ),
    (
        "    public static AuthenticationStrategy getStrategy(AuthConfig config, Supplier<?> metadataService) {",
        "    /** 反射创建 {@link AuthenticationStrategy}，默认 {@link StatelessAuthenticationStrategy}。 */\n    public static AuthenticationStrategy getStrategy(AuthConfig config, Supplier<?> metadataService) {",
    ),
    (
        "    public static AuthenticationContext newContext(AuthConfig config, Metadata metadata, GeneratedMessageV3 request) {",
        "    /** 通过 Provider 从 gRPC 请求构建 {@link AuthenticationContext}。 */\n    public static AuthenticationContext newContext(AuthConfig config, Metadata metadata, GeneratedMessageV3 request) {",
    ),
    (
        "    public static AuthenticationContext newContext(AuthConfig config, ChannelHandlerContext context,\n        RemotingCommand command) {",
        "    /** 通过 Provider 从 Remoting 命令构建 {@link AuthenticationContext}。 */\n    public static AuthenticationContext newContext(AuthConfig config, ChannelHandlerContext context,\n        RemotingCommand command) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/manager/AuthenticationMetadataManager.java"] = [
    (
        "public interface AuthenticationMetadataManager {",
        "/**\n * 认证元数据管理接口：用户 CRUD 及超级用户判定。\n */\npublic interface AuthenticationMetadataManager {",
    ),
    (
        "    void shutdown();",
        "    /** 关闭底层元数据提供者。 */\n    void shutdown();",
    ),
    (
        "    void initUser(AuthConfig authConfig);",
        "    /** 根据配置初始化默认用户与内部客户端凭证。 */\n    void initUser(AuthConfig authConfig);",
    ),
    (
        "    CompletableFuture<Void> createUser(User user);",
        "    /** 创建新用户。 */\n    CompletableFuture<Void> createUser(User user);",
    ),
    (
        "    CompletableFuture<Void> updateUser(User user);",
        "    /** 更新已有用户的密码、类型或状态。 */\n    CompletableFuture<Void> updateUser(User user);",
    ),
    (
        "    CompletableFuture<Void> deleteUser(String username);",
        "    /** 删除用户并同步清理 ACL。 */\n    CompletableFuture<Void> deleteUser(String username);",
    ),
    (
        "    CompletableFuture<User> getUser(String username);",
        "    /** 按用户名查询用户。 */\n    CompletableFuture<User> getUser(String username);",
    ),
    (
        "    CompletableFuture<List<User>> listUser(String filter);",
        "    /** 按过滤条件列出用户。 */\n    CompletableFuture<List<User>> listUser(String filter);",
    ),
    (
        "    CompletableFuture<Boolean> isSuperUser(String username);",
        "    /** 判断用户是否为 {@link UserType#SUPER}。 */\n    CompletableFuture<Boolean> isSuperUser(String username);",
    ),
]

R[
    "auth/src/main/java/org/apache/rocketmq/auth/authentication/manager/AuthenticationMetadataManagerImpl.java"
] = [
    (
        "public class AuthenticationMetadataManagerImpl implements AuthenticationMetadataManager {",
        "/**\n * {@link AuthenticationMetadataManager} 默认实现：委托 Provider 持久化用户，\n * 并在启动时初始化配置中的默认用户与内部客户端凭证。\n */\npublic class AuthenticationMetadataManagerImpl implements AuthenticationMetadataManager {",
    ),
    (
        "    public AuthenticationMetadataManagerImpl(AuthConfig authConfig) {",
        "    /** 装配认证/授权元数据提供者并执行 {@link #initUser}。 */\n    public AuthenticationMetadataManagerImpl(AuthConfig authConfig) {",
    ),
    (
        "    @Override\n    public void shutdown() {",
        "    /** 依次关闭认证与授权元数据提供者。 */\n    @Override\n    public void shutdown() {",
    ),
    (
        "    @Override\n    public void initUser(AuthConfig authConfig) {",
        "    /** 解析 initAuthenticationUser 与 innerClientAuthenticationCredentials 并幂等创建用户。 */\n    @Override\n    public void initUser(AuthConfig authConfig) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> createUser(User user) {",
        "    /** 校验后创建用户，默认类型 NORMAL、状态 ENABLE。 */\n    @Override\n    public CompletableFuture<Void> createUser(User user) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> updateUser(User user) {",
        "    /** 合并更新密码、用户类型与状态。 */\n    @Override\n    public CompletableFuture<Void> updateUser(User user) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Void> deleteUser(String username) {",
        "    /** 并行删除用户记录与对应 ACL。 */\n    @Override\n    public CompletableFuture<Void> deleteUser(String username) {",
    ),
    (
        "    @Override\n    public CompletableFuture<User> getUser(String username) {",
        "    /** 按用户名查询用户。 */\n    @Override\n    public CompletableFuture<User> getUser(String username) {",
    ),
    (
        "    @Override\n    public CompletableFuture<List<User>> listUser(String filter) {",
        "    /** 列出符合过滤条件的用户。 */\n    @Override\n    public CompletableFuture<List<User>> listUser(String filter) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Boolean> isSuperUser(String username) {",
        "    /** 查询用户并判断是否为超级用户。 */\n    @Override\n    public CompletableFuture<Boolean> isSuperUser(String username) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/model/Subject.java"] = [
    (
        "public interface Subject {",
        "/**\n * 授权主体抽象：提供 subjectKey 与 {@link SubjectType}，\n * 支持从 \"Type:Name\" 格式字符串反序列化。\n */\npublic interface Subject {",
    ),
    (
        "    @JSONField(serialize = false)\n    String getSubjectKey();",
        "    /** 返回 \"SubjectType:identifier\" 格式的主体键，不参与 JSON 序列化。 */\n    @JSONField(serialize = false)\n    String getSubjectKey();",
    ),
    (
        "    SubjectType getSubjectType();",
        "    /** 返回主体类型枚举。 */\n    SubjectType getSubjectType();",
    ),
    (
        "    default boolean isSubject(SubjectType subjectType) {",
        "    /** 判断当前主体是否属于指定类型。 */\n    default boolean isSubject(SubjectType subjectType) {",
    ),
    (
        "    @SuppressWarnings(\"unchecked\")\n    static <T extends Subject> T of(String subjectKey) {",
        "    /** 解析 subjectKey 并构造对应 {@link Subject} 实例（当前支持 User）。 */\n    @SuppressWarnings(\"unchecked\")\n    static <T extends Subject> T of(String subjectKey) {",
    ),
]

R["auth/src/main/java/org/apache/rocketmq/auth/authentication/model/User.java"] = [
    (
        "public class User implements Subject {",
        "/**\n * 认证用户实体：实现 {@link Subject}，承载用户名、密码、类型与状态。\n */\npublic class User implements Subject {",
    ),
    (
        "    public static User of(String username) {",
        "    /** 仅指定用户名的工厂方法。 */\n    public static User of(String username) {",
    ),
    (
        "    public static User of(String username, String password) {",
        "    /** 指定用户名与密码的工厂方法。 */\n    public static User of(String username, String password) {",
    ),
    (
        "    public static User of(String username, String password, UserType userType) {",
        "    /** 指定用户名、密码与用户类型的工厂方法。 */\n    public static User of(String username, String password, UserType userType) {",
    ),
    (
        "    @Override\n    public String getSubjectKey() {",
        "    /** 返回 \"User:username\" 格式的主体键。 */\n    @Override\n    public String getSubjectKey() {",
    ),
    (
        "    @Override\n    public SubjectType getSubjectType() {",
        "    /** 固定返回 {@link SubjectType#USER}。 */\n    @Override\n    public SubjectType getSubjectType() {",
    ),
    (
        "    public String getUsername() {",
        "    /** 返回用户名（accessKey）。 */\n    public String getUsername() {",
    ),
    (
        "    public void setUsername(String username) {",
        "    /** 设置用户名。 */\n    public void setUsername(String username) {",
    ),
    (
        "    public String getPassword() {",
        "    /** 返回密码/secretKey。 */\n    public String getPassword() {",
    ),
    (
        "    public void setPassword(String password) {",
        "    /** 设置密码/secretKey。 */\n    public void setPassword(String password) {",
    ),
    (
        "    public UserType getUserType() {",
        "    /** 返回用户类型。 */\n    public UserType getUserType() {",
    ),
    (
        "    public void setUserType(UserType userType) {",
        "    /** 设置用户类型。 */\n    public void setUserType(UserType userType) {",
    ),
    (
        "    public UserStatus getUserStatus() {",
        "    /** 返回用户状态。 */\n    public UserStatus getUserStatus() {",
    ),
    (
        "    public void setUserStatus(UserStatus userStatus) {",
        "    /** 设置用户状态。 */\n    public void setUserStatus(UserStatus userStatus) {",
    ),
]
