import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ErrorAlert } from './error-alert';

describe('ErrorAlert', () => {
  it('renders default message', () => {
    render(<ErrorAlert />);
    expect(screen.getByText('Something went wrong. Please try again.')).toBeInTheDocument();
  });

  it('renders custom message', () => {
    render(<ErrorAlert message="Failed to load data." />);
    expect(screen.getByText('Failed to load data.')).toBeInTheDocument();
  });

  it('shows retry button when onRetry provided', () => {
    render(<ErrorAlert onRetry={() => {}} />);
    expect(screen.getByRole('button', { name: 'Try again' })).toBeInTheDocument();
  });

  it('hides retry button when no onRetry', () => {
    render(<ErrorAlert />);
    expect(screen.queryByRole('button', { name: 'Try again' })).not.toBeInTheDocument();
  });

  it('calls onRetry when retry button clicked', async () => {
    const onRetry = vi.fn();
    render(<ErrorAlert onRetry={onRetry} />);
    await userEvent.click(screen.getByRole('button', { name: 'Try again' }));
    expect(onRetry).toHaveBeenCalledOnce();
  });
});
