package de.hdawg.slice.parse;

import static org.assertj.core.api.Assertions.assertThat;

import de.hdawg.slice.pdf.Word;
import java.util.List;
import org.junit.jupiter.api.Test;

class DataLineFilterTest {

  @Test
  void keepsLineWithExactlyOneEightDigitId() {
    Line line =
        new Line(
            3,
            100f,
            List.of(
                new Word("1", 60f, 65f, 100f, 3),
                new Word("Mustermann", 84.5f, 150f, 100f, 3),
                new Word("12345678", 329f, 360f, 100f, 3)));

    assertThat(new DataLineFilter().isDataLine(line)).isTrue();
  }

  @Test
  void rejectsHeaderLineWithoutEightDigitId() {
    Line line =
        new Line(
            3,
            50f,
            List.of(new Word("Rang", 60f, 80f, 50f, 3), new Word("Name", 84.5f, 110f, 50f, 3)));

    assertThat(new DataLineFilter().isDataLine(line)).isFalse();
  }

  @Test
  void rejectsClubLineWithFourDigitNumberOnly() {
    Line line =
        new Line(
            3,
            200f,
            List.of(
                new Word("TC", 386f, 396f, 200f, 3),
                new Word("1899", 397f, 420f, 200f, 3),
                new Word("Berlin", 421f, 460f, 200f, 3)));

    assertThat(new DataLineFilter().isDataLine(line)).isFalse();
  }

  @Test
  void rejectsLineWithTwoEightDigitIds() {
    Line line =
        new Line(
            3,
            100f,
            List.of(
                new Word("12345678", 100f, 150f, 100f, 3),
                new Word("87654321", 200f, 250f, 100f, 3)));

    assertThat(new DataLineFilter().isDataLine(line)).isFalse();
  }
}
