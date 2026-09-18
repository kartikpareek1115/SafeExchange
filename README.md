# SafeExchange 🔐

A platform where users can safely buy and sell digital assets from each other. Payments are held securely until both sides fulfill their end of the deal — no blind trust required.

## 🚀 Overview

SafeExchange solves a simple problem: when two strangers trade a digital asset online, how does either side know the other won't just disappear after getting paid (or after getting the asset)?

SafeExchange acts as a trusted middle layer:
- The buyer's payment is locked as soon as they claim a listing.
- The seller only gets paid once the buyer confirms they received the asset.
- If something goes wrong, the transaction can be flagged/disputed instead of funds disappearing silently.

## 🔄 How It Works

1. **User logs in** — registration requires email OTP verification; login is blocked until the account is verified.
2. **User chooses to sell or buy.**
3. **Seller lists** a digital asset for sale.
4. **Buyer finds the listing and claims it.**
5. **Payment is held safely** by the platform (funded state).
6. **Seller delivers** the asset.
7. **Buyer confirms receipt** → payment is released to the seller.

Each transaction moves through a finite-state-machine-driven lifecycle (e.g. `pending → funded → released / disputed`), so the status of every deal is always well-defined.

## 🛠️ Tech Stack

**Backend**
- Spring Boot 4.1
- Spring Security with stateless JWT authentication
- JPA entities modeling the transaction domain
- REST APIs documented with Swagger / OpenAPI
- MySQL

**Frontend**
- Vault/ledger-themed UI
- Wax-seal progress visual tracking each transaction stage
- Login/register flow with email OTP verification
- Forgot-password flow with password complexity rules

**Other**
- Gmail SMTP (App Password) for sending OTP emails

## ✨ Features

- 🔑 Secure JWT-based authentication
- 📧 Email OTP verification required before login (no partial access for unverified users)
- 🔁 Forgot-password flow
- 🔒 Password complexity rules
- 📦 Full transaction lifecycle management via FSM
- 📄 Swagger/OpenAPI documentation for all endpoints
- 🎨 Custom vault/ledger-inspired UI with live progress tracking

## 📸 Screenshots

<img width="1410" height="645" alt="image" src="https://github.com/user-attachments/assets/f4fcba28-59e9-4270-ad7a-20333b9fe59f" />

## 📦 Getting Started

```bash
# Clone the repository
git clone <repo-url>
cd safeexchange

# Configure application.properties / .env with your DB and Gmail SMTP credentials

# Run the backend
./mvnw spring-boot:run
```

Update the database name, JWT secret, and Gmail App Password in your config before running.

## 🗺️ Roadmap

- [ ] Dispute resolution flow
- [ ] Admin dashboard
- [ ] Transaction history / analytics
- [ ] Deployment (cloud hosting)

## 🤝 Feedback

Built as a portfolio project to go beyond CRUD tutorials and understand real-world transaction/trust system design. Feedback from anyone who has built similar systems is very welcome!

## 📄 License

This project is open source. Feel free to explore, fork, or reach out with suggestions.
