package de.hdawg.slice.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ScoreTest {

  @Test
  void rendersPointsAsGermanDecimalAndInternationalAsMarker() {
    Score points = new Score.Points(new BigDecimal("24855.0"));
    Score atp = Score.International.ATP;
    Score wta = Score.International.WTA;

    assertThat(render(points)).isEqualTo("24855.0");
    assertThat(render(atp)).isEqualTo("ATP");
    assertThat(render(wta)).isEqualTo("WTA");
  }

  @Test
  void rendersSpecialJuniorScoreTokens() {
    assertThat(render(Score.ProtectedRanking.INSTANCE)).isEqualTo("PR");
    assertThat(render(Score.Projected.INSTANCE)).isEqualTo("Einst.");
  }

  private String render(Score score) {
    return switch (score) {
      case Score.Points p -> p.value().toString();
      case Score.International intl -> intl.name();
      case Score.ProtectedRanking ignored -> "PR";
      case Score.Projected ignored -> "Einst.";
    };
  }
}
