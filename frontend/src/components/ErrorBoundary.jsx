import { Component } from 'react';

/**
 * Error Boundary — catches runtime errors and displays them
 * on screen instead of showing a blank page.
 */
export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('ErrorBoundary caught:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ padding: 40, background: '#020617', minHeight: '100vh', color: '#F8FAFC', fontFamily: 'monospace' }}>
          <h2 style={{ color: '#EF4444', fontSize: 20, marginBottom: 16 }}>⚠️ Runtime Error Caught</h2>
          <pre style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-all', color: '#94A3B8', fontSize: 13 }}>
            {this.state.error?.toString()}
          </pre>
          <pre style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-all', color: '#64748B', fontSize: 12, marginTop: 16 }}>
            {this.state.error?.stack}
          </pre>
        </div>
      );
    }
    return this.props.children;
  }
}
