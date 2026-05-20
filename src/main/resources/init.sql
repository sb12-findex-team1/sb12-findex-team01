-- 데이터베이스 생성
CREATE DATABASE findex;

-- 사용자 생성
CREATE USER findex_user WITH PASSWORD 'findex1234';

-- 데이터베이스 권한 부여
GRANT ALL PRIVILEGES ON DATABASE findex TO findex_user;

-- findex DB로 접속 후 실행
\c findex

-- public 스키마 권한 부여
GRANT ALL ON SCHEMA public TO findex_user;

-- 스키마 소유자 변경 (선택이지만 보통 같이 함)
ALTER SCHEMA public OWNER TO findex_user;

-- 앞으로 생성되는 테이블/시퀀스 권한 자동 부여
ALTER DEFAULT PRIVILEGES IN SCHEMA public
	GRANT ALL ON TABLES TO findex_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
	GRANT ALL ON SEQUENCES TO findex_user;