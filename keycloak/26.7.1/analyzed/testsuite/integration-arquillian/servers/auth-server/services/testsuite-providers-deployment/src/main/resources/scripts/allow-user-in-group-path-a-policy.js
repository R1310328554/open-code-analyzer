// 基于组路径的授权策略：marta 位于 /Group A 时授予访问
var realm = $evaluation.getRealm();

// 按完整路径 /Group A 检查用户 marta 是否为该组成员
if (realm.isUserInGroup('marta', '/Group A')) {
    $evaluation.grant();
}
