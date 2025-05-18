package kkpa.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DocumentIngestor {

  private static final Logger log = LoggerFactory.getLogger(DocumentIngestor.class);

  private final EmbeddingModel embeddingModel;
  private static EmbeddingStore<TextSegment> embeddingStore;

  @Inject
  public DocumentIngestor(
      EmbeddingModel embeddingModel,
      @Named("documentStore") EmbeddingStore<TextSegment> embeddingStore) {
    this.embeddingStore = embeddingStore;
    this.embeddingModel = embeddingModel;
  }

  public void setupDocuments() throws Exception {
    List<Path> pdfFiles = getPdfFiles();
    log.info("Injecting {} files", pdfFiles.size());
    for (Path path : pdfFiles) {
      ingestPDFs(path);
    }
  }

  private void ingestPDFs(Path pdfFile) throws Exception {
    String pdfFileName = pdfFile.getFileName().toString();
    log.info("Injecting File {}", pdfFileName);
    // Load PDF file and parse it into a Document
    ApachePdfBoxDocumentParser pdfParser = new ApachePdfBoxDocumentParser();
    Document document = pdfParser.parse(Files.newInputStream(pdfFile));

    // Split document into segments
    DocumentSplitter splitter = DocumentSplitters.recursive(2000, 200);
    List<TextSegment> segments = splitter.split(document);
    for (TextSegment segment : segments) {

      segment.metadata().put("filename", pdfFileName);
    }

    // Convert segments into embeddings
    List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

    // Store embeddings into Qdrant
    embeddingStore.addAll(embeddings, segments);
  }

  // end::adocStore[]

  private static List<Path> getPdfFiles() throws IOException {
    List<Path> pdfFiles = new ArrayList<>();
    Path rootPath = Paths.get("").toAbsolutePath();

    try (Stream<Path> paths = Files.walk(rootPath)) {
      paths
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".pdf"))
          .forEach(pdfFiles::add);
    }

    return pdfFiles;
  }
}
