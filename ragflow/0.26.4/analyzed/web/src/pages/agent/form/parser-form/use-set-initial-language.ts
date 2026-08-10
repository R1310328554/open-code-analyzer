// use-set-initial-language.ts — Parser 表单：语言字段为空时写入 crossLanguage 首项默认值。

import { crossLanguageOptions } from '@/components/cross-language-form-field';
import { isEmpty } from 'lodash';
import { useEffect } from 'react';
import { useFormContext } from 'react-hook-form';
import { buildFieldNameWithPrefix } from './utils';

/** 展示语言选择且 lang 未填时，setValue 为 crossLanguageOptions[0]。 */
export function useSetInitialLanguage({
  prefix,
  languageShown,
}: {
  prefix: string;
  languageShown: boolean;
}) {
  const form = useFormContext();
  const lang = form.getValues(buildFieldNameWithPrefix('lang', prefix));

  useEffect(() => {
    if (languageShown && isEmpty(lang)) {
      form.setValue(
        buildFieldNameWithPrefix('lang', prefix),
        crossLanguageOptions[0].value,
        {
          shouldValidate: true,
          shouldDirty: true,
        },
      );
    }
  }, [form, lang, languageShown, prefix]);
}
