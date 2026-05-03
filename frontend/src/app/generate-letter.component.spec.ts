import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { GenerateLetterComponent } from './generate-letter.component';
import { LetterApiService } from './letter-api.service';
import { LoggingService } from './logging.service';
import { createEmptyLetterRequest } from './models';

describe('GenerateLetterComponent', () => {
  let fixture: ComponentFixture<GenerateLetterComponent>;
  let component: GenerateLetterComponent;
  let api: jasmine.SpyObj<LetterApiService>;
  let logger: jasmine.SpyObj<LoggingService>;

  beforeEach(async () => {
    api = jasmine.createSpyObj<LetterApiService>('LetterApiService', [
      'getTemplates',
      'validateAddress',
      'previewLetter',
      'generateLetter'
    ]);
    logger = jasmine.createSpyObj<LoggingService>('LoggingService', ['info', 'warn', 'error']);

    api.getTemplates.and.returnValue(of([]));
    api.validateAddress.and.returnValue(of({ valid: true, message: 'validated' }));
    api.previewLetter.and.returnValue(of('<html>preview</html>'));
    api.generateLetter.and.returnValue(of({
      id: '1',
      memberName: 'John Doe',
      templateName: 'Welcome Letter',
      referenceNumber: 'REF-1',
      generatedAt: 'today',
      fileName: 'letter.pdf',
      filePath: '/tmp/letter.pdf'
    }));

    await TestBed.configureTestingModule({
      imports: [GenerateLetterComponent],
      providers: [
        { provide: LetterApiService, useValue: api },
        { provide: LoggingService, useValue: logger }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(GenerateLetterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load templates on init', () => {
    expect(api.getTemplates).toHaveBeenCalled();
    expect(logger.info).toHaveBeenCalledWith('templates_load_requested');
  });

  it('should set single template automatically', () => {
    api.getTemplates.and.returnValue(of([{ name: 'Only Template', templatePath: 'path' }]));

    fixture = TestBed.createComponent(GenerateLetterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.request.templateName).toBe('Only Template');
  });

  it('should add a dependent', () => {
    const originalCount = component.request.dependents.length;

    component.addDependent();

    expect(component.request.dependents.length).toBe(originalCount + 1);
    expect(logger.info).toHaveBeenCalledWith('dependent_added', { dependentCount: component.request.dependents.length });
  });

  it('should remove a dependent when more than one exists', () => {
    component.request.dependents.push({
      dependentName: 'Dep',
      relationship: 'Child',
      enrollmentDate: '2024-01-01',
      pcpName: 'PCP',
      location: 'Loc'
    });

    component.removeDependent(0);

    expect(component.request.dependents.length).toBe(1);
    expect(logger.info).toHaveBeenCalledWith('dependent_removed', { dependentCount: 1 });
  });

  it('should reject removing the last dependent', () => {
    component.removeDependent(0);

    expect(component.validationMessage).toBe('At least one dependent is required.');
    expect(logger.warn).toHaveBeenCalledWith('dependent_remove_rejected', { reason: 'minimum_one_dependent_required' });
  });

  it('should validate address successfully', () => {
    component.request.memberName = 'John Doe';
    component.request.referenceNumber = 'REF-1';

    component.validateAddress();

    expect(component.validationMessage).toBe('validated');
    expect(component.addressValidated).toBeTrue();
    expect(component.statusMessage).toBe('');
  });

  it('should handle failed address validation request', () => {
    api.validateAddress.and.returnValue(throwError(() => new HttpErrorResponse({
      status: 400,
      error: 'Validation failed'
    })));

    component.validateAddress();

    expect(component.validationMessage).toBe('Validation failed');
    expect(component.addressValidated).toBeFalse();
    expect(component.previewHtml).toBe('');
    expect(logger.error).toHaveBeenCalledWith('validate_address_failed', { message: 'Validation failed' });
  });

  it('should clear preview when validation result is invalid', () => {
    component.previewHtml = 'existing';
    api.validateAddress.and.returnValue(of({ valid: false, message: 'bad address' }));

    component.validateAddress();

    expect(component.addressValidated).toBeFalse();
    expect(component.previewHtml).toBe('');
  });

  it('should preview letter successfully', () => {
    component.previewLetter();

    expect(component.previewHtml).toBe('<html>preview</html>');
    expect(component.statusMessage).toBe('');
    expect(logger.info).toHaveBeenCalledWith('preview_letter_completed', { previewLength: '<html>preview</html>'.length });
  });

  it('should handle preview failure', () => {
    api.previewLetter.and.returnValue(throwError(() => new HttpErrorResponse({
      status: 400,
      error: 'Preview failed'
    })));

    component.previewLetter();

    expect(component.previewHtml).toBe('');
    expect(component.statusMessage).toBe('Preview failed');
    expect(logger.error).toHaveBeenCalledWith('preview_letter_failed', { message: 'Preview failed' });
  });

  it('should generate letter successfully and reset form', () => {
    component.request.memberName = 'John Doe';
    component.request.templateName = 'Welcome Letter';
    component.request.referenceNumber = 'REF-1';
    component.previewHtml = 'preview';
    component.addressValidated = true;
    component.validationMessage = 'ok';

    component.generateLetter();

    expect(component.statusMessage).toBe('Letter generated successfully: letter.pdf');
    expect(component.validationMessage).toBe('');
    expect(component.previewHtml).toBe('');
    expect(component.addressValidated).toBeFalse();
    expect(component.request).toEqual(createEmptyLetterRequest());
  });

  it('should handle generate letter failure', () => {
    api.generateLetter.and.returnValue(throwError(() => new HttpErrorResponse({
      status: 500,
      error: 'Generate failed'
    })));

    component.generateLetter();

    expect(component.statusMessage).toBe('Generate failed');
    expect(logger.error).toHaveBeenCalledWith('generate_letter_failed', { message: 'Generate failed' });
  });

  it('should rebuild address from populated fields', () => {
    component.request.addressLine1 = 'Line 1';
    component.request.addressLine2 = 'Line 2';
    component.request.city = 'Pune';
    component.request.state = 'MH';
    component.request.zipcode = '411001';

    component.rebuildAddress();

    expect(component.request.address).toBe('Line 1\nLine 2\nPune, MH 411001');
  });

  it('should rebuild address with only zipcode when city and state are empty', () => {
    component.request = createEmptyLetterRequest();
    component.request.zipcode = '411001';

    component.rebuildAddress();

    expect(component.request.address).toBe('411001');
  });
});

// Made with Bob
