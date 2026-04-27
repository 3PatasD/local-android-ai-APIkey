FROM android:latest

WORKDIR /app

# Copy project files
COPY . .

# Make gradlew executable
RUN chmod +x gradlew

# Build debug APK
RUN ./gradlew assembleDebug

# The APK will be at app/build/outputs/apk/debug/app-debug.apk
