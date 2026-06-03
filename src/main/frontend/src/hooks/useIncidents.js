import { useEffect, useState } from 'react';
import { fetchIncidents } from '../services/incidents.js';

export function useIncidents() {
  const [state, setState] = useState({
    incidents: [],
    status: 'loading',
    error: '',
  });

  useEffect(() => {
    let isMounted = true;

    fetchIncidents()
      .then((incidents) => {
        if (!isMounted) {
          return;
        }

        setState({
          incidents,
          status: incidents.length > 0 ? 'ready' : 'empty',
          error: '',
        });
      })
      .catch((error) => {
        if (!isMounted) {
          return;
        }

        setState({
          incidents: [],
          status: 'error',
          error: error.message,
        });
      });

    return () => {
      isMounted = false;
    };
  }, []);

  return state;
}
