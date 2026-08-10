// translation-context.ts — jsonjoy-builder 国际化 React Context，默认注入英语语言包。

import { createContext } from 'react';
import { en } from './locales/en';
import type { Translation } from './translation-keys.ts';

/** 全局翻译 Context，组件通过 useTranslation 读取当前语言包。 */
export const TranslationContext = createContext<Translation>(en);
