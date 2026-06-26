package de.hdawg.slice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.hdawg.slice.api.Discipline;
import de.hdawg.slice.api.RankingEntry;
import de.hdawg.slice.api.Severity;
import de.hdawg.slice.parse.Line;
import de.hdawg.slice.pdf.Word;
import de.hdawg.slice.validate.ParseMode;
import de.hdawg.slice.validate.ParseReportBuilder;
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

    assertThatThrownBy(
            () -> slice.processDataLines(List.of(malformed), Discipline.JUNIOREN, report))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void strictModeThrowsOnValidationError() {
    // Row is parseable but fails validation: "ZZZ" is not a known VBD association code,
    // so validateAssociation emits an ERROR which STRICT mode must surface immediately.
    Line row = lineOf("1", "Surname", "Given", "12345678", "ZZZ", "TC", "Verein", "59,0");

    Slice slice = Slice.builder().mode(ParseMode.STRICT).build();
    ParseReportBuilder report = new ParseReportBuilder();

    assertThatThrownBy(() -> slice.processDataLines(List.of(row), Discipline.HERREN, report))
        .isInstanceOf(IllegalStateException.class);
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
