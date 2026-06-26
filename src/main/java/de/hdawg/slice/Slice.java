package de.hdawg.slice;

import de.hdawg.slice.api.Discipline;
import de.hdawg.slice.api.ParseIssue;
import de.hdawg.slice.api.ParseResult;
import de.hdawg.slice.api.RankingEntry;
import de.hdawg.slice.api.RankingList;
import de.hdawg.slice.api.Score;
import de.hdawg.slice.api.Severity;
import de.hdawg.slice.metadata.DisciplineDetector;
import de.hdawg.slice.metadata.MetadataExtractor;
import de.hdawg.slice.parse.DataLineFilter;
import de.hdawg.slice.parse.Line;
import de.hdawg.slice.parse.LineGrouper;
import de.hdawg.slice.parse.ParsedRow;
import de.hdawg.slice.parse.RowParser;
import de.hdawg.slice.pdf.PdfWordExtractor;
import de.hdawg.slice.pdf.Word;
import de.hdawg.slice.validate.ParseMode;
import de.hdawg.slice.validate.ParseReportBuilder;
import de.hdawg.slice.validate.Validators;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Entry point for parsing DTB tennis ranking PDFs into typed {@link ParseResult} objects.
 *
 * <p>Typical usage:
 *
 * <pre>{@code
 * ParseResult result = Slice.builder()
 *     .mode(ParseMode.LENIENT)
 *     .build()
 *     .parse(Path.of("herren.pdf"));
 *
 * RankingList list = result.list();
 * List<ParseIssue> issues = result.issues();
 * }</pre>
 *
 * <p>Use {@link ParseMode#LENIENT} (the default) to collect all issues and skip problematic rows.
 * Use {@link ParseMode#STRICT} to have the first error throw an {@link IllegalStateException}.
 */
// Top-level facade couples to all pipeline stages by design.
@SuppressWarnings("PMD.CouplingBetweenObjects")
public final class Slice {

  private static final String SCORE_TOKEN_PR = "PR";
  private static final String SCORE_TOKEN_EINST = "Einst.";

  private final ParseMode mode;

  private Slice(ParseMode mode) {
    this.mode = mode;
  }

  /** Returns a new builder for configuring and creating a {@link Slice} instance. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Parses the given DTB ranking PDF and returns the structured ranking list together with all
   * warnings and errors encountered during parsing.
   *
   * <p>The pipeline executes as follows:
   *
   * <ol>
   *   <li>Extract word positions from the PDF via PDFBox.
   *   <li>Group words into lines by y-coordinate.
   *   <li>Filter for lines that contain exactly one 8-digit DTB ID.
   *   <li>Parse each data line into a {@link de.hdawg.slice.api.RankingEntry}.
   *   <li>Validate association codes, nationality codes, names, and points.
   *   <li>Extract ranking metadata (Stichtag, validFrom, pointsThreshold, birthYears) from the full
   *       document text.
   * </ol>
   *
   * @param pdf path to a DTB ranking PDF file
   * @return the parsed {@link ParseResult}, always non-null; issues list is non-null and may be
   *     empty
   * @throws IOException if the PDF file cannot be read
   * @throws IllegalStateException if {@link ParseMode#STRICT} is active and any parse or validation
   *     error is encountered
   */
  public ParseResult parse(Path pdf) throws IOException {
    // Load the PDF text once: page-1 text for discipline detection (deliberate
    // scoping to avoid stray discipline words in footers/cross-references),
    // and full-document text for metadata extraction.
    PdfTexts texts = extractPdfTexts(pdf);
    Discipline discipline = new DisciplineDetector().detect(texts.firstPage());

    List<Word> words = new PdfWordExtractor().extract(pdf);
    List<Line> lines = new LineGrouper().group(words);
    List<Line> dataLines = new DataLineFilter().keepDataLines(lines);

    ParseReportBuilder report = new ParseReportBuilder();
    List<RankingEntry> entries = processDataLines(dataLines, discipline, report);

    MetadataExtractor.Result meta = new MetadataExtractor().extract(texts.full(), discipline);
    report.addAll(meta.issues());
    boolean metaHasError = meta.issues().stream().anyMatch(i -> i.severity() == Severity.ERROR);
    if (metaHasError && mode == ParseMode.STRICT) {
      throw new IllegalStateException(
          "Metadata extraction failed in STRICT mode: " + meta.issues());
    }
    LocalDate stichtag =
        meta.metadata().stichtag() != null ? meta.metadata().stichtag() : LocalDate.MIN;
    LocalDate validFrom =
        meta.metadata().validFrom() != null ? meta.metadata().validFrom() : LocalDate.MIN;

    RankingList list =
        new RankingList(
            discipline,
            stichtag,
            validFrom,
            meta.metadata().pointsThreshold(),
            meta.metadata().birthYears(),
            entries);
    return new ParseResult(list, report.build());
  }

  /**
   * Parses, validates and collects ranking entries for the given data lines. Package-private so the
   * LENIENT/STRICT row-error and parse-exception handling can be unit-tested without a backing PDF.
   */
  List<RankingEntry> processDataLines(
      List<Line> dataLines, Discipline discipline, ParseReportBuilder report) {
    RowParser rowParser = new RowParser();
    Validators validators = new Validators();
    List<RankingEntry> entries = new ArrayList<>();

    for (Line line : dataLines) {
      ParsedRow row = tryParseRow(rowParser, line, report);
      if (row == null) {
        continue;
      }
      if (shouldSkipAListe(row, discipline)) {
        continue;
      }

      List<ParseIssue> rowIssues =
          validators.validateRow(row, discipline, line.page(), line.rawText());
      report.addAll(rowIssues);

      boolean rowHasError = rowIssues.stream().anyMatch(i -> i.severity() == Severity.ERROR);
      if (rowHasError && mode == ParseMode.STRICT) {
        throw new IllegalStateException("Validation failed in STRICT mode: " + rowIssues);
      }
      if (rowHasError) {
        continue;
      }

      Score score = buildScore(row, discipline);
      // Validators.validateRow emits an ERROR when association is null/unknown, and such
      // rows are skipped above (or the STRICT path already threw). So association is
      // guaranteed non-null here, even though ParsedRow.association() is @Nullable.
      String association =
          Objects.requireNonNull(row.association(), "association validated non-null above");
      entries.add(
          new RankingEntry(
              row.rank(),
              row.lastName(),
              row.firstName(),
              row.nationality(),
              row.dtbId(),
              association,
              row.club(),
              score));
    }
    return entries;
  }

  /**
   * Attempts to parse a line. On RuntimeException, records the issue and returns null (LENIENT) or
   * re-throws wrapped in IllegalStateException (STRICT).
   */
  // RowParser may throw any unchecked exception on malformed PDF data; all are downgraded to
  // ParseIssue.
  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private ParsedRow tryParseRow(RowParser rowParser, Line line, ParseReportBuilder report) {
    try {
      return rowParser.parse(line);
    } catch (RuntimeException ex) {
      ParseIssue issue =
          new ParseIssue(
              Severity.ERROR,
              line.page(),
              line.rawText(),
              "Zeile nicht parsebar: " + ex.getMessage());
      report.add(issue);
      if (mode == ParseMode.STRICT) {
        throw new IllegalStateException("Parse failed in STRICT mode: " + issue, ex);
      }
      return null;
    }
  }

  private static boolean shouldSkipAListe(ParsedRow row, Discipline discipline) {
    return (discipline == Discipline.HERREN || discipline == Discipline.DAMEN) && row.fromAListe();
  }

  private static Score buildScore(ParsedRow row, Discipline discipline) {
    if (row.points() != null) {
      return new Score.Points(row.points());
    }
    if (SCORE_TOKEN_PR.equals(row.scoreToken())) {
      return Score.ProtectedRanking.INSTANCE;
    }
    if (SCORE_TOKEN_EINST.equals(row.scoreToken())) {
      return Score.Projected.INSTANCE;
    }
    return discipline == Discipline.DAMEN ? Score.International.WTA : Score.International.ATP;
  }

  private record PdfTexts(String firstPage, String full) {}

  private PdfTexts extractPdfTexts(Path pdf) throws IOException {
    try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
      PDFTextStripper page1Stripper = new PDFTextStripper();
      page1Stripper.setStartPage(1);
      page1Stripper.setEndPage(1);
      String firstPage = page1Stripper.getText(document);
      String full = new PDFTextStripper().getText(document);
      return new PdfTexts(firstPage, full);
    }
  }

  /** Builder for {@link Slice}. */
  public static final class Builder {
    private ParseMode parseMode = ParseMode.LENIENT;

    /**
     * Sets the parse mode. Defaults to {@link ParseMode#LENIENT} if not called.
     *
     * @return this builder, for chaining
     */
    public Builder mode(ParseMode mode) {
      this.parseMode = mode;
      return this;
    }

    /** Builds a {@link Slice} instance with the configured settings. */
    public Slice build() {
      return new Slice(parseMode);
    }
  }
}
