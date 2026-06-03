import React from 'react';

const BERKELEY_SEAL_URL =
  'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a1/Seal_of_University_of_California%2C_Berkeley.svg/1200px-Seal_of_University_of_California%2C_Berkeley.svg.png';

export function AppHeader({ searchTerm, onSearchChange }) {
  function handleSubmit(event) {
    event.preventDefault();
  }

  return React.createElement(
    'header',
    { className: 'app-header' },
    React.createElement(
      'div',
      { className: 'brand' },
      React.createElement('img', {
        src: BERKELEY_SEAL_URL,
        alt: 'UC Berkeley seal',
      }),
      React.createElement(
        'div',
        null,
        React.createElement('h1', null, 'SafeZone'),
        React.createElement('p', null, 'UC Berkeley incident awareness')
      )
    ),
    React.createElement(
      'form',
      { className: 'search-panel', onSubmit: handleSubmit },
      React.createElement('label', { htmlFor: 'incident-search' }, 'Find incident'),
      React.createElement('input', {
        id: 'incident-search',
        type: 'search',
        placeholder: 'Search type, status, or description',
        value: searchTerm,
        onChange: (event) => onSearchChange(event.target.value),
      })
    )
  );
}
