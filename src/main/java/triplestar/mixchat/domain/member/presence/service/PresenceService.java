package triplestar.mixchat.domain.member.presence.service;

import java.util.List;
import java.util.Map;
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

    public Map<Long, Boolean> isOnlineBulk(List<Long> memberIds) {
        return presenceRepository.isOnlineBulk(memberIds);
    }

    public List<Long> getOnlineMemberIds(long offset, long size) {
        return presenceRepository.getOnlineMemberIds(offset, size);
    }
}
