package de.hdawg.slice.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class YearRangeTest {

  @Test
  void containsReturnsTrueForYearInRange() {
    YearRange range = new YearRange(2008, 2014);

    assertThat(range.contains(2008)).isTrue();
    assertThat(range.contains(2011)).isTrue();
    assertThat(range.contains(2014)).isTrue();
  }

  @Test
  void containsReturnsFalseForYearOutsideRange() {
    YearRange range = new YearRange(2008, 2014);

    assertThat(range.contains(2007)).isFalse();
    assertThat(range.contains(2015)).isFalse();
  }

  @Test
  void constructorThrowsWhenFromExceedsTo() {
    assertThatThrownBy(() -> new YearRange(2015, 2008))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
