export interface AddressValidationResult {
  valid: boolean;
  message: string;
}

export interface DependentInfo {
  dependentName: string;
  relationship: string;
  enrollmentDate: string;
  pcpName: string;
  location: string;
}

export interface LetterRecord {
  id: string;
  memberName: string;
  templateName: string;
  referenceNumber: string;
  generatedAt: string;
  fileName: string;
  filePath: string;
}

export interface TemplateOption {
  name: string;
  templatePath: string;
}

export interface LetterRequest {
  templateName: string;
  memberName: string;
  referenceNumber: string;
  subject: string;
  address: string;
  addressLine1: string;
  addressLine2: string;
  city: string;
  state: string;
  zipcode: string;
  effectiveDate: string;
  remarks: string;
  customFields: Record<string, string>;
  dependents: DependentInfo[];
}

export function createEmptyDependent(): DependentInfo {
  return {
    dependentName: '',
    relationship: '',
    enrollmentDate: '',
    pcpName: '',
    location: ''
  };
}

export function createEmptyLetterRequest(): LetterRequest {
  return {
    templateName: '',
    memberName: '',
    referenceNumber: '',
    subject: '',
    address: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    zipcode: '',
    effectiveDate: '',
    remarks: '',
    customFields: {},
    dependents: [createEmptyDependent()]
  };
}

// Made with Bob
