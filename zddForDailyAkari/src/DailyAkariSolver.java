import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DailyAkariSolver {
    
    public DailyAkari dailyAkari;
    public int height;
    public int width;
    public ZDD<Integer> zdd;

    static final List<Direction> DIRECTIONS = Arrays.asList(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);

    public DailyAkariSolver(DailyAkari dailyAkari) {
        this.dailyAkari = dailyAkari;
        this.height = dailyAkari.height;
        this.width = dailyAkari.width;
        ArrayList<Integer> coodinates = new ArrayList<>();
        for (int c = 0; c < height * width; c++) coodinates.add(c);
        this.zdd = new ZDD<>(coodinates);
    }
    
    public ZDD<Integer>.ZDD_Node solve() {

        dailyAkari.resetBoard();
        System.out.println("start solve...");

        ArrayList<Integer> blankCoodinates = new ArrayList<>();
        for (int c = 0; c < height * width; c++) {
            int i = c / width;
            int j = c % width;
            if (dailyAkari.isBlank(i, j)) blankCoodinates.add(c);    
        }
        ZDD<Integer>.ZDD_Node answers = zdd.powerSet(blankCoodinates);
        
        for (int c = 0; c < height * width; c++) {
            int i = c / width;
            int j = c % width;
            if (dailyAkari.isBlank(i, j)) {
                answers = answers.hint_range(getCrossBlanks(c), 1, 2);
            } else {
                int ni, nj;
                ni = i + Direction.DOWN.di;
                nj = j + Direction.DOWN.dj;
                if (dailyAkari.isInBoard(ni, nj)) {
                    int nc = ni * width + nj;
                    answers = answers.hint_le(getDownBlanks(nc), 1);
                }
                ni = i + Direction.RIGHT.di;
                nj = j + Direction.RIGHT.dj;
                if (dailyAkari.isInBoard(ni, nj)) {
                    int nc = ni * width + nj;
                    answers = answers.hint_le(getRightBlanks(nc), 1);
                }
                if (dailyAkari.isHintBlock(i, j)) {
                    answers = answers.hint(getNeighbourBlanks(c), dailyAkari.getHint(i, j));
                }
            }
        }

        for (int i = 0; i < height; i++) {
            int j = 0;
            int c = i * width + j;
            if (dailyAkari.isBlank(i, j)) {
                answers = answers.hint_le(getRightBlanks(c), 1);
            }
        }
        
        for (int j = 0; j < width; j++) {
            int i = 0;
            int c = i * width + j;
            if (dailyAkari.isBlank(i, j)) {
                answers = answers.hint_le(getDownBlanks(c), 1);
            }
        }

        System.out.println("collect. the board has " + answers.size() + " answers.");

        return answers;
    }

    private ArrayList<Integer> getCrossBlanks(int c) {
        int i = c / width;
        int j = c % width;
        ArrayList<Integer> crossBlanks = new ArrayList<>(Arrays.asList(c));
        for (Direction d : DIRECTIONS) {
            int ni = i + d.di;
            int nj = j + d.dj;
            while (dailyAkari.isInBoard(ni, nj) && dailyAkari.isBlank(ni, nj)) {
                crossBlanks.add(ni * width + nj);
                ni += d.di;
                nj += d.dj;
            }
        }
        return crossBlanks;
    }

    private ArrayList<Integer> getDownBlanks(int c) {
        int i = c / width;
        int j = c % width;
        ArrayList<Integer> downBlanks = new ArrayList<>();
        while (dailyAkari.isInBoard(i, j) && dailyAkari.isBlank(i, j)) {
            downBlanks.add(i * width + j);
            i += Direction.DOWN.di;
            j += Direction.DOWN.dj;
        }
        return downBlanks;
    }

    private ArrayList<Integer> getRightBlanks(int c) {
        int i = c / width;
        int j = c % width;
        ArrayList<Integer> downBlanks = new ArrayList<>();
        while (dailyAkari.isInBoard(i, j) && dailyAkari.isBlank(i, j)) {
            downBlanks.add(i * width + j);
            i += Direction.RIGHT.di;
            j += Direction.RIGHT.dj;
        }
        return downBlanks;
    }
    
    private ArrayList<Integer> getNeighbourBlanks(int c) {
        int i = c / width;
        int j = c % width;
        ArrayList<Integer> neighbourBlanks = new ArrayList<>();
        for (Direction d : DIRECTIONS) {
            int ni = i + d.di;
            int nj = j + d.dj;
            if (dailyAkari.isInBoard(ni, nj) && dailyAkari.isBlank(ni, nj)) {
                neighbourBlanks.add(ni * width + nj);
            }
        }
        return neighbourBlanks;
    }
    
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String boardFileName = br.readLine();
        DailyAkari dailyAkari = new DailyAkari(boardFileName);
        dailyAkari.showBoard();

        DailyAkariSolver solver = new DailyAkariSolver(dailyAkari);
        ZDD<Integer>.ZDD_Node answers = solver.solve();

        ZDD_Visualizer<Integer> visualizer = new ZDD_Visualizer<>(solver.zdd);
        visualizer.visualize(answers);
    }
}
