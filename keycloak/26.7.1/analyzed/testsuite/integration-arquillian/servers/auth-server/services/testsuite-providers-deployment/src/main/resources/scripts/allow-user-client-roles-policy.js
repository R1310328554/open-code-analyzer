// 基于用户客户端角色列表的授权策略：trinity 在 role-mapping-client 中仅拥有 client-role-a 时授予
var realm = $evaluation.getRealm();
var roles = realm.getUserClientRoles('trinity', 'role-mapping-client');

// 获取用户在指定客户端下的全部角色，要求恰好 1 个且为 client-role-a
if (roles.size() == 1 && roles.contains('client-role-a')) {
    $evaluation.grant();
}