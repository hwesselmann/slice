package de.hdawg.tennis.slice.parse;

/**
 * Encodes the fixed x-coordinate layout of DTB ranking PDF columns.
 *
 * <p>The ranking table prints the last name and first name in adjacent sub-columns within the name
 * region. There is no explicit column separator in the PDF; instead the two sub-columns are
 * distinguished by a fixed x-coordinate boundary derived empirically from the four fixture PDFs.
 */
public final class ColumnTemplate {

  /**
   * X-coordinate threshold (in PDF user units) that separates the last-name from the first-name
   * sub-column.
   */
  public static final float NAME_VORNAME_BOUNDARY_X = 185f;

  private ColumnTemplate() {}

  /**
   * Returns {@code true} if the word at x-position {@code x0} belongs to the first-name (Vorname)
   * sub-column, i.e. {@code x0 >= }{@link #NAME_VORNAME_BOUNDARY_X}.
   *
   * @param x0 the left edge x-coordinate of the word in PDF user units
   */
  public static boolean isVorname(float x0) {
    return x0 >= NAME_VORNAME_BOUNDARY_X;
  }
}
