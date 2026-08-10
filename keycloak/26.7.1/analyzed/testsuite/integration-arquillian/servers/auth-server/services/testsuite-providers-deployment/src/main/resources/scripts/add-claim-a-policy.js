// 测试策略：向权限对象添加 claim-a 声明（含两个值）后授予访问
$evaluation.getPermission().addClaim('claim-a', 'claim-a');$evaluation.getPermission().addClaim('claim-a', 'claim-a1');$evaluation.grant();