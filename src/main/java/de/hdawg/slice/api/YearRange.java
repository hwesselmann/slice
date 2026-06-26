package de.hdawg.slice.api;

/**
 * A closed birth-year range used to describe the eligible age group for junior disciplines.
 *
 * @param from the first eligible birth year (inclusive)
 * @param to the last eligible birth year (inclusive); must be &gt;= {@code from}
 */
public record YearRange(int from, int to) {
  public YearRange {
    if (from > to) {
      throw new IllegalArgumentException("from (%d) must be <= to (%d)".formatted(from, to));
    }
  }

  /**
   * Returns {@code true} if {@code year} falls within this range, i.e. {@code from <= year <= to}.
   */
  public boolean contains(int year) {
    return year >= from && year <= to;
  }
}
