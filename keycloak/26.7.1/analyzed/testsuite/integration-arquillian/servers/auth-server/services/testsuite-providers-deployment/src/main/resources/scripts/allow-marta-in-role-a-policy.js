// 基于用户领域角色的授权策略：marta 拥有 role-a 时授予访问
var realm = $evaluation.getRealm();

// 检查用户 marta 是否被分配了 role-a 领域角色
if (realm.isUserInRealmRole('marta', 'role-a')) {
    $evaluation.grant();
}
