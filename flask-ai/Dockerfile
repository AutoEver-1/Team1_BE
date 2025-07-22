# Alpine 기반 Python 3.11 경량 이미지 사용
FROM python:3.11-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 환경변수 설정
ENV JAVA_HOME=/usr/lib/jvm/default-jdk

# Alpine 패키지 업데이트 및 필수 패키지 설치
RUN apk update && \
    apk add --no-cache \
    openjdk11-jre \
    g++ \
    gcc \
    musl-dev \
    curl \
    && rm -rf /var/cache/apk/*

# pip 업그레이드
RUN pip install --upgrade pip

# requirements.txt 복사 및 의존성 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 애플리케이션 코드 복사
COPY . .

# 포트 노출
EXPOSE 5050

# 헬스체크
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:5050/ || exit 1

# Flask 애플리케이션 실행
CMD ["python", "app.py"] 