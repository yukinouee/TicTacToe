import java.io.*;
import java.net.*;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private JFrame frame; // JFrameを追加
    private JPanel boardPanel; // ボードを表示するためのパネルを追加
    private JLabel[][] boardLabels; // ボードのセルを表示するためのラベルを追加

    public static void main(String[] args) {
        Client client = new Client();
        Scanner scanner = new Scanner(System.in);

        client.connect();

        try {
            String input = client.in.readLine();
            if (input.equals("Client 1")) {
                input = client.in.readLine();
                if (input.equals("Battle Mode: ")) {
                    System.out.print(input);
                    client.out.println(scanner.nextLine());
                }
            }
        } catch (IOException ioException) {
            System.err.println(ioException);
        }

        // Swingコンポーネントの初期化と表示
        client.initializeGUI();

        try {
            while (true) {
                String boardData = client.in.readLine();
                // System.out.println(boardData);

                // ボードの状態を更新して表示
                client.updateBoardLabels(boardData);

                String input = client.in.readLine();
                if (input.equals("0-8の数字を入力してください: ") || input.equals("適切な数字を入力してください")) {
                    System.out.print(input);
                    String str = scanner.nextLine();
                    client.out.println(str);
                }
                if (input.equals("Draw") || input.contains("won")) {
                    System.out.println(input);
                    break;
                }
            }
        } catch (IOException ioException) {
            System.err.println(ioException);
        } finally {
            client.close();
        }

        scanner.close();
    }

    void connect() {
        try {
            InetAddress inetAddress = InetAddress.getByName("localhost");
            System.out.println("Inet Address = " + inetAddress);

            this.socket = new Socket(inetAddress, Server.PORT);
            System.out.println("socket = " + this.socket);

            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream())), true);
        } catch (IOException ioException) {
            System.err.println(ioException);
        }
    }

    void initializeGUI() {
        frame = new JFrame("Tic-Tac-Toe");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ボードを表示するパネルを作成
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawLine(g);
            }
        };
        boardPanel.setLayout(new GridLayout(3, 3));

        // ボードのセルを表示するラベルを作成してパネルに追加
        boardLabels = new JLabel[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                boardLabels[i][j] = new JLabel();
                boardLabels[i][j].setHorizontalAlignment(SwingConstants.CENTER);
                boardLabels[i][j].setFont(new Font("Arial", Font.BOLD, 50));
                boardPanel.add(boardLabels[i][j]);
            }
        }

        frame.getContentPane().add(boardPanel);
        frame.setSize(300, 300);
        frame.setVisible(true);
    }

    void drawLine(Graphics g) {
        int width = boardPanel.getWidth();
        int height = boardPanel.getHeight();

        // 格子の描画
        g.setColor(Color.BLACK);
        g.drawLine(width / 3, 0, width / 3, height);
        g.drawLine((2 * width) / 3, 0, (2 * width) / 3, height);
        g.drawLine(0, height / 3, width, height / 3);
        g.drawLine(0, (2 * height) / 3, width, (2 * height) / 3);
    }

    void updateBoardLabels(String boardData) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                boardLabels[i][j].setText(String.valueOf(boardData.charAt(i * 3 + j)));
            }
        }
    }

    void close() {
        System.out.println("Closing...");
        try {
            if (this.socket != null) this.socket.close();
        } catch (IOException ioException) {
            System.err.println(ioException);
        }
    }
}