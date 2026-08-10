// 基于组完整路径的授权策略：alice 位于 /Group A/Group B/Group E 时授予
var realm = $evaluation.getRealm();

    // 按完整组路径匹配子组 Group E
if (realm.isUserInGroup('alice', '/Group A/Group B/Group E')) {
    $evaluation.grant();
}
