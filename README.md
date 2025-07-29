# Farmer Weather App ☀️

공공데이터 날씨 API를 사용한 안드로이드 앱입니다.  
현재 위치의 날씨를 보여줍니다.

## 주요 기능
- 현재 위치 기반 날씨 표시
- 다크모드 지원
- 강수량 표시

## 확인 방법
https://play.google.com/store/apps/details?id=com.farmer.weather

## 기술 스택
- Kotlin
- Jetpack Compose
- MVVM + ViewModel
- OpenWeather API

## 스크린샷
(화면 스크린샷)

## 주요 문제 해결
- ViewModel에서 상태 흐름을 안전하게 제어하지 못해 API 실패 시 앱이 크래시되는 문제 발견
- 원인 및 해결: state를 여러 함수에서 설정하여 흐름을 보기 어려웠고 로직을 실패했음에도 다음 프로세스가 실행되어 크래시되는 문제였다. 서로 분리했던 상태제어를 한 곳에 합치고, 로직 도중에 실패하면 진행 못하도록 변경했다.

## Assets Attribution
- 날씨 아이콘: [미리캔버스](https://www.miricanvas.com/) AI generated
- error icon: [Flaticon](https://www.flaticon.com/free-icons/cross)
- nodata icon: [Flaticon](https://www.flaticon.com/free-icons/cancel)

## 개선 계획
- 지역 검색 및 즐겨찾기