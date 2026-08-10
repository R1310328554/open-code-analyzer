/** PatternFly 表格单元格：将 bundle 键翻译为当前语言文案。 */
import { label } from "@keycloak/keycloak-ui-shared";
import { IFormatter, IFormatterValueType } from "@patternfly/react-table";
import { TFunction } from "i18next";

/** 返回表格 formatter：有值则 i18n label，否则显示 em dash。 */
export const translationFormatter =
  (t: TFunction): IFormatter =>
  (data?: IFormatterValueType) => {
    return data ? label(t, data as string) || "—" : "—";
  };
