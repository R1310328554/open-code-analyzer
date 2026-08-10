/** User Profile 元数据校验与错误消息解析工具。 */
import { UserProfileAttributeMetadata } from "@keycloak/keycloak-admin-client/lib/defs/userProfileMetadata";
import { isUserProfileError } from "@keycloak/keycloak-ui-shared";
import { TFunction } from "i18next";

/** 属性是否必填：显式 required 或校验器隐含最小长度大于零。 */
export function isRequiredAttribute({
  required,
  validators,
}: UserProfileAttributeMetadata): boolean {
  // 检查 required 标志，或校验器是否隐含必填（如 length.min > 0）
  return required || hasRequiredValidators(validators);
}

/**
 * 从 UserProfileError 提取并格式化全部错误消息为单条字符串。
 * 兼容 responseData 中单条或多条 errors 结构。
 *
 * @param error 错误对象（应为 UserProfileError）
 * @param t i18n 翻译函数
 * @returns 以分号连接的错误文案；非 UserProfile 错误时返回空串
 */
export function extractUserProfileErrorMessages(
  error: unknown,
  t: TFunction,
): string {
  if (!isUserProfileError(error)) {
    return "";
  }

  const responseData = error.responseData as
    | { errors?: { errorMessage?: string; params?: string[] }[] }
    | { errorMessage?: string; params?: string[] };

  const errors =
    "errors" in responseData && responseData.errors
      ? responseData.errors
      : [responseData as { errorMessage?: string; params?: string[] }];

  const errorMessages = errors
    .map((e) => {
      const params = e.params
        ? Object.fromEntries(e.params.map((v, i) => [i.toString(), v]))
        : {};
      return t(e.errorMessage || "", {
        ...params,
        defaultValue: e.errorMessage,
      });
    })
    .filter((msg) => msg && msg.trim() !== "");

  return errorMessages.length > 0 ? errorMessages.join("; ") : "";
}

/**
 * 校验器配置是否使属性在语义上成为必填项。
 */
function hasRequiredValidators(
  validators?: UserProfileAttributeMetadata["validators"],
): boolean {
  // 无校验器则非隐含必填
  if (!validators) {
    return false;
  }

  // length 校验器若 min > 0，则等价于必填；此处需防御性访问因校验器无强类型
  if (
    "length" in validators &&
    "min" in validators.length &&
    typeof validators.length.min === "number"
  ) {
    return validators.length.min > 0;
  }

  return false;
}
