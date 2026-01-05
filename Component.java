/**
 * Common component in a Composite design pattern.
 *
 * <p>Implementations of this interface represent nodes in a tree structure:
 * - Leaf implementations encapsulate primitive elements and return their own
 *   textual representation.
 * - Composite implementations contain children and typically return an aggregated
 *   or formatted representation derived from their children.</p>
 *
 * <p>Contract for print():
 * - Returns a human-readable String representation of this component.
 * - Preferably non-null; if an implementation may return null this behavior
 *   should be documented on that implementation.</p>
 *
 * <p>Implementations decide thread-safety and formatting details. Callers should
 * rely only on the guarantee that print() yields a textual representation and
 * should avoid mutating component state during printing.</p>
 *
 * @see java.util.Collection for common child-management patterns in composites
 */
interface Component {
    public String print ();
}