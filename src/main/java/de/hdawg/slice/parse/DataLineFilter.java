package de.hdawg.slice.parse;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Identifies and filters lines that represent a single player row in the ranking table.
 *
 * <p>The presence of exactly one 8-digit token is used as the discriminator: header lines, footer
 * lines, and legend lines either contain no such token or contain more than one.
 */
public final class DataLineFilter {

  private static final Pattern EIGHT_DIGIT_ID = Pattern.compile("\\b\\d{8}\\b");

  /**
   * Returns {@code true} if {@code line} contains exactly one 8-digit word token, making it a
   * candidate data row with a single DTB ID.
   */
  public boolean isDataLine(Line line) {
    long matches =
        line.words().stream().filter(word -> EIGHT_DIGIT_ID.matcher(word.text()).matches()).count();
    return matches == 1;
  }

  /**
   * Returns an unmodifiable list of all lines from {@code lines} for which {@link
   * #isDataLine(Line)} returns {@code true}, preserving the original order.
   */
  public List<Line> keepDataLines(List<Line> lines) {
    return lines.stream().filter(this::isDataLine).toList();
  }
}
