import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { tap } from 'rxjs';

import { LoggingService } from './logging.service';

export const TRANSACTION_ID_HEADER = 'X-Transaction-Id';

export const transactionLoggingInterceptor: HttpInterceptorFn =
  (request: HttpRequest<unknown>, next: HttpHandlerFn) => {
    const logger = inject(LoggingService);
    const transactionId = logger.createTransactionId();
    const startedAt = Date.now();

    const requestWithTransactionId = request.clone({
      setHeaders: {
        [TRANSACTION_ID_HEADER]: transactionId
      }
    });

    logger.info('http_request_started', {
      method: request.method,
      url: request.urlWithParams
    }, transactionId);

    return next(requestWithTransactionId).pipe(
      tap({
        next: (event: HttpEvent<unknown>) => {
          if (event instanceof HttpResponse) {
            logger.info('http_request_completed', {
              method: request.method,
              url: request.urlWithParams,
              status: event.status,
              durationMs: Date.now() - startedAt
            }, transactionId);
          }
        },
        error: (error: unknown) => {
          const status = error instanceof HttpErrorResponse ? error.status : undefined;
          const message = error instanceof HttpErrorResponse ? error.message : 'Unknown error';

          logger.error('http_request_failed', {
            method: request.method,
            url: request.urlWithParams,
            status,
            durationMs: Date.now() - startedAt,
            message
          }, transactionId);
        }
      })
    );
  };

// Made with Bob