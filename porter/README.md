# porter

안드로이드 포팅을 위해 scikit-learn 모델을 자바 또는 C 네이티브 언어로 변환하는 툴입니다.

## 파일 설명

- **port.py**: 포팅을 하는 파이썬 스크립트
- **test_model.pkl**: 학습이 완료된 테스트용 `RandomForest` 모델 객체를 파이썬 `pickle`을 사용하여 시리얼화한 파일이며, 모델에 대한 정보는 추후에 노션에 따로 올리겠습니다.
- **requirements.txt**: 가상환경 구성 시 필요한 파일

## 사용법

### 가상환경 구성

파이썬 가상환경을 만듭니다.

```bash
$ cd ./porter
$ python -m venv .venv
```

가상환경을 활성화합니다. 이 과정이 완료되면 프롬프트 옆에 `(.venv)`라는 문자열이 나타나야 합니다.

```bash
$ . ./.venv/bin/activate
(.venv) $
```

필요한 모듈들을 설치합니다.

```bash
(.venv) $ pip install -r requirements.txt
```

### 실행 예

Java 파일 생성

```bash
python port.py java -o Model.java -i model.pkl
python port.py java -o ClassificationModel.java -i classification_model.pkl
```

C 파일 생성

```bash
python port.py c -o model.c -i model.pkl
python port.py c -o ClassificationModel.c -i calssification_model.pkl
```

### Input file 설정

-i 옵션을 이용해 `.pkl`파일의 경로를 전달합니다. 
`.pkl` 파일에는 `scikit-learn.ensemble.RandomForest` 객체가 저장되어 있어야 합니다.

## 기타 사항

Random forest 특성상 코드의 길이가 매우 길어질 수 있기 때문에, 성능이 저조하면 추후에 최적화 작업을 진행할 때 Java나 Kotlin보다는 C로 변환한 다음 dynamic linking을 하는 방법을 생각해볼 수 있겠습니다.

Java로 포팅하는 경우, MainActivity.kt이 위치한 디렉토리에도 포팅된 파일을 저장합니다.