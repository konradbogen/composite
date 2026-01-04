import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.*;

public class LilyPond implements Component {

    private final Component content;

    public LilyPond(Component content) {
        this.content = content;
    }

    @Override
    public String print() {
        try {
            Path templatePath = Paths.get(
                "/Users/konradbogen/Library/Mobile Documents/com~apple~CloudDocs/Obsidian/konrad/Musik/Template.md"
            );
            String template = Files.readString(templatePath);

            String raw = content.print()
                .replace("```lily", "")
                .replace("```", "");

            // Preserve insertion order
            Map<String, List<String>> voices = new LinkedHashMap<>();

            // Match: voiceName = { ... }
            Pattern pattern = Pattern.compile(
                "(voice\\w+)\\s*=\\s*\\{([\\s\\S]*?)\\}",
                Pattern.MULTILINE
            );

            Matcher matcher = pattern.matcher(raw);

            while (matcher.find()) {
                String voiceName = matcher.group(1);
                String body = matcher.group(2).trim();

                voices.computeIfAbsent(voiceName, k -> new ArrayList<>())
                      .add(body);
            }

            // Assemble final LilyPond voice definitions
            StringBuilder assembled = new StringBuilder();

            for (Map.Entry<String, List<String>> entry : voices.entrySet()) {
                assembled.append(entry.getKey()).append(" = {\n");
                for (String block : entry.getValue()) {
                    assembled.append("  ").append(block).append("\n");
                }
                assembled.append("}\n\n");
            }

            String result = template
                .replace("####MUSICGOESHERE####", assembled.toString())
                .replace("###DATEGOESHERE###", LocalDate.now().toString());

            Path out = Paths.get("lily");
            Files.writeString(
                out,
                result,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            );

            return result;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
