package de.hdawg.tennis.slice.validate;

import static org.assertj.core.api.Assertions.assertThat;

import de.hdawg.tennis.slice.api.ParseIssue;
import de.hdawg.tennis.slice.api.Severity;
import org.junit.jupiter.api.Test;

class ParseReportBuilderTest {

  @Test
  void hasErrorsReturnsTrueWhenErrorPresent() {
    ParseReportBuilder builder = new ParseReportBuilder();
    builder.add(new ParseIssue(Severity.ERROR, 1, "raw", "test error"));

    assertThat(builder.hasErrors()).isTrue();
  }

  @Test
  void hasErrorsReturnsFalseWithOnlyWarnings() {
    ParseReportBuilder builder = new ParseReportBuilder();
    builder.add(new ParseIssue(Severity.WARNING, 1, "raw", "test warning"));

    assertThat(builder.hasErrors()).isFalse();
  }
}
