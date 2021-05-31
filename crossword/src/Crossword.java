import java.util.*;

public class Crossword {
    
    public final char[][] characters;
    public final boolean[][] mappedCharacters;
    public final int rows, cols;

    public Crossword(int cols, int rows, ICharacterSelector characterSelector) {
        assert cols >= 1 && rows >= 1 : "Must have 1 or more rows/columns";

        characters = new char[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                characters[i][j] = characterSelector.selectCharacter();
            }
        }
        mappedCharacters = new boolean[rows][cols];
        this.rows = rows;
        this.cols = cols;
    }

    public String renderCrossword() {
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                builder.append(characters[row][col]);
                builder.append(' ');
            }
            builder.append('\n');
        }
        return new String(builder);
    }

    public String renderCrosswordRow(int row) {
        StringBuilder rowString = new StringBuilder();
        for (int col = 0; col < cols; col++) {
            rowString.append(characters[row][col]);
            rowString.append(' ');
        }
        return new String(rowString);
    }

    public boolean testWordAvailability(String word, CrosswordDirection direction, int row, int col) {
        int endLengthRow = row + direction.getRowChange() * word.length();
        int endLengthCol = col + direction.getColChange() * word.length();
        // Out of bounds
        if (!(endLengthRow >= 0 && endLengthRow < rows)) return false;
        if (!(endLengthCol >= 0 && endLengthCol < cols)) return false;

        for (int i = 0; i < word.length(); i++) {
            if (mappedCharacters[row + direction.getRowChange() * i][col + direction.getColChange() * i]) return false;
        }

        return true;
    }

    public void renderWord(String word, CrosswordDirection direction, int row, int col) {
        int endLengthRow = row + direction.getRowChange() * word.length();
        int endLengthCol = col + direction.getColChange() * word.length();
        assert endLengthRow >= 0 && endLengthRow < rows : "Row out of range";
        assert endLengthCol >= 0 && endLengthCol < cols : "Column out of range";
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            characters[row + direction.getRowChange() * i][col + direction.getColChange() * i] = Character.toUpperCase(c);
            mappedCharacters[row + direction.getRowChange() * i][col + direction.getColChange() * i] = true;
        }
    }

    public interface ICharacterSelector {

        char selectCharacter();

    }

    public enum CrosswordDirection {

        DOWN(0, 1), DOWNLEFT(-1, 1), LEFT(-1, 0), UPLEFT(-1, -1), UP(0, -1), UPRIGHT(1, -1), RIGHT(1, 0), DOWNRIGHT(1, 1);

        private final int colChange, rowChange;

        CrosswordDirection(int colChange, int rowChange) {
            this.colChange = colChange;
            this.rowChange = rowChange;
        }

        public int getColChange() {
            return colChange;
        }

        public int getRowChange() {
            return rowChange;
        }

        public static CrosswordDirection pickRandomDirection(Random random) {
            return CrosswordDirection.values()[random.nextInt(CrosswordDirection.values().length)];
        }
    }

}
