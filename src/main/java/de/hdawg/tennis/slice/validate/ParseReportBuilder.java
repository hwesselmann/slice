package de.hdawg.tennis.slice.validate;

import de.hdawg.tennis.slice.api.ParseIssue;
import de.hdawg.tennis.slice.api.Severity;
import java.util.ArrayList;
import java.util.List;

/** Accumulates {@link ParseIssue} entries during a parse run and produces an immutable snapshot. */
public final class ParseReportBuilder {

  private final List<ParseIssue> issues = new ArrayList<>();

  /**
   * Appends a single issue to this report.
   *
   * @return this builder, for chaining
   */
  public ParseReportBuilder add(ParseIssue issue) {
    issues.add(issue);
    return this;
  }

  /**
   * Appends all issues from the given list to this report.
   *
   * @return this builder, for chaining
   */
  public ParseReportBuilder addAll(List<ParseIssue> newIssues) {
    issues.addAll(newIssues);
    return this;
  }

  /**
   * Returns an unmodifiable snapshot of all accumulated issues in insertion order. May be called
   * multiple times; each call returns an independent copy.
   */
  public List<ParseIssue> build() {
    return List.copyOf(issues);
  }

  /**
   * Returns {@code true} if at least one accumulated issue has severity {@link Severity#ERROR
   * ERROR}.
   */
  public boolean hasErrors() {
    return issues.stream().anyMatch(issue -> issue.severity() == Severity.ERROR);
  }
}
