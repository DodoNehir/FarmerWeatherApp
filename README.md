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
- ViewModel에서 많은 역할을 한 곳에 몰아서 처리하고, 상태 흐름을 안전하게 제어하지 못해 API 실패 시 앱이 크래시되는 문제 발견 

## Assets Attribution
- 날씨 아이콘: [미리캔버스](https://www.miricanvas.com/) AI generated
- error icon: [Flaticon](https://www.flaticon.com/free-icons/cross)
- nodata icon: [Flaticon](https://www.flaticon.com/free-icons/cancel)

## 개선 계획
- 지역 검색 및 즐겨찾기