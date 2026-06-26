package de.hdawg.tennis.slice.metadata;

import static org.assertj.core.api.Assertions.assertThat;

import de.hdawg.tennis.slice.api.Discipline;
import de.hdawg.tennis.slice.api.Severity;
import de.hdawg.tennis.slice.api.YearRange;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;

class MetadataExtractorTest {

  private static final String HERREN_FULL_TEXT =
      """
            Deutsche Rangliste 2025 – Herren
            Stichtag 31.12.2025
            Gültig ab 09.01.2026
            Datum: 08.01.2026
            Deutsche Rangliste bis 603 Punkte
            \s
            Rang Name                  Nat    DTB-ID  VBD Club              Punkte
            1    Zverev Alexander      GER    01234567 TVN TC Hamburg       3500.0
            """;

  private static final String DAMEN_FULL_TEXT =
      """
            Deutsche Rangliste 2025 – Damen
            Stichtag 31.12.2025
            Gültig ab 09.01.2026
            Datum: 08.01.2026
            Deutsche Rangliste bis 580 Punkte
            \s
            Rang Name                  Nat    DTB-ID  VBD Club              Punkte
            1    Siegemund Laura       GER    02345678 WTF TC Stuttgart     2100.0
            """;

  private static final String JUNIOREN_FULL_TEXT =
      """
            Deutsche Rangliste 2025 – Junioren U18 - U12
            Junioren U18 - U12  Jg. 2008 - 2014
            (Jg. 2008-2014)
            Stichtag 31.12.2025
            Gültig ab 09.01.2026
            Datum: 08.01.2026
            \s
            Rang Name                  Nat    DTB-ID  VBD Club              Punkte
            1    Mustermann Max        GER    03456789 TVN TC Berlin         500.0
            """;

  private static final String JUNIORINNEN_FULL_TEXT =
      """
            Deutsche Rangliste 2025 – Juniorinnen U18 - U12
            Junioren U18 - U12  Jg. 2008 - 2014
            (Jg. 2008-2014)
            Stichtag 31.12.2025
            Gültig ab 09.01.2026
            Datum: 08.01.2026
            \s
            Rang Name                  Nat    DTB-ID  VBD Club              Punkte
            1    Musterfrau Maria      GER    04567890 TVN TC Hamburg        450.0
            """;

  // This text simulates the legend trap: the ATP/WTA legend box prints
  // "Stichtag" on one line and the date on the NEXT line (newline-separated).
  // The pattern must NOT pick this up. The real stichtag IS on the same line.
  private static final String LEGEND_TRAP_TEXT =
      """
            Deutsche Rangliste 2025 – Herren
            Stichtag 31.12.2025
            Gültig ab 09.01.2026
            Deutsche Rangliste bis 603 Punkte
            ATP-Referenzrangliste
            Stichtag
            15.12.2025
            Ende der Legende
            """;

  @Test
  void extractsHerrenMetadata() {
    MetadataExtractor.Result result =
        new MetadataExtractor().extract(HERREN_FULL_TEXT, Discipline.HERREN);

    assertThat(result.issues()).isEmpty();
    assertThat(result.metadata().stichtag()).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 31));
    assertThat(result.metadata().validFrom()).isEqualTo(LocalDate.of(2026, Month.JANUARY, 9));
    assertThat(result.metadata().pointsThreshold()).isEqualTo(603);
    assertThat(result.metadata().birthYears()).isNull();
  }

  @Test
  void extractsDamenMetadata() {
    MetadataExtractor.Result result =
        new MetadataExtractor().extract(DAMEN_FULL_TEXT, Discipline.DAMEN);

    assertThat(result.issues()).isEmpty();
    assertThat(result.metadata().stichtag()).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 31));
    assertThat(result.metadata().validFrom()).isEqualTo(LocalDate.of(2026, Month.JANUARY, 9));
    assertThat(result.metadata().pointsThreshold()).isEqualTo(580);
    assertThat(result.metadata().birthYears()).isNull();
  }

  @Test
  void extractsJuniorenMetadata() {
    MetadataExtractor.Result result =
        new MetadataExtractor().extract(JUNIOREN_FULL_TEXT, Discipline.JUNIOREN);

    assertThat(result.issues()).isEmpty();
    assertThat(result.metadata().stichtag()).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 31));
    assertThat(result.metadata().validFrom()).isEqualTo(LocalDate.of(2026, Month.JANUARY, 9));
    assertThat(result.metadata().pointsThreshold()).isNull();
    assertThat(result.metadata().birthYears()).isEqualTo(new YearRange(2008, 2014));
  }

  @Test
  void extractsJuniorinnenMetadata() {
    MetadataExtractor.Result result =
        new MetadataExtractor().extract(JUNIORINNEN_FULL_TEXT, Discipline.JUNIORINNEN);

    assertThat(result.issues()).isEmpty();
    assertThat(result.metadata().stichtag()).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 31));
    assertThat(result.metadata().validFrom()).isEqualTo(LocalDate.of(2026, Month.JANUARY, 9));
    assertThat(result.metadata().pointsThreshold()).isNull();
    assertThat(result.metadata().birthYears()).isEqualTo(new YearRange(2008, 2014));
  }

  @Test
  void missingStichtagProducesError() {
    String text = "Gültig ab 09.01.2026\nDeutsche Rangliste bis 603 Punkte\n";
    MetadataExtractor.Result result = new MetadataExtractor().extract(text, Discipline.HERREN);

    assertThat(result.metadata().stichtag()).isNull();
    assertThat(result.issues())
        .hasSize(1)
        .anyMatch(i -> i.severity() == Severity.ERROR && i.message().contains("Stichtag"));
  }

  @Test
  void missingGueltigAbProducesWarningNotError() {
    String text = "Stichtag 31.12.2025\nDeutsche Rangliste bis 603 Punkte\n";
    MetadataExtractor.Result result = new MetadataExtractor().extract(text, Discipline.HERREN);

    assertThat(result.metadata().validFrom()).isNull();
    assertThat(result.issues())
        .hasSize(1)
        .anyMatch(i -> i.severity() == Severity.WARNING && i.message().contains("Gültig ab"));
  }

  @Test
  void legendTrapIsNotPickedUpAsStichtag() {
    MetadataExtractor.Result result =
        new MetadataExtractor().extract(LEGEND_TRAP_TEXT, Discipline.HERREN);

    // The real stichtag is 31.12.2025; the legend's 15.12.2025 (newline-separated) must be ignored
    assertThat(result.metadata().stichtag()).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 31));
    assertThat(result.issues()).isEmpty();
  }

  @Test
  void noPointsThresholdIssueForJuniors() {
    // Juniors don't have a points threshold — no WARNING should be generated
    MetadataExtractor.Result result =
        new MetadataExtractor().extract(JUNIOREN_FULL_TEXT, Discipline.JUNIOREN);

    assertThat(result.metadata().pointsThreshold()).isNull();
    assertThat(result.issues()).noneMatch(i -> i.message().contains("Punkte"));
  }

  @Test
  void noBirthYearsIssueForHerren() {
    // Herren don't have birth years — no WARNING should be generated
    MetadataExtractor.Result result =
        new MetadataExtractor().extract(HERREN_FULL_TEXT, Discipline.HERREN);

    assertThat(result.metadata().birthYears()).isNull();
    assertThat(result.issues()).noneMatch(i -> i.message().contains("Jahrgang"));
  }

  @Test
  void missingPointsThresholdProducesWarningForHerren() {
    String text = "Stichtag 31.12.2025\nGültig ab 09.01.2026\n";
    MetadataExtractor.Result result = new MetadataExtractor().extract(text, Discipline.HERREN);

    assertThat(result.metadata().pointsThreshold()).isNull();
    assertThat(result.issues())
        .anyMatch(i -> i.severity() == Severity.WARNING && i.message().contains("Punkte"));
  }

  @Test
  void missingBirthYearsProducesWarningForJuniors() {
    String text = "Stichtag 31.12.2025\nGültig ab 09.01.2026\n";
    MetadataExtractor.Result result = new MetadataExtractor().extract(text, Discipline.JUNIOREN);

    assertThat(result.metadata().birthYears()).isNull();
    assertThat(result.issues())
        .anyMatch(i -> i.severity() == Severity.WARNING && i.message().contains("Jg."));
  }
}
