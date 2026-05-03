import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { LetterApiService } from './letter-api.service';
import { LoggingService } from './logging.service';
import {
  AddressValidationResult,
  LetterRecord,
  LetterRequest,
  TemplateOption,
  createEmptyDependent,
  createEmptyLetterRequest
} from './models';

@Component({
  selector: 'app-generate-letter',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="card">
      <h2>Generate Letter</h2>
      <div class="form-row">
        <label for="template">Template <span class="required-mark">*</span></label>
        <select id="template" name="template" [(ngModel)]="request.templateName">
          <option value="">Select Template</option>
          <option *ngFor="let template of templates" [value]="template.name">{{ template.name }}</option>
        </select>
      </div>
      <div class="form-row">
        <label for="memberName">Member Name <span class="required-mark">*</span></label>
        <input id="memberName" name="memberName" [(ngModel)]="request.memberName" />
      </div>
      <div class="form-row">
        <label for="addressLine1">Address Line 1 <span class="required-mark">*</span></label>
        <input id="addressLine1" name="addressLine1" [(ngModel)]="request.addressLine1" (ngModelChange)="rebuildAddress()" />
      </div>
      <div class="form-row">
        <label for="addressLine2">Address Line 2</label>
        <input id="addressLine2" name="addressLine2" [(ngModel)]="request.addressLine2" (ngModelChange)="rebuildAddress()" />
      </div>
      <div class="address-grid">
        <div class="form-row">
          <label for="city">City <span class="required-mark">*</span></label>
          <input id="city" name="city" [(ngModel)]="request.city" (ngModelChange)="rebuildAddress()" />
        </div>
        <div class="form-row">
          <label for="state">State <span class="required-mark">*</span></label>
          <input id="state" name="state" [(ngModel)]="request.state" (ngModelChange)="rebuildAddress()" />
        </div>
        <div class="form-row">
          <label for="zipcode">Zipcode <span class="required-mark">*</span></label>
          <input id="zipcode" name="zipcode" [(ngModel)]="request.zipcode" (ngModelChange)="rebuildAddress()" />
        </div>
      </div>
      <div class="form-row">
        <label for="referenceNumber">Reference Number</label>
        <input id="referenceNumber" name="referenceNumber" [(ngModel)]="request.referenceNumber" />
      </div>

      <h3>Dependents <span class="required-mark">*</span></h3>
      <p class="hint">At least one dependent is required.</p>

      <table class="data-table dependent-table">
        <thead>
        <tr>
          <th>Dependent Name <span class="required-mark">*</span></th>
          <th>Relationship <span class="required-mark">*</span></th>
          <th>Enrollment Date <span class="required-mark">*</span></th>
          <th>PCP Name <span class="required-mark">*</span></th>
          <th>Location <span class="required-mark">*</span></th>
          <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr *ngFor="let dependent of request.dependents; let i = index">
          <td><input name="dependentName{{ i }}" [(ngModel)]="dependent.dependentName" /></td>
          <td><input name="relationship{{ i }}" [(ngModel)]="dependent.relationship" /></td>
          <td><input name="enrollmentDate{{ i }}" [(ngModel)]="dependent.enrollmentDate" /></td>
          <td><input name="pcpName{{ i }}" [(ngModel)]="dependent.pcpName" /></td>
          <td><input name="location{{ i }}" [(ngModel)]="dependent.location" /></td>
          <td><button type="button" (click)="removeDependent(i)">Delete</button></td>
        </tr>
        </tbody>
      </table>

      <div class="form-row action-row">
        <button type="button" (click)="addDependent()">Add Dependent</button>
        <button type="button" (click)="validateAddress()">Validate Address</button>
        <button type="button" *ngIf="addressValidated" (click)="previewLetter()">View Letter</button>
        <button type="button" class="btn-primary" *ngIf="addressValidated" (click)="generateLetter()">Generate PDF Letter</button>
      </div>

      <p *ngIf="validationMessage" [class]="addressValidated ? 'message-success' : 'message-error'">{{ validationMessage }}</p>
      <p *ngIf="statusMessage" class="message-success">{{ statusMessage }}</p>

      <div *ngIf="previewHtml" class="preview-card">
        <h3>Letter Preview</h3>
        <iframe class="preview-frame" [srcdoc]="previewHtml"></iframe>
      </div>
    </div>
  `
})
export class GenerateLetterComponent implements OnInit {
  private readonly api = inject(LetterApiService);
  private readonly logger = inject(LoggingService);

  templates: TemplateOption[] = [];
  request: LetterRequest = createEmptyLetterRequest();
  statusMessage = '';
  validationMessage = '';
  previewHtml = '';
  addressValidated = false;

  ngOnInit(): void {
    this.logger.info('templates_load_requested');
    this.api.getTemplates().subscribe({
      next: (templates: TemplateOption[]) => {
        this.templates = templates;
        this.logger.info('templates_load_succeeded', { templateCount: templates.length });
        if (templates.length === 1) {
          this.request.templateName = templates[0].name;
        }
      }
    });
  }

  addDependent(): void {
    this.request.dependents.push(createEmptyDependent());
    this.logger.info('dependent_added', { dependentCount: this.request.dependents.length });
  }

  removeDependent(index: number): void {
    if (this.request.dependents.length > 1 && index >= 0 && index < this.request.dependents.length) {
      this.request.dependents.splice(index, 1);
      this.logger.info('dependent_removed', { dependentCount: this.request.dependents.length });
    } else if (this.request.dependents.length === 1) {
      this.validationMessage = 'At least one dependent is required.';
      this.logger.warn('dependent_remove_rejected', { reason: 'minimum_one_dependent_required' });
    }
  }

  validateAddress(): void {
    this.logger.info('validate_address_triggered', {
      memberName: this.request.memberName,
      referenceNumber: this.request.referenceNumber
    });
    this.api.validateAddress(this.request).subscribe({
      next: (result: AddressValidationResult) => {
        this.validationMessage = result.message;
        this.addressValidated = result.valid;
        this.logger.info('validate_address_completed', { valid: result.valid, message: result.message });
        this.statusMessage = '';
        if (!this.addressValidated) {
          this.previewHtml = '';
        }
      },
      error: (error: HttpErrorResponse) => {
        this.validationMessage = this.extractMessage(error, 'Address validation failed.');
        this.addressValidated = false;
        this.logger.error('validate_address_failed', { message: this.validationMessage });
        this.previewHtml = '';
      }
    });
  }

  previewLetter(): void {
    this.logger.info('preview_letter_triggered', {
      memberName: this.request.memberName,
      templateName: this.request.templateName
    });
    this.api.previewLetter(this.request).subscribe({
      next: (html: string) => {
        this.previewHtml = html;
        this.statusMessage = '';
        this.logger.info('preview_letter_completed', { previewLength: html.length });
      },
      error: (error: HttpErrorResponse) => {
        this.previewHtml = '';
        this.statusMessage = this.extractMessage(error, 'Failed to build preview.');
        this.logger.error('preview_letter_failed', { message: this.statusMessage });
      }
    });
  }

  generateLetter(): void {
    this.logger.info('generate_letter_triggered', {
      memberName: this.request.memberName,
      templateName: this.request.templateName,
      referenceNumber: this.request.referenceNumber
    });
    this.api.generateLetter(this.request).subscribe({
      next: (record: LetterRecord) => {
        this.statusMessage = `Letter generated successfully: ${record.fileName}`;
        this.validationMessage = '';
        this.logger.info('generate_letter_completed', { fileName: record.fileName, letterId: record.id });
        this.previewHtml = '';
        this.addressValidated = false;
        this.request = createEmptyLetterRequest();
      },
      error: (error: HttpErrorResponse) => {
        this.statusMessage = this.extractMessage(error, 'Failed to generate letter.');
        this.logger.error('generate_letter_failed', { message: this.statusMessage });
      }
    });
  }

  rebuildAddress(): void {
    const lines: string[] = [];
    if (this.request.addressLine1?.trim()) {
      lines.push(this.request.addressLine1.trim());
    }
    if (this.request.addressLine2?.trim()) {
      lines.push(this.request.addressLine2.trim());
    }

    const cityParts: string[] = [];
    if (this.request.city?.trim()) {
      cityParts.push(this.request.city.trim());
    }
    if (this.request.state?.trim()) {
      cityParts.push(this.request.state.trim());
    }
    let lastLine = cityParts.join(cityParts.length > 1 ? ', ' : '');
    if (this.request.zipcode?.trim()) {
      lastLine = lastLine ? `${lastLine} ${this.request.zipcode.trim()}` : this.request.zipcode.trim();
    }
    if (lastLine) {
      lines.push(lastLine);
    }

    this.request.address = lines.join('\n');
  }

  private extractMessage(error: HttpErrorResponse, fallback: string): string {
    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }
    return fallback;
  }
}

// Made with Bob
