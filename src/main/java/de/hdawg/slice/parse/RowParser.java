package de.hdawg.slice.parse;

import de.hdawg.slice.pdf.Word;
import de.hdawg.slice.ref.VbdWhitelist;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Parses a single data {@link Line} into a {@link ParsedRow} by anchoring on the 8-digit DTB ID and
 * reading all other fields relative to it.
 *
 * <p>Column layout (left to right): rank [A] lastName firstName [NAT] dtbId [association] club
 * [points]. Fields in brackets are optional. Name tokens are split into last/first name by the
 * x-coordinate threshold defined in {@link ColumnTemplate}.
 */
public final class RowParser {

  private static final Pattern EIGHT_DIGIT_ID = Pattern.compile("\\d{8}");
  private static final Pattern POINTS = Pattern.compile("\\d{1,6},\\d");

  /** Raw score tokens that appear in the points column instead of a numeric value. */
  private static final java.util.Set<String> SPECIAL_SCORE_TOKENS =
      java.util.Set.of("PR", "Einst.");

  /**
   * The nationality column holds a 3-letter uppercase country code (IOC/DOSB, e.g. GER, SUI, NED).
   * Real name tokens are mixed-case and never match this, so any token immediately left of the id
   * matching this pattern is treated as a NAT-code candidate and captured verbatim — recognition
   * (whitelist check) is the validator's job, so genuinely-unknown codes fail loud instead of being
   * silently swept into the name region.
   */
  private static final Pattern NAT_CODE = Pattern.compile("^[A-Z]{3}$");

  /**
   * Parses the given line into a {@link ParsedRow}.
   *
   * @param line a data line containing exactly one 8-digit DTB ID word
   * @return the parsed row; never {@code null}
   * @throws IllegalArgumentException if no 8-digit ID token is found in the line
   * @throws RuntimeException (any subtype) if the line is otherwise malformed; callers should catch
   *     broadly and convert to a {@link de.hdawg.slice.api.ParseIssue}
   */
  public ParsedRow parse(Line line) {
    List<Word> words = line.words();

    int idIndex = indexOfId(words);
    String dtbId = words.get(idIndex).text();

    String association = findAssociationCode(words, idIndex);
    String nationality = findNationalityCode(words, idIndex);
    BigDecimal points = findPoints(words);
    String scoreToken = points == null ? findScoreToken(words) : null;

    boolean hasTrailingScore = (points != null || scoreToken != null);
    int clubEnd = hasTrailingScore ? words.size() - 1 : words.size();
    int clubStart = idIndex + (association != null ? 2 : 1);
    String club = String.join(" ", textsOf(words.subList(clubStart, clubEnd)));

    int nameRegionEnd = nationality != null ? idIndex - 1 : idIndex;
    Word rankWord = words.get(0);
    boolean fromAListe = "A".equals(rankWord.text());
    int rankTokenIndex = fromAListe ? 1 : 0;
    int rank = Integer.parseInt(words.get(rankTokenIndex).text());

    String[] nameParts = buildName(words, rankTokenIndex, nameRegionEnd);

    return new ParsedRow(
        rank,
        fromAListe,
        nameParts[0],
        nameParts[1],
        nationality,
        dtbId,
        association,
        club,
        points,
        scoreToken);
  }

  private @Nullable String findAssociationCode(List<Word> words, int idIndex) {
    if (idIndex + 1 < words.size() && VbdWhitelist.isKnown(words.get(idIndex + 1).text())) {
      return words.get(idIndex + 1).text();
    }
    return null;
  }

  private @Nullable String findNationalityCode(List<Word> words, int idIndex) {
    if (idIndex - 1 >= 0 && NAT_CODE.matcher(words.get(idIndex - 1).text()).matches()) {
      return words.get(idIndex - 1).text();
    }
    return null;
  }

  private @Nullable BigDecimal findPoints(List<Word> words) {
    Word lastWord = words.get(words.size() - 1);
    if (!POINTS.matcher(lastWord.text()).matches()) {
      return null;
    }
    return new BigDecimal(lastWord.text().replace(',', '.'));
  }

  private @Nullable String findScoreToken(List<Word> words) {
    String last = words.get(words.size() - 1).text();
    return SPECIAL_SCORE_TOKENS.contains(last) ? last : null;
  }

  /** Returns a two-element array: {@code [lastName, firstName]}. */
  private String[] buildName(List<Word> words, int rankTokenIndex, int nameRegionEnd) {
    StringBuilder lastName = new StringBuilder();
    StringBuilder firstName = new StringBuilder();
    List<Word> nameTokens = words.subList(rankTokenIndex + 1, nameRegionEnd);
    for (Word token : nameTokens) {
      StringBuilder target = ColumnTemplate.isVorname(token.x0()) ? firstName : lastName;
      if (!target.isEmpty()) {
        target.append(' ');
      }
      target.append(token.text());
    }
    return new String[] {lastName.toString(), firstName.toString()};
  }

  private int indexOfId(List<Word> words) {
    for (int i = 0; i < words.size(); i++) {
      if (EIGHT_DIGIT_ID.matcher(words.get(i).text()).matches()) {
        return i;
      }
    }
    throw new IllegalArgumentException("Line has no 8-digit id: " + line(words));
  }

  private String line(List<Word> words) {
    return textsOf(words).toString();
  }

  private List<String> textsOf(List<Word> words) {
    return words.stream().map(Word::text).toList();
  }
}
