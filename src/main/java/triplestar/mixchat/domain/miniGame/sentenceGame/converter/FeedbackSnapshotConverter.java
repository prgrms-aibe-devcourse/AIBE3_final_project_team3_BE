package triplestar.mixchat.domain.miniGame.sentenceGame.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;
import triplestar.mixchat.domain.miniGame.sentenceGame.entity.FeedbackSnapshot;

@Converter
public class FeedbackSnapshotConverter implements AttributeConverter<List<FeedbackSnapshot>, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<FeedbackSnapshot> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Convert to JSON failed", e);
        }
    }

    @Override
    public List<FeedbackSnapshot> convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(
                    dbData,
                    mapper.getTypeFactory().constructCollectionType(List.class, FeedbackSnapshot.class)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Convert from JSON failed", e);
        }
    }
}