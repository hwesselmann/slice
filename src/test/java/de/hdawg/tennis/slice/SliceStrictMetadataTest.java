package de.hdawg.tennis.slice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.hdawg.tennis.slice.validate.ParseMode;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SliceStrictMetadataTest {

  @TempDir Path tempDir;

  @Test
  void strictModeThrowsWhenMetadataExtractionFails() throws IOException {
    // PDF contains "Herren" so DisciplineDetector succeeds, but has no stichtag date.
    // MetadataExtractor emits an ERROR → STRICT mode must throw IllegalStateException.
    Path pdf = buildPdf("Deutsche Rangliste Herren");

    assertThatThrownBy(() -> Slice.builder().mode(ParseMode.STRICT).build().parse(pdf))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("STRICT");
  }

  private Path buildPdf(String pageText) throws IOException {
    Path out = tempDir.resolve("test.pdf");
    try (PDDocument doc = new PDDocument()) {
      PDPage page = new PDPage();
      doc.addPage(page);
      try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
        cs.newLineAtOffset(50, 700);
        cs.showText(pageText);
        cs.endText();
      }
      doc.save(out.toFile());
    }
    return out;
  }
}
