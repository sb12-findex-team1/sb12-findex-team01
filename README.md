# Team01

## 팀원 구성
<table  width="100%">
  <tr>
    <td  align="center">
      <img  src="https://avatars.githubusercontent.com/u/114418850?v=4"  width="100px;"  alt=""/>
    </td>
    <td  align="center">
      <img  src="https://avatars.githubusercontent.com/u/266769032?v=4"  width="100px;"  alt=""/>
    </td>
    <td  align="center">
      <img  src="https://avatars.githubusercontent.com/u/37824338?v=4"  width="100px;"  alt=""/>
    </td>
    <td  align="center">
      <img  src="https://avatars.githubusercontent.com/u/266508451?v=4"  width="100px;"  alt=""/>
    </td>
    <td  align="center">
      <img  src="https://avatars.githubusercontent.com/u/83206642?v=4"  width="100px;"  alt=""/>
    </td>
    <td  align="center">
      <img  src="https://avatars.githubusercontent.com/u/98449664?v=4"  width="100px;"  alt=""/>
    </td>
  </tr>
  <tr>
    <td align="center">
        <a href="https://github.com/99hyeon">
          <div>박서현(팀장)</div>
        </a>
    </td>
    <td align="center">
        <a href="https://github.com/lyoonat">
          <div>류승지</div>
        </a>
    </td>
    <td align="center">
        <a href="https://github.com/kyhun1007">
          <div>이경훈</div>
        </a>
    </td>
    <td align="center">
        <a href="https://github.com/yesungyoo">
          <div>이예성</div>
        </a>
    </td>
    <td align="center">
        <a href="https://github.com/jeon-minji">
          <div>전민지</div>
        </a>
    </td>
    <td align="center">
        <a href="https://github.com/Hize18">
          <div>전태훈</div>
        </a>
    </td>
  </tr>
</table>

## 프로젝트 소개
- 공공데이터 Open API 기반 금융 지수 데이터 관리 및 자동 연동 Spring Boot 백엔드 시스템 구축
- 프로젝트 기간: 2026.05.14 ~ 2026.05.22

## 기술 스택
- Framework: Spring Boot
- Database & ORM: PostgreSQL, Spring Data JPA, QueryDSL
- Scheduling: Spring Scheduler
- External API: 공공데이터포털 Open API
- 배포: Railway.io
- 협업: Git/Github, Notion, Discord

## 팀원별 구현 기능
**류승지**
- 지수 데이터 CRUD
- 날짜 범위 조회
- 정렬/페이지네이션
- CSV 다운로드

**박서현**
- open api 자동 연결 활성화/비활성화
- Scheduler 배치

**이경훈**
- 차트 데이터 생성
- 이동 평균, 랭킹, 성과 계산
- Railway로 배포

**이예성**
- 지수 정보 CRUD
- 검색/정렬/커서 페이지네이션
- 지수 정보 favorite 처리

**전민지**
- SyncJob 저장/조회
- 연동 성공/실패 기록
- GlobalExceptionHandler, ErrorResponse 관리

**전태훈**
- 기초 엔티티, 스키마 작업
- 외부 api 호출
- 지수 정보 자동 생성, 저장

## 파일 구조
```text
📦 src
 ┣ 📂main
 ┃ ┣ 📂java/com/codeit/findex
 ┃ ┃ ┣ 📂client
 ┃ ┃ ┃ ┣ IndexApiClient.java
 ┃ ┃ ┃ ┗ IndexApiProperties.java
 ┃ ┃ ┣ 📂config
 ┃ ┃ ┃ ┣ IndexApiConfig.java
 ┃ ┃ ┃ ┗ QueryDslConfig.java
 ┃ ┃ ┣ 📂controller
 ┃ ┃ ┃ ┣ AutoSyncController.java
 ┃ ┃ ┃ ┣ IndexDataController.java
 ┃ ┃ ┃ ┣ IndexInfoController.java
 ┃ ┃ ┃ ┗ SyncJobController.java
 ┃ ┃ ┣ 📂csv
 ┃ ┃ ┃ ┗ IndexDataCsvExporter.java
 ┃ ┃ ┣ 📂dto
 ┃ ┃ ┃ ┣ 📂autosync
 ┃ ┃ ┃ ┣ 📂client
 ┃ ┃ ┃ ┣ 📂indexData
 ┃ ┃ ┃ ┣ 📂indexinfo
 ┃ ┃ ┃ ┣ 📂syncjob
 ┃ ┃ ┃ ┗ ErrorResponse.java
 ┃ ┃ ┣ 📂entity
 ┃ ┃ ┃ ┣ 📂base
 ┃ ┃ ┃ ┃ ┗ BaseEntity.java 
 ┃ ┃ ┃ ┣ AutoSync.java
 ┃ ┃ ┃ ┣ IndexData.java
 ┃ ┃ ┃ ┣ IndexInfo.java
 ┃ ┃ ┃ ┣ JobType.java
 ┃ ┃ ┃ ┣ PeriodType.java
 ┃ ┃ ┃ ┣ Result.java
 ┃ ┃ ┃ ┣ SourceType.java
 ┃ ┃ ┃ ┗ SyncJob.java
 ┃ ┃ ┣ 📂exception
 ┃ ┃ ┃ ┣ DuplicateException.java
 ┃ ┃ ┃ ┗ GlobalExceptionHandler.java
 ┃ ┃ ┣ 📂repository
 ┃ ┃ ┃ ┣ 📂querydsl
 ┃ ┃ ┃ ┃ ┣ IndexInfoQueryRepository.java
 ┃ ┃ ┃ ┃ ┗ IndexInfoQueryRepositoryImpl.java
 ┃ ┃ ┃ ┣ AutoSyncRepository.java
 ┃ ┃ ┃ ┣ IndexDataRepository.java
 ┃ ┃ ┃ ┣ IndexDataRepositoryCustom.java
 ┃ ┃ ┃ ┣ IndexDataRepositoryImpl.java
 ┃ ┃ ┃ ┣ IndexInfoRepository.java
 ┃ ┃ ┃ ┗ SyncJobRepository.java
 ┃ ┃ ┣ 📂scheduler
 ┃ ┃ ┃ ┗ AutoSyncScheduler.java
 ┃ ┃ ┣ 📂service
 ┃ ┃ ┃ ┣ 📂impl
 ┃ ┃ ┃ ┃ ┣ AutoIndexDataSyncServiceImpl.java
 ┃ ┃ ┃ ┃ ┣ AutoSyncServiceImpl.java
 ┃ ┃ ┃ ┃ ┣ ClientIndexSyncServiceImpl.java
 ┃ ┃ ┃ ┃ ┣ IndexDataServiceImpl.java
 ┃ ┃ ┃ ┃ ┣ IndexInfoServiceImpl.java
 ┃ ┃ ┃ ┃ ┗ SyncJobServiceImpl.java
 ┃ ┃ ┃ ┣ AutoIndexDataSyncService.java
 ┃ ┃ ┃ ┣ AutoSyncService.java
 ┃ ┃ ┃ ┣ ClientIndexSyncService.java
 ┃ ┃ ┃ ┣ IndexDataService.java
 ┃ ┃ ┃ ┣ IndexInfoService.java
 ┃ ┃ ┃ ┗ SyncJobService.java
 ┃ ┃ ┗ Sb12FindexTeam1Application.java
 ┃ ┃  
 ┃ ┣ 📂resources
 ┃ ┃ ┣ 📂static
 ┃ ┃ ┣ 📜application-dev.yaml
 ┃ ┃ ┣ 📜application-prod.yaml
 ┃ ┃ ┣ 📜application.yaml
 ┃ ┃ ┣ 📜init.sql
 ┃ ┃ ┗ 📜schema.sql
 ┣ Application.java
 ┗.gitignore

```

## 구현 홈페이지
배포 url : https://sb12-findex-team01-production.up.railway.app/

## 프로젝트 회고
(추후 추가 예정)
