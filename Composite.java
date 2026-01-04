import java.util.LinkedList;

class Composite implements Component {
    public LinkedList<Component> children;

    public Composite () {
        this.children = new LinkedList<Component>();
    }

    public void add (Component c) {
        this.children.add (c);
    }
    public void remove (Component c) {
        this.children.remove (c);
    }
    public Component getChild (int i) {
        return this.children.get (i);
    }
    public String print () {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < this.children.size(); i++) {
            output.append (this.children.get (i).print ());
            output.append("\n");
        }
        return output.toString();
    }
}