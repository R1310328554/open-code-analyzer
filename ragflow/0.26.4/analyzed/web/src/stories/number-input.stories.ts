/**
 * number-input.stories.ts — NumberInput 组件 Storybook：步进器、边界与样式变体。
 */

import type { Meta, StoryObj } from '@storybook/react-webpack5';

import { fn } from 'storybook/test';

import NumberInput from '@/components/originui/number-input';

// Storybook 默认导出 meta 配置
// NumberInput 数字输入框多种用法
/** Storybook meta：组件文档、布局与 argTypes。 */
const meta = {
  title: 'Example/NumberInput',
  component: NumberInput,
  parameters: {
    // Optional parameter to center the component in the Canvas. More info: https://storybook.js.org/docs/configure/story-layout
    layout: 'centered',
    docs: {
      description: {
        component: `
## NumberInput Component

NumberInput is a numeric input component with increment/decrement buttons. It provides a user-friendly interface for entering numeric values with built-in validation and keyboard controls.

### Import Path
\`\`\`typescript
import NumberInput from '@/components/originui/number-input';
\`\`\`

### Basic Usage
\`\`\`tsx
import { useState } from 'react';
import NumberInput from '@/components/originui/number-input';

function MyComponent() {
  const [value, setValue] = useState(0);

  return (
    <NumberInput
      value={value}
      onChange={(newValue) => setValue(newValue)}
    />
  );
}
\`\`\`

### Features
- Increment/decrement buttons for easy value adjustment
- Keyboard input validation (only allows numeric input)
- Customizable height and styling (wrapper and input)
- Min/max value constraints
- Option to hide increment/decrement buttons
- Responsive design with Tailwind CSS
        `,
      },
    },
  },
  // This component will have an automatically generated Autodocs entry: https://storybook.js.org/docs/writing-docs/autodocs
  tags: ['autodocs'],
  // More on argTypes: https://storybook.js.org/docs/api/argtypes
  argTypes: {
    value: {
      description: 'The current numeric value',
      control: { type: 'number' },
    },
    onChange: {
      description: 'Callback function called when value changes',
      control: false,
    },
    height: {
      description:
        'Custom height for the input component (number or string with px)',
      control: { type: 'text' },
    },
    className: {
      description: 'Additional CSS classes for the wrapper',
      control: { type: 'text' },
    },
    inputClassName: {
      description: 'Additional CSS classes for the input element',
      control: { type: 'text' },
    },
    min: {
      description: 'Minimum allowed value',
      control: { type: 'number' },
    },
    max: {
      description: 'Maximum allowed value',
      control: { type: 'number' },
    },
    hideIcons: {
      description: 'Hide the increment/decrement buttons',
      control: { type: 'boolean' },
    },
  },
  // Use `fn` to spy on the onChange arg, which will appear in the actions panel once invoked: https://storybook.js.org/docs/essentials/actions#action-args
  args: { onChange: fn() },
} satisfies Meta<typeof NumberInput>;

/** 默认导出 meta 供 Storybook 自动发现。 */
export default meta;
/** 本文件 Story 类型别名。 */
type Story = StoryObj<typeof meta>;

// 各 Story 通过 args 展示组件不同状态
/** 默认值 0 的基础样式。 */
export const Default: Story = {
  args: {
    value: 0,
  },
  parameters: {
    docs: {
      description: {
        story: `
### Default Number Input

Shows the basic number input with default styling and zero value.

\`\`\`tsx
<NumberInput
  value={0}
  onChange={(value) => console.log('Value changed:', value)}
/>
\`\`\`
        `,
      },
    },
  },
  tags: ['!dev'],
};

/** 预设初始数值。 */
export const WithInitialValue: Story = {
  args: {
    value: 10,
  },
  parameters: {
    docs: {
      description: {
        story: `
### With Initial Value

Shows the number input with a predefined initial value.

\`\`\`tsx
<NumberInput
  value={10}
  onChange={(value) => console.log('Value changed:', value)}
/>
\`\`\`
        `,
      },
    },
  },
  tags: ['!dev'],
};

/** 自定义容器高度。 */
export const CustomHeight: Story = {
  args: {
    value: 5,
    height: '60px',
  },
  parameters: {
    docs: {
      description: {
        story: `
### Custom Height

Shows the number input with custom height styling.

\`\`\`tsx
<NumberInput
  value={5}
  height="60px"
  onChange={(value) => console.log('Value changed:', value)}
/>
\`\`\`
        `,
      },
    },
  },
  tags: ['!dev'],
};

/** 通过 className 定制外观。 */
export const WithCustomClass: Story = {
  args: {
    value: 3,
    className: 'border-blue-500 bg-blue-50',
  },
  parameters: {
    docs: {
      description: {
        story: `
### With Custom Styling

Shows the number input with custom CSS classes for styling.

\`\`\`tsx
<NumberInput
  value={3}
  className="border-blue-500 bg-blue-50"
  onChange={(value) => console.log('Value changed:', value)}
/>
\`\`\`
        `,
      },
    },
  },
  tags: ['!dev'],
};

/** min/max 约束非法输入。 */
export const WithMinMax: Story = {
  args: {
    value: 5,
    min: 0,
    max: 10,
  },
  parameters: {
    docs: {
      description: {
        story: `
### With Min/Max Constraints

Shows the number input with minimum and maximum value constraints. Values outside the range are rejected.

\`\`\`tsx
<NumberInput
  value={5}
  min={0}
  max={10}
  onChange={(value) => console.log('Value changed:', value)}
/>
\`\`\`
        `,
      },
    },
  },
  tags: ['!dev'],
};

/** hideIcons 隐藏加减按钮。 */
export const HideIcons: Story = {
  args: {
    value: 7,
    hideIcons: true,
  },
  parameters: {
    docs: {
      description: {
        story: `
### Without Icons

Shows the number input with increment/decrement buttons hidden, leaving only the text input field.

\`\`\`tsx
<NumberInput
  value={7}
  hideIcons
  onChange={(value) => console.log('Value changed:', value)}
/>
\`\`\`
        `,
      },
    },
  },
  tags: ['!dev'],
};

/** inputClassName 直接作用于 input 元素。 */
export const WithInputClassName: Story = {
  args: {
    value: 4,
    inputClassName: 'text-red-500 font-bold',
  },
  parameters: {
    docs: {
      description: {
        story: `
### With Input Class Name

Shows the number input with custom CSS classes applied directly to the input element.

\`\`\`tsx
<NumberInput
  value={4}
  inputClassName="text-red-500 font-bold"
  onChange={(value) => console.log('Value changed:', value)}
/>
\`\`\`
        `,
      },
    },
  },
  tags: ['!dev'],
};
