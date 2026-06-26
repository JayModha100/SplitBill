# SplitBill

SplitBill is an Android application that helps groups keep track of shared expenses and settle payments with minimal hassle. Whether it's a trip, a roommate household, or a group outing, SplitBill allows users to create groups, record expenses, and calculate who owes whom.

## Features

- Google Sign-In using Firebase Authentication
- Create and join groups using a unique join code
- Secure cloud storage with Firebase Firestore
- Track shared expenses within a group
- Calculate balances between members
- Simplify debts to reduce the number of transactions required for settlement
- UPI payment support for quick settlements
- Clean and responsive interface built with Jetpack Compose

## Tech Stack

- Kotlin
- Jetpack Compose
- Firebase Authentication
- Firebase Firestore
- Android Credential Manager
- Material 3

## Getting Started

### Prerequisites

- Android Studio
- Android SDK 24 or above
- Firebase project with Authentication and Firestore enabled

### Installation

1. Clone the repository.

```bash
git clone https://github.com/JayModha100/SplitBill
```

2. Open the project in Android Studio.

3. Connect the project to Firebase by adding your `google-services.json` file to the `app` directory.

4. Sync Gradle and build the project.

5. Run the application on an emulator or physical Android device.

## Project Structure

```
app/
├── data/
│   ├── model/
│   └── repository/
├── domain/
├── ui/
│   ├── auth/
│   ├── group/
│   ├── home/
│   ├── navigation/
│   └── theme/
└── util/
```

## Future Improvements

- Expense history
- Group invitations through links
- Notifications and reminders
- Offline support
- Currency selection
- Expense categories and analytics

## Contributors

- Jay Modha
- Aastik Choudhary

## License

This project is intended for educational purposes.

## Screenshots

<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/49616d1c-15c5-452d-9473-615ff4f4f924" />

<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/563e531b-5a48-43d3-a563-d8d35bf7cf36" />

<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/33fe876d-1b8f-4aae-b169-412fed4b4322" />

<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/d9a333cb-0743-431a-9ef4-16775a90dab2" />

<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/b9ef4b85-ff9d-44f6-9f1d-81e37a444712" />

<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/4a26d341-88d3-42f8-830f-49f78f46e953" />
