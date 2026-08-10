#!/usr/bin/env python3
"""Generate wave22a annotated Keycloak LDAP storage provider + idm files."""
from __future__ import annotations

import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "keycloak/26.7.1/original"
ANAL = ROOT / "keycloak/26.7.1/analyzed"

FILES = [
    "federation/ldap/src/main/java/org/keycloak/storage/ldap/LDAPConfig.java",
    "federation/ldap/src/main/java/org/keycloak/storage/ldap/LDAPIdentityStoreRegistry.java",
    "federation/ldap/src/main/java/org/keycloak/storage/ldap/LDAPStorageProvider.java",
    "federation/ldap/src/main/java/org/keycloak/storage/ldap/LDAPStorageProviderFactory.java",
    "federation/ldap/src/main/java/org/keycloak/storage/ldap/LDAPStorageUserManager.java",
    "federation/ldap/src/main/java/org/keycloak/storage/ldap/LDAPUtils.java",
    "federation/ldap/src/main/java/org/keycloak/storage/ldap/LDAPWritesOnlyUserModelDelegate.java",
    "federation/ldap/src/main/java/org/keycloak/storage/ldap/ObjectFactoryBuilder.java",
    "federation/ldap/src/main/java/org/keycloak/storage/ldap/ReadonlyLDAPUserModelDelegate.java",
    "federation/ldap/src/main/java/org/keycloak/storage/ldap/idm/model/LDAPDn.java",
    "federation/ldap/src/main/java/org/keycloak/storage/ldap/idm/model/LDAPObject.java",
    "federation/ldap/src/main/java/org/keycloak/storage/ldap/idm/query/Condition.java",
    "federation/ldap/src/main/java/org/keycloak/storage/ldap/idm/query/EscapeStrategy.java",
    "federation/ldap/src/main/java/org/keycloak/storage/ldap/idm/query/Sort.java",
    "federation/ldap/src/main/java/org/keycloak/storage/ldap/idm/query/internal/AndCondition.java",
]

HAN = re.compile(r"[\u4e00-\u9fff]")


def count_han(text: str) -> int:
    return len(HAN.findall(text))


def patch(rel: str, transforms: list[tuple[str, str]]) -> str:
    src = (ORIG / rel).read_text(encoding="utf-8")
    for old, new in transforms:
        if old not in src:
            raise ValueError(f"{rel}: patch anchor not found:\n{old[:200]}")
        src = src.replace(old, new, 1)
    return src


def write_analyzed(rel: str, content: str) -> int:
    dest = ANAL / rel
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(content, encoding="utf-8")
    n = count_han(content)
    if n == 0:
        raise ValueError(f"{rel}: no CJK Han characters")
    return n


def annotate_all() -> dict[str, int]:
    counts: dict[str, int] = {}

    counts[FILES[0]] = write_analyzed(
        FILES[0],
        patch(
            FILES[0],
            [
                (
                    """/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 *
 */
public class LDAPConfig""",
                    """/**
 * LDAP 用户联邦存储的配置封装，从组件 {@link org.keycloak.common.util.MultivaluedHashMap} 读取连接、搜索、厂商与同步参数。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPConfig""",
                ),
                (
                    "    public static final String DEFAULT_CONNECTION_TIMEOUT = \"5000\";",
                    "    /** 默认 LDAP 连接超时（毫秒）。 */\n    public static final String DEFAULT_CONNECTION_TIMEOUT = \"5000\";",
                ),
                (
                    "        // hardcoded for now",
                    "        // 当前固定使用 Sun JNDI LDAP 上下文工厂",
                ),
                (
                    "            // Just for the backwards compatibility 1.2 -> 1.3 . Should be removed later.",
                    "            // 1.2 → 1.3 向后兼容：旧键 userDnSuffix",
                ),
                (
                    "        // Trim them",
                    "        // 去除 objectClass 名称首尾空白",
                ),
                (
                    "        // not supported for now",
                    "        // 暂不支持额外连接属性",
                ),
                (
                    "            // Differences of unique attribute among various vendors",
                    "            // 各 LDAP 厂商 UUID 属性名不同，按 vendor 推断默认值",
                ),
                (
                    "                // Just for the backwards compatibility 1.2 -> 1.3 . Should be removed later.",
                    "                // 1.2 → 1.3 向后兼容：sAMAccountName 作 RDN 时回退为 cn",
                ),
                (
                    "    public String getConnectionUrl() {",
                    "    /** 返回 LDAP 连接 URL。 */\n    public String getConnectionUrl() {",
                ),
                (
                    "    public boolean isActiveDirectory() {",
                    "    /** 当前配置是否指向 Active Directory。 */\n    public boolean isActiveDirectory() {",
                ),
                (
                    "    public UserStorageProvider.EditMode getEditMode() {",
                    "    /** 返回用户编辑模式（只读/可写/不同步）。 */\n    public UserStorageProvider.EditMode getEditMode() {",
                ),
                (
                    "    public boolean isImportEnabled() {",
                    "    /** 是否将 LDAP 用户导入 Keycloak 本地数据库。 */\n    public boolean isImportEnabled() {",
                ),
            ],
        ),
    )

    counts[FILES[1]] = write_analyzed(
        FILES[1],
        patch(
            FILES[1],
            [
                (
                    """/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPIdentityStoreRegistry""",
                    """/**
 * 按 LDAP 组件 ID 缓存 {@link LDAPConfig}，并在配置变更时重建 {@link org.keycloak.storage.ldap.idm.store.ldap.LDAPIdentityStore}。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPIdentityStoreRegistry""",
                ),
                (
                    "        // Ldap config might have changed for the realm. In this case, we must re-initialize",
                    "        // realm 中 LDAP 配置可能已变更，需重新构建 LDAPConfig",
                ),
                (
                    "    // Don't log LDAP password",
                    "    // 日志中不输出 bind 密码（toString 已剔除 BIND_CREDENTIAL）",
                ),
                (
                    "    public LDAPIdentityStore getLdapStore(KeycloakSession session, ComponentModel ldapModel, Map<ComponentModel, LDAPConfigDecorator> configDecorators) {",
                    """    /**
     * 获取或创建与给定 LDAP 组件对应的 {@link org.keycloak.storage.ldap.idm.store.ldap.LDAPIdentityStore}。
     * 会先应用 mapper 提供的 {@link org.keycloak.storage.ldap.mappers.LDAPConfigDecorator} 再比较缓存。
     */
    public LDAPIdentityStore getLdapStore(KeycloakSession session, ComponentModel ldapModel, Map<ComponentModel, LDAPConfigDecorator> configDecorators) {""",
                ),
            ],
        ),
    )

    counts[FILES[2]] = write_analyzed(
        FILES[2],
        patch(
            FILES[2],
            [
                (
                    """/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class LDAPStorageProvider implements UserStorageProvider,""",
                    """/**
 * LDAP 用户存储提供器：用户查找/注册、凭据校验、Kerberos/SPNEGO、同步导入及 mapper 代理链的核心实现。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class LDAPStorageProvider implements UserStorageProvider,""",
                ),
                (
                    "    // these exist to make sure that we only hit ldap once per transaction",
                    "    // 事务内尽量只访问 LDAP 一次（由 LDAPStorageUserManager 缓存已加载对象）",
                ),
                (
                    "        // We need to avoid having CachedUserModel as cache is upper-layer then LDAP. Hence having CachedUserModel here may cause StackOverflowError",
                    "        // 避免在此层使用 CachedUserModel，否则上层缓存与 LDAP 代理链可能 StackOverflow",
                ),
                (
                    "                // Any attempt to write data, which are not supported by the LDAP schema, should fail",
                    "                // 非 LDAP schema 支持的字段写入应失败（只写 LDAP 委托）",
                ),
                (
                    "                // This check is skipped when register new user as there are many \"generic\" attributes always written (EG. enabled, emailVerified) and those are usually unsupported by LDAP schema",
                    "                // 注册新用户时跳过：enabled、emailVerified 等通用属性通常不在 LDAP schema 中",
                ),
                (
                    "    /**\n     * LDAP search supports {@link UserModel#SEARCH}, {@link UserModel#EXACT} and",
                    """    /**
     * LDAP 用户搜索：支持 {@link UserModel#SEARCH}、{@link UserModel#EXACT} 及 mapper 映射的属性。
     * LDAP search supports {@link UserModel#SEARCH}, {@link UserModel#EXACT} and""",
                ),
                (
                    "    /**\n     * Searches LDAP using logical conjunction of params. It uses the LDAP mappers",
                    """    /**
     * 按属性 Map 逻辑与（AND）搜索 LDAP；未映射属性则返回空流。
     * Searches LDAP using logical conjunction of params. It uses the LDAP mappers""",
                ),
                (
                    "    /**\n     * Searches LDAP using logical disjunction of params. It supports",
                    """    /**
     * 按 {@code search} 参数对用户名/邮箱/姓名等字段做逻辑或（OR）搜索。
     * Searches LDAP using logical disjunction of params. It supports""",
                ),
                (
                    "    /**\n     * @param local\n     * @return ldapUser corresponding to local user or null if user is no longer in LDAP\n     */",
                    """    /**
     * 加载并校验本地用户对应的 LDAP 条目。
     * @param local 本地 {@link UserModel}
     * @return 对应的 {@link LDAPObject}，LDAP 中已不存在则返回 null
     */""",
                ),
                (
                    "    protected enum ImportType {\n        FORCED, // the import is forced\n        NOT_FORCED_RETURN_NULL, // the import is not forced and null is returned when a previous user exists\n        NOT_FORCED_RETURN_EXISTING  // the import is not forced and existing user is returned\n    };",
                    """    /** LDAP 用户导入策略。 */
    protected enum ImportType {
        FORCED, // 强制导入
        NOT_FORCED_RETURN_NULL, // 不强制：已存在本地用户时返回 null
        NOT_FORCED_RETURN_EXISTING  // 不强制：返回已有本地用户
    };""",
                ),
                (
                    "    /**\n     * Called after successful kerberos authentication",
                    """    /**
     * Kerberos 认证成功后查找或创建 Keycloak 用户。
     * Called after successful kerberos authentication""",
                ),
                (
                    "    /**\n     * This method leverages existing pagination support in {@link LDAPQuery#getResultList()}. It sets the limit for the query",
                    """    /**
     * 利用 {@link LDAPQuery#getResultList()} 的分页能力，按 firstResult/maxResults 惰性填充结果流。
     * This method leverages existing pagination support in {@link LDAPQuery#getResultList()}. It sets the limit for the query""",
                ),
                (
                    "                        //the very 1st page - Pagination context might not yet be present",
                    "                        // 首页：分页上下文可能尚未初始化",
                ),
                (
                    "            // Fallback to username (backwards compatibility)",
                    "            // 回退用用户名（向后兼容）",
                ),
                (
                    "                // LDAP password policy requires a forced password change.",
                    "                // LDAP 密码策略要求强制改密",
                ),
                (
                    "                        // Just a fallback. It should not happen during normal authentication process",
                    "                        // 兜底路径，正常认证流程不应走到此处",
                ),
            ],
        ),
    )

    counts[FILES[3]] = write_analyzed(
        FILES[3],
        patch(
            FILES[3],
            [
                (
                    """/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class LDAPStorageProviderFactory implements UserStorageProviderFactory<LDAPStorageProvider>, ImportSynchronization""",
                    """/**
 * LDAP 用户存储工厂：组件生命周期、配置校验、默认 mapper 创建及全量/增量同步。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class LDAPStorageProviderFactory implements UserStorageProviderFactory<LDAPStorageProvider>, ImportSynchronization""",
                ),
                (
                    "    // Check if it's some performance overhead to create this map in every request. But probably not...",
                    "    // 每次请求构建 decorator 映射的开销通常可忽略",
                ),
                (
                    "        // This parses the configuration directly as cfg.getConnectionPooling() will take into account the current StartTLS setting",
                    "        // 直接读原始配置：getConnectionPooling() 会结合 StartTLS 决定是否启用连接池",
                ),
                (
                    "        // editMode is mandatory",
                    "        // editMode 为必填项",
                ),
                (
                    "        // validatePasswordPolicy applicable only for WRITABLE mode",
                    "        // validatePasswordPolicy 仅 WRITABLE 模式可用",
                ),
                (
                    "            // the ldap component is being created, use short id for ldap components",
                    "            // 新建组件时使用短 ID",
                ),
                (
                    "        // set connection pooling for plain and tls protocols by default",
                    "        // 默认启用 plain/ssl 协议的 JNDI 连接池",
                ),
                (
                    "    // Best effort to create appropriate mappers according to our LDAP config",
                    "    // 按 LDAP 配置尽力创建合适的默认 mapper",
                ),
                (
                    "        // CN is typically used as RDN for Active Directory deployments",
                    "        // AD 部署通常以 CN 作为 RDN",
                ),
                (
                    "                // For AD deployments with \"cn\" as username, we will map \"givenName\" to first name",
                    "                // AD 且用户名为 cn 时，givenName 映射到 firstName",
                ),
                (
                    "        // map createTimeStamp as read-only",
                    "        // 创建时间戳只读映射",
                ),
                (
                    "        // MSAD specific mapper for account state propagation",
                    "        // MSAD 账户控制状态同步 mapper",
                ),
                (
                    "        // In case that \"Sync Registration\" is ON and the LDAP v3 Password-modify extension is ON, we will create hardcoded mapper to create",
                    "        // 开启同步注册且使用 Password Modify 扩展时，写入随机 userPassword 以便用户可登录",
                ),
                (
                    "            // propagate username LDAP attribute change to the username mapper.",
                    "            // 用户名 LDAP 属性变更时同步到 username mapper",
                ),
                (
                    "            // TODO: Remove all existing keycloak users, which have federation links, but are not in LDAP. Perhaps don't check users, which were just added or updated during this sync?",
                    "            // TODO: 清理 federationLink 存在但 LDAP 中已不存在的本地用户",
                ),
                (
                    "        // Sync newly created and updated users",
                    "        // 同步自 lastSync 以来新建或修改的用户",
                ),
                (
                    "            // LDAP pagination not available. Do everything in single transaction",
                    "            // 无 LDAP 分页时一次性拉取全部结果",
                ),
                (
                    "    /**\n     *  !! This function must be called from try-with-resources block, otherwise Vault secrets may be leaked !!",
                    """    /**
     * 必须在 try-with-resources 中调用，否则 Vault 密钥可能泄漏。
     *  !! This function must be called from try-with-resources block, otherwise Vault secrets may be leaked !!""",
                ),
                (
                    "                // Process each user in it's own transaction to avoid global fail",
                    "                // 每个用户独立事务，避免单条失败导致整批回滚",
                ),
                (
                    "                            // Add new user to Keycloak",
                    "                            // 新用户写入 Keycloak 本地库",
                ),
                (
                    "                                // Update keycloak user",
                    "                                // 更新已有 Keycloak 用户属性",
                ),
                (
                    "                // Remove user if we already added him during this transaction",
                    "                // 本事务中已添加的用户在失败时回滚删除",
                ),
            ],
        ),
    )

    counts[FILES[4]] = write_analyzed(
        FILES[4],
        patch(
            FILES[4],
            [
                (
                    """/**
 * Track which LDAP users were already enlisted during this transaction
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPStorageUserManager""",
                    """/**
 * 跟踪当前事务内已加载的 LDAP 用户与代理 {@link UserModel}，避免重复查询并持有 {@link org.keycloak.storage.ldap.mappers.LDAPTransaction}。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPStorageUserManager""",
                ),
                (
                    "    public UserModel getManagedProxiedUser(String userId) {",
                    "    /** 返回事务内已建立的代理用户，未登记则 null。 */\n    public UserModel getManagedProxiedUser(String userId) {",
                ),
                (
                    "    public LDAPObject getManagedLDAPObject(String userId) {",
                    "    /** 返回事务内缓存的 {@link LDAPObject}。 */\n    public LDAPObject getManagedLDAPObject(String userId) {",
                ),
                (
                    "    public void setManagedProxiedUser(UserModel proxiedUser, LDAPObject ldapObject) {",
                    "    /** 登记代理用户、LDAP 对象及对应的 LDAP 写事务。 */\n    public void setManagedProxiedUser(UserModel proxiedUser, LDAPObject ldapObject) {",
                ),
                (
                    "    private static class ManagedUserEntry {",
                    "    /** 单用户事务条目：代理模型、LDAP 对象与写事务。 */\n    private static class ManagedUserEntry {",
                ),
            ],
        ),
    )

    counts[FILES[5]] = write_analyzed(
        FILES[5],
        patch(
            FILES[5],
            [
                (
                    """/**
 * Allow to directly call some operations against LDAPIdentityStore.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPUtils""",
                    """/**
 * LDAP 用户/组操作的静态工具：创建用户、构建查询、成员关系、分页加载与过滤器校验等。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPUtils""",
                ),
                (
                    """    /**
     * Method to create a user in the LDAP. The user will be created when all
     * mandatory attributes specified by the mappers are set. The method
     * onRegisterUserToLDAP is first called in each mapper to set any default or
     * initial value.
     *
     * @param ldapProvider The ldap provider
     * @param realm The realm of the user
     * @param user The user model
     * @return The LDAPObject created or to be created when mandatory attributes are filled
     */""",
                    """    /**
     * 在 LDAP 中创建用户：各 mapper 先调用 onRegisterUserToLDAP，待必填属性齐备后写入目录。
     *
     * @param ldapProvider The ldap provider
     * @param realm The realm of the user
     * @param user The user model
     * @return The LDAPObject created or to be created when mandatory attributes are filled
     */""",
                ),
                (
                    """    /**
     * Method that creates a user in the LDAP when all the attributes marked as
     * mandatory by the mappers are set. The method onRegisterUserToLDAP is
     * first called in each mapper to set any default or initial value. When
     * the user is finally created the passed consumerOnCreated parameter is
     * executed (can be null).
     *
     * @param ldapProvider The ldap provider
     * @param realm The realm of the user
     * @param user The user model
     * @param consumerOnCreated The consumer to execute when the user is created
     * @return The LDAPObject created or to be created when mandatory attributes are filled
     */""",
                    """    /**
     * 创建 LDAP 用户并在写入成功后执行 consumerOnCreated（可为 null）。
     *
     * @param ldapProvider The ldap provider
     * @param realm The realm of the user
     * @param user The user model
     * @param consumerOnCreated The consumer to execute when the user is created
     * @return The LDAPObject created or to be created when mandatory attributes are filled
     */""",
                ),
                (
                    "    public static LDAPQuery createQueryForUserSearch(LDAPStorageProvider ldapProvider, RealmModel realm) {",
                    """    /** 构建用户搜索用的 {@link org.keycloak.storage.ldap.idm.query.internal.LDAPQuery}（含自定义过滤器与 mapper 属性）。 */
    public static LDAPQuery createQueryForUserSearch(LDAPStorageProvider ldapProvider, RealmModel realm) {""",
                ),
                (
                    "        // Mark the password modification time attribute as read-only so it is not sent back on updates — it is an",
                    "        // 密码修改时间属性标记为只读，更新时不回写（由 LDAP 服务端维护）",
                ),
                (
                    "    // ldapUser has filled attributes, but doesn't have filled dn.",
                    "    // ldapUser 已有属性但尚未设置 DN",
                ),
                (
                    "    // roles & groups",
                    "    // 角色与组相关工具",
                ),
                (
                    "            // On MSAD with object class \"group\", empty member must not be added. Specified object classes typically",
                    "            // MSAD group 对象类不能写空 member；其他 groupOf* 类需占位空成员",
                ),
                (
                    "    public static String getUsername(LDAPObject ldapUser, LDAPConfig config) {",
                    "    /** 从 LDAP 条目读取 Keycloak 用户名（导入模式下转小写）。 */\n    public static String getUsername(LDAPObject ldapUser, LDAPConfig config) {",
                ),
                (
                    "    public static void checkUuid(LDAPObject ldapUser, LDAPConfig config) {",
                    "    /** 校验 LDAP 条目 UUID 非空，否则抛出配置错误。 */\n    public static void checkUuid(LDAPObject ldapUser, LDAPConfig config) {",
                ),
                (
                    """    /**
     * Convert Generalized Time as defined in RFC4517 to the Date
     */""",
                    """    /**
     * 将 RFC4517 Generalized Time 字符串转换为 {@link Date}。
     */""",
                ),
            ],
        ),
    )

    counts[FILES[6]] = write_analyzed(
        FILES[6],
        patch(
            FILES[6],
            [
                (
                    """/**
 * User model delegate, which tracks what attributes were written to LDAP in this transaction. For those attributes, it will skip
 * calling delegate for doing any additional updates.
 *
 * It may be typically used together with Read-Only delegate. The result is that read-only exception will be thrown when attempt
 * to update any user attribute, which is NOT mapped to LDAP.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPWritesOnlyUserModelDelegate extends UserModelDelegate""",
                    """/**
 * 用户模型委托：记录本事务已写入 LDAP 的属性，避免对下层委托重复更新。
 * <p>
 * 常与只读委托组合：未映射到 LDAP 的属性修改将抛出 {@link org.keycloak.storage.ReadOnlyException}。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPWritesOnlyUserModelDelegate extends UserModelDelegate""",
                ),
                (
                    "    // Checks if attribute was updated in LDAP in this transaction",
                    "    // 判断本事务内该属性是否已由 LDAP mapper 更新",
                ),
                (
                    "    // Checks if requiredAction was updated in LDAP in this transaction",
                    "    // 判断本事务内该 requiredAction 是否已在 LDAP 侧处理",
                ),
            ],
        ),
    )

    counts[FILES[7]] = write_analyzed(
        FILES[7],
        patch(
            FILES[7],
            [
                (
                    """/**
 * <p>A {@link javax.naming.spi.ObjectFactoryBuilder} implementation to filter out referral references if they do not
 * point to an LDAP URL.
 *
 * <p>When the LDAP provider encounters a referral, it tries to create an {@link ObjectFactory} from this builder.
 * If the referral reference contains an LDAP URL, a {@link DirContextObjectFactory} is created to handle the referral.
 * Otherwise, a {@link CommunicationException} is thrown to indicate that the referral cannot be processed.
 */
final class ObjectFactoryBuilder implements javax.naming.spi.ObjectFactoryBuilder, ObjectFactory""",
                    """/**
 * JNDI {@link javax.naming.spi.ObjectFactoryBuilder}：仅处理含 LDAP URL 的 referral，否则拒绝非安全转介。
 * <p>
 * 遇到 referral 时若引用含 LDAP URL，则创建 {@link DirContextObjectFactory} 建立 {@link org.keycloak.storage.ldap.idm.store.ldap.SessionBoundInitialLdapContext}；
 * 否则抛出 {@link CommunicationException}。
 */
final class ObjectFactoryBuilder implements javax.naming.spi.ObjectFactoryBuilder, ObjectFactory""",
                ),
                (
                    "    static boolean isSet() {",
                    "    /** 检测当前 JVM 是否已安装本 Keycloak ObjectFactoryBuilder。 */\n    static boolean isSet() {",
                ),
                (
                    "    private record DirContextObjectFactory(String ldapUrl) implements ObjectFactory {",
                    "    /** 按 referral URL 克隆环境并打开会话绑定的 LDAP 上下文。 */\n    private record DirContextObjectFactory(String ldapUrl) implements ObjectFactory {",
                ),
            ],
        ),
    )

    counts[FILES[8]] = write_analyzed(
        FILES[8],
        patch(
            FILES[8],
            [
                (
                    """/**
 * Will be good to get rid of this class and use ReadOnlyUserModelDelegate, but it can't be done now due the backwards compatibility.
 * See KEYCLOAK-15139 as an example
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ReadonlyLDAPUserModelDelegate extends UserModelDelegate""",
                    """/**
 * 只读 LDAP 用户委托：除 {@link LDAPStorageProvider#INTERNAL_ATTRIBUTES} 外，任何属性变更均抛出 {@link ReadOnlyException}。
 * <p>
 * 因向后兼容暂保留（参见 KEYCLOAK-15139），理想情况可统一为 ReadOnlyUserModelDelegate。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ReadonlyLDAPUserModelDelegate extends UserModelDelegate""",
                ),
            ],
        ),
    )

    counts[FILES[9]] = write_analyzed(
        FILES[9],
        patch(
            FILES[9],
            [
                (
                    """/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPDn""",
                    """/**
 * LDAP 可分辨名称（DN）的轻量封装，基于 {@link javax.naming.ldap.LdapName} 提供 RDN 访问与父子关系判断。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPDn""",
                ),
                (
                    "        // In certain OpenLDAP implementations the uniqueMember attribute is mandatory",
                    "        // 部分 OpenLDAP 实现允许空 DN 字符串（uniqueMember 占位场景）",
                ),
                (
                    """    /**
     * @return first entry. Usually entry corresponding to something like "uid=joe" from the DN like "uid=joe,dc=something,dc=org"
     */""",
                    """    /**
     * 返回最左侧 RDN，例如 DN {@code uid=joe,dc=example,dc=org} 中的 {@code uid=joe}。
     * @return first entry. Usually entry corresponding to something like "uid=joe" from the DN like "uid=joe,dc=something,dc=org"
     */""",
                ),
                (
                    """    /**
     *
     * @return DN like "dc=something,dc=org" from the DN like "uid=joe,dc=something,dc=org".
     * Returned DN will be new clone not related to the original DN instance.
     *
     */""",
                    """    /**
     * 返回去掉最左侧 RDN 后的父 DN 副本（与原实例独立）。
     * @return DN like "dc=something,dc=org" from the DN like "uid=joe,dc=something,dc=org".
     * Returned DN will be new clone not related to the original DN instance.
     */""",
                ),
                (
                    "    public boolean isDescendantOf(LDAPDn expectedParentDn) {",
                    "    /** 判断当前 DN 是否为 expectedParentDn 的后代（前缀匹配）。 */\n    public boolean isDescendantOf(LDAPDn expectedParentDn) {",
                ),
                (
                    """    /**
     * Single RDN inside the DN. RDN usually consists of single item like "uid=john" . In some rare cases, it can have multiple
     * sub-entries like "uid=john+sn=Doe"
     */""",
                    """    /**
     * DN 中的单个 RDN；常见为 {@code uid=john}，少数为多值 {@code uid=john+sn=Doe}。
     * Single RDN inside the DN. RDN usually consists of single item like "uid=john" . In some rare cases, it can have multiple
     * sub-entries like "uid=john+sn=Doe"
     */""",
                ),
            ],
        ),
    )

    counts[FILES[10]] = write_analyzed(
        FILES[10],
        patch(
            FILES[10],
            [
                (
                    """/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPObject""",
                    """/**
 * LDAP 目录条目的内存表示：UUID、DN、objectClass、属性集、只读/range 属性及必填属性延迟提交。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPObject""",
                ),
                (
                    "    // In most cases, there is single \"rdnAttributeName\" . Usually \"uid\" or \"cn\"",
                    "    // 多数情况下 RDN 属性为 uid 或 cn",
                ),
                (
                    "    // NOTE: names of read-only attributes are lower-cased to avoid case sensitivity issues",
                    "    // 只读属性名统一小写，避免大小写差异",
                ),
                (
                    "    // Copy of \"attributes\" containing lower-cased keys and original case-sensitive attribute name",
                    "    // 属性名大小写不敏感索引，保留原始大小写键",
                ),
                (
                    "    // range attributes are always read from 0 to max so just saving the top value",
                    "    // range 属性分段读取，仅记录当前已读上限",
                ),
                (
                    "    // consumer to be executed when mandatory attributes are set",
                    "    // 必填属性全部就绪后执行的回调",
                ),
                (
                    "    public void executeOnMandatoryAttributesComplete(Set<String> mandatoryAttributeNames, Consumer<LDAPObject> consumer) {",
                    """    /**
     * 注册必填属性集合；全部赋值完成后执行 consumer（用于延迟 create/add）。
     */
    public void executeOnMandatoryAttributesComplete(Set<String> mandatoryAttributeNames, Consumer<LDAPObject> consumer) {""",
                ),
                (
                    "    // Case-insensitive",
                    "    // 属性名大小写不敏感",
                ),
                (
                    """    /**
     * Useful when single value will be used as the "RDN" attribute. Which will be most of the cases
     * @param rdnAttributeName The RDN of the ldap object
     */""",
                    """    /**
     * 设置单个 RDN 属性名（常见场景）。
     * @param rdnAttributeName The RDN of the ldap object
     */""",
                ),
            ],
        ),
    )

    counts[FILES[11]] = write_analyzed(
        FILES[11],
        patch(
            FILES[11],
            [
                (
                    """/**
 * <p>A {@link Condition} is used to specify how a specific query parameter
 * is defined in order to filter query results.</p>
 *
 * @author Pedro Igor
 */
public interface Condition""",
                    """/**
 * LDAP 过滤器条件：描述查询参数如何编码为 LDAP search filter 片段。
 *
 * @author Pedro Igor
 */
public interface Condition""",
                ),
                (
                    """    /**
     * Will change the parameter name if it is "modelParamName" to "ldapParamName" . Implementation can apply this to subconditions as well.
     *
     * It is used to update LDAP queries, which were created with model parameter name ( for example "firstName" ) and rewrite them to use real
     * LDAP mapped attribute (for example "givenName" )
     */""",
                    """    /**
     * 将模型参数名（如 firstName）重写为 LDAP 属性名（如 givenName）；复合条件应递归更新子条件。
     *
     * Will change the parameter name if it is "modelParamName" to "ldapParamName" . Implementation can apply this to subconditions as well.
     * It is used to update LDAP queries, which were created with model parameter name ( for example "firstName" ) and rewrite them to use real
     * LDAP mapped attribute (for example "givenName" )
     */""",
                ),
            ],
        ),
    )

    counts[FILES[12]] = write_analyzed(
        FILES[12],
        patch(
            FILES[12],
            [
                (
                    """/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public enum EscapeStrategy""",
                    """/**
 * LDAP 过滤器值的转义策略：默认 UTF-8 转义或八位组（OCTET_STRING）十六进制形式。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public enum EscapeStrategy""",
                ),
                (
                    """    /**
     * Escaping of LDAP special characters including non-ASCII characters like é.
     */""",
                    """    /** 转义 LDAP 特殊字符及非 ASCII 字符（如 é）。 */""",
                ),
                (
                    "    // Escaping value as Octet-String",
                    "    // 按八位组（Octet-String）十六进制转义",
                ),
            ],
        ),
    )

    counts[FILES[13]] = write_analyzed(
        FILES[13],
        patch(
            FILES[13],
            [
                (
                    """/**
 * @author Pedro Igor
 */
public class Sort""",
                    """/**
 * LDAP 查询结果排序描述：属性名与升序/降序标志。
 *
 * @author Pedro Igor
 */
public class Sort""",
                ),
                (
                    "    public String getParameter() {",
                    "    /** 排序依据的 LDAP/模型属性名。 */\n    public String getParameter() {",
                ),
                (
                    "    public boolean isAscending() {",
                    "    /** 是否升序排序。 */\n    public boolean isAscending() {",
                ),
            ],
        ),
    )

    counts[FILES[14]] = write_analyzed(
        FILES[14],
        patch(
            FILES[14],
            [
                (
                    """/**
 * <p>And condition for filters.</p>
 *
 * @author rmartinc
 */
public class AndCondition implements Condition""",
                    """/**
 * LDAP 逻辑与（{@code &}）复合条件，依次拼接各子条件的 filter 片段。
 *
 * @author rmartinc
 */
public class AndCondition implements Condition""",
                ),
                (
                    "    public AndCondition(Condition... innerConditions) {",
                    "    /** 由多个子条件构成 AND 过滤器。 */\n    public AndCondition(Condition... innerConditions) {",
                ),
            ],
        ),
    )

    return counts


def git_annotate_commit(counts: dict[str, int]) -> tuple[str, int]:
    env = {"GIT_INDEX_FILE": "/tmp/kc22a_ann.index"}
    subprocess.run(["git", "fetch", "origin", "main"], cwd=ROOT, check=True)
    tip = subprocess.check_output(["git", "rev-parse", "origin/main"], cwd=ROOT, text=True).strip()
    done_blob_tip = subprocess.check_output(
        ["git", "show", f"{tip}:keycloak/26.7.1/_reports/class-queue/done.txt"],
        cwd=ROOT,
        text=True,
    )

    subprocess.run(["git", "read-tree", "origin/main"], cwd=ROOT, env=env, check=True)

    for rel in FILES:
        blob = subprocess.check_output(
            ["git", "hash-object", "-w", str(ANAL / rel)], cwd=ROOT, text=True
        ).strip()
        subprocess.run(
            [
                "git",
                "update-index",
                "--add",
                "--cacheinfo",
                f"100644,{blob},keycloak/26.7.1/analyzed/{rel}",
            ],
            cwd=ROOT,
            env=env,
            check=True,
        )

    staged = subprocess.check_output(
        ["git", "diff-index", "--cached", "--name-only", "origin/main"],
        cwd=ROOT,
        env=env,
        text=True,
    ).strip().split("\n")
    staged = [s for s in staged if s]
    expected = {f"keycloak/26.7.1/analyzed/{rel}" for rel in FILES}
    if set(staged) != expected:
        raise SystemExit(f"ABORT: staged paths mismatch: {staged}")

    tree = subprocess.check_output(["git", "write-tree"], cwd=ROOT, env=env, text=True).strip()
    tree_count = int(
        subprocess.check_output(["git", "ls-tree", "-r", tree], cwd=ROOT, text=True).count("\n")
    )
    if tree_count < 110986:
        raise SystemExit(f"ABORT: tree count {tree_count} < 110986")

    msg = "docs(keycloak): annotate wave22a [0:15] ldap storage provider + idm"
    sha = subprocess.check_output(
        ["git", "commit-tree", tree, "-p", tip, "-m", msg],
        cwd=ROOT,
        env=env,
        text=True,
    ).strip()

    subprocess.run(["git", "fetch", "origin", "main"], cwd=ROOT, check=True)
    new_tip = subprocess.check_output(["git", "rev-parse", "origin/main"], cwd=ROOT, text=True).strip()
    if new_tip != tip:
        done_blob_new = subprocess.check_output(
            ["git", "show", f"{new_tip}:keycloak/26.7.1/_reports/class-queue/done.txt"],
            cwd=ROOT,
            text=True,
        )
        if done_blob_new != done_blob_tip:
            raise SystemExit("ABORT_DONE_WIPE")
        subprocess.run(["git", "read-tree", "origin/main"], cwd=ROOT, env=env, check=True)
        for rel in FILES:
            blob = subprocess.check_output(
                ["git", "hash-object", "-w", str(ANAL / rel)], cwd=ROOT, text=True
            ).strip()
            subprocess.run(
                [
                    "git",
                    "update-index",
                    "--add",
                    "--cacheinfo",
                    f"100644,{blob},keycloak/26.7.1/analyzed/{rel}",
                ],
                cwd=ROOT,
                env=env,
                check=True,
            )
        tree = subprocess.check_output(["git", "write-tree"], cwd=ROOT, env=env, text=True).strip()
        tree_count = int(
            subprocess.check_output(["git", "ls-tree", "-r", tree], cwd=ROOT, text=True).count("\n")
        )
        sha = subprocess.check_output(
            ["git", "commit-tree", tree, "-p", new_tip, "-m", msg],
            cwd=ROOT,
            env=env,
            text=True,
        ).strip()

    subprocess.run(["git", "push", "origin", f"{sha}:refs/heads/main"], cwd=ROOT, check=True)
    return sha, tree_count


def git_mark_done() -> tuple[str, int, int]:
    subprocess.run(["git", "fetch", "origin", "main"], cwd=ROOT, check=True)
    subprocess.run(
        [
            "python3",
            "scripts/mark_batch_done.py",
            "--project",
            "keycloak",
            "--version",
            "26.7.1",
            "--note",
            "wave22a ldap storage provider + idm",
            *FILES,
        ],
        cwd=ROOT,
        check=True,
    )
    env = {"GIT_INDEX_FILE": "/tmp/kc22a_mark.index"}
    tip = subprocess.check_output(["git", "rev-parse", "origin/main"], cwd=ROOT, text=True).strip()
    subprocess.run(["git", "read-tree", tip], cwd=ROOT, env=env, check=True)
    for fname in ("done.txt", "worker.log"):
        rel = f"keycloak/26.7.1/_reports/class-queue/{fname}"
        blob = subprocess.check_output(
            ["git", "hash-object", "-w", str(ROOT / rel)], cwd=ROOT, text=True
        ).strip()
        subprocess.run(
            ["git", "update-index", "--add", "--cacheinfo", f"100644,{blob},{rel}"],
            cwd=ROOT,
            env=env,
            check=True,
        )
    staged = subprocess.check_output(
        ["git", "diff-index", "--cached", "--name-only", tip],
        cwd=ROOT,
        env=env,
        text=True,
    ).strip().split("\n")
    staged = [s for s in staged if s]
    expected = {
        "keycloak/26.7.1/_reports/class-queue/done.txt",
        "keycloak/26.7.1/_reports/class-queue/worker.log",
    }
    if set(staged) != expected:
        raise SystemExit(f"ABORT: mark staged paths mismatch: {staged}")
    for s in staged:
        if "analyzed/" in s:
            raise SystemExit(f"ABORT: analyzed/ in mark commit: {s}")
    tree = subprocess.check_output(["git", "write-tree"], cwd=ROOT, env=env, text=True).strip()
    sha = subprocess.check_output(
        [
            "git",
            "commit-tree",
            tree,
            "-p",
            tip,
            "-m",
            "queue: mark keycloak wave22a ldap storage provider + idm done",
        ],
        cwd=ROOT,
        env=env,
        text=True,
    ).strip()
    subprocess.run(["git", "push", "origin", f"{sha}:refs/heads/main"], cwd=ROOT, check=True)
    done_n = len(
        (ROOT / "keycloak/26.7.1/_reports/class-queue/done.txt").read_text(encoding="utf-8").splitlines()
    )
    pending_n = len(
        (ROOT / "keycloak/26.7.1/_reports/class-queue/pending.txt").read_text(encoding="utf-8").splitlines()
    )
    return sha, done_n, pending_n


def main() -> int:
    counts = annotate_all()
    annotate_sha, tree_count = git_annotate_commit(counts)
    queue_sha, done_n, pending_n = git_mark_done()
    all_chinese = all(v > 0 for v in counts.values())
    out = {
        "annotate_sha": annotate_sha,
        "queue_sha": queue_sha,
        "tree_count": tree_count,
        "per_file_cjk_han": counts,
        "all_chinese": all_chinese,
        "diff_tree_count": 15,
        "done": done_n,
        "pending": pending_n,
    }
    print(json.dumps(out, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
