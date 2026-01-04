import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.concurrent.*;

/**
 * LilyPond renderer that produces both PDF and MIDI from a .ly string.
 */
public class LilypondRenderer {

    private final String lilypondExecutable;
    private final Duration timeout;

    /** @param lilypondExecutable path to lilypond executable */
    public LilypondRenderer(String lilypondExecutable, Duration timeout) {
        this.lilypondExecutable = lilypondExecutable;
        this.timeout = timeout;
    }

    /** Convenience constructor with default 30s timeout */
    public LilypondRenderer(String lilypondExecutable) {
        this(lilypondExecutable, Duration.ofSeconds(30));
    }

    /**
     * Renders a LilyPond source string to PDF and MIDI files in memory.
     *
     * @param lilySource the .ly file content
     * @return a Result object containing PDF and MIDI bytes
     */
    public Result render(String lilySource) throws IOException, InterruptedException, TimeoutException {
        Path tmpDir = Files.createTempDirectory("lilypond-render-");
        lilySource = lilySource.replace ("````lily", "").replace ("```", "");
        try {
            Path inputLy = tmpDir.resolve("input.ly");
            Files.writeString(inputLy, lilySource, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            String outputPrefix = "output";
            Path outputPrefixPath = tmpDir.resolve(outputPrefix);

            ProcessBuilder pb = new ProcessBuilder(
                    lilypondExecutable,
                    "-o", outputPrefixPath.toString(),
                    inputLy.toString()
            );
            pb.directory(tmpDir.toFile());
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Capture process output asynchronously
            StringBuilder procOutput = new StringBuilder();
            ExecutorService gobbler = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "lilypond-output-gobbler");
                t.setDaemon(true);
                return t;
            });
            Future<?> gobblerFuture = gobbler.submit(() -> {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        procOutput.append(line).append(System.lineSeparator());
                    }
                } catch (IOException ignore) {}
            });

            boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new TimeoutException("LilyPond timed out. Output so far:\n" + procOutput);
            }

            gobblerFuture.get(5, TimeUnit.SECONDS);
            gobbler.shutdownNow();

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new IOException("LilyPond exited with code " + exitCode + ". Output:\n" + procOutput);
            }

            Path pdfPath = tmpDir.resolve(outputPrefix + ".pdf");
            Path midiPath = tmpDir.resolve(outputPrefix + ".mid");

            if (!Files.exists(pdfPath)) {
                throw new IOException("PDF not generated. Output:\n" + procOutput);
            }
            if (!Files.exists(midiPath)) {
                throw new IOException("MIDI not generated. Output:\n" + procOutput);
            }

            return new Result(
                    Files.readAllBytes(pdfPath),
                    Files.readAllBytes(midiPath)
            );

        } catch (ExecutionException e) {
            throw new IOException("Failed to read LilyPond process output.", e);
        } finally {
            // clean up temporary directory
            try { deleteRecursively(tmpDir); } catch (IOException ignored) {}
        }
    }

    /**
     * Simple recursive delete
     */
    private static void deleteRecursively(Path path) throws IOException {
        if (Files.notExists(path)) return;
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
                for (Path child : ds) deleteRecursively(child);
            }
            Files.deleteIfExists(path);
        } else {
            Files.deleteIfExists(path);
        }
    }

    /** Result object holding PDF and MIDI bytes */
    public static class Result {
        public final byte[] pdf;
        public final byte[] midi;
        public Result(byte[] pdf, byte[] midi) {
            this.pdf = pdf;
            this.midi = midi;
        }
    }

    /** Convenience method to write to disk */
    public void renderToFiles(String lilySource, Path pdfDest, Path midiDest)
            throws IOException, InterruptedException, TimeoutException {
        Result result = render(lilySource);
        Files.write(pdfDest, result.pdf, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.write(midiDest, result.midi, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /** Example usage */
    public static void main(String[] args) {
        String lilySource = """
                \\version "2.24.2"
                \\header { title = "Hello LilyPond" composer = "Generated by Java" }
                \\score {
                  \\new StaffGroup <<
                    \\new Staff { \\clef treble c'4 d' e' f' }
                    \\new Staff { \\clef bass c,4 g, c g, }
                  >>
                  \\midi {}
                  \\layout {}
                }
                """;

        LilypondRenderer renderer = new LilypondRenderer("lilypond", Duration.ofSeconds(30));
        try {
            Path pdfFile = Paths.get("hello.pdf");
            Path midiFile = Paths.get("hello.midi");
            renderer.renderToFiles(lilySource, pdfFile, midiFile);
            System.out.println("PDF written to " + pdfFile.toAbsolutePath());
            System.out.println("MIDI written to " + midiFile.toAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
