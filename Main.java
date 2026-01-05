import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;
import java.io.IOException;

/**
 * Entry point of the application.
 * Builds components from Obsidian notes and renders them using LilyPond.
 */
public class Main {
    /**
     * Application entry point — prints computer science notes and a music score.
     *
     * @param args command-line arguments
     * @throws IOException if an I/O error occurs during printing
     */
    public static void main(String[] args) throws IOException {
        print_computer_science_notes();
        print_music_score();
    }

    /**
     * Build a music score from an Obsidian note, convert it to LilyPond format and render PDF and MIDI files.
     *
     * Parses a specific Obsidian "Beat.md" file into a component, generates LilyPond input, and invokes the
     * external LilyPond renderer to produce "hello.pdf" and "hello.mid". Any IO, interruption or timeout
     * exceptions are caught and printed; the method logs the output PDF path to stdout.
     */
    private static void print_music_score() {
        ObsidianBuilder builder = new ObsidianBuilder();
        builder.makeFromObsidian(
            "/Users/konradbogen/Library/Mobile Documents/com~apple~CloudDocs/Obsidian/konrad/Musik/Beat.md",
            null
        );
        LilyPond lily = new LilyPond(builder.getComponent());
        String lilyPath = "/Applications/LilyPond 2.app/Contents/Resources/bin/lilypond";
        LilypondRenderer renderer = new LilypondRenderer(lilyPath);
        Path out = Paths.get("hello.pdf");
        try {
            Path pdfFile = Paths.get("hello.pdf");
            Path midiFile = Paths.get("hello.mid");
            renderer.renderToFiles(lily.print(), pdfFile, midiFile);
        } catch (IOException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
        System.out.println("PDF written to " + out.toAbsolutePath());
    }

    /**
     * Build notes from the Obsidian index file and print the resulting component
     * using the "ws2526" context.
     */
    private static void print_computer_science_notes() {
        ObsidianBuilder builder = new ObsidianBuilder();

        builder.makeFromObsidian(
            "/Users/konradbogen/Library/Mobile Documents/com~apple~CloudDocs/Obsidian/konrad/Informatik/_Index.md",
            ""
        );
        Main.print(builder.getComponent(), "ws2526");
    }

    /**
     * Writes the printed representation of a component to a text file.
     *
     * @param c    the component to print
     * @param name base name of the output file (without extension)
     */
    public static void print(Component c, String name) {
        try {
            PrintWriter writer = new PrintWriter(name + ".txt", "UTF-8");
            writer.println(c.print());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
