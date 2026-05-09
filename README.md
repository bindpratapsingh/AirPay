# AirPay - Offline UPI Payments 💸

**AirPay** is an Android application designed to enable UPI (Unified Payments Interface) transactions without an active internet connection. By utilizing the **\*99# USSD** service and **IVR** (Interactive Voice Response) channels, AirPay provides financial accessibility to users in low-connectivity areas or those with limited data access.

---

## 📥 Downloads
You can find the latest stable version of the app in the **Releases** section.
- **[Download Latest APK](https://github.com/bindpratapsingh/AirPay/releases/latest)**

---

## ✨ Features
- **Offline Money Transfer:** Send money using a Mobile Number or UPI ID without internet.
- **Balance Check:** Instant bank balance inquiry via USSD automation.
- **Scan & Pay:** Extract UPI IDs from QR codes using ML Kit (Offline entry thereafter).
- **Biometric Security:** Fingerprint and PIN-based locking to keep your wallet safe.
- **Favourites:** Save frequent contacts for faster, one-tap offline payments.
- **Transaction History:** Localized log of all your offline activities.
- **Jio/IVR Support:** Specialized mode for networks that prioritize IVR over USSD.

## 🛠️ How it Works
AirPay simplifies the complex \*99# USSD menu system. When you initiate a payment, the app dynamically constructs the correct USSD string and triggers a system call, automating the steps you would otherwise have to do manually.

## 🚀 Tech Stack
- **Language:** Java
- **UI:** Material 3, CoordinatorLayout, BottomAppBar
- **Scanner:** Google ML Kit Barcode Scanning
- **Storage:** SharedPreferences (Encrypted)
- **Security:** BiometricPrompt API

## 🛠️ Installation & Setup
1. Clone the repository: `git clone https://github.com/bindpratapsingh/AirPay.git`
2. Open in **Android Studio**.
3. Build the APK: `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
4. Install on an Android device with a SIM card registered for UPI.

## ⚠️ Disclaimer
*AirPay is an independent tool designed to facilitate the use of the \*99# USSD service provided by NPCI. Ensure your bank and mobile operator support USSD/IVR payments before use. Carrier charges for USSD/Calls may apply.*

## 📩 Contact & Support
For any inquiries, support, or feedback, please reach out via:
- **LinkedIn:** [Bind Pratap Singh](https://www.linkedin.com/in/bind-pratap-singh)
- **Bug Reports:** [Open an Issue](https://github.com/bindpratapsingh/AirPay/issues/new)

---
Developed by [Bind Pratap Singh](https://github.com/bindpratapsingh)
<img width="256" height="256" alt="icon" src="https://github.com/user-attachments/assets/34d212a6-29da-4269-9c72-167834d4276e" />
