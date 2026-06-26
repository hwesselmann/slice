package de.hdawg.tennis.slice.api;

import org.jspecify.annotations.Nullable;

/**
 * A single player entry in a DTB ranking list.
 *
 * @param rank the player's numeric rank within the list (1-based)
 * @param lastName the player's last name (Nachname), as printed in the PDF
 * @param firstName the player's first name (Vorname), as printed in the PDF
 * @param nationality the player's three-letter IOC/DOSB nationality code (e.g. {@code "GER"}), or
 *     {@code null} if absent from the source row
 * @param dtbId the player's unique 8-digit DTB identifier
 * @param association the two-or-three-letter VBD (regional tennis association) code (e.g. {@code
 *     "WTV"})
 * @param club the name of the player's tennis club as printed in the PDF
 * @param score the player's ranking score — either a numeric {@link Score.Points} value or an
 *     {@link Score.International} indicator for ATP/WTA-ranked players
 */
public record RankingEntry(
    int rank,
    String lastName,
    String firstName,
    @Nullable String nationality,
    String dtbId,
    String association,
    String club,
    Score score) {}
