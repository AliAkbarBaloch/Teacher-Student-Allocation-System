/**
 * Validation utility functions for form inputs
 */

/**
 * Validates an email address format
 * @param email - The email address to validate
 * @returns true if the email format is valid, false otherwise
 */
export function validateEmail(email: string): boolean {
  if (!email || !email.trim()) {
    return false;
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email.trim());
}

/**
 * Validates a phone number format
 * Allows digits, spaces, dashes, parentheses, and + at the start
 * @param phoneNumber - The phone number to validate
 * @returns true if the phone number format is valid, false otherwise
 */
export function validatePhoneNumber(phoneNumber: string): boolean {
  if (!phoneNumber || !phoneNumber.trim()) {
    return false;
  }
  // Basic phone validation: allows digits, spaces, dashes, parentheses, and + at the start
  const phoneRegex = /^\+?[\d\s\-()]+$/;
  return phoneRegex.test(phoneNumber.trim());
}

