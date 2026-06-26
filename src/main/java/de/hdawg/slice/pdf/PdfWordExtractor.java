package de.hdawg.slice.pdf;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * Extracts individual {@link Word} objects with their x/y coordinates and page number from a PDF
 * file using PDFBox.
 */
public final class PdfWordExtractor {

  /**
   * Loads the PDF at the given path and extracts all text tokens as {@link Word} objects, including
   * their bounding-box coordinates and page numbers.
   *
   * @param pdf path to the PDF file to extract words from
   * @return the extracted words in the order PDFBox delivers them (not yet sorted or grouped)
   * @throws IOException if the PDF file cannot be opened or read
   */
  public List<Word> extract(Path pdf) throws IOException {
    List<Word> words = new ArrayList<>();
    try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
      CollectingStripper stripper = new CollectingStripper(words);
      stripper.setSortByPosition(true);
      stripper.getText(document);
    }
    return words;
  }

  private static final class CollectingStripper extends PDFTextStripper {

    private final List<Word> sink;

    CollectingStripper(List<Word> sink) throws IOException {
      this.sink = sink;
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) {
      String trimmed = text.strip();
      if (trimmed.isEmpty() || textPositions.isEmpty()) {
        return;
      }
      TextPosition first = textPositions.get(0);
      TextPosition last = textPositions.get(textPositions.size() - 1);
      float x0 = first.getXDirAdj();
      float x1 = last.getXDirAdj() + last.getWidthDirAdj();
      float y = first.getYDirAdj();
      sink.add(new Word(trimmed, x0, x1, y, getCurrentPageNo()));
    }
  }
}
