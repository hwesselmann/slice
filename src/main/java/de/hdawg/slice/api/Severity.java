package de.hdawg.slice.api;

/**
 * Severity level of a {@link ParseIssue} encountered during ranking list parsing.
 *
 * <p>In {@link de.hdawg.slice.validate.ParseMode#STRICT STRICT} mode any {@code ERROR} causes an
 * {@link IllegalStateException}. In {@code LENIENT} mode the affected row or metadata field is
 * skipped and the issue is collected in the {@link ParseResult}.
 */
public enum Severity {
  /**
   * A non-fatal anomaly (e.g. missing {@code validFrom} date or a possible encoding problem). The
   * associated row or field is still included in the result.
   */
  WARNING,
  /**
   * A fatal parse or validation failure (e.g. unknown VBD association code, unparseable row). The
   * associated row is excluded from the {@link RankingList#entries() entries} in LENIENT mode.
   */
  ERROR
}
