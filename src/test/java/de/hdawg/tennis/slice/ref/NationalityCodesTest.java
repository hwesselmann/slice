package de.hdawg.tennis.slice.ref;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NationalityCodesTest {

  @Test
  void knowsCommonCodesFromTheExamplePdfs() {
    assertThat(NationalityCodes.isKnown("ITA")).isTrue();
    assertThat(NationalityCodes.isKnown("TUR")).isTrue();
    assertThat(NationalityCodes.isKnown("RUS")).isTrue();
  }

  @Test
  void knowsGermanyByBothIsoAndDtbPrintedCode() {
    assertThat(NationalityCodes.isKnown("DEU")).isTrue();
    assertThat(NationalityCodes.isKnown("GER")).isTrue();
  }

  @Test
  void knowsIocCodesActuallyPrintedByTheDtb() {
    // IOC/DOSB codes that differ from ISO-3166-1 alpha-3 and occur in the fixtures.
    assertThat(NationalityCodes.isKnown("SUI")).isTrue();
    assertThat(NationalityCodes.isKnown("NED")).isTrue();
    assertThat(NationalityCodes.isKnown("CRO")).isTrue();
    assertThat(NationalityCodes.isKnown("IRI")).isTrue();
    assertThat(NationalityCodes.isKnown("RSA")).isTrue();
  }

  @Test
  void knowsIocCodesFromRankingInfo() {
    assertThat(NationalityCodes.isKnown("POR")).isTrue(); // Portugal
    assertThat(NationalityCodes.isKnown("MAS")).isTrue(); // Malaysia
    assertThat(NationalityCodes.isKnown("PHI")).isTrue(); // Philippines
    assertThat(NationalityCodes.isKnown("URU")).isTrue(); // Uruguay
  }

  @Test
  void rejectsUnknownCode() {
    assertThat(NationalityCodes.isKnown("XXX")).isFalse();
  }
}
