import { Badge } from "@/components/ui/badge";

interface JsonDiffViewerProps {
  previousValue: string | null;
  newValue: string | null;
  className?: string;
}

/**
 * Component to display JSON diff between previous and new values
 */
export function JsonDiffViewer({
  previousValue,
  newValue,
  className = "",
}: JsonDiffViewerProps) {
  const formatJson = (jsonString: string | null): string => {
    if (!jsonString) return "";
    try {
      const parsed = JSON.parse(jsonString);
      return JSON.stringify(parsed, null, 2);
    } catch {
      return jsonString;
    }
  };

  const previousFormatted = formatJson(previousValue);
  const newFormatted = formatJson(newValue);

  const hasPrevious = previousFormatted.length > 0;
  const hasNew = newFormatted.length > 0;

  if (!hasPrevious && !hasNew) {
    return (
      <div className={`text-sm text-muted-foreground ${className}`}>
        No value data available
      </div>
    );
  }

  return (
    <div className={`grid gap-4 ${hasPrevious && hasNew ? "md:grid-cols-2" : "md:grid-cols-1"} ${className}`}>
      {hasPrevious && (
        <div className="border rounded-lg overflow-hidden">
          <div className="px-4 py-3 border-b bg-muted/50">
            <Badge variant="outline" className="bg-red-50 text-red-700 dark:bg-red-950 dark:text-red-300 border-red-200 dark:border-red-800">
              Previous Value
            </Badge>
          </div>
          <div className="p-4">
            <pre className="text-xs bg-muted p-3 rounded-md overflow-x-auto max-h-96 overflow-y-auto font-mono">
              <code className="text-red-600 dark:text-red-400">
                {previousFormatted}
              </code>
            </pre>
          </div>
        </div>
      )}

      {hasNew && (
        <div className="border rounded-lg overflow-hidden">
          <div className="px-4 py-3 border-b bg-muted/50">
            <Badge variant="outline" className="bg-green-50 text-green-700 dark:bg-green-950 dark:text-green-300 border-green-200 dark:border-green-800">
              New Value
            </Badge>
          </div>
          <div className="p-4">
            <pre className="text-xs bg-muted p-3 rounded-md overflow-x-auto max-h-96 overflow-y-auto font-mono">
              <code className="text-green-600 dark:text-green-400">
                {newFormatted}
              </code>
            </pre>
          </div>
        </div>
      )}
    </div>
  );
}

