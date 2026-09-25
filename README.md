# FaceAttend

An Android attendance app with **Admin** and **Staff** roles, where staff can only mark
attendance if a live selfie matches the face an admin enrolled for them. Each successful
check-in stores the **timestamp, selfie and GPS location**.

Everything runs **fully offline and on-device** — no backend, no network calls, no cloud
face API.

---

## Demo credentials

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Staff | `EMP001` (Aarav Sharma) | `staff123` |
| Staff | `EMP002` (Priya Nair) | `staff123` |

Staff sign in with their **employee ID**. Both demo staff are seeded on first launch with
no face enrolled — an admin has to enrol them first.

Authentication is deliberately dummy, as the assignment allows. Credentials live in
[`Credentials.kt`](app/src/main/java/com/faceattend/app/data/Credentials.kt).

---

## Try it in 60 seconds

1. Sign in as **admin / admin123**.
2. Tap a staff member → **Enrol face** → allow camera → **Capture**.
3. Sign out (overflow menu) and sign in as **EMP001 / staff123**.
4. **Mark attendance** → **Capture**. A matching face records attendance with time and
   location; a different face is rejected.
5. **View attendance history** to see the stored records.

---

## Technology & architecture

**Kotlin · XML Views + ViewBinding · MVVM + Repository · Room · single `:app` module**

| Concern | Choice |
|---|---|
| UI | XML layouts + ViewBinding, Material 3, single Activity + Navigation Component (SafeArgs) |
| Presentation | MVVM — `ViewModel` + `LiveData`, one-shot state cleared via `onStateHandled()` |
| Persistence | Room (SQLite); selfies as JPEG in app-internal storage, paths stored in Room |
| Face detection | ML Kit Face Detection (bundled model, no Play Services dependency) |
| Face recognition | FaceNet TFLite, 160×160 input → 128-d embedding, cosine similarity |
| Camera | CameraX (`Preview` + `ImageCapture`), front lens |
| Location | `FusedLocationProviderClient` |
| DI | Manual — [`AppContainer`](app/src/main/java/com/faceattend/app/AppContainer.kt). A DI framework would be more machinery than this app needs. |

### How a face becomes a decision

```
selfie bitmap
  → ML Kit detects the largest face          (FaceDetectorHelper)
  → crop to the box + 15% margin             (ImageUtils)
  → FaceNet TFLite → 128-d embedding         (FaceEmbeddingHelper)
  → cosine similarity vs enrolled embedding  (FaceMatcher)
  → matched if similarity >= 0.4
```

`FaceRecognizer` is the single entry point for that pipeline, so ViewModels never touch
ML Kit or TFLite directly.

[`FaceMatcher`](app/src/main/java/com/faceattend/app/face/FaceMatcher.kt) is deliberately
free of Android and ML dependencies. It is the actual recognition decision, so it is plain
Kotlin, fully unit-tested, and the embedding *producer* can be swapped without touching
anything downstream.

### Data model

```kotlin
Staff(id, employeeId, name, faceImagePath?, embeddingJson?, createdAt)
AttendanceRecord(id, staffId→Staff, timestamp, selfieImagePath,
                 latitude?, longitude?, matchSimilarity, result)
```

Embeddings are stored as a JSON array string via `EmbeddingCodec`.

### Project layout

```
com.faceattend.app
├── data/       entities · db (Room) · repository · Credentials
├── face/       FaceDetectorHelper · FaceEmbeddingHelper · FaceMatcher · FaceRecognizer
├── util/       SelfieCapture · LocationHelper · ImageFileStore · SessionManager
└── ui/         login · admin (stafflist, addstaff, profile, enroll)
                      · staff (home, attendance, result, history)
                      · capture (shared CameraX fragment)
```

---

## Running it

**Requirements:** Android Studio (AGP 8.7.3 / Gradle 8.13 / Kotlin 2.0.21), JDK 17+,
a device or emulator on **API 24+**.

```bash
./gradlew assembleDebug
```

The APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

Open the project in Android Studio and hit Run, or install manually:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

> **Emulator note:** the AVD's *emulated* front camera cannot serve a still capture — its
> HAL crashes on `takePicture`. Use a **physical device**, or start the emulator with the
> host webcam so there is a real face to detect:
> ```bash
> emulator -avd <name> -camera-front webcam0
> ```
> For location on an emulator, set a position via Extended Controls → Location.

### Tests

```bash
./gradlew testDebugUnitTest lint
```

Covers the recognition decision (`FaceMatcherTest` — similarity maths, threshold
boundaries, degenerate vectors) and embedding serialisation (`EmbeddingCodecTest`).

---

## Decisions & assumptions

- **Failed match attempts are still recorded**, with `result = NOT_MATCHED` and the
  similarity score, so there is an audit trail of who tried to check in as whom.
  Attendance is only *marked* on a match, per the requirement.
- **Location is best-effort.** If the user denies location, attendance still records with
  `latitude`/`longitude` null and the UI says "unavailable", rather than blocking the core
  face-match feature on a secondary permission.
- **Permissions are requested at point of use** — camera when a capture screen opens,
  location only inside the attendance flow (enrolment doesn't need it), never at launch.
- **Admin is not a `Staff` row.** Admin is a fixed credential, not an employee record, so
  the staff list shows only real staff.
- **One face per staff member.** Re-enrolling replaces the previous embedding. Production
  systems typically average several captures for robustness.
- **Capture has a 10s timeout** so a wedged camera HAL cannot strand the UI behind a
  spinner — this was observed on the emulator.
- Threshold **0.4** is the FaceNet author's published cosine threshold. It is a single
  named constant (`FaceMatcher.DEFAULT_THRESHOLD`) and should be tuned against real data —
  for attendance, a false accept is the worse failure, so err higher.

## Limitations

- **No liveness detection.** A photo of an enrolled person's face held to the camera would
  pass. Real deployments need anti-spoofing (blink/depth/challenge-response); it was out of
  scope for a 2-day build.
- **Accuracy is not tuned.** The threshold was not validated against a labelled dataset.
- **The APK is large (~83 MB debug)** because the float32 FaceNet model is ~23 MB and
  native libraries ship for all ABIs. A quantised model and ABI splits would cut this
  substantially.
- **Location is a single point fix**, not verified against a geofence — there is no check
  that a staff member is actually on site.
- Dummy auth with a shared staff password; no password hashing, no lockout, no sessions
  beyond a `SharedPreferences` flag.

## Third-party model attribution

`app/src/main/assets/facenet.tflite` is the FaceNet model bundled by
[`shubham0204/FaceRecognition_With_FaceNet_Android`](https://github.com/shubham0204/FaceRecognition_With_FaceNet_Android)
(reference implementation Apache-2.0). Input 160×160×3, output 128-d, with per-image
standardisation — the model was trained that way, and fixed `(x-127.5)/128` normalisation
produces unusable embeddings.

It is included **for demo and educational purposes**. The model weights' original training
lineage traces to research code; it has not been licence-audited for commercial use and
carries no accuracy guarantee.
