// 基于用户领域角色的授权策略：trinity 拥有 client-role-b 时授予访问
var realm = $evaluation.getRealm();

// 检查用户 trinity 是否被分配了 client-role-b 领域角色（测试脚本命名沿用 client-role-b）
if (realm.isUserInRealmRole('trinity', 'client-role-b')) {
    $evaluation.grant();
}
