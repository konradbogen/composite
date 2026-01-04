public class StringComponent implements Component {
    private String content;
    public StringComponent (String content) {
        this.content = content;
    }
    public String print () {
        return this.content;
    }
    public void setContent (String c) {
        this.content = c;
    }
}
