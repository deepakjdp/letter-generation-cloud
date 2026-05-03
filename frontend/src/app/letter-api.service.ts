import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { AddressValidationResult, LetterRecord, LetterRequest, TemplateOption } from './models';

@Injectable({
  providedIn: 'root'
})
export class LetterApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api';

  getTemplates(): Observable<TemplateOption[]> {
    return this.http.get<TemplateOption[]>(`${this.baseUrl}/templates`);
  }

  validateAddress(request: LetterRequest): Observable<AddressValidationResult> {
    return this.http.post<AddressValidationResult>(`${this.baseUrl}/address/validate`, request);
  }

  previewLetter(request: LetterRequest): Observable<string> {
    return this.http.post(`${this.baseUrl}/letters/preview`, request, { responseType: 'text' });
  }

  generateLetter(request: LetterRequest): Observable<LetterRecord> {
    return this.http.post<LetterRecord>(`${this.baseUrl}/letters`, request);
  }

  getLetters(memberName?: string): Observable<LetterRecord[]> {
    let params = new HttpParams();
    if (memberName && memberName.trim()) {
      params = params.set('memberName', memberName.trim());
    }
    return this.http.get<LetterRecord[]>(`${this.baseUrl}/letters`, { params });
  }

  buildDownloadUrl(filePath: string, fileName: string): string {
    const params = new URLSearchParams({
      filePath,
      fileName
    });
    return `${this.baseUrl}/letters/download?${params.toString()}`;
  }
}

// Made with Bob
