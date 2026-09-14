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

<h2>Location Tracking</h2>

The application uses Android's Foreground Service with the Fused Location Provider to obtain high-accuracy location updates. The user's latitude and longitude are continuously synchronized with Firebase Realtime Database, allowing the Admin Dashboard to display the user's live location on Google Maps.

<h2>Session Management</h2>

The application uses Firebase Authentication and SharedPreferences to maintain login and vehicle-session information. LastScreenManager stores and restores the previously active screen, helping the application continue from the appropriate screen after reopening.

<h2>Firebase Data</h2>

<h3>The application uses Firebase Realtime Database for:</h3>

User profiles <br>
Admin/User connection information  <br>
Vehicle IDs <br>
User IDs  <br>
Connection status  <br>
Live latitude and longitude  <br>

<h3>Firebase offline persistence is also enabled through the application class.</h3>

<h2>Project Structure</h2>

![MyTrackerApp Project Structure](screenshots/Project-Structure.png)

<h3>Project Output</h3>

[📄 View Project Output](docs/Project-Output.pdf)

<h3>Author</h3>
Archana <br>
BCA Student | Java Developer | Android Development<br>

GitHub: Archana-v36
