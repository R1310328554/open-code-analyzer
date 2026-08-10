// 授权策略：alice 不在 /Group A 的直接成员中时授予（不含父组继承）
var realm = $evaluation.getRealm();

    // 第三个参数 false 表示不检查父组继承关系
if (!realm.isUserInGroup('alice', '/Group A', false)) {
    $evaluation.grant();
}
