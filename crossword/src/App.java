import java.util.*;
import java.io.*;

import org.fusesource.jansi.*;

public class App {
    public static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static char selectRandomCharacter(Random random) {
        return alphabet.charAt(random.nextInt(alphabet.length()));
    }

    public static List<String> readCrosswordWordsInput(Scanner scanner, Crossword crossword) {
        System.out.print(Ansi.ansi().fg(Ansi.Color.YELLOW).a("Hint: type number to select random words").reset().newline().a("Enter the words you would like to add (separate by commas): "));
        List<String> words;
        String input = scanner.next();
        try {
            int wordCount = Integer.parseInt(input);
            if (wordCount < 1) {
                throw new IllegalArgumentException("At least 1 word is required");
            }
            words = new ArrayList<String>();
            for (int i = 0; i < wordCount; i++) {
                words.add(WordListLoader.selectRandomWord((word) -> {
                    return word.length() >= 3 && word.length() <= Math.min(crossword.rows, crossword.cols);
                }));
            }
            return words;
        } catch (NumberFormatException nfe) {
            input = input.trim().replaceAll("\\s+", "");
            if (input.contains(",")) {
                words = Arrays.asList(input.split(","));
            } else {
                words = new ArrayList<String>();
                words.add(input);
            }
            System.out.print(Ansi.ansi().reset().newline());
            return words;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {

        if (args.length == 1) {
            String inputArg = args[0];
            if (!inputArg.matches("[0-9]+:[0-9]+:[a-zA-Z,]+")) {
                System.out.print(Ansi.ansi().fgRed().a("ERROR: Invalid input! Must follow format rows:cols:wordlist").reset());
                return;
            }
            String[] components = inputArg.split(":");
            int rows;
            int cols;
            List<String> wordlist;
            try {
                rows = Integer.parseInt(components[0]);
                cols = Integer.parseInt(components[1]);
            } catch (NumberFormatException ex) {
                System.out.print(Ansi.ansi().fgRed().a("ERROR: Invalid row/column amount specified! Must follow format rows:cols:wordlist").reset());
                return;
            }
            if (components[2].contains(",")) {
                wordlist = Arrays.asList(components[2].replaceAll("\\s+", "").split(","));
            } else {
                wordlist = Arrays.asList(components[2].replaceAll("\\s+", ""));
            }
            Random random = new Random();
            Crossword crossword = new Crossword(rows, cols, () -> selectRandomCharacter(random));
            for (String word : wordlist) {
                Crossword.CrosswordDirection direction = Crossword.CrosswordDirection.pickRandomDirection(random);
                int randomRow;
                int randomCol;
                do {
                    randomRow = random.nextInt(rows);
                    randomCol = random.nextInt(cols);
                } while (!crossword.testWordAvailability(word, direction, randomRow, randomCol));
                crossword.renderWord(word, direction, randomRow, randomCol);
            }
            for (int row = 0; row < crossword.rows; row++) {
                System.out.println(crossword.renderCrosswordRow(row));
            }
            return;
        }

        AnsiConsole.systemInstall();
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter number of cols: ");
        int cols = scanner.nextInt();
        System.out.print("Enter number of rows: ");
        int rows = scanner.nextInt();

        Random random = new Random();

        Crossword crossword = new Crossword(rows, cols, () -> selectRandomCharacter(random));
        System.out.print(Ansi.ansi().eraseScreen().a("This is your current crossword:").newline().newline());
        for (int row = 0; row < crossword.characters.length; row++) {
            System.out.print(Ansi.ansi().fg(Ansi.Color.RED).cursorRight(4).a(crossword.renderCrosswordRow(row)).newline());
        }
        System.out.print(Ansi.ansi().newline().newline().reset());
        List<String> words = readCrosswordWordsInput(scanner, crossword);
        if (words == null) {
            return;
        }
        String overrideReply = "";
        if (words.size() > (rows + cols) / 2) {
            System.out.print(Ansi.ansi().fg(Ansi.Color.YELLOW).a("WARNING: " + words.size() + " words is not recommended for this crossword size. Continue (y/n) ? ").reset());
            overrideReply = scanner.next();
            while (!overrideReply.toLowerCase().startsWith("y")) {
                if (words.size() > (rows + cols) / 2) {
                    words = readCrosswordWordsInput(scanner, crossword);
                    System.out.print(Ansi.ansi().fg(Ansi.Color.YELLOW).a("WARNING: " + words.size() + " words is not recommended for this crossword size. Continue (y/n) ? ").reset());
                    overrideReply = scanner.next();
                } else {
                    break;
                }
            }
        }

        System.out.print(Ansi.ansi().reset().newline().a("Rendering words ").fg(Ansi.Color.RED).a(words.toString()).reset().a(" on crossword with size ").fg(Ansi.Color.RED).a(rows + "x" + cols).reset().a("...").newline());

        for (String word : words) {
            Crossword.CrosswordDirection direction = Crossword.CrosswordDirection.pickRandomDirection(random);
            int randomRow;
            int randomCol;
            do {
                randomRow = random.nextInt(rows);
                randomCol = random.nextInt(cols);
            } while (!crossword.testWordAvailability(word, direction, randomRow, randomCol));
            crossword.renderWord(word, direction, randomRow, randomCol);
        }

        System.out.print(Ansi.ansi().newline().a("Finalized crossword:").newline().newline().fg(Ansi.Color.RED));
        for (int row = 0; row < crossword.rows; row++) {
            System.out.print(Ansi.ansi().cursorRight(4).a(crossword.renderCrosswordRow(row)).newline());
        }

        System.out.print(Ansi.ansi().reset().newline().newline().a("Crossword with key:").newline().fg(Ansi.Color.RED));
        for (int row = 0; row < crossword.rows; row++) {
            System.out.print(Ansi.ansi().newline().cursorRight(4));
            for (int col = 0; col < crossword.cols; col++) {
                if (crossword.mappedCharacters[row][col]) {
                    System.out.print(Ansi.ansi().fg(Ansi.Color.GREEN));
                } else {
                    System.out.print(Ansi.ansi().fg(Ansi.Color.RED));
                }
                System.out.print(Ansi.ansi().a(crossword.characters[row][col] + " "));
            }
        }

        System.out.print(Ansi.ansi().reset().newline().newline().a("Save to file (y/n) ? "));
        String saveToFileResponse = scanner.next();
        if (saveToFileResponse.toLowerCase().startsWith("y")) {
            System.out.print("Enter filename : ");
            String filename = scanner.next();
            if (filename.length() > 0) {
                File exportFile = new File("./" + filename);
                if (exportFile.exists()) {
                    System.out.print("Override existing file (y/n) ? ");
                    String overrideFileReply = scanner.next();
                    if (overrideFileReply.toLowerCase().startsWith("y")) {
                        if (!exportFile.delete()) {
                            System.out.println("Failed to override file");
                            scanner.close();
                            return;
                        }
                    } else {
                        scanner.close();
                        return;
                    }
                }
                try {
                    exportFile.createNewFile();
                    FileOutputStream out = new FileOutputStream(exportFile);
                    // Write metadata
                    out.write(String.format("[Meta]\nRows: %s\nCols: %s\nWords: %s\n[Crossword]\n", crossword.rows, crossword.cols, words).getBytes());
                    for (int row = 0; row < crossword.rows; row++) {
                        out.write(crossword.renderCrosswordRow(row).concat("\n").getBytes());
                    }
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        scanner.close();
    }

}
