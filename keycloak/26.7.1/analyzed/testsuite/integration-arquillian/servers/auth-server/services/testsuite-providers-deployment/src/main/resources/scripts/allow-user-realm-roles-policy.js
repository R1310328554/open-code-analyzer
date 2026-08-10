// 基于用户领域角色列表的授权策略：marta 拥有 uma_authorization 与 role-a 时授予访问
var realm = $evaluation.getRealm();
var roles = realm.getUserRealmRoles('marta');

// 获取用户 marta 的全部领域角色，要求恰好 2 个且包含 uma_authorization 与 role-a
if (roles.size() == 2 && roles.contains('uma_authorization') && roles.contains('role-a')) {
    $evaluation.grant();
}

