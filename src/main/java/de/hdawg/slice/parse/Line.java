package de.hdawg.slice.parse;

import de.hdawg.slice.pdf.Word;
import java.util.List;

/**
 * A horizontal line of {@link Word} objects on a single PDF page, ordered left-to-right by
 * x-coordinate.
 *
 * @param page the 1-based PDF page number
 * @param y the y-coordinate of the line in PDF user units (baseline of the first word)
 * @param words the words on this line in left-to-right order; always an unmodifiable list
 */
public record Line(int page, float y, List<Word> words) {

  public Line {
    words = List.copyOf(words);
  }

  /**
   * Returns the space-joined text of all words on this line, as it would appear when read
   * left-to-right. Returns an empty string if the line has no words.
   */
  public String rawText() {
    return words.stream().map(Word::text).reduce((a, b) -> a + " " + b).orElse("");
  }
}
