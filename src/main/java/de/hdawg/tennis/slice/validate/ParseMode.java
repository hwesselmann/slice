package de.hdawg.tennis.slice.validate;

import de.hdawg.tennis.slice.Slice;
import de.hdawg.tennis.slice.api.ParseIssue;
import de.hdawg.tennis.slice.api.ParseResult;
import de.hdawg.tennis.slice.api.Severity;

/**
 * Controls how the parser responds to errors encountered during parsing and validation.
 *
 * <p>Pass to {@link Slice.Builder#mode(ParseMode) Slice.Builder.mode()} before building a {@link
 * Slice} instance.
 */
public enum ParseMode {
  /**
   * Any {@link Severity#ERROR ERROR}-level issue immediately throws an {@link
   * IllegalStateException}, aborting the parse.
   */
  STRICT,
  /**
   * Errors are collected as {@link ParseIssue} entries in the {@link ParseResult} and the affected
   * row or metadata field is skipped. Parsing continues for all remaining rows.
   */
  LENIENT
}
