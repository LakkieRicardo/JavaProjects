import java.util.*;
import java.util.function.Predicate;

public class WordListLoader {
    
    public static final List<String> wordList;

    private WordListLoader() {}

    public static String selectRandomWord(Random random) {
        return wordList.get(random.nextInt(wordList.size()));
    }

    public static String selectRandomWord() {
        return selectRandomWord(new Random());
    }
    
    public static String selectRandomWord(Random random, Predicate<String> tester) {
        String randomWord = selectRandomWord(random);
        if (!tester.test(randomWord)) return selectRandomWord(random, tester);
        return randomWord;
    }

    public static String selectRandomWord(Predicate<String> tester) {
        return selectRandomWord(new Random(), tester);
    }

    static {
        wordList = new ArrayList<String>();
        Scanner wordListScanner = new Scanner(App.class.getResourceAsStream("word-list.txt"));
        while (wordListScanner.hasNextLine()) {
            wordList.add(wordListScanner.nextLine());
        }
    }

}
