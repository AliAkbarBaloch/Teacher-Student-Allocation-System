package de.unipassau.allocationsystem.constant;

/**
 * Constants for plan change types used in PlanChangeLog.changeType
 */
public final class PlanChangeTypes {

    private PlanChangeTypes() {
    }

    /** Change type indicating creation of a new plan or plan entity. */
    public static final String CREATE = "CREATE";
    
    /** Change type indicating modification of an existing plan. */
    public static final String UPDATE = "UPDATE";
    
    /** Change type indicating deletion of a plan or plan entity. */
    public static final String DELETE = "DELETE";
    
    /** Change type indicating a status transition of a plan. */
    public static final String STATUS_CHANGE = "STATUS_CHANGE";
}
