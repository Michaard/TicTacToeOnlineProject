public class Board3x3 extends Board {

    Board3x3() {
        super(3);
    }

    public boolean isFinished(int i, int j) {
        int c = super.board[i][j];
        if (checkRow(i, c)) return true;
        else if (checkColumn(j, c)) return true;
        else if (checkBiasA(c)) return true;
        else if (checkBiasB(c)) return true;
        return false;
    }

    public void putCharAt(int c, int i, int j) throws ArrayIndexOutOfBoundsException {
        super.putCharAt(c, i, j);
    }

    private boolean checkRow(int row, int c) {
        for (int i = 0; i < 3; i++) {
            if (super.board[row][i] != c) return false;
        }
        return true;
    }

    private boolean checkColumn(int column, int c) {
        for (int i = 0; i < 3; i++) {
            if (super.board[i][column] != c) return false;
        }
        return true;
    }

    private boolean checkBiasA(int c) {
        for (int i = 0; i < 3; i++) {
            if (super.board[i][i] != c)
                return false;
        }
        return true;
    }

    private boolean checkBiasB(int c) {
        int j = 0;
        for (int i = 2; i >= 0; i--, j++) {
            if (super.board[i][j] != c)
                return false;
        }
        return true;
    }

    public void clear() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                super.board[i][j] = 0;
            }
        }
    }
}
