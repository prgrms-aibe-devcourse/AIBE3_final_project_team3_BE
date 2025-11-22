package triplestar.mixchat.domain.member.presence.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.member.presence.repository.PresenceRepository;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final PresenceRepository presenceRepository;

    public void heartbeat(Long memberId) {
        presenceRepository.save(memberId);
    }

    public boolean isOnline(Long memberId) {
        return presenceRepository.isOnline(memberId);
    }

    public void delete(Long memberId) {
        presenceRepository.delete(memberId);
    }
}
