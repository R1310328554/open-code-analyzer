/**
 * button-loading.stories.ts — ButtonLoading 组件 Storybook：带 loading 动画的按钮。
 */

import type { Meta, StoryObj } from '@storybook/react-webpack5';

import { fn } from 'storybook/test';

import { ButtonLoading } from '@/components/ui/button';

// Storybook 默认导出 meta 配置
// ButtonLoading 加载态按钮示例
/** Storybook meta：组件文档、布局与 argTypes。 */
const meta = {
  title: 'Example/ButtonLoading',
  component: ButtonLoading,
  parameters: {
    // Optional parameter to center the component in the Canvas. More info: https://storybook.js.org/docs/configure/story-layout
    layout: 'centered',
    docs: {
      description: {
        component: `
## Component Description

ButtonLoading is a button component with a loading state and supports displaying loading animation.        `,
      },
    },
  },
  // This component will have an automatically generated Autodocs entry: https://storybook.js.org/docs/writing-docs/autodocs
  tags: ['autodocs'],
  // More on argTypes: https://storybook.js.org/docs/api/argtypes
  argTypes: {
    loading: { control: 'boolean' },
  },
  // Use `fn` to spy on the onClick arg, which will appear in the actions panel once invoked: https://storybook.js.org/docs/essentials/actions#action-args
  args: { onClick: fn() },
} satisfies Meta<typeof ButtonLoading>;

/** 默认导出 meta 供 Storybook 自动发现。 */
export default meta;
/** 本文件 Story 类型别名。 */
type Story = StoryObj<typeof meta>;

// 各 Story 通过 args 展示组件不同状态
/** loading=true 时展示旋转指示器。 */
export const WithLoading: Story = {
  args: {
    loading: true,
    children: 'Button',
  },
  parameters: {
    docs: {
      description: {
        story: `
### Usage Examples

\`\`\`tsx
import { ButtonLoading } from '@/components/ui/button';

<ButtonLoading loading={true}>
  Loading Button
</ButtonLoading>
\`\`\`
        `,
      },
    },
  },
  tags: ['!dev'],
};
