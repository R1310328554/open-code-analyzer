// 基于组路径的授权策略：alice 位于 /Group E 时授予
var realm = $evaluation.getRealm();

    // 按完整路径 /Group E 匹配组成员
if (realm.isUserInGroup('alice', '/Group E')) {
    $evaluation.grant();
}
