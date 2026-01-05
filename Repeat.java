/**
 * Repeat is a Component decorator that repeats another Component
 * a fixed number of times during rendering.
 */
public class Repeat implements Component {

    /** The wrapped component to be repeated */
    Component component;

    /**
     * Creates a Repeat wrapper around a component.
     *
     * @param _component Component to repeat
     */
    public Repeat(Component _component) {
        this.component = _component;
    }

    /**
     * Prints the wrapped component multiple times.
     *
     * @return Concatenated output of repeated component
     */
    public String print() {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            content.append(component.print());
        }
        return content.toString();
    }
}
