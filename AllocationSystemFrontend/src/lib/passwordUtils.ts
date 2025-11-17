export interface PasswordStrength {
  score: number; // 0-5
  label: string;
  color: string;
  checks: {
    minLength: boolean;
    hasUppercase: boolean;
    hasLowercase: boolean;
    hasNumber: boolean;
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

