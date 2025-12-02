package triplestar.mixchat.global.websocket;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomClosedEventPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendRoomClosed(Long roomId, String roomName, String reasonLabel) {
        Map<String, Object> payload = Map.of(
                "type", "ROOM_CLOSED",
                "roomId", roomId,
                "roomName", roomName,
                "reasonLabel", reasonLabel
        );

        messagingTemplate.convertAndSend("/topic/group/rooms/" + roomId, payload);
    }
}
