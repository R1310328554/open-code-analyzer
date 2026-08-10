import type PasswordPolicyTypeRepresentation from "@keycloak/keycloak-admin-client/lib/defs/passwordPolicyTypeRepresentation";

/** 表单提交的策略 id 到参数值的映射。 */
export type SubmittedValues = {
  [index: string]: string;
};

/** Keycloak 密码策略字符串中多条规则之间的分隔符。 */
const POLICY_SEPARATOR = " and ";

/**
 * 将策略类型列表与表单值序列化为 Keycloak API 接受的策略字符串。
 * 格式示例：`length(8) and digits(1)`
 */
export const serializePolicy = (
  policies: PasswordPolicyTypeRepresentation[],
  submitted: SubmittedValues,
) =>
  policies
    .map((policy) => `${policy.id}(${submitted[policy.id!]})`)
    .join(POLICY_SEPARATOR);

type PolicyValue = PasswordPolicyTypeRepresentation & {
  value?: string;
};

/**
 * 解析领域密码策略字符串，仅保留当前已知策略类型中存在的条目。
 * @param value 服务端返回的策略字符串
 * @param policies 当前可用的策略类型定义
 */
export const parsePolicy = (
  value: string,
  policies: PasswordPolicyTypeRepresentation[],
) =>
  value
    .split(POLICY_SEPARATOR)
    .map(parsePolicyToken)
    .reduce<PolicyValue[]>((result, { id, value }) => {
      const matchingPolicy = policies.find((policy) => policy.id === id);

      if (!matchingPolicy) {
        return result;
      }

      return result.concat({ ...matchingPolicy, value });
    }, []);

type PolicyTokenParsed = {
  id: string;
  value?: string;
};

/** 解析单条策略 token，支持带括号参数与无参数两种形式。 */
function parsePolicyToken(token: string): PolicyTokenParsed {
  const valueStart = token.indexOf("(");

  if (valueStart === -1) {
    return { id: token.trim() };
  }

  const id = token.substring(0, valueStart).trim();
  const valueEnd = token.lastIndexOf(")");

  if (valueEnd === -1) {
    return { id };
  }

  const value = token.substring(valueStart + 1, valueEnd).trim();

  return { id, value };
}
