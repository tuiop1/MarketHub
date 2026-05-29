# MarketHub

MarketHub is a Spring Boot backend for a marketplace-style application. It supports local and Google OAuth2 authentication, JWT-based API security, merchant onboarding, admin-managed categories and merchant verification, product management, product images stored in MinIO, shopping carts, checkout, order creation, Redis caching, Redis-backed rate limiting, PostgreSQL persistence, Liquibase migrations, Swagger/OpenAPI documentation, and Dockerized local development.
---

## Table of Contents

- [Project Overview](#project-overview)
- [Main Features](#main-features)
- [Technology Stack](#technology-stack)
- [Architecture Overview](#architecture-overview)
- [Domain Model](#domain-model)
- [Security Model](#security-model)
- [Product Image Storage](#product-image-storage)
- [Redis Usage](#redis-usage)
- [API Documentation](#api-documentation)
- [API Endpoints](#api-endpoints)
- [Environment Variables](#environment-variables)
- [How to Run the Project](#how-to-run-the-project)
- [Database Migrations](#database-migrations)
- [Development Notes](#development-notes)

---

## Project Overview

MarketHub models a small online marketplace backend with role-based access control.

The application supports four main access levels:

- **Anonymous users** can browse public products, public categories, verified active merchants, and public product images.
- **Authenticated users** can create a merchant profile, manage their cart, purchase products directly, purchase their cart, and view their own orders.
- **Merchants** can manage their own products and product images after their merchant account is verified and active.
- **Admins** can manage categories and verify, enable, or disable merchants.

The backend exposes a REST API under:

```text
/api/v1
```

For protected endpoints, clients send the JWT as a bearer token:

```http
Authorization: Bearer <jwt-token>
```

---

## Main Features

### Authentication and Security

- User registration with email and password.
- Local login with JWT access token generation.
- Google OAuth2 login using Spring Security OAuth2 Client.
- Internal JWT returned after successful Google login.
- BCrypt password hashing for local accounts.
- `LOCAL` and `GOOGLE` authentication provider support on the user model.
- Stateless JWT authentication for API requests after login.
- Temporary session support for the OAuth2 authorization-code redirect flow.
- Custom JWT filter and authentication entry point.
- Role-based access control with `USER`, `MERCHANT`, and `ADMIN` roles.
- Method-level authorization with `@PreAuthorize`.

### User and Admin Bootstrap

- User entity with UUID primary keys.
- Admin user bootstrap from environment variables.
- Admin creation is skipped if the configured admin email already exists.
- OAuth-compatible user schema where password, first name, last name, and birth date may be nullable for OAuth users.

### Merchant Management

- Authenticated users can create a merchant profile.
- Merchant creation upgrades the user role to `MERCHANT`.
- Merchants can view their own merchant profile.
- Public users can view active and verified merchants.
- Admins can list unverified merchants.
- Admins can verify merchants.
- Admins can enable or disable merchants.
- Shop name uniqueness is enforced.

### Category Management

- Public active category listing.
- Public category details.
- Admin-only category listing including inactive categories.
- Admin-only category creation.
- Admin-only category update.
- Admin-only category enable/disable operations.

### Product Management

- Public product listing for active products from active, verified merchants and active categories.
- Public product details with product images.
- Merchant-only product creation.
- Merchant-only product listing and details for the authenticated merchant.
- Merchant-only product update.
- Merchant-only soft deletion/deactivation.
- Product ownership validation through authenticated merchant context.
- Product stock tracking.
- Prices stored as integer cents via `priceCents`.
- JPA `@EntityGraph` usage for controlled fetching of related entities and images.
- Pessimistic locking for selected product operations that need consistency.

### Product Images

- Merchant product image upload with multipart requests.
- Merchant product image deletion.
- Public product image download as a Spring `Resource`.
- Image metadata stored in PostgreSQL.
- Image binary content stored in MinIO through an object-storage abstraction.
- Product image response included in public and merchant product responses.
- Per-product image limit.
- File size validation.
- Content-type allowlist for image uploads.
- Image position support for stable ordering.
- Cache eviction when product images are added or removed.

### Shopping Cart

- One cart per user.
- Lazy cart creation when a user adds the first item.
- Add product to cart.
- Increment quantity when the same product already exists in the cart.
- Remove cart item from own cart.
- View own cart with calculated totals.
- Product availability validation when adding to cart.
- Final quantity stock validation when increasing an existing cart item.
- Pessimistic lock on existing cart items during quantity updates.
- Unique database constraint for `(cart_id, product_id)`.

### Orders and Purchasing

- Authenticated users can purchase products directly from a request body.
- Authenticated users can purchase all items from their cart.
- Cart checkout clears the cart after successful order creation.
- Purchase requests merge duplicated product IDs before order creation.
- Order items store product, merchant, and price snapshots.
- Total order price is calculated on the backend.
- Product stock is decreased during purchase.
- Pessimistic locking is used when loading buyable products to prevent overselling.
- Users cannot purchase their own merchant products.
- Authenticated users can view their own orders.
- Order status and payment status are modeled separately.

### Redis Infrastructure

- Redis-backed Spring Cache configuration.
- JSON cache serialization with `GenericJackson2JsonRedisSerializer`.
- String key serialization.
- Disabled null-value caching.
- Default cache TTL of 30 minutes.
- Redis-backed fixed-window rate limiter.
- Structured `429 Too Many Requests` responses with `Retry-After` header.

### Database and Migrations

- PostgreSQL as the main relational database.
- JPA/Hibernate for ORM.
- Liquibase for schema migrations.
- `ddl-auto: validate` for schema validation instead of automatic schema generation.
- UUID primary keys.
- Database indexes for frequently queried foreign keys and fields.
- Unique constraints for user email, merchant shop name, one cart per user, and one cart item per product in a cart.

### Dockerized Development

- Dockerfile for running the Spring Boot application from the built JAR.
- Docker Compose setup with:
  - Spring Boot application
  - PostgreSQL
  - Redis
  - MinIO
- Persistent Docker volumes for PostgreSQL and MinIO.
- Environment-variable-based configuration.
- Separate Docker profile configuration.

---

## Technology Stack

### Language and Runtime

- Java 21
- Maven
- Spring Boot 3.5.x

### Spring Ecosystem

- Spring Web
- Spring Data JPA
- Spring Security
- Spring Security OAuth2 Client
- Spring Validation
- Spring Cache
- Spring Data Redis

### Persistence and Migrations

- PostgreSQL
- Hibernate
- Liquibase
- H2 for tests

### Security

- JWT / JJWT
- BCrypt
- Google OAuth2 login
- Role-based authorization
- Method-level security

### Object Storage

- MinIO
- MinIO Java SDK
- Object-storage abstraction for upload, download, and delete operations

### API Documentation

- Springdoc OpenAPI
- Swagger UI

### Mapping and Boilerplate Reduction

- MapStruct
- Lombok
- Lombok MapStruct Binding

### Infrastructure

- Docker
- Docker Compose
- Redis
- MinIO

---

## Architecture Overview

The project is organized by domain modules rather than by technical layer only. Related controllers, services, repositories, DTOs, mappers, and exceptions are kept close to their domain.

Current main packages:

```text
com.tuiop.markethub
├── admin
├── auth
│   ├── dto
│   └── oauth2
├── carts
├── categories
├── common
│   ├── exceptions
│   ├── redis
│   └── storage
├── merchants
├── orders
├── products
│   └── images
├── ratelimiter
├── security
└── users
```

Typical request flow:

```text
HTTP Request
   ↓
Spring Security filter chain / RateLimitFilter
   ↓
Controller
   ↓
Service
   ↓
Repository / ObjectStorageService
   ↓
Database / Redis / MinIO
   ↓
Mapper
   ↓
DTO Response
```

Design principles used:

- Controllers stay thin and delegate business logic to services.
- Services contain transactional business operations.
- Repositories handle persistence queries.
- DTOs separate API contracts from JPA entities.
- MapStruct maps entities to response DTOs.
- Liquibase owns database schema evolution.
- Spring Security handles route protection, OAuth2 login, JWT authentication, and role checks.
- Global exception handling returns structured API errors.
- Object storage is abstracted behind `ObjectStorageService`.

---

## Domain Model

### User

Represents a registered user.

Main responsibilities:

- Stores identity data.
- Stores local password hash when the account uses local authentication.
- Stores authentication provider data for local and Google users.
- Stores role information.
- Can become a merchant.

Roles:

```text
USER
MERCHANT
ADMIN
```

Authentication providers:

```text
LOCAL
GOOGLE
```

### Merchant

Represents a shop owned by a user.

Main responsibilities:

- Stores shop name and description.
- Tracks whether the merchant is verified.
- Tracks whether the merchant is active.
- Owns products.

### Category

Represents a public product category.

Main responsibilities:

- Groups products.
- Can be active or inactive.
- Managed by admins.

### Product

Represents an item offered by a merchant.

Main responsibilities:

- Stores product name and description.
- Stores price in cents.
- Stores stock quantity.
- Belongs to a category.
- Belongs to a merchant.
- Can be active or inactive.
- Owns product images.

### ProductImage

Represents metadata for an image attached to a product.

Main responsibilities:

- Stores product relation.
- Stores MinIO object key.
- Stores content type.
- Stores file size.
- Stores image position.
- Controls public image download through product visibility rules.

### Cart

Represents a user's shopping cart.

Main responsibilities:

- Belongs to one user.
- Owns cart items.
- Enforces one cart per user.
- Provides calculated cart totals through DTO mapping.

### CartItem

Represents one product in a cart.

Main responsibilities:

- Belongs to a cart.
- References a product.
- Stores quantity.
- Enforces one cart item per product in the same cart.

### Order

Represents a purchase made by an authenticated user.

Main responsibilities:

- Stores order status.
- Stores payment status.
- Stores total price.
- Owns order items.

### OrderItem

Represents a product inside an order.

Important detail: order items store snapshots such as product name, merchant name, and price at purchase time. This protects historical order data from later product or merchant changes.

---

## Security Model

MarketHub uses a hybrid login model:

```text
Local login / Google OAuth2 login
   ↓
MarketHub JWT
   ↓
Bearer token for API requests
```

### Local Authentication Flow

1. User registers or logs in through `/api/v1/auth`.
2. Backend validates credentials.
3. Backend returns a JWT token.
4. Client sends the token in future protected requests.
5. JWT filter validates the token and loads the authenticated user.

### Google OAuth2 Flow

1. Client opens:

```text
/oauth2/authorization/google
```

2. Spring Security redirects the user to Google.
3. Google redirects back to:

```text
/login/oauth2/code/google
```

4. Spring Security validates the OAuth2 callback.
5. `OAuth2LoginSuccessHandler` calls `OAuth2LoginService`.
6. The service finds or creates a MarketHub user.
7. The backend returns a normal MarketHub JWT.

### Public Endpoints

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/products
GET  /api/v1/products/{productId}
GET  /api/v1/categories
GET  /api/v1/categories/{categoryId}
GET  /api/v1/merchants
GET  /api/v1/product-images/{productImageId}/content
GET  /oauth2/authorization/google
GET  /login/oauth2/code/google
GET  /swagger-ui/**
GET  /v3/api-docs/**
```

### Protected Endpoints

Authenticated users:

```text
POST   /api/v1/merchants/me
GET    /api/v1/cart
POST   /api/v1/cart/items
DELETE /api/v1/cart/items/{id}
POST   /api/v1/cart/purchase
POST   /api/v1/orders/purchase
GET    /api/v1/orders/me
```

Merchants:

```text
GET    /api/v1/merchants/me
POST   /api/v1/merchant/products
GET    /api/v1/merchant/products
GET    /api/v1/merchant/products/{productId}
PUT    /api/v1/merchant/products/{productId}
DELETE /api/v1/merchant/products/{productId}
POST   /api/v1/products/{productId}/images
DELETE /api/v1/products/{productId}/images/{imageId}
```

Admins:

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

## Product Image Storage

Product image binary data is stored in MinIO, while image metadata is stored in PostgreSQL.

Metadata stored in the database includes:

```text
id
product_id
object_key
content_type
size_bytes
position_number
created_at
```

The service flow is:

```text
Multipart upload request
   ↓
Product ownership and visibility validation
   ↓
Image count / file size / content type validation
   ↓
Object uploaded to MinIO
   ↓
ProductImage metadata saved in PostgreSQL
   ↓
Product cache evicted
```

Download flow:

```text
GET /api/v1/product-images/{productImageId}/content
   ↓
Check that the image belongs to a publicly visible product
   ↓
Download object stream from MinIO
   ↓
Return Spring Resource with inline Content-Disposition
```

---

## Redis Usage

Redis is used for caching and rate limiting.

### Cache Configuration

Current cache configuration includes:

- Redis-backed `CacheManager`.
- JSON value serialization through `GenericJackson2JsonRedisSerializer`.
- String key serialization.
- Null-value caching disabled.
- Default TTL of 30 minutes.

### Current Cache Use Cases

- Caching public product details.
- Evicting cached product data when products are updated or deactivated.
- Evicting cached product data when product images are uploaded or deleted.
- Evicting public product cache after purchase-related stock changes.

### Rate Limiting

The rate limiter uses a fixed-window algorithm backed by Redis.

Configuration:

```yaml
app:
  rate-limit:
    enabled: true
    request-limit: ${REQUEST_LIMIT:10}
    window-size: ${WINDOW_SIZE:1m}
```

Example:

```env
REQUEST_LIMIT=60
WINDOW_SIZE=1m
```

When the limit is exceeded, the API returns:

```text
429 Too Many Requests
```

with a structured `ApiError` response and a `Retry-After` header.

---

## API Documentation

The project uses Springdoc OpenAPI and Swagger UI.

After starting the application, open:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON is available at:

```text
http://localhost:8080/v3/api-docs
```

For protected endpoints, first call:

```text
POST /api/v1/auth/login
```

Then authorize Swagger requests with:

```text
Bearer <token>
```

For Google login, start the browser OAuth flow at:

```text
http://localhost:8080/oauth2/authorization/google
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

Register request:

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "birthDate": "2000-01-01",
  "email": "john.doe@example.com",
  "password": "password123"
}
```

Login request:

```json
{
  "email": "john.doe@example.com",
  "password": "password123"
}
```

Auth response:

```json
{
  "token": "jwt-token-value",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

### OAuth2 Login

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/oauth2/authorization/google` | Public | Starts Google OAuth2 login |
| GET | `/login/oauth2/code/google` | Public | OAuth2 callback handled by Spring Security |

Successful Google login returns the same `AuthResponse` shape as local login.

### Public Product API

Base path:

```text
/api/v1/products
```

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/` | Public | Returns paginated active public products |
| GET | `/{productId}` | Public | Returns one public product by ID |
| POST | `/{productId}/images` | MERCHANT | Uploads an image for an owned product |
| DELETE | `/{productId}/images/{imageId}` | MERCHANT | Deletes an image from an owned product |

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
  "updatedAt": "2026-05-06T10:00:00Z",
  "images": [
    {
      "id": "image-uuid",
      "productId": "product-uuid",
      "filename": "object-key.webp",
      "contentType": "image/webp",
      "sizeBytes": 123456,
      "createdAt": "2026-05-06T10:00:00Z"
    }
  ]
}
```

Image upload example:

```http
POST /api/v1/products/{productId}/images?position=1
Content-Type: multipart/form-data

image=<file>
```

### Public Product Image API

Base path:

```text
/api/v1/product-images
```

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/{productImageId}/content` | Public | Downloads image content for a publicly visible product image |

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
| DELETE | `/{productId}` | Deactivates one product owned by the authenticated merchant |

Create product request:

```json
{
  "name": "Mechanical Keyboard",
  "description": "Compact keyboard with tactile switches",
  "priceCents": 7999,
  "stockQuantity": 20,
  "categoryId": "category-uuid"
}
```

Update product request:

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

Create category request:

```json
{
  "name": "Electronics",
  "description": "Electronic devices and accessories"
}
```

Update category request:

```json
{
  "name": "Consumer Electronics",
  "description": "Devices, gadgets, and accessories"
}
```

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

Create merchant request:

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

### Cart API

Base path:

```text
/api/v1/cart
```

Access:

```text
Authenticated user required
```

| Method | Endpoint | Description |
|---|---|---|
| GET | `/` | Returns the authenticated user's cart |
| POST | `/items` | Adds a product to the cart or increases its quantity |
| DELETE | `/items/{id}` | Removes a cart item from the authenticated user's cart |
| POST | `/purchase` | Creates an order from the current cart and clears the cart |

Add to cart request:

```json
{
  "productId": "product-uuid",
  "quantity": 2
}
```

Cart response shape:

```json
{
  "id": "cart-uuid",
  "userId": "user-uuid",
  "totalPriceCents": 15998,
  "cartItems": [
    {
      "id": "cart-item-uuid",
      "cartId": "cart-uuid",
      "productId": "product-uuid",
      "productName": "Mechanical Keyboard",
      "priceCents": 7999,
      "quantity": 2,
      "totalPriceCents": 15998,
      "productActive": true,
      "stockQuantity": 15
    }
  ]
}
```

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
| POST | `/purchase` | Creates a purchase order directly from request items |
| GET | `/me` | Returns the authenticated user's orders |

Purchase request:

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

The application is configured through environment variables. Create a local `.env` file in the project root when using Docker Compose.

Example `.env`:

```env
# PostgreSQL
POSTGRES_DB=markethub
POSTGRES_USER=markethub_user
POSTGRES_PASSWORD=replace_me

# Redis
REDIS_PASSWORD=replace_me

# Google OAuth2
GOOGLE_CLIENT_ID=replace_me
GOOGLE_CLIENT_SECRET=replace_me

# JWT
JWT_SECRET=replace_me_with_a_long_secure_secret_at_least_32_characters
JWT_EXPIRATION=86400000

# Admin bootstrap
APP_ADMIN_EMAIL=admin@example.com
APP_ADMIN_PASSWORD=replace_me

# MinIO
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=replace_me
APP_MINIO_BUCKET=markethub-images

# Product images
APP_PRODUCT_IMAGES_MAX_FILE_SIZE_BYTES=5242880
APP_PRODUCT_IMAGES_LIMIT=5
APP_IMAGES_ALLOWED_TYPES=image/jpeg,image/png,image/webp

# Rate limiting
REQUEST_LIMIT=60
WINDOW_SIZE=1m
```

Do not commit the real `.env` file to Git.

### Google OAuth2 Redirect URI

For local development, the Google Cloud Console OAuth client should include this authorized redirect URI:

```text
http://localhost:8080/login/oauth2/code/google
```

---

## How to Run the Project

### Prerequisites

You need:

- Java 21
- Maven Wrapper included in the project
- Docker
- Docker Compose
- Google OAuth2 client credentials if OAuth login is enabled/used

### Docker Setup

The simplest way to run the full backend stack is Docker Compose.

#### 1. Create `.env`

Create a `.env` file and fill in the variables listed above.

#### 2. Build the JAR

The current Dockerfile expects the built JAR to exist at:

```text
target/market-hub.jar
```

Build it first:

```bash
./mvnw clean package -DskipTests
```

#### 3. Start PostgreSQL, Redis, MinIO, and the application

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

MinIO console:

```text
http://localhost:9001
```

#### 4. Stop containers

```bash
docker compose down
```

#### 5. Stop containers and remove volumes/data

```bash
docker compose down -v
```

### Local Development Without Docker App Container

You can run PostgreSQL, Redis, and MinIO through Docker, then start the Spring Boot app from IntelliJ or Maven.

Example:

```bash
docker compose up postgres redis minio
```

Then run the app locally:

```bash
./mvnw spring-boot:run
```

When using the Compose Redis service with the default password, set the matching local Redis password before starting the app:

```bash
export SPRING_REDIS_PASSWORD=redis_password
```

Make sure your local environment variables match `application.yaml`.

---

## Database Migrations

Liquibase is used for database schema management.

Master changelog:

```text
src/main/resources/db/changelog/db.changelog-master.yaml
```

Current changelog files:

```text
001-init-schema.xml
002-drop-currency-from-orders.yaml
003-migrate-product-images-to-minio.yaml
004-add-column-size_bytes-to-product_images.yaml
005-change-type-size_bytes-product_images.yaml
006-support-oauth-users.yaml
```

The schema includes tables for:

```text
users
merchants
categories
products
product_images
carts
cart_items
orders
order_items
```

---

## Development Notes

### Error Handling

The project uses a global exception handler and structured API error responses.

Error response shape:

```json
{
  "timestamp": "2026-05-06T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found",
  "path": "/api/v1/products/product-uuid"
}
```

Handled cases include:

- invalid JSON request bodies
- validation errors
- not-found errors
- conflict errors
- file upload errors
- unsupported media types
- file size limit errors
- object storage failures
- generic fallback errors

### Validation

Request DTOs use Jakarta Bean Validation annotations such as:

```java
@NotBlank
@NotNull
@Email
@Size
@Positive
@Min
@NotEmpty
@Valid
```

This keeps invalid input away from the service layer.

### DTO Mapping

MapStruct is used to map entities to response DTOs. JPA entities are not exposed directly through the REST API.

### Transactions

Business operations are handled in service methods with Spring transactions.

- Read operations use read-only transactions where applicable.
- Create/update/delete/purchase operations use normal transactions.
- Image upload uses transaction rollback configuration for `IOException`.
- Purchase operations use pessimistic locking for stock consistency.

### Stock Consistency

Purchasing loads buyable products with pessimistic write locks. This helps prevent overselling when multiple users try to buy the same product concurrently.

### Caching Consistency

Public product details are cached. Product updates, image changes, and purchases evict affected public product cache entries to avoid stale public data.
