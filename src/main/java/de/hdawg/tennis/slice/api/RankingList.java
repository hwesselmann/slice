package de.hdawg.tennis.slice.api;

import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * The complete ranking list for a single DTB discipline, including all parsed metadata and player
 * entries.
 *
 * <p>If metadata could not be extracted from the PDF (e.g. no "Stichtag" line was found), the
 * corresponding date fields fall back to {@link LocalDate#MIN} and a {@link ParseIssue} with {@link
 * Severity#ERROR} is included in the {@link ParseResult}.
 *
 * @param discipline the discipline this ranking list applies to
 * @param stichtag the reference date ("Stichtag") of the ranking, i.e. the date up to which results
 *     were counted; falls back to {@link LocalDate#MIN} if not found in the PDF
 * @param validFrom the date from which this ranking is in effect ("Gültig ab"); falls back to
 *     {@link LocalDate#MIN} if not found in the PDF
 * @param pointsThreshold the maximum DTB points value that qualifies a player for inclusion in this
 *     list ("bis N Punkte"), or {@code null} for junior disciplines where no threshold is published
 * @param birthYears the eligible birth-year range for junior disciplines ("Jg. YYYY–YYYY"), or
 *     {@code null} for senior disciplines
 * @param entries the parsed player entries in ranking order; always an unmodifiable list
 */
public record RankingList(
    Discipline discipline,
    LocalDate stichtag,
    LocalDate validFrom,
    @Nullable Integer pointsThreshold,
    @Nullable YearRange birthYears,
    List<RankingEntry> entries) {
  public RankingList {
    entries = List.copyOf(entries);
  }
}
