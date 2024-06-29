import java.io.*;

import java.net.InetAddress;
import java.net.Socket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private String battleMode;

    private JFrame frame;
    private JPanel mainPanel;
    private JPanel boardPanel;
    private JPanel messagePanel;
    private JLabel[][] boardLabels;
    private JLabel messageLabel;

    Client() {
        this.battleMode = "Human";  // default
    }

    public static void main(String[] args) {
        Client client = new Client();

        client.connect();

        try {
            String clientName = client.in.readLine();
            if (clientName.equals("Client 1")) client.selectGameMode();

            client.showGameScreen();

            while (true) {
                String boardData = client.in.readLine();
                client.updateBoardLabels(boardData);
                System.out.println(boardData);

                String content = client.in.readLine();
                client.updateMessageLabels(content);
                System.out.println(content);
                System.out.print("\n");

                if (content.equals("Draw") || content.contains("won")) {
                    break;
                }
            }
        } catch (IOException ioException) {
            System.err.println(ioException);
        } finally {
            client.close();
        }
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

    void selectGameMode() {
        int selectedModeIndex = JOptionPane.showOptionDialog(null, "Choose a battle mode:", "Battle Mode",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, Game.battleModes, null);
        
        if (selectedModeIndex == JOptionPane.CLOSED_OPTION) {
            this.close();
            return;
        }

        this.battleMode = Game.battleModes[selectedModeIndex];
        this.out.println(this.battleMode);
    }

    void showGameScreen() {
        frame = new JFrame(String.format("Tic-Tac-Toe (vs. %s)", this.battleMode));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                drawLine(graphics);
            }
        };
        boardPanel.setLayout(new GridLayout(Board.ROW, Board.COL));
        boardPanel.setPreferredSize(new Dimension(300, 300));

        boardLabels = new JLabel[Board.ROW][Board.COL];
        for (int row = 0; row < Board.ROW; row++) {
            for (int col = 0; col < Board.COL; col++) {
                boardLabels[row][col] = new JLabel();
                boardLabels[row][col].setHorizontalAlignment(SwingConstants.CENTER);
                boardLabels[row][col].setFont(new Font("Arial", Font.BOLD, 50));
                boardLabels[row][col].addMouseListener(new CustomMouseAdapter(row, col, out));
                boardPanel.add(boardLabels[row][col]);
            }
        }

        messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());
        messagePanel.setPreferredSize(new Dimension(300, 75));
        messageLabel = new JLabel();
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 30));
        messagePanel.add(messageLabel);

        mainPanel.add(messagePanel, BorderLayout.NORTH);
        mainPanel.add(boardPanel, BorderLayout.CENTER);

        frame.getContentPane().add(mainPanel);
        frame.setSize(300, 375);
        frame.setVisible(true);
    }

    void drawLine(Graphics g) {
        int width = boardPanel.getWidth();
        int height = boardPanel.getHeight();

        g.setColor(Color.BLACK);
        g.drawLine(width / 3, 0, width / 3, height);
        g.drawLine((2 * width) / 3, 0, (2 * width) / 3, height);
        g.drawLine(0, height / 3, width, height / 3);
        g.drawLine(0, (2 * height) / 3, width, (2 * height) / 3);
    }

    void updateBoardLabels(String boardData) {
        for (int row = 0; row < Board.ROW; row++) {
            for (int col = 0; col < Board.COL; col++) {
                int index = row * Board.COL + col;
                String data = String.valueOf(boardData.charAt(index));
                if (data.equals("X") || data.equals("O")) {
                    boardLabels[row][col].setText(data);
                } else {
                    boardLabels[row][col].setText("");
                }
            }
        }
        boardPanel.repaint();
    }

    void updateMessageLabels(String message) {
        messageLabel.setText(message);
    }

    void close() {
        System.out.println("Closing...");
        try {
            if (this.in != null) this.in.close();
            if (this.out != null) this.out.close();
            if (this.socket != null) this.socket.close();
        } catch (IOException ioException) {
            System.err.println(ioException);
        }
    }
}

class CustomMouseAdapter extends MouseAdapter {
    private int row;
    private int col;
    private PrintWriter out;

    public CustomMouseAdapter(int row, int col, PrintWriter out) {
        this.row = row;
        this.col = col;
        this.out = out;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        int index = row * Board.COL + col;
        out.println(index);
    }
}