package triplestar.mixchat.global.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;

@Converter
public class StringListConverter extends JsonListConverter<String> {

    public StringListConverter() {
        super(new TypeReference<>() {});
    }
}