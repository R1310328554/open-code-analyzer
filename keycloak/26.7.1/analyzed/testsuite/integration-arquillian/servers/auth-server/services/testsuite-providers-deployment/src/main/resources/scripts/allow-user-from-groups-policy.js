// 基于用户组成员关系的授权策略：jdoe 同时属于两个指定子组时授予访问
var realm = $evaluation.getRealm();
var groups = realm.getUserGroups('jdoe');

// 获取用户 jdoe 的全部组路径，要求恰好包含 /Group A/Group B 与 /Group A/Group D
if (groups.size() == 2 && groups.contains('/Group A/Group B') && groups.contains('/Group A/Group D')) {
    $evaluation.grant();
}
