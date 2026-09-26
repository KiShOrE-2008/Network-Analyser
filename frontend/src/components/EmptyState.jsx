import React from 'react';
import { Inbox } from 'lucide-react';

export default function EmptyState({
  title = 'No Data Available',
  description = 'No records match the selected filter or query criteria.',
  icon: Icon = Inbox,
  actionLabel,
  onAction
}) {
  return (
    <div className="empty-state-box">
      <div className="empty-state-icon">
        <Icon size={36} />
      </div>
      <h3 className="empty-state-title">{title}</h3>
      <p className="empty-state-desc">{description}</p>
      {actionLabel && onAction && (
        <button className="btn btn-primary btn-sm" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </div>
  );
}
