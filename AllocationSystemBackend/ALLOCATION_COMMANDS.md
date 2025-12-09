# Command Line Instructions for Running Teacher Allocation

This document provides commands to run the teacher allocation algorithm implementation.

## Prerequisites

1. Ensure you have Java 17+ installed
2. Ensure you have Gradle installed (or use Gradle Wrapper)
3. Ensure your database is configured and running
4. Ensure you have an Academic Year created in the database with the ID you want to use

## Option 1: Using Gradle Run Task (Recommended for Development)

Navigate to the `AllocationSystemBackend` directory and run:

```bash
# Windows (PowerShell/CMD)
cd AllocationSystemBackend
gradlew bootRun --args="2"

# Linux/Mac
cd AllocationSystemBackend
./gradlew bootRun --args="2"
```

Replace `1` with your actual academic year ID.

## Option 2: Build JAR and Run (Recommended for Production)

### Step 1: Build the JAR file

```bash
# Windows
cd AllocationSystemBackend
gradlew build

# Linux/Mac
cd AllocationSystemBackend
./gradlew build
```

The JAR file will be created at: `AllocationSystemBackend/build/libs/AllocationSystemBackend-0.0.1-SNAPSHOT.jar`

### Step 2: Run the JAR with academic year ID

```bash
# Windows
java -jar build\libs\AllocationSystemBackend-0.0.1-SNAPSHOT.jar 1

# Linux/Mac
java -jar build/libs/AllocationSystemBackend-0.0.1-SNAPSHOT.jar 1
```

Replace `1` with your actual academic year ID.

## Option 3: Using Gradle Application Plugin

```bash
# Windows
cd AllocationSystemBackend
gradlew run --args="2"

# Linux/Mac
cd AllocationSystemBackend
./gradlew run --args="2"
```

## Option 4: Run Without Arguments (Info Mode)

If you run without arguments, the application will start normally but won't execute the allocation. It will just log instructions:

```bash
# Windows
gradlew bootRun

# Linux/Mac
./gradlew bootRun
```

## Examples

### Example 1: Run allocation for Academic Year ID 1
```bash
./gradlew bootRun --args="2"
```

### Example 2: Run allocation for Academic Year ID 5
```bash
./gradlew bootRun --args="5"
```

### Example 3: Build and run JAR for Academic Year ID 1
```bash
./gradlew build
java -jar build/libs/AllocationSystemBackend-0.0.1-SNAPSHOT.jar 1
```

## Expected Output

When the allocation runs successfully, you should see logs like:

```
INFO  - Starting allocation process for academic year ID: 1
INFO  - Allocation process completed successfully!
INFO  - Allocation Plan ID: 123, Status: APPROVED, Version: 1.0
```

## Troubleshooting

### Error: "Academic year with ID X not found"
- Ensure the academic year exists in your database
- Check that the database connection is configured correctly
- Verify the academic year ID is correct

### Error: "Academic year is locked and cannot be modified"
- The academic year has been locked
- You may need to unlock it in the database or through the application

### Error: "Invalid academic year ID format"
- Ensure you're passing a numeric value
- Example: `1` not `"1"` or `one`

### Application doesn't start
- Check database connection settings in `application.properties`
- Ensure all required database tables exist (Flyway migrations should handle this)
- Check Java version (requires Java 17+)

## Programmatic Usage

You can also call the allocation service programmatically from your code:

```java
@Autowired
private TeacherAllocationService teacherAllocationService;

public void runAllocation(Long academicYearId) {
    AllocationPlan plan = teacherAllocationService.performAllocation(academicYearId);
    // Handle the result
}
```

## Notes

- The allocation process is transactional - if it fails, all changes will be rolled back
- The allocation creates a new AllocationPlan with status DRAFT initially, then updates it to APPROVED upon completion
- Allocation warnings are created for any unmet demands
- Credit hour tracking is updated for all teachers after allocation

