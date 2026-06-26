package de.hdawg.tennis.slice.metadata;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DisciplineDetectorTest {

  @Test
  void throwsWhenNoDisciplineKeywordFound() {
    assertThatThrownBy(() -> new DisciplineDetector().detect("Unbekannte Rangliste 2025"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Could not detect discipline");
  }
}
