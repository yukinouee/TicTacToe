import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;

public class Game {
    private Board board;
    public Player[] players;
    private int playerIndex;

    Game(Player player1, HumanPlayer player2) {
        this.board = new Board();
        this.players = new Player[]{player1, player2};
        this.playerIndex = 0;  // 0 or 1
    }

    public void start() {
        while (true) {
            Player currentPlayer = this.players[this.playerIndex];
            currentPlayer.play(this.board);
            System.out.println(board.render("Grid"));

            if (this.board.isWin(this.playerIndex)) {
                System.out.println(Board.MARKS.get(this.playerIndex) + " won!");
                break;
            } else if (board.isEnd()) {
                System.out.println("Draw");
                break;
            }

            this.playerIndex ^= 1;
        }
    }
}

class Board {
    static final int HEIGHT = 3;
    static final int WIDTH = 3;

    static final int EMPTY = -1;

    public static final Map<Integer, String> MARKS = new HashMap<Integer, String>() {{
        put(0, "X");
        put(1, "O");
    }};

    private int counter;
    private int[] state;

    Board() {
        this.counter = 0;
        this.state = new int[HEIGHT * WIDTH];

        for (int index = 0; index < HEIGHT * WIDTH; index++) {
            this.state[index] = EMPTY;
        }
    }

    String render(String type) {
        String field = "";

        switch (type) {
            case "Grid":
                field = """
0|1|2
-----
3|4|5
-----
6|7|8
                """;
                break;
            case "Line":
                field = "012345678";
                break;
        }

        for (int index = 0; index < HEIGHT * WIDTH; index++) {
            if (this.state[index] != EMPTY) {
                field = field.replace(Integer.toString(index), MARKS.get(this.state[index]));
            }
        }

        return field;
    }

    boolean move(int index) {
        if (this.state[index] != EMPTY) return false;

        int playerIndex = this.counter % 2;
        this.state[index] = playerIndex;
        this.counter++;

        return true;
    }

    boolean unmove(int index) {
        this.state[index] = EMPTY;
        this.counter--;
        return true;
    }

    boolean isWin(int playerIndex) {
        if (
            (this.state[0] == playerIndex && this.state[1] == playerIndex && this.state[2] == playerIndex) || 
            (this.state[3] == playerIndex && this.state[4] == playerIndex && this.state[5] == playerIndex) || 
            (this.state[6] == playerIndex && this.state[7] == playerIndex && this.state[8] == playerIndex) || 
            (this.state[0] == playerIndex && this.state[3] == playerIndex && this.state[6] == playerIndex) || 
            (this.state[1] == playerIndex && this.state[4] == playerIndex && this.state[7] == playerIndex) || 
            (this.state[2] == playerIndex && this.state[5] == playerIndex && this.state[8] == playerIndex) || 
            (this.state[0] == playerIndex && this.state[4] == playerIndex && this.state[8] == playerIndex) || 
            (this.state[2] == playerIndex && this.state[4] == playerIndex && this.state[6] == playerIndex)
        ) {
            return true;
        }
        return false;
    }

    boolean isEnd() {
        for (int index = 0; index < HEIGHT * WIDTH; index++) {
            if (this.state[index] == EMPTY) return false;
        }
        return true;
    }

    int[] validMoves() {
        ArrayList<Integer> moves = new ArrayList<Integer>();
        for (int index = 0; index < HEIGHT * WIDTH; index++) {
            if (this.state[index] == EMPTY) {
                moves.add(index);
            }
        }
        return moves.stream().mapToInt(x -> x).toArray();
    }
}

class Move {
    int score;
    int moveIndex;

    Move(int score, int moveIndex) {
        this.score = score;
        this.moveIndex = moveIndex;
    }
}

abstract class Player {
    public abstract void play(Board board);
}

class HumanPlayer extends Player {
    private BufferedReader in;
    private PrintWriter out;

    HumanPlayer(BufferedReader in, PrintWriter out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void play(Board board) {
        System.out.print("Human Player: ");
        try {
            while (true) {
                this.out.println(board.render("Line"));
                this.out.println("0-8の数字を入力してください: ");
                String input = this.in.readLine();
                System.out.println(input);
                
                try {
                    int index = Integer.parseInt(input);
                    boolean success = board.move(index);
                    if (success) break;
                    else this.out.println("適切な数字を入力してください");
                } catch (NumberFormatException numberFormatException) {
                    System.err.println(numberFormatException);
                }
            }
        } catch (IOException ioException) {
            System.err.println(ioException);
        }
    }
}

class RandomPlayer extends Player {
    private Random random;

    RandomPlayer() {
        this.random = new Random();
    }

    @Override
    public void play(Board board) {
        int[] validMoves = board.validMoves();
        System.out.println(validMoves.length);
        int index = validMoves[this.random.nextInt(validMoves.length)];
        System.out.println("Random Player(CPU): " + index);
        board.move(index);
    }
}

class BetterPlayer extends Player {
    private Random random;
    private int playerIndex;

    BetterPlayer(int playerIndex) {
        this.random = new Random();
        this.playerIndex = playerIndex;
    }

    @Override
    public void play(Board board) {
        int[] validMoves = board.validMoves();
        for (int index = 0; index < validMoves.length; index++) {
            board.move(validMoves[index]);
            if (board.isWin(this.playerIndex)) {
                System.out.println("Better Player(CPU): " + index);
                return;
            }
            board.unmove(validMoves[index]);
        }

        int index = validMoves[this.random.nextInt(validMoves.length)];
        System.out.println("Better Player(CPU): " + index);
        board.move(index);
    }
}

class ExpertPlayer extends Player {
    private int playerIndex;
    
    ExpertPlayer(int playerIndex) {
        this.playerIndex = playerIndex;
    }

    private Move minimax(Board board, int playerIndex) {
        int maximizePlayerIndex = 0;
        int minimizePlayerIndex = 1;

        int nextPlayerIndex = playerIndex ^ 1;

        if (board.isWin(maximizePlayerIndex)) {
            return new Move(1, Board.EMPTY);
        } else if (board.isWin(minimizePlayerIndex)) {
            return new Move(-1, Board.EMPTY);
        } else if (board.isEnd()) {
            return new Move(0, Board.EMPTY);
        }

        if (playerIndex == maximizePlayerIndex) {
            Move bestMove = new Move(Integer.MIN_VALUE, Board.EMPTY);
            
            int[] validMoves = board.validMoves();
            for (int index = 0; index < validMoves.length; index++) {
                board.move(validMoves[index]);
                Move move = minimax(board, nextPlayerIndex);
                if (bestMove.score < move.score) {
                    bestMove.score = move.score;
                    bestMove.moveIndex = validMoves[index];
                }
                board.unmove(validMoves[index]);
            }

            return bestMove;
        } else {
            Move bestMove = new Move(Integer.MAX_VALUE, Board.EMPTY);
            
            int[] validMoves = board.validMoves();
            for (int index = 0; index < validMoves.length; index++) {
                board.move(validMoves[index]);
                Move move = minimax(board, nextPlayerIndex);
                if (bestMove.score > move.score) {
                    bestMove.score = move.score;
                    bestMove.moveIndex = validMoves[index];
                }
                board.unmove(validMoves[index]);
            }

            return bestMove;
        }
    }

    @Override
    public void play(Board board) {
        Move move = this.minimax(board, this.playerIndex);
        System.out.println("Expert Player: " + move.moveIndex);
        board.move(move.moveIndex);
    }
}