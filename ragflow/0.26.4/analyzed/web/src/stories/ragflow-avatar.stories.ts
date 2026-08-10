/**
 * ragflow-avatar.stories.ts — RAGFlowAvatar Storybook：图片头像与首字母渐变兜底。
 */

import type { Meta, StoryObj } from '@storybook/react-webpack5';

import { RAGFlowAvatar } from '@/components/ragflow-avatar';

// Storybook 默认导出 meta 配置
// RAGFlowAvatar 头像组件示例
/** Storybook meta：组件文档、布局与 argTypes。 */
const meta = {
  title: 'Example/RAGFlowAvatar',
  component: RAGFlowAvatar,
  parameters: {
    // Optional parameter to center the component in the Canvas. More info: https://storybook.js.org/docs/configure/story-layout
    layout: 'centered',
    docs: {
      description: {
        component: `
## RAGFlowAvatar Component

RAGFlowAvatar is a customizable avatar component that displays user avatars with intelligent fallbacks. When an image is not available, it generates colorful gradient backgrounds with user initials.

### Import Path
\`\`\`typescript
import { RAGFlowAvatar } from '@/components/ragflow-avatar';
\`\`\`

### Basic Usage
\`\`\`tsx
import { RAGFlowAvatar } from '@/components/ragflow-avatar';

function MyComponent() {
  return (
    <RAGFlowAvatar
      name="John Doe"
      avatar="https://example.com/avatar.jpg"
      isPerson={true}
    />
  );
}
\`\`\`

### Features
- Displays user avatar images when available
- Generates colorful gradient fallbacks with initials
- Supports both person (circular) and non-person (rounded) styles
- Automatic font size calculation based on container size
- Color generation based on name for consistent appearance
- Responsive design with resize observer
        `,
      },
    },
  },
  // This component will have an automatically generated Autodocs entry: https://storybook.js.org/docs/writing-docs/autodocs
  tags: ['autodocs'],
  // More on argTypes: https://storybook.js.org/docs/api/argtypes
  argTypes: {
    name: {
      description:
        'The name to display initials for when no avatar is available',
      control: { type: 'text' },
      type: { name: 'string', required: false },
    },
    avatar: {
      description: 'The URL of the avatar image',
      control: { type: 'text' },
      type: { name: 'string', required: false },
    },
    isPerson: {
      description: 'Whether this avatar represents a person (affects styling)',
      control: { type: 'boolean' },
      type: { name: 'boolean', required: false },
      defaultValue: false,
    },
    className: {
      description: 'Additional CSS classes to apply',
      control: { type: 'text' },
      type: { name: 'string', required: false },
    },
  },
  args: {
    name: 'John Doe',
    isPerson: false,
  },
} satisfies Meta<typeof RAGFlowAvatar>;

/** 默认导出 meta 供 Storybook 自动发现。 */
export default meta;
/** 本文件 Story 类型别名。 */
type Story = StoryObj<typeof meta>;

// 各 Story 通过 args 展示组件不同状态
/** 无图片时按姓名生成首字母渐变。 */
export const WithInitials: Story = {
  args: {
    name: 'John Doe',
    isPerson: false,
  },
  parameters: {
    docs: {
      description: {
        story: `
### With Initials Only

Shows the avatar component with only a name, displaying generated initials with a gradient background.

\`\`\`tsx
<RAGFlowAvatar
  name="John Doe"
  isPerson={false}
/>
\`\`\`
        `,
      },
    },
  },
  tags: ['!dev'],
};

/** 展示真实头像图片（isPerson 圆形）。 */
export const WithAvatar: Story = {
  args: {
    name: 'Jane Smith',
    avatar:
      'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjQiIGhlaWdodD0iNjQiIHZpZXdCb3g9IjAgMCA2NCA2NCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjY0IiBoZWlnaHQ9IjY0IiByeD0iMzIiIGZpbGw9IiM0RjZERUUiLz4KPGNpcmNsZSBjeD0iMzIiIGN5PSIyNCIgcj0iOCIgZmlsbD0id2hpdGUiLz4KPHBhdGggZD0iTTQ4IDQ4QzQ4IDQxLjM3MyA0MS45NDA2IDM2IDM0LjUgMzZIMjkuNUMyMi4wNTk0IDM2IDE2IDQxLjM3MyAxNiA0OCIgZmlsbD0id2hpdGUiLz4KPC9zdmc+',
    isPerson: true,
  },
  parameters: {
    docs: {
      description: {
        story: `
### With Avatar Image

Shows the avatar component with an actual image. When isPerson is true, the avatar will be circular.

\`\`\`tsx
<RAGFlowAvatar
  name="Jane Smith"
  avatar="https://example.com/avatar.jpg"
  isPerson={true}
/>
\`\`\`
        `,
      },
    },
  },
  tags: ['!dev'],
};

/** isPerson=true 圆形个人头像。 */
export const PersonStyle: Story = {
  args: {
    name: 'Alice Johnson',
    isPerson: true,
  },
  parameters: {
    docs: {
      description: {
        story: `
### Person Style (Circular)

Shows the avatar component with isPerson set to true, which makes it circular.

\`\`\`tsx
<RAGFlowAvatar
  name="Alice Johnson"
  isPerson={true}
/>
\`\`\`
        `,
      },
    },
  },
  tags: ['!dev'],
};

/** isPerson=false 圆角矩形（如 Bot）。 */
export const NonPersonStyle: Story = {
  args: {
    name: 'Bot Assistant',
    isPerson: false,
  },
  parameters: {
    docs: {
      description: {
        story: `
### Non-Person Style (Rounded Rectangle)

Shows the avatar component with isPerson set to false, which makes it a rounded rectangle.

\`\`\`tsx
<RAGFlowAvatar
  name="Bot Assistant"
  isPerson={false}
/>
\`\`\`
        `,
      },
    },
  },
  tags: ['!dev'],
};
