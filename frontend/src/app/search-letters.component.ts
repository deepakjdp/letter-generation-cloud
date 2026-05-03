import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { LetterApiService } from './letter-api.service';
import { LoggingService } from './logging.service';
import { LetterRecord } from './models';

@Component({
  selector: 'app-search-letters',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="card">
      <h2>Search Letters</h2>
      <div class="form-row">
        <label for="memberName">Member Name</label>
        <input id="memberName" name="memberName" [(ngModel)]="memberName" />
      </div>
      <div class="form-row">
        <button type="button" class="btn-primary" (click)="search()">Search</button>
      </div>

      <p *ngIf="errorMessage" class="message-error">{{ errorMessage }}</p>

      <table class="data-table">
        <thead>
        <tr>
          <th>Member Name</th>
          <th>Template</th>
          <th>Generated At</th>
          <th>Download</th>
        </tr>
        </thead>
        <tbody>
        <tr *ngIf="results.length === 0">
          <td colspan="4">No letters found.</td>
        </tr>
        <tr *ngFor="let letter of results">
          <td>{{ letter.memberName }}</td>
          <td>{{ letter.templateName }}</td>
          <td>{{ letter.generatedAt }}</td>
          <td>
            <a [href]="downloadUrl(letter)" target="_blank" rel="noopener">Download</a>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  `
})
export class SearchLettersComponent {
  private readonly api = inject(LetterApiService);
  private readonly logger = inject(LoggingService);

  memberName = '';
  results: LetterRecord[] = [];
  errorMessage = '';

  search(): void {
    this.logger.info('search_letters_triggered', { memberNameFilter: this.memberName });
    this.api.getLetters(this.memberName).subscribe({
      next: (letters: LetterRecord[]) => {
        this.results = letters;
        this.errorMessage = '';
        this.logger.info('search_letters_completed', { resultCount: letters.length });
      },
      error: () => {
        this.errorMessage = 'Failed to search letters.';
        this.logger.error('search_letters_failed', { message: this.errorMessage });
      }
    });
  }

  downloadUrl(letter: LetterRecord): string {
    return this.api.buildDownloadUrl(letter.filePath, letter.fileName);
  }
}

// Made with Bob
