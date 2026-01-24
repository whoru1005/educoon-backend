# edu-CooN

> AI 기반 개인 학습 보조 및 실시간 스터디 그룹 플랫폼
> 

---

## 프로젝트 개요

EduCoon은 학습자가 PDF 자료 업로드하면 AI가 핵심 내용을 요약하고 퀴즈를 생성해주며, AI 추천 스터디룸 등에서 실시간 스터디룸을 통해 동료들과 함께 공부할 수 있는 환경을 제공하는 플랫폼입니다.

## 앱소개 영상

https://drive.google.com/file/d/16uxUsTWVM8PEIUCQZEp3SouT8ICXLbdS/view

## Tech Stack

- **Framework**: Java 17, Spring Boot 3.4.1
- **Database**: MySQL 8.0, Redis
- **Infrastructure**: AWS (EC2, ALB), Docker, Docker Compose
- **AI Integration**: Google Gemini AI API
- **Communication**: Spring Security, OAuth2 (Kakao), WebSocket (STOMP)

## System Architecture

본 프로젝트는 서비스의 안정성과 보안을 위해 다음과 같은 계층형 인프라 구조를 채택했습니다.

![아키텍쳐.png](attachment:14261526-37c2-461f-989a-03f2e99f930d:아키텍쳐.png)

```
•ALB(Application Load Balancer) 도입을 통한 단일 진입점 확보 및 인프라 유연성 강화
•보안 그룹(Security Group) 계층화: 서버(EC2)를 외부 인터넷으로부터 격리하고 오직 ALB의 요청만 수신하도록 설계
```

## 트러블 슈팅 과정

### 1. 스터디룸 입장 동시성 제어 (Concurrency Control)

- **문제 상황**
    - **상황:** 정원이 1명 남은 인기 스터디룸에 다수의 사용자가 동시에 '입장' 버튼을 클릭하는 상황 발생.
    - **원인:** `countBy...`를 통해 현재 인원을 조회하는 시점과 데이터를 업데이트하는 시점 사이의 차이가 존재함. 여러 스레드가 동시에 아직 자리가 있다고 판단하여 입장 로직을 통과하는 **레이스 컨디션(Race Condition)** 발생.
    - **결과:** 설정된 `maxCapacity`를 초과하여 사용자가 입장하게 되며, 서비스의 신뢰도와 데이터 정합성이 깨지는 문제 식별.
- **해결 방법**
    - JPA의 비관적 락(Pessimistic Lock)을 도입하여, 특정 사용자가 해당 스터디룸 정보를 조회하는 시점부터 트랜잭션이 완료될 때까지 다른 사용자의 접근(수정/조회)을 데이터베이스 수준에서 차단함.
    - 데이터 정합성이 비즈니스 로직의 핵심이므로, 충돌 발생 시 재시도 로직을 별도로 구현해야 하는 낙관적 락보다 확실한 제어가 가능한 방식을 선택함.

```java
// StudyRoomRepository.java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM StudyRoom s WHERE s.id = :id")
Optional<StudyRoom> findByIdWithLock(@Param("id") Long id);
```

- **실제 실행되는 SQL**
    
    ```sql
    `SELECT 
        s.id, s.current_count, s.max_capacity 
    FROM 
        study_room s 
    WHERE 
        s.id = ? 
    FOR UPDATE; -- 해당 로우에 쓰기 잠금을 획득하여 다른 트랜잭션의 접근을 대기
    ```
    
- **분석 및 검증**
    - **검증 방식:** `ExecutorService`와 `CountDownLatch`를 활용하여 100개의 스레드가 동시에 입장 로직을 호출하는 멀티스레드 테스트 환경 구축.
    - **결과:** 락 적용 전에는 정원을 초과하여 100건 중  10건이 성공했으나, 적용 후에는 **정확히 남은 정원만큼만 성공**하고 나머지는 `CustomException`을 반환하며 순차적으로 처리됨을 확인.

![동시성 테스트.png](attachment:21e28a4a-0f54-4d09-a629-47c21253bec4:동시성_테스트.png)

- **성능 고려:** 비관적 락은 DB 커넥션을 점유하는 시간이 길어질 수 있으므로, 입장 로직 내의 외부 API 호출 등을 트랜잭션 외부로 분리하여 락 점유 시간을 최소화함.
- **최적화 및 고도화**
    - **실무 최적화:** 현재는 DB 수준의 락을 사용하지만, 사용자 수가 폭발적으로 증가할 경우 DB 커넥션 풀 고갈 문제가 발생할 수 있음.
    - **인덱스 활용:** `FOR UPDATE` 쿼리가 테이블 전체에 락을 걸지 않도록, 조건절인 `id` 컬럼에 인덱스가 적절히 설정되어 있는지 확인하여 레코드 단위의 락(Row-level Lock)이 유지되도록 함.
    - **고도화 방향:** 추후 분산 환경(다중 서버)으로 확장될 경우를 대비하여, Redis의 `Redisson` 라이브러리를 활용한 **분산 락(Distributed Lock)** 도입을 검토하거나 이를 통해 DB 부하를 줄이고 애플리케이션 레벨에서 더 효율적인 동시성 제어가 가능

![image.png](attachment:6b983690-a6e4-419f-bc48-e8f55b7cadb4:image.png)

### 2. 인프라 보안 계층화: 로드 밸런서와 보안 그룹 체이닝

- 문제 상황
    - **상황:** 초기 개발 편의를 위해 애플리케이션 서버(EC2)의 8080 포트가 `0.0.0.0/0` (전체 개방)으로 설정되어 공인 IP만 알면 누구나 직접 접속이 가능한 상태.
    - **원인:** 로드 밸런서를 배치하지 않고 EC2 자체의 방화벽(보안 그룹)이 열려있어 트래픽이 IP만 알면 직접 서버에 도달할 수 있음.
    - **결과:** 서버의 공인 IP가 포트 스캐닝에 노출되어 무차별 대입 공격(Brute Force)이나 DDoS 공격의 타겟이 될 수 있으며, 앞단의 보안 장치가 무력화되는 보안 취약점 식별.
- 해결 방법
    - **보안 그룹 체이닝(Security Group Chaining)** 기술을 도입하여, 네트워크 접근 제어를 계층화함.
    - IP 주소 기반의 차단 방식은 ALB의 유동적인 IP 변경에 대응하기 어려우므로, **'ALB의 보안 그룹 ID'** 자체를 EC2 보안 그룹의 인바운드 소스(Source)로 지정하는 방식을 선택.
    - 이를 통해 오직 "ALB를 통과한 검증된 트래픽"만 애플리케이션 서버에 도달하도록 강제함.
- 실제 적용된 설정

![image.png](attachment:7d29f116-4fd1-4c9f-929f-e897d3d2d96c:image.png)

- 분석 및 검증
    - **검증 방식:** 네트워크 격리 여부를 확인하기 위해 두 가지 경로로 접근 테스트 수행 (Browser & Postman).
        1. **공격자 시나리오:** EC2 Public IP(`13.124.xx.xx:8081`)로 직접 요청 시도.
        2. **정상 사용자 시나리오:** ALB DNS 도메인(`my-alb-123...amazonaws.com`)으로 요청 시도.
    - **결과:**
        - **Direct IP:** 연결 시간 초과(Timeout) 발생하며 접속 불가 확인. (방화벽 차단 성공)
        - **ALB DNS:** `200 OK` 정상 응답 확인.
    - **보안 효과:** 공격 표면(Attack Surface)을 단일 진입점(ALB)으로 축소시켜, 서버의 실제 IP가 노출되더라도 포트 레벨에서의 무단 접근을 원천 차단함.
- 최적화 및 고도화
    - **실무 최적화:** IP CIDR 블록으로 관리할 경우 ALB 스케일링 시 IP 변경에 대응하기 위해 주기적인 관리가 필요하지만, **보안 그룹 ID 참조 방식**을 통해 관리 포인트 없이 동적 환경에 유연하게 대응 가능
    - **구조적 개선:** 현재는 EC2가 Public Subnet에 위치하여 보안 그룹으로만 막고 있으나, 이는 완벽한 격리는 아님.
    - **고도화 방향:** 추후 **EC2를 외부 인터넷과 물리적으로 단절된 Private Subnet으로 이전**하고, NAT Gateway를 통해서만 아웃바운드 통신을 하도록 3-Tier Architecture (Web-WAS-DB)로 인프라를 재구성하여 보안 수준을 엔터프라이즈 급으로 강화할 가능

### 3. AI 연동: 타임아웃 처리 및 Spring AI 도입 (AI Integration)

- 문제 상황
    - **상황:** 생성형 AI(Gemini)를 활용한 텍스트 요약 및 퀴즈 생성 기능 구현 중 두 가지 주요 문제 직면.
    - **원인:**
        1. **응답 지연 및 불안정성:** AI 모델의 응답 속도가 불규칙하여(최대 30초 이상), 클라이언트가 무한 대기하거나 연결이 끊기는 문제 발생.
        2. **비정형 데이터 응답:** AI에게 JSON 형식을 요청해도, 마크다운 코드 블럭(````json ... ````)을 포함하거나 불필요한 서술어를 섞어서 반환하여 파싱 에러(`JsonParseException`)가 빈번함.
        3. **유지보수 복잡성:** `GeminiApiService`를 직접 구현하면서 HTTP 요청/응답 처리, 프롬프트 관리, JSON 파싱 로직 증가하여 코드가 복잡해짐.
- 해결 방법
    - **Resilience(탄력성) 확보:** `WebClient`와 `Project Reactor`의 `timeout()` 연산자를 사용하여, 설정된 시간(30초) 내 응답이 없으면 즉시 예외 처리하고 `onErrorResume`으로 사용자에게 안내 메시지를 반환하도록 안전 장치 마련.
    - **데이터 전처리(Preprocessing):** `GeminiApiService`에 정규표현식을 이용한 `cleanJson` 메서드를 구현하여, AI 응답에서 순수한 JSON 문자열만 추출하는 로직 적용.
    - **Spring AI 도입 (Architecture Upgrade):**
        - 단순 HTTP 호출 방식을 **Spring AI (`ChatClient`)** 프레임워크로 고도화.
        - *`PromptTemplate`*을 사용하여 프롬프트 관리를 체계화하고, **`BeanOutputConverter`*를 통해 AI 응답을 자바 객체(`QuizQuestionDto`)로 자동 매핑하여 파싱 로직을 간소화함.
- 실제 적용된 코드
    
    **1. 안정성 확보: 타임아웃 및 에러 핸들링 (Reactive Stream)**
    
    ```sql
    // AiController.java
    @PostMapping("/chat")
    public Mono<ResponseEntity<Map<String, String>>> chat(@RequestBody ChatRequest request){
        return aiService.chat(request.message())
                .map(response -> ResponseEntity.ok(Map.of("response", response)))
                .timeout(Duration.ofSeconds(30)) // 30초 타임아웃 강제 설정
                .onErrorResume(e -> {
                    log.error("AI 응답 시간 초과 또는 오류 발생", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "AI가 잠시 생각을 정리 중입니다. 다시 시도해주세요.")));
                });
    }
    ```
    
    **2. Spring AI 도입: 객체 자동 매핑 (BeanOutputConverter)**
    
    ```sql
    // AiService.java : Spring AI 도입으로 파싱 로직 제거
    public Mono<List<QuizQuestionDto>> quizText(String text, QuestionType quizType) {
        // 1. 응답받을 타입 정의 (List<QuizQuestionDto>)
        BeanOutputConverter<List<QuizQuestionDto>> converter = 
                new BeanOutputConverter<>(new ParameterizedTypeReference<>() {});
    
        // 2. 프롬프트 템플릿 생성
        String instructions = """
                다음 텍스트를 기반으로 {typeDescription} 유형의 퀴즈 5개를 생성해줘.
                JSON 포맷으로 응답해줘.
                """;
                
        // 3. ChatClient 호출 및 자동 변환
        return Mono.fromCallable(() -> {
            PromptTemplate promptTemplate = new PromptTemplate(instructions);
            Prompt prompt = promptTemplate.create(Map.of("typeDescription", quizType, "text", text));
            
            // .entity(converter)를 통해 AI 응답을 즉시 Java 객체로 변환
            return chatClient.prompt(prompt).call().entity(converter);
        });
    }
    ```
    

- 분석 및 검증
    - **검증 방식:**
        1. **타임아웃 테스트:** AI 응답 지연 상황을 모킹(Mocking)하여 30초 경과 시 `TimeoutException`이 발생하고, `onErrorResume`이 정상적으로 친절한 에러 메시지를 반환하는지 확인.
        2. **Spring AI 변환 테스트:** 기존 `cleanJson` 로직 없이 `BeanOutputConverter`만으로 마크다운이 포함된 응답을 DTO로 정상 변환하는지 단위 테스트 수행.
    - **결과:**
        - 타임아웃 정책 적용 후, 서버 스레드 점유(Blocking) 문제가 해결되어 전체 시스템의 안정성 확보.
        - Spring AI 도입으로 `GeminiApiService` 대비 **코드 라인 수 약 40% 감소** 및 프롬프트 관리의 유연성 증대.

![diagram-export-2026.-1.-21.-오전-10_26_17.png](attachment:ccba0134-12c2-4ca2-a3d5-60e11440e228:diagram-export-2026.-1.-21.-오전-10_26_17.png)

### 4. Redis를 활용한 실시간 세션 동기화 및 확장성 확보

- 문제 상황
    - **상황:** 초기에는 개발을 위해 자바의 `ConcurrentHashMap`을 사용하여 실시간 참여자 상태와 세션 매핑 정보를 애플리케이션 메모리에서 관리함.
    - **한계 (Limitations)**
        1. **서버 확장 불가 (Scale-out Issue):** 로컬 메모리는 서버 간 공유되지 않으므로, 추후 트래픽 증가로 서버를 증설할 경우 **서로 다른 서버에 접속한 유저끼리는 실시간 상태를 확인할 수 없는 고립 문제** 발생.
        2. **데이터 휘발성:** 배포를 위해 서버를 재시작할 때마다 메모리에 있던 모든 참여자 정보가 초기화되어, 사용자가 강제 퇴장 처리되거나 유령 유저 데이터가 남는 등 운영상의 불안정성 식별.
        3. **메모리 관리 부담:** 동시 접속자가 급증할 경우 JVM 메모리 사용량이 예측 불가능하게 증가하여 `OutOfMemoryError`의 잠재적 위험 존재.
- 해결 방법
    - **인메모리 저장소 외부화 (Redis 도입):** 애플리케이션의 상태(State)를 로컬 메모리에서 외부의 Redis로 이관하여 **무상태 아키텍처**를 구현.
    - **데이터 구조 설계:**
        - **`Hash` (방 관리):** `room:{roomId}` 키에 `{userId: UserInfo}`를 저장하여, 어떤 서버에서 요청이 오더라도 동일한 참여자 목록을 조회할 수 있도록 동기화.
        - **`Value` (세션 매핑):** `session:{sessionId}` 키에 유저 정보를 매핑하여, WebSocket 연결 해제 시 서버가 즉시 유저를 식별할 수 있도록 역인덱싱(Reverse Indexing) 구현.
- 실제 적용된 코드
    
    **1. Redis 기반 세션 레지스트리** 
    
    ```sql
    // 기존 ConcurrentHashMap을 RedisTemplate으로 대체하여 상태 외부화
    public void userJoin(Long roomId, UserStatus userStatus) {
        // 서버가 재시작되어도 Redis에 데이터가 남아있으므로 상태 유지 가능
        redisTemplate.opsForHash().put("room:" + roomId, userStatus.getUserId().toString(), userStatus);
    }
    
    public void registerSession(String sessionId, UserLocation location) {
        // 세션 ID로 유저 정보를 찾을 수 있도록 매핑 (Disconnect 핸들링용)
        // 24시간 TTL을 설정하여 비정상 종료된 세션 데이터의 자동 정리(Garbage Collection) 위임
        redisTemplate.opsForValue().set("session:" + sessionId, location, Duration.ofHours(24));
    }
    ```
    
    **2. 연결 해제 및 퇴장 처리 (WebSocketEventListener.java)**
    
    ```sql
    @EventListener
    public void handleWebSocketDisConnectListener(SessionDisconnectEvent event) {
        String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
    
        // Redis에서 세션 정보 조회 (어떤 서버 인스턴스에서 조회를 요청해도 동일한 결과 보장)
        Optional<UserLocation> locationOpt = sessionRoomRegistry.unregisterSession(sessionId);
    
        if (locationOpt.isPresent()) {
            sessionRoomRegistry.userLeave(locationOpt.get().getRoomId(), locationOpt.get().getUserId());
            // 퇴장 메시지 전송 로직...
        }
    }
    ```
    
- 분석 및 검증
    - **검증 방식:**
        1. **서버 재시작 테스트:** 유저가 스터디룸에 입장해 있는 상태에서 **Spring Boot 애플리케이션을 강제로 재시작**한 후, 다시 접속했을 때 Redis에 저장된 참여자 목록이 유지되는지 확인.
        2. **지연 시간 비교:** 로컬 메모리(ns 단위) 대비 Redis 네트워크 통신(ms 단위)으로 인한 딜레이를 측정하였으나, 평균 1~3ms 내외로 실시간 서비스에 영향이 없음을 확인.
    - **결과:**
        - 서버 재시작 시 데이터가 증발하던 문제가 완벽히 해결됨 (**지속성 확보**).
        - 애플리케이션 힙 메모리 사용량을 유저 수와 무관하게 일정 수준으로 유지하여 시스템 안정성 확보.
        
        ```
        [검증 결과 로그]
        $ redis-cli
        127.0.0.1:6379> HGETALL room:101
        1) "1"
        2) "{\"userId\":1,\"nickname\":\"User_1\",\"status\":\"STUDYING\",\"enterTime\":\"2026-01-21T17:00:00\"}"
        3) "2"
        4) "{\"userId\":2,\"nickname\":\"User_2\",\"status\":\"STUDYING\",\"enterTime\":\"2026-01-21T17:00:05\"}"
        ```
