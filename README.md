# MarketHub

MarketHub is a Spring Boot backend for a marketplace-style application where users can register, become merchants, manage products, browse public marketplace data, and create purchase orders. The project is focused on backend architecture: domain modeling, REST API design, JWT-based security, database migrations, Dockerized local development, Redis integration, validation, and structured error handling.

> **Project status:** MarketHub is still in active development. The current version already contains the core marketplace backend, authentication, merchant/product/category/order workflows, Redis infrastructure, and Docker setup. Future work may include Google OAuth2 login, a completed shopping cart API, and product image upload/management.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Main Features](#main-features)
- [Technology Stack](#technology-stack)
- [Architecture Overview](#architecture-overview)
- [Domain Model](#domain-model)
- [Security Model](#security-model)
- [Redis Usage](#redis-usage)
- [API Documentation with Swagger](#api-documentation-with-swagger)
- [API Endpoints](#api-endpoints)
- [Environment Variables](#environment-variables)
- [How to Run the Project](#how-to-run-the-project)
- [Docker Setup](#docker-setup)
- [Database Migrations](#database-migrations)
- [Development Notes](#development-notes)

---

## Project Overview

MarketHub models a small online marketplace backend.

The application supports three main roles:

- **Anonymous users** can view public products, categories, and verified active merchants.
- **Authenticated users** can create a merchant profile and place purchase orders.
- **Merchants** can create, view, update, and delete their own products.
- **Admins** can manage categories and verify/enable/disable merchants.

The backend exposes a REST API under:

```text
/api/v1
```

The application uses JWT authentication and is stateless. After login or registration, the client receives a bearer token and sends it in the `Authorization` header for protected endpoints.

```http
Authorization: Bearer <jwt-token>
```

---

## Main Features

### Authentication

- User registration
- User login
- JWT access token generation
- BCrypt password hashing
- Stateless Spring Security configuration
- Custom authenticated user details

### Merchant Management

- Authenticated users can create a merchant profile
- Merchants can view their own merchant profile
- Public users can view active and verified merchants
- Admins can verify merchants
- Admins can enable or disable merchant accounts

### Product Management

- Public product listing
- Public product details
- Merchant-only product creation
- Merchant-only product update
- Merchant-only product deletion/deactivation
- Product ownership validation through authenticated merchant context
- Product stock tracking

### Category Management

- Public active category listing
- Public category details
- Admin-only category creation
- Admin-only category update
- Admin-only category enable/disable operations

### Orders and Purchasing

- Authenticated users can purchase products
- Order items store product and merchant snapshots
- Total order price is calculated on the backend
- Product stock is decreased during purchase
- Pessimistic locking is used when loading buyable products to protect stock consistency under concurrent purchases
- Authenticated users can view their own orders

### Redis Infrastructure

- Redis connection configuration
- Redis cache manager
- JSON cache serialization
- Cache TTL configuration
- Redis-backed rate limiting infrastructure

### Database and Migrations

- PostgreSQL as the main relational database
- JPA/Hibernate for ORM
- Liquibase for schema migrations
- `ddl-auto: validate` to ensure Hibernate validates the schema instead of generating it automatically

### Dockerized Development

- Dockerfile for running the Spring Boot application
- Docker Compose setup with:
  - Spring Boot application
  - PostgreSQL
  - Redis
- Environment-variable-based configuration

---

## Technology Stack

### Language and Runtime

- Java 21
- Maven
- Spring Boot 3

### Spring Ecosystem

- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Cache
- Spring Data Redis

### Persistence

- PostgreSQL
- Hibernate
- Liquibase

### Security

- JWT / JJWT
- BCrypt
- Role-based authorization

### API Documentation

- Springdoc OpenAPI
- Swagger UI

### Mapping and Boilerplate Reduction

- MapStruct
- Lombok

### Infrastructure

- Docker
- Docker Compose
- Redis

---

## Architecture Overview

The project is organized by domain modules rather than by technical layer only. This keeps related controllers, services, repositories, DTOs, mappers, and exceptions close to each other.

Current main packages:

```text
com.tuiop.markethub
├── admin
├── auth
├── carts
├── categories
├── common
├── merchants
├── orders
├── products
├── ratelimiter
├── security
└── users
```

### Typical Request Flow

```text
HTTP Request
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
Database
   ↓
Mapper
   ↓
DTO Response
```

### Design Principles Used

- Controllers remain thin and delegate business logic to services.
- Services contain transactional business operations.
- Repositories handle persistence access.
- DTOs separate API contracts from JPA entities.
- MapStruct handles entity-to-response mapping.
- Liquibase controls database schema changes.
- Spring Security handles route protection and method-level authorization.
- Global exception handling provides structured API error responses.

---

## Domain Model

The current backend contains the following main domain objects:

### User

Represents a registered user of the system.

Main responsibilities:

- Stores user identity data
- Stores encoded password
- Stores role information
- Can become a merchant

Roles:

```text
USER
MERCHANT
ADMIN
```

### Merchant

Represents a shop owned by a user.

Main responsibilities:

- Stores shop name and description
- Tracks whether the merchant is verified
- Tracks whether the merchant is active
- Owns products

### Product

Represents an item offered by a merchant.

Main responsibilities:

- Stores product name and description
- Stores price in cents
- Stores stock quantity
- Belongs to a category
- Belongs to a merchant
- Can be active/inactive

### Category

Represents a public product category.

Main responsibilities:

- Groups products
- Can be active or inactive
- Managed by admins

### Order

Represents a purchase made by an authenticated user.

Main responsibilities:

- Stores order status
- Stores payment status
- Stores total price
- Owns order items

### OrderItem

Represents a product inside an order.

Important detail: order items store snapshots such as product name, merchant name, and price at purchase time. This protects historical order data from later product or merchant changes.

### Cart and CartItem

Cart entities are already present in the domain model, but the public cart API is not the main focus of the current version. Cart functionality is planned to be expanded later.

### ProductImage

Product image entity support exists in the domain model, but full image upload and product image management are planned for later development.

---

## Security Model

MarketHub uses stateless JWT-based security.

### Authentication Flow

1. User registers or logs in.
2. Backend validates credentials.
3. Backend returns a JWT token.
4. Client sends the token in future requests:

```http
Authorization: Bearer <jwt-token>
```

5. JWT filter validates the token and loads the authenticated user.

### Public Endpoints

The following endpoints are publicly accessible:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/products
GET  /api/v1/products/{productId}
GET  /api/v1/categories
GET  /api/v1/categories/{categoryId}
GET  /api/v1/merchants
GET  /swagger-ui/**
GET  /v3/api-docs/**
```

### Protected Endpoints

Authenticated users can:

```text
POST /api/v1/merchants/me
POST /api/v1/orders/purchase
GET  /api/v1/orders/me
```

Merchants can:

```text
POST   /api/v1/merchant/products
GET    /api/v1/merchant/products
GET    /api/v1/merchant/products/{productId}
PUT    /api/v1/merchant/products/{productId}
DELETE /api/v1/merchant/products/{productId}
GET    /api/v1/merchants/me
```

Admins can:

```text
GET   /api/v1/admin/categories
POST  /api/v1/admin/categories
PUT   /api/v1/admin/categories/{categoryId}
PATCH /api/v1/admin/categories/{categoryId}/enable
PATCH /api/v1/admin/categories/{categoryId}/disable
GET   /api/v1/admin/merchants/unverified
PATCH /api/v1/admin/merchants/{merchantId}/verify
PATCH /api/v1/admin/merchants/{merchantId}/enable
PATCH /api/v1/admin/merchants/{merchantId}/disable
```

---

## Redis Usage

Redis is used as infrastructure for caching and rate limiting.

### Cache Configuration

The project uses Spring Cache with Redis as the cache provider.

Current cache configuration includes:

- Redis-backed `CacheManager`
- JSON serialization through `GenericJackson2JsonRedisSerializer`
- String key serialization
- Disabled null-value caching
- Default TTL of 30 minutes

### Current Cache Use Cases

Current cache-related logic includes:

- Caching public product details
- Caching category details
- Evicting cached product/category data when related data changes
- Evicting product cache after purchase-related stock changes

### Rate Limiting

A fixed-window rate limiter is included in the project. It uses Redis to count requests within a configured time window.

Rate limit settings are configured with:

```yaml
app:
  rate-limit:
    enabled: true
    request-limit: ${REQUEST_LIMIT}
    window-size: ${WINDOW_SIZE}
```

Example:

```env
REQUEST_LIMIT=60
WINDOW_SIZE=1m
```

---

## API Documentation with Swagger

This project uses Springdoc OpenAPI and Swagger UI.

After starting the application, open:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON is available at:

```text
http://localhost:8080/v3/api-docs
```

Swagger UI can be used to test endpoints directly from the browser.

For protected endpoints, first call:

```text
POST /api/v1/auth/login
```

Then copy the returned JWT token and authorize requests with:

```text
Bearer <token>
```

---

## API Endpoints

### Authentication API

Base path:

```text
/api/v1/auth
```

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/register` | Public | Registers a new user and returns a JWT token |
| POST | `/login` | Public | Authenticates an existing user and returns a JWT token |

#### Register Request

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "birthDate": "2000-01-01",
  "email": "john.doe@example.com",
  "password": "password123"
}
```

#### Login Request

```json
{
  "email": "john.doe@example.com",
  "password": "password123"
}
```

#### Auth Response

```json
{
  "token": "jwt-token-value",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

---

### Public Product API

Base path:

```text
/api/v1/products
```

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/` | Public | Returns paginated active public products |
| GET | `/{productId}` | Public | Returns one public product by ID |

Example:

```http
GET /api/v1/products?page=0&size=20&sort=createdAt,desc
```

Product response shape:

```json
{
  "id": "product-uuid",
  "merchantId": "merchant-uuid",
  "categoryId": "category-uuid",
  "name": "Mechanical Keyboard",
  "description": "Compact mechanical keyboard",
  "priceCents": 7999,
  "stockQuantity": 15,
  "active": true,
  "createdAt": "2026-05-06T10:00:00Z",
  "updatedAt": "2026-05-06T10:00:00Z"
}
```

---

### Merchant Product API

Base path:

```text
/api/v1/merchant/products
```

Access:

```text
MERCHANT role required
```

| Method | Endpoint | Description |
|---|---|---|
| POST | `/` | Creates a product for the authenticated merchant |
| GET | `/` | Returns the authenticated merchant's products |
| GET | `/{productId}` | Returns one product owned by the authenticated merchant |
| PUT | `/{productId}` | Updates one product owned by the authenticated merchant |
| DELETE | `/{productId}` | Deletes/deactivates one product owned by the authenticated merchant |

#### Create Product Request

```json
{
  "name": "Mechanical Keyboard",
  "description": "Compact keyboard with tactile switches",
  "priceCents": 7999,
  "stockQuantity": 20,
  "categoryId": "category-uuid"
}
```

#### Update Product Request

```json
{
  "name": "Mechanical Keyboard Pro",
  "description": "Updated compact keyboard with tactile switches",
  "priceCents": 8999,
  "stockQuantity": 15,
  "categoryId": "category-uuid",
  "active": true
}
```

---

### Public Category API

Base path:

```text
/api/v1/categories
```

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/` | Public | Returns paginated active categories |
| GET | `/{categoryId}` | Public | Returns one category by ID |

Category response shape:

```json
{
  "id": "category-uuid",
  "name": "Electronics",
  "description": "Electronic devices and accessories",
  "active": true,
  "createdAt": "2026-05-06T10:00:00Z"
}
```

---

### Admin Category API

Base path:

```text
/api/v1/admin/categories
```

Access:

```text
ADMIN role required
```

| Method | Endpoint | Description |
|---|---|---|
| GET | `/` | Returns all categories, including inactive ones |
| POST | `/` | Creates a new category |
| PUT | `/{categoryId}` | Updates an existing category |
| PATCH | `/{categoryId}/enable` | Enables a category |
| PATCH | `/{categoryId}/disable` | Disables a category |

#### Create Category Request

```json
{
  "name": "Electronics",
  "description": "Electronic devices and accessories"
}
```

#### Update Category Request

```json
{
  "name": "Consumer Electronics",
  "description": "Devices, gadgets, and accessories"
}
```

---

### Public Merchant API

Base path:

```text
/api/v1/merchants
```

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/` | Public | Returns paginated active and verified merchants |
| POST | `/me` | Authenticated | Creates a merchant profile for the authenticated user |
| GET | `/me` | MERCHANT | Returns the authenticated merchant's profile |

#### Create Merchant Request

```json
{
  "shopName": "John's Tech Store",
  "description": "Computer hardware and accessories"
}
```

Merchant response shape:

```json
{
  "id": "merchant-uuid",
  "userId": "user-uuid",
  "shopName": "John's Tech Store",
  "description": "Computer hardware and accessories",
  "verified": false,
  "active": true,
  "createdAt": "2026-05-06T10:00:00Z"
}
```

---

### Admin Merchant API

Base path:

```text
/api/v1/admin/merchants
```

Access:

```text
ADMIN role required
```

| Method | Endpoint | Description |
|---|---|---|
| GET | `/unverified` | Returns paginated unverified merchants |
| PATCH | `/{merchantId}/verify` | Verifies a merchant |
| PATCH | `/{merchantId}/disable` | Disables a merchant |
| PATCH | `/{merchantId}/enable` | Enables a merchant |

---

### Order API

Base path:

```text
/api/v1/orders
```

Access:

```text
Authenticated user required
```

| Method | Endpoint | Description |
|---|---|---|
| POST | `/purchase` | Creates a purchase order |
| GET | `/me` | Returns the authenticated user's orders |

#### Purchase Request

```json
{
  "items": [
    {
      "productId": "product-uuid",
      "quantity": 2
    }
  ]
}
```

Order response shape:

```json
{
  "id": "order-uuid",
  "status": "CREATED",
  "totalPriceCents": 15998,
  "items": [
    {
      "id": "order-item-uuid",
      "productId": "product-uuid",
      "merchantId": "merchant-uuid",
      "productNameSnapshot": "Mechanical Keyboard",
      "merchantNameSnapshot": "John's Tech Store",
      "priceSnapshotCents": 7999,
      "quantity": 2,
      "totalPriceSnapshotCents": 15998
    }
  ],
  "createdAt": "2026-05-06T10:00:00Z",
  "paymentStatus": "PENDING"
}
```

---

## Pagination

Endpoints returning pages accept standard Spring pagination parameters:

```text
?page=0&size=20&sort=createdAt,desc
```

Common examples:

```http
GET /api/v1/products?page=0&size=10
GET /api/v1/categories?page=0&size=20&sort=name,asc
GET /api/v1/orders/me?page=0&size=10&sort=createdAt,desc
```

---

## Environment Variables

The application is configured through environment variables.

Create a local `.env` file in the project root when using Docker Compose.

### `.env.example`

```env
# PostgreSQL
POSTGRES_DB=markethub
POSTGRES_USER=markethub_user
POSTGRES_PASSWORD=replace_me

# Redis
REDIS_PASSWORD=replace_me

# JWT
JWT_SECRET=replace_me_with_a_long_secure_secret_at_least_32_characters
JWT_EXPIRATION=86400000

# Admin bootstrap
APP_ADMIN_EMAIL=admin@example.com
APP_ADMIN_PASSWORD=replace_me

# Rate limiting
REQUEST_LIMIT=60
WINDOW_SIZE=1m
```

Do not commit the real `.env` file to Git.

---

## How to Run the Project

### Prerequisites

You need:

- Java 21
- Maven Wrapper included in the project
- Docker
- Docker Compose

---

## Docker Setup

The simplest way to run the full backend stack is Docker Compose.

### 1. Create `.env`

Copy the example values:

```bash
cp .env.example .env
```

Then adjust secrets if needed.

### 2. Build the JAR

The current Dockerfile expects the built JAR to exist at:

```text
target/market-hub.jar
```

Build it first:

```bash
./mvnw clean package -DskipTests
```

### 3. Start PostgreSQL, Redis, and the application

```bash
docker compose up --build
```

The application should be available at:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

### 4. Stop containers

```bash
docker compose down
```

### 5. Stop containers and remove volumes/data

```bash
docker compose down -v
```

---

## Local Development Without Docker App Container

You can also run PostgreSQL and Redis through Docker, then start the Spring Boot app from IntelliJ or Maven.

Example:

```bash
docker compose up postgres redis
```

Then run the app with the `dev` profile.

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

Make sure your local environment variables match `application-dev.yaml` / `application.yaml` requirements.

---

## Database Migrations

Liquibase is used for database schema management.

Master changelog:

```text
src/main/resources/db/changelog/db.changelog-master.yaml
```

Current changesets include:

```text
001-init-schema.xml
002.drop-currency-from-orders.yaml
```

## Development Notes

### Error Handling

The project uses a global exception handler and structured API error responses. Validation errors, not-found errors, conflict errors, authentication errors, and business exceptions are handled consistently.

### Validation

Request DTOs use Jakarta Bean Validation annotations such as:

```java
@NotBlank
@NotNull
@Email
@Size
@Positive
@Min
```

This keeps invalid input away from the service layer.

### DTO Mapping

MapStruct is used to map entities to response DTOs. This avoids exposing JPA entities directly through the REST API.

### Transactions

Business operations are handled in service methods with Spring transactions. Read operations can use read-only transactions, while create/update/purchase operations use normal transactions.

### Stock Consistency

Purchasing uses pessimistic locking when loading buyable products. This helps prevent overselling when multiple users try to buy the same product concurrently.

---
