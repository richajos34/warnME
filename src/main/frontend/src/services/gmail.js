async function parseGmailError(response, actionLabel) {
  let message = `${actionLabel} returned ${response.status}`;

  try {
    const payload = await response.json();
    message = payload.message || payload.error || message;
  } catch {
    const text = await response.text();
    if (text) {
      message = text;
    }
  }

  if (response.status === 401) {
    return 'Reconnect Berkeley Gmail, then try again. Your Gmail connection is only stored in the current browser session.';
  }

  return message;
}

export async function fetchGmailStatus() {
  const response = await fetch('/api/gmail/status', {
    credentials: 'same-origin',
  });

  if (!response.ok) {
    throw new Error(await parseGmailError(response, 'Gmail status check'));
  }

  return response.json();
}

export async function searchWarnMeEmails() {
  const response = await fetch('/api/gmail/warnme/search', {
    credentials: 'same-origin',
  });

  if (!response.ok) {
    throw new Error(await parseGmailError(response, 'Gmail WarnMe search'));
  }

  return response.json();
}

export async function importWarnMeEmails() {
  const response = await fetch('/api/gmail/warnme/import', {
    method: 'POST',
    credentials: 'same-origin',
  });

  if (!response.ok) {
    throw new Error(await parseGmailError(response, 'Gmail WarnMe import'));
  }

  return response.json();
}

export async function syncWarnMeEmails() {
  const response = await fetch('/api/gmail/warnme/import', {
    method: 'POST',
    credentials: 'same-origin',
  });

  if (response.status === 401) {
    return { connected: false, summary: null };
  }

  if (!response.ok) {
    throw new Error(await parseGmailError(response, 'Gmail WarnMe sync'));
  }

  return response.json();
}
