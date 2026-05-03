import { TestBed } from '@angular/core/testing';

import { LoggingService } from './logging.service';

describe('LoggingService', () => {
  let service: LoggingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LoggingService);
  });

  it('should create a transaction id using crypto.randomUUID when available', () => {
    const originalCrypto = globalThis.crypto;
    const mockCrypto = {
      randomUUID: jasmine.createSpy().and.returnValue('txn-crypto')
    } as unknown as Crypto;

    Object.defineProperty(globalThis, 'crypto', {
      value: mockCrypto,
      configurable: true
    });

    expect(service.createTransactionId()).toBe('txn-crypto');

    Object.defineProperty(globalThis, 'crypto', {
      value: originalCrypto,
      configurable: true
    });
  });

  it('should fall back when crypto.randomUUID is unavailable', () => {
    const originalCrypto = globalThis.crypto;

    Object.defineProperty(globalThis, 'crypto', {
      value: undefined,
      configurable: true
    });

    const transactionId = service.createTransactionId();

    expect(transactionId.startsWith('txn-')).toBeTrue();

    Object.defineProperty(globalThis, 'crypto', {
      value: originalCrypto,
      configurable: true
    });
  });

  it('should log info payloads', () => {
    spyOn(console, 'info');

    service.info('info_event', { key: 'value' }, 'txn-1');

    expect(console.info).toHaveBeenCalled();
  });

  it('should log warn payloads', () => {
    spyOn(console, 'warn');

    service.warn('warn_event', { key: 'value' }, 'txn-2');

    expect(console.warn).toHaveBeenCalled();
  });

  it('should log error payloads', () => {
    spyOn(console, 'error');

    service.error('error_event', { key: 'value' }, 'txn-3');

    expect(console.error).toHaveBeenCalled();
  });
});

// Made with Bob
