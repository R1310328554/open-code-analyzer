// 基于组短名称的授权策略：marta 属于 Group A 组时授予访问
var realm = $evaluation.getRealm();

// 按组短名称（不含路径前缀）检查用户 marta 是否为 Group A 成员
if (realm.isUserInGroup('marta', 'Group A')) {
    $evaluation.grant();
}
