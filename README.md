# FitLife - Fitness Workout App

A comprehensive fitness tracking and workout planning Android application.

## Features

- 🔐 User Authentication (Email/Password + Guest Mode)
- 💪 Workout Management (Create, Edit, Delete)
- 📅 Weekly Planning with Day Selection
- ✅ Active Workout Tracking with Progress
- 📊 BMI Tracking and Health Metrics
- 🔔 Two-Factor Authentication (Demo)
- 📱 Material Design 3 UI
- 🌙 Dark/Light Theme Support
- 📤 Share Workouts and Equipment Lists

## Tech Stack

- **Language**: Java
- **Architecture**: MVVM for Workout Management
- **Database**: Firebase Firestore + Room (Offline Cache)
- **Authentication**: Firebase Auth
- **UI**: Material Design 3, ViewPager2, RecyclerView
- **Image Loading**: Glide
- **Gestures**: Custom GestureHelper (Swipe, Double Tap, Shake, Pinch)

## Project Structure
app/
├── src/main/java/.../
│ ├── activities/ # All Activities
│ ├── fragments/ # Fragments (Home tabs)
│ ├── adapters/ # RecyclerView Adapters
│ ├── models/ # Data Models
│ ├── database/ # Room Database
│ ├── repository/ # Repository Pattern
│ ├── viewmodels/ # MVVM ViewModels
│ └── utils/ # Helper Classes
└── src/main/res/ # Layouts, Drawables, Values
