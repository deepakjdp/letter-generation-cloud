import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  HttpErrorResponse,
  HttpResponse,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { LoggingService } from './logging.service';
import { TRANSACTION_ID_HEADER, transactionLoggingInterceptor } from './transaction-logging.interceptor';

describe('transactionLoggingInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let logger: jasmine.SpyObj<LoggingService>;

  beforeEach(() => {
    logger = jasmine.createSpyObj<LoggingService>('LoggingService', ['createTransactionId', 'info', 'error']);
    logger.createTransactionId.and.returnValue('txn-123');

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([transactionLoggingInterceptor])),
        provideHttpClientTesting(),
        { provide: LoggingService, useValue: logger }
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should attach transaction id header and log request lifecycle on success', () => {
    httpClient.get('/api/templates').subscribe();

    const request = httpMock.expectOne('/api/templates');
    expect(request.request.headers.get(TRANSACTION_ID_HEADER)).toBe('txn-123');

    request.event(new HttpResponse({ status: 200, body: [] }));

    expect(logger.info).toHaveBeenCalledWith('http_request_started', {
      method: 'GET',
      url: '/api/templates'
    }, 'txn-123');

    expect(logger.info).toHaveBeenCalledWith(
      'http_request_completed',
      jasmine.objectContaining({
        method: 'GET',
        url: '/api/templates',
        status: 200
      }),
      'txn-123'
    );
  });

  it('should log failures on http error', () => {
    httpClient.get('/api/templates').subscribe({
      error: () => undefined
    });

    const request = httpMock.expectOne('/api/templates');
    request.flush('boom', { status: 500, statusText: 'Server Error' });

    expect(logger.error).toHaveBeenCalledWith(
      'http_request_failed',
      jasmine.objectContaining({
        method: 'GET',
        url: '/api/templates',
        status: 500
      }),
      'txn-123'
    );
  });

  it('should log unknown error message for non-http errors', () => {
    httpClient.get('/api/templates').subscribe({
      error: () => undefined
    });

    const request = httpMock.expectOne('/api/templates');
    request.error(new ProgressEvent('network'));

    expect(logger.error).toHaveBeenCalledWith(
      'http_request_failed',
      jasmine.objectContaining({
        method: 'GET',
        url: '/api/templates',
        message: jasmine.any(String)
      }),
      'txn-123'
    );
  });
});

// Made with Bob
