import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client extends JFrame implements Runnable, ActionListener {
    private JTextField textField;
    private JTextArea textArea;
    private JButton sendButton;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Thread chatThread;

    public Client() {
        // Set up UI components
        textField = new JTextField(40);
        textArea = new JTextArea(20, 50);
        sendButton = new JButton("Send");

        sendButton.addActionListener(this);

        // Layout and visibility settings
        setLayout(new FlowLayout());
        add(textField);
        add(new JScrollPane(textArea));
        add(sendButton);

        textArea.setEditable(false);
        setTitle("Client");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        // Establish client connection
        try {
            socket = new Socket("localhost", 12345);

            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Start chat thread
        chatThread = new Thread(this);
        chatThread.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = textField.getText();
        textArea.append("Client: " + msg + "\n");
        textField.setText("");

        try {
            dataOutputStream.writeUTF(msg);
            dataOutputStream.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                String msg = dataInputStream.readUTF();
                textArea.append("Server: " + msg + "\n");
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}
