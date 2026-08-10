// 基于用户属性的授权策略：jdoe 具备指定自定义属性组合时授予访问
var realm = $evaluation.getRealm();
var attributes = realm.getUserAttributes('jdoe');

// 验证用户 jdoe 拥有 6 个属性键，其中 a1 含 2 个值、a2 首值为 '3'
if (attributes.size() == 6 && attributes.containsKey('a1') && attributes.containsKey('a2') && attributes.get('a1').size() == 2 && attributes.get('a2').get(0).equals('3')) {
    $evaluation.grant();
}
