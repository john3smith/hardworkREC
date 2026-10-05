# hardworkREC

운동명과 운동기록(횟수·분:초)을 저장하고, 달력에서 운동한 날을 확인하는 Android 앱입니다.

## 기능

- **오늘**: 운동명 추가, 오늘의 기록과 합계 확인, 운동 기록 시작
- **기록**: 운동명 선택, 숫자 키패드로 횟수 입력, 시간 한 칸 연속 입력 (`125` → `1:25`, `1234` → `12:34`)
- **카운트**: 화면 중앙 원형 버튼을 누를 때마다 1 증가. `−1`과 `횟수 적용` 제공
- **달력**: 운동한 날짜에 점 표시. 날짜를 누르면 해당 날짜의 기록 표시
- **백업**: Android 파일 선택기로 JSON 백업 저장·가져오기. Google Drive에 보관하려면 저장 위치에서 Drive를 선택해야 합니다. 가져오기는 기존 기록을 보존하고 UUID 기준으로 중복을 건너뜁니다.

기록은 기기의 SQLite 데이터베이스에 저장됩니다. Android 시스템의 앱 데이터 자동 백업도 허용하지만, 자동 백업의 실행 시점·복원은 기기와 Google 계정 설정에 따릅니다. 앱의 백업 화면은 수동 백업입니다. **APK 자체는 Google Drive에 업로드하지 않습니다.**

## 빌드

JDK 17과 Android SDK 36이 필요합니다. `ANDROID_HOME`을 SDK 디렉터리로 설정한 다음:

```powershell
.\gradlew.bat --no-daemon lintDebug testDebugUnitTest assembleDebug
```

디버그 APK: `app/build/outputs/apk/debug/app-debug.apk`.

## 백업 형식

UTF-8 JSON `schemaVersion: 1`. 운동명과 기록 UUID·횟수·시간(초)·기록시각을 포함합니다. 가져오기 전 형식과 값을 검증하며 기존 데이터는 삭제하지 않습니다. 백업 파일은 암호화되지 않으므로 공유 위치는 사용자가 신중히 선택해야 합니다.
