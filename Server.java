import java.io.*;
import java.net.*;

public class Server {
    public static final int PORT = 8080;

    private ServerSocket serverSocket;
    private Socket socket1;
    private BufferedReader in1;
    private PrintWriter out1;

    private Socket socket2;
    private BufferedReader in2;
    private PrintWriter out2;

    private String battleMode;
    private Game game;

    public static void main(String[] args) {
        Server server = new Server();
        
        server.start();
        server.connect();
        
        server.setupGame();

        try {
            server.game.start();
        } catch (Exception e) {
            System.err.println(e);
        }
        server.close();
    }

    void start() {
        try {
            this.serverSocket = new ServerSocket(PORT);
            System.out.println("Started: " + this.serverSocket);
        } catch (IOException ioException) {
            System.err.println(ioException);
        }
    }

    void connect() {
        try {
            // Connect the first client
            this.socket1 = this.serverSocket.accept();
            System.out.println("Connection accepted." + this.socket1);

            this.in1 = new BufferedReader(new InputStreamReader(this.socket1.getInputStream()));
            this.out1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.socket1.getOutputStream())), true);
            
            this.out1.println("Client 1");
            this.out1.println("Battle Mode: ");
            battleMode = this.in1.readLine();

            if (!battleMode.equals("Human")) return;

            // Connect the second client (if necessary)
            this.socket2 = this.serverSocket.accept();
            System.out.println("Connection accepted." + this.socket2);

            this.in2 = new BufferedReader(new InputStreamReader(this.socket2.getInputStream()));
            this.out2 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.socket2.getOutputStream())), true);

            this.out2.println("Client 2");

        } catch (IOException ioException) {
            System.err.println(ioException);
        }
    }

    void setupGame() {
        if (battleMode.equals("Random")) {
            this.game = new Game(new RandomPlayer(), new HumanPlayer(this.in1, this.out1));
        } else if (battleMode.equals("Better")) {
            this.game = new Game(new BetterPlayer(0), new HumanPlayer(this.in1, this.out1));
        } else if (battleMode.equals("Expert")) {
            this.game = new Game(new ExpertPlayer(0), new HumanPlayer(this.in1, this.out1));
        } else if (battleMode.equals("Human")) {
            this.game = new Game(new HumanPlayer(this.in1, this.out1), new HumanPlayer(this.in2, this.out2));
        }
    }

    void close() {
        System.out.println("Closing...");
        try {
            if (this.socket1 != null) this.socket1.close();
            if (this.socket2 != null) this.socket2.close();
            if (this.serverSocket != null) this.serverSocket.close();
        } catch (IOException ioException) {
            System.err.println(ioException);
        }
    }
}