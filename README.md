Github Link : "https://github.com/hitesh-kshirsagar"

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


**Tables not created:**
- Set `spring.jpa.hibernate.ddl-auto=create` on first run, then change to `update`
