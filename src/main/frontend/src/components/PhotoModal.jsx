import React from 'react';

export function PhotoModal({ photoUrl, onClose }) {
  return React.createElement(
    'div',
    {
      className: 'photo-modal',
      role: 'dialog',
      'aria-modal': 'true',
      'aria-label': 'Nearby incident photo',
    },
    React.createElement('img', { src: photoUrl, alt: 'Nearby incident location' }),
    React.createElement('button', { type: 'button', onClick: onClose }, 'Close')
  );
}
