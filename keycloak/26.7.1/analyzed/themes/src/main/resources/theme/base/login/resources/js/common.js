// 按模板格式（如 {3}-{4}）将纯数字输入格式化为分段数字串
export const formatNumber = (input, format) => {
    if (!input) {
        return "";
    }

    // 解析格式串中每段期望的数字位数，如 {3}、{4}
    const digitPattern = format.match(/{\d+}/g);

    if (!digitPattern) {
        return "";
    }

    // 各段位数之和即为允许输入的最大数字长度
    const maxSize = digitPattern.reduce((total, p) => total + parseInt(p.replace("{", "").replace("}", "")), 0)

    // 仅保留数字字符
    let rawValue = input.replace(/\D+/g, '');

    // 校验剩余内容为合法整数
    if (parseInt(rawValue) != rawValue) {
        return "";
    }

    // 超长时截断至模板允许的最大位数
    if (rawValue.length > maxSize) {
        rawValue = rawValue.substring(0, maxSize);
    }

    // 根据各段位数构造捕获分组正则
    const formatter = digitPattern.reduce((result, p) => result + `(\\d${p})`, "^");

    // 正则匹配成功则 digits[1..n] 为各段数字
    let digits = new RegExp(formatter).exec(rawValue);

    // 无法匹配模板时原样返回输入
    if (!digits) {
        return input;
    }

    let result = format;

    // 将各段数字替换回格式模板中的占位符
    for (let i = 0; i < digitPattern.length; i++) {
        result = result.replace(digitPattern[i], digits[i + 1]);
    }

    return result;
}