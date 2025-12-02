# SmartExpense - Personal Finance Management System

<div align="center">

<img src="src/main/resources/static/images/logo.png" alt="SmartExpense Logo" width="150">

**A comprehensive personal finance management application built with Spring Boot**

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)

</div>

---

## 🛠️ Technology Stack

### Backend Technologies
- **Java 17** - Programming language
- **Spring Boot 3.4.0** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence layer
- **Spring Cloud OpenFeign 4.2.0** - Microservice communication
- **Hibernate** - ORM framework
- **MySQL 8.0** - Relational database
- **Lombok** - Boilerplate code reduction
- **Maven** - Build automation and dependency management

### Frontend Technologies
- **Thymeleaf** - Server-side template engine
- **HTML5/CSS3** - Markup and styling
- **JavaScript** - Client-side scripting
- **Chart.js** - Data visualization

### External Services & Libraries
- **Stripe API** - Payment processing (v24.16.0)
- **OAuth2 Client** - Google authentication integration
- **iText7** - PDF generation (v7.2.5)
- **html2pdf** - HTML to PDF conversion (v4.0.5)
- **GreenAPI** - SMS notifications
- **JavaMailSender** - Email notifications

### Development Tools
- **Spring Boot DevTools** - Development productivity
- **Spring Boot Actuator** - Application monitoring
- **SLF4J** - Logging framework
- **JUnit** - Testing framework
- **H2 Database** - In-memory database for testing

---

## ⚠️ Important: Microservice Architecture

**This application uses a microservice architecture and REQUIRES the Notification Microservice to be running.**

### Service Components

- **Main Application (SmartExpense):** Runs on port `9090` (this repository)
- **Notification Microservice:** Runs on port `9091` (separate repository: `notification-svc`)

### ⚠️ CRITICAL: Both Services Must Run Together

**Both services MUST be running simultaneously for the application to function properly.**

The notification microservice is **essential** for:
- ✅ **User Registration** - Cannot create notification preferences without the microservice
- ✅ **Notification Settings** - All notification preferences are managed by the microservice
- ✅ **Email Notifications** - Subscription expiry alerts, monthly reports
- ✅ **SMS Notifications** - Subscription expiry alerts via WhatsApp/SMS
- ✅ **Notification History** - All notification records are stored in the microservice

**If the notification microservice is not running:**
- ❌ User registration will fail (cannot create preferences)
- ❌ Notification settings cannot be managed
- ❌ No notifications will be sent
- ❌ Application will have limited functionality

**Startup Order:**
1. ✅ Start Notification Microservice (port 9091) - **FIRST**
2. ✅ Start SmartExpense Main App (port 9090) - **SECOND**

See the [Notification Microservice](#-notification-microservice) section below for detailed setup instructions.

---

## 🌐 Application Host & Port

The application runs on:
- **Host:** `localhost`
- **Port:** `9090`
- **Full URL:** `http://localhost:9090`

### Access Points
- **Home/Login:** `http://localhost:9090/`
- **Dashboard:** `http://localhost:9090/dashboard`
- **Admin Panel:** `http://localhost:9090/admin` (Admin access required)

---

## 🔐 Default Admin Credentials

**Important:** On first startup, the application automatically creates a default admin user for testing purposes.

### Default Admin Account
- **Username:** `admin`
- **Password:** `admin`
- **Email:** `admin@gmail.com`
- **Role:** `ADMIN`

### Additional Test Data
The admin user is automatically initialized with:
- Default wallet
- Sample subscriptions (Netflix, Spotify Premium, Gym Membership)
- Sample transactions (income and expenses)
- Default notification preferences

**Note:** The admin user is only created if the database is empty (no existing users). If you want to reset and recreate the admin user, clear the database and restart the application.

---

## 📦 Prerequisites

Before running the application, ensure you have the following installed:

- **JDK 17 or higher** - [Download Java](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.6+** (or use Maven Wrapper included in project)
- **MySQL 8.0+** - [Download MySQL](https://dev.mysql.com/downloads/mysql/)
- **Git** - [Download Git](https://git-scm.com/downloads)

### Required for Full Functionality
- **Notification Microservice** - Must be running on port 9091 (see Notification Microservice section)
- **SMTP Server Access** - For email notifications (Gmail, Outlook, etc.)
- **GreenAPI Account** - For SMS/WhatsApp notifications

### Optional Dependencies
- **Stripe Account** - For payment processing (test mode keys are included)

---

## 🚀 Quick Start Guide

### Step 1: Clone the Repository

```bash
git clone https://github.com/yourusername/ExpenseTracker.git
cd ExpenseTracker
```

### Step 2: Database Setup

1. **Start MySQL Server** on your local machine

2. **Create Database** (optional - will be created automatically):
   ```sql
   CREATE DATABASE SmartExpens;
   ```

3. **Update Database Credentials** in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password
   ```
   
   **Note:** The database will be created automatically if it doesn't exist (`createDatabaseIfNotExist=true`).

### Step 3: Build the Application

**Windows:**
```bash
mvnw.cmd clean install
```

**Linux/Mac:**
```bash
./mvnw clean install
```

### Step 4: Setup Notification Microservice

**⚠️ CRITICAL:** The notification microservice MUST be running before starting the main application.

1. **Navigate to notification service directory** (if in separate location):
   ```bash
   cd ../notification-svc
   ```

2. **Configure database and credentials** (see Notification Microservice section below)

3. **Start the notification microservice:**
   ```bash
   mvnw.cmd spring-boot:run
   ```
   
   Wait for: `Started Application in X.XXX seconds` (port 9091)

### Step 5: Run the Main Application

**Windows:**
```bash
mvnw.cmd spring-boot:run
```

**Linux/Mac:**
```bash
./mvnw spring-boot:run
```

**Or using Java directly:**
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Step 6: Access the Application

1. **Verify both services are running:**
   - Main app: `http://localhost:9090`
   - Notification service: `http://localhost:9091`

2. **Open your browser** and navigate to: **http://localhost:9090**

3. **Login with the default admin credentials:**
   - Username: `admin`
   - Password: `admin`

---

## 🎯 Features Overview

### Core Features (Available to All Users)
- ✅ **Transaction Management** - Track income and expenses across 15+ categories
- ✅ **Subscription Management** - Manage recurring subscriptions with expiry alerts
- ✅ **Dashboard** - Real-time financial overview with balance and spending insights
- ✅ **User Profile** - Manage personal information and preferences
- ✅ **Notifications** - Email and SMS notifications for expiring subscriptions
- ✅ **Google OAuth2 Login** - Sign in with Google account

### PRO Features (Premium Subscription)
- ⭐ **Budget Planning** - Set and monitor monthly budgets by category
- ⭐ **Advanced Reports** - Detailed monthly financial analysis
- ⭐ **PDF Reports** - Professional PDF reports with automated email delivery
- ⭐ **Enhanced Analytics** - Category breakdown and expense history

---

## 📋 First-Time User Guide

### 1. Initial Login
- Use the default admin account (`admin`/`admin`) to explore all features
- Or register a new account at `/register`

### 2. Explore the Dashboard
- View your current balance, income, expenses, and top spending categories
- The admin account comes pre-loaded with sample data

### 3. Manage Transactions
- Navigate to **Transactions** to add income or expenses
- Categorize transactions (Food, Transport, Utilities, etc.)
- View transaction history and filter by date/category

### 4. Manage Subscriptions
- Add recurring subscriptions (Netflix, Spotify, etc.)
- Set expiry dates and receive alerts 7 days before expiration
- Mark subscriptions as paid

### 5. Upgrade to PRO (Optional)
- Navigate to **Upgrade** section
- Complete payment via Stripe (test mode)
- Unlock budget planning, advanced reports, and PDF exports

### 6. Budget Planning (PRO Only)
- Set monthly budgets for different categories
- Monitor spending against budgets with visual indicators
- Receive alerts when approaching budget limits

### 7. Generate Reports (PRO Only)
- View detailed monthly financial reports
- Download PDF reports
- Enable automated monthly email reports

---

## ⚙️ Configuration

### Application Properties

Key configuration in `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=9090

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/SmartExpens?allowPublicKeyRetrieval=true&useSSL=false&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Stripe Configuration (Test Mode)
stripe.api.key=sk_test_...
stripe.public.key=pk_test_...
stripe.webhook.secret=whsec_...

# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=...
spring.security.oauth2.client.registration.google.client-secret=...
```

### Disable Admin Initializer

To disable automatic admin user creation, add to `application.properties`:

```properties
app.admin.initializer.enabled=false
```

---

## 🔌 Notification Microservice

### ⚠️ IMPORTANT: Both Services Must Run Together

**The SmartExpense application REQUIRES the Notification Microservice to be running simultaneously.** 

The main application depends on the notification microservice for:
- **Creating notification preferences** - Users cannot set up notification preferences without the microservice
- **Sending notifications** - Email and SMS notifications are handled by the microservice
- **Managing notification history** - All notification records are stored in the microservice

**If the notification microservice is not running, the following features will fail:**
- User registration (cannot create notification preferences)
- Notification settings management
- Subscription expiry alerts
- Monthly report email delivery
- All notification-related functionality

### Microservice Details

**Service Information:**
- **Host:** `localhost`
- **Port:** `9091`
- **Base URL:** `http://localhost:9091/api/v1`
- **Database:** Separate MySQL database (`notification_svc_sept_2025`)

**Features:**
- ✅ Email notifications with PDF attachments
- ✅ SMS/WhatsApp notifications via GreenAPI
- ✅ Notification preferences management (create/update/retrieve)
- ✅ Notification history tracking
- ✅ Automatic notification type detection (EMAIL/SMS based on contact info)

**Communication:**
- Main app uses **Spring Cloud OpenFeign** to communicate with the microservice
- RESTful API endpoints for notifications and preferences
- Asynchronous notification processing

### Quick Setup

1. **Navigate to notification service directory:**
   ```bash
   cd ../notification-svc
   # or
   cd path/to/notification-svc
   ```

2. **Configure database and credentials** in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/notification_svc_sept_2025?createDatabaseIfNotExist=true
   spring.datasource.username=root
   spring.datasource.password=your_password
   
   # Email configuration (Gmail example)
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your_email@gmail.com
   spring.mail.password=your_app_password
   
   # GreenAPI configuration (for SMS)
   green-api.instance-id=your_instance_id
   green-api.api-token=your_api_token
   green-api.api-url=https://your_instance.api.green-api.com
   ```

3. **Start the microservice:**
   ```bash
   mvnw.cmd spring-boot:run
   # or
   ./mvnw spring-boot:run
   ```

4. **Verify it's running:**
   - Check logs for: `Started Application in X.XXX seconds`
   - Service should be accessible at `http://localhost:9091`

### Running Both Services

**Recommended approach:** Open two terminal windows:

**Terminal 1 - Main Application:**
```bash
cd ExpenseTracker
mvnw.cmd spring-boot:run
```

**Terminal 2 - Notification Microservice:**
```bash
cd notification-svc
mvnw.cmd spring-boot:run
```

**Startup Order:**
1. Start the notification microservice first (port 9091)
2. Then start the main application (port 9090)

**Verification:**
- Main app: `http://localhost:9090`
- Notification service: `http://localhost:9091/api/v1/preferences?userId=...` (test endpoint)

For detailed information about the notification microservice, see the [Notification Service README](../notification-svc/README.md).

---

## 📚 API Endpoints

### Public Endpoints
- `GET /` - Home/Login page
- `GET /login` - Login page
- `POST /login` - Process login
- `GET /register` - Registration page
- `POST /register` - Process registration
- `GET /login/oauth2/code/google` - Google OAuth2 callback

### Authenticated Endpoints
- `GET /dashboard` - User dashboard
- `GET /transactions` - List transactions
- `POST /transactions` - Add transaction
- `DELETE /transactions/{id}` - Delete transaction
- `GET /payments` - List subscriptions
- `POST /payments` - Add subscription
- `DELETE /payments/{id}` - Delete subscription
- `POST /payments/pay/{id}` - Mark subscription as paid
- `GET /profile` - User profile
- `POST /profile` - Update profile
- `GET /notifications` - Notification preferences
- `POST /notifications/toggle` - Toggle notifications
- `POST /notifications/toggle-monthly-report` - Toggle monthly reports

### PRO Features (Requires PRO Subscription)
- `GET /budget` - Budget overview
- `POST /budget` - Create/update budget
- `DELETE /budget/{id}` - Delete budget
- `GET /report` - Financial reports
- `GET /report/pdf` - Download PDF report

### Upgrade & Payment
- `GET /upgrade` - Upgrade page
- `POST /upgrade/create-checkout-session` - Create Stripe checkout
- `GET /upgrade/success` - Payment success page
- `GET /upgrade/cancel` - Payment cancellation page
- `POST /upgrade/webhook` - Stripe webhook handler

### Admin Endpoints
- `GET /admin` - Admin panel (Admin role required)
- `POST /admin/users/{id}/role` - Change user role

---

## 🏗️ Project Structure

```
ExpenseTracker/
├── src/
│   ├── main/
│   │   ├── java/app/
│   │   │   ├── budget/              # Budget planning module
│   │   │   ├── confg/               # Configuration classes
│   │   │   ├── event/               # Spring Events
│   │   │   ├── exception/           # Custom exceptions
│   │   │   ├── notification/        # Notification integration
│   │   │   ├── payment/             # Payment processing (Stripe)
│   │   │   ├── report/              # Report generation
│   │   │   ├── scheduler/           # Scheduled tasks
│   │   │   ├── security/            # Security configuration
│   │   │   ├── subscription/        # Subscription module
│   │   │   ├── transactions/        # Transaction module
│   │   │   ├── user/                # User module
│   │   │   │   └── init/            # Admin user initializer
│   │   │   ├── wallet/              # Wallet module
│   │   │   └── web/                 # Web layer (Controllers)
│   │   └── resources/
│   │       ├── static/              # Static resources (CSS, JS, images)
│   │       ├── templates/           # Thymeleaf templates
│   │       └── application.properties
│   └── test/                        # Test files
├── pom.xml                          # Maven configuration
├── mvnw                             # Maven wrapper (Unix)
├── mvnw.cmd                         # Maven wrapper (Windows)
└── README.md                        # This file
```

---

## 🔒 Security Features

- **Spring Security** - Comprehensive security framework
- **BCrypt Password Encryption** - Secure password hashing
- **Role-Based Access Control (RBAC)** - Admin and User roles
- **CSRF Protection** - Cross-site request forgery prevention
- **Secure Sessions** - HTTP-only, secure cookie sessions
- **Input Validation** - Server-side validation
- **SQL Injection Prevention** - JPA parameterized queries
- **OAuth2 Integration** - Secure third-party authentication

---

## 🧪 Testing

Run all tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=TransactionServiceTest
```

**Test Database:** Uses H2 in-memory database for testing (configured in `src/test/resources/application.properties`).

---

## 📅 Scheduled Tasks

The application includes automated scheduled tasks:

1. **Subscription Expiry Notifications**
   - Runs daily at 9:00 AM
   - Sends alerts 7 days before subscription expiry

2. **Monthly Reports**
   - Runs on the first day of each month at 9:00 AM
   - Generates and emails PDF reports to PRO users with enabled monthly reports

---

## 🐛 Troubleshooting

### Common Issues

**1. Database Connection Error**
- Ensure MySQL server is running
- Verify database credentials in `application.properties`
- Check if database exists or allow auto-creation

**2. Port Already in Use**
- Change port in `application.properties`: `server.port=9091`
- Or stop the process using port 9090

**3. Admin User Not Created**
- Check if database already contains users
- Verify `app.admin.initializer.enabled=true` in properties
- Check application logs for errors

**4. Stripe Payment Issues**
- Ensure test mode keys are configured
- Check webhook URL configuration
- Verify Stripe account is in test mode

**5. OAuth2 Login Not Working**
- Verify Google OAuth2 credentials in `application.properties`
- Check redirect URI matches: `http://localhost:9090/login/oauth2/code/google`

---

## 📝 Development Notes

### Database Schema
- Tables are auto-created on startup (`spring.jpa.hibernate.ddl-auto=update`)
- Schema updates automatically based on entity changes
- **Warning:** In production, use `validate` or `none` instead of `update`

### Logging
- Logging level: `WARN` for org packages
- SQL logging: Disabled by default (set to `DEBUG` for development)
- Admin user creation is logged at `INFO` level

### Caching
- Spring Cache enabled for performance optimization
- Cache configuration in `BeanConfiguration.java`

---

## 🎯 Future Enhancements

- Mobile application (iOS/Android)
- Multi-currency support
- Investment tracking
- Goal setting and tracking
- Banking API integration
- Collaborative budgeting
- Data export (CSV, Excel)
- Recurring transaction templates

---

## 📄 License

This project is licensed under the MIT License.

---

## 👥 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📞 Support

For issues, questions, or contributions, please open an issue on the GitHub repository.

---

<div align="center">

**Built with ❤️ using Spring Boot**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)

</div>
