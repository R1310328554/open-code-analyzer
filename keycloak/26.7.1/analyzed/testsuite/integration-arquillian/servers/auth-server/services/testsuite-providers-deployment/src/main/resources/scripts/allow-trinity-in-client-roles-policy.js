// 基于用户客户端角色的授权策略：trinity 在 role-mapping-client 中拥有 client-role-a 时授予
var realm = $evaluation.getRealm();

// 检查用户在指定客户端下是否被分配了 client-role-a 角色
if (realm.isUserInClientRole('trinity', 'role-mapping-client', 'client-role-a')) {
    $evaluation.grant();
}
