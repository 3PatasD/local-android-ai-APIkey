# 📱 How to Compile APK and Create GitHub Release

This guide explains how to compile the APK with the new API Key feature and publish it as a GitHub release.

## Prerequisites

- **Android Studio** 2024.1+ (or Android SDK)
- **JDK 17+**
- **Git**
- GitHub account (for publishing releases)

## Step 1: Clone and Checkout the Feature Branch

```bash
# Clone the repository
git clone https://github.com/3PatasD/local-android-ai-APIkey.git
cd local-android-ai-APIkey

# Checkout the feature branch with API Key implementation
git checkout claude/add-api-key-feature-GrmSc

# Verify you're on the correct branch
git branch
# Should show: * claude/add-api-key-feature-GrmSc
```

## Step 2: Build the APK

### Method A: Using Android Studio (Recommended)

1. **Open the Project**
   - Launch Android Studio
   - File → Open
   - Select the cloned `local-android-ai-APIkey` directory
   - Click OK

2. **Wait for Gradle Sync**
   - Android Studio will automatically sync Gradle files
   - This may take 2-5 minutes
   - Wait for the sync to complete before proceeding

3. **Build the APK**
   - Menu: Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Or use keyboard shortcut: Ctrl+Shift+B (Windows/Linux) or Cmd+Shift+B (Mac)
   - Wait for the build to complete

4. **Locate the APK**
   - Build output: `app/build/outputs/apk/debug/app-debug.apk`
   - You can also click "Locate" in the build notification

### Method B: Using Command Line

```bash
# Set Android SDK path (adjust for your system)

# Linux:
export ANDROID_HOME=$HOME/Android/Sdk

# macOS:
export ANDROID_HOME=$HOME/Library/Android/sdk

# Windows (PowerShell):
$env:ANDROID_HOME="C:\Users\<YourUsername>\AppData\Local\Android\Sdk"

# Build the APK
./gradlew assembleDebug

# The APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

### Method C: Using Gradle Wrapper Script

```bash
# For Linux/macOS:
chmod +x gradlew
./gradlew assembleDebug

# For Windows:
gradlew.bat assembleDebug
```

## Step 3: Test the APK (Optional but Recommended)

```bash
# Install on connected Android device
adb install app/build/outputs/apk/debug/app-debug.apk

# Or in Android Studio:
# Select Run → Run 'app' from the menu
```

**After installation, test the new API Key feature:**

```bash
# Get device IP (replace with your device's IP)
DEVICE_IP="192.168.1.100:8005"

# 1. Generate an API key
curl -X POST http://$DEVICE_IP/api-keys/generate \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Key"}'

# Expected response:
# {
#   "success": true,
#   "key": "sk_AbCdEfGhIjKlMnOpQrStUvWxYz123456",
#   "name": "Test Key"
# }

# 2. Test protected endpoint with the key
API_KEY="sk_AbCdEfGhIjKlMnOpQrStUvWxYz123456"
curl -X POST http://$DEVICE_IP/ai/text \
  -H "Authorization: Bearer $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Hello", "maxTokens": 50}'

# 3. Verify that public endpoints still work without keys
curl http://$DEVICE_IP/health
```

## Step 4: Create GitHub Release

### Option A: Using GitHub Web Interface

1. **Navigate to Releases Page**
   - Go to: https://github.com/3PatasD/local-android-ai-APIkey/releases
   - Click "Create a new release"

2. **Fill in Release Details**
   - **Tag version:** `v1.1.0` (or your preferred version)
   - **Release title:** `🔐 API Key Authentication System (v1.1.0)`
   - **Description:** Copy from `RELEASE_v1.1.0.md`

3. **Upload the APK**
   - Scroll to "Attach binaries by dropping them here or selecting them"
   - Drag and drop or select: `app/build/outputs/apk/debug/app-debug.apk`
   - Wait for upload to complete

4. **Publish Release**
   - Check "This is a pre-release" if not final (optional)
   - Click "Publish release"

### Option B: Using GitHub CLI (if installed)

```bash
# Navigate to the repository directory
cd /path/to/local-android-ai-APIkey

# Create release with APK
gh release create v1.1.0 \
  --title "🔐 API Key Authentication System (v1.1.0)" \
  --notes-file RELEASE_v1.1.0.md \
  app/build/outputs/apk/debug/app-debug.apk
```

### Option C: Using Git Command Line with Tags

```bash
# Create and push tag
git tag -a v1.1.0 -m "Release v1.1.0: API Key Authentication System"
git push origin v1.1.0

# Then go to GitHub releases page and create release from tag
```

## Step 5: Verify Release

1. **Check Release Page**
   - Visit: https://github.com/3PatasD/local-android-ai-APIkey/releases/tag/v1.1.0
   - Verify APK is listed as an asset
   - Verify all details are correct

2. **Share Release**
   - Copy the release URL
   - Share with users
   - Direct users to download APK from the release page

## Release Contents

Your GitHub release should include:

✅ **App Version:** 1.1.0  
✅ **APK File:** app-debug.apk (~50-100MB)  
✅ **Release Notes:** Features, usage, installation  
✅ **Documentation:** Links to /api-keys/help  
✅ **Examples:** API usage patterns  
✅ **Build Instructions:** Reference to BUILD_INSTRUCTIONS.md

## Installation for End Users

After release, users can:

```bash
# Download the APK from the release page
# Then install it

# Option 1: Using adb
adb install <path-to-downloaded-apk>/app-debug.apk

# Option 2: Direct file transfer
# Copy APK to device via USB and install directly
```

## Troubleshooting Build Issues

### Issue: "SDK location not found"
```bash
# Create local.properties in project root
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

### Issue: "Gradle sync failed"
- File → Invalidate Caches / Restart
- Wait for sync to complete
- Retry build

### Issue: "Insufficient storage"
- Delete previous builds: `./gradlew clean`
- Ensure at least 5GB free space

### Issue: "Build tools not found"
- Open Android Studio Settings
- Appearance & Behavior → System Settings → Android SDK
- SDK Tools tab → Install required build tools
- Retry build

## APK Naming Convention

For production releases, consider renaming:

```bash
# From:
app-debug.apk

# To:
ai-phone-server-v1.1.0-api-key-authentication.apk
```

## Release Checklist

Before publishing, verify:

- [ ] APK compiles without errors
- [ ] APK size is reasonable (~50-100MB)
- [ ] API Key feature works on test device
- [ ] All new endpoints respond correctly
- [ ] Public endpoints work without keys
- [ ] Documentation is complete
- [ ] Release notes are clear
- [ ] GitHub tag is created
- [ ] APK is attached to release
- [ ] Release notes are published

## Next Steps

After successful release:

1. **Update Documentation**
   - Add release to README.md
   - Update version numbers in docs

2. **Announce Release**
   - GitHub discussion/announcement
   - Email users
   - Social media (if applicable)

3. **Gather Feedback**
   - Monitor GitHub issues
   - Collect user feedback
   - Plan improvements

4. **Plan Next Features**
   - Review feature requests
   - Plan v1.2.0+ improvements
   - Consider:
     - API key expiration
     - Rate limiting
     - Key scopes
     - Audit logging

## Support Resources

- **Full Documentation:** `/help` endpoint on running server
- **API Key Docs:** `/api-keys/help` endpoint
- **Build Guide:** See `BUILD_INSTRUCTIONS.md`
- **Release Notes:** See `RELEASE_v1.1.0.md`
- **Source Code:** Branch `claude/add-api-key-feature-GrmSc`

---

**Questions?** Open an issue on GitHub:  
https://github.com/3PatasD/local-android-ai-APIkey/issues

**Successfully compiled and released?** Congratulations! 🎉
