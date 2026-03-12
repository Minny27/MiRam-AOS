# Google Play Production Release Guide

## 1. Create the Play Console account

1. Prepare a dedicated Google account for Play Console ownership.
2. Choose the correct account type:
   - Personal: individual / hobby / student use
   - Organization: company / business / government / commercial use
3. Pay the one-time Play Console registration fee.
4. Complete developer identity verification.
5. Verify required contact email and phone numbers.

## 2. Prepare required account information

### Personal account
- Developer name
- Legal name
- Legal address
- Contact email
- Contact phone
- Developer email shown on Google Play

### Organization account
- Developer name
- D-U-N-S number
- Organization name and address
- Organization phone
- Organization website
- Contact name
- Contact email
- Contact phone
- Developer email shown on Google Play
- Developer phone shown on Google Play

## 3. Create the upload keystore

Use Android Studio:

1. Build > Generate Signed Bundle / APK
2. Choose Android App Bundle
3. Create new keystore

Or use `keytool`:

```bash
keytool -genkey -v \
  -keystore miram-upload-key.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias upload
```

Important:
- Keep the keystore outside the repository if possible.
- Back it up securely.
- Do not lose the passwords.

## 4. Configure local signing

Copy the example file:

```bash
cp key.properties.example key.properties
```

Fill in:

```properties
storeFile=/absolute/path/to/your-release-key.jks
storePassword=...
keyAlias=upload
keyPassword=...
```

## 5. Set release version

Edit `gradle.properties`:

```properties
appVersionCode=1
appVersionName=1.0.0
```

Rules:
- Increase `appVersionCode` on every Play upload.
- Update `appVersionName` for human-readable releases.

## 6. Build the release bundle

```bash
./gradlew :app:bundleRelease
```

Expected output:

```text
app/build/outputs/bundle/release/app-release.aab
```

## 7. Configure Play App Signing

For a new app:
1. Create the app in Play Console.
2. Start a release.
3. Upload the signed `.aab`.
4. Let Google manage the app signing key, or upload your own if you need the same key across stores.

## 8. Complete Play Console setup

Before publishing, complete at least:
- App name
- Short description
- Full description
- App icon
- Feature graphic
- Screenshots
- App category
- Contact details
- Privacy policy URL if applicable
- Data safety form
- Content rating questionnaire
- Ads declaration
- Target audience / news / special app declarations if prompted

## 9. Testing path

### If the account is a new personal account
- Run a closed test first
- Keep at least 12 testers opted in for 14 consecutive days
- Then apply for production access

### Recommended rollout order
1. Internal testing
2. Closed testing
3. Production staged rollout

## 10. App-specific policy notes for MiRam

- This app uses exact alarm permissions, so Play may ask for permission declarations during review.
- This app appears to be an alarm app, which is the kind of app where exact alarm usage may be core functionality, but the declaration still needs to be filled in correctly in Play Console.
- Final `applicationId` should be decided before first release. Changing it later creates a different app on Google Play.

## 11. Current project release config

Already configured in code:
- Release signing reads from `key.properties`
- `key.properties` is gitignored
- `key.properties.example` is included
- Release build enables code shrinking and resource shrinking
- Version values are read from `gradle.properties`

## 12. Recommended next actions

1. Decide final production package name
2. Create Play Console owner account
3. Choose personal vs organization account
4. Create upload keystore
5. Fill `key.properties`
6. Build `bundleRelease`
7. Create Play Console app
8. Upload to internal test first
