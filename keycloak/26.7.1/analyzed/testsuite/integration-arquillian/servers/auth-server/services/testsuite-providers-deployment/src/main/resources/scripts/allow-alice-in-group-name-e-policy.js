// 基于组名称的授权策略：alice 属于名为 Group E 的组时授予
var realm = $evaluation.getRealm();

    // 仅按组名匹配，不要求完整路径
if (realm.isUserInGroup('alice', 'Group E')) {
    $evaluation.grant();
}
