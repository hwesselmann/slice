package de.hdawg.slice.parse;

import de.hdawg.slice.pdf.Word;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Groups individual {@link de.hdawg.slice.pdf.Word} objects into {@link Line} objects by
 * y-coordinate proximity, and strips legend boilerplate words before grouping.
 */
public final class LineGrouper {

  private static final float Y_TOLERANCE = 1.0f;

  // DTB ranking PDFs print a small vertical legend box near the top-right margin of the table
  // ("Reihenfolge" / "gemäß ATP-" or "gemäß WTA-" / "Rangliste" / "Stichtag" / a cutoff date like
  // "15.12.2025"), explaining that the list order follows the ATP/WTA ranking as of that date.
  // Its line pitch doesn't match the data table's row pitch, so as the page progresses the two
  // grids drift in and out of phase: whenever a legend line's y happens to fall within
  // Y_TOLERANCE of a data row's y, the legend word would otherwise get merged onto that row
  // (observed corrupting the club column for the row immediately preceding/at the collision,
  // e.g. "LTTC Rot-Weiß Berlin gemäß ATP-" or "Der Club an der Alster 15.12.2025"). These are
  // fixed boilerplate strings from the PDF template, never real data, so they are dropped by
  // exact content match before line grouping rather than guessed at via position heuristics.
  private static final Pattern CUTOFF_DATE = Pattern.compile("\\d{1,2}\\.\\d{1,2}\\.\\d{4}");

  /**
   * Groups the given words into lines sorted by page and y-coordinate.
   *
   * <p>Words whose y-coordinates differ by at most {@code 1.0} PDF user unit are considered to
   * belong to the same line. Within each line, words are sorted left-to-right by x-coordinate.
   * Known legend boilerplate tokens are removed before grouping.
   *
   * @param words the raw words extracted from the PDF, in any order
   * @return the grouped lines in page-then-y order; never {@code null}
   */
  public List<Line> group(List<Word> words) {
    List<Word> sorted =
        words.stream()
            .filter(word -> !isLegendBoilerplate(word))
            .sorted(Comparator.comparingInt(Word::page).thenComparing(Word::y))
            .toList();

    List<Line> lines = new ArrayList<>();
    List<Word> current = new ArrayList<>();
    int currentPage = -1;
    float currentY = Float.NaN;

    for (Word word : sorted) {
      boolean sameLine =
          !current.isEmpty()
              && word.page() == currentPage
              && Math.abs(word.y() - currentY) <= Y_TOLERANCE;
      if (!sameLine) {
        flush(lines, current, currentPage, currentY);
        current = new ArrayList<>();
        currentPage = word.page();
        currentY = word.y();
      }
      current.add(word);
    }
    flush(lines, current, currentPage, currentY);
    return lines;
  }

  private void flush(List<Line> lines, List<Word> current, int page, float y) {
    if (current.isEmpty()) {
      return;
    }
    List<Word> byX = current.stream().sorted(Comparator.comparing(Word::x0)).toList();
    lines.add(new Line(page, y, byX));
  }

  private boolean isLegendBoilerplate(Word word) {
    String text = word.text().strip();
    return switch (text) {
      case "Reihenfolge", "gemäß ATP-", "gemäß WTA-", "Rangliste", "Stichtag" -> true;
      default -> CUTOFF_DATE.matcher(text).matches();
    };
  }
}
