package de.hdawg.slice.metadata;

import de.hdawg.slice.api.Discipline;
import java.util.Locale;

/** Determines the {@link Discipline} of a ranking list from the first page of the PDF text. */
public final class DisciplineDetector {

  /**
   * Detects the discipline by scanning {@code fullText} (typically the first-page text) for the
   * German discipline keywords. Juniorinnen is checked before Junioren to avoid a false JUNIOREN
   * match on a string that contains "JUNIORINNEN".
   *
   * @param fullText the extracted text to search, typically from the first PDF page only
   * @return the detected {@link Discipline}
   * @throws IllegalArgumentException if none of the four discipline keywords is found
   */
  public Discipline detect(String fullText) {
    String normalized = fullText.toUpperCase(Locale.ROOT).replace(" ", "");
    if (normalized.contains("JUNIORINNEN")) {
      return Discipline.JUNIORINNEN;
    }
    if (normalized.contains("JUNIOREN")) {
      return Discipline.JUNIOREN;
    }
    if (normalized.contains("DAMEN")) {
      return Discipline.DAMEN;
    }
    if (normalized.contains("HERREN")) {
      return Discipline.HERREN;
    }
    throw new IllegalArgumentException("Could not detect discipline from PDF text");
  }
}
