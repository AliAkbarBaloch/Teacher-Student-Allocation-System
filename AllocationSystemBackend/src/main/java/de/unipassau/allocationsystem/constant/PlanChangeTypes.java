package de.unipassau.allocationsystem.constant;

/**
 * Constants for plan change types used in PlanChangeLog.changeType
 */
public final class PlanChangeTypes {

    private PlanChangeTypes() {
    }

    /** Creation of new allocation plan or plan entity. */
    public static final String CREATE = "CREATE";

    /** Modification of existing plan data or settings. */
    public static final String UPDATE = "UPDATE";

    /** Removal of plan or plan-related entity. */
    public static final String DELETE = "DELETE";

    /** Transition of plan status (e.g., DRAFT to ACTIVE). */
    public static final String STATUS_CHANGE = "STATUS_CHANGE";
}
