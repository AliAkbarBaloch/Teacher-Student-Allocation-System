package de.unipassau.allocationsystem.constant;

/**
 * Constants for plan change types used in PlanChangeLog.changeType
 */
public final class PlanChangeTypes {

    /** Change type for creation operations. */
    public static final String CREATE = "CREATE";
    
    /** Change type for update/modification operations. */
    public static final String UPDATE = "UPDATE";
    
    /** Change type for deletion operations. */
    public static final String DELETE = "DELETE";
    
    /** Change type for status change operations. */
    public static final String STATUS_CHANGE = "STATUS_CHANGE";

    private PlanChangeTypes() {

    }
}
