import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;
import java.io.PrintWriter;
import java.io.IOException;

public class Main {
   
    public static void main (String[] args) throws IOException {
        ObsidianBuilder builder = new ObsidianBuilder();
        builder.makeFromObsidian("/Users/konradbogen/Library/Mobile Documents/com~apple~CloudDocs/Obsidian/konrad/Informatik/_Index.md", "");
        Main.print(builder.getComponent(), "ws2526");
        builder.makeFromObsidian("/Users/konradbogen/Library/Mobile Documents/com~apple~CloudDocs/Obsidian/konrad/Musik/Beat.md", null);
        LilyPond lily = new LilyPond(builder.getComponent());
        String lilyPath = "/Applications/LilyPond 2.app/Contents/Resources/bin/lilypond"; // or "C:\\Program Files\\LilyPond\\usr\\bin\\lilypond.exe"
        LilypondRenderer renderer = new LilypondRenderer(lilyPath);
        Path out = Paths.get("hello.pdf");
        try {
            Path pdfFile = Paths.get("hello.pdf");
            Path midiFile = Paths.get("hello.mid");
            if (true) {renderer.renderToFiles(lily.print(), pdfFile, midiFile);};        
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        System.out.println("PDF written to " + out.toAbsolutePath());    
    } 

    public static void print (Component c, String name) {
        try {
         PrintWriter writer = new PrintWriter(name + ".txt", "UTF-8");
        writer.println(c.print ());
        writer.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
