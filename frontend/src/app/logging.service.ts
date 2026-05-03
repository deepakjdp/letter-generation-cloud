import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoggingService {
  private readonly appName = 'letter-gen-frontend';

  createTransactionId(): string {
    if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
      return crypto.randomUUID();
    }
    return `txn-${Date.now()}-${Math.random().toString(16).slice(2)}`;
  }

  info(event: string, details: Record<string, unknown> = {}, transactionId?: string): void {
    this.log('INFO', event, details, transactionId);
  }

  warn(event: string, details: Record<string, unknown> = {}, transactionId?: string): void {
    this.log('WARN', event, details, transactionId);
  }

  error(event: string, details: Record<string, unknown> = {}, transactionId?: string): void {
    this.log('ERROR', event, details, transactionId);
  }

  private log(level: 'INFO' | 'WARN' | 'ERROR',
              event: string,
              details: Record<string, unknown>,
              transactionId?: string): void {
    const payload = {
      timestamp: new Date().toISOString(),
      appName: this.appName,
      logType: 'frontend',
      level,
      event,
      transactionId: transactionId ?? '',
      ...details
    };

    if (level === 'ERROR') {
      console.error(payload);
      return;
    }
    if (level === 'WARN') {
      console.warn(payload);
      return;
    }
    console.info(payload);
  }
}

// Made with Bob