import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { LetterApiService } from './letter-api.service';
import { createEmptyLetterRequest } from './models';

describe('LetterApiService', () => {
  let service: LetterApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        LetterApiService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(LetterApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should get templates', () => {
    service.getTemplates().subscribe();

    const request = httpMock.expectOne('/api/templates');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('should validate address', () => {
    const payload = createEmptyLetterRequest();

    service.validateAddress(payload).subscribe();

    const request = httpMock.expectOne('/api/address/validate');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({ valid: true, message: 'ok' });
  });

  it('should preview letter as text', () => {
    const payload = createEmptyLetterRequest();

    service.previewLetter(payload).subscribe();

    const request = httpMock.expectOne('/api/letters/preview');
    expect(request.request.method).toBe('POST');
    expect(request.request.responseType).toBe('text');
    request.flush('<html></html>');
  });

  it('should generate letter', () => {
    const payload = createEmptyLetterRequest();

    service.generateLetter(payload).subscribe();

    const request = httpMock.expectOne('/api/letters');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({
      id: '1',
      memberName: 'John',
      templateName: 'Welcome',
      referenceNumber: 'REF',
      generatedAt: 'today',
      fileName: 'file.pdf',
      filePath: '/tmp/file.pdf'
    });
  });

  it('should get letters without filter', () => {
    service.getLetters().subscribe();

    const request = httpMock.expectOne(req => req.url === '/api/letters' && !req.params.has('memberName'));
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('should get letters with trimmed filter', () => {
    service.getLetters('  John Doe  ').subscribe();

    const request = httpMock.expectOne(req => req.url === '/api/letters' && req.params.get('memberName') === 'John Doe');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('should build download url', () => {
    const url = service.buildDownloadUrl('C:/letters/file.pdf', 'file name.pdf');

    expect(url).toContain('/api/letters/download?');
    expect(url).toContain('filePath=C%3A%2Fletters%2Ffile.pdf');
    expect(url).toContain('fileName=file+name.pdf');
  });
});

// Made with Bob
