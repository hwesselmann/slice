package de.hdawg.tennis.slice.parse;

import static org.assertj.core.api.Assertions.assertThat;

import de.hdawg.tennis.slice.pdf.Word;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class RowParserTest {

  @Test
  void parsesPlainRowWithNationalityAndPoints() {
    Line line =
        lineOf(
            "84",
            "Meyer",
            "auf",
            "der",
            "Heide",
            "Erika",
            "GER",
            "12345678",
            "TVN",
            "TC",
            "1899",
            "Blau-Weiss",
            "Berlin",
            "24855,0");

    ParsedRow row = new RowParser().parse(line);

    assertThat(row.rank()).isEqualTo(84);
    assertThat(row.fromAListe()).isFalse();
    assertThat(row.lastName()).isEqualTo("Meyer auf der Heide");
    assertThat(row.firstName()).isEqualTo("Erika");
    assertThat(row.nationality()).isEqualTo("GER");
    assertThat(row.dtbId()).isEqualTo("12345678");
    assertThat(row.association()).isEqualTo("TVN");
    assertThat(row.club()).isEqualTo("TC 1899 Blau-Weiss Berlin");
    assertThat(row.points()).isEqualTo(new BigDecimal("24855.0"));
  }

  @Test
  void parsesRowWithoutNationality() {
    Line line =
        lineOf("A", "1", "Samsonova", "Liudmilla", "29862749", "WTV", "Verein", "X", "50068,0");

    ParsedRow row = new RowParser().parse(line);

    assertThat(row.fromAListe()).isTrue();
    assertThat(row.rank()).isEqualTo(1);
    assertThat(row.nationality()).isNull();
  }

  @Test
  void parsesRowWithEmptyPointsCellAsNull() {
    Line line = lineOf("1", "Paolini", "Jasmine", "ITA", "11122233", "TVN", "Verein", "Y");

    ParsedRow row = new RowParser().parse(line);

    assertThat(row.points()).isNull();
    assertThat(row.scoreToken()).isNull();
  }

  @Test
  void parsesProtectedRankingToken() {
    Line line = lineOf("5", "Mustermann", "Erika", "GER", "12345678", "TVN", "TC Verein", "PR");

    ParsedRow row = new RowParser().parse(line);

    assertThat(row.points()).isNull();
    assertThat(row.scoreToken()).isEqualTo("PR");
    assertThat(row.club()).isEqualTo("TC Verein");
  }

  @Test
  void parsesProjectedEntryToken() {
    Line line =
        lineOf("12", "Mustermann", "Erika", "GER", "12345678", "TVN", "TC Verein", "Einst.");

    ParsedRow row = new RowParser().parse(line);

    assertThat(row.points()).isNull();
    assertThat(row.scoreToken()).isEqualTo("Einst.");
    assertThat(row.club()).isEqualTo("TC Verein");
  }

  @Test
  void capturesRecognizedNationalityCodeWithoutPollutingFirstName() {
    Line line =
        lineOf(
            "10",
            "Surname",
            "Filler",
            "Filler",
            "Given",
            "SUI",
            "11093635",
            "BB",
            "TC Verein",
            "59,0");

    ParsedRow row = new RowParser().parse(line);

    assertThat(row.nationality()).isEqualTo("SUI");
    assertThat(row.firstName()).isEqualTo("Given");
    assertThat(row.firstName()).doesNotContain("SUI");
    assertThat(row.lastName()).doesNotContain("SUI");
  }

  @Test
  void capturesUnrecognizedNationalityCodeSoValidatorCanFlagIt() {
    // A genuinely-unknown 3-uppercase code must still be captured into the NAT
    // slot (never swept into firstName) so the validator can fail loud on it.
    Line line =
        lineOf(
            "10",
            "Surname",
            "Filler",
            "Filler",
            "Given",
            "XXX",
            "11093635",
            "BB",
            "TC Verein",
            "59,0");

    ParsedRow row = new RowParser().parse(line);

    assertThat(row.nationality()).isEqualTo("XXX");
    assertThat(row.firstName()).isEqualTo("Given");
    assertThat(row.firstName()).doesNotContain("XXX");
    assertThat(row.lastName()).doesNotContain("XXX");
  }

  private Line lineOf(String... tokens) {
    List<Word> words = new java.util.ArrayList<>();
    float x = 60f;
    for (String token : tokens) {
      words.add(new Word(token, x, x + token.length() * 6f, 100f, 3));
      x += token.length() * 6f + 4f;
    }
    return new Line(3, 100f, words);
  }
}
