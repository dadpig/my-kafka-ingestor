#!/usr/bin/env python3
"""
Refactored Data Generator
- Database: 10,000 sales + 500 products
- File System: 1,000 customers
- Web Service: 100 salespeople (via MockWebServiceController)
"""

import random
import json
from datetime import datetime, timedelta

random.seed(42)

# Constants
REGIONS = ["North", "South", "East", "West", "Central"]
SEGMENTS = ["Premium", "Standard", "Basic"]
CATEGORIES = ["Electronics", "Clothing", "Food", "Books", "Home"]
MANUFACTURERS = ["TechCorp", "FashionHub", "FoodCo", "BookWorld", "HomeStyle"]
CHANNELS = ["Online", "Store", "Mobile", "Partner"]
COMPANY_PREFIXES = ["Tech", "Global", "Innovate", "Digital", "Smart", "Mega", "Ultra", "Super", "Pro", "Elite"]
COMPANY_SUFFIXES = ["Corp", "Solutions", "Industries", "Systems", "Enterprises", "Group", "Partners", "Technologies", "Ventures", "Labs"]

CITIES_BY_COUNTRY = [
    ["USA", "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose"],
    ["Canada", "Toronto", "Vancouver", "Montreal", "Calgary", "Ottawa", "Edmonton", "Winnipeg", "Quebec City"],
    ["UK", "London", "Manchester", "Birmingham", "Leeds", "Glasgow", "Liverpool", "Edinburgh", "Bristol"],
    ["Germany", "Berlin", "Munich", "Frankfurt", "Hamburg", "Cologne", "Stuttgart", "Dusseldorf", "Dortmund"],
    ["France", "Paris", "Lyon", "Marseille", "Toulouse", "Nice", "Nantes", "Strasbourg", "Bordeaux"],
    ["Spain", "Madrid", "Barcelona", "Valencia", "Seville", "Bilbao", "Malaga", "Zaragoza", "Murcia"],
    ["Italy", "Rome", "Milan", "Naples", "Turin", "Florence", "Genoa", "Bologna", "Venice"]
]

FIRST_NAMES = ["James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael", "Linda", "William", "Elizabeth",
               "David", "Barbara", "Richard", "Susan", "Joseph", "Jessica", "Thomas", "Sarah", "Charles", "Karen"]

LAST_NAMES = ["Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
              "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin"]


def generate_company_name():
    prefix = random.choice(COMPANY_PREFIXES)
    suffix = random.choice(COMPANY_SUFFIXES)
    return f"{prefix}{suffix} {'Inc' if random.random() > 0.5 else 'Ltd'}"


def generate_email(name):
    domain = ''.join(c for c in name.lower() if c.isalnum())[:10]
    return f"contact@{domain}.com"


# 1. Generate Products SQL (500 records)
print("Generating products SQL...")
with open('data/products-500.sql', 'w') as f:
    f.write("-- Products Data: 500 records\n")
    f.write("-- Source: DATABASE\n\n")

    for i in range(1, 501):
        product_id = f"PROD{i:05d}"
        category = random.choice(CATEGORIES)
        name = f"{category} Product {i}".replace("'", "''")
        price = round(10 + random.random() * 990, 2)
        manufacturer = random.choice(MANUFACTURERS)
        days_ago = random.randint(0, 365)

        f.write(f"INSERT INTO products (product_id, name, category, price, manufacturer, created_at, updated_at, processed) ")
        f.write(f"VALUES ('{product_id}', '{name}', '{category}', {price}, '{manufacturer}', ")
        f.write(f"DATEADD('DAY', -{days_ago}, CURRENT_TIMESTAMP), NULL, FALSE);\n")

        if i % 100 == 0:
            print(f"  Generated {i} products")

print(f"✅ Products SQL created: data/products-500.sql (500 records)\n")


# 2. Generate Sales SQL (10,000 records)
print("Generating sales SQL...")
with open('data/sales-10k.sql', 'w') as f:
    f.write("-- Sales Data: 10,000 records\n")
    f.write("-- References: 1,000 customers (FILE_SYSTEM), 100 salespeople (WEB_SERVICE), 500 products (DATABASE)\n")
    f.write("-- Source: DATABASE\n\n")

    for i in range(1, 10001):
        sale_id = f"SALE{i:08d}"
        customer_id = f"CUST{((i - 1) % 1000) + 1:05d}"  # Cycle through 1,000 customers
        product_id = f"PROD{random.randint(1, 500):05d}"  # Random 500 products
        salesperson_id = f"SP{random.randint(1, 100):05d}"  # Random 100 salespeople

        quantity = random.randint(1, 20)
        unit_price = round(10 + random.random() * 990, 2)
        total_amount = round(unit_price * quantity, 2)
        channel = random.choice(CHANNELS)

        days_ago = random.randint(0, 90)
        hours_ago = random.randint(0, 23)
        minutes_ago = random.randint(0, 59)

        f.write(f"INSERT INTO sales (sale_id, customer_id, product_id, salesperson_id, quantity, unit_price, total_amount, sale_date, channel, created_at, processed) ")
        f.write(f"VALUES ('{sale_id}', '{customer_id}', '{product_id}', '{salesperson_id}', {quantity}, {unit_price}, {total_amount}, ")
        f.write(f"DATEADD('MINUTE', -{minutes_ago}, DATEADD('HOUR', -{hours_ago}, DATEADD('DAY', -{days_ago}, CURRENT_TIMESTAMP))), ")
        f.write(f"'{channel}', CURRENT_TIMESTAMP, FALSE);\n")

        if i % 1000 == 0:
            print(f"  Generated {i} sales")

print(f"✅ Sales SQL created: data/sales-10k.sql (10,000 records)\n")


# 3. Generate Customers JSON (1,000 records)
print("Generating customers JSON...")
customers = []
for i in range(1, 1001):
    customer_id = f"CUST{i:05d}"
    name = generate_company_name()
    days_ago = random.randint(0, 365)
    created_at = (datetime.now() - timedelta(days=days_ago)).isoformat() + "Z"

    customers.append({
        "customerId": customer_id,
        "name": name,
        "email": generate_email(name),
        "segment": random.choice(SEGMENTS),
        "region": random.choice(REGIONS),
        "createdAt": created_at
    })

    if i % 200 == 0:
        print(f"  Generated {i} customers")

with open('data/customers-1k.json', 'w') as f:
    json.dump(customers, f, indent=2)

print(f"✅ Customers JSON created: data/customers-1k.json (1,000 records)\n")


# 4. Generate Salespeople Reference JSON (100 records - for MockWebServiceController)
print("Generating salespeople reference JSON...")
salespeople = []
for i in range(1, 101):
    salesperson_id = f"SP{i:05d}"
    first_name = random.choice(FIRST_NAMES)
    last_name = random.choice(LAST_NAMES)
    country_data = random.choice(CITIES_BY_COUNTRY)
    country = country_data[0]
    city = random.choice(country_data[1:])
    days_ago = random.randint(0, 730)
    created_at = (datetime.now() - timedelta(days=days_ago)).isoformat() + "Z"

    salespeople.append({
        "salespersonId": salesperson_id,
        "name": f"{first_name} {last_name}",
        "email": f"{first_name.lower()}.{last_name.lower()}@salesforce.com",
        "city": city,
        "country": country,
        "createdAt": created_at
    })

    if i % 20 == 0:
        print(f"  Generated {i} salespeople")

with open('data/salespeople-100-reference.json', 'w') as f:
    json.dump(salespeople, f, indent=2)

print(f"✅ Salespeople reference JSON created: data/salespeople-100-reference.json (100 records)")
print("   Note: MockWebServiceController will serve these dynamically\n")

print("=" * 80)
print("DATA GENERATION COMPLETED!")
print("=" * 80)
print("\nData Distribution:")
print("  📊 Database (SQL):     10,000 sales → data/sales-10k.sql")
print("  📊 Database (SQL):     500 products → data/products-500.sql")
print("  📁 File System (JSON): 1,000 customers → data/customers-1k.json")
print("  🌐 Web Service (REST): 100 salespeople → MockWebServiceController")
print("\nRelationships:")
print("  • Sales reference: CUST00001-CUST01000 (customers from FILE_SYSTEM)")
print("  • Sales reference: PROD00001-PROD00500 (products from DATABASE)")
print("  • Sales reference: SP00001-SP00100 (salespeople from WEB_SERVICE)")
print("=" * 80)
