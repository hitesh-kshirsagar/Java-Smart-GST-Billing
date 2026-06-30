-- ============================================================
-- Smart GST Billing & Compliance Engine — MySQL Schema
-- ============================================================
-- Run this ONCE before starting the application.
-- The app uses spring.jpa.hibernate.ddl-auto=update so
-- Hibernate will auto-create tables on first run, but this
-- script adds indexes, foreign keys, and sample data.
-- ============================================================

-- Create and select database
CREATE DATABASE IF NOT EXISTS gst_billing_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE gst_billing_db;

-- ============================================================
-- Table: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL DEFAULT 'USER',

    PRIMARY KEY (id),
    UNIQUE KEY uq_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table: products
-- ============================================================
CREATE TABLE IF NOT EXISTS products (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    name           VARCHAR(200)   NOT NULL,
    price          DECIMAL(10,2)  NOT NULL,
    gst_percentage DECIMAL(5,2)   NOT NULL DEFAULT 18.00,
    hsn_code       VARCHAR(20)    NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_products_hsn (hsn_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table: customers
-- ============================================================
CREATE TABLE IF NOT EXISTS customers (
    id    BIGINT       NOT NULL AUTO_INCREMENT,
    name  VARCHAR(200) NOT NULL,
    gstin VARCHAR(15)  NULL,
    state VARCHAR(100) NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_customers_gstin (gstin),
    INDEX idx_customers_state (state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table: invoices
-- ============================================================
CREATE TABLE IF NOT EXISTS invoices (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    invoice_number VARCHAR(30)   NOT NULL,
    date           DATE          NOT NULL,
    customer_id    BIGINT        NOT NULL,
    taxable_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_cgst     DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_sgst     DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_igst     DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_amount   DECIMAL(12,2) NOT NULL DEFAULT 0.00,

    PRIMARY KEY (id),
    UNIQUE KEY uq_invoices_number (invoice_number),
    INDEX idx_invoices_date (date),
    INDEX idx_invoices_customer (customer_id),

    CONSTRAINT fk_invoices_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table: invoice_items
-- ============================================================
CREATE TABLE IF NOT EXISTS invoice_items (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    invoice_id BIGINT        NOT NULL,
    product_id BIGINT        NOT NULL,
    quantity   INT           NOT NULL DEFAULT 1,
    price      DECIMAL(10,2) NOT NULL,
    cgst       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    sgst       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    igst       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    line_total DECIMAL(12,2) NOT NULL DEFAULT 0.00,

    PRIMARY KEY (id),
    INDEX idx_items_invoice (invoice_id),
    INDEX idx_items_product (product_id),

    CONSTRAINT fk_items_invoice
        FOREIGN KEY (invoice_id)
        REFERENCES invoices (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_items_product
        FOREIGN KEY (product_id)
        REFERENCES products (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Sample Data
-- ============================================================

-- Admin user (password: admin123 — BCrypt hash)
INSERT IGNORE INTO users (username, password, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN'),
('staff', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER');
-- Default password for both: admin123

-- Sample products
INSERT IGNORE INTO products (name, price, gst_percentage, hsn_code) VALUES
('Laptop - Dell Inspiron 15',          55000.00, 18, '8471'),
('Smartphone - Samsung Galaxy M54',    25000.00, 18, '8517'),
('Office Chair - Ergonomic',            8500.00, 18, '9401'),
('A4 Paper (500 sheets)',                 350.00,  5, '4802'),
('Toner Cartridge - HP LaserJet',       3200.00, 18, '8443'),
('External SSD 1TB',                    7500.00, 18, '8471'),
('HDMI Cable 2m',                        650.00, 18, '8544'),
('UPS 600VA',                           3800.00, 18, '8504'),
('Mouse Pad XL',                         299.00, 18, '9999'),
('Accounting Software - Annual License',12000.00, 18, '9983');

-- Sample customers (mix of intra-state and inter-state)
INSERT IGNORE INTO customers (name, gstin, state) VALUES
('Raj Enterprises',       '27AABCU9603R1ZX', 'Maharashtra'),   -- intra-state
('Delhi Tech Solutions',  '07AABCU9603R1ZY', 'Delhi'),          -- inter-state
('Bangalore IT Corp',     '29AABCU9603R1ZZ', 'Karnataka'),      -- inter-state
('Pune Hardware Supplies', '27BBMCS9999K1ZC', 'Maharashtra'),   -- intra-state
('Chennai Traders',       '33AABCU9603R1ZA', 'Tamil Nadu'),     -- inter-state
('Unregistered Buyer',     NULL,              'Maharashtra');    -- unregistered
