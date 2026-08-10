// Storybook 预览全局配置：加载 i18n、iconfont、Tailwind 与 TooltipProvider 装饰器。
import '@/locales/config';
import type { Preview } from '@storybook/react-webpack5';
import { createElement } from 'react';
import '../public/iconfont.js';
import { TooltipProvider } from '../src/components/ui/tooltip';

import '../tailwind.css';

const preview: Preview = {  // controls matchers 与全局 React 装饰器
  parameters: {
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/i,
      },
    },
  },
  decorators: [  // 为每个 Story 包裹 TooltipProvider
    (Story) => createElement(TooltipProvider, null, createElement(Story)),
  ],
};

export default preview;
