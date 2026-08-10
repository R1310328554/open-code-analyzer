// 基于组角色的授权策略：子组 /Group A/Group D 拥有 role-a 时授予访问
var realm = $evaluation.getRealm();

// 检查指定路径的子组是否被分配了 role-a 领域角色
if (realm.isGroupInRole('/Group A/Group D', 'role-a')) {
    $evaluation.grant();
}

