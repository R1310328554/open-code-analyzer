/**
 * skeleton-card.stories.ts — SkeletonCard Storybook：内容加载占位骨架屏。
 */

import type { Meta, StoryObj } from '@storybook/react-webpack5';

import { SkeletonCard } from '@/components/skeleton-card';

// Storybook 默认导出 meta 配置
// SkeletonCard 骨架卡片示例
/** Storybook meta：组件文档、布局与 argTypes。 */
const meta = {
  title: 'Example/SkeletonCard',
  component: SkeletonCard,
  parameters: {
    // Optional parameter to center the component in the Canvas. More info: https://storybook.js.org/docs/configure/story-layout
    layout: 'centered',
    docs: {
      description: {
        component: `
## SkeletonCard Component

SkeletonCard is a loading placeholder component that displays skeleton lines while content is being loaded. It provides a consistent loading experience with animated placeholders.

### Import Path
\`\`\`typescript
import { SkeletonCard } from '@/components/skeleton-card';
\`\`\`

### Basic Usage
\`\`\`tsx
import { SkeletonCard } from '@/components/skeleton-card';

function MyComponent() {
  return (
    <SkeletonCard className="w-64" />
  );
}
\`\`\`

### Features
- Displays animated skeleton loading placeholders
- Three lines of skeleton content with varying widths
- Customizable styling through className prop
- Consistent spacing and appearance
- Built on top of the Skeleton UI component
        `,
      },
    },
  },
  // This component will have an automatically generated Autodocs entry: https://storybook.js.org/docs/writing-docs/autodocs
  tags: ['autodocs'],
  // More on argTypes: https://storybook.js.org/docs/api/argtypes
  argTypes: {
    className: {
      description: 'Additional CSS classes to apply to the skeleton card',
      control: { type: 'text' },
      type: { name: 'string', required: false },
    },
  },
  args: {
    className: '',
  },
} satisfies Meta<typeof SkeletonCard>;

/** 默认导出 meta 供 Storybook 自动发现。 */
export default meta;
/** 本文件 Story 类型别名。 */
type Story = StoryObj<typeof meta>;

// 各 Story 通过 args 展示组件不同状态

/** 通过 className 控制骨架宽度。 */
export const WithCustomWidth: Story = {
  args: {
    className: 'w-80',
  },
  parameters: {
    docs: {
      description: {
        story: `
### Custom Width

Shows the skeleton card with a custom width applied.

\`\`\`tsx
<SkeletonCard className="w-80" />
\`\`\`
        `,
      },
    },
  },
  tags: ['!dev'],
};
