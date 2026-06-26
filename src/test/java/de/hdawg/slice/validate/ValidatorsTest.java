package de.hdawg.slice.validate;

import static org.assertj.core.api.Assertions.assertThat;

import de.hdawg.slice.api.Discipline;
import de.hdawg.slice.api.Severity;
import de.hdawg.slice.parse.ParsedRow;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidatorsTest {

  @Test
  void flagsMissingAssociationAsError() {
    ParsedRow row =
        new ParsedRow(
            1,
            false,
            "Mustermann",
            "Erika",
            "GER",
            "12345678",
            null,
            "TC Berlin",
            new BigDecimal("100.0"),
            null);

    List<de.hdawg.slice.api.ParseIssue> issues =
        new Validators().validateRow(row, Discipline.HERREN, 3, "raw");

    assertThat(issues)
        .anyMatch(issue -> issue.severity() == Severity.ERROR && issue.message().contains("VBD"));
  }

  @Test
  void allowsNullPointsForAnyHerrenRank() {
    Validators validators = new Validators();
    ParsedRow rank1 =
        new ParsedRow(
            1, false, "Mustermann", "Erika", "GER", "12345678", "TVN", "TC Berlin", null, null);
    ParsedRow rank50 =
        new ParsedRow(
            50, false, "Mustermann", "Erika", "GER", "12345678", "TVN", "TC Berlin", null, null);

    assertThat(validators.validateRow(rank1, Discipline.HERREN, 3, "raw"))
        .noneMatch(issue -> issue.severity() == Severity.ERROR);
    assertThat(validators.validateRow(rank50, Discipline.HERREN, 3, "raw"))
        .noneMatch(issue -> issue.severity() == Severity.ERROR);
  }

  @Test
  void flagsUnknownNationalityCodeAsError() {
    ParsedRow row =
        new ParsedRow(
            1,
            false,
            "Mustermann",
            "Erika",
            "XXX",
            "12345678",
            "TVN",
            "TC Berlin",
            new BigDecimal("100.0"),
            null);

    List<de.hdawg.slice.api.ParseIssue> issues =
        new Validators().validateRow(row, Discipline.HERREN, 3, "raw");

    assertThat(issues)
        .anyMatch(
            issue -> issue.severity() == Severity.ERROR && issue.message().contains("NAT-Code"));
  }

  @Test
  void warnsOnUnicodeReplacementCharacterInName() {
    ParsedRow row =
        new ParsedRow(
            1,
            false,
            "Mu�ller",
            "Erika",
            "GER",
            "12345678",
            "TVN",
            "TC Berlin",
            new BigDecimal("100.0"),
            null);

    List<de.hdawg.slice.api.ParseIssue> issues =
        new Validators().validateRow(row, Discipline.HERREN, 3, "raw");

    assertThat(issues)
        .anyMatch(
            issue ->
                issue.severity() == Severity.WARNING
                    && issue.message().contains("Replacement-Zeichen"));
  }

  @Test
  void juniorRowsMayNeverHaveEmptyPoints() {
    ParsedRow row =
        new ParsedRow(
            1, false, "Mustermann", "Erika", "GER", "12345678", "TVN", "TC Berlin", null, null);

    List<de.hdawg.slice.api.ParseIssue> issues =
        new Validators().validateRow(row, Discipline.JUNIOREN, 3, "raw");

    assertThat(issues)
        .anyMatch(
            issue -> issue.severity() == Severity.ERROR && issue.message().contains("Punkte"));
  }

  @Test
  void allowsNullPointsForAnyDamenRank() {
    Validators validators = new Validators();
    ParsedRow rank1 =
        new ParsedRow(
            1, false, "Mustermann", "Erika", "GER", "12345678", "TVN", "TC Berlin", null, null);
    ParsedRow rank50 =
        new ParsedRow(
            50, false, "Mustermann", "Erika", "GER", "12345678", "TVN", "TC Berlin", null, null);

    assertThat(validators.validateRow(rank1, Discipline.DAMEN, 3, "raw"))
        .noneMatch(issue -> issue.severity() == Severity.ERROR);
    assertThat(validators.validateRow(rank50, Discipline.DAMEN, 3, "raw"))
        .noneMatch(issue -> issue.severity() == Severity.ERROR);
  }

  @Test
  void flagsBlankLastNameAsError() {
    ParsedRow row =
        new ParsedRow(
            1,
            false,
            "",
            "Erika",
            "GER",
            "12345678",
            "TVN",
            "TC Berlin",
            new BigDecimal("100.0"),
            null);

    List<de.hdawg.slice.api.ParseIssue> issues =
        new Validators().validateRow(row, Discipline.HERREN, 3, "raw");

    assertThat(issues).anyMatch(issue -> issue.severity() == Severity.ERROR);
  }

  @Test
  void acceptsProtectedRankingAndProjectedForJuniors() {
    Validators validators = new Validators();
    ParsedRow pr =
        new ParsedRow(
            5, false, "Mustermann", "Erika", "GER", "12345678", "TVN", "TC Berlin", null, "PR");
    ParsedRow einst =
        new ParsedRow(
            10,
            false,
            "Mustermann",
            "Erika",
            "GER",
            "12345678",
            "TVN",
            "TC Berlin",
            null,
            "Einst.");

    assertThat(validators.validateRow(pr, Discipline.JUNIOREN, 3, "raw"))
        .noneMatch(issue -> issue.severity() == Severity.ERROR);
    assertThat(validators.validateRow(einst, Discipline.JUNIORINNEN, 3, "raw"))
        .noneMatch(issue -> issue.severity() == Severity.ERROR);
  }
}
