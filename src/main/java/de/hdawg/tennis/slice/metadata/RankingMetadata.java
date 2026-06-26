package de.hdawg.tennis.slice.metadata;

import de.hdawg.tennis.slice.api.YearRange;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

public record RankingMetadata(
    @Nullable LocalDate stichtag,
    @Nullable LocalDate validFrom,
    @Nullable Integer pointsThreshold,
    @Nullable YearRange birthYears) {}
