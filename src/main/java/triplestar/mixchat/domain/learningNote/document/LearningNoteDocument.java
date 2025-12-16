package triplestar.mixchat.domain.learningNote.document;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Document(indexName = "learning_note_embeddings")
public class LearningNoteDocument {
    @Field(type = FieldType.Dense_Vector, dims = 1536)
    private float[] embedding;

    @Id
    @Field
    private Long noteId;

    public LearningNoteDocument(float[] embedding, Long noteId) {
        this.embedding = embedding;
        this.noteId = noteId;
    }
}