# SplitBill

A bill splitting Android app built with Compose and Firebase.

## Setup Instructions

1. Ensure you have the `google-services.json` placed correctly in `app/`.
2. Enable Authentication (Email/Password) and Firestore in your Firebase Console.
3. Deploy the security rules defined in `firestore.rules` to your Firebase project to enforce member-scoped security. This is required for the app to function securely.
