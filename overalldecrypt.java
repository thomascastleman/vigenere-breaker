import java.util.*;

class Overall {

	private static Map<Integer, Character> letterEquiv = new HashMap<Integer, Character>();
	private static Map<Character, Integer> numEquiv = new HashMap<Character, Integer>();
	private static char[] alphabet = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

	public static Scanner input = new Scanner(System.in);
	public static Scanner confirm = new Scanner(System.in);

	public static void main(String[] args) {
		// initialize hash map of characters to int values, and int to chars
		for (int i = 0; i < 26; i++) {
            numEquiv.put(alphabet[i], i);
            letterEquiv.put(i, alphabet[i]);
        }

        // get input
		String text = "";
        System.out.println("\nEnter text: ");
        if (input.hasNextLine()) {
        	text = input.nextLine();
        }
        text = text.toLowerCase();

        System.out.print("\nDetermining possible key lengths... ");
        ArrayList<Integer> possibleKeyLengths = getKeyLength(text);		// get all possible key lengths
        System.out.println("Done");
        System.out.println(possibleKeyLengths.size() + " key lengths found.");

        while (possibleKeyLengths.size() != 0) {
        	// crack key
        	System.out.println("\nAttempting with key length " + possibleKeyLengths.get(0) + "...");
	        String key = decrypt(text, possibleKeyLengths.get(0));
	        possibleKeyLengths.remove(0);
	        System.out.println("\nKey: " + key);

	        // try decryption
	        System.out.println("\nDecryption Attempt: ");
	        System.out.println(decryptVigenere(text, key));

	        System.out.print("\nSuccessful? (y / n): ");
	        if (confirm.hasNextLine()) {
	        	if ((confirm.nextLine()).equals("y")) {
	        		break;
	        	}
	        }
	        System.out.println("");


        }
	}

	// decrypts a string of text with a keyword
	private static String decryptVigenere(String text, String keyword) {
    	String attempt = "";	// decryption attempt
    	int keyIndex = 0;		// index in keyword
    	for (int i = 0; i < text.length(); i++) {
    		if (numEquiv.get(text.charAt(i)) != null) {		// if char is alphabetic
    			// decrypt
    			attempt += String.valueOf(decryptLetter(text.charAt(i), numEquiv.get(keyword.charAt(keyIndex))));
    			keyIndex = keyIndex + 1 >= keyword.length() ? 0 : keyIndex + 1;
    		} else {
    			// do not decrypt
    			attempt += String.valueOf(text.charAt(i));
    		}
    	}

    	return attempt;
    }

    // decrypts a single letter with a shift
	private static char decryptLetter(char letter, int shift) {
		if (numEquiv.get(letter) != null) {
			int num = numEquiv.get(letter);
			num -= shift;
			if (num < 0) {
				num += 26;
			}
			return letterEquiv.get(num);
		} else {
			return letter;
		}
	}

	// analyzes input and returns most likely key length
	private static ArrayList<Integer> getKeyLength(String text) {
		// initialize combined text
		String combinedText = "";
		for (int i = 0; i < text.length(); i++) {
			// if char is in alphabet
			if (numEquiv.get(text.charAt(i)) != null) {
				// add to combined text (no spaces or punctuation needed)
				combinedText += String.valueOf(text.charAt(i));
			}
		}

		int[] coincidences = new int[combinedText.length()];

		// for all shifts
		for (int index = 1; index < combinedText.length(); index++) {
			int temp = index;
			for (int i = 0; i < combinedText.length(); i++) {
				if (combinedText.charAt(i) == combinedText.charAt(temp)) {
					coincidences[index]++;
				}
				temp = temp + 1 == combinedText.length() ? 0 : temp + 1;
			}
		}

		// display coincidences
		// System.out.println("Coincidences: ");
		// for (int i = 0; i < coincidences.length; i++) {
		// 	System.out.println(coincidences[i]);
		// }

		// get min and max values for scaling
		int min = coincidences[0];
		int max = coincidences[0];
		for (int i = 0; i < coincidences.length; i++) {
			if (coincidences[i] < min) {
				min = coincidences[i];
			} else if (coincidences[i] > max) {
				max = coincidences[i];
			}
		}

		// convert to percentages
		double[] percentages = new double[coincidences.length];
		for (int i = 0; i < coincidences.length; i++) {
			percentages[i] = (float) (coincidences[i] - min) / (max - min);
		}

		// display percentages
		// System.out.println("\nPercentages: ");
		// for (int i = 0; i < percentages.length; i++) {
		// 	System.out.println(percentages[i]);
		// }

		// intervals between high percentages
		ArrayList<Integer> intervals = new ArrayList<Integer>();

		// get intervals
		int previous = 0;
		for (int i = 0; i < percentages.length; i++) {
			if (percentages[i] > 0.75) {				// if in 75th percentile, get interval
				if (i != 0) {
					intervals.add(i - previous);
				}
				previous = i;
			}
		}

		// display intervals
		// System.out.println("\nIntervals: ");
		// for (int i = 0; i < intervals.size(); i++) {
		// 	System.out.println(intervals.get(i));
		// }


		// get mode of intervals
		int occurrences[] = new int[Collections.max(intervals) + 1];
		for (int i = 0; i < intervals.size(); i++) {
			occurrences[intervals.get(i)]++;
		}

		ArrayList<Integer> orderedLengths = new ArrayList<Integer>();	// 
		int highestOccurrenceIndex = 0;

		while (true) {
			for (int i = 0; i < occurrences.length; i++) {
				if (occurrences[i] > occurrences[highestOccurrenceIndex]) {
					highestOccurrenceIndex = i;
				}
			}

			if (occurrences[highestOccurrenceIndex] == 0) {
				break;
			}

			orderedLengths.add(highestOccurrenceIndex);		// add next most likely key length to orderedlengths
			occurrences[highestOccurrenceIndex] = 0;
		}

		return orderedLengths;
	}

	// returns key of cipher, given the key length
	private static String decrypt(String text, int keylength) {
		// groups of characters from every nth position in ciphertext
		String[] groups = new String[keylength];
		for (int i = 0; i < groups.length; i++) {
			groups[i] = "";
		}

		// populate groups 
		int index = 0;
		for (int i = 0; i < text.length(); i++) {
			// if char in alphabet
			if (numEquiv.get(text.charAt(i)) != null) {
				groups[index] += String.valueOf(text.charAt(i));		// add character to group
				index = index + 1 > keylength - 1 ? 0 : index + 1;		// increment index and wrap around once reached key length
			}
		}

		// display groups
		// System.out.println("Groups: ");
		// for (int i = 0; i < groups.length; i++) {
		// 	System.out.println(i + ": " + groups[i] + "\n");
		// }

		// get key by running frequency analysis on each group
		String key = "";
		for (int i = 0; i < groups.length; i++) {
			String letter = String.valueOf(letterEquiv.get(analyze(groups[i])));
			System.out.println("Frequency analysis result for group " + i + ": " + letter);
			key += letter;
		}

		return key;
	}

	// analyzes frequency in input, returns best shift
	private static int analyze(String input) {
		// total number of alphabetic characters in input
		int total = 0;
		// actual percentage letter frequencies in English
		double frequencies[] = {8.167, 1.492, 2.782, 4.253, 12.702, 2.228, 2.015, 6.094, 6.966, 0.153, 0.772, 4.025, 2.406, 6.749, 7.507, 1.929, 0.095, 5.987, 6.327, 9.056, 2.758, 0.978, 2.360, 0.150, 1.974, 0.074};
		// array of frequencies from user input
		double inputFreq[] = new double[26];


		// get letter frequencies in input
		for (int i = 0; i < input.length(); i++) {						// for every character in input
			for (int k = 0; k < alphabet.length; k++) {
				if (input.charAt(i) == alphabet[k]) {					// if character is in alphabet
					inputFreq[numEquiv.get(input.charAt(i))]++;			// add to that character's frequency
					total++;											// increment total number of characters found
					break;
				}
			}
		}

		// convert frequencies to percentages
		for (int i = 0; i < inputFreq.length; i++) {
			inputFreq[i] = (inputFreq[i] / total) * 100.0;
		}

		// calculate all losses
		double losses[] = new double[26];					// array to record total difference in percent frequency between input and true frequencies
		for (int shift = 0; shift < 26; shift++) {			// for every possible shift
			int index = shift;

			// add loss for each character frequency to total loss
			for (double value : frequencies) {
				losses[shift] += Math.abs(value - inputFreq[index]);			// add difference between percentage values to losses
				index = index + 1 >= inputFreq.length ? 0 : index + 1;		// increment index, if at end of array, go to 0th index
			}
		}

		// get index (shift value) of lowest loss
		int max = 0;
		for (int i = 0; i < losses.length; i++) {
			if (losses[i] < losses[max]) {
				max = i;
			}
		}

		return max;
	}
}