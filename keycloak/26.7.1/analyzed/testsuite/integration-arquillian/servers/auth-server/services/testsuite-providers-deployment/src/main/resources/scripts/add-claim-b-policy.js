// 测试策略：向权限对象添加 claim-b 声明后授予访问
$evaluation.getPermission().addClaim('claim-b', 'claim-b');$evaluation.grant();