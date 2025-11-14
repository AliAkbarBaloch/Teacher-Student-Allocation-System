# Allocation System Backend - API (initial)

This file describes the initial REST API contract and how to test the dummy endpoints included for development.

## OpenAPI spec

The API contract (OpenAPI 3.0) is available at `src/main/resources/openapi.yaml` in this module. You can open the file directly or serve it using Spring Boot static resources.

Dummy endpoints (available after running the backend):

- GET /api/v1/allocations

  - Returns an array of allocations (in-memory)

- POST /api/v1/allocations

  - Creates an allocation (dummy). Request body:
    {
    "studentId": "stu-123",
    "roomId": "room-A"
    }
  - Responses: 201 with created allocation, 400 with ErrorResponse on invalid input

- GET /api/v1/students/{studentId}

  - Returns a sample student object. If `studentId` is `not-found`, returns 404

- GET /api/v1/rooms
  - Returns a list of dummy rooms

## Error format

All error responses conform to the `ErrorResponse` schema defined in the OpenAPI file:

{
"code": "ERROR_CODE",
"message": "Human readable message"
}

## How to run

Use the existing Gradle wrapper in this module:

1. Build: `./gradlew build`
2. Run: `./gradlew bootRun` (if Spring Boot plugin configured in build.gradle)

## Notes

This is an initial, minimal setup meant for local testing and contract design. Next steps could include:

- adding swagger-ui or springdoc-openapi to serve a browsable spec
- adding integration tests for the endpoints
- replacing in-memory behavior with real services/repositories
