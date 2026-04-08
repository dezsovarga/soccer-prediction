import { describe, it, expect, vi } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import { ToastProvider, useToast } from './toast';

function ToastTrigger() {
  const { toast } = useToast();
  return (
    <div>
      <button onClick={() => toast('success', 'Saved!')}>Success</button>
      <button onClick={() => toast('error', 'Failed!')}>Error</button>
    </div>
  );
}

function renderWithToast() {
  return render(
    <ToastProvider>
      <ToastTrigger />
    </ToastProvider>
  );
}

describe('Toast', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });
  afterEach(() => {
    vi.useRealTimers();
  });

  it('shows success toast', () => {
    renderWithToast();

    act(() => {
      screen.getByRole('button', { name: 'Success' }).click();
    });

    expect(screen.getByText('Saved!')).toBeInTheDocument();
    expect(screen.getByRole('alert')).toBeInTheDocument();
  });

  it('shows error toast', () => {
    renderWithToast();

    act(() => {
      screen.getByRole('button', { name: 'Error' }).click();
    });

    expect(screen.getByText('Failed!')).toBeInTheDocument();
  });

  it('auto-dismisses after 4 seconds', () => {
    renderWithToast();

    act(() => {
      screen.getByRole('button', { name: 'Success' }).click();
    });

    expect(screen.getByText('Saved!')).toBeInTheDocument();

    act(() => {
      vi.advanceTimersByTime(4100);
    });

    expect(screen.queryByText('Saved!')).not.toBeInTheDocument();
  });

  it('dismisses on click', () => {
    renderWithToast();

    act(() => {
      screen.getByRole('button', { name: 'Success' }).click();
    });

    expect(screen.getByText('Saved!')).toBeInTheDocument();

    act(() => {
      screen.getByRole('button', { name: 'Dismiss' }).click();
    });

    expect(screen.queryByText('Saved!')).not.toBeInTheDocument();
  });

  it('throws when useToast used outside provider', () => {
    vi.useRealTimers();
    function Broken() {
      useToast();
      return null;
    }
    expect(() => render(<Broken />)).toThrow('useToast must be used within a ToastProvider');
  });
});
