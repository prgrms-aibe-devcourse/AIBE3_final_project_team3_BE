package triplestar.mixchat.global.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class JsonListConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    // Java List -> JSON 문자열로 변환하여 DB에 저장
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("List<String> JSON 변환 실패", e);
        }
    }

    @Override
    // DB JSON 문자열 -> Java List로 변환
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 문자열 List<String> 변환 실패", e);
        }
    }

}
