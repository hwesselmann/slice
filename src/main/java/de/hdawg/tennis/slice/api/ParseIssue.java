package de.hdawg.tennis.slice.api;

/**
 * A single issue (warning or error) encountered while parsing a DTB ranking PDF.
 *
 * @param severity how serious the issue is; {@link Severity#ERROR} rows are excluded from the
 *     {@link RankingList}
 * @param page the 1-based PDF page number on which the issue was detected, or {@code 1} for
 *     document-level metadata issues
 * @param rawLine the raw text of the offending line as extracted from the PDF, or a placeholder
 *     such as {@code "(Dokumentmetadaten)"} for metadata issues
 * @param message a human-readable description of the issue, written in German
 */
public record ParseIssue(Severity severity, int page, String rawLine, String message) {}
