import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Spinner, PageSpinner, FullPageSpinner } from './spinner';

describe('Spinner', () => {
  it('renders with status role', () => {
    render(<Spinner />);
    expect(screen.getByRole('status', { name: 'Loading' })).toBeInTheDocument();
  });

  it('applies size classes', () => {
    const { container } = render(<Spinner size="lg" />);
    expect(container.firstChild).toHaveClass('size-8');
  });
});

describe('PageSpinner', () => {
  it('renders spinner', () => {
    render(<PageSpinner />);
    expect(screen.getByRole('status', { name: 'Loading' })).toBeInTheDocument();
  });

  it('shows message when provided', () => {
    render(<PageSpinner message="Loading data..." />);
    expect(screen.getByText('Loading data...')).toBeInTheDocument();
  });

  it('hides message when not provided', () => {
    const { container } = render(<PageSpinner />);
    expect(container.querySelectorAll('p')).toHaveLength(0);
  });
});

describe('FullPageSpinner', () => {
  it('renders spinner', () => {
    render(<FullPageSpinner />);
    expect(screen.getByRole('status', { name: 'Loading' })).toBeInTheDocument();
  });
});
