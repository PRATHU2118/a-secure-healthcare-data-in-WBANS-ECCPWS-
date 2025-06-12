# ECCPWS - ECC-based Secure Protocol for WBAN Systems

## 🔒 Project Title
**Securing Healthcare Data in WBANs: An ECC-Based Approach**

## 🧠 Overview

This project implements a secure communication protocol for **Wireless Body Area Networks (WBANs)** using **Elliptic Curve Cryptography (ECC)**. The protocol aims to safeguard sensitive health data in wearable health monitoring systems (WHMS) from major threats like:

- Passive insider attacks  
- Replay attacks  
- Impersonation attacks  
- Desynchronization issues  
- Lack of forward/backward secrecy

Based on the paper:  
📄 *ECCPWS: An ECC-based protocol for WBAN systems*  
📝 Published in *Computer Networks (2023)*

## 🎯 Objectives

- Design a lightweight and secure ECC-based authentication system
- Achieve **forward & backward secrecy** with session keys and timestamps
- Encrypt patient data before storage and during transmission
- Provide role-based secure login for **Admin**, **Doctors**, and **Patients**
- Maintain data integrity, confidentiality, and user authenticity

## 🧩 Modules

1. **Dataset Management**
   - Uses a human vital sign dataset
   - Encrypts & stores health data securely

2. **ECC Key Generation**
   - Generates public/private keys
   - Implements ECC-based key exchange

3. **User Login & Registration**
   - ECC-based authentication
   - Role-based access (Patient, Doctor, Admin)

4. **Encryption & Secure Storage**
   - Encrypts data before storage using ECC
   - Ensures confidentiality and integrity

5. **Session Key Establishment**
   - ECC-based mutual authentication
   - Secure session key after login

6. **Secure Data Transfer**
   - Prevents replay, impersonation, and insider attacks
   - Uses ECC during communication

## 🩺 Dataset Used

**🔗 Human Vital Sign Dataset**  
📌 Link: [Kaggle - Vital Sign Dataset](https://www.kaggle.com/datasets/nasirayub2/human-vital-sign-dataset)

Includes:
- Heart rate  
- Blood pressure  
- Body temperature  

Applications:
- Healthcare monitoring simulation
- WBAN protocol testing

## 🧪 How to Run the Project

> ⚙️ Backend: Java  
> 🖥 Frontend: HTML + CSS  
> 🛢 Database: (e.g., MySQL or SQLite, based on your code)

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/ECCPWS.git
   cd ECCPWS
   ```

2. Compile Java files:
   ```bash
   javac -d bin src/*.java
   java Main
   ```

3. Run the frontend:
   - Open `index.html` in a browser
   - Login/Register as Patient/Doctor/Admin

4. Dataset:
   - Download from the Kaggle link above
   - Place in the `/data/` folder

## 🖼 Sample Screens / Features

- ✅ User Registration (Patient/Doctor/Admin)
- ✅ Login using ECC token
- ✅ Encrypted data storage
- ✅ Secure session key validation
- ✅ Admin interface with logs
- ✅ Role-based data views

## 🔐 Security Architecture Highlights

- ECC public/private key pair per user
- Session key generation using ECC key agreement
- Protection against:
  - Replay attacks
  - Impersonation
  - Insider threats
- Timestamp + Random values = Desynchronization-resistant

## 📊 Comparative Analysis

- Improved **storage cost**
- Reduced **encryption time**
- High **security guarantees**
- Compared to other ECC-based WBAN protocols

## 👥 Team Members

- **K. Manoj Kumar Reddy** (226003078)  
- **K. Sai Koushik** (226003081)  
- **Prathap R. V.** (226003103)

Guided by:  
**Mrs. Vimala Devi P**, Assistant Professor – II, SRC SASTRA

## 🧾 References

1. F. Piromoradian, M. Safkhani, S.M. Dakhilalian, *ECCPWS: An ECC-based protocol for WBAN systems*, Computer Networks, 2023.  
2. R. Amin et al., *A robust patient monitoring system using wireless medical sensor networks*, 2018.  
3. S.A. Chaudhry et al., *Biometric authentication scheme for TMIS*, 2016.  
4. M. Yavari et al., *Blockchain-based authentication protocol for IoT*, 2020.  
5. H. Khan et al., *Lightweight ECC for WBANs*, 2019.  
6. P. Mohit, *Mutual authentication for E-healthcare*, 2021.

## 📄 License

This project is licensed under the MIT License. See the `LICENSE` file for more details.

## 🌟 Feedback & Contributions

Feedback, forks, and pull requests are welcome.  
If you liked the project, don’t forget to ⭐ the repository!
