package de.hdawg.slice.metadata;

import de.hdawg.slice.api.Discipline;
import de.hdawg.slice.api.ParseIssue;
import de.hdawg.slice.api.Severity;
import de.hdawg.slice.api.YearRange;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Extracts ranking-list metadata (stichtag, validFrom, pointsThreshold, birthYears) from the full
 * PDF text. All issues use page=1 and rawLine="(Dokumentmetadaten)".
 */
public final class MetadataExtractor {

  /**
   * Matches "Stichtag 31.12.2025" or "Stichtag: 31.12.2025". Uses [ \t] (not \s) so it does NOT
   * match "Stichtag\n15.12.2025" (the ATP/WTA legend trap).
   */
  private static final Pattern STICHTAG =
      Pattern.compile("Stichtag:?[ \\t]+(\\d{2})\\.(\\d{2})\\.(\\d{4})");

  /**
   * Matches "Gültig ab 09.01.2026". Uses the \\u00FC regex Unicode escape so the pattern works
   * regardless of source-file encoding. Java's Pattern engine resolves ü (in the regex string) to
   * the ü character at runtime.
   */
  private static final Pattern GUELTIG_AB =
      Pattern.compile("G\\u00FCltig ab[ \\t]+(\\d{2})\\.(\\d{2})\\.(\\d{4})");

  /** Matches "bis 603 Punkte" on data-page headers (Herren/Damen only). */
  private static final Pattern POINTS_THRESHOLD =
      Pattern.compile("bis[ \\t]+(\\d{1,5})[ \\t]+Punkte");

  /** Matches "Jg. 2008 - 2014" or "Jg. 2008-2014" (juniors only). */
  private static final Pattern BIRTH_YEARS =
      Pattern.compile("Jg\\.[ \\t]*(\\d{4})[ \\t]*-[ \\t]*(\\d{4})");

  private static final String RAW_LINE = "(Dokumentmetadaten)";
  private static final int META_PAGE = 1;

  /**
   * The result of a metadata extraction run.
   *
   * @param metadata the extracted metadata; fields that could not be parsed are {@code null}
   * @param issues any warnings or errors encountered during extraction; always an unmodifiable list
   */
  public record Result(RankingMetadata metadata, List<ParseIssue> issues) {
    public Result {
      issues = List.copyOf(issues);
    }
  }

  /**
   * Extracts all ranking-list metadata from the full text of the PDF document.
   *
   * <p>Fields that cannot be found or parsed are returned as {@code null} in the {@link
   * RankingMetadata}; a corresponding {@link ParseIssue} is added to the result. {@code
   * pointsThreshold} is only extracted for senior disciplines; {@code birthYears} only for junior
   * disciplines.
   *
   * @param fullDocumentText the complete plain-text content of the PDF as produced by {@link
   *     org.apache.pdfbox.text.PDFTextStripper}
   * @param discipline the discipline detected from the first page, used to decide which optional
   *     fields to extract
   * @return the extraction result, never {@code null}
   */
  public Result extract(String fullDocumentText, Discipline discipline) {
    List<ParseIssue> issues = new ArrayList<>();

    @Nullable LocalDate stichtag = parseStichtag(fullDocumentText, issues);
    @Nullable LocalDate validFrom = parseValidFrom(fullDocumentText, issues);
    @Nullable Integer pointsThreshold = parsePointsThreshold(fullDocumentText, discipline, issues);
    @Nullable YearRange birthYears = parseBirthYears(fullDocumentText, discipline, issues);

    return new Result(
        new RankingMetadata(stichtag, validFrom, pointsThreshold, birthYears), issues);
  }

  private @Nullable LocalDate parseStichtag(String text, List<ParseIssue> issues) {
    Matcher m = STICHTAG.matcher(text);
    if (!m.find()) {
      issues.add(
          new ParseIssue(Severity.ERROR, META_PAGE, RAW_LINE, "Stichtag nicht im PDF gefunden"));
      return null;
    }
    return parseDate(
        m.group(1), m.group(2), m.group(3), Severity.ERROR, "Stichtag-Datum ungültig", issues);
  }

  private @Nullable LocalDate parseValidFrom(String text, List<ParseIssue> issues) {
    Matcher m = GUELTIG_AB.matcher(text);
    if (!m.find()) {
      issues.add(
          new ParseIssue(
              Severity.WARNING, META_PAGE, RAW_LINE, "'Gültig ab'-Datum nicht gefunden"));
      return null;
    }
    return parseDate(
        m.group(1), m.group(2), m.group(3), Severity.WARNING, "'Gültig ab'-Datum ungültig", issues);
  }

  private @Nullable Integer parsePointsThreshold(
      String text, Discipline discipline, List<ParseIssue> issues) {
    if (discipline.isJunior()) {
      return null; // not applicable; no issue
    }
    Matcher m = POINTS_THRESHOLD.matcher(text);
    if (!m.find()) {
      issues.add(
          new ParseIssue(Severity.WARNING, META_PAGE, RAW_LINE, "Punkte-Schwelle nicht gefunden"));
      return null;
    }
    return Integer.valueOf(m.group(1));
  }

  private @Nullable YearRange parseBirthYears(
      String text, Discipline discipline, List<ParseIssue> issues) {
    if (!discipline.isJunior()) {
      return null; // not applicable; no issue
    }
    Matcher m = BIRTH_YEARS.matcher(text);
    if (!m.find()) {
      issues.add(
          new ParseIssue(
              Severity.WARNING, META_PAGE, RAW_LINE, "Jahrgangsbereich (Jg.) nicht gefunden"));
      return null;
    }
    int from = Integer.parseInt(m.group(1));
    int to = Integer.parseInt(m.group(2));
    try {
      return new YearRange(from, to);
    } catch (IllegalArgumentException e) {
      issues.add(
          new ParseIssue(
              Severity.WARNING,
              META_PAGE,
              RAW_LINE,
              "Jahrgangsbereich ungültig: " + e.getMessage()));
      return null;
    }
  }

  private @Nullable LocalDate parseDate(
      String day,
      String month,
      String year,
      Severity severity,
      String message,
      List<ParseIssue> issues) {
    try {
      return LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
    } catch (DateTimeException e) {
      issues.add(new ParseIssue(severity, META_PAGE, RAW_LINE, message + ": " + e.getMessage()));
      return null;
    }
  }
}
