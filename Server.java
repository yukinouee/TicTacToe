import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static final int PORT = 8080;

    private ServerSocket serverSocket;
    private Socket[] clienSockets;

    private BufferedReader in1;
    private PrintWriter out1;

    private BufferedReader in2;
    private PrintWriter out2;

    private String battleMode;
    private Game game;

    public static void main(String[] args) {
        Server server = new Server();
        
        server.start();

        while (true) {
            server.clienSockets = new Socket[2];
            server.connect();
            server.initGame();

            try {
                new GameHandler(server.game, server.clienSockets).start();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
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
            this.clienSockets[0] = this.serverSocket.accept();
            System.out.println("Connection accepted." + this.clienSockets[0]);
            this.clienSockets[0] = this.clienSockets[0];

            this.in1 = new BufferedReader(new InputStreamReader(this.clienSockets[0].getInputStream()));
            this.out1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.clienSockets[0].getOutputStream())), true);
            
            this.out1.println("Client 1");
            this.battleMode = this.in1.readLine();

            if (this.battleMode == null) {
                this.clienSockets[0].close();
                return;
            }

            if (!this.battleMode.equals("Human")) return;

            // Connect the second client (if necessary)
            this.clienSockets[1] = this.serverSocket.accept();
            System.out.println("Connection accepted." + this.clienSockets[1]);

            this.in2 = new BufferedReader(new InputStreamReader(this.clienSockets[1].getInputStream()));
            this.out2 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.clienSockets[1].getOutputStream())), true);

            this.out2.println("Client 2");

        } catch (IOException ioException) {
            System.err.println(ioException);
        }
    }

    void initGame() {
        if (this.battleMode == null) return;
        if (this.battleMode.equals("Random (CPU)")) {
            this.game = new Game(new RandomPlayer(), new HumanPlayer(1, this.in1, this.out1));
        } else if (this.battleMode.equals("Better (CPU)")) {
            this.game = new Game(new BetterPlayer(0), new HumanPlayer(1, this.in1, this.out1));
        } else if (this.battleMode.equals("Expert (CPU)")) {
            this.game = new Game(new ExpertPlayer(0), new HumanPlayer(1, this.in1, this.out1));
        } else if (this.battleMode.equals("Human")) {
            this.game = new Game(new HumanPlayer(0, this.in1, this.out1), new HumanPlayer(1, this.in2, this.out2));
        }
    }

    void close() {
        System.out.println("Closing...");
        try {
            if (this.serverSocket != null) this.serverSocket.close();
        } catch (IOException ioException) {
            System.err.println(ioException);
        }
    }
}

class GameHandler extends Thread {
    private Game game;
    private Socket[] clientSockets;

    GameHandler(Game game, Socket[] clientSockets) {
        this.game = game;
        this.clientSockets = clientSockets;
    }

    @Override
    public void run() {
        game.start();

        for (Socket clientSocket : this.clientSockets) {
            try {
                if (clientSocket != null) clientSocket.close();
            } catch (IOException ioException) {
                System.err.println(ioException);
            }
        }
    }
}