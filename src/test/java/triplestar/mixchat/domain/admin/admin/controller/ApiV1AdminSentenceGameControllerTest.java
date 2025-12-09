package triplestar.mixchat.domain.admin.admin.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("관리자 미니게임 API 테스트")
class ApiV1AdminSentenceGameControllerTest {
//
//    @Autowired
//    MockMvc mockMvc;
//
//    @Autowired
//    MemberRepository memberRepository;
//
//    @Autowired
//    LearningNoteRepository learningNoteRepository;
//
//    @Autowired
//    ObjectMapper objectMapper;
//
//    @Autowired
//    SentenceGameRepository sentenceGameRepository;
//
//    private Member testAdmin;
//    private Member testMember;
//
//    @BeforeEach
//    void setUp() {
//        testAdmin = memberRepository.save(TestMemberFactory.createAdmin("testAdmin"));
//        testMember = memberRepository.save(TestMemberFactory.createMember("testMember"));
//
//    }
//
//    @Test
//    @DisplayName("관리자 미니게임 문장 등록 성공")
//    @WithUserDetails(value = "testAdmin", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    void createMiniGame_success() throws Exception {
//
//        AdminSentenceGameCreateReq req = new AdminSentenceGameCreateReq(
//                "I goed to school.",
//                "I went to school."
//        );
//
//        MvcResult result = mockMvc.perform(
//                        post("/api/v1/admin/sentence-game")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(req))
//                )
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.msg").value("미니게임 문장이 등록되었습니다."))
//                .andExpect(jsonPath("$.data.sentenceGameId").isNumber())
//                .andReturn();
//
//        String responseBody = result.getResponse().getContentAsString();
//        Integer idInt = JsonPath.read(responseBody, "$.data.sentenceGameId");
//        Long generatedId = idInt.longValue();
//
//        assertThat(generatedId).isNotNull();
//
//        SentenceGame saved = sentenceGameRepository.findById(generatedId)
//                .orElseThrow(() -> new AssertionError("DB에 저장된 문장을 찾을 수 없음"));
//
//        assertThat(saved.getOriginalContent()).isEqualTo("I goed to school.");
//        assertThat(saved.getCorrectedContent()).isEqualTo("I went to school.");
//    }
//
//    @Test
//    @DisplayName("미니게임 등록 실패 - Validation 오류 발생(originalContent 빈값)")
//    @WithUserDetails(value = "testAdmin", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    void createMiniGame_validation_fail() throws Exception {
//
//        AdminSentenceGameCreateReq req = new AdminSentenceGameCreateReq(
//                "", // invalid
//                "I went to school."
//        );
//
//        mockMvc.perform(
//                        post("/api/v1/admin/sentence-game")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(req))
//                )
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("미니게임 등록 실패 - 관리자 권한이 아니면 403")
//    @WithUserDetails(value = "testMember", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    void createMiniGame_forbidden() throws Exception {
//
//        AdminSentenceGameCreateReq req = new AdminSentenceGameCreateReq(
//                "I goed",
//                "I went"
//        );
//
//        mockMvc.perform(
//                        post("/api/v1/admin/sentence-game")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(req))
//                )
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    @DisplayName("미니게임 등록용 전체 학습노트 목록 조회 성공")
//    @WithUserDetails(value = "testAdmin", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    void getSentenceGameNoteList_success() throws Exception {
//
//        // --- given: 학습노트 2개 저장 ---
//        LearningNote note1 = learningNoteRepository.save(
//                LearningNote.create(
//                        testMember,
//                        "I goed to school.",
//                        "I went to school."
//                )
//        );
//
//        LearningNote note2 = learningNoteRepository.save(
//                LearningNote.create(
//                        testMember,
//                        "She don't like apples.",
//                        "She doesn't like apples."
//                )
//        );
//
//        MvcResult result = mockMvc.perform(
//                        get("/api/v1/admin/sentence-game/notes")
//                )
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.msg").value("미니게임 등록용 학습노트 목록 조회 성공"))
//                .andExpect(jsonPath("$.data.content").isArray())
//                .andReturn();
//
//        String json = result.getResponse().getContentAsString();
//        List<Map<String, Object>> list = JsonPath.read(json, "$.data.content");
//
//        assertThat(list.size()).isEqualTo(2);
//
//        Map<String, Object> resp1 = list.stream()
//                .filter(item -> ((Integer) item.get("id")).longValue() == note1.getId())
//                .findFirst()
//                .orElseThrow(() -> new AssertionError("note1 응답 없음"));
//
//        assertThat(resp1.get("originalContent")).isEqualTo(note1.getOriginalContent());
//        assertThat(resp1.get("correctedContent")).isEqualTo(note1.getCorrectedContent());
//
//        Map<String, Object> resp2 = list.stream()
//                .filter(item -> ((Integer) item.get("id")).longValue() == note2.getId())
//                .findFirst()
//                .orElseThrow(() -> new AssertionError("note2 응답 없음"));
//
//        assertThat(resp2.get("originalContent")).isEqualTo(note2.getOriginalContent());
//        assertThat(resp2.get("correctedContent")).isEqualTo(note2.getCorrectedContent());
//    }
//
//    @Test
//    @DisplayName("미니게임 등록용 전체 학습노트 목록 조회 실패 - USER는 접근 불가")
//    @WithUserDetails(value = "testMember", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    void getMiniGameList_forbidden() throws Exception {
//
//        mockMvc.perform(
//                        get("/api/v1/admin/sentence-game/notes")
//                )
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    @DisplayName("관리자 미니게임 문장 목록 조회 성공")
//    @WithUserDetails(
//            value = "testAdmin",
//            userDetailsServiceBeanName = "testUserDetailsService",
//            setupBefore = TestExecutionEvent.TEST_EXECUTION
//    )
//    void getMiniGameList_success() throws Exception {
//
//        // --- given: 문장게임 2개 저장 ---
//        sentenceGameRepository.save(
//                SentenceGame.createSentenceGame("I goed", "I went")
//        );
//        sentenceGameRepository.save(
//                SentenceGame.createSentenceGame("She dont like apple", "She does not like apples")
//        );
//
//        MvcResult result = mockMvc.perform(
//                        get("/api/v1/admin/sentence-game")
//                )
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.msg").value("문장게임 목록 조회 성공"))
//                .andExpect(jsonPath("$.data.content").isArray())
//                .andReturn();
//
//        String json = result.getResponse().getContentAsString();
//        List<Map<String, Object>> list = JsonPath.read(json, "$.data.content");
//
//        assertThat(list.size()).isEqualTo(2);
//    }
//
//    @Test
//    @DisplayName("미니게임 목록 조회 실패 - USER는 접근 불가")
//    @WithUserDetails(value = "testMember",userDetailsServiceBeanName = "testUserDetailsService",setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    void getMiniGameList_forbidden2() throws Exception {
//
//        mockMvc.perform(
//                        get("/api/v1/admin/sentence-game")
//                )
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    @DisplayName("문장게임 삭제 성공")
//    @WithUserDetails(
//            value = "testAdmin",
//            userDetailsServiceBeanName = "testUserDetailsService",
//            setupBefore = TestExecutionEvent.TEST_EXECUTION
//    )
//    void deleteMiniGame_success() throws Exception {
//
//        // --- given: 문장 하나 저장 후 id 추출 ---
//        SentenceGame saved = sentenceGameRepository.save(
//                SentenceGame.createSentenceGame("I goed", "I went")
//        );
//
//        Long id = saved.getId();
//
//        mockMvc.perform(
//                        delete("/api/v1/admin/sentence-game/" + id)
//                )
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.msg").value("문장게임 문장이 삭제되었습니다."));
//
//        // --- then: DB에서 삭제되었는지 확인 ---
//        boolean exists = sentenceGameRepository.existsById(id);
//        assertThat(exists).isFalse();
//    }
//
//    @Test
//    @DisplayName("문장게임 삭제 실패 - 존재하지 않는 ID")
//    @WithUserDetails(
//            value = "testAdmin",
//            userDetailsServiceBeanName = "testUserDetailsService",
//            setupBefore = TestExecutionEvent.TEST_EXECUTION
//    )
//    void deleteMiniGame_notFound() throws Exception {
//
//        mockMvc.perform(
//                        delete("/api/v1/admin/sentence-game/99999")
//                )
//                .andExpect(status().isBadRequest()); // IllegalArgumentException 처리 방식과 동일
//    }
//
//    @Test
//    @DisplayName("문장게임 삭제 실패 - USER는 접근 불가")
//    @WithUserDetails(
//            value = "testMember",
//            userDetailsServiceBeanName = "testUserDetailsService",
//            setupBefore = TestExecutionEvent.TEST_EXECUTION
//    )
//    void deleteMiniGame_forbidden() throws Exception {
//
//        mockMvc.perform(
//                        delete("/api/v1/admin/sentence-game/1")
//                )
//                .andExpect(status().isForbidden());
//    }

}