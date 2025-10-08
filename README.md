# BiometricApp

BiometricApp is an Android application that demonstrates biometric authentication (such as fingerprint) for user registration and secure data retrieval. The app makes use of Jetpack Compose for its UI, the AndroidX Biometric API for authentication, and integrates with Firebase for backend data storage.

## Features

- **Biometric Authentication:** Authenticate users using fingerprint or other supported biometric methods.
- **User Registration:** Register a user securely after successful biometric authentication.
- **Secure Data Storage:** Store user data (name, ID) in Firestore via Firebase.
- **Data Retrieval:** Retrieve registered user data after biometric authentication.
- **Modern UI:** Built using Jetpack Compose and Material3 design.

## How It Works

1. **Check Biometric Availability:** Users can check if biometric authentication is available on their device.
2. **Register New User:** 
   - Tap "Register" and enter a name and unique ID.
   - Authenticate with biometrics (e.g., fingerprint).
   - Upon success, user data is stored securely in Firebase Firestore.
3. **Retrieve Data:**
   - Enter the ID to retrieve.
   - Authenticate biometrically.
   - Upon successful authentication, the app fetches and displays the user data from Firestore.

## Project Structure

- `app/src/main/java/com/biometricapp/ui/`
  - **MainActivity.kt:** Entry point. Handles initialization and authentication.
  - **BiometricScreen.kt:** Main UI composable with registration and retrieval options.
  - **MainViewModel.kt:** ViewModel for managing UI state and logic.
  - **MainViewModelFactory.kt:** Factory for ViewModel creation.
  - **BiometricAppTheme.kt:** Compose theme configuration.
- `app/src/main/java/com/biometricapp/data/`
  - **UserRepository.kt:** Handles Firebase Firestore interactions for user data.
  - **User.kt:** Data class representing a user.
- `app/src/main/java/com/biometricapp/ui/theme/`
  - **Color.kt, Type.kt, Theme.kt:** Material theme configuration.

## Dependencies

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [AndroidX Biometric](https://developer.android.com/training/sign-in/biometric-auth)
- [Firebase Auth & Firestore](https://firebase.google.com/docs/android/setup)
- [Material3](https://developer.android.com/jetpack/compose/themes/material3)

## Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/gjh70/BiometricApp.git
   ```

2. **Configure Firebase:**
   - Create a Firebase project and add your Android app.
   - Download the `google-services.json` file and place it in `app/`.
   - Enable Firestore and (optionally) Anonymous Auth.

3. **Build and Run:**
   - Open the project in Android Studio.
   - Sync Gradle and run the app on a device with biometric capabilities.

## Screenshots
(images/screenshot.png)



## License

MIT License

---

**Note:** This app is a demonstration and should not be used as-is for production apps without further security reviews.
