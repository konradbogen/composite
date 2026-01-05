import java.util.Random;

/**
 * Composite that randomly selects and prints one of its child components.
 */
public class VerticalComposite extends Composite implements Component {

    /**
     * Prints a randomly chosen child component.
     *
     * @return string output of a random child, or an empty string if no children exist
     */
    public String print() {
        Random rand = new Random();
        if (this.children.size() == 0) {
            return "";
        }
        int n = rand.nextInt(this.children.size());
        return this.children.get(n).print();
    }
}
