# Smart Inventory Management System

**Enterprise warehouse management with AI-powered insights**

[![Java](https://img.shields.io/badge/Java-11-orange)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-green)](https://spring.io/projects/spring-boot)
[![Python](https://img.shields.io/badge/Python-3.11-blue)](https://www.python.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Overview

The Smart Inventory Management System is a full-stack enterprise application designed to streamline warehouse operations through intelligent automation and AI-driven decision-making. It combines a robust Java/Spring Boot backend for core inventory operations with a Python-based AI microservice that delivers demand forecasting, smart reorder suggestions, anomaly detection, and natural language querying capabilities.

Built for mid-to-large scale warehouse environments, the system provides real-time stock tracking across multiple warehouses, automated low-stock alerts, comprehensive audit trails for all stock movements, and actionable insights powered by machine learning models.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                          Client Browser                             │
│                     (Thymeleaf / REST API)                          │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────────────┐
│                     Spring Boot Application                          │
│                         (Java 11, Port 8080)                         │
│                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────────────┐  │
│  │  Controllers  │  │   Services   │  │  AI Integration Service   │──┼──┐
│  └──────┬───────┘  └──────┬───────┘  └───────────────────────────┘  │  │
│         │                 │                                          │  │
│  ┌──────▼─────────────────▼──────┐  ┌────────────────────────────┐  │  │
│  │       JPA Repositories        │  │       Redis Cache           │  │  │
│  └──────────────┬────────────────┘  └─────────────┬──────────────┘  │  │
└─────────────────┼─────────────────────────────────┼─────────────────┘  │
                  │                                 │                    │
                  ▼                                 ▼                    │
      ┌───────────────────┐              ┌──────────────────┐           │
      │   PostgreSQL 15   │              │   Redis 7         │           │
      │   (Port 5432)     │              │   (Port 6379)     │           │
      └───────────────────┘              └──────────────────┘           │
                                                                        │
                  ┌─────────────────────────────────────────────────────┘
                  ▼
      ┌──────────────────────────────────────────────┐
      │         AI Microservice (Python)              │
      │         FastAPI (Port 8000)                   │
      │                                               │
      │  ┌──────────────┐  ┌───────────────────────┐ │
      │  │  Forecasting  │  │  Anomaly Detection    │ │
      │  │  (sklearn)    │  │  (Z-score + IQR)      │ │
      │  └──────────────┘  └───────────────────────┘ │
      │  ┌──────────────┐  ┌───────────────────────┐ │
      │  │  Smart Reorder│  │  NL Query             │ │
      │  │  (EOQ model)  │  │  (LangChain+OpenAI)  │ │
      │  └──────────────┘  └───────────────────────┘ │
      └──────────────────────────────────────────────┘
```

---

## Tech Stack

| Layer              | Technology                                      |
|--------------------|--------------------------------------------------|
| **Backend**        | Java 11, Spring Boot 2.7, Spring Data JPA, Spring Security |
| **AI Service**     | Python 3.11, FastAPI, scikit-learn, LangChain    |
| **Frontend**       | Thymeleaf, Bootstrap 5, Chart.js                 |
| **Database**       | PostgreSQL 15                                    |
| **Cache**          | Redis 7                                          |
| **Containerization** | Docker, Docker Compose                        |
| **Build Tool**     | Maven 3.8                                        |
| **Testing**        | JUnit 5, Mockito, pytest                         |

---

## Features

### Core Inventory Management
- Multi-warehouse product tracking with real-time stock levels
- SKU-based product catalog with category and supplier associations
- Stock movement processing (Inbound, Outbound, Transfer)
- Automated low-stock alerts and configurable minimum thresholds
- Full audit trail for all inventory transactions
- Role-based access control (Admin, Manager, Operator)

### AI-Powered Intelligence
- **Demand Forecasting** -- Predict future product demand using linear regression with seasonal decomposition
- **Smart Reorder Suggestions** -- Calculate optimal reorder quantities using Economic Order Quantity (EOQ) and safety stock models
- **Anomaly Detection** -- Identify unusual stock movements using Z-score and Interquartile Range (IQR) analysis
- **Natural Language Queries** -- Ask inventory questions in plain English via LangChain and OpenAI integration

### Reporting and Analytics
- Interactive dashboard with key performance indicators
- Stock level charts and movement trend visualizations
- Exportable inventory reports (CSV, PDF)
- Supplier performance tracking and lead time analysis

---

## Getting Started

### Prerequisites

| Requirement      | Version   |
|-----------------|-----------|
| Java (JDK)      | 11+       |
| Maven            | 3.8+      |
| Python           | 3.11+     |
| Docker           | 20.10+    |
| Docker Compose   | 2.0+      |

### Quick Start with Docker Compose

This is the fastest way to get the entire system running.

```bash
# Clone the repository
git clone https://github.com/your-org/smart-inventory-management.git
cd smart-inventory-management

# (Optional) Set OpenAI API key for NL query feature
export OPENAI_API_KEY=your-api-key-here

# Start all services
docker compose up -d

# Verify all containers are running
docker compose ps

# View application logs
docker compose logs -f app
```

The application will be available at:

| Service          | URL                          |
|-----------------|------------------------------|
| Web Application  | http://localhost:8080         |
| AI Service API   | http://localhost:8000/docs    |
| PostgreSQL       | localhost:5432                |
| Redis            | localhost:6379                |

### Manual Setup

#### 1. Start Infrastructure Services

```bash
# Start only PostgreSQL and Redis
docker compose up -d postgres redis
```

#### 2. Run the AI Service

```bash
cd ai-service
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

#### 3. Run the Java Application

```bash
# From the project root
mvn clean install -DskipTests
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Default Credentials

| Role     | Username | Password   |
|----------|----------|------------|
| Admin    | admin    | admin123   |
| Manager  | manager  | manager123 |
| Operator | operator | operator123|

---

## API Endpoints

### Java REST API (Port 8080)

| Method | Endpoint                        | Description                       |
|--------|---------------------------------|-----------------------------------|
| GET    | `/api/products`                 | List all products                 |
| GET    | `/api/products/{id}`            | Get product by ID                 |
| POST   | `/api/products`                 | Create a new product              |
| PUT    | `/api/products/{id}`            | Update an existing product        |
| DELETE | `/api/products/{id}`            | Delete a product                  |
| GET    | `/api/products/low-stock`       | List products below min stock     |
| GET    | `/api/warehouses`               | List all warehouses               |
| GET    | `/api/warehouses/{id}/stock`    | Get stock levels for a warehouse  |
| POST   | `/api/stock-movements`          | Process a stock movement          |
| GET    | `/api/stock-movements`          | List stock movements with filters |
| GET    | `/api/categories`               | List all categories               |
| GET    | `/api/suppliers`                | List all suppliers                |
| GET    | `/api/reports/inventory-summary`| Generate inventory summary report |
| GET    | `/actuator/health`              | Application health check          |

### Python AI Service (Port 8000)

| Method | Endpoint                        | Description                          |
|--------|---------------------------------|--------------------------------------|
| GET    | `/health`                       | AI service health check              |
| POST   | `/api/v1/forecast`              | Generate demand forecast             |
| GET    | `/api/v1/reorder-suggestions`   | Get smart reorder suggestions        |
| POST   | `/api/v1/anomaly-detection`     | Detect anomalies in stock movements  |
| POST   | `/api/v1/nl-query`              | Natural language inventory query     |
| GET    | `/docs`                         | Swagger UI (auto-generated)          |

---

## AI Features

### Demand Forecasting

The forecasting engine uses **Linear Regression** combined with **seasonal decomposition** to predict future product demand. Historical sales data is decomposed into trend, seasonal, and residual components. The model is retrained periodically and provides confidence scores alongside predictions.

```
POST /api/v1/forecast
{
    "product_id": 1,
    "forecast_days": 30
}

Response:
{
    "product_id": 1,
    "product_name": "Wireless Mouse",
    "forecast_days": 30,
    "predicted_demand": 450,
    "confidence_score": 0.87,
    "seasonal_factor": 1.12,
    "trend": "INCREASING"
}
```

### Smart Reorder Suggestions

Reorder quantities are calculated using the **Economic Order Quantity (EOQ)** formula combined with **Safety Stock** analysis. The system considers lead times, demand variability, holding costs, and ordering costs to recommend the optimal reorder point and quantity.

```
EOQ = sqrt((2 * D * S) / H)

Where:
  D = Annual demand
  S = Ordering cost per order
  H = Holding cost per unit per year

Safety Stock = Z * sigma_d * sqrt(L)

Where:
  Z     = Service level Z-score
  sigma = Standard deviation of demand
  L     = Lead time in days
```

### Anomaly Detection

Stock movement anomalies are identified using a dual-method approach:

- **Z-score Analysis** -- Flags movements where the quantity deviates more than 2.5 standard deviations from the historical mean for that product.
- **Interquartile Range (IQR)** -- Identifies outliers falling below Q1 - 1.5*IQR or above Q3 + 1.5*IQR.

Both methods run in parallel, and a movement is flagged if either method detects an anomaly.

### Natural Language Query

Powered by **LangChain** and **OpenAI GPT**, the natural language interface allows users to query inventory data using plain English. The system translates questions into structured database queries and returns human-readable responses.

Example queries:
- "What are the top 5 products by stock value?"
- "Which warehouses have critically low stock levels?"
- "Show me the stock movement trend for product ELEC-001 over the last 90 days."

> **Note:** This feature requires a valid `OPENAI_API_KEY` environment variable.

---

## Project Structure

```
smart-inventory-management/
├── docker-compose.yml
├── Dockerfile
├── pom.xml
├── README.md
├── .gitignore
├── ai-service/
│   ├── Dockerfile
│   ├── requirements.txt
│   ├── main.py
│   ├── models/
│   │   ├── forecasting.py
│   │   ├── reorder.py
│   │   └── anomaly.py
│   ├── routers/
│   │   ├── forecast_router.py
│   │   ├── reorder_router.py
│   │   ├── anomaly_router.py
│   │   └── nlquery_router.py
│   └── tests/
│       └── test_models.py
├── src/
│   ├── main/
│   │   ├── java/com/warehouse/inventory/
│   │   │   ├── InventoryApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── RedisConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── WarehouseController.java
│   │   │   │   ├── StockMovementController.java
│   │   │   │   └── DashboardController.java
│   │   │   ├── dto/
│   │   │   │   ├── ForecastResponse.java
│   │   │   │   └── ReorderSuggestion.java
│   │   │   ├── exception/
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── InsufficientStockException.java
│   │   │   │   └── AiServiceException.java
│   │   │   ├── model/
│   │   │   │   ├── Product.java
│   │   │   │   ├── Category.java
│   │   │   │   ├── Supplier.java
│   │   │   │   ├── Warehouse.java
│   │   │   │   ├── WarehouseStock.java
│   │   │   │   ├── StockMovement.java
│   │   │   │   └── MovementType.java
│   │   │   ├── repository/
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── SupplierRepository.java
│   │   │   │   ├── WarehouseRepository.java
│   │   │   │   ├── WarehouseStockRepository.java
│   │   │   │   └── StockMovementRepository.java
│   │   │   └── service/
│   │   │       ├── ProductService.java
│   │   │       ├── CategoryService.java
│   │   │       ├── SupplierService.java
│   │   │       ├── StockMovementService.java
│   │   │       └── AiIntegrationService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       └── templates/
│   │           ├── layout.html
│   │           ├── dashboard.html
│   │           └── products/
│   │               ├── list.html
│   │               ├── detail.html
│   │               └── form.html
│   └── test/
│       ├── java/com/warehouse/inventory/
│       │   ├── InventoryApplicationTests.java
│       │   ├── service/
│       │   │   ├── ProductServiceTest.java
│       │   │   ├── StockMovementServiceTest.java
│       │   │   └── AiIntegrationServiceTest.java
│       │   └── controller/
│       │       └── ProductControllerTest.java
│       └── resources/
│           └── application-test.properties
└── docs/
    └── screenshots/
```

---

## Screenshots

| Screen | Preview |
|--------|---------|
| Dashboard | ![Dashboard](docs/screenshots/dashboard.png) |
| Product List | ![Products](docs/screenshots/product-list.png) |
| Stock Movements | ![Movements](docs/screenshots/stock-movements.png) |
| AI Forecast | ![Forecast](docs/screenshots/ai-forecast.png) |
| Reorder Suggestions | ![Reorder](docs/screenshots/reorder-suggestions.png) |

> Screenshots will be added after the first stable release.

---

## License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 Smart Inventory Management System

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
