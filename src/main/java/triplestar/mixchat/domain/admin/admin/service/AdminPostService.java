package triplestar.mixchat.domain.admin.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.admin.admin.constant.PostDeleteReason;
import triplestar.mixchat.domain.notification.constant.NotificationType;
import triplestar.mixchat.domain.post.post.entity.Post;
import triplestar.mixchat.domain.post.post.repository.PostRepository;
import triplestar.mixchat.global.notifiaction.NotificationEvent;

@Service
@RequiredArgsConstructor
public class AdminPostService {

    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void deletePostByAdmin(Long adminId, Long postId, int reasonCode) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + postId));

        Long writerId = post.getAuthor().getId();

        // 이유 Enum 변환
        PostDeleteReason reason = PostDeleteReason.fromCode(reasonCode);

        // 게시글 삭제
        postRepository.delete(post);

        // 🔥 알림 이벤트 발행
        NotificationEvent event = new NotificationEvent(
                writerId,
                adminId, // 관리자 → senderId 없음
                NotificationType.POST_DELETED,
                post.getTitle() + "게시글이  " + reason.getLabel() + " 사유로 삭제되었습니다."
        );

        eventPublisher.publishEvent(event);
    }
}