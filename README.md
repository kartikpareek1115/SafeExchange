# SafeExchange 🔐

A peer-to-peer digital asset escrow platform built with **Spring Boot**, designed to act as a trust layer for P2P transactions — funds/assets are held securely until both parties fulfill the agreed conditions.

---

## 📖 Overview

SafeExchange solves a common problem in peer-to-peer transactions: **trust between strangers**. Instead of either party sending first and hoping the other follows through, SafeExchange holds the asset in escrow and releases it only when the agreed conditions are met — with built-in support for disputes.

This project was built to go beyond typical CRUD tutorials and explore real-world backend system design: state machines, secure authentication, and clean REST API design.

---

## ✨ Features

- 🔑 **Stateless Authentication** — JWT-based auth with Spring Security
- 🔁 **Escrow State Machine** — a finite-state-machine driven flow (`PENDING → FUNDED → RELEASED / DISPUTED`, etc.) that enforces valid transitions and prevents invalid state changes
- 📦 **Domain-Driven Entities** — JPA entities modeling users, escrow transactions, and transaction states
- 📄 **API Documentation** — fully documented REST endpoints via Swagger/OpenAPI
- 🛡️ **Secure by Design** — stateless sessions, token-based access control

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| Framework | Spring Boot 4.1 |
| Security | Spring Security + JWT |
| Persistence | Spring Data JPA |
| Database | MySQL |
| API Docs | Swagger / OpenAPI |
| Build Tool | Maven |

---

## 🏗️ Architecture

The project was built incrementally:

1. **Project Setup** — Spring Boot 4.1 initialization
2. **Domain Modeling** — JPA entities for users and escrow transactions
3. **Security Layer** — stateless JWT authentication with Spring Security
4. **Escrow Logic** — a finite-state-machine governing the escrow lifecycle
5. **REST Controllers** — exposing the escrow and auth operations
6. **API Documentation** — Swagger integration for endpoint discovery and testing

```
Client → REST Controller → Service Layer (FSM validation) → Repository → MySQL
                ↑
         JWT Auth Filter (Spring Security)
```

---

## 🔄 Escrow Flow

A typical transaction moves through the following states:

```
PENDING → FUNDED → RELEASED
              ↓
          DISPUTED
```

- **PENDING** — escrow created, awaiting funding
- **FUNDED** — asset/funds locked in escrow
- **RELEASED** — funds released to the recipient after conditions are met
- **DISPUTED** — either party raises a dispute, halting release until resolved

---

## 📷 API Documentation

Full API documentation is available via Swagger UI once the app is running:

```
http://localhost:8080/swagger-ui/index.html
```




<!-- ![Swagger UI](docs/swagger-ui.png) -->
<img width="1317" height="639" alt="image" src="https://github.com/user-attachments/assets/7a990322-c4cf-4b3e-bcbd-39a9cd60ca64" />

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven
- MySQL

### Setup

```bash
# Clone the repository
git clone https://github.com/kartikpareek1115/safeexchange.git
cd safeexchange

# Configure your database in application.properties
# spring.datasource.url=jdbc:mysql://localhost:3306/safeexchange_db
# spring.datasource.username=your_username
# spring.datasource.password=your_password

# Build and run
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080` and Swagger docs at `/swagger-ui/index.html`.

---

## 🗺️ Roadmap

- [x] Spring Boot project setup
- [x] JPA entities & database design
- [x] JWT-based authentication
- [x] Escrow FSM logic
- [x] REST controllers
- [x] Swagger API documentation
- [ ] Frontend (vault/ledger themed UI)
- [ ] Deployment

---

## 👤 Author

**Kartik Pareek** (Void)
B.Tech CSE, LNCT Indore
Java Backend Developer

- GitHub: (https://github.com/kartikpareek1115)
- LinkedIn: (https://linkedin.com/in/kartikpareekofficial)

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
