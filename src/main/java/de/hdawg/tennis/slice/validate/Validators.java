package de.hdawg.tennis.slice.validate;

import de.hdawg.tennis.slice.api.Discipline;
import de.hdawg.tennis.slice.api.ParseIssue;
import de.hdawg.tennis.slice.api.Severity;
import de.hdawg.tennis.slice.parse.ParsedRow;
import de.hdawg.tennis.slice.ref.NationalityCodes;
import de.hdawg.tennis.slice.ref.VbdWhitelist;
import java.util.ArrayList;
import java.util.List;

/** Applies all per-row and global validation rules to parsed ranking data. */
public final class Validators {

  /**
   * Validates a single parsed row against all applicable rules (association code, nationality code,
   * name completeness, encoding quality, and points presence).
   *
   * @param row the parsed row to validate
   * @param discipline the discipline of the ranking list, used to determine points-empty rules
   * @param page the 1-based PDF page number of the row, included in any emitted {@link ParseIssue}
   * @param rawLine the original text of the row as extracted from the PDF, included in any emitted
   *     {@link ParseIssue}
   * @return an unmodifiable list of all issues found; empty if the row is fully valid
   */
  public List<ParseIssue> validateRow(
      ParsedRow row, Discipline discipline, int page, String rawLine) {
    List<ParseIssue> issues = new ArrayList<>();
    issues.addAll(validateAssociation(row, page, rawLine));
    issues.addAll(validateNationality(row, page, rawLine));
    issues.addAll(validateName(row, page, rawLine));
    issues.addAll(validateEncoding(row, page, rawLine));
    issues.addAll(validatePoints(row, discipline, page, rawLine));
    return issues;
  }

  /**
   * Validates cross-row invariants for the complete set of parsed rows (e.g. rank uniqueness,
   * ordering). Currently a no-op placeholder; returns an empty list.
   *
   * @param rows all parsed rows for the ranking list
   * @param discipline the discipline of the ranking list
   * @return an unmodifiable list of cross-row issues; currently always empty
   */
  public List<ParseIssue> validateGlobal(List<ParsedRow> rows, Discipline discipline) {
    return List.of();
  }

  private List<ParseIssue> validateAssociation(ParsedRow row, int page, String rawLine) {
    if (row.association() == null || !VbdWhitelist.isKnown(row.association())) {
      return List.of(error(page, rawLine, "Unbekannter oder fehlender VBD-Code"));
    }
    return List.of();
  }

  private List<ParseIssue> validateNationality(ParsedRow row, int page, String rawLine) {
    if (row.nationality() != null && !NationalityCodes.isKnown(row.nationality())) {
      return List.of(error(page, rawLine, "Unbekannter NAT-Code: " + row.nationality()));
    }
    return List.of();
  }

  private List<ParseIssue> validateName(ParsedRow row, int page, String rawLine) {
    if (row.lastName().isBlank() || row.firstName().isBlank()) {
      return List.of(error(page, rawLine, "Name oder Vorname leer"));
    }
    return List.of();
  }

  private List<ParseIssue> validateEncoding(ParsedRow row, int page, String rawLine) {
    if (hasReplacementChar(row.lastName())
        || hasReplacementChar(row.firstName())
        || hasReplacementChar(row.club())) {
      return List.of(warning(page, rawLine, "Mögliches Encoding-Problem (Replacement-Zeichen)"));
    }
    return List.of();
  }

  private List<ParseIssue> validatePoints(
      ParsedRow row, Discipline discipline, int page, String rawLine) {
    if (row.points() != null || row.scoreToken() != null) {
      return List.of();
    }
    // Herren/Damen: null score means the player is ranked via ATP/WTA; the variable number
    // of such players at the top of the DTB list makes a rank threshold impractical.
    // Junioren/Juniorinnen must have a numeric DTB score or a recognised token (PR, Einst.).
    boolean allowedEmpty =
        switch (discipline) {
          case HERREN, DAMEN -> true;
          case JUNIOREN, JUNIORINNEN -> false;
        };
    if (!allowedEmpty) {
      return List.of(
          error(page, rawLine, "Punkte-Zelle leer (Junioren/Juniorinnen benötigen DTB-Punkte)"));
    }
    return List.of();
  }

  // PDFBox emits the Unicode replacement character U+FFFD for glyphs it cannot map.
  // A literal ASCII '?' also occurs as a genuine baked-in glyph in one source PDF;
  // both are treated as a possible encoding problem (documented WARNING).
  private boolean hasReplacementChar(String value) {
    return value.indexOf('?') >= 0 || value.indexOf('�') >= 0;
  }

  private ParseIssue error(int page, String rawLine, String message) {
    return new ParseIssue(Severity.ERROR, page, rawLine, message);
  }

  private ParseIssue warning(int page, String rawLine, String message) {
    return new ParseIssue(Severity.WARNING, page, rawLine, message);
  }
}
