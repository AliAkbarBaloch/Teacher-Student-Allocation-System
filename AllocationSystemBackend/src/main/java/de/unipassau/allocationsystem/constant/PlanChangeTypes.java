package de.unipassau.allocationsystem.constant;

/**
 * Constants for plan change types used in PlanChangeLog.changeType
 */
public final class PlanChangeTypes {

    private PlanChangeTypes() {
    }

    /** Creates a new plan or plan entity. */
    public static final String CREATE = "CREATE";
    
    /** Updates an existing plan. */
    public static final String UPDATE = "UPDATE";
    
    /** Deletes a plan or plan entity. */
    public static final String DELETE = "DELETE";
    
    /** Changes plan status. */
    public static final String STATUS_CHANGE = "STATUS_CHANGE";
}
