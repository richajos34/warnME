import { useCallback, useEffect, useRef, useState } from 'react';
import { fetchIncidents } from '../services/incidents.js';

export function useIncidents({ pollIntervalMs = 30000 } = {}) {
  const isMountedRef = useRef(false);
  const [state, setState] = useState({
    incidents: [],
    status: 'loading',
    error: '',
  });

  const loadIncidents = useCallback(async () => {
    setState((currentState) => ({ ...currentState, status: 'loading', error: '' }));

    const incidents = await fetchIncidents();
    if (!isMountedRef.current) {
      return;
    }

    setState({
      incidents,
      status: incidents.length > 0 ? 'ready' : 'empty',
      error: '',
    });
  }, []);

  useEffect(() => {
    isMountedRef.current = true;

    loadIncidents().catch((error) => {
      if (!isMountedRef.current) {
        return;
      }

      setState({
        incidents: [],
        status: 'error',
        error: error.message,
      });
    });

    return () => {
      isMountedRef.current = false;
    };
  }, [loadIncidents]);

  useEffect(() => {
    if (!pollIntervalMs) {
      return undefined;
    }

    const intervalId = window.setInterval(() => {
      loadIncidents().catch((error) => {
        if (!isMountedRef.current) {
          return;
        }

        setState({
          incidents: [],
          status: 'error',
          error: error.message,
        });
      });
    }, pollIntervalMs);

    return () => window.clearInterval(intervalId);
  }, [loadIncidents, pollIntervalMs]);

  return { ...state, reloadIncidents: loadIncidents };
}
