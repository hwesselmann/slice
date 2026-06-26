package de.hdawg.tennis.slice;

import static org.assertj.core.api.Assertions.assertThat;

import de.hdawg.tennis.slice.api.ParseResult;
import de.hdawg.tennis.slice.api.RankingEntry;
import de.hdawg.tennis.slice.api.RankingList;
import de.hdawg.tennis.slice.api.Severity;
import de.hdawg.tennis.slice.api.YearRange;
import de.hdawg.tennis.slice.validate.ParseMode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SliceGoldenTest {

  private static final Path PDF_DIR = Path.of("src/test/resources/pdf");
  private static final Path SNAPSHOT_DIR = Path.of("src/test/resources/snapshots");

  @ParameterizedTest
  @ValueSource(strings = {"herren", "damen", "junioren", "juniorinnen"})
  void parsesFixtureWithoutErrorsAndMatchesSnapshot(String name) throws IOException {
    Path pdf = PDF_DIR.resolve(name + ".pdf");
    if (Files.notExists(pdf)) {
      org.junit.jupiter.api.Assumptions.abort(
          "Missing fixture %s — copy the real DTB PDF there to enable this test".formatted(pdf));
    }

    ParseResult result = Slice.builder().mode(ParseMode.LENIENT).build().parse(pdf);

    assertThat(result.issues())
        .as("issues for %s", name)
        .filteredOn(issue -> issue.severity() == Severity.ERROR)
        .isEmpty();

    assertMetadata(result.list(), name);

    String actual = snapshotOf(result.list().entries());
    Path snapshotFile = SNAPSHOT_DIR.resolve(name + ".snapshot.txt");

    if (Files.notExists(snapshotFile)) {
      Files.createDirectories(SNAPSHOT_DIR);
      Files.writeString(snapshotFile, actual, StandardOpenOption.CREATE_NEW);
      org.junit.jupiter.api.Assertions.fail(
          "Recorded new snapshot at %s — re-run the test to verify it now matches"
              .formatted(snapshotFile));
    }

    String expected = Files.readString(snapshotFile);
    assertThat(actual).isEqualTo(expected);
  }

  private void assertMetadata(RankingList list, String fixtureName) {
    LocalDate expectedStichtag = LocalDate.of(2025, Month.DECEMBER, 31);
    LocalDate expectedValidFrom = LocalDate.of(2026, Month.JANUARY, 9);

    assertThat(list.stichtag()).as("stichtag for %s", fixtureName).isEqualTo(expectedStichtag);
    assertThat(list.validFrom()).as("validFrom for %s", fixtureName).isEqualTo(expectedValidFrom);

    switch (fixtureName) {
      case "herren" -> {
        assertThat(list.pointsThreshold()).as("pointsThreshold for herren").isEqualTo(603);
        assertThat(list.birthYears()).as("birthYears for herren").isNull();
      }
      case "damen" -> {
        assertThat(list.pointsThreshold()).as("pointsThreshold for damen").isEqualTo(580);
        assertThat(list.birthYears()).as("birthYears for damen").isNull();
      }
      case "junioren", "juniorinnen" -> {
        assertThat(list.pointsThreshold()).as("pointsThreshold for %s", fixtureName).isNull();
        assertThat(list.birthYears())
            .as("birthYears for %s", fixtureName)
            .isEqualTo(new YearRange(2008, 2014));
      }
      default -> throw new IllegalArgumentException("Unknown fixture: " + fixtureName);
    }
  }

  private String snapshotOf(List<RankingEntry> entries) {
    StringBuilder sb = new StringBuilder();
    for (RankingEntry entry : entries) {
      sb.append(entry.rank())
          .append('\t')
          .append(entry.lastName())
          .append('\t')
          .append(entry.firstName())
          .append('\t')
          .append(entry.nationality())
          .append('\t')
          .append(entry.dtbId())
          .append('\t')
          .append(entry.association())
          .append('\t')
          .append(entry.club())
          .append('\t')
          .append(entry.score())
          .append('\n');
    }
    return sb.toString();
  }
}
