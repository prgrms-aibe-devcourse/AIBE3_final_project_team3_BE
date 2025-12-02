package triplestar.mixchat.domain.member.presence.service;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.member.presence.dto.ExpiredPresence;
import triplestar.mixchat.domain.member.presence.repository.PresenceRepository;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final PresenceRepository presenceRepository;
    private final MemberRepository memberRepository;

    public void heartbeat(Long memberId) {
        presenceRepository.save(memberId);
    }

    public Set<Long> filterIsOnline(List<Long> memberIds) {
        return presenceRepository.filterIsOnline(memberIds);
    }

    public List<Long> getOnlineMemberIds(long offset, long size) {
        return presenceRepository.getOnlineMemberIds(offset, size);
    }

    public void disconnect(Long memberId) {
        presenceRepository.remove(memberId);
    }

    @Scheduled(fixedRateString = "${presence.scheduled.cleanup-rate-ms}")
    public void removeExpiredEntries() {
        List<ExpiredPresence> expiredPresences = presenceRepository.cleanupExpired();
        if (expiredPresences.isEmpty()) {
            return;
        }
    }
}
