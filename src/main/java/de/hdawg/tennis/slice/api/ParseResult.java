package de.hdawg.tennis.slice.api;

import de.hdawg.tennis.slice.Slice;
import java.util.List;

/**
 * The result of parsing a DTB ranking PDF via {@link Slice#parse(java.nio.file.Path)
 * Slice.parse()}.
 *
 * @param list the structured ranking list extracted from the PDF
 * @param issues all warnings and errors encountered during parsing and validation, in the order
 *     they were detected; always an unmodifiable list
 */
public record ParseResult(RankingList list, List<ParseIssue> issues) {
  public ParseResult {
    issues = List.copyOf(issues);
  }
}
