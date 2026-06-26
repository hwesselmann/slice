package de.hdawg.tennis.slice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.hdawg.tennis.slice.api.Discipline;
import de.hdawg.tennis.slice.api.RankingEntry;
import de.hdawg.tennis.slice.api.Score;
import de.hdawg.tennis.slice.api.Severity;
import de.hdawg.tennis.slice.parse.Line;
import de.hdawg.tennis.slice.pdf.Word;
import de.hdawg.tennis.slice.validate.ParseMode;
import de.hdawg.tennis.slice.validate.ParseReportBuilder;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SliceProcessLineTest {

  @Test
  void lenientModeRecordsErrorAndContinuesWhenRankTokenIsNotNumeric() {
    // Leading token is non-numeric -> RowParser throws NumberFormatException.
    // LENIENT must not abort the document: record an ERROR and skip the row.
    Line malformed = lineOf("X", "Surname", "Given", "GER", "12345678", "BB", "TC Verein", "59,0");

    Slice slice = Slice.builder().mode(ParseMode.LENIENT).build();
    ParseReportBuilder report = new ParseReportBuilder();

    List<RankingEntry> entries =
        slice.processDataLines(List.of(malformed), Discipline.JUNIOREN, report);

    assertThat(entries).isEmpty();
    assertThat(report.build())
        .anyMatch(
            issue ->
                issue.severity() == Severity.ERROR && issue.message().contains("nicht parsebar"));
  }

  @Test
  void strictModeThrowsWhenRankTokenIsNotNumeric() {
    Line malformed = lineOf("X", "Surname", "Given", "GER", "12345678", "BB", "TC Verein", "59,0");

    Slice slice = Slice.builder().mode(ParseMode.STRICT).build();
    ParseReportBuilder report = new ParseReportBuilder();

    List<Line> lines = List.of(malformed);
    assertThatThrownBy(() -> slice.processDataLines(lines, Discipline.JUNIOREN, report))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void strictModeThrowsOnValidationError() {
    // Row is parseable but fails validation: "ZZZ" is not a known VBD association code,
    // so validateAssociation emits an ERROR which STRICT mode must surface immediately.
    Line row = lineOf("1", "Surname", "Given", "12345678", "ZZZ", "TC", "Verein", "59,0");

    Slice slice = Slice.builder().mode(ParseMode.STRICT).build();
    ParseReportBuilder report = new ParseReportBuilder();

    List<Line> lines = List.of(row);
    assertThatThrownBy(() -> slice.processDataLines(lines, Discipline.HERREN, report))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void lenientModeContinuesOnValidationError() {
    // Row parses fine but association "ZZZ" is unknown → validation ERROR → skipped, not thrown
    Line row = lineOf("1", "Surname", "Given", "12345678", "ZZZ", "TC", "Verein", "59,0");

    Slice slice = Slice.builder().mode(ParseMode.LENIENT).build();
    ParseReportBuilder report = new ParseReportBuilder();

    List<RankingEntry> entries = slice.processDataLines(List.of(row), Discipline.HERREN, report);

    assertThat(entries).isEmpty();
    assertThat(report.build()).anyMatch(i -> i.severity() == Severity.ERROR);
  }

  @Test
  void protectedRankingTokenBuildsCorrectScore() {
    // Multi-token last name pushes "Erika" past the NAME_VORNAME_BOUNDARY_X threshold (185f)
    Line row =
        lineOf(
            "5",
            "Muster",
            "von",
            "der",
            "Heiden",
            "Erika",
            "GER",
            "12345678",
            "TVN",
            "TC",
            "Verein",
            "PR");

    Slice slice = Slice.builder().mode(ParseMode.LENIENT).build();
    ParseReportBuilder report = new ParseReportBuilder();

    List<RankingEntry> entries = slice.processDataLines(List.of(row), Discipline.JUNIOREN, report);

    assertThat(entries).hasSize(1);
    assertThat(entries.get(0).score()).isInstanceOf(Score.ProtectedRanking.class);
  }

  @Test
  void projectedTokenBuildsCorrectScore() {
    Line row =
        lineOf(
            "10",
            "Muster",
            "von",
            "der",
            "Heiden",
            "Erika",
            "GER",
            "12345678",
            "TVN",
            "TC",
            "Verein",
            "Einst.");

    Slice slice = Slice.builder().mode(ParseMode.LENIENT).build();
    ParseReportBuilder report = new ParseReportBuilder();

    List<RankingEntry> entries =
        slice.processDataLines(List.of(row), Discipline.JUNIORINNEN, report);

    assertThat(entries).hasSize(1);
    assertThat(entries.get(0).score()).isInstanceOf(Score.Projected.class);
  }

  private Line lineOf(String... tokens) {
    List<Word> words = new ArrayList<>();
    float x = 60f;
    for (String token : tokens) {
      words.add(new Word(token, x, x + token.length() * 6f, 100f, 3));
      x += token.length() * 6f + 4f;
    }
    return new Line(3, 100f, words);
  }
}
