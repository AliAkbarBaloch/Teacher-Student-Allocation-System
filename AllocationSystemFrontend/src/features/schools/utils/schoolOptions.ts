import type { TFunction } from "i18next";

import { SCHOOL_TYPE_VALUES } from "../types/school.types";

export const createSchoolTypeOptions = (t: TFunction<"schools">) =>
  SCHOOL_TYPE_VALUES.map((value) => ({
    value,
    label: t(`typeLabels.${value}`),
  }));

