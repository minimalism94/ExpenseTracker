# SmartExpense - Personal Finance Management System

<div align="center">

<img src="src/main/resources/static/images/logo.png" alt="SmartExpense Logo" width="150">

**A comprehensive personal finance management application built with Spring Boot**

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)

</div>

---

---

## 🎯 Overview

**SmartExpense** is a modern personal finance management system that helps users track income, expenses, subscriptions, and budgets. The application offers **Basic** and **PRO** subscription tiers with advanced features for PRO users.

**Key Features:**
- 💰 Complete financial tracking (income, expenses, subscriptions)
- 📊 Advanced analytics and detailed reports
- 💳 Subscription management with expiry alerts
- 📈 Budget planning by category (PRO)
- 📧 Automated monthly PDF reports (PRO)
- 🔔 Smart notifications for expiring subscriptions
- 💳 Secure Stripe payment processing

---

## ✨ Features

### Core Features

- **Transaction Management**: Track income and expenses across 15+ categories (Housing, Food, Transport, Utilities, Entertainment, etc.)
- **Subscription Management**: Manage recurring subscriptions with automatic expiry alerts (7 days before expiry)
- **Dashboard**: Real-time financial overview with balance, income, expenses, and top spending categories
- **User Management**: Secure registration, authentication, and profile management with role-based access (Admin/User)

### PRO Features

- **Budget Planning**: Set and monitor monthly budgets by category with visual indicators
- **Advanced Reports**: Detailed monthly financial analysis with category breakdown and expense history
- **PDF Reports**: Professional monthly PDF reports with automated email delivery
- **Enhanced Notifications**: Customizable notification preferences including monthly report emails

---

## 🏗️ Architecture

The application follows a **microservices architecture** with the main application communicating with a separate notification microservice via Feign Client.

**Main Application (Port 9090)**
- Web Layer (Controllers) → Service Layer (Business Logic) → Data Layer (JPA Repositories)
- Scheduled Tasks: Monthly reports, subscription expiry notifications
- Spring Events: User upgrade events

**Notification Microservice (Port 9091)**
- Email notifications with PDF attachments
- SMS notifications via GreenAPI
- Notification preferences management

**External Integrations**
- Stripe (Payment processing)
- MySQL (Database)
- GreenAPI (SMS notifications)

---

## 🛠️ Technology Stack

**Backend:** Java 17, Spring Boot 3.4.0, Spring Security, Spring Data JPA, Spring Cloud OpenFeign, Hibernate, MySQL 8.0

**Frontend:** Thymeleaf, HTML5/CSS3, JavaScript, Chart.js

**External Services:** Stripe API (payments), GreenAPI (SMS), JavaMailSender (email)

**PDF Generation:** iText7, html2pdf

**Tools:** Maven, Lombok, SLF4J

---

## 📦 Prerequisites

- **JDK 17+**, **Maven 3.6+** (or Maven Wrapper), **MySQL 8.0+**, **Git**
- **Optional:** SMTP server for email, GreenAPI account for SMS

---

## 🚀 Installation & Setup

### 1. Clone and Setup

```bash
git clone https://github.com/yourusername/ExpenseTracker.git
cd ExpenseTracker
```

### 2. Database Configuration

Create MySQL database and update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/SmartExpens?allowPublicKeyRetrieval=true&useSSL=false&createDatabaseIfNotExist=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Stripe Configuration (Optional)

Get API keys from [Stripe Dashboard](https://dashboard.stripe.com/) and update `application.properties`:
```properties
stripe.api.key=sk_test_your_secret_key
stripe.public.key=pk_test_your_public_key
stripe.webhook.secret=whsec_your_webhook_secret
```

### 4. Build and Run

```bash
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

Application starts on **http://localhost:9090**

---

## 🔌 Notification Microservice

Separate microservice for handling notifications (runs on port 9091).

**Setup:**
1. Navigate to notification service directory
2. Configure email/SMS in `application.properties`
3. Run: `mvn spring-boot:run`

**Features:** Email notifications with PDF attachments, SMS via GreenAPI, notification preferences, notification history

**Communication:** Main app uses Spring Cloud OpenFeign to communicate with microservice

---

## 📚 API Endpoints

**Main Application:**
- Authentication: `/`, `/login`, `/register`, `/logout`
- Dashboard: `/dashboard`
- Transactions: `/transactions` (GET, POST, DELETE)
- Subscriptions: `/payments` (GET, POST, DELETE, `/pay/{id}`)
- Budget (PRO): `/budget` (GET, POST, DELETE)
- Reports (PRO): `/report`
- Profile: `/profile` (GET, POST)
- Notifications: `/notifications` (GET, POST `/toggle`, POST `/toggle-monthly-report`)
- Upgrade: `/upgrade` (GET, POST `/create-checkout-session`, GET `/success`, GET `/cancel`, POST `/webhook`)
- Admin: `/admin` (Admin only)

**Notification Microservice:**
- `POST /api/v1/notifications` - Send notification
- `GET /api/v1/notifications?userId={id}` - Get notifications
- `POST /api/v1/preferences` - Update preferences
- `GET /api/v1/preferences?userId={id}` - Get preferences

---

## 📁 Project Structure

```
ExpenseTracker/
├── src/
│   ├── main/
│   │   ├── java/app/
│   │   │   ├── budget/              # Budget planning module
│   │   │   │   ├── model/           # Budget entity
│   │   │   │   ├── repository/     # Budget repository
│   │   │   │   └── service/        # Budget business logic
│   │   │   ├── confg/              # Configuration classes
│   │   │   │   ├── BeanConfiguration.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── event/              # Spring Events
│   │   │   │   ├── listener/       # Event listeners
│   │   │   │   └── UserUpgradedEvent.java
│   │   │   ├── exception/          # Custom exceptions
│   │   │   ├── notification/       # Notification integration
│   │   │   │   ├── client/         # Feign client
│   │   │   │   └── service/       # Notification service
│   │   │   ├── payment/            # Payment processing
│   │   │   │   └── service/       # Stripe service
│   │   │   ├── report/             # Report generation
│   │   │   │   └── service/       # PDF report service
│   │   │   ├── scheduler/          # Scheduled tasks
│   │   │   │   ├── config/        # Cron expressions
│   │   │   │   ├── ReportScheduler.java
│   │   │   │   └── SubscriptionScheduler.java
│   │   │   ├── subscription/       # Subscription module
│   │   │   ├── transactions/       # Transaction module
│   │   │   ├── user/              # User module
│   │   │   ├── wallet/            # Wallet module
│   │   │   └── web/               # Web layer
│   │   │       ├── dto/           # Data Transfer Objects
│   │   │       │   └── mapper/    # DTO mappers
│   │   │       └── *.java         # Controllers
│   │   └── resources/
│   │       ├── static/            # Static resources
│   │       │   ├── css/          # Stylesheets
│   │       │   └── images/       # Images
│   │       ├── templates/         # Thymeleaf templates
│   │       └── application.properties
│   └── test/                      # Test files
├── pom.xml                        # Maven configuration
├── mvnw                           # Maven wrapper
└── README.md                      # This file
```

---

## ⚙️ Configuration

Key settings in `application.properties`:
- Server port: `9090`
- Database: MySQL connection settings
- Stripe: API keys for payment processing
- JPA: Hibernate auto-update enabled

**Scheduled Tasks:**
- Subscription notifications: Daily at 9 AM
- Monthly reports: First day of month at 9 AM

---

## 💻 Usage

1. **Register/Login**: Create account or login with credentials
2. **Add Transactions**: Track income and expenses by category
3. **Manage Subscriptions**: Add subscriptions and mark as paid
4. **View Dashboard**: See financial overview and top categories
5. **Upgrade to PRO**: Unlock budget planning, detailed reports, and PDF exports
6. **Budget Planning (PRO)**: Set monthly budgets and monitor spending
7. **Reports (PRO)**: View detailed financial analysis and download PDFs

---

## 🔒 Security

Spring Security with role-based access, BCrypt password encryption, CSRF protection, secure sessions, input validation, and SQL injection prevention via JPA.

---

## 🧪 Testing

```bash
mvn test
```

---

## 🎯 Future Enhancements

- Mobile application (iOS/Android)
- Multi-currency support
- Investment tracking
- Goal setting and tracking
- Banking API integration
- Collaborative budgeting

---

<div align="center">

**Built with ❤️ using Spring Boot**

</div>

