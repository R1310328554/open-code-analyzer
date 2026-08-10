// 取款限额授权策略：仅当账户取款金额不超过 100 时授予访问
var context = $evaluation.getContext();
var attributes = context.getAttributes();
// 从评估上下文读取自定义属性 my.bank.account.withdraw.value
var withdrawValue = attributes.getValue('my.bank.account.withdraw.value');

// 属性存在且数值 ≤ 100 时允许操作
if (withdrawValue && withdrawValue.asDouble(0) <= 100) {
    $evaluation.grant();
}
