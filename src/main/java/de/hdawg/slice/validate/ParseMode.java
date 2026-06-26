package de.hdawg.slice.validate;

/**
 * Controls how the parser responds to errors encountered during parsing and validation.
 *
 * <p>Pass to {@link de.hdawg.slice.Slice.Builder#mode(ParseMode) Slice.Builder.mode()} before
 * building a {@link de.hdawg.slice.Slice} instance.
 */
public enum ParseMode {
  /**
   * Any {@link de.hdawg.slice.api.Severity#ERROR ERROR}-level issue immediately throws an {@link
   * IllegalStateException}, aborting the parse.
   */
  STRICT,
  /**
   * Errors are collected as {@link de.hdawg.slice.api.ParseIssue} entries in the {@link
   * de.hdawg.slice.api.ParseResult} and the affected row or metadata field is skipped. Parsing
   * continues for all remaining rows.
   */
  LENIENT
}
