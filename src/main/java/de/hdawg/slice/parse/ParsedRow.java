package de.hdawg.slice.parse;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

public record ParsedRow(
    int rank,
    boolean fromAListe,
    String lastName,
    String firstName,
    @Nullable String nationality,
    String dtbId,
    @Nullable String association,
    String club,
    @Nullable BigDecimal points,
    @Nullable String scoreToken) {}
