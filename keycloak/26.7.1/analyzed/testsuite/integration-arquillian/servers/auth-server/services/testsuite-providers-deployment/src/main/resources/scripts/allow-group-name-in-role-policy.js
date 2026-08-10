// 基于组名的授权策略：用户 marta 属于 Group C 组时授予访问
var realm = $evaluation.getRealm();

// 按组短名称（非完整路径）检查用户是否为该组成员
if (realm.isUserInGroup('marta', 'Group C')) {
    $evaluation.grant();
}
