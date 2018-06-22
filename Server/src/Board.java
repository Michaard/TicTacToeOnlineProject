public abstract class Board {
    protected int DIM;

    protected int[][] board;

    Board(int dim) {
        DIM = dim;

        board = new int[DIM][DIM];
    }

    abstract public boolean isFinished(int i, int j);

    abstract public void clear();

    public void putCharAt(int c, int i, int j) throws ArrayIndexOutOfBoundsException {
        if (i >= DIM || j >= DIM) throw new ArrayIndexOutOfBoundsException();

        board[i][j] = c;
    }

    public boolean isFull() {
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++) {
                if (board[i][j] != 1 && board[i][j] != 2) {
                    return false;
                }
            }
        }
        return true;
    }
}
