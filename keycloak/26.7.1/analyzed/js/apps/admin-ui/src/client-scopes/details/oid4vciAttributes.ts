import { ClientScopeDefaultOptionalType } from "../../components/client-scope/ClientScopeTypes";
import { convertAttributeNameToForm } from "../../util";

/**
 * 提交前需清理的空 OID4VC 属性键列表。
 * 与可选 OID4VC 表单字段保持同步；新增字符串/数值类属性且需裁剪时请一并加入。
 */
export const OID4VC_ATTRIBUTE_KEYS = [
  "vc.credential_configuration_id",
  "vc.credential_identifier",
  "vc.issuer_did",
  "vc.expiry_in_seconds",
  "vc.credential_build_config.token_jws_type",
  "vc.supported_credential_types",
  "vc.verifiable_credential_type",
  "vc.credential_build_config.sd_jwt.visible_claims",
  "vc.display",
  "vc.binding_required",
  "vc.binding_required_proof_types",
  "vc.cryptographic_binding_methods_supported",
  "vc.refresh_interval_in_seconds",
] as const;

/** 判断值是否为空：null、undefined 或仅含空白字符的字符串。 */
const isEmptyValue = (value: unknown) =>
  value === null ||
  value === undefined ||
  (typeof value === "string" && value.trim() === "");

/**
 * 从客户端作用域载荷中移除已知的空 OID4VC 可选属性，避免向服务端提交无意义字段。
 */
export const removeEmptyOid4vcAttributes = (
  values: ClientScopeDefaultOptionalType,
): ClientScopeDefaultOptionalType => {
  const fieldNames = OID4VC_ATTRIBUTE_KEYS.map((attr) =>
    convertAttributeNameToForm<ClientScopeDefaultOptionalType>(
      `attributes.${attr}`,
    ),
  );

  // OID4VC 属性目前为扁平结构，浅拷贝即可；若将来 attributes.vc.* 出现嵌套对象需改为深拷贝
  const cleanedValues = { ...values } as Record<string, unknown>;
  const hadAttributes = Boolean(cleanedValues.attributes);
  const cleanedAttributes = {
    ...(cleanedValues.attributes as Record<string, unknown> | undefined),
  };

  for (const fieldName of fieldNames) {
    const attrKey = fieldName.replace(/^attributes\./, "");
    if (isEmptyValue(cleanedAttributes[attrKey])) {
      delete cleanedAttributes[attrKey];
    }
  }

  if (Object.keys(cleanedAttributes).length === 0) {
    if (hadAttributes) {
      delete cleanedValues.attributes;
    }
  } else {
    cleanedValues.attributes = cleanedAttributes;
  }

  return cleanedValues as unknown as ClientScopeDefaultOptionalType;
};
