// 基于资源属性的授权策略：资源具备指定属性组合时授予访问
var permission = $evaluation.getPermission();
var resource = permission.getResource();
var attributes = resource.getAttributes();

// 验证资源拥有 a1、a2 两个属性键，a1 含 2 个值，a2 首值为 '3'，并通过多种 API 交叉确认
if (attributes.size() == 2 && attributes.containsKey('a1') && attributes.containsKey('a2') && attributes.get('a1').size() == 2 && attributes.get('a2').get(0).equals('3') && resource.getAttribute('a1').size() == 2 && resource.getSingleAttribute('a2').equals('3')) {
    $evaluation.grant();
}

