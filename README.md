# FallMon : 낙상 감지 및 낙상 종류 판별 애플리케이션 제작

application_base가 디폴트 브랜치입니다.

#### 논의가 필요한 사항에 대해서는 Issues를 확인해주시기 바랍니다.

<br>

## 파일 설명 요약

- **.idea**: **(주의)** 로컬 IDE 세팅용 파일인데, commit/merge 시에 잘못 포함되었지만 대체로 문제는 없습니다. 문제 발생시 로컬에서 .idea를 지우고 IDE를 재시작해보시기 바랍니다.
- **app**: 애플리케이션을 구성하는 모든 코드가 들어간 폴더입니다. 애플리케이션 개발은 이 폴더 내에서 모두 이루어집니다.
- **porter**: Python 머신러닝 모델을 Java/C로 변환해주는 포팅툴을 담은 폴더입니다.
- **FallMon-debug.apk**: FallMon 애플리케이션을 쉽게 설치하기 위해 빌드된 apk 파일(debug)입니다. 설치하려는 기기에 옮긴 후, apk 파일을 실행하여 설치해주시면 됩니다.
- **기타 파일**: 직접 수정할 필요없는 파일들입니다.

<br>

## 리포지토리 클론

```bash
$ git clone https://github.com/snuhcs/FallMon.git
```

<br>

## app 폴더 구성

폴더는 **진한 글씨**로 되어 있습니다.

**app**<br>
┣ **src**<br>
┃┗ **main**<br>
┃ ┣ **java**<br>
┃ ┃ ┗ **com**<br>
┃ ┃  ┗ **example**<br>
┃ ┃   ┗ **fallmon**<br>
┃ ┃    ┗ **presentation**<br>
┃ ┃     ┣ **math**<br>
┃ ┃     ┃┗ FallMonMath.kt : 센서데이터를 전처리하기 위한 함수를 모아둔 오브젝트입니다.<br>
┃ ┃     ┃<br>
┃ ┃     ┣ **retrofit**<br>
┃ ┃     ┃┣ **dto** : 서버와 통신을 위해 Json 형식을 Kotlin으로 변환하기 위한 데이터클래스 폴더입니다.<br>
┃ ┃     ┃┗ FallMonService.kt : 서버에 POST/GET을 요청하는 데에 사용되는 함수의 인터페이스입니다.<br>
┃ ┃     ┃<br>
┃ ┃     ┣ **theme** : 애플리케이션의 theme을 저장한 폴더입니다. 기본값 그대로인 상태입니다.<br>
┃ ┃     ┃<br>
┃ ┃     ┣ ClassificationModel.java : 낙상 종류 분류 모델입니다.<br>
┃ ┃     ┣ ConfirmedActivity.kt : 서버로의 낙상 기록 전송 확인을 위한 액티비티입니다.<br>
┃ ┃     ┣ DataClasses.kt : 낙상 종류 데이터클래스입니다.<br>
┃ ┃     ┣ DetectedActivity.kt : 낙상 감지 확인 및 기록 전송을 위한 액티비티입니다.<br>
┃ ┃     ┣ FallDetectionService.kt : 센서 데이터 수집~모델 실행까지 진행하는 낙상 감지 서비스입니다.<br>
┃ ┃     ┣ Features.kt : 처리된 데이터를 저장하기 위한 오브젝트입니다.<br>
┃ ┃     ┣ HistoryActivity.kt : 최근 낙상 기록을 조회할 수 있는 액티비티입니다.<br>
┃ ┃     ┣ MainActivity.kt : 메인 화면 액티비티입니다. 낙상 감지 on/off, 설정, 기록 버튼이 있습니다.<br>
┃ ┃     ┣ Model.java : 낙상 감지 모델입니다.<br>
┃ ┃     ┣ RetrofitClient.kt : 서버와 연결하기 위한 오브젝트입니다.<br>
┃ ┃     ┗ SettingActivity.kt : 애플리케이션의 설정을 변경하기 위한 액티비티입니다.<br>
┃ ┣ **res**<br>
┃ ┃ ┣ **layout** : 액티비티의 레이아웃 xml 파일을 저장해둔 폴더입니다.<br>
┃ ┃ ┣ **mipmap-...** : 애플리케이션 아이콘을 저장해둔 폴더입니다.<br>
┃ ┃ ┗ **drawable-...** : 이미지버튼의 백터 이미지 파일을 저장해둔 폴더입니다.<br>
┃ ┃<br>
┃ ┣ AndroidManifest.xml : manifest 파일입니다. 액티비티, 서비스는 여기에 기입해야 작동합니다.<br>
┃ ┗ ic_launcher-playstore.png<br>
┃ <br>
┗ build.gradle.kts : 앱 빌드한 gradle 파일입니다. 사용할 리포지토리/패키지 등을 implement합니다. <br>
<br>
각 파일별 자세한 설명은 각 파일 코드 내의 주석을 참고해주시기 바랍니다.
<br>

## porter

Python 머신러닝 모델을 Java/C로 변환해주는 포팅툴을 담은 폴더입니다.

자세한 내용은 porter 폴더 내의 README.md를 참고해주시기 바랍니다.

<br>

## FallMon-debug.apk

FallMon 애플리케이션을 쉽게 설치하기 위해 빌드된 apk 파일(debug)입니다. 

설치하려는 기기에 옮긴 후, apk 파일을 실행하여 설치해주시면 됩니다.

스마트워치에 최적화되어있기 때문에, 스마트폰 환경에서 사용시 UI의 형태가 이상하거나 낙상 감지가 제대로 되지 않을 수 있습니다.

<br>

## Android Studio 에서 앱을 run할 수가 없어요!

로컬에서 프로젝트를 삭제하고 나서, 다시 리포지토리를 clone해주신 후에

Android Studio를 재시작해주시면 대부분의 상황에서 IDE가 app을 인식하여 실행할 수 있을 것입니다.

<br>

## Application User Manual

### Application
![image](https://github.com/snuhcs/FallMon/assets/39697564/edb1f888-10f6-469b-a836-7c2eaa5faf9b)
![image](https://github.com/snuhcs/FallMon/assets/39697564/07915bac-e412-4f7f-83a3-f12d285e4d01)
![image](https://github.com/snuhcs/FallMon/assets/39697564/00c9d3e8-4814-4fe7-a9b6-aae5c7bcdbf5)
![image](https://github.com/snuhcs/FallMon/assets/39697564/fd7df50c-1db4-4d13-9b02-df5a5d789dd0)
![image](https://github.com/snuhcs/FallMon/assets/39697564/43f44a75-2ca5-4302-af8a-7118a8ddb1cd)
![image](https://github.com/snuhcs/FallMon/assets/39697564/66f9d8bd-97c4-4c95-904e-99860654e1e6)

### Server
![image](https://github.com/snuhcs/FallMon/assets/39697564/fb674fd0-f3bd-4431-b945-d11a469f3895)
![image](https://github.com/snuhcs/FallMon/assets/39697564/d14261b2-86ab-40d5-9f8a-0d5bddbcafbb)


