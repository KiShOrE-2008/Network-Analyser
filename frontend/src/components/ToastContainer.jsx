import React from 'react';
import { CheckCircle, AlertTriangle, XCircle, Info, X } from 'lucide-react';

export default function ToastContainer({ toasts = [], onDismiss }) {
  if (!toasts || toasts.length === 0) return null;

  return (
    <div className="toast-container">
      {toasts.map((toast) => {
        const isSuccess = toast.type === 'success';
        const isError = toast.type === 'error';
        const isWarning = toast.type === 'warning';

        return (
          <div key={toast.id} className={`toast-item ${toast.type || 'info'}`}>
            <div className="toast-icon">
              {isSuccess && <CheckCircle size={18} color="var(--online)" />}
              {isError && <XCircle size={18} color="var(--offline)" />}
              {isWarning && <AlertTriangle size={18} color="var(--warning)" />}
              {!isSuccess && !isError && !isWarning && <Info size={18} color="var(--primary)" />}
            </div>
            <div className="toast-body">
              {toast.title && <div className="toast-title">{toast.title}</div>}
              <div className="toast-message">{toast.message}</div>
            </div>
            <button 
              className="toast-close"
              onClick={() => onDismiss(toast.id)}
              aria-label="Dismiss toast notification"
            >
              <X size={14} />
            </button>
          </div>
        );
      })}
    </div>
  );
}
