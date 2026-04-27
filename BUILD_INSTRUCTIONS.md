# Build Instructions for AI Phone Server v1.1.0

## ✨ New Features in v1.1.0
- **API Key Authentication System**: Secure your AI endpoints with API keys
- **API Key Management Endpoints**: Generate, list, revoke, and manage API keys
- **Automatic Authentication Middleware**: Transparent protection for sensitive operations
- **Usage Statistics**: Track API key usage and performance

## Prerequisites

1. **Android Studio** (recommended) or **Android SDK**
   - Download from: https://developer.android.com/studio
   - Minimum API Level: 31
   - Target API Level: 35

2. **Java Development Kit (JDK)**
   - JDK 17 or higher recommended

3. **Git** (to clone the repository)

## Building the APK

### Option 1: Using Android Studio (Recommended)

1. Clone the repository:
   ```bash
   git clone https://github.com/3PatasD/local-android-ai-APIkey.git
   cd local-android-ai-APIkey
   git checkout claude/add-api-key-feature-GrmSc
   ```

2. Open the project in Android Studio:
   - File → Open → Select the project folder

3. Wait for Gradle to sync

4. Build the APK:
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Or use: Shift+Ctrl+B

5. Find the built APK:
   - Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
   - Release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`

### Option 2: Using Command Line

1. Clone and navigate to the project:
   ```bash
   git clone https://github.com/3PatasD/local-android-ai-APIkey.git
   cd local-android-ai-APIkey
   git checkout claude/add-api-key-feature-GrmSc
   ```

2. Set ANDROID_HOME (adjust path as needed):
   ```bash
   # macOS
   export ANDROID_HOME=$HOME/Library/Android/sdk
   
   # Linux
   export ANDROID_HOME=$HOME/Android/Sdk
   
   # Windows (PowerShell)
   $env:ANDROID_HOME="C:\Users\<YourUsername>\AppData\Local\Android\Sdk"
   ```

3. Build the APK:
   ```bash
   # Debug build
   ./gradlew assembleDebug
   
   # Release build (requires signing configuration)
   ./gradlew assembleRelease
   ```

4. The APK will be located at:
   - Debug: `app/build/outputs/apk/debug/app-debug.apk`
   - Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

### Option 3: Using the build script

```bash
chmod +x build.sh
./build.sh
```

## Installation

### Install on Connected Android Device

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Or via Android Studio

1. Run → Run 'app'
2. Select your device/emulator

## Testing the New API Key Feature

### 1. Start the Server
- Open the app on your Android device
- Tap "Start Server"

### 2. Generate an API Key
```bash
curl -X POST http://<DEVICE_IP>:8005/api-keys/generate \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Key"}'
```

**Response:**
```json
{
  "success": true,
  "key": "sk_A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6",
  "name": "Test Key",
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 3. Use the Key for Protected Endpoints
```bash
# AI Text Generation (requires API key)
curl -X POST http://<DEVICE_IP>:8005/ai/text \
  -H "Authorization: Bearer sk_A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6" \
  -H "Content-Type: application/json" \
  -d '{"prompt": "What is AI?", "maxTokens": 100}'
```

### 4. List All Keys
```bash
curl http://<DEVICE_IP>:8005/api-keys/list
```

### 5. Get API Key Help
```bash
curl http://<DEVICE_IP>:8005/api-keys/help
```

## Architecture Changes

### New Files Added:
- `app/src/main/kotlin/me/bechberger/phoneserver/security/APIKeyManager.kt`
  - Handles API key generation, validation, and persistence
  - Stores keys in SharedPreferences (encrypted by Android)

- `app/src/main/kotlin/me/bechberger/phoneserver/security/APIKeyInterceptor.kt`
  - Middleware for request authentication
  - Implements requirement levels (NONE, OPTIONAL, REQUIRED)

### Modified Files:
- `app/src/main/kotlin/me/bechberger/phoneserver/server/WebServer.kt`
  - Added API key manager integration
  - Added authentication middleware
  - Added API key management endpoints (/api-keys/*)
  - Updated documentation endpoint

## API Key Endpoints

| Endpoint | Method | Auth Required | Description |
|----------|--------|----------------|-------------|
| `/api-keys/help` | GET | No | Documentation |
| `/api-keys/generate` | POST | No | Generate new key |
| `/api-keys/list` | GET | No | List all keys |
| `/api-keys/{id}` | GET | No | View key details |
| `/api-keys/{id}` | PUT | No | Update key name |
| `/api-keys/{id}/revoke` | POST | No | Revoke a key |
| `/api-keys/{id}` | DELETE | No | Delete a key |
| `/api-keys/stats` | GET | No | View statistics |

## Protected Endpoints (Require API Key)

- `POST /ai/text` - AI text generation
- `POST /ai/object_detection` - Object detection
- `POST /ai/models/download` - Download AI models
- `POST /ai/text/upload-model-api` - Upload custom models
- `POST /ai/models/cleanup` - Cleanup models
- `DELETE /ai/models/{modelName}` - Delete a model

## Public Endpoints (No Key Required)

- `GET /status` - Server status
- `GET /health` - Health check
- `GET /capabilities` - API capabilities
- `GET /location` - Location services (permission-based)
- `GET /orientation` - Device orientation
- `GET /capture` - Camera capture (permission-based)
- `POST /display` - Display text
- `GET /help` - API documentation

## Troubleshooting

### Gradle Build Fails
- Ensure `ANDROID_HOME` is set correctly
- Run `./gradlew clean` before building
- Update Gradle: `./gradlew wrapper --gradle-version=8.13`

### API Key Not Working
- Verify the key format starts with `sk_`
- Check that the key is active (not revoked)
- Use `GET /api-keys/list` to list valid keys

### Compilation Issues
- Ensure JDK 17+ is installed
- Clear the Gradle cache: `rm -rf .gradle build`
- Check Android SDK tools are updated

## Release Notes

### Version 1.1.0 - API Key Authentication

**Added:**
- Complete API key management system
- Secure authentication for AI endpoints
- Key generation, validation, and revocation
- Usage statistics and tracking
- Backward compatibility with existing endpoints

**Security:**
- Keys stored securely in SharedPreferences
- Support for Bearer token format
- Automatic last-used tracking
- Easy key revocation without deletion

## Support

For issues or questions:
1. Check the API documentation at `/help`
2. Review API key help at `/api-keys/help`
3. Open an issue on GitHub: https://github.com/3PatasD/local-android-ai-APIkey/issues
