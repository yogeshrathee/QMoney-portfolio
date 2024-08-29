# Stock Portfolio Management System

This project is a Stock Portfolio Management application that calculates annualized returns for a portfolio of stock trades. The application fetches historical stock prices and calculates the annualized returns based on the purchase and end dates.

--------------------------------------------------------------------

## Display Result

 ### 1. Anand 
![Anand](https://github.com/yogeshrathee/QMoney-portfolio/blob/cfa1cf76209a2de399d301b4ccd3a641b4481319/output/anand.jpg)

 ### 2. Suresh 
![Suresh](https://github.com/yogeshrathee/QMoney-portfolio/blob/cfa1cf76209a2de399d301b4ccd3a641b4481319/output/Suresh.jpg)

 ### 3. Faster Fene
![Faster Fene](https://github.com/yogeshrathee/QMoney-portfolio/blob/cfa1cf76209a2de399d301b4ccd3a641b4481319/output/Faster%20Fene.jpg)


-----------------------------------------------------------------


## Overview

The application allows users to input a portfolio of stock trades and fetches historical data to calculate the total and annualized returns for each trade. The results can be sorted and displayed in various formats.

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven or Gradle
- Tiingo API Key (for fetching historical stock prices)

### Installation

  1. Clone the repository:
     ```sh
     git clone https://github.com/yourusername/stock-portfolio-management.git
     cd stock-portfolio-management
  
  2. Configure your Tiingo API key: Replace the placeholder in the PortfolioManagerImpl class with your actual Tiingo API key.
  
  3. Build the project using Gradle:
    ```sh
    ./gradlew build

## API Endpoints
     ```sh
    /calculate-annualized-returns
  
    Method: POST
    Description: Calculates the annualized returns for a list of stock trades.

### Request Body :
       {
    "portfolioTrades": [
      {
        "symbol": "AAPL",
        "quantity": 10,
        "purchaseDate": "2020-01-01"
      },
      ...
    ],
    "endDate": "2021-01-01"
  }

### Response:
    [
      {
        "symbol": "AAPL",
        "annualizedReturn": 0.23,
        "totalReturns": 23.45
      },
      ...
    ]

## Project Structure:
    src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── crio/
    │   │           └── warmup/
    │   │               └── stock/
    │   │                   ├── dto/
    │   │                   ├── log/
    │   │                   ├── portfolio/
    │   │                   └── config/
    │   └── resources/
    │       └── application.properties
    ├── test/
    │   ├── java/
    │   │   └── com/
    │   │       └── crio/
    │   │           └── warmup/
    │   │               └── stock/
    │   │                   ├── PortfolioManagerImplTest.java
    │   │                   └── PortfolioManagerTest.java
    └── build.gradle


## Technologies Used
    Java 11: The primary programming language used for development.
    Spring Boot: For creating stand-alone, production-grade Spring-based applications.
    Gradle: For project build automation.
    JUnit: For unit testing.
