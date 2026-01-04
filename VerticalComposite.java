import java.util.Random;

public class VerticalComposite extends Composite implements Component {
    public String print () {
        Random rand = new Random();
        if (this.children.size () == 0) {
            return "";
        }
        int n = rand.nextInt(this.children.size());
        return this.children.get(n).print ();
    }
}
