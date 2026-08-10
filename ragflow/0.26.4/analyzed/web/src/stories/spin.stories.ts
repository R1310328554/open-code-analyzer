/**
 * spin.stories.ts — Spin Storybook：加载旋转指示器与尺寸变体。
 */

import type { Meta, StoryObj } from '@storybook/react-webpack5';

import { Spin } from '@/components/ui/spin';

// Storybook 默认导出 meta 配置
// Spin 加载动画组件示例
/** Storybook meta：组件文档、布局与 argTypes。 */
const meta = {
  title: 'Example/Spin',
  component: Spin,
  parameters: {
    // Optional parameter to center the component in the Canvas. More info: https://storybook.js.org/docs/configure/story-layout
    layout: 'centered',
    docs: {
      description: {
        component: `
## Spin Component

Spin is a loading spinner component that can be used to indicate loading states. It supports different sizes and can wrap other content to create loading overlays.

### Import Path
\`\`\`typescript
import { Spin } from '@/components/ui/spin';
\`\`\`

### Basic Usage
\`\`\`tsx
import { Spin } from '@/components/ui/spin';

function MyComponent() {
  return (
    <Spin spinning={true}>
      <div>Your content here</div>
    </Spin>
  );
}
\`\`\`

### Features
- Three different sizes: small, default, and large
- Can wrap content to create loading overlays
- Smooth animation with CSS transitions
- Customizable styling with className prop
- Built with Tailwind CSS
        `,
      },
    },
  },
  // This component will have an automatically generated Autodocs entry: https://storybook.js.org/docs/writing-docs/autodocs
  tags: ['autodocs'],
  // More on argTypes: https://storybook.js.org/docs/api/argtypes
  argTypes: {
    spinning: {
      description: 'Whether the spinner is active',
      control: { type: 'boolean' },
    },
    size: {
      description: 'Size of the spinner',
      control: { type: 'select' },
      options: ['small', 'default', 'large'],
    },
    className: {
      description: 'Additional CSS classes for styling',
      control: { type: 'text' },
    },
    children: {
      description: 'Content to be wrapped by the spinner',
      control: false,
    },
  },
  // Use `fn` to spy on any callbacks
  args: {},
} satisfies Meta<typeof Spin>;

/** 默认导出 meta 供 Storybook 自动发现。 */
export default meta;
/** 本文件 Story 类型别名。 */
type Story = StoryObj<typeof meta>;

// 各 Story 通过 args 展示组件不同状态
/** 默认尺寸且 spinning 激活。 */
export const Default: Story = {
  args: {
    spinning: true,
    size: 'default',
  },
  parameters: {
    docs: {
      description: {
        story: `
### Default Spinner

Shows the basic spinner with default size and active state.

\`\`\`tsx
<Spin spinning={true} size="default" />
\`\`\`
        `,
      },
    },
  },
  tags: ['!dev'],
};
