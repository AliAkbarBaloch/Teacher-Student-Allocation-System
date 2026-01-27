/**
 * Represents the strength assessment of a password
 */
export interface PasswordStrength {
  /** Password strength score from 0-5 based on criteria met */
  score: number; // 0-5
  /** Human-readable strength label (weak, fair, good, strong) */
  label: string;
  /** CSS color class for displaying the strength */
  color: string;
  /** Individual checks that determine password strength */
  checks: {
    /** Whether password meets minimum length requirement (8+ characters) */
    minLength: boolean;
    /** Whether password contains uppercase letters */
    hasUppercase: boolean;
    /** Whether password contains lowercase letters */
    hasLowercase: boolean;
    /** Whether password contains numbers */
    hasNumber: boolean;
    /** Whether password contains special characters */
    hasSpecial: boolean;
  };
}

export function calculatePasswordStrength(password: string): PasswordStrength {
  const checks = {
    minLength: password.length >= 8,
    hasUppercase: /[A-Z]/.test(password),
    hasLowercase: password.split('').some(char => char >= 'a' && char <= 'z'),
    hasNumber: /[0-9]/.test(password),
    hasSpecial: /[!@#$%^&*(),.?":{}|<>]/.test(password),
  };

  const score = Object.values(checks).filter(Boolean).length;
  
  let label = "";
  let color = "";
  
  if (score <= 1) {
    label = "weak";
    color = "text-red-500";
  } else if (score === 2) {
    label = "fair";
    color = "text-orange-500";
  } else if (score === 3) {
    label = "good";
    color = "text-yellow-500";
  } else {
    label = "strong";
    color = "text-green-500";
  }

  return { score, label, color, checks };
}

