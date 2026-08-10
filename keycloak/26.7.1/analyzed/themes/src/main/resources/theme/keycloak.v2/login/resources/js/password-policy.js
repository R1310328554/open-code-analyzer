// keycloak.v2 主题：客户端密码策略实时校验
// keycloak.v2 主题：客户端密码策略实时校验
// 各策略名称对应的校验函数，失败时返回本地化错误消息
// 各策略名称对应的校验函数，失败时返回本地化错误消息
const policies = {
  length: (policy, value) => {
    if (value.length < policy.value) {
      return templateError(policy);
    }
  },
  maxLength: (policy, value) => {
    if (value.length > policy.value) {
      return templateError(policy);
    }
  },
  upperCase: (policy, value) => {
    if (
      value.split("").filter((char) => char === char.toUpperCase() && char !== char.toLowerCase()).length <
      policy.value
    ) {
      return templateError(policy);
    }
  },
  lowerCase: (policy, value) => {
    if (
      value.split("").filter((char) => char === char.toLowerCase() && char !== char.toUpperCase()).length <
      policy.value
    ) {
      return templateError(policy);
    }
  },
  digits: (policy, value) => {
    const digits = value.split("").filter((char) => char.match(/\d/));
    if (digits.length < policy.value) {
      return templateError(policy);
    }
  },
  specialChars: (policy, value) => {
    let specialChars = value.split("").filter((char) => char.match(/\W/));
    if (specialChars.length < policy.value) {
      return templateError(policy);
    }
  },
};

// 将策略模板中的 {0} 替换为策略阈值
const templateError = (policy) => policy.error.replace("{0}", policy.value);

// 对密码依次执行激活的策略，返回所有未通过的错误消息列表
export function validatePassword(password, activePolicies) {
  const errors = [];
  for (const p of activePolicies) {
    const validationError = policies[p.name](p.policy, password);
    if (validationError) {
      errors.push(validationError);
    }
  }
  return errors;
}
