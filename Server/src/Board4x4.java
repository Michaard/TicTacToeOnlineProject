public class Board4x4 extends Board {

    Board4x4() {
        super(4);
    }

    public boolean isFinished(int i, int j) {
        int c = super.board[i][j];
        return starCheck(i, j, c);
    }

    public void putCharAt(int c, int i, int j) throws ArrayIndexOutOfBoundsException {
        super.putCharAt(c, i, j);
    }

    public void clear() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                super.board[i][j] = 0;
            }
        }
    }

    private boolean starCheck(int i, int j, int c) {
        if (i < 3) {
            if (super.board[0][j] == c && super.board[1][j] == c && super.board[2][j] == c)
                return true;
        }
        if (i > 0) {
            if (super.board[1][j] == c && super.board[2][j] == c && super.board[3][j] == c)
                return true;
        }
        if (j < 3) {
            if (super.board[i][0] == c && super.board[i][1] == c && super.board[i][2] == c)
                return true;
        }
        if (j > 0) {
            if (super.board[i][1] == c && super.board[i][2] == c && super.board[i][3] == c)
                return true;
        }
        if (i == j && i < 3) {
            if (super.board[0][0] == c && super.board[1][1] == c && super.board[2][2] == c)
                return true;
        }
        if (i == j && i > 0) {
            if (super.board[1][1] == c && super.board[2][2] == c && super.board[3][3] == c)
                return true;
        }
        if (j - i == 1) {
            if (super.board[0][1] == c && super.board[1][2] == c && super.board[2][3] == c)
                return true;
        }
        if (j - i == -1) {
            if (super.board[1][0] == c && super.board[2][1] == c && super.board[3][2] == c)
                return true;
        }
        if (i == 2 && j == 0 || i == 1 && j == 1 || i == 0 && j == 2) {
            if (super.board[2][0] == c && super.board[1][1] == c && super.board[0][2] == c)
                return true;
        }
        if (i == 0 && j == 3 || i == 1 && j == 2 || i == 2 && j == 1) {
            if (super.board[2][1] == c && super.board[1][2] == c && super.board[0][3] == c)
                return true;
        }
        if (i == 1 && j == 2 || i == 2 && j == 1 || i == 3 && j == 0) {
            if (super.board[3][0] == c && super.board[2][1] == c && super.board[1][2] == c)
                return true;
        }
        if (i == 1 && j == 3 || i == 2 && j == 2 || i == 3 && j == 1) {
            if (super.board[3][1] == c && super.board[2][2] == c && super.board[1][3] == c)
                return true;
        }
        return false;
    }
}
