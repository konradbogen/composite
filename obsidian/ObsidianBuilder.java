import java.io.*;
import java.util.LinkedList;


public class ObsidianBuilder {
    Component component;
    Composite composite;
    LinkedList<Composite> vertical_composites;
    String current_title;
    Boolean reading_first_vertical = true;
    public Component makeFromObsidian (String filename, String header) {
        this.composite = new Composite ();
        this.vertical_composites = new LinkedList<Composite>();
        this.read (filename, header);
        return this.component;
    }


    public Component getComponent () {
        return this.component;
    }


    public void read (String filename, String header) {
        try (BufferedReader br = new BufferedReader (new FileReader (filename))) {
            String line;
            this.vertical_composites.add (new VerticalComposite());
            while ((line = br.readLine ()) != null) {
                boolean repeat = false;
                if (line.endsWith("*")) {
                    repeat = true;
                    line = line.replace ("*", "");
                }
                if (line.startsWith("# ")) {
                    start_new_vertical_composite(line);
                }else if (line.startsWith ("[[") || line.startsWith("![[")) {
                    Composite component = new Composite ();
                    if (line.contains("!")) {
                        line = line.replace("!", "");
                        component.add(new StringComponent("\\mark \\default"));
                    }
                    if (line.contains("||")) {
                        line = line.replace("||", "");
                        composite.add(new StringComponent("\\bar \"||\""));
                    }
                    if (line.contains("?")) {
                        line = line.replace("?", "");
                        composite.add(new StringComponent("\\break"));
                    }
                    if (line.contains("####")) {
                        component.add(add_nested_composite(line));
                    } else if (line.contains("+")) {
                        component.add(add_pure_text_line(line));
                    } else {
                        component.add(add_line(line));
                    }
                    if (repeat == true) {
                        Repeat repeater = new Repeat (component);
                        this.vertical_composites.getLast().add(repeater); 
                    }else { 
                        this.vertical_composites.getLast().add(component);
                    }
                }
            }
            composite.add (vertical_composites.getLast());
            this.component = composite;
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    private Component add_pure_text_line (String line) {
        line = line.replace ("+", "");
        boolean hadBang = line.startsWith("!");
        line = line.replace("[[", "").replace("]]", "");
        if (hadBang) {
            line = line.substring(1);
        }
        int pipeIdx = line.indexOf('|');
        if (pipeIdx != -1) {
            line = line.substring(0, pipeIdx);
        }
        line = line.trim();
             if (!line.endsWith(".md")) {
            line = line + ".md";
        }
        String basePath = "/Users/konradbogen/Library/Mobile Documents/com~apple~CloudDocs/Obsidian/konrad/";
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
        if (content.endsWith(System.lineSeparator())) {
            content = content.substring(0, content.length() - System.lineSeparator().length());
        }
        StringComponent s = new StringComponent(content);
        return s;
    }


    private Component add_line(String line) {
        StringBuilder current_line_content = new StringBuilder ();
        if (!line.startsWith("!")) {
            current_line_content.append ("!");
        }
        current_line_content.append (line);
        StringComponent s = new StringComponent(current_line_content.toString());
        return s;
    }


    private Component add_nested_composite(String line) {
        StringBuilder path = new StringBuilder();
        path.append("/Users/konradbogen/Library/Mobile Documents/com~apple~CloudDocs/Obsidian/konrad/");
        line = line.replace ("[[", "");
        line = line.replace ("]]", "");
        line = line.replace ("####", "");
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
        nested.makeFromObsidian (path.toString(),"" );
        return nested.getComponent();
    }


    private void start_new_vertical_composite(String line) {
        if (!this.reading_first_vertical) { 
            this.composite.add(vertical_composites.getLast());
            this.vertical_composites.add (new VerticalComposite()); 
        }
        this.current_title = line;
        this.reading_first_vertical = false;
    }
}
