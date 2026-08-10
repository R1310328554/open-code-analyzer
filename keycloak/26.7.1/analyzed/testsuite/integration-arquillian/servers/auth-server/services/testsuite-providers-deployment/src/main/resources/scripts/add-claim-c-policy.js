// 测试策略：向权限对象添加 claim-c 声明后授予访问
$evaluation.getPermission().addClaim('claim-c', 'claim-c');$evaluation.grant();