import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { LetterApiService } from './letter-api.service';
import { LoggingService } from './logging.service';
import { LetterRecord } from './models';

@Component({
  selector: 'app-view-letters',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card">
      <h2>View Letters</h2>

      <p *ngIf="errorMessage" class="message-error">{{ errorMessage }}</p>

      <table class="data-table">
        <thead>
        <tr>
          <th>Member Name</th>
          <th>Template</th>
          <th>Generated At</th>
          <th>File Name</th>
          <th>Download</th>
        </tr>
        </thead>
        <tbody>
        <tr *ngIf="letters.length === 0">
          <td colspan="5">No letters found.</td>
        </tr>
        <tr *ngFor="let letter of letters">
          <td>{{ letter.memberName }}</td>
          <td>{{ letter.templateName }}</td>
          <td>{{ letter.generatedAt }}</td>
          <td>{{ letter.fileName }}</td>
          <td>
            <a [href]="downloadUrl(letter)" target="_blank" rel="noopener">Download</a>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  `
})
export class ViewLettersComponent implements OnInit {
  private readonly api = inject(LetterApiService);
  private readonly logger = inject(LoggingService);

  letters: LetterRecord[] = [];
  errorMessage = '';

  ngOnInit(): void {
    this.logger.info('view_letters_load_requested');
    this.api.getLetters().subscribe({
      next: (letters) => {
        this.letters = letters;
        this.errorMessage = '';
        this.logger.info('view_letters_load_completed', { resultCount: letters.length });
      },
      error: () => {
        this.errorMessage = 'Failed to load letters.';
        this.logger.error('view_letters_load_failed', { message: this.errorMessage });
      }
    });
  }

  downloadUrl(letter: LetterRecord): string {
    return this.api.buildDownloadUrl(letter.filePath, letter.fileName);
  }
}

// Made with Bob
