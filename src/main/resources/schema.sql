-- Refactored Database Schema
-- Architecture: Database contains Sales + Products
--               File System contains Customers (JSON)
--               Web Service contains Salespeople (REST API)

-- Products Table (500 records)
CREATE TABLE IF NOT EXISTS products (
    product_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(100),
    price DECIMAL(19, 4) NOT NULL,
    manufacturer VARCHAR(200),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    processed BOOLEAN NOT NULL DEFAULT FALSE
);

-- Indexes for products
CREATE INDEX IF NOT EXISTS idx_product_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_product_manufacturer ON products(manufacturer);
CREATE INDEX IF NOT EXISTS idx_product_processed_created ON products(processed, created_at);

-- Sales Table (10,000 records)
-- References: customers (FILE_SYSTEM), salespeople (WEB_SERVICE), products (DATABASE)
CREATE TABLE IF NOT EXISTS sales (
    sale_id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,     -- References FILE_SYSTEM (CUST00001-CUST01000)
    product_id VARCHAR(50) NOT NULL,      -- References products table (PROD00001-PROD00500)
    salesperson_id VARCHAR(50) NOT NULL,  -- References WEB_SERVICE (SP00001-SP00100)
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL,
    total_amount DECIMAL(19, 4) NOT NULL,
    sale_date TIMESTAMP NOT NULL,
    channel VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE
);

-- Indexes for sales
CREATE INDEX IF NOT EXISTS idx_sale_customer ON sales(customer_id);
CREATE INDEX IF NOT EXISTS idx_sale_product ON sales(product_id);
CREATE INDEX IF NOT EXISTS idx_sale_salesperson ON sales(salesperson_id);
CREATE INDEX IF NOT EXISTS idx_sale_date ON sales(sale_date);
CREATE INDEX IF NOT EXISTS idx_sale_channel ON sales(channel);
CREATE INDEX IF NOT EXISTS idx_sale_processed_created ON sales(processed, created_at);

-- Foreign key to products (optional, for data integrity)
-- ALTER TABLE sales ADD CONSTRAINT fk_sale_product FOREIGN KEY (product_id) REFERENCES products(product_id);

-- Note: customers and salespersons tables are NOT needed in this schema
-- Customers are loaded from JSON files (File System)
-- Salespeople are fetched from REST API (Web Service)
