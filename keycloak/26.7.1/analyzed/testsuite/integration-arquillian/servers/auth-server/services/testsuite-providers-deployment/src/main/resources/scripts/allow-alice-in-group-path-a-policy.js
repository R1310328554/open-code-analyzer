// 基于组路径的授权策略：alice 位于 /Group A 时授予
var realm = $evaluation.getRealm();

    // 按完整路径 /Group A 匹配组成员
if (realm.isUserInGroup('alice', '/Group A')) {
    $evaluation.grant();
}
