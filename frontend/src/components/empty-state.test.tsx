import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { EmptyState } from './empty-state';

describe('EmptyState', () => {
  it('renders message', () => {
    render(<EmptyState message="No items found." />);
    expect(screen.getByText('No items found.')).toBeInTheDocument();
  });

  it('renders action when provided', () => {
    render(<EmptyState message="Empty" action={<button>Add item</button>} />);
    expect(screen.getByRole('button', { name: 'Add item' })).toBeInTheDocument();
  });

  it('does not render action when not provided', () => {
    const { container } = render(<EmptyState message="Empty" />);
    expect(container.querySelectorAll('button')).toHaveLength(0);
  });
});
