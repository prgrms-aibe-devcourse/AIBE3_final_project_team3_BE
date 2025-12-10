package triplestar.mixchat.global.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import triplestar.mixchat.domain.miniGame.sentenceGame.entity.FeedbackSnapshot;

@Converter
public class FeedbackSnapshotListConverter extends JsonListConverter<FeedbackSnapshot> {

    public FeedbackSnapshotListConverter() {
        super(new TypeReference<>() {});
    }
}