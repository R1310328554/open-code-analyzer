// 始终拒绝的授权策略：在权限上附加自定义声明后显式拒绝访问
// 向权限添加 deny-policy 声明，便于测试断言拒绝决策携带的元数据
$evaluation.getPermission().addClaim('deny-policy', 'deny-policy');
// 拒绝本次授权评估
$evaluation.deny();
