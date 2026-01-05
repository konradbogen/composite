/**
 * Leaf component that represents plain string content.
 */
public class StringComponent implements Component {

    /** Stored string content */
    private String content;

    /**
     * Creates a new StringComponent with the given content.
     *
     * @param content the string content to store
     */
    public StringComponent(String content) {
        this.content = content;
    }

    /**
     * Returns the stored string content.
     *
     * @return the content as a string
     */
    public String print() {
        return this.content;
    }

    /**
     * Updates the stored string content.
     *
     * @param c the new content
     */
    public void setContent(String c) {
        this.content = c;
    }
}
