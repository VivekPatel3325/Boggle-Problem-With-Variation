
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boggleValidationsRecursive.BoogleInterface;

public class Boggle implements BoogleInterface {

	//List to store words of the dictionary.
	private List<String> dictionary;

	//List to store input for the puzzle grid. 
	private List<String> lettersOfGrid;

	//NxN puzzle grid,which is formed as per the data store in lettersOfGrid. 
	private String board[][];

	//Map to store coordinates of each element of the puzzle grid.
	private Map<String, List<String>> coordinateMapping;

	//NxN boolean 2D array to keep track of visited cell of the puzzle grid.
	private boolean isCellVisited[][];

	//Keep track that number of characters in each row of the puzzle are equal or not.
	private boolean unEqualRows = false;

	//List of possible direction for traversing 
	private static final String left = "L";
	private static final String right = "R";
	private static final String up = "U";
	private static final String down = "D";
	private static final String dUpLeft = "N";
	private static final String dUpRight = "E";
	private static final String dDownRight = "S";
	private static final String dDownLeft = "W";

	//Default constructor for the instantiation and initialization of the class variables
	Boggle() {

		dictionary = new ArrayList<String>();
		lettersOfGrid = new ArrayList<String>();
		coordinateMapping = new HashMap<String, List<String>>();
	}

	/**
	 * Method to read word of the dictionary and return true or false 
	 * 
	 * stream source to read data from the given file
	 * return true if dictionary is ready to use for puzzle solving else false 
	 */
	public boolean getDictionary(BufferedReader stream){

		//Return false if the given stream has not been instantiated 
		if (stream == null) {

			return false;
		}
		String line;
		try {
			//Read data till the end of the file
			while ((line = stream.readLine()) != null) {
				//If file contains blank line then mark it as end of the file
				if (line.trim().equals("")) {
					break;
				} else {
					//Consider only those words,which are at least two characters long. 
					if (line.trim().length() > 1) {
						dictionary.add(line.trim());
					}else {
						return false;
					}
				}
			}
		} catch (IOException e) {

			return false; 
		}
		return true;
	}

	/**
	 * Method to read data for the puzzle grid and return true or false 
	 * 
	 * stream source to read data from the given file
	 * return true if puzzle is read and ready for puzzle solving else false. 
	 */
	public boolean getPuzzle(BufferedReader stream){

		int index = 0;
		//Return false if the given stream has not been instantiated
		if (stream == null) {
			return false;
		}
		String line;
		try {
			//Read data till the end of the file
			while ((line = stream.readLine()) != null) {
				
				//If file contains blank line then mark it as end of the file
				if (line.trim().equals("")) {
					break;
				} else {

					//Validate number of characters in each row of the puzzle are equal or not.If not 
					//stop reading file and return false.
					if (index != 0) {
						String previousInput = lettersOfGrid.get(index - 1);

						// Check if input is of equal length or not
						if (previousInput.length() != line.length()) {
							unEqualRows = true;
							return false;// Exception can be thrown here
						}
					}

					lettersOfGrid.add(line);
					index++;
				}
			}
		} catch (IOException e) {
			
			return false;
		}

		return true;
	}

	/**Instantiate NxN puzzle grid as per the data read by getPuzzle() method.
	 * 
	 * */
	private void formPuzzleBoard() {
		
		
		//Instantiate NxN array for puzzle grid and keep track of all visited cell.
		board = new String[lettersOfGrid.size()][lettersOfGrid.get(0).length()];
		isCellVisited = new boolean[lettersOfGrid.size()][lettersOfGrid.get(0).length()];

		//Get the coordinates for the each element of the grid and store in HashMap.
		int row = 0;

		for (String letters : lettersOfGrid) {

			char characters[] = letters.toCharArray();

			for (int column = 0; column < characters.length; column++) {

				board[row][column] = String.valueOf(characters[column]);

				if (!coordinateMapping.containsKey(String.valueOf(characters[column]))) {
					List<String> coordinates = new ArrayList<String>();
					// Column=X coordinate,Row=Y coordinate
					coordinates.add(String.valueOf(row) + "," + String.valueOf(column));
					coordinateMapping.put(String.valueOf(characters[column]), coordinates);
				} else {

					List<String> coordinates = coordinateMapping.get(String.valueOf(characters[column]));
					coordinates.add(String.valueOf(row) + "," + String.valueOf(column));
					coordinateMapping.put(String.valueOf(characters[column]), coordinates);
				}

			}
			row++;
		}

	}

	/**
	 * return list of words of the dictionary found from the puzzle grid by
	 * satisfying all constraints of the game
	 */
	public List<String> solve() {

		// List to maintain list of all words found in the puzzle grid.
		List<String> wordFound = new ArrayList<String>();

		// Solve puzzle if puzzle is not 0x0 and has equal numbers of characters in each
		// row else return empty list.
		if (lettersOfGrid.size() > 0 && !unEqualRows) {
			formPuzzleBoard();

			int currentRow = 0, currentColumn = 0, startingX = 0, startingY = 0;
			StringBuffer word = new StringBuffer();
			StringBuffer path = new StringBuffer("");

			// Start solving puzzle for each word of the dictionary
			for (String wordFromDic : dictionary) {

				// convert word into char array
				char letters[] = wordFromDic.toCharArray();
				word.setLength(0);
				path.setLength(0);

				// Get the coordinates for the first letter of the word.
				List<String> coordinates = coordinateMapping.get(String.valueOf(letters[0]));

				// If the start letter is found in multiple cells of the grid then sort list of
				// coordinates of the letter
				if (coordinates != null && coordinates.size() != 1) {

					// Get list of coordinated of the initial letter of the word
					List<String> coordinatesToBeSort = new ArrayList<String>(coordinates);

					// List of sorted coordinates by keeping pair of min X and min Y on top.
					List<String> sortedCoordinates = sortXYCoordinates(coordinatesToBeSort);
					coordinates = sortedCoordinates;

				}

				if (coordinates != null) {

					// Find word for each pair of coordinates of the letter
					for (String coordinate : coordinates) {
						currentColumn = Integer.valueOf(String.valueOf(coordinate.charAt(2)));
						currentRow = Integer.valueOf(String.valueOf(coordinate.charAt(0)));
						word.setLength(0);
						path.setLength(0);

						startingX = currentColumn;
						startingY = currentRow;

						boolean wordExist = false;

						// Call recursive function for searching word
						wordExist = searchWord(currentColumn, currentRow, path, isCellVisited, word, wordFromDic,
								startingX, startingY, wordFound);

						// If word exist for the pair of min X and min Y coordinates stop further
						// iteration.
						if (wordExist) {
							break;
						}

					}

				}

			}

		}

		// Sort the list of word.
		Collections.sort(wordFound);
		return wordFound;
	}

	/**
	 * return true if the given word found in the puzzle grid according to the constraints of the game else false. 
	 *  
	 */
	public boolean searchWord(int column, int row, StringBuffer path, boolean[][] visited, StringBuffer word,
			String wordToBefound, int startX, int startY, List<String> wordFound) {
		
		
		//If word is found  then mark it as true and stop searching process for other directions.
		boolean stopChecking = false;

		//Return false if row or column is negative or cell is visited 
		if (column < 0 || row < 0 || column >= lettersOfGrid.get(0).length() || row >= lettersOfGrid.size()
				|| visited[row][column]) {
			return stopChecking = false;
		}

		//Mark the cell visited for the given pair of row and column 
		isCellVisited[row][column] = true;
		word.append(board[row][column]);

		//If the the given string is not a substring of the word return false 
		if (!wordToBefound.substring(0, word.length()).equals(word.toString())) {
			word.deleteCharAt(word.length() - 1);
			isCellVisited[row][column] = false;
			return stopChecking = false;
		}

		//If word is found then build the result string
		if (word.toString().equals(wordToBefound)) {
			String resultString = "";
			startY = lettersOfGrid.size() - startY;
			startX = startX + 1;
			resultString = wordToBefound + "\t" + startX + "\t" + startY + "\t" + path;
			wordFound.add(resultString);
			isCellVisited[row][column] = false;
			return stopChecking = true;
		}

		if (!stopChecking) {
			path.append(up);
			// Search a word in up direction
			stopChecking = searchWord(column, row - 1, path, visited, word, wordToBefound, startX, startY, wordFound);
			path.deleteCharAt(path.length() - 1);
		}

		if (!stopChecking) {
			path.append(down);
			// Search a word in down direction
			stopChecking = searchWord(column, row + 1, path, visited, word, wordToBefound, startX, startY, wordFound);
			path.deleteCharAt(path.length() - 1);
		}

		if (!stopChecking) {
			path.append(right);
			// Search a word in right direction
			stopChecking = searchWord(column + 1, row, path, visited, word, wordToBefound, startX, startY, wordFound);
			path.deleteCharAt(path.length() - 1);
		}

		if (!stopChecking) {
			path.append(left);
			// Search a word in left direction
			stopChecking = searchWord(column - 1, row, path, visited, word, wordToBefound, startX, startY, wordFound);																		// direction
			path.deleteCharAt(path.length() - 1);
		}

		if (!stopChecking) {
			path.append(dUpRight);
			stopChecking = searchWord(column + 1, row - 1, path, visited, word, wordToBefound, startX, startY,
					wordFound);// Search a word in up and right direction
			path.deleteCharAt(path.length() - 1);
		}

		if (!stopChecking) {
			path.append(dUpLeft);
			stopChecking = searchWord(column - 1, row - 1, path, visited, word, wordToBefound, startX, startY,
					wordFound);// Search a word in up and left direction
			path.deleteCharAt(path.length() - 1);
		}

		if (!stopChecking) {

			path.append(dDownRight);
			stopChecking = searchWord(column + 1, row + 1, path, visited, word, wordToBefound, startX, startY,
					wordFound);// Search a word in down and right direction
			path.deleteCharAt(path.length() - 1);
		}

		if (!stopChecking) {
			path.append(dDownLeft);
			stopChecking = searchWord(column - 1, row + 1, path, visited, word, wordToBefound, startX, startY,
					wordFound);// Search a word in down and left direction
			path.deleteCharAt(path.length() - 1);
		}

		if (!stopChecking) {
			word.deleteCharAt(word.length() - 1);
		}

		// word.deleteCharAt(word.length() - 1);
		isCellVisited[row][column] = false;
		return stopChecking;
	}

	
	/**
	 * Sort the coordinates for the given letter
	 * For example::
	  	  unsorted             sorted 
			Y X    				Y X
			3,0    				3,0
			1,2    				1,1
			3,2    				1,2
			1,1    				2,2
			1,2    				3,2			 
	 */
	public List<String> sortXYCoordinates(List<String> coordinatesToBeSort) {
		
		List<String> sortedCoordinates = new ArrayList<String>();

		
		//Sort the pair of X,Y coordinates according to X coordinate
		for (int counter = 0; counter < coordinatesToBeSort.size() - 1; counter++) {

			for (int index = 0; index < coordinatesToBeSort.size() - 1; index++) {

				if (Integer.parseInt(String.valueOf(coordinatesToBeSort.get(index + 1).charAt(2))) < Integer
						.parseInt(String.valueOf(coordinatesToBeSort.get(index).charAt(2)))) {

					swap(index, index + 1, coordinatesToBeSort.get(index), coordinatesToBeSort.get(index + 1),
							coordinatesToBeSort);

				}

			}

		}

		//Sort the pair of X,Y coordinates according to Y coordinate
		while (coordinatesToBeSort.size() != 0) {

			String min = coordinatesToBeSort.get(0);

			for (int index = 1; index < coordinatesToBeSort.size(); index++) {

				if (Integer.valueOf(String.valueOf(min.charAt(2))) == Integer
						.valueOf(String.valueOf(coordinatesToBeSort.get(index).charAt(2)))) {

					if (lettersOfGrid.size() - Integer
							.valueOf(String.valueOf(coordinatesToBeSort.get(index).charAt(0))) < lettersOfGrid.size()
									- Integer.valueOf(String.valueOf(min.charAt(0)))) {

						String temporary = coordinatesToBeSort.get(index);
						coordinatesToBeSort.set(index, min);
						coordinatesToBeSort.set(0, temporary);
						min = temporary;
					}

				}

			}
			sortedCoordinates.add(coordinatesToBeSort.remove(0));
		}

		return sortedCoordinates;
	}

	public void swap(int currentIndex, int nextIndex, String currentString, String nextString, List<String> points) {

		String temporary = (String) points.get(nextIndex);
		points.set(nextIndex, points.get(currentIndex));
		points.set(currentIndex, temporary);

	}

	/**
	 * return puzzle of the game as a string
	 * */
	public String print() {
		StringBuffer puzzle = new StringBuffer("");

		if (!unEqualRows) {
			for (String row : lettersOfGrid) {
				puzzle.append(row);
				puzzle.append("\n");
			}
		}
		return puzzle.toString();
	}

}
