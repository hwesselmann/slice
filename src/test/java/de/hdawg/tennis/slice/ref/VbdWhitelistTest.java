package de.hdawg.tennis.slice.ref;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VbdWhitelistTest {

  @Test
  void knowsSeedCodes() {
    assertThat(VbdWhitelist.isKnown("TVN")).isTrue();
    assertThat(VbdWhitelist.isKnown("BAD")).isTrue();
  }

  @Test
  void rejectsUnknownCode() {
    assertThat(VbdWhitelist.isKnown("ZZZ")).isFalse();
  }
}
