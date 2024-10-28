import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class Server extends JFrame implements Runnable, ActionListener {
    private JTextField textField;
    private JTextArea textArea;
    private JButton sendButton;

    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Thread chatThread;

    private Connection connection;

    public Server() {
        textField = new JTextField(40);
        textArea = new JTextArea(20, 50);
        sendButton = new JButton("Send");

        sendButton.addActionListener(this);

        setLayout(new FlowLayout());
        add(textField);
        add(new JScrollPane(textArea));
        add(sendButton);

        textArea.setEditable(false);
        setTitle("Server");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        connectToDatabase();
        loadMessageHistory();

        try {
            serverSocket = new ServerSocket(12345);
            socket = serverSocket.accept();

            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        chatThread = new Thread(this);
        chatThread.start();
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:chat.db");
            System.out.println("Database connection established.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMessageHistory() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM messages ORDER BY timestamp")) {
            while (rs.next()) {
                String sender = rs.getString("sender");
                String message = rs.getString("message");
                textArea.append(sender + ": " + message + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveMessage(String sender, String message) {
        String sql = "INSERT INTO messages (sender, message) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, sender);
            pstmt.setString(2, message);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = textField.getText();
        textArea.append("Server: " + msg + "\n");
        textField.setText("");

        try {
            dataOutputStream.writeUTF(msg);
            dataOutputStream.flush();
            saveMessage("Server", msg);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                String msg = dataInputStream.readUTF();
                textArea.append("Client: " + msg + "\n");
                saveMessage("Client", msg);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Server::new);
    }
}
