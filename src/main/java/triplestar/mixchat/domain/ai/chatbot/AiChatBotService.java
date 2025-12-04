package triplestar.mixchat.domain.ai.chatbot;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.constant.AiChatRoomType;
import triplestar.mixchat.domain.chat.chat.entity.AIChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.AIChatRoomRepository;

// Facade pattern
@Service
@RequiredArgsConstructor
public class AiChatBotService {

    private final RagSqlTutorService ragSqlTutorService;
    private final RagVectorService ragVectorService;
    private final AiFreeTalkService aiFreeTalkService;
    private final AIChatRoomRepository aiChatRoomRepository;

    @Transactional(readOnly = true)
    public String chat(Long userId, Long roomId, String userMessage) {
        AIChatRoom aiChatRoom = aiChatRoomRepository.findByIdWithPersona(roomId)
                .orElseThrow(() -> new EntityNotFoundException("해당 Id의 방 없음: " + roomId));

        // 페르소나 가져오기
        // ex: 'Engage in a free conversation on any topic. Keep the dialogue natural.'
        String persona = aiChatRoom.getPersona().getContent();

        AiChatRoomType roomType = aiChatRoom.getRoomType();

        return switch (roomType) {
            case ROLE_PLAY -> aiFreeTalkService.chat(aiChatRoom, userMessage, persona);
            case TUTOR_PERSONAL -> ragSqlTutorService.chat(userId, aiChatRoom, userMessage, persona);
            case TUTOR_SIMILAR -> ragVectorService.chat(userId, aiChatRoom, userMessage, persona);
        };
    }
}
