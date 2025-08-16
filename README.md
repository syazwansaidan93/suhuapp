# Suhu Widget: A Temperature Monitoring App Widget

This project provides a simple, reliable Android home screen widget for monitoring real-time indoor and outdoor temperatures. It's built to be robust, using modern Android development practices to ensure a seamless experience.

## ✨ Key Features

* **Real-time Display:** Shows both the indoor and outdoor temperatures, fetched from a remote API.

* **Automatic Hourly Updates:** The widget uses the **WorkManager API** to periodically fetch new data every hour. This ensures the temperature readings stay up-to-date even when the device is in Doze mode or other power-saving states.

* **Manual Refresh:** A simple tap on the widget triggers an immediate data refresh, allowing you to get the latest temperature on demand.

* **API Fallback:** The app is configured to first attempt to retrieve data from a local API (`http://suhu.home/api/`) and, if that fails, will seamlessly switch to a remote fallback API (`https://syazwansaidan.site/api/`).

* **Clean and Minimalist UI:** The widget has a simple, centered, and easy-to-read design.

## 🛠️ Setup and Installation

### Prerequisites

* **Android Studio:** The latest version is recommended.

* **Android SDK:** Target SDK version 34 or higher.

* **WorkManager:** The project relies on the `work-runtime-ktx` dependency for background tasks.

* **Network Access:** Ensure your device has internet access to reach the APIs.

### Project Structure

* `app/src/main/res/layout/widget_layout.xml`: Defines the visual layout of the widget.

* `app/src/main/res/xml/widget_provider_info.xml`: Configures the widget's size and update period.

* `app/src/main/java/com/wan/suhu/TempAppWidgetProvider.kt`: The main widget provider class that handles updates and user interaction.

* `app/src/main/java/com.wan.suhu/SuhuWorker.kt`: The worker class that performs the actual network requests for data fetching.

### Running the Project

1. Clone this repository to your local machine.

2. Open the project in Android Studio.

3. Ensure your device or emulator has network access to the specified API endpoints.

4. Build and run the project. The app will install on your device.

5. Add the "Suhu Widget" from your device's home screen widget selector.

## ⚙️ API Endpoints

The app expects the following JSON response format from both the primary and fallback APIs:

```
{
  "outdoorTempC": 25.5,
  "indoorTempC": 22.1
}

```

The app will attempt to connect to:

1. **Primary API:** `http://suhu.home/api/`

2. **Fallback API:** `https://syazwansaidan.site/api/`

## troubleshooting

### Widget Not Updating Automatically

If the widget is not updating automatically on an hourly basis, it is likely due to **Android's battery optimization features**. To fix this, you need to manually disable battery optimization for the app:

1. Go to your phone's **Settings** -> **Apps** -> **Suhu**.

2. Select **Battery** or **Battery Usage**.

3. Change the setting from "Optimized" to **Unrestricted** or "Don't optimize."
