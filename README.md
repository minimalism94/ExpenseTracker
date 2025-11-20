# SmartExpense - Personal Finance Management System

<div align="center">

![SmartExpense Logo](src/main/resources/static/images/logo.png)

**A comprehensive personal finance management application built with Spring Boot**

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Microservice Architecture](#microservice-architecture)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Usage](#usage)
- [Contributing](#contributing)

---

## 🎯 Overview

**SmartExpense** is a modern, full-featured personal finance management system designed to help users track their income, expenses, subscriptions, and budgets efficiently. The application provides both **Basic** and **PRO** subscription tiers, with advanced features available for PRO users.

### Key Highlights

- 💰 **Complete Financial Tracking**: Track income, expenses, and subscriptions in one place
- 📊 **Advanced Analytics**: Detailed reports and category breakdowns
- 💳 **Subscription Management**: Never miss a payment with subscription tracking
- 📈 **Budget Planning**: Set and monitor budgets by category (PRO feature)
- 📧 **Automated Reports**: Monthly PDF reports sent via email (PRO feature)
- 🔔 **Smart Notifications**: Get notified about expiring subscriptions
- 💳 **Secure Payments**: Integrated Stripe payment processing for PRO upgrades

---

## ✨ Features

### Core Features (Available to All Users)

#### 📝 Transaction Management
- **Income Tracking**: Record and categorize all income sources
- **Expense Tracking**: Track expenses across 15+ categories:
  - Housing, Food, Transport, Utilities, Clothing
  - Entertainment, Travel, Education, Loans
  - Savings, Health, Family, Gifts, Home, Other
- **Category Analysis**: View spending patterns by category
- **Transaction History**: Complete history with date filtering

#### 💳 Subscription Management
- **Subscription Tracking**: Add and manage recurring subscriptions
- **Payment Tracking**: Mark subscriptions as paid
- **Expiry Alerts**: Automatic notifications for subscriptions expiring within 7 days
- **Subscription Types**: Support for monthly, quarterly, and annual subscriptions

#### 👤 User Management
- **User Registration & Authentication**: Secure user accounts with Spring Security
- **Profile Management**: Update personal information and preferences
- **Role-Based Access**: Admin and User roles with different permissions

#### 📊 Dashboard
- **Financial Overview**: Real-time balance, income, and expense summaries
- **Top Categories**: Visual representation of spending by category
- **Recent Transactions**: Quick view of latest financial activities
- **Upcoming Subscriptions**: Display of subscriptions requiring payment

### PRO Features (Premium Subscription)

#### 📈 Advanced Analytics & Reports
- **Detailed Financial Reports**: Comprehensive monthly financial analysis
- **Category Breakdown**: Detailed spending analysis by category
- **Expense History**: Day-by-day expense tracking
- **PDF Export**: Professional monthly reports in PDF format
- **Email Delivery**: Automated monthly PDF reports sent to your email

#### 💰 Budget Planning
- **Category-Based Budgets**: Set monthly budgets for each expense category
- **Budget Tracking**: Monitor spending against budgets
- **Budget Alerts**: Visual indicators for over-budget categories
- **Budget Summary**: Total budget, spent, and remaining amounts

#### 🔔 Enhanced Notifications
- **Monthly Report Toggle**: Enable/disable monthly PDF email reports
- **Customizable Preferences**: Control notification preferences

---

## 🏗️ Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    SmartExpense Application                  │
│                    (Port: 9090)                              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Web Layer  │  │ Service Layer│  │  Data Layer  │      │
│  │ (Controllers)│  │  (Business   │  │ (Repositories│      │
│  │              │  │   Logic)     │  │   & JPA)     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         Scheduled Tasks (Schedulers)                  │  │
│  │  - Monthly Report Generation                          │  │
│  │  - Subscription Expiry Notifications                  │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         Spring Events                                │  │
│  │  - User Upgrade Events                               │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
└──────────────────────┬─────────────────────────────────────┘
                       │
                       │ Feign Client (HTTP)
                       │
┌──────────────────────▼─────────────────────────────────────┐
│          Notification Microservice                         │
│          (Port: 9091)                                      │
├────────────────────────────────────────────────────────────┤
│  - Email Notifications                                     │
│  - SMS Notifications (via GreenAPI)                        │
│  - Notification Preferences Management                    │
│  - PDF Attachment Support                                  │
└────────────────────────────────────────────────────────────┘
                       │
                       │ External APIs
                       │
┌──────────────────────▼─────────────────────────────────────┐
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │    Stripe    │  │   MySQL DB   │  │   GreenAPI   │    │
│  │  (Payments)  │  │  (Database)  │  │     (SMS)    │    │
│  └──────────────┘  └──────────────┘  └──────────────┘    │
└────────────────────────────────────────────────────────────┘
```

### Design Patterns

- **MVC (Model-View-Controller)**: Separation of concerns
- **Service Layer Pattern**: Business logic encapsulation
- **Repository Pattern**: Data access abstraction
- **DTO Pattern**: Data transfer objects for API communication
- **Event-Driven Architecture**: Spring Events for decoupled communication
- **Microservices**: Notification service as separate microservice

---

## 🛠️ Technology Stack

### Backend
- **Java 17**: Modern Java features and performance
- **Spring Boot 3.4.0**: Rapid application development framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database abstraction layer
- **Spring Cloud OpenFeign**: Microservice communication
- **Hibernate**: ORM framework
- **MySQL 8.0**: Relational database

### Frontend
- **Thymeleaf**: Server-side templating engine
- **HTML5/CSS3**: Modern web standards
- **JavaScript**: Client-side interactivity
- **Chart.js**: Data visualization

### Payment & External Services
- **Stripe API**: Payment processing
- **GreenAPI**: SMS notifications
- **JavaMailSender**: Email delivery

### PDF Generation
- **iText7**: PDF document generation
- **html2pdf**: HTML to PDF conversion

### Build & Tools
- **Maven**: Dependency management and build tool
- **Lombok**: Boilerplate code reduction
- **SLF4J + Logback**: Logging framework

---

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 17** or higher
- **Maven 3.6+** (or use Maven Wrapper included in project)
- **MySQL 8.0+** database server
- **Git** for version control
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code recommended)

### Optional (for Microservice)
- **SMTP Server** configuration for email notifications
- **GreenAPI Account** for SMS notifications (optional)

---

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/ExpenseTracker.git
cd ExpenseTracker
```

### 2. Database Setup

1. **Create MySQL Database**:
   ```sql
   CREATE DATABASE SmartExpens;
   ```

2. **Update Database Configuration** in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/SmartExpens?allowPublicKeyRetrieval=true&useSSL=false&createDatabaseIfNotExist=true
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

### 3. Configure Stripe (for Payment Features)

1. **Get Stripe API Keys** from [Stripe Dashboard](https://dashboard.stripe.com/)

2. **Update** `src/main/resources/application.properties`:
   ```properties
   stripe.api.key=sk_test_your_secret_key
   stripe.public.key=pk_test_your_public_key
   stripe.webhook.secret=whsec_your_webhook_secret
   stripe.success.url=http://localhost:9090/upgrade/success?session_id={CHECKOUT_SESSION_ID}
   stripe.cancel.url=http://localhost:9090/upgrade/cancel
   ```

### 4. Build the Project

```bash
# Using Maven Wrapper (Windows)
mvnw.cmd clean install

# Using Maven Wrapper (Linux/Mac)
./mvnw clean install

# Or using Maven directly
mvn clean install
```

### 5. Run the Application

```bash
# Using Maven Wrapper
mvnw.cmd spring-boot:run

# Or using Maven
mvn spring-boot:run
```

The application will start on **http://localhost:9090**

### 6. Access the Application

- **Home Page**: http://localhost:9090/
- **Login**: http://localhost:9090/login
- **Register**: http://localhost:9090/register
- **Dashboard**: http://localhost:9090/dashboard (after login)

---

## 🔌 Microservice Architecture

### Notification Microservice

The application uses a separate **Notification Microservice** for handling all notification-related operations.

#### Setup Notification Microservice

1. **Navigate to notification service**:
   ```bash
   cd ../GITREPO/notification-svc
   ```

2. **Configure Email Settings** in `src/main/resources/application.properties`:
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your_email@gmail.com
   spring.mail.password=your_app_password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

3. **Configure SMS (Optional)**:
   ```properties
   greenapi.instance.id=your_instance_id
   greenapi.api.token=your_api_token
   ```

4. **Run the Microservice**:
   ```bash
   mvn spring-boot:run
   ```

   The microservice will start on **http://localhost:9091**

#### Microservice Features

- **Email Notifications**: Send emails with HTML content and PDF attachments
- **SMS Notifications**: Send SMS via GreenAPI integration
- **Notification Preferences**: Manage user notification preferences
- **Notification History**: Track all sent notifications

#### Communication

The main application communicates with the notification microservice using **Spring Cloud OpenFeign**:

```java
@FeignClient(name = "notification-svc", url = "localhost:9091/api/v1")
public interface NotificationClient {
    // API methods
}
```

---

## 📚 API Documentation

### Main Application Endpoints

#### Authentication
- `GET /` - Home page
- `GET /login` - Login page
- `POST /login` - Process login
- `GET /register` - Registration page
- `POST /register` - Process registration
- `GET /logout` - Logout

#### Dashboard
- `GET /dashboard` - User dashboard with financial overview

#### Transactions
- `GET /transactions` - List all transactions
- `POST /transactions` - Create new transaction
- `DELETE /transactions/{id}` - Delete transaction

#### Subscriptions
- `GET /payments` - List all subscriptions
- `POST /payments` - Create new subscription
- `POST /payments/{id}/pay` - Pay subscription
- `DELETE /payments/{id}` - Delete subscription

#### Budget (PRO Only)
- `GET /budget` - Budget planning page
- `POST /budget` - Create/update budget
- `DELETE /budget/{id}` - Delete budget

#### Reports (PRO Only)
- `GET /report` - Monthly financial report

#### Profile
- `GET /profile` - User profile page
- `POST /profile` - Update profile

#### Notifications
- `GET /notifications` - Notification preferences
- `POST /notifications/toggle` - Toggle email notifications
- `POST /notifications/toggle-monthly-report` - Toggle monthly report emails

#### Upgrade
- `GET /upgrade` - Upgrade to PRO page
- `POST /upgrade/create-checkout-session` - Create Stripe checkout session
- `GET /upgrade/success` - Payment success callback
- `GET /upgrade/cancel` - Payment cancellation
- `POST /upgrade/webhook` - Stripe webhook handler

#### Admin
- `GET /admin` - Admin panel (Admin role only)

### Notification Microservice Endpoints

- `POST /api/v1/notifications` - Send notification
- `GET /api/v1/notifications?userId={id}` - Get user notifications
- `POST /api/v1/preferences` - Create/update notification preferences
- `GET /api/v1/preferences?userId={id}` - Get user preferences

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

### Application Properties

Key configuration options in `application.properties`:

```properties
# Server Configuration
server.port=9090

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/SmartExpens
spring.datasource.username=root
spring.datasource.password=your_password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Stripe Configuration
stripe.api.key=your_stripe_secret_key
stripe.public.key=your_stripe_public_key
stripe.webhook.secret=your_webhook_secret

# Logging
logging.level.org.springframework=WARN
logging.level.app=INFO
```

### Scheduled Tasks

Scheduled tasks are configured in `scheduler/config/CronExpressions.java`:

- **Subscription Notifications**: Daily at 9 AM
- **Monthly Reports**: First day of month at 9 AM

---

## 💻 Usage

### Getting Started

1. **Register a New Account**:
   - Navigate to `/register`
   - Fill in username, email, password, and country
   - Click "Register"

2. **Login**:
   - Use your credentials to log in
   - You'll be redirected to the dashboard

3. **Add Transactions**:
   - Go to "Transactions" in the sidebar
   - Click "Add Transaction"
   - Fill in amount, category, description, and date
   - Choose transaction type (Income/Expense)

4. **Manage Subscriptions**:
   - Go to "Subscriptions"
   - Add new subscriptions with name, price, and expiry date
   - Mark subscriptions as paid when you pay them

5. **Upgrade to PRO** (Optional):
   - Click "Upgrade to Pro" in the sidebar
   - Complete payment via Stripe
   - Unlock advanced features

### PRO Features Usage

1. **Budget Planning**:
   - Navigate to "Budget" (PRO only)
   - Set monthly budgets for each category
   - Monitor spending against budgets

2. **Detailed Reports**:
   - Go to "Report" (PRO only)
   - View comprehensive financial analysis
   - Download PDF reports

3. **Monthly Email Reports**:
   - Enable in "Notifications" page
   - Receive automated monthly PDF reports

---

## 🔒 Security Features

- **Spring Security**: Role-based access control
- **Password Encryption**: BCrypt password hashing
- **CSRF Protection**: Enabled for all forms
- **Session Management**: Secure session handling
- **Input Validation**: Server-side validation for all inputs
- **SQL Injection Prevention**: JPA/Hibernate parameterized queries

---

## 🧪 Testing

Run tests using Maven:

```bash
mvn test
```

---

## 📝 License

This project is licensed under the MIT License.

---

## 👥 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📧 Contact & Support

For questions, issues, or contributions, please open an issue on GitHub.

---

## 🎯 Future Enhancements

- [ ] Mobile application (iOS/Android)
- [ ] Multi-currency support
- [ ] Investment tracking
- [ ] Goal setting and tracking
- [ ] Recurring transaction templates
- [ ] Data export to Excel/CSV
- [ ] Advanced analytics with machine learning
- [ ] Integration with banking APIs
- [ ] Collaborative budgeting (family/shared budgets)

---

<div align="center">

**Built with ❤️ using Spring Boot**

⭐ Star this repo if you find it helpful!

</div>

