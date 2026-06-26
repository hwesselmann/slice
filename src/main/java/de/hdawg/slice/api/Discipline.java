package de.hdawg.slice.api;

/**
 * The four tennis discipline categories used in official DTB ranking lists.
 *
 * <p>Discipline determines which metadata fields are present ({@link RankingList#birthYears()} for
 * juniors, {@link RankingList#pointsThreshold()} for seniors), which {@link Score} types are valid,
 * and how the validator applies the points-empty rule.
 */
public enum Discipline {
  /** Men's singles ranking (Herren). */
  HERREN,
  /** Women's singles ranking (Damen). */
  DAMEN,
  /** Boys' singles ranking (Junioren). */
  JUNIOREN,
  /** Girls' singles ranking (Juniorinnen). */
  JUNIORINNEN;

  /**
   * Returns {@code true} if this discipline is a junior category ({@link #JUNIOREN} or {@link
   * #JUNIORINNEN}).
   */
  public boolean isJunior() {
    return this == JUNIOREN || this == JUNIORINNEN;
  }
}
