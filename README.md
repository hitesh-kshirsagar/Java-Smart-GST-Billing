# Smart GST Billing & Compliance Engine

A complete production-ready full-stack GST billing system built with **Java 17 + Spring Boot 3**, **MySQL**, **Hibernate/JPA**, **JWT authentication**, **iText PDF generation**, and a **Thymeleaf + Vanilla JS** frontend.

---

## Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Backend      | Java 17, Spring Boot 3.2            |
| Security     | Spring Security + JWT (JJWT 0.11)   |
| ORM          | Hibernate 6 / Spring Data JPA       |
| Database     | MySQL 8.x                           |
| PDF          | iText 5.5                           |
| Build        | Maven 3.8+                          |
| Frontend     | Thymeleaf + HTML/CSS/Vanilla JS     |
| Validation   | Bean Validation (Jakarta)           |

---

## Project Structure

```
smart-gst-billing/
├── pom.xml
├── schema.sql                          ← Run this first
├── README.md
└── src/
    ├── main/
    │   ├── java/com/gst/billing/
    │   │   ├── SmartGstBillingApplication.java
    │   │   ├── config/
    │   │   │   ├── JwtAuthFilter.java      ← JWT request filter
    │   │   │   └── SecurityConfig.java     ← Spring Security setup
    │   │   ├── controller/
    │   │   │   ├── AuthController.java     ← POST /api/auth/login|register
    │   │   │   ├── ProductController.java  ← /api/products CRUD
    │   │   │   ├── CustomerController.java ← /api/customers CRUD
    │   │   │   ├── InvoiceController.java  ← /api/invoices + PDF download
    │   │   │   ├── ReportController.java   ← /api/invoices/report
    │   │   │   └── PageController.java     ← Thymeleaf page routes
    │   │   ├── service/
    │   │   │   ├── AuthService.java
    │   │   │   ├── ProductService.java
    │   │   │   ├── CustomerService.java
    │   │   │   ├── InvoiceService.java
    │   │   │   ├── GstCalculationService.java  ← Core GST engine
    │   │   │   ├── PdfGenerationService.java   ← iText PDF builder
    │   │   │   ├── ReportService.java
    │   │   │   └── CustomUserDetailsService.java
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java
    │   │   │   ├── ProductRepository.java
    │   │   │   ├── CustomerRepository.java
    │   │   │   ├── InvoiceRepository.java
    │   │   │   └── InvoiceItemRepository.java
    │   │   ├── entity/
    │   │   │   ├── User.java
    │   │   │   ├── Product.java
    │   │   │   ├── Customer.java
    │   │   │   ├── Invoice.java
    │   │   │   └── InvoiceItem.java
    │   │   ├── dto/
    │   │   │   ├── LoginRequest.java / RegisterRequest.java / JwtResponse.java
    │   │   │   ├── ProductDto.java / CustomerDto.java
    │   │   │   ├── InvoiceRequest.java / InvoiceItemRequest.java
    │   │   │   ├── InvoiceResponse.java / InvoiceItemResponse.java
    │   │   │   ├── GstCalculationResult.java
    │   │   │   └── ReportDto.java
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── DuplicateInvoiceException.java
    │   │   │   └── InvalidGstinException.java
    │   │   └── util/
    │   │       ├── JwtUtil.java
    │   │       └── GstinValidator.java
    │   └── resources/
    │       ├── application.properties
    │       ├── templates/          ← Thymeleaf HTML pages
    │       │   ├── login.html / register.html
    │       │   ├── dashboard.html / products.html
    │       │   ├── customers.html / invoices.html
    │       │   └── reports.html
    │       └── static/
    │           ├── css/style.css
    │           └── js/app.js
    └── test/
        └── java/com/gst/billing/
            └── GstCalculationServiceTest.java   ← Unit tests
```

---

## Prerequisites

- Java 17+ ([Download](https://adoptium.net/))
- Maven 3.8+ (`mvn -v` to verify)
- MySQL 8.x running locally

---

## Setup Instructions

### Step 1 — Create MySQL database and tables

```bash
mysql -u root -p < schema.sql
```

Or run manually in MySQL Workbench / DBeaver:

```sql
CREATE DATABASE IF NOT EXISTS gst_billing_db;
USE gst_billing_db;
-- (then paste the rest of schema.sql)
```

### Step 2 — Configure database credentials

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gst_billing_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD_HERE
```

Also update your business details:

```properties
app.business.name=Your Company Name
app.business.gstin=27XXXXXXXXXX1ZX
app.business.address=123 Your Street, City, State - 000000
app.business.state=Maharashtra        # ← MUST match your registered state
```

### Step 3 — Build and run

```bash
cd smart-gst-billing
mvn clean package -DskipTests
java -jar target/smart-gst-billing-1.0.0.jar
```

Or to run directly with Maven:

```bash
mvn spring-boot:run
```

### Step 4 — Open in browser

```
http://localhost:8080/login
```

**Default credentials** (loaded from schema.sql):

| Username | Password   | Role  |
|----------|------------|-------|
| admin    | admin123   | ADMIN |
| staff    | admin123   | USER  |

---

## API Endpoints

All `/api/**` endpoints (except `/api/auth/**`) require:
```
Authorization: Bearer <JWT_TOKEN>
```

### Authentication

| Method | Endpoint              | Description        |
|--------|-----------------------|--------------------|
| POST   | `/api/auth/register`  | Create new account |
| POST   | `/api/auth/login`     | Get JWT token      |

**Login request:**
```json
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```
**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "role": "ADMIN"
}
```

### Products

| Method | Endpoint              | Description          |
|--------|-----------------------|----------------------|
| GET    | `/api/products`       | List all products    |
| GET    | `/api/products/{id}`  | Get product by ID    |
| POST   | `/api/products`       | Create product       |
| PUT    | `/api/products/{id}`  | Update product       |
| DELETE | `/api/products/{id}`  | Delete product       |

**Create product:**
```json
POST /api/products
{
  "name": "Laptop",
  "price": 55000.00,
  "gstPercentage": 18,
  "hsnCode": "8471"
}
```

### Customers

| Method | Endpoint               | Description             |
|--------|------------------------|-------------------------|
| GET    | `/api/customers`       | List all customers      |
| POST   | `/api/customers`       | Create customer         |
| PUT    | `/api/customers/{id}`  | Update customer         |
| DELETE | `/api/customers/{id}`  | Delete customer         |

**Create customer:**
```json
POST /api/customers
{
  "name": "Raj Enterprises",
  "gstin": "27AABCU9603R1ZX",
  "state": "Maharashtra"
}
```

### Invoices

| Method | Endpoint                    | Description                   |
|--------|-----------------------------|-------------------------------|
| POST   | `/api/invoices/create`      | Create invoice (GST auto-calc)|
| GET    | `/api/invoices`             | List all invoices             |
| GET    | `/api/invoices/{id}`        | Get invoice by ID             |
| GET    | `/api/invoices/{id}/pdf`    | Download PDF invoice          |
| GET    | `/api/invoices/report`      | Current month GST report      |
| GET    | `/api/invoices/report?month=3&year=2024` | Specific month report |

**Create invoice:**
```json
POST /api/invoices/create
{
  "customerId": 1,
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 4, "quantity": 10 }
  ]
}
```

**Invoice response (intra-state, CGST + SGST):**
```json
{
  "id": 1,
  "invoiceNumber": "INV-2024-0001",
  "date": "2024-05-15",
  "customerName": "Raj Enterprises",
  "customerState": "Maharashtra",
  "taxableAmount": 113500.00,
  "totalCgst": 10215.00,
  "totalSgst": 10215.00,
  "totalIgst": 0.00,
  "totalAmount": 133930.00,
  "items": [...]
}
```

---

## GST Calculation Logic

```
IF seller_state == buyer_state (intra-state):
    CGST = (price × qty × gst%) / 2 / 100
    SGST = (price × qty × gst%) / 2 / 100
    IGST = 0

ELSE (inter-state):
    CGST = 0
    SGST = 0
    IGST = (price × qty × gst%) / 100

Line Total = (price × qty) + CGST + SGST + IGST
```

The seller state is configured in `application.properties` under `app.business.state`.

---

## Running Unit Tests

```bash
mvn test
```

Tests cover:
- Intra-state CGST/SGST calculation (5%, 12%, 18%, 28%)
- Inter-state IGST calculation
- 0% GST (exempt items)
- Case-insensitive state comparison
- Tax penny accuracy (CGST + SGST = total GST)

---

## GSTIN Validation

The `GstinValidator` checks:
- Exactly 15 characters
- First 2: state code digits (01–37)
- Next 5: uppercase letters (PAN letters)
- Next 4: digits (PAN digits)
- Next 1: uppercase letter (PAN check)
- Next 1: entity code (1–9 or A–Z)
- Next 1: always `Z`
- Last 1: check digit (0–9 or A–Z)

Valid example: `27AABCU9603R1ZX`

---

## Frontend Pages

| URL          | Description                        |
|--------------|------------------------------------|
| `/login`     | Login with username + password     |
| `/register`  | Create new user account            |
| `/dashboard` | Stats + recent invoices overview   |
| `/products`  | Product CRUD with modal form       |
| `/customers` | Customer management + GSTIN input  |
| `/invoices`  | Invoice creation + PDF download    |
| `/reports`   | Monthly GST summary report         |

---

## Troubleshooting

**MySQL connection refused:**
- Ensure MySQL is running: `sudo systemctl start mysql`
- Verify credentials in `application.properties`

**Port 8080 already in use:**
```properties
server.port=9090
```

**JWT token expired:**
- Default expiry: 24 hours
- Change `app.jwt.expiration-ms=86400000` in properties

**PDF not downloading:**
- The frontend sends the JWT via Authorization header
- Ensure you are logged in before clicking PDF

**Tables not created:**
- Set `spring.jpa.hibernate.ddl-auto=create` on first run, then change to `update`
