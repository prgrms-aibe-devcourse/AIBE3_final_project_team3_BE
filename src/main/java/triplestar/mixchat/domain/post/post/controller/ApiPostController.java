package triplestar.mixchat.domain.post.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.post.post.dto.CommentCreateReq;
import triplestar.mixchat.domain.post.post.dto.CommentResp;
import triplestar.mixchat.domain.post.post.dto.CommentUpdateReq;
import triplestar.mixchat.domain.post.post.dto.LikeStatusResp;
import triplestar.mixchat.domain.post.post.dto.PostDetailResp;
import triplestar.mixchat.domain.post.post.dto.PostSummaryResp;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.springdoc.CommonBadResponse;
import triplestar.mixchat.global.springdoc.SignInInRequireResponse;
import triplestar.mixchat.global.springdoc.SuccessResponse;

import java.util.List;

@Tag(name = "ApiV1PostController", description = "게시글 API")
@SuccessResponse
@CommonBadResponse
@SecurityRequirement(name = "Authorization")
public interface ApiPostController {

    @Operation(summary = "게시글 작성", description = "새 게시글을 작성합니다.")
    @SignInInRequireResponse
    CustomResponse<PostDetailResp> createPost(
            @Parameter(hidden = true)
            CustomUserDetails userDetails,
            @Parameter(description = "제목", required = true, example = "첫 번째 글")
            @NotBlank @Size(min = 1, max = 255)
            String title,
            @Parameter(description = "내용", required = true, example = "안녕하세요")
            @NotBlank @Size(min = 1, max = 10000)
            String content,
            @Parameter(description = "이미지 파일들 (최대 10개)")
            List<MultipartFile> images
    );

    @Operation(summary = "게시글 목록 조회", description = "정렬 조건과 페이지 정보를 이용해 게시글 목록을 조회합니다.")
    CustomResponse<Page<PostSummaryResp>> getPosts(
            @Parameter(
                    description = "정렬 조건 (LATEST: 최신순, POPULAR: 인기순)",
                    schema = @Schema(type = "string", allowableValues = {"LATEST", "POPULAR"}),
                    example = "LATEST"
            )
            String sort,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            Integer page,
            @Parameter(description = "페이지당 게시글 수", example = "20")
            Integer size
    );

    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회하며 조회수를 1 증가시킵니다.")
    CustomResponse<PostDetailResp> getPost(Long postId);

    @Operation(summary = "게시글 수정", description = "작성자 또는 관리자만 게시글을 수정할 수 있습니다.")
    @SignInInRequireResponse
    CustomResponse<PostDetailResp> updatePost(
            @Parameter(hidden = true)
            CustomUserDetails userDetails,
            Long postId,
            @NotBlank @Size(min = 1, max = 255) String title,
            @NotBlank @Size(min = 1, max = 10000) String content,
            @Parameter(description = "이미지 파일들 (최대 10개)")
            List<MultipartFile> images
    );

    @Operation(summary = "게시글 삭제", description = "작성자 또는 관리자만 게시글을 삭제할 수 있습니다.")
    @SignInInRequireResponse
    CustomResponse<Void> deletePost(
            @Parameter(hidden = true)
            CustomUserDetails userDetails,
            Long postId
    );

    @Operation(summary = "게시글 좋아요", description = "게시글에 좋아요를 추가합니다.")
    @SignInInRequireResponse
    CustomResponse<LikeStatusResp> likePost(
            @Parameter(hidden = true)
            CustomUserDetails userDetails,
            Long postId
    );

    @Operation(summary = "게시글 좋아요 취소", description = "게시글에 눌렀던 좋아요를 취소합니다.")
    @SignInInRequireResponse
    CustomResponse<LikeStatusResp> unlikePost(
            @Parameter(hidden = true)
            CustomUserDetails userDetails,
            Long postId
    );

    @Operation(summary = "댓글 목록 조회", description = "게시글에 달린 댓글과 대댓글을 조회합니다.")
    CustomResponse<List<CommentResp>> getComments(Long postId);

    @Operation(summary = "댓글 작성", description = "게시글에 댓글 또는 대댓글을 작성합니다.")
    @SignInInRequireResponse
    CustomResponse<CommentResp> createComment(
            @Parameter(hidden = true)
            CustomUserDetails userDetails,
            Long postId,
            @Valid CommentCreateReq req
    );

    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다. 작성자 본인 또는 관리자만 수정할 수 있습니다.")
    @SignInInRequireResponse
    CustomResponse<CommentResp> updateComment(
            @Parameter(hidden = true)
            CustomUserDetails userDetails,
            Long postId,
            Long commentId,
            @Valid CommentUpdateReq req
    );

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. 작성자 본인 또는 관리자만 삭제할 수 있습니다.")
    @SignInInRequireResponse
    CustomResponse<Void> deleteComment(
            @Parameter(hidden = true)
            CustomUserDetails userDetails,
            Long postId,
            Long commentId
    );

    @Operation(summary = "댓글 좋아요", description = "댓글에 좋아요를 추가합니다.")
    @SignInInRequireResponse
    CustomResponse<LikeStatusResp> likeComment(
            @Parameter(hidden = true)
            CustomUserDetails userDetails,
            Long postId,
            Long commentId
    );

    @Operation(summary = "댓글 좋아요 취소", description = "댓글의 좋아요를 취소합니다.")
    @SignInInRequireResponse
    CustomResponse<LikeStatusResp> unlikeComment(
            @Parameter(hidden = true)
            CustomUserDetails userDetails,
            Long postId,
            Long commentId
    );
}
