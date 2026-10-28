import { HttpErrorResponse } from '@angular/common/http';

export function apiErrorMessage(error: HttpErrorResponse, fallback: string): string {
  if (error.status === 0 || error.status === 502 || error.status === 504) {
    return 'No pudimos conectar con el servidor. Comprobá que el backend esté disponible y volvé a intentar.';
  }

  const body: unknown = error.error;
  if (typeof body !== 'object' || body === null) return fallback;

  const problem = body as { detail?: unknown; errors?: unknown };
  const errors = Array.isArray(problem.errors)
    ? problem.errors.filter((value): value is string => typeof value === 'string')
    : [];

  if (errors.length) return errors.join(' · ');
  return typeof problem.detail === 'string' ? problem.detail : fallback;
}
