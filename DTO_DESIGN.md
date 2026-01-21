# New DTO Architecture Design

## 1. Overview
This document outlines the new Data Transfer Object (DTO) architecture for the Fuint Food project. The new design abandons the legacy inheritance-based VO pattern in favor of a clean, composition-based, and type-safe architecture.

## 2. Key Principles
-   **Separation of Concerns**: Strict separation between Request (Command/Query) and Response (View) objects.
-   **Composition over Inheritance**: Eliminates deep inheritance hierarchies (e.g., `PageParams`) in favor of composition and interfaces.
-   **Explicit Validation**: Uses standard JSR-303/JSR-380 (Jakarta Validation) annotations with strict Groups.
-   **Compile-time Mapping**: Uses MapStruct for high-performance, type-safe object mapping, replacing reflection-based `BeanUtils`.
-   **Immutability**: Encourages immutable design for DTOs.

## 3. Directory Structure
The new DTOs are located in `com.fuint.openapi.dto`:

```
com.fuint.openapi.dto
├── api                 # Core interfaces
│   ├── BaseRequest.java
│   ├── BaseResponse.java
│   └── Validatable.java
├── request             # Request DTOs (Input)
│   ├── OrderCreateReqDTO.java
│   └── PaginationRequest.java
├── response            # Response DTOs (Output)
│   ├── OrderDetailRespDTO.java
│   └── OrderInfoDTO.java
└── assembler           # Object Mappers
    └── OrderMapper.java
```

## 4. Implementation Details

### 4.1 Base Interfaces
All DTOs implement marker interfaces to ensure type safety and consistent behavior.
-   `BaseRequest`: Marker for input objects.
-   `BaseResponse`: Marker for output objects.
-   `Validatable`: Indicates the object supports self-validation.

### 4.2 Validation
Validation is enforced using standard annotations (`@NotNull`, `@Size`, etc.) directly on DTO fields.
Validation groups (e.g., `Create`, `Update`) can be used to reuse DTOs for different scenarios without inheritance.

### 4.3 Pagination
Pagination is handled via composition. Request DTOs that require pagination include `PaginationRequest` as a field or implement a `Pageable` interface, rather than extending a base class.

### 4.4 Mapping Strategy (MapStruct)
We use MapStruct 1.5.5.Final.
-   **Performance**: Code is generated at compile time. No reflection overhead.
-   **Safety**: Missing mappings cause compile errors (configured to warn/error).
-   **Usage**:
    ```java
    @Mapper(componentModel = "spring")
    public interface OrderMapper {
        OrderCreateReqVO toVO(OrderCreateReqDTO dto);
    }
    ```

## 5. Migration Guide
1.  Create new DTOs in `com.fuint.openapi.dto`.
2.  Define Mappers in `assembler` package.
3.  Update Controller to accept/return new DTOs.
4.  Use Mapper to convert to/from legacy Service VOs (temporary) or update Service to use new DTOs.

## 6. Benefits
-   **Performance**: MapStruct is significantly faster than BeanUtils.
-   **Maintainability**: Explicit mappings are easier to debug than magic reflection.
-   **Clarity**: Flat DTO structures are easier to understand than complex inheritance trees.
-   **Testability**: Pure POJOs are easier to test.
