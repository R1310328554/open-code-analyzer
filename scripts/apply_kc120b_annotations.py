#!/usr/bin/env python3
"""Apply Chinese annotations to wave120b group/idp/session models — from original, idempotent."""
from __future__ import annotations

import shutil
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "keycloak/26.7.1/original"
ANAL = ROOT / "keycloak/26.7.1/analyzed"

BATCH = [
    ln.strip()
    for ln in Path("/tmp/kc120b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

ANNOTATORS: dict[str, object] = {}


def annotator(fn):
    ANNOTATORS[fn.__name__.removeprefix("annotate_")] = fn
    return fn


def copy_from_original(rel: str) -> Path:
    dst = ANAL / rel
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(ORIG / rel, dst)
    return dst


def replace_once(content: str, old: str, new: str) -> str:
    if new in content:
        return content
    if old not in content:
        raise ValueError(f"Pattern not found: {old[:120]!r}")
    return content.replace(old, new, 1)


# --- GroupModel ---


@annotator
def groupmodel(content: str) -> str:
    content = replace_once(
        content,
        "/**\n * @author <a href=\"mailto:bill@burkecentral.com\">Bill Burke</a>\n * @version $Revision: 1 $\n */",
        "/**\n * 用户组模型：表示 Realm 或组织内的层级组结构，支持角色映射与属性。\n"
        " * <p>继承 {@link RoleMapperModel} 与 {@link Model}，提供子组、事件与组织关联。</p>\n"
        " *\n * @author <a href=\"mailto:bill@burkecentral.com\">Bill Burke</a>\n * @version $Revision: 1 $\n */",
    )
    content = replace_once(
        content,
        "    enum Type {\n        REALM(0),\n        ORGANIZATION(1);",
        "    /** 组类型：Realm 级组或组织级组。 */\n    enum Type {\n        /** Realm 级组 */ REALM(0),\n        /** 组织级组 */ ORGANIZATION(1);",
    )
    content = replace_once(
        content,
        "    interface GroupEvent extends ProviderEvent {",
        "    /** 组生命周期相关事件的基接口。 */\n    interface GroupEvent extends ProviderEvent {",
    )
    content = replace_once(
        content,
        "    interface GroupCreatedEvent extends GroupEvent {",
        "    /** 组创建事件。 */\n    interface GroupCreatedEvent extends GroupEvent {",
    )
    content = replace_once(
        content,
        "    interface GroupRemovedEvent extends GroupEvent {",
        "    /** 组删除事件。 */\n    interface GroupRemovedEvent extends GroupEvent {",
    )
    content = replace_once(
        content,
        "    interface GroupUpdatedEvent extends GroupEvent {",
        "    /** 组更新事件。 */\n    interface GroupUpdatedEvent extends GroupEvent {",
    )
    content = replace_once(
        content,
        "    interface GroupMemberJoinEvent extends GroupEvent {",
        "    /** 用户加入组事件。 */\n    interface GroupMemberJoinEvent extends GroupEvent {",
    )
    content = replace_once(
        content,
        "    interface GroupMemberLeaveEvent extends GroupEvent {",
        "    /** 用户离开组事件。 */\n    interface GroupMemberLeaveEvent extends GroupEvent {",
    )
    content = replace_once(
        content,
        "    interface GroupPathChangeEvent extends GroupEvent {",
        "    /** 组路径变更事件（移动组层级时触发）。 */\n    interface GroupPathChangeEvent extends GroupEvent {",
    )
    content = replace_once(
        content,
        "    /**\n     * Get timestamp of group creation. May be null for groups created before this feature introduction.\n     */",
        "    /**\n     * 获取组创建时间戳；该功能引入前创建的组可能为 {@code null}。\n"
        "     * Get timestamp of group creation. May be null for groups created before this feature introduction.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Get timestamp of last group modification. May be null for groups that have not been modified\n     * since this feature was introduced.\n     */",
        "    /**\n     * 获取组最后修改时间戳；自该功能引入后未修改的组可能为 {@code null}。\n"
        "     * Get timestamp of last group modification. May be null for groups that have not been modified\n     * since this feature was introduced.\n     */",
    )
    content = replace_once(
        content,
        "    String getId();",
        "    /** @return 组唯一标识符 */\n    String getId();",
    )
    content = replace_once(
        content,
        "    String getName();",
        "    /** @return 组名称 */\n    String getName();",
    )
    content = replace_once(
        content,
        "    void setName(String name);",
        "    /** @param name 组名称 */\n    void setName(String name);",
    )
    content = replace_once(
        content,
        "    String getDescription();",
        "    /** @return 组描述 */\n    String getDescription();",
    )
    content = replace_once(
        content,
        "    void setDescription(String description);",
        "    /** @param description 组描述 */\n    void setDescription(String description);",
    )
    content = replace_once(
        content,
        "    /**\n     * Set single value of specified attribute. Remove all other existing values\n     *\n     * @param name\n     * @param value\n     */",
        "    /**\n     * 设置指定属性的单一值，并移除该属性的其他已有值。\n"
        "     * Set single value of specified attribute. Remove all other existing values\n     *\n     * @param name\n     * @param value\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * @param name\n     * @return null if there is not any value of specified attribute or first value otherwise. Don't throw exception if there are more values of the attribute\n     */",
        "    /**\n     * 获取指定属性的第一个值；无值时返回 {@code null}，多值时不抛异常。\n"
        "     * @param name\n     * @return null if there is not any value of specified attribute or first value otherwise. Don't throw exception if there are more values of the attribute\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns group attributes that match the given name as a stream.\n     * @param name {@code String} Name of the attribute to be used as a filter.\n     * @return Stream of all attribute values or empty stream if there are not any values. Never return {@code null}.\n     */",
        "    /**\n     * 以流形式返回匹配给定名称的组属性值。\n"
        "     * Returns group attributes that match the given name as a stream.\n"
        "     * @param name {@code String} Name of the attribute to be used as a filter.\n     * @return Stream of all attribute values or empty stream if there are not any values. Never return {@code null}.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns all sub groups for the parent group as a stream.\n     * The stream is sorted by the group name.\n     *\n     * @return Stream of {@link GroupModel}. Never returns {@code null}.\n     */",
        "    /**\n     * 返回父组下所有子组的流，按组名排序。\n"
        "     * Returns all sub groups for the parent group as a stream.\n     * The stream is sorted by the group name.\n     *\n     * @return Stream of {@link GroupModel}. Never returns {@code null}.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns all sub groups for the parent group matching the fuzzy search as a stream, paginated.\n     * Stream is sorted by the group name.\n     *\n     * @param search searched string. If empty or {@code null} all subgroups are returned.\n     * @return Stream of {@link GroupModel}. Never returns {@code null}.\n     */",
        "    /**\n     * 返回匹配模糊搜索的子组流（分页），按组名排序。\n"
        "     * Returns all sub groups for the parent group matching the fuzzy search as a stream, paginated.\n     * Stream is sorted by the group name.\n     *\n     * @param search searched string. If empty or {@code null} all subgroups are returned.\n     * @return Stream of {@link GroupModel}. Never returns {@code null}.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns all sub groups for the parent group as a stream, paginated.\n     *\n     * @param firstResult First result to return. Ignored if negative or {@code null}.\n     * @param maxResults Maximum number of results to return. Ignored if negative or {@code null}.\n     * @return\n     */",
        "    /**\n     * 返回父组下所有子组的流（分页）。\n"
        "     * Returns all sub groups for the parent group as a stream, paginated.\n     *\n     * @param firstResult First result to return. Ignored if negative or {@code null}.\n     * @param maxResults Maximum number of results to return. Ignored if negative or {@code null}.\n     * @return\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns all subgroups for the parent group matching the search as a stream, paginated.\n     * Stream is sorted by the group name.\n     *\n     * @param search search string. If empty or {@code null} all subgroups are returned.\n     * @param exact toggles fuzzy searching\n     * @param firstResult First result to return. Ignored if negative or {@code null}.\n     * @param maxResults Maximum number of results to return. Ignored if negative or {@code null}.\n     * @return Stream of {@link GroupModel}. Never returns {@code null}.\n     */",
        "    /**\n     * 返回匹配搜索条件的子组流（分页），按组名排序。\n"
        "     * Returns all subgroups for the parent group matching the search as a stream, paginated.\n     * Stream is sorted by the group name.\n     *\n     * @param search search string. If empty or {@code null} all subgroups are returned.\n     * @param exact toggles fuzzy searching\n     * @param firstResult First result to return. Ignored if negative or {@code null}.\n     * @param maxResults Maximum number of results to return. Ignored if negative or {@code null}.\n     * @return Stream of {@link GroupModel}. Never returns {@code null}.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns the number of groups contained beneath this group.\n     *\n     * @return The number of groups beneath this group. Never returns {@code null}.\n     */",
        "    /**\n     * 返回该组下子组的数量。\n"
        "     * Returns the number of groups contained beneath this group.\n     *\n     * @return The number of groups beneath this group. Never returns {@code null}.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * You must also call addChild on the parent group, addChild on RealmModel if there is no parent group\n     *\n     * @param group\n     */",
        "    /**\n     * 设置父组；同时需在父组上调用 {@code addChild}，若无父组则在 {@link RealmModel} 上调用。\n"
        "     * You must also call addChild on the parent group, addChild on RealmModel if there is no parent group\n     *\n     * @param group\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Automatically calls setParent() on the subGroup\n     *\n     * @param subGroup\n     */",
        "    /**\n     * 添加子组，并自动在子组上调用 {@code setParent()}。\n"
        "     * Automatically calls setParent() on the subGroup\n     *\n     * @param subGroup\n     */",
    )
    content = replace_once(
        content,
        "    void removeChild(GroupModel subGroup);",
        "    /**\n     * 移除子组，并自动在子组上调用 {@code setParent()}。\n"
        "     * Automatically calls setParent() on the subGroup\n     *\n     * @param subGroup\n     */\n    void removeChild(GroupModel subGroup);",
    )
    content = replace_once(
        content,
        "    /**\n     * @return Organization this group belongs to, or null if the group is of {@link Type#REALM}.\n     */",
        "    /**\n     * 返回组所属组织；{@link Type#REALM} 类型组返回 {@code null}。\n"
        "     * @return Organization this group belongs to, or null if the group is of {@link Type#REALM}.\n     */",
    )
    return content


# --- GroupProvider ---


@annotator
def groupprovider(content: str) -> str:
    content = replace_once(
        content,
        "/**\n *\n * Provider of group records\n * @author mhajas\n *\n */",
        "/**\n * 组数据提供者：负责 Realm 内用户组的 CRUD、查询与层级移动。\n"
        " * <p>Provider of group records</p>\n * @author mhajas\n *\n */",
    )
    content = replace_once(
        content,
        "    static boolean DEFAULT_ESCAPE_SLASHES = false;",
        "    /** 组路径中是否转义斜杠的默认值。 */\n    static boolean DEFAULT_ESCAPE_SLASHES = false;",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns groups for the given realm.\n     *\n     * @param realm Realm.\n     * @return Stream of groups in the Realm.\n     */",
        "    /**\n     * 返回指定 Realm 内的所有组。\n"
        "     * Returns groups for the given realm.\n     *\n     * @param realm Realm.\n     * @return Stream of groups in the Realm.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns a stream of groups with given ids.\n     * Effectively the same as {@code getGroupsStream(realm, ids, null, null, null)}.\n     *\n     * @param realm Realm.\n     * @param ids Stream of ids.\n     * @return Stream of GroupModels with the specified ids\n     */",
        "    /**\n     * 按 ID 流返回组，等价于 {@code getGroupsStream(realm, ids, null, null, null)}。\n"
        "     * Returns a stream of groups with given ids.\n     * Effectively the same as {@code getGroupsStream(realm, ids, null, null, null)}.\n     *\n     * @param realm Realm.\n     * @param ids Stream of ids.\n     * @return Stream of GroupModels with the specified ids\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns a paginated stream of groups with given ids and given search value in group names.\n     *\n     * @param realm Realm.\n     * @param ids Stream of ids.\n     * @param search Case insensitive string which will be searched for. Ignored if null.\n     * @param first Index of the first result to return. Ignored if negative or {@code null}.\n     * @param max Maximum number of results to return. Ignored if negative or {@code null}.\n     * @return Stream of desired groups. Never returns {@code null}.\n     */",
        "    /**\n     * 按 ID 与名称搜索条件返回分页组流。\n"
        "     * Returns a paginated stream of groups with given ids and given search value in group names.\n     *\n     * @param realm Realm.\n     * @param ids Stream of ids.\n     * @param search Case insensitive string which will be searched for. Ignored if null.\n     * @param first Index of the first result to return. Ignored if negative or {@code null}.\n     * @param max Maximum number of results to return. Ignored if negative or {@code null}.\n     * @return Stream of desired groups. Never returns {@code null}.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns a number of groups that contains the search string in the name\n     *\n     * @param realm Realm.\n     * @param ids List of ids.\n     * @param search Case insensitive string which will be searched for. Ignored if null.\n     * @return Number of groups.\n     */",
        "    /**\n     * 返回名称包含搜索字符串的组数量。\n"
        "     * Returns a number of groups that contains the search string in the name\n     *\n     * @param realm Realm.\n     * @param ids List of ids.\n     * @param search Case insensitive string which will be searched for. Ignored if null.\n     * @return Number of groups.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns a number of groups/top level groups (i.e. groups without parent group) for the given realm.\n     *\n     * @param realm Realm.\n     * @param onlyTopGroups When true the function returns a count of top level groups only.\n     * @return Number of groups/top level groups.\n     */",
        "    /**\n     * 返回 Realm 内组或顶级组（无父组）的数量。\n"
        "     * Returns a number of groups/top level groups (i.e. groups without parent group) for the given realm.\n     *\n     * @param realm Realm.\n     * @param onlyTopGroups When true the function returns a count of top level groups only.\n     * @return Number of groups/top level groups.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns groups with the given role in the given realm.\n     *\n     * @param realm Realm.\n     * @param role Role.\n     * @param firstResult First result to return. Ignored if negative or {@code null}.\n     * @param maxResults Maximum number of results to return. Ignored if negative or {@code null}.\n     * @return Stream of groups with the given role. Never returns {@code null}.\n     */",
        "    /**\n     * 返回拥有指定角色的组流。\n"
        "     * Returns groups with the given role in the given realm.\n     *\n     * @param realm Realm.\n     * @param role Role.\n     * @param firstResult First result to return. Ignored if negative or {@code null}.\n     * @param maxResults Maximum number of results to return. Ignored if negative or {@code null}.\n     * @return Stream of groups with the given role. Never returns {@code null}.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns all top level groups (i.e. groups without parent group) for the given realm.\n     *\n     * @param realm Realm.\n     * @return Stream of all top level groups in the realm. Never returns {@code null}.\n     */",
        "    /**\n     * 返回 Realm 内所有顶级组（无父组）。\n"
        "     * Returns all top level groups (i.e. groups without parent group) for the given realm.\n     *\n     * @param realm Realm.\n     * @return Stream of all top level groups in the realm. Never returns {@code null}.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Creates a new group with the given name in the given realm.\n     * Effectively the same as {@code createGroup(realm, null, name, null)}.\n     *\n     * @param realm Realm.\n     * @param name Name.\n     * @throws ModelDuplicateException If there is already a top-level group with the given name\n     * @return Model of the created group.\n     */",
        "    /**\n     * 在 Realm 中创建指定名称的新组，等价于 {@code createGroup(realm, null, name, null)}。\n"
        "     * Creates a new group with the given name in the given realm.\n     * Effectively the same as {@code createGroup(realm, null, name, null)}.\n     *\n     * @param realm Realm.\n     * @param name Name.\n     * @throws ModelDuplicateException If there is already a top-level group with the given name\n     * @return Model of the created group.\n     */",
    )
    content = replace_once(
        content,
        "    GroupModel createGroup(RealmModel realm, String id, Type type, String name, GroupModel toParent);",
        "    /**\n     * 创建指定类型、名称与父组的新组。\n     *\n     * @param realm Realm.\n     * @param id Id, will be generated if {@code null}.\n     * @param type the group type. if not set, defaults to {@link Type#REALM}\n     * @param name Name.\n     * @param toParent Parent group, or {@code null} if the group is top level group\n     * @throws ModelDuplicateException If a group with the given id already exists or the toParent group has a subgroup with the given name\n     * @return Model of the created group\n     */\n    GroupModel createGroup(RealmModel realm, String id, Type type, String name, GroupModel toParent);",
    )
    content = replace_once(
        content,
        "    /**\n     * Removes the given group for the given realm.\n     *\n     * @param realm Realm.\n     * @param group Group.\n     * @return true if the group was removed, false if group doesn't exist or doesn't belong to the given realm\n     */",
        "    /**\n     * 从 Realm 中移除指定组。\n"
        "     * Removes the given group for the given realm.\n     *\n     * @param realm Realm.\n     * @param group Group.\n     * @return true if the group was removed, false if group doesn't exist or doesn't belong to the given realm\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * This method is used for moving groups in group structure, for example:\n     * <ul>\n     * <li>making an existing child group child group of some other group,</li>\n     * <li>setting a top level group (i.e. group without parent group) child of some group,</li>\n     * <li>making a child group top level group (i.e. removing its parent group).</li>\n     * <ul/>\n     *\n     * @param realm Realm owning this group.\n     * @param group Group to update.\n     * @param toParent New parent group, or {@code null} if we are moving the group to top level group.\n     * @throws ModelDuplicateException If there is already a group with group.name under the toParent group (or top-level if toParent is null)\n     */",
        "    /**\n     * 在组层级结构中移动组（变更父组或提升为顶级组）。\n"
        "     * This method is used for moving groups in group structure, for example:\n     * <ul>\n     * <li>making an existing child group child group of some other group,</li>\n     * <li>setting a top level group (i.e. group without parent group) child of some group,</li>\n     * <li>making a child group top level group (i.e. removing its parent group).</li>\n     * <ul/>\n     *\n     * @param realm Realm owning this group.\n     * @param group Group to update.\n     * @param toParent New parent group, or {@code null} if we are moving the group to top level group.\n     * @throws ModelDuplicateException If there is already a group with group.name under the toParent group (or top-level if toParent is null)\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Called when a realm is removed.\n     * Should remove all groups that belong to the realm.\n     *\n     * @param realm a reference to the realm\n     */",
        "    /**\n     * Realm 删除前的清理回调，应移除该 Realm 下所有组。\n"
        "     * Called when a realm is removed.\n     * Should remove all groups that belong to the realm.\n     *\n     * @param realm a reference to the realm\n     */",
    )
    return content


# --- IdentityProviderCapability ---


@annotator
def identityprovidercapability(content: str) -> str:
    content = replace_once(
        content,
        "public enum IdentityProviderCapability {\n\n    USER_LINKING\n\n}",
        "/**\n * 身份提供方能力枚举：描述 IdP 实例支持的功能特性。\n */\npublic enum IdentityProviderCapability {\n\n    /** 支持用户账号关联（联邦登录后绑定本地用户） */ USER_LINKING\n\n}",
    )
    return content


# --- IdentityProviderMapperModel ---


@annotator
def identityprovidermappermodel(content: str) -> str:
    content = replace_once(
        content,
        "/**\n * Specifies a mapping from broker login to user data.\n *\n * @author <a href=\"mailto:bill@burkecentral.com\">Bill Burke</a>\n * @version $Revision: 1 $\n */",
        "/**\n * 身份提供方映射器模型：定义联邦登录到本地用户数据的属性映射规则。\n"
        " * Specifies a mapping from broker login to user data.\n *\n * @author <a href=\"mailto:bill@burkecentral.com\">Bill Burke</a>\n * @version $Revision: 1 $\n */",
    )
    content = replace_once(
        content,
        "    public static final String SYNC_MODE = \"syncMode\";",
        "    /** 同步模式配置键。 */\n    public static final String SYNC_MODE = \"syncMode\";",
    )
    content = replace_once(
        content,
        "    public String getId() {",
        "    /** @return 映射器唯一标识符 */\n    public String getId() {",
    )
    content = replace_once(
        content,
        "    public String getName() {",
        "    /** @return 映射器显示名称 */\n    public String getName() {",
    )
    content = replace_once(
        content,
        "    public String getIdentityProviderAlias() {",
        "    /** @return 所属身份提供方别名 */\n    public String getIdentityProviderAlias() {",
    )
    content = replace_once(
        content,
        "    public String getIdentityProviderMapper() {",
        "    /** @return 映射器实现类型 ID */\n    public String getIdentityProviderMapper() {",
    )
    content = replace_once(
        content,
        "    public IdentityProviderMapperSyncMode getSyncMode() {",
        "    /** @return 用户属性同步模式 */\n    public IdentityProviderMapperSyncMode getSyncMode() {",
    )
    content = replace_once(
        content,
        "    public Map<String, String> getConfig() {",
        "    /** @return 映射器配置项 */\n    public Map<String, String> getConfig() {",
    )
    content = replace_once(
        content,
        "    public Map<String, List<String>> getConfigMap(String configKey) {",
        "    /** @param configKey 配置键\n     * @return 反序列化后的多值配置映射 */\n    public Map<String, List<String>> getConfigMap(String configKey) {",
    )
    return content


# --- IdentityProviderMapperSyncMode ---


@annotator
def identityprovidermappersyncmode(content: str) -> str:
    content = replace_once(
        content,
        "public enum IdentityProviderMapperSyncMode {\n    INHERIT, LEGACY, IMPORT, FORCE\n}",
        "/**\n * 身份提供方映射器同步模式。\n */\npublic enum IdentityProviderMapperSyncMode {\n    /** 继承 IdP 级同步模式 */ INHERIT,\n    /** 遗留模式 */ LEGACY,\n    /** 导入模式：首次登录导入属性 */ IMPORT,\n    /** 强制模式：每次登录强制同步 */ FORCE\n}",
    )
    return content


# --- IdentityProviderModel ---


@annotator
def identityprovidermodel(content: str) -> str:
    content = replace_once(
        content,
        "/**\n * <p>A model type representing the configuration for identity providers. It provides some common properties and also a {@link org.keycloak.models.IdentityProviderModel#config}\n * for configuration options and properties specifics to a identity provider.</p>\n *\n * @author Pedro Igor\n */",
        "/**\n * 身份提供方配置模型：封装 IdP 通用属性及实现特定的 {@link org.keycloak.models.IdentityProviderModel#config} 配置项。\n"
        " * <p>A model type representing the configuration for identity providers. It provides some common properties and also a {@link org.keycloak.models.IdentityProviderModel#config}\n * for configuration options and properties specifics to a identity provider.</p>\n *\n * @author Pedro Igor\n */",
    )
    content = replace_once(
        content,
        "    /**\n     * <p>An user-defined identifier to unique identify an identity provider instance.</p>\n     */",
        "    /**\n     * 用户定义的别名，用于唯一标识一个 IdP 实例。\n     * <p>An user-defined identifier to unique identify an identity provider instance.</p>\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * <p>An identifier used to reference a specific identity provider implementation. The value of this field is the same\n     * across instances of the same provider implementation.</p>\n     */",
        "    /**\n     * 引用具体 IdP 实现类型的标识符，同一实现的所有实例共享该值。\n"
        "     * <p>An identifier used to reference a specific identity provider implementation. The value of this field is the same\n     * across instances of the same provider implementation.</p>\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Specifies if particular provider should be used by default for authentication even before displaying login screen\n     */",
        "    /**\n     * 是否在显示登录页之前默认使用该 IdP 进行认证。\n"
        "     * Specifies if particular provider should be used by default for authentication even before displaying login screen\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * <p>A map containing the configuration and properties for a specific identity provider instance and implementation. The items\n     * in the map are understood by the identity provider implementation.</p>\n     */",
        "    /**\n     * 存储 IdP 实例与实现特定的配置项，由具体 IdP 实现解析。\n"
        "     * <p>A map containing the configuration and properties for a specific identity provider instance and implementation. The items\n     * in the map are understood by the identity provider implementation.</p>\n     */",
    )
    content = replace_once(
        content,
        "    public String getInternalId() {",
        "    /** @return 内部持久化 ID */\n    public String getInternalId() {",
    )
    content = replace_once(
        content,
        "    public String getAlias() {",
        "    /** @return IdP 别名 */\n    public String getAlias() {",
    )
    content = replace_once(
        content,
        "    public String getProviderId() {",
        "    /** @return IdP 实现类型 ID */\n    public String getProviderId() {",
    )
    content = replace_once(
        content,
        "    public boolean isEnabled() {",
        "    /** @return 是否已启用 */\n    public boolean isEnabled() {",
    )
    content = replace_once(
        content,
        "    public boolean isStoreTokenInSession() {",
        "    /** @return 是否在会话中存储联邦令牌（API v2 特性） */\n    public boolean isStoreTokenInSession() {",
    )
    content = replace_once(
        content,
        "    /**\n     * <p>Validates this configuration.\n     *\n     * <p>Sub-classes can override this method in order to enforce provider specific validations.\n     *\n     * @param realm the realm\n     */",
        "    /**\n     * 校验 IdP 配置；子类可覆盖以添加实现特定校验。\n"
        "     * <p>Validates this configuration.\n     *\n     * <p>Sub-classes can override this method in order to enforce provider specific validations.\n     *\n     * @param realm the realm\n     */",
    )
    content = replace_once(
        content,
        "    public IdentityProviderSyncMode getSyncMode() {",
        "    /** @return IdP 级用户同步模式 */\n    public IdentityProviderSyncMode getSyncMode() {",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns flag whether the users within this IdP should be transient, ie. not stored in Keycloak database.\n     * Default value: {@code false}.\n     * @return\n     */",
        "    /**\n     * 该 IdP 用户是否为临时用户（不写入 Keycloak 数据库），默认 {@code false}。\n"
        "     * Returns flag whether the users within this IdP should be transient, ie. not stored in Keycloak database.\n     * Default value: {@code false}.\n     * @return\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Configures the IdP to not store users in Keycloak database. Default value: {@code false}.\n     * @return\n     */",
        "    /**\n     * 配置 IdP 不将用户持久化到 Keycloak 数据库，默认 {@code false}。\n"
        "     * Configures the IdP to not store users in Keycloak database. Default value: {@code false}.\n     * @return\n     */",
    )
    content = replace_once(
        content,
        "\tpublic IdentityProviderShowInAccountConsole getShowInAccountConsole() {",
        "\t/** @return 在账户控制台中的显示策略 */\n\tpublic IdentityProviderShowInAccountConsole getShowInAccountConsole() {",
    )
    return content


# --- IdentityProviderQuery ---


@annotator
def identityproviderquery(content: str) -> str:
    content = replace_once(
        content,
        "public class IdentityProviderQuery {",
        "/**\n * 身份提供方查询构建器：按类型、能力与附加选项筛选 IdP。\n */\npublic class IdentityProviderQuery {",
    )
    content = replace_once(
        content,
        "    public static IdentityProviderQuery any() {",
        "    /** @return 匹配任意类型 IdP 的查询 */\n    public static IdentityProviderQuery any() {",
    )
    content = replace_once(
        content,
        "    public static IdentityProviderQuery userAuthentication() {",
        "    /** @return 用户认证类 IdP 查询（默认联邦登录场景） */\n    public static IdentityProviderQuery userAuthentication() {",
    )
    content = replace_once(
        content,
        "    public static IdentityProviderQuery type(IdentityProviderType type) {",
        "    /** @param type IdP 类型\n     * @return 按类型筛选的查询 */\n    public static IdentityProviderQuery type(IdentityProviderType type) {",
    )
    content = replace_once(
        content,
        "    public static IdentityProviderQuery capability(IdentityProviderCapability capability) {",
        "    /** @param capability IdP 能力\n     * @return 按能力筛选的查询 */\n    public static IdentityProviderQuery capability(IdentityProviderCapability capability) {",
    )
    content = replace_once(
        content,
        "    public IdentityProviderQuery with(String key, String value) {",
        "    /** @param key 搜索选项键\n     * @param value 搜索选项值\n     * @return 当前查询（链式） */\n    public IdentityProviderQuery with(String key, String value) {",
    )
    content = replace_once(
        content,
        "    public IdentityProviderQuery with(Map<String, String> options) {",
        "    /** @param options 搜索选项映射\n     * @return 当前查询（链式） */\n    public IdentityProviderQuery with(Map<String, String> options) {",
    )
    content = replace_once(
        content,
        "    public IdentityProviderType getType() {",
        "    /** @return IdP 类型筛选条件 */\n    public IdentityProviderType getType() {",
    )
    content = replace_once(
        content,
        "    public IdentityProviderCapability getCapability() {",
        "    /** @return IdP 能力筛选条件 */\n    public IdentityProviderCapability getCapability() {",
    )
    content = replace_once(
        content,
        "    public Map<String, String> getOptions() {",
        "    /** @return 附加搜索选项 */\n    public Map<String, String> getOptions() {",
    )
    return content


# --- IdentityProviderShowInAccountConsole ---


@annotator
def identityprovidershowinaccountconsole(content: str) -> str:
    content = replace_once(
        content,
        "/**\n * @author Réda Housni Alaoui\n */",
        "/**\n * 控制 IdP 在账户控制台中的可见性策略。\n *\n * @author Réda Housni Alaoui\n */",
    )
    content = replace_once(
        content,
        "public enum IdentityProviderShowInAccountConsole {\n\tALWAYS,\n\tWHEN_LINKED,\n\tNEVER\n}",
        "public enum IdentityProviderShowInAccountConsole {\n\t/** 始终显示 */ ALWAYS,\n\t/** 仅已关联时显示 */ WHEN_LINKED,\n\t/** 从不显示 */ NEVER\n}",
    )
    return content


# --- IdentityProviderStorageProvider ---


@annotator
def identityproviderstorageprovider(content: str) -> str:
    content = replace_once(
        content,
        "/**\n * The {@link IdentityProviderStorageProvider} is concerned with the storage/retrieval of the configured identity providers\n * in Keycloak. In other words, it is a provider of identity providers (IDPs) and, as such, handles the CRUD operations for IDPs.\n * </p>\n * It is not to be confused with the {@code IdentityProvider} found in server-spi-private as that provider is meant to be\n * implemented by actual identity providers that handle the logic of authenticating users with third party brokers, such\n * as Microsoft, Google, GitHub, LinkedIn, etc.\n *\n * @author <a href=\"mailto:sguilhen@redhat.com\">Stefan Guilhen</a>\n */",
        "/**\n * 身份提供方存储提供者：负责 Keycloak 中已配置 IdP 的持久化 CRUD 与查询。\n"
        " * <p>The {@link IdentityProviderStorageProvider} is concerned with the storage/retrieval of the configured identity providers\n * in Keycloak. In other words, it is a provider of identity providers (IDPs) and, as such, handles the CRUD operations for IDPs.\n * </p>\n * <p>勿与 server-spi-private 中的 {@code IdentityProvider} 混淆——后者由 Google、GitHub 等具体联邦实现。</p>\n"
        " * It is not to be confused with the {@code IdentityProvider} found in server-spi-private as that provider is meant to be\n * implemented by actual identity providers that handle the logic of authenticating users with third party brokers, such\n * as Microsoft, Google, GitHub, LinkedIn, etc.\n *\n * @author <a href=\"mailto:sguilhen@redhat.com\">Stefan Guilhen</a>\n */",
    )
    content = replace_once(
        content,
        "    /**\n     * Creates a new identity provider from the specified model.\n     *\n     * @param model a {@link IdentityProviderModel} containing the identity provider's data.\n     * @return the model of the created identity provider.\n     */",
        "    /**\n     * 根据模型创建新 IdP。\n"
        "     * Creates a new identity provider from the specified model.\n     *\n     * @param model a {@link IdentityProviderModel} containing the identity provider's data.\n     * @return the model of the created identity provider.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Updates the identity provider using the specified model.\n     *\n     * @param model a {@link IdentityProviderModel} containing the identity provider's data.\n     */",
        "    /**\n     * 根据模型更新 IdP 配置。\n"
        "     * Updates the identity provider using the specified model.\n     *\n     * @param model a {@link IdentityProviderModel} containing the identity provider's data.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Removes the identity provider with the specified alias.\n     *\n     * @param providerAlias the alias of the identity provider to be removed.\n     * @return {@code true} if an IDP with the specified alias was found and removed; {@code false} otherwise.\n     */",
        "    /**\n     * 按别名删除 IdP。\n"
        "     * Removes the identity provider with the specified alias.\n     *\n     * @param providerAlias the alias of the identity provider to be removed.\n     * @return {@code true} if an IDP with the specified alias was found and removed; {@code false} otherwise.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Removes all identity providers from the realm.\n     */",
        "    /**\n     * 删除 Realm 内所有 IdP。\n     * Removes all identity providers from the realm.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Obtains the identity provider with the specified internal id.\n     *\n     * @param internalId the identity provider's internal id.\n     * @return a reference to the identity provider, or {@code null} if no provider is found.\n     */",
        "    /**\n     * 按内部 ID 获取 IdP。\n"
        "     * Obtains the identity provider with the specified internal id.\n     *\n     * @param internalId the identity provider's internal id.\n     * @return a reference to the identity provider, or {@code null} if no provider is found.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Obtains the identity provider with the specified alias.\n     *\n     * @param alias the identity provider's alias.\n     * @return a reference to the identity provider, or {@code null} if no provider is found.\n     */",
        "    /**\n     * 按别名获取 IdP。\n"
        "     * Obtains the identity provider with the specified alias.\n     *\n     * @param alias the identity provider's alias.\n     * @return a reference to the identity provider, or {@code null} if no provider is found.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns all identity providers in the current realm of a given type.\n     * @param query the query of identity provider to return\n     * @return a non-null stream of {@link IdentityProviderModel}s that match the specified type\n     */",
        "    /**\n     * 返回当前 Realm 中匹配查询条件的所有 IdP。\n"
        "     * Returns all identity providers in the current realm of a given type.\n     * @param query the query of identity provider to return\n     * @return a non-null stream of {@link IdentityProviderModel}s that match the specified type\n     */",
    )
    content = replace_once(
        content,
        "    Stream<IdentityProviderModel> getAllStream(IdentityProviderQuery query, Integer first, Integer max);",
        "    /**\n     * 按查询条件返回分页 IdP 流，支持别名、启用状态、配置属性等筛选。\n     *\n     * @param query the query for identity providers to match.\n     * @param first the position of the first result to be processed (pagination offset). Ignored if negative or {@code null}.\n     * @param max the maximum number of results to be returned. Ignored if negative or {@code null}.\n     * @return a non-null stream of {@link IdentityProviderModel}s that match the search criteria.\n     */\n    Stream<IdentityProviderModel> getAllStream(IdentityProviderQuery query, Integer first, Integer max);",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns all identity providers available for login, according to the specified mode. An IDP can be used for login\n     * if it is enabled, is not a link-only IDP, and is not configured to be hidden on login page.\n     * </p>\n     * The mode parameter may narrow the list of IDPs that are available. {@code FETCH_MODE.REALM_ONLY} fetches only realm-level\n     * IDPs (i.e. those not associated with any org). {@code FETCH_MODE.ORG_ONLY} will work together with the {@code organizationId}\n     * parameter. If the latter is set, only the IDPs associated with that org will be returned. Otherwise, the method returns\n     * the IDPs associated with any org. {@code FETCH_MODE.ALL} combines both approaches, returning both the realm-level\n     * IDPs with those associated with organizations (or a specific organization as per the {@code organizationId} param).\n     *\n     * @param mode the fetch mode to be used. Can be {@code REALM_ONLY}, {@code ORG_ONLY}, or {@code ALL}.\n     * @param organizationId an optional organization ID. If present and the mode is not {@code REALM_ONLY}, the param indicates\n     *                       that only IDPs associated with the specified organization are to be returned.\n     * @return a non-null stream of {@link IdentityProviderModel}s that are suitable for being displayed in the login pages.\n     */",
        "    /**\n     * 返回可用于登录页的 IdP（已启用、非仅关联、未隐藏）。\n"
        "     * Returns all identity providers available for login, according to the specified mode. An IDP can be used for login\n     * if it is enabled, is not a link-only IDP, and is not configured to be hidden on login page.\n     * </p>\n     * The mode parameter may narrow the list of IDPs that are available. {@code FETCH_MODE.REALM_ONLY} fetches only realm-level\n     * IDPs (i.e. those not associated with any org). {@code FETCH_MODE.ORG_ONLY} will work together with the {@code organizationId}\n     * parameter. If the latter is set, only the IDPs associated with that org will be returned. Otherwise, the method returns\n     * the IDPs associated with any org. {@code FETCH_MODE.ALL} combines both approaches, returning both the realm-level\n     * IDPs with those associated with organizations (or a specific organization as per the {@code organizationId} param).\n     *\n     * @param mode the fetch mode to be used. Can be {@code REALM_ONLY}, {@code ORG_ONLY}, or {@code ALL}.\n     * @param organizationId an optional organization ID. If present and the mode is not {@code REALM_ONLY}, the param indicates\n     *                       that only IDPs associated with the specified organization are to be returned.\n     * @return a non-null stream of {@link IdentityProviderModel}s that are suitable for being displayed in the login pages.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns the number of IDPs in the realm.\n     *\n     * @return the number of IDPs found in the realm.\n     */",
        "    /**\n     * 返回 Realm 内 IdP 总数。\n"
        "     * Returns the number of IDPs in the realm.\n     *\n     * @return the number of IDPs found in the realm.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Checks whether the realm has any configured identity providers or not.\n     *\n     * @return {@code true} if the realm has at least one configured identity provider (federation is enabled); {@code false}\n     * otherwise.\n     */",
        "    /**\n     * 检查 Realm 是否已配置 IdP（是否启用身份联邦）。\n"
        "     * Checks whether the realm has any configured identity providers or not.\n     *\n     * @return {@code true} if the realm has at least one configured identity provider (federation is enabled); {@code false}\n     * otherwise.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Enum to control how login identity providers should be fetched.\n     */",
        "    /**\n     * 控制登录页 IdP 获取范围（Realm 级、组织级或全部）。\n     * Enum to control how login identity providers should be fetched.\n     */",
    )
    content = replace_once(
        content,
        "        /** only realm-level providers should be fetched (not linked to any organization) **/\n        REALM_ONLY,\n        /** only providers linked to organizations should be fetched **/\n        ORG_ONLY,\n        /** all providers should fetched, regardless of being linked to an organization or not **/\n        ALL",
        "        /** 仅 Realm 级 IdP（未关联组织） */ REALM_ONLY,\n        /** 仅组织关联 IdP */ ORG_ONLY,\n        /** 全部 IdP */ ALL",
    )
    content = replace_once(
        content,
        "    /**\n     * Creates a new identity provider mapper from the specified model.\n     *\n     * @param model a {@link IdentityProviderMapperModel} containing the identity provider mapper's data.\n     * @return the model of the created identity provider mapper.\n     */",
        "    /**\n     * 创建 IdP 映射器。\n"
        "     * Creates a new identity provider mapper from the specified model.\n     *\n     * @param model a {@link IdentityProviderMapperModel} containing the identity provider mapper's data.\n     * @return the model of the created identity provider mapper.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns all identity provider mappers as a stream.\n     * @return Stream of {@link IdentityProviderMapperModel}. Never returns {@code null}.\n     */",
        "    /**\n     * 返回所有 IdP 映射器流。\n"
        "     * Returns all identity provider mappers as a stream.\n     * @return Stream of {@link IdentityProviderMapperModel}. Never returns {@code null}.\n     */",
    )
    return content


# --- IdentityProviderSyncMode ---


@annotator
def identityprovidersyncmode(content: str) -> str:
    content = replace_once(
        content,
        "public enum IdentityProviderSyncMode {\n    LEGACY, IMPORT, FORCE\n}",
        "/**\n * 身份提供方级用户属性同步模式。\n */\npublic enum IdentityProviderSyncMode {\n    /** 遗留模式 */ LEGACY,\n    /** 导入模式 */ IMPORT,\n    /** 强制同步模式 */ FORCE\n}",
    )
    return content


# --- IdentityProviderType ---


@annotator
def identityprovidertype(content: str) -> str:
    content = replace_once(
        content,
        "public enum IdentityProviderType {",
        "/**\n * 身份提供方类型：区分用户认证、客户端断言、信任材料等用途。\n */\npublic enum IdentityProviderType {",
    )
    content = replace_once(
        content,
        "    ANY,\n    USER_AUTHENTICATION(USER_LINKING),\n    CLIENT_ASSERTION,\n    TRUST_MATERIAL,\n    EXCHANGE_EXTERNAL_TOKEN(USER_LINKING),\n    JWT_AUTHORIZATION_GRANT(USER_LINKING);",
        "    /** 任意类型 */ ANY,\n    /** 用户认证（支持账号关联） */ USER_AUTHENTICATION(USER_LINKING),\n    /** 客户端断言 */ CLIENT_ASSERTION,\n    /** 信任材料 */ TRUST_MATERIAL,\n    /** 外部令牌交换 */ EXCHANGE_EXTERNAL_TOKEN(USER_LINKING),\n    /** JWT 授权授予 */ JWT_AUTHORIZATION_GRANT(USER_LINKING);",
    )
    content = replace_once(
        content,
        "    public Set<IdentityProviderCapability> getCapabilities() {",
        "    /** @return 该类型支持的能力集合 */\n    public Set<IdentityProviderCapability> getCapabilities() {",
    )
    return content


# --- IssuedVerifiableCredentialModel ---


@annotator
def issuedverifiablecredentialmodel(content: str) -> str:
    content = replace_once(
        content,
        "public class IssuedVerifiableCredentialModel {",
        "/**\n * 已签发可验证凭证（VC）记录模型，用于 OID4VCI 场景。\n */\npublic class IssuedVerifiableCredentialModel {",
    )
    content = replace_once(
        content,
        "    // This represents UUID of the client, which acts as OID4VCI wallet",
        "    // 表示作为 OID4VCI 钱包的客户端 UUID",
    )
    content = replace_once(
        content,
        "    public String getId() {",
        "    /** @return 记录唯一标识符 */\n    public String getId() {",
    )
    content = replace_once(
        content,
        "    public String getUserId() {",
        "    /** @return 持有该 VC 的用户 ID */\n    public String getUserId() {",
    )
    content = replace_once(
        content,
        "    public String getVerifiableCredentialId() {",
        "    /** @return 可验证凭证定义 ID */\n    public String getVerifiableCredentialId() {",
    )
    content = replace_once(
        content,
        "    public Long getIssuedAt() {",
        "    /** @return 签发时间戳（毫秒） */\n    public Long getIssuedAt() {",
    )
    content = replace_once(
        content,
        "    public Long getExpiresAt() {",
        "    /** @return 过期时间戳（毫秒） */\n    public Long getExpiresAt() {",
    )
    content = replace_once(
        content,
        "    public String getClientId() {",
        "    /** @return OID4VCI 钱包客户端 UUID */\n    public String getClientId() {",
    )
    content = replace_once(
        content,
        "    public String getRevision() {",
        "    /** @return 记录修订版本号 */\n    public String getRevision() {",
    )
    return content


# --- KeyManager ---


@annotator
def keymanager(content: str) -> str:
    content = replace_once(
        content,
        "/**\n * @author <a href=\"mailto:sthorger@redhat.com\">Stian Thorgersen</a>\n */",
        "/**\n * 密钥管理器：提供 Realm 级签名、加密与 HMAC 密钥的查询与获取。\n"
        " *\n * @author <a href=\"mailto:sthorger@redhat.com\">Stian Thorgersen</a>\n */",
    )
    content = replace_once(
        content,
        "    KeyWrapper getActiveKey(RealmModel realm, KeyUse use, String algorithm);",
        "    /** @param realm Realm\n     * @param use 密钥用途\n     * @param algorithm 算法名称\n     * @return 当前活动密钥 */\n    KeyWrapper getActiveKey(RealmModel realm, KeyUse use, String algorithm);",
    )
    content = replace_once(
        content,
        "    KeyWrapper getKey(RealmModel realm, String kid, KeyUse use, String algorithm);",
        "    /** @param realm Realm\n     * @param kid 密钥 ID\n     * @param use 密钥用途\n     * @param algorithm 算法名称\n     * @return 匹配的密钥包装器 */\n    KeyWrapper getKey(RealmModel realm, String kid, KeyUse use, String algorithm);",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns all {@code KeyWrapper} for the given realm.\n     * @param realm {@code RealmModel}.\n     * @return Stream of all {@code KeyWrapper} in the realm. Never returns {@code null}.\n     */",
        "    /**\n     * 返回 Realm 内所有密钥。\n"
        "     * Returns all {@code KeyWrapper} for the given realm.\n     * @param realm {@code RealmModel}.\n     * @return Stream of all {@code KeyWrapper} in the realm. Never returns {@code null}.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns all {@code KeyWrapper} for the given realm that match given criteria.\n     * @param realm {@code RealmModel}.\n     * @param use {@code KeyUse}.\n     * @param algorithm {@code String}.\n     * @return Stream of all {@code KeyWrapper} in the realm. Never returns {@code null}.\n     */",
        "    /**\n     * 按用途与算法筛选 Realm 密钥。\n"
        "     * Returns all {@code KeyWrapper} for the given realm that match given criteria.\n     * @param realm {@code RealmModel}.\n     * @param use {@code KeyUse}.\n     * @param algorithm {@code String}.\n     * @return Stream of all {@code KeyWrapper} in the realm. Never returns {@code null}.\n     */",
    )
    content = replace_once(
        content,
        "    class ActiveRsaKey {",
        "    /** 当前活动 RSA 密钥（含私钥、公钥与证书）。 */\n    class ActiveRsaKey {",
    )
    content = replace_once(
        content,
        "    class ActiveHmacKey {",
        "    /** 当前活动 HMAC 密钥。 */\n    class ActiveHmacKey {",
    )
    content = replace_once(
        content,
        "    class ActiveAesKey {",
        "    /** 当前活动 AES 密钥。 */\n    class ActiveAesKey {",
    )
    return content


# --- KeycloakContext ---


@annotator
def keycloakcontext(content: str) -> str:
    content = replace_once(
        content,
        "/**\n * @author <a href=\"mailto:sthorger@redhat.com\">Stian Thorgersen</a>\n */",
        "/**\n * Keycloak 请求上下文：封装当前 Realm、客户端、HTTP 请求/响应、URI 与用户会话等运行时状态。\n"
        " *\n * @author <a href=\"mailto:sthorger@redhat.com\">Stian Thorgersen</a>\n */",
    )
    content = replace_once(
        content,
        "    /**\n     * @throws ContextNotActiveException if no request is active and a non-full URL hostname is configured\n     */\n    URI getAuthServerUrl();",
        "    /**\n     * 获取认证服务器基础 URL。\n     * @throws ContextNotActiveException if no request is active and a non-full URL hostname is configured\n     */\n    URI getAuthServerUrl();",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns the URI assuming it is a frontend request. To resolve URI for a backend request use {@link #getUri(UrlType)}\n     *\n     * method calls on the returned {@link KeycloakUriInfo} may throw a {@link ContextNotActiveException} if no request is active\n     */\n    KeycloakUriInfo getUri();",
        "    /**\n     * 获取前端请求 URI；后端请求请使用 {@link #getUri(UrlType)}。\n"
        "     * Returns the URI assuming it is a frontend request. To resolve URI for a backend request use {@link #getUri(UrlType)}\n     *\n     * method calls on the returned {@link KeycloakUriInfo} may throw a {@link ContextNotActiveException} if no request is active\n     */\n    KeycloakUriInfo getUri();",
    )
    content = replace_once(
        content,
        "    RealmModel getRealm();",
        "    /** @return 当前 Realm */\n    RealmModel getRealm();",
    )
    content = replace_once(
        content,
        "    void setRealm(RealmModel realm);",
        "    /** @param realm 设置当前 Realm */\n    void setRealm(RealmModel realm);",
    )
    content = replace_once(
        content,
        "    ClientModel getClient();",
        "    /** @return 当前客户端 */\n    ClientModel getClient();",
    )
    content = replace_once(
        content,
        "    OrganizationModel getOrganization();",
        "    /** @return 当前组织上下文 */\n    OrganizationModel getOrganization();",
    )
    content = replace_once(
        content,
        "    /**\n     * If there is no active request, a {@link ClientConnection} will still be returned\n     */\n    ClientConnection getConnection();",
        "    /**\n     * 获取客户端连接信息；无活动请求时仍可返回 {@link ClientConnection}。\n"
        "     * If there is no active request, a {@link ClientConnection} will still be returned\n     */\n    ClientConnection getConnection();",
    )
    content = replace_once(
        content,
        "    Locale resolveLocale(UserModel user);",
        "    /** @param user 用户\n     * @return 解析后的区域设置 */\n    Locale resolveLocale(UserModel user);",
    )
    content = replace_once(
        content,
        "    /**\n     * Get current AuthenticationSessionModel, can be null out of the AuthenticationSession context.\n     *\n     * @return current AuthenticationSessionModel or null\n     */\n    AuthenticationSessionModel getAuthenticationSession();",
        "    /**\n     * 获取当前认证会话；认证会话上下文外可能为 {@code null}。\n"
        "     * Get current AuthenticationSessionModel, can be null out of the AuthenticationSession context.\n     *\n     * @return current AuthenticationSessionModel or null\n     */\n    AuthenticationSessionModel getAuthenticationSession();",
    )
    content = replace_once(
        content,
        "    /**\n     * If there is no active request, a {@link ContextNotActiveException} will be thrown\n     */\n    HttpRequest getHttpRequest();",
        "    /**\n     * 获取当前 HTTP 请求；无活动请求时抛出 {@link ContextNotActiveException}。\n"
        "     * If there is no active request, a {@link ContextNotActiveException} will be thrown\n     */\n    HttpRequest getHttpRequest();",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns a {@link Token} representing the bearer token used to authenticate and authorize the current request.\n     *\n     * @return the bearer token\n     */\n    Token getBearerToken();",
        "    /**\n     * 返回用于认证与授权当前请求的 Bearer 令牌。\n"
        "     * Returns a {@link Token} representing the bearer token used to authenticate and authorize the current request.\n     *\n     * @return the bearer token\n     */\n    Token getBearerToken();",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns the {@link UserModel} bound to this context. The user is first resolved from the {@link #getBearerToken()} set to this\n     * context, if any. Otherwise, it will be resolved from the {@link #getUserSession()} set to this context, if any.\n     *\n     * @return the {@link UserModel} bound to this context.\n     */\n    UserModel getUser();",
        "    /**\n     * 返回绑定到上下文的用户：优先从 Bearer 令牌解析，否则从用户会话解析。\n"
        "     * Returns the {@link UserModel} bound to this context. The user is first resolved from the {@link #getBearerToken()} set to this\n     * context, if any. Otherwise, it will be resolved from the {@link #getUserSession()} set to this context, if any.\n     *\n     * @return the {@link UserModel} bound to this context.\n     */\n    UserModel getUser();",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns the permissions evaluator that can be used to check if the current user has permissions to perform an action on realm resources.\n     * @return the permissions evaluator\n     */\n    Permissions getPermissions();",
        "    /**\n     * 返回权限评估器，用于检查当前用户对 Realm 资源的操作权限。\n"
        "     * Returns the permissions evaluator that can be used to check if the current user has permissions to perform an action on realm resources.\n     * @return the permissions evaluator\n     */\n    Permissions getPermissions();",
    )
    return content


# --- KeycloakSession ---


@annotator
def keycloaksession(content: str) -> str:
    content = replace_once(
        content,
        "/**\n * @author <a href=\"mailto:bill@burkecentral.com\">Bill Burke</a>\n * @version $Revision: 1 $\n */",
        "/**\n * Keycloak 会话：单次请求/事务的工作单元，提供 Provider 访问与上下文管理。\n"
        " * <p>实现 {@link AutoCloseable}，会话结束时释放资源。</p>\n"
        " *\n * @author <a href=\"mailto:bill@burkecentral.com\">Bill Burke</a>\n * @version $Revision: 1 $\n */",
    )
    content = replace_once(
        content,
        "    KeycloakContext getContext();",
        "    /** @return 当前请求上下文 */\n    KeycloakContext getContext();",
    )
    content = replace_once(
        content,
        "    KeycloakTransactionManager getTransactionManager();",
        "    /** @return 事务管理器 */\n    KeycloakTransactionManager getTransactionManager();",
    )
    content = replace_once(
        content,
        "    /**\n     * Get dedicated provider instance of provider type clazz that was created for this session.  If one hasn't been created yet,\n     * find the factory and allocate by calling ProviderFactory.create(KeycloakSession).  The provider to use is determined\n     * by the \"provider\" config entry in keycloak-server boot configuration. See the <a href=\"https://www.keycloak.org/docs/latest/server_development/index.html#_use_available_providers\">Server developer guide</a> for the details.\n     *\n     *\n     *\n     * @param clazz\n     * @param <T>\n     * @return\n     */",
        "    /**\n     * 获取本会话的 Provider 实例；若尚未创建则通过工厂分配。\n"
        "     * Get dedicated provider instance of provider type clazz that was created for this session.  If one hasn't been created yet,\n     * find the factory and allocate by calling ProviderFactory.create(KeycloakSession).  The provider to use is determined\n     * by the \"provider\" config entry in keycloak-server boot configuration. See the <a href=\"https://www.keycloak.org/docs/latest/server_development/index.html#_use_available_providers\">Server developer guide</a> for the details.\n     *\n     *\n     *\n     * @param clazz\n     * @param <T>\n     * @return\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns a component provider for a component from the realm that is relevant to this session.\n     * The relevant realm must be set prior to calling this method in the context, see {@link KeycloakContext#getRealm()}.\n     * @param <T>\n     * @param clazz\n     * @param componentId Component configuration\n     * @throws IllegalArgumentException If the realm is not set in the context.\n     * @return Provider configured according to the {@param componentId}, {@code null} if it cannot be instantiated.\n     */",
        "    /**\n     * 从当前 Realm 获取组件 Provider；调用前须在上下文中设置 Realm，见 {@link KeycloakContext#getRealm()}。\n"
        "     * Returns a component provider for a component from the realm that is relevant to this session.\n     * The relevant realm must be set prior to calling this method in the context, see {@link KeycloakContext#getRealm()}.\n     * @param <T>\n     * @param clazz\n     * @param componentId Component configuration\n     * @throws IllegalArgumentException If the realm is not set in the context.\n     * @return Provider configured according to the {@param componentId}, {@code null} if it cannot be instantiated.\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Invalidates intermediate states of the given objects, both immediately and at the end of this session.\n     * @param type Type of the objects to invalidate\n     * @param params Parameters used for the invalidation\n     */",
        "    /**\n     * 立即使给定对象的中间状态失效，并在会话结束时再次失效。\n"
        "     * Invalidates intermediate states of the given objects, both immediately and at the end of this session.\n     * @param type Type of the objects to invalidate\n     * @param params Parameters used for the invalidation\n     */",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns a managed provider instance.  Will start a provider transaction.  This transaction is managed by the KeycloakSession\n     * transaction.\n     *\n     * @return\n     * @throws IllegalStateException if transaction is not active\n     */\n    RealmProvider realms();",
        "    /**\n     * 返回受管 Realm Provider，并启动 Provider 事务（由 KeycloakSession 事务管理）。\n"
        "     * Returns a managed provider instance.  Will start a provider transaction.  This transaction is managed by the KeycloakSession\n     * transaction.\n     *\n     * @return\n     * @throws IllegalStateException if transaction is not active\n     */\n    RealmProvider realms();",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns a managed group provider instance.\n     *\n     * @return Currently used GroupProvider instance.\n     * @throws IllegalStateException if transaction is not active\n     */\n    GroupProvider groups();",
        "    /**\n     * 返回受管组 Provider 实例。\n"
        "     * Returns a managed group provider instance.\n     *\n     * @return Currently used GroupProvider instance.\n     * @throws IllegalStateException if transaction is not active\n     */\n    GroupProvider groups();",
    )
    content = replace_once(
        content,
        "    /**\n     * Returns the default IDP provider .\n     *\n     * @return the default IDP provider.\n     */\n    IdentityProviderStorageProvider identityProviders();",
        "    /**\n     * 返回默认 IdP 存储 Provider。\n"
        "     * Returns the default IDP provider .\n     *\n     * @return the default IDP provider.\n     */\n    IdentityProviderStorageProvider identityProviders();",
    )
    content = replace_once(
        content,
        "    /**\n     * A cached view of all users in system including  users loaded by UserStorageProviders\n     *\n     * @return UserProvider instance\n     */\n    UserProvider users();",
        "    /**\n     * 系统内所有用户的缓存视图，包含 UserStorageProvider 加载的用户。\n"
        "     * A cached view of all users in system including  users loaded by UserStorageProviders\n     *\n     * @return UserProvider instance\n     */\n    UserProvider users();",
    )
    content = replace_once(
        content,
        "    /**\n     * Key manager\n     *\n      * @return\n     */\n    KeyManager keys();",
        "    /**\n     * 密钥管理器。\n     * Key manager\n     *\n      * @return\n     */\n    KeyManager keys();",
    )
    content = replace_once(
        content,
        "    /**\n     * Theme manager\n     *\n     * @return\n     */\n    ThemeManager theme();",
        "    /**\n     * 主题管理器。\n     * Theme manager\n     *\n     * @return\n     */\n    ThemeManager theme();",
    )
    content = replace_once(
        content,
        "    /**\n     * Token manager\n     *\n     * @return\n     */\n    TokenManager tokens();",
        "    /**\n     * 令牌管理器。\n     * Token manager\n     *\n     * @return\n     */\n    TokenManager tokens();",
    )
    content = replace_once(
        content,
        "    /**\n     * Vault transcriber\n     */\n    VaultTranscriber vault();",
        "    /**\n     * 保险库转录器，用于读取密钥等敏感配置。\n     * Vault transcriber\n     */\n    VaultTranscriber vault();",
    )
    content = replace_once(
        content,
        "    /**\n     * Client Policy Manager\n     */\n    ClientPolicyManager clientPolicy();",
        "    /**\n     * 客户端策略管理器。\n     * Client Policy Manager\n     */\n    ClientPolicyManager clientPolicy();",
    )
    content = replace_once(
        content,
        "    boolean isClosed();",
        "    /** @return 会话是否已关闭 */\n    boolean isClosed();",
    )
    return content


def main() -> int:
    for rel in BATCH:
        stem = Path(rel).stem.lower()
        fn = ANNOTATORS.get(stem)
        if fn is None:
            raise SystemExit(f"No annotator for {rel} (stem={stem})")
        path = copy_from_original(rel)
        text = path.read_text(encoding="utf-8")
        path.write_text(fn(text), encoding="utf-8")
        print(f"annotated {rel}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
