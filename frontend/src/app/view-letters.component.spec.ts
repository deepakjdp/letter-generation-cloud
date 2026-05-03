import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { ViewLettersComponent } from './view-letters.component';
import { LetterApiService } from './letter-api.service';
import { LoggingService } from './logging.service';

describe('ViewLettersComponent', () => {
  let fixture: ComponentFixture<ViewLettersComponent>;
  let component: ViewLettersComponent;
  let api: jasmine.SpyObj<LetterApiService>;
  let logger: jasmine.SpyObj<LoggingService>;

  beforeEach(async () => {
    api = jasmine.createSpyObj<LetterApiService>('LetterApiService', ['getLetters', 'buildDownloadUrl']);
    logger = jasmine.createSpyObj<LoggingService>('LoggingService', ['info', 'error']);

    api.getLetters.and.returnValue(of([]));
    api.buildDownloadUrl.and.returnValue('/download');

    await TestBed.configureTestingModule({
      imports: [ViewLettersComponent],
      providers: [
        { provide: LetterApiService, useValue: api },
        { provide: LoggingService, useValue: logger }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ViewLettersComponent);
    component = fixture.componentInstance;
  });

  it('should load letters on init', () => {
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

    fixture.detectChanges();

    expect(api.getLetters).toHaveBeenCalledWith();
    expect(component.letters).toEqual(letters);
    expect(component.errorMessage).toBe('');
    expect(logger.info).toHaveBeenCalledWith('view_letters_load_requested');
    expect(logger.info).toHaveBeenCalledWith('view_letters_load_completed', { resultCount: 1 });
  });

  it('should handle load failure', () => {
    api.getLetters.and.returnValue(throwError(() => new Error('boom')));

    fixture.detectChanges();

    expect(component.errorMessage).toBe('Failed to load letters.');
    expect(logger.error).toHaveBeenCalledWith('view_letters_load_failed', { message: 'Failed to load letters.' });
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
