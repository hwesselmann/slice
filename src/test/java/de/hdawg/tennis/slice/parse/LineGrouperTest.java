package de.hdawg.tennis.slice.parse;

import static org.assertj.core.api.Assertions.assertThat;

import de.hdawg.tennis.slice.pdf.Word;
import java.util.List;
import org.junit.jupiter.api.Test;

class LineGrouperTest {

  @Test
  void groupsWordsWithinYToleranceIntoOneLine() {
    List<Word> words =
        List.of(
            new Word("84", 60f, 73f, 100.0f, 3),
            new Word("Mustermann", 84.5f, 150f, 100.2f, 3),
            new Word("Erika", 196.2f, 220f, 100.1f, 3));

    List<Line> lines = new LineGrouper().group(words);

    assertThat(lines).hasSize(1);
    assertThat(lines.get(0).words()).hasSize(3);
    assertThat(lines.get(0).rawText()).isEqualTo("84 Mustermann Erika");
  }

  @Test
  void separatesWordsOnDifferentYIntoDifferentLines() {
    List<Word> words =
        List.of(new Word("84", 60f, 73f, 100.0f, 3), new Word("85", 60f, 73f, 113.0f, 3));

    List<Line> lines = new LineGrouper().group(words);

    assertThat(lines).hasSize(2);
  }

  @Test
  void mergesWrappedFragmentsWithinTinyYDeltaOnSamePage() {
    List<Word> words =
        List.of(
            new Word("Sportvereinigung", 386f, 470f, 100.0f, 3),
            new Word("Heepen", 386f, 430f, 100.4f, 3));

    List<Line> lines = new LineGrouper().group(words);

    assertThat(lines).hasSize(1);
  }

  @Test
  void dropsPageLegendBoilerplateInsteadOfMergingItIntoADataRow() {
    // Reproduces the real DTB PDF layout: a top-right legend box ("Reihenfolge" / "gemäß
    // ATP-" / "Rangliste" / "Stichtag" / a cutoff date) whose line pitch drifts against the
    // table's row pitch, so a legend word can land within Y_TOLERANCE of a data row's y and
    // would otherwise get appended to that row's club name (see LineGrouper's javadoc-style
    // comment on CUTOFF_DATE for the full story; observed corrupting real rows in herren.pdf
    // and damen.pdf during golden-test verification).
    List<Word> words =
        List.of(
            new Word("2", 72.72f, 76.85f, 166.10f, 3),
            new Word("Altmaier", 84.48f, 111.93f, 166.10f, 3),
            new Word("Daniel", 196.22f, 216.92f, 166.10f, 3),
            new Word("GER", 307.97f, 321.90f, 166.10f, 3),
            new Word("19850056", 329.23f, 361.92f, 166.10f, 3),
            new Word("BB", 364.99f, 373.88f, 166.10f, 3),
            new Word("LTTC Rot-Weiß Berlin", 386.11f, 453.25f, 166.10f, 3),
            new Word("gemäß ATP-", 540.00f, 573.11f, 165.50f, 3));

    List<Line> lines = new LineGrouper().group(words);

    assertThat(lines).hasSize(1);
    assertThat(lines.get(0).rawText())
        .isEqualTo("2 Altmaier Daniel GER 19850056 BB LTTC Rot-Weiß Berlin");
  }

  @Test
  void dropsCutoffDateLegendBoilerplate() {
    List<Word> words =
        List.of(
            new Word("5", 72.72f, 76.85f, 197.06f, 3),
            new Word("Korpatsch", 84.48f, 117.50f, 197.06f, 3),
            new Word("Tamara", 196.22f, 221.65f, 197.06f, 3),
            new Word("GER", 307.97f, 321.90f, 197.06f, 3),
            new Word("29500169", 329.23f, 361.92f, 197.06f, 3),
            new Word("HAM", 364.99f, 380.53f, 197.06f, 3),
            new Word("Der Club an der Alster", 386.11f, 460.05f, 197.06f, 3),
            new Word("15.12.2025", 541.32f, 574.20f, 197.30f, 3));

    List<Line> lines = new LineGrouper().group(words);

    assertThat(lines).hasSize(1);
    assertThat(lines.get(0).rawText())
        .isEqualTo("5 Korpatsch Tamara GER 29500169 HAM Der Club an der Alster");
  }
}
