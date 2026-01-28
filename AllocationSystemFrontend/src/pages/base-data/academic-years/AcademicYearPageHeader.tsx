import { Plus } from "lucide-react";
import { Button } from "@/components/ui/button";

export function AcademicYearPageHeader(props: {
    title: string;
    subtitle: string;
    createLabel: string;
    onCreateClick: () => void;
}) {
    const { title, subtitle, createLabel, onCreateClick } = props;

    return (
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
            <div>
                <h2 className="text-2xl font-semibold tracking-tight">{title}</h2>
                <p className="text-muted-foreground text-sm mt-1">{subtitle}</p>
            </div>

            <Button onClick={onCreateClick}>
                <Plus className="mr-2 h-4 w-4" />
                {createLabel}
            </Button>
        </div>
    );
}
