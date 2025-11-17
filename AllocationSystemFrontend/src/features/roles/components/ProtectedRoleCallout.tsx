import { AlertCircle } from "lucide-react";

interface ProtectedRoleCalloutProps {
  message: string;
}

export function ProtectedRoleCallout({ message }: ProtectedRoleCalloutProps) {
  return (
    <div className="flex items-center gap-2 mt-2 p-2 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-md">
      <AlertCircle className="h-4 w-4 text-yellow-600 dark:text-yellow-400" />
      <span className="text-sm text-yellow-800 dark:text-yellow-300">{message}</span>
    </div>
  );
}

