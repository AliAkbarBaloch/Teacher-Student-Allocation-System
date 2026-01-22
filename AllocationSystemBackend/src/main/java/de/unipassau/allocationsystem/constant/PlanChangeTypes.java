package de.unipassau.allocationsystem.constant;

/**
 * Constants for plan change types used in PlanChangeLog.changeType
 */
public final class PlanChangeTypes {

    private PlanChangeTypes() {
    }

    public static final String CREATE = "CREATE"; // New plan creation
    
    public static final String UPDATE = "UPDATE"; // Plan modification
    
    public static final String DELETE = "DELETE"; // Plan deletion
    
    public static final String STATUS_CHANGE = "STATUS_CHANGE"; // Status transition
}
