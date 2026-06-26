package de.hdawg.slice.api;

import java.math.BigDecimal;

/**
 * A player's ranking score.
 *
 * <p>Most players carry a numeric {@link Points} value. The remaining variants cover the cases
 * where no DTB points number appears in the source PDF:
 *
 * <ul>
 *   <li>{@link International} — senior player ranked via ATP/WTA instead of DTB points
 *   <li>{@link ProtectedRanking} — player retains their ranking position under a protected ranking
 *       (source PDF token: {@code PR}); occurs when a player is absent but keeps their position
 *   <li>{@link Projected} — player is projected into the ranking despite insufficient tournament
 *       results (source PDF token: {@code Einst.}); rare
 * </ul>
 */
public sealed interface Score {

  /**
   * A numeric DTB points value, formatted as a decimal with exactly one fractional digit (e.g.
   * {@code 1234,5} in the source PDF, stored as {@link BigDecimal}).
   */
  record Points(BigDecimal value) implements Score {}

  /**
   * Indicates that the player is ranked by the ATP (men) or WTA (women) international ranking and
   * therefore has no DTB points entry in the ranking list.
   */
  enum International implements Score {
    /** ATP-ranked player (Herren discipline). */
    ATP,
    /** WTA-ranked player (Damen discipline). */
    WTA
  }

  /**
   * Protected ranking ({@code PR}): the player retains their ranking position without having
   * accumulated the required DTB points (e.g. while studying or living abroad).
   */
  enum ProtectedRanking implements Score {
    INSTANCE
  }

  /**
   * Projected entry ({@code Einst.}): the player is classified into a ranking position despite not
   * meeting the standard number-of-wins requirement.
   */
  enum Projected implements Score {
    INSTANCE
  }
}
