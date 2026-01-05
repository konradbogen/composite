import java.util.LinkedList;

/**
 * Composite component in the Composite design pattern.
 * Manages a collection of child components.
 */
class Composite implements Component {

    /** List of child components */
    public LinkedList<Component> children;

    /**
     * Creates an empty Composite.
     */
    public Composite() {
        this.children = new LinkedList<Component>();
    }

    /**
     * Adds a component to this composite.
     *
     * @param c the component to add
     */
    public void add(Component c) {
        this.children.add(c);
    }

    /**
     * Removes a component from this composite.
     *
     * @param c the component to remove
     */
    public void remove(Component c) {
        this.children.remove(c);
    }

    /**
     * Returns the child component at the given index.
     *
     * @param i index of the child
     * @return the child component
     */
    public Component getChild(int i) {
        return this.children.get(i);
    }

    /**
     * Prints all child components by delegating to their print methods.
     *
     * @return combined string representation of all children
     */
    public String print() {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < this.children.size(); i++) {
            output.append(this.children.get(i).print());
            output.append("\n");
        }
        return output.toString();
    }
}
