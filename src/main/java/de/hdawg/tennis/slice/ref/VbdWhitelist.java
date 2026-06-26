package de.hdawg.tennis.slice.ref;

import java.util.Set;

/**
 * Whitelist of the official two- or three-letter VBD (Verband, i.e. regional tennis association)
 * codes as they appear in DTB ranking PDFs.
 *
 * <p>The parser captures the token immediately right of the DTB ID as the association code verbatim
 * and leaves recognition to this whitelist. An unrecognised code fails validation with an ERROR so
 * new VBD codes surface explicitly rather than being silently accepted.
 */
public final class VbdWhitelist {

  private static final Set<String> CODES =
      Set.of(
          "TVN", "BAD", "HAM", "WTV", "BB", "BTV", "TVM", "RPF", "SLH", "WTB", "STV", "HTV", "TNB",
          "STB", "TTV", "TSA", "TMV");

  private VbdWhitelist() {}

  /**
   * Returns {@code true} if {@code code} is a known VBD association code.
   *
   * @param code the token to look up, as extracted from the PDF
   */
  public static boolean isKnown(String code) {
    return CODES.contains(code);
  }
}
