
import { Checkbox } from "@/components/ui/checkbox";
import { useTranslation } from "react-i18next";

type Subject = {
    id: number;
    subjectTitle: string;
}

type TeacherSubjectsFieldProps = {
    subjects: Subject[];
    subjectsLoading: boolean;
    selectedSubjectIds: number[];
    disabled: boolean;
    onChange: (nextIds: number[]) => void;
}


export function TeacherSubjectsField({
    subjects,
    subjectsLoading,
    selectedSubjectIds,
    disabled,
    onChange,
}: TeacherSubjectsFieldProps) {

    const { t } = useTranslation("teachers");
    return (
        <div className="space-y-2 md:col-span-2">
            {/* title for subjects checkboxes */}
            <label className="text-sm font-medium">
                {t("form.fields.subjects")}
            </label>

            {/*conditional rendering*/}

            {/*if subjects are still loading show Loading subjects*/}
            {subjectsLoading ? (
                <p className="text-sm text-muted-foreground">
                    {t("form.loadingSubjects", { defaultValue: "Loading subjects..." })}
                </p>
                //else if subjects exists - show checkboxes
            ) : subjects.length ? (
                <div className="grid grid-cols-2 gap-3">

                    {/*for every subject we recieved from the backend draw checkbox*/}
                    {subjects.map((subject) => {

                        //is this subject already checked 
                        const checked = selectedSubjectIds.includes(subject.id);

                        return (
                            <div
                                key={subject.id}
                                className="flex items-center gap-2 rounded-md border px-3 py-2"
                            >
                                <Checkbox
                                    checked={checked}
                                    disabled={disabled}
                                    id={`subject-${subject.id}`}

                                    onCheckedChange={(value) => {
                                        const nextIds = value
                                            //check - add the subject to checked 
                                            ? [...selectedSubjectIds, subject.id]
                                            // uncheck - delete 
                                            : selectedSubjectIds.filter((id) => id !== subject.id);
                                        //update the form state
                                        onChange(nextIds);
                                    }}
                                />
                                <label
                                    htmlFor={`subject-${subject.id}`}
                                    className="text-sm cursor-pointer select-none"
                                >
                                    {subject.subjectTitle}
                                </label>

                            </div>
                        );
                    }
                    )}
                </div>
                //else = no subjects at all - show "no data"
            ) : (
                <p className="text-sm text-muted-foreground">{t("table.noData")}</p>
            )
            }
        </div>
    );

}