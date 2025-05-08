import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DailyAkari {

    public int height;
    public int width;
    private DailyAkariBoard dailyAkariBoard;

    public DailyAkari(String boardFileName) throws IOException{
        this.dailyAkariBoard = readBoard(boardFileName);
        this.height = dailyAkariBoard.height;
        this.width = dailyAkariBoard.width;
    }

    public DailyAkariBoard readBoard(String boardFileName) throws IOException{
        
        int height = 0;
        int width = 0;
        String[] board = new String[0];

        try(BufferedReader br = new BufferedReader(new FileReader(boardFileName))) {
            String line;
            boolean isFirstLine = true;
            int i = 0;
            while (br.ready()) {
                line = br.readLine();
                if (isFirstLine) {
                    String[] hw = line.split(" ", 2);
                    height = Integer.parseInt(hw[0]);
                    width = Integer.parseInt(hw[1]);
                    board = new String[height];
                    isFirstLine = false;
                } else {
                    board[i++] = line;
                }
            }
        }

        return new DailyAkariBoard(height, width, board);
        
    }

    public String getBoard(int i, int j) {
        return dailyAkariBoard.getBoard(i, j);
    }
    
    public int getHint(int i, int j) {
        if (!isHintBlock(i, j)) return -1;
        return Integer.parseInt(getBoard(i, j));
    }

    public void clickBoard(int i, int j) {
        if (!isInBoard(i, j) || isBlock(i, j)) return;
        if (isBlank(i, j)) {
            dailyAkariBoard.setBoard(i, j, "@");
        } else {
            dailyAkariBoard.setBoard(i, j, ".");
        }
    }

    public boolean isInBoard(int i, int j) {
        return 0 <= i && i < height && 0 <= j && j < width; 
    }

    public boolean isBlock(int i, int j) {
        return !(isBlank(i, j) || isAkari(i, j));
    }

    public boolean isUnHintBlock(int i, int j) {
        return isBlock(i, j) && getBoard(i, j).equals("#");
    }

    public boolean isHintBlock(int i, int j) {
        return isBlock(i, j) && !isUnHintBlock(i, j);
    }

    public boolean isBlank(int i, int j) {
        String s = getBoard(i, j);
        return s.equals(".");
    }

    public boolean isAkari(int i, int j) {
        String s = getBoard(i, j);
        return s.equals("@");
    }

    public void showBoard() {
        System.out.println("Current borad is:");
        for (int i = 0; i < height; i++) {
            String row = "";
            for (int j = 0; j < width; j++) {
                row += getBoard(i, j);
            }
            System.out.println(row);
        }
    }

    public void resetBoard() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (isAkari(i, j)) {
                    clickBoard(i, j);
                }
            }
        }
        System.out.println("board has been reset.");
    }



    private class DailyAkariBoard {
        
        private int height;
        private int width;
        private String[][] board;
        
        public DailyAkariBoard(int height, int width, String[] board) {
            this.height = height;
            this.width = width;
            this.board = new String[height][width];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    setBoard(i, j, board[i].substring(j, j+1));
                }
            }
        }

        private void setBoard(int i, int j, String s) {
            board[i][j] = s;
        }
        
        private String getBoard(int i, int j) {
            return board[i][j];
        }
    }



    // showBoard() テスト用
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String boardFileName = br.readLine();
        DailyAkari dailyAkari = new DailyAkari(boardFileName);
        dailyAkari.showBoard();
        System.out.println(dailyAkari.isHintBlock(0, 3));
    }
}
