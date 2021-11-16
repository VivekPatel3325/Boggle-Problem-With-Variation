package boggleValidationsRecursive;

import java.io.BufferedReader;
import java.util.List;

public interface BoogleInterface {

	boolean getDictionary(BufferedReader stream);

	boolean getPuzzle(BufferedReader stream);

	List<String> solve();

	String print();
}
