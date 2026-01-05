import java.io.*;
import java.util.LinkedList;

/**
 * ObsidianBuilder parses an Obsidian markdown file and converts it into a
 * hierarchical Component structure.
 *
 * <p>The structure is organized vertically (per heading) using
 * VerticalComposite objects, which themselves contain Composite elements
 * representing musical or textual fragments.</p>
 *
 * <p>This builder supports:
 * <ul>
 *   <li>Headings as vertical separators</li>
 *   <li>Embedded links ([[file]])</li>
 *   <li>Inline commands (*, +, ####, ?, ||)</li>
 *   <li>Recursive inclusion of nested Obsidian documents</li>
 * </ul>
 * </p>
 */
public class ObsidianBuilder {

    /** Root component produced by the builder */
    Component component;

    /** Top-level composite that holds all vertical composites */
    Composite composite;

    /** List of vertical composites (one per section / heading) */
    LinkedList<Composite> vertical_composites;

    /** Current section title (markdown heading) */
    String current_title;

    /** Flag to avoid emitting an empty first vertical composite */
    Boolean reading_first_vertical = true;

    /**
     * Builds a Component tree from an Obsidian markdown file.
     *
     * @param filename Path to the markdown file
     * @param header   Optional header string (currently unused)
     * @return Root Component representing the parsed document
     */
    public Component makeFromObsidian(String filename, String header) {
        this.composite = new Composite();
        this.vertical_composites = new LinkedList<>();
        this.read(filename, header);
        return this.component;
    }

    /**
     * Returns the root Component produced by the last build.
     *
     * @return Root Component
     */
    public Component getComponent() {
        return this.component;
    }

    /**
     * Reads the given markdown file line by line and constructs
     * the internal Component hierarchy.
     *
     * <p>Recognized syntax:
     * <ul>
     *   <li><code># </code> — starts a new vertical composite</li>
     *   <li><code>*</code> — marks the line as repeatable</li>
     *   <li><code>[[file]]</code> — includes another Obsidian file</li>
     *   <li><code>!</code> — inserts a LilyPond mark</li>
     *   <li><code>||</code> — inserts a bar line</li>
     *   <li><code>?</code> — inserts a line break</li>
     *   <li><code>####</code> — recursively includes a nested composite</li>
     *   <li><code>+</code> — inserts raw file contents</li>
     * </ul>
     * </p>
     *
     * @param filename Path to the markdown file
     * @param header   Optional header string (currently unused)
     */
    public void read(String filename, String header) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;

            // Start with an initial vertical composite
            this.vertical_composites.add(new VerticalComposite());

            while ((line = br.readLine()) != null) {
                boolean repeat = false;

                // Handle repeat marker
                if (line.endsWith("*")) {
                    repeat = true;
                    line = line.replace("*", "");
                }

                // Section heading
                if (line.startsWith("# ")) {
                    start_new_vertical_composite(line);

                // Embedded link or image link
                } else if (line.startsWith("[[") || line.startsWith("![[")) {

                    Composite component = new Composite();

                    // Image links produce a rehearsal mark
                    if (line.contains("!")) {
                        line = line.replace("!", "");
                        component.add(new StringComponent("\\mark \\default"));
                    }

                    // Double barline
                    if (line.contains("||")) {
                        line = line.replace("||", "");
                        composite.add(new StringComponent("\\bar \"||\""));
                    }

                    // Line break
                    if (line.contains("?")) {
                        line = line.replace("?", "");
                        composite.add(new StringComponent("\\break"));
                    }

                    // Nested composite
                    if (line.contains("####")) {
                        component.add(add_nested_composite(line));

                    // Raw text inclusion
                    } else if (line.contains("+")) {
                        component.add(add_pure_text_line(line));

                    // Normal line inclusion
                    } else {
                        component.add(add_line(line));
                    }

                    // Wrap in Repeat if needed
                    if (repeat) {
                        Repeat repeater = new Repeat(component);
                        this.vertical_composites.getLast().add(repeater);
                    } else {
                        this.vertical_composites.getLast().add(component);
                    }
                }
            }

            // Add final vertical composite
            composite.add(vertical_composites.getLast());
            this.component = composite;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the full contents of a referenced markdown file and
     * inserts it verbatim as a StringComponent.
     *
     * @param line Markdown link line with '+' marker
     * @return Component containing raw file contents
     */
    private Component add_pure_text_line(String line) {
        line = line.replace("+", "");
        boolean hadBang = line.startsWith("!");

        line = line.replace("[[", "").replace("]]", "");

        if (hadBang) {
            line = line.substring(1);
        }

        // Strip alias after '|'
        int pipeIdx = line.indexOf('|');
        if (pipeIdx != -1) {
            line = line.substring(0, pipeIdx);
        }

        line = line.trim();

        if (!line.endsWith(".md")) {
            line = line + ".md";
        }

        String basePath =
            "/Users/konradbogen/Library/Mobile Documents/com~apple~CloudDocs/Obsidian/konrad/";
        String filePath = basePath + line;

        StringBuilder fileContent = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String l;
            while ((l = br.readLine()) != null) {
                fileContent.append(l).append(System.lineSeparator());
            }
        } catch (IOException e) {
            fileContent.append("ERROR_READING_FILE: ").append(e.getMessage());
        }

        String content = fileContent.toString();

        // Trim trailing newline
        if (content.endsWith(System.lineSeparator())) {
            content = content.substring(
                0,
                content.length() - System.lineSeparator().length()
            );
        }

        return new StringComponent(content);
    }

    /**
     * Wraps a single Obsidian link line into a StringComponent,
     * ensuring it starts with '!'.
     *
     * @param line Markdown link line
     * @return StringComponent containing the processed line
     */
    private Component add_line(String line) {
        StringBuilder current_line_content = new StringBuilder();

        if (!line.startsWith("!")) {
            current_line_content.append("!");
        }

        current_line_content.append(line);
        return new StringComponent(current_line_content.toString());
    }

    /**
     * Recursively parses a nested Obsidian file and returns its Component tree.
     *
     * @param line Markdown link with #### marker
     * @return Component representing the nested document
     */
    private Component add_nested_composite(String line) {
        StringBuilder path = new StringBuilder();
        path.append(
            "/Users/konradbogen/Library/Mobile Documents/com~apple~CloudDocs/Obsidian/konrad/"
        );

        line = line.replace("[[", "")
                   .replace("]]", "")
                   .replace("####", "");

        int pipeIdx = line.indexOf('|');
        if (pipeIdx != -1) {
            line = line.substring(0, pipeIdx);
        }

        line = line.trim();

        if (!line.endsWith(".md")) {
            line = line + ".md";
        }

        path.append(line);

        ObsidianBuilder nested = new ObsidianBuilder();
        nested.makeFromObsidian(path.toString(), "");
        return nested.getComponent();
    }

    /**
     * Starts a new vertical composite when a markdown heading is encountered.
     *
     * @param line Heading line (starting with '# ')
     */
    private void start_new_vertical_composite(String line) {
        if (!this.reading_first_vertical) {
            this.composite.add(vertical_composites.getLast());
            this.vertical_composites.add(new VerticalComposite());
        }

        this.current_title = line;
        this.reading_first_vertical = false;
    }
}
