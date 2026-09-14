<h1>MyTrackerApp</h1>

<h3>A real-time Android vehicle tracking system built with Java, Firebase, Google Maps, QR-based vehicle connectivity, and foreground location tracking.</h3>

<h2>Features</h2>
📱 Phone number authentication with Firebase OTP <br>
👤 Admin and User role management <br>
🚗 Vehicle connection using QR code <br>
📷 Admin generates a vehicle QR code <br>
🔗 User scans QR code to connect with an Admin <br>
📍 Real-time user location tracking using Google Maps <br>
🔄 Real-time location synchronization using Firebase Realtime Database <br>
🛰️ Foreground location tracking service <br>
⏱️ Location updates every 5 seconds while tracking <br>
🗺️ Admin dashboard with live user markers and location details <br>
🔐 Session management using Firebase Authentication and SharedPreferences <br>
🔁 Automatic screen/session restoration using LastScreenManager <br>
💾 Firebase offline persistence <br>
🛑 Start/Stop location tracking <br>
🚪 Secure logout and session cleanup <br>
👤 User profile management with name, phone number, vehicle number, and role <br>

<h3>The QR connection flow stores the user and vehicle relationship in Firebase and changes the connection status to connected.</h3>

<h2>Technologies Used</h2>
Java <br>
Android Studio <br>
Firebase Authentication <br>
Firebase Realtime Database <br>
Google Maps SDK <br>
Google Fused Location Provider <br>
ZXing / JourneyApps Barcode Scanner <br>
SharedPreferences <br>
Android Foreground Service <br>
XML Layouts <br>
Gradle <br>

<h2>Application Flow</h2>

![MyTrackerApp Application Flow](screenshots/Application%20flow%20chart.png)
