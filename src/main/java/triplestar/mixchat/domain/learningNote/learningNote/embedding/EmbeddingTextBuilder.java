package triplestar.mixchat.domain.learningNote.learningNote.embedding;

import triplestar.mixchat.domain.learningNote.learningNote.entity.Feedback;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;

public class EmbeddingTextBuilder {
    public static String build(LearningNote note) {
        StringBuilder sb = new StringBuilder();

        sb.append("Original: ").append(note.getOriginalContent()).append("\n");
        sb.append("Corrected: ").append(note.getCorrectedContent()).append("\n\n");

        sb.append("Feedbacks:\n");
        for (Feedback fb : note.getFeedbacks()) {
            sb.append("- Tag: ").append(fb.getTag()).append("\n");
            sb.append("  Problem: ").append(fb.getProblem()).append("\n");
            sb.append("  Correction: ").append(fb.getCorrection()).append("\n");
            sb.append("  Extra: ").append(fb.getExtra()).append("\n\n");
        }
        return sb.toString();
    }
}