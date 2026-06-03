export async function searchWarnMeEmails() {
  const response = await fetch('/api/gmail/warnme/search');

  if (!response.ok) {
    throw new Error(`Gmail WarnMe search returned ${response.status}`);
  }

  return response.json();
}
