import { createEmptyDependent, createEmptyLetterRequest } from './models';

describe('models', () => {
  it('should create an empty dependent', () => {
    expect(createEmptyDependent()).toEqual({
      dependentName: '',
      relationship: '',
      enrollmentDate: '',
      pcpName: '',
      location: ''
    });
  });

  it('should create an empty letter request with one dependent and empty custom fields', () => {
    const request = createEmptyLetterRequest();

    expect(request.templateName).toBe('');
    expect(request.memberName).toBe('');
    expect(request.referenceNumber).toBe('');
    expect(request.subject).toBe('');
    expect(request.address).toBe('');
    expect(request.addressLine1).toBe('');
    expect(request.addressLine2).toBe('');
    expect(request.city).toBe('');
    expect(request.state).toBe('');
    expect(request.zipcode).toBe('');
    expect(request.effectiveDate).toBe('');
    expect(request.remarks).toBe('');
    expect(request.customFields).toEqual({});
    expect(request.dependents.length).toBe(1);
    expect(request.dependents[0]).toEqual(createEmptyDependent());
  });
});

// Made with Bob
