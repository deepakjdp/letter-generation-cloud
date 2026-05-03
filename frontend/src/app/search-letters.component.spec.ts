import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { SearchLettersComponent } from './search-letters.component';
import { LetterApiService } from './letter-api.service';
import { LoggingService } from './logging.service';

describe('SearchLettersComponent', () => {
  let fixture: ComponentFixture<SearchLettersComponent>;
  let component: SearchLettersComponent;
  let api: jasmine.SpyObj<LetterApiService>;
  let logger: jasmine.SpyObj<LoggingService>;

  beforeEach(async () => {
    api = jasmine.createSpyObj<LetterApiService>('LetterApiService', ['getLetters', 'buildDownloadUrl']);
    logger = jasmine.createSpyObj<LoggingService>('LoggingService', ['info', 'error']);

    api.getLetters.and.returnValue(of([]));
    api.buildDownloadUrl.and.returnValue('/download');

    await TestBed.configureTestingModule({
      imports: [SearchLettersComponent],
      providers: [
        { provide: LetterApiService, useValue: api },
        { provide: LoggingService, useValue: logger }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SearchLettersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should search letters successfully', () => {
    const letters = [{
      id: '1',
      memberName: 'John Doe',
      templateName: 'Welcome',
      referenceNumber: 'REF',
      generatedAt: 'today',
      fileName: 'file.pdf',
      filePath: '/tmp/file.pdf'
    }];
    api.getLetters.and.returnValue(of(letters));

    component.memberName = 'John';
    component.search();

    expect(api.getLetters).toHaveBeenCalledWith('John');
    expect(component.results).toEqual(letters);
    expect(component.errorMessage).toBe('');
    expect(logger.info).toHaveBeenCalledWith('search_letters_triggered', { memberNameFilter: 'John' });
    expect(logger.info).toHaveBeenCalledWith('search_letters_completed', { resultCount: 1 });
  });

  it('should handle search failure', () => {
    api.getLetters.and.returnValue(throwError(() => new Error('boom')));

    component.search();

    expect(component.errorMessage).toBe('Failed to search letters.');
    expect(logger.error).toHaveBeenCalledWith('search_letters_failed', { message: 'Failed to search letters.' });
  });

  it('should build download url', () => {
    const letter = {
      id: '1',
      memberName: 'John Doe',
      templateName: 'Welcome',
      referenceNumber: 'REF',
      generatedAt: 'today',
      fileName: 'file.pdf',
      filePath: '/tmp/file.pdf'
    };

    const result = component.downloadUrl(letter);

    expect(result).toBe('/download');
    expect(api.buildDownloadUrl).toHaveBeenCalledWith('/tmp/file.pdf', 'file.pdf');
  });
});

// Made with Bob
