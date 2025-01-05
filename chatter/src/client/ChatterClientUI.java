package client;

import java.io.*;
import java.net.InetAddress;

import javax.swing.*;

import client.commands.CommandManager;
import client.util.ColorPane;

import java.awt.*;
import java.awt.event.*;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

// TODO convert this to a subclass of JFrame and away from being a singleton
public class ChatterClientUI {
    
    private static JFrame frame;
    private static ColorPane messageHistory;
    private static TextField messageInput;

    private static JTextField hostField;
    private static JTextField portField;
    private static JTextField usernameField;
    private static ChatterServerConnectionDetails details;
    
    private ChatterClientUI() { }

    private static void handleConnectClick() {
        String hostText = hostField.getText();
        String portText = portField.getText();
        String usernameText = usernameField.getText();
        try {
                details.host = InetAddress.getByName(hostText);
            
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error on host address: " + ex.getMessage());
                return;
            }
        try {
            details.port = Integer.parseInt(portText);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error on port: " + ex.getMessage());
            return;
        }
        try {
            if (!usernameText.matches("^[a-zA-Z0-9]{1,16}$")) {
                throw new Exception("Username does not match required format");
            }
            details.username = usernameText;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error on username: " + ex.getMessage());
            return;
        }
    }

    public static ChatterServerConnectionDetails getConnectionDetails() {
        JFrame frame = new JFrame("Connection details");
        frame.setLayout(new GridBagLayout());
        frame.setResizable(true);
        frame.setLocationRelativeTo(null); // center
        
        // Label
        JLabel label = new JLabel("Connection details");
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(new Font("Helvetica", Font.PLAIN, 24));
        GridBagConstraints labelCons = new GridBagConstraints();
        labelCons.gridx = 0;
        labelCons.gridy = 0;
        labelCons.gridwidth = 2;
        labelCons.insets = new Insets(25, 25, 25, 25);
        frame.add(label, labelCons);

        // Host label
        JLabel hostLabel = new JLabel("Host address");
        GridBagConstraints hostLabelCons = new GridBagConstraints();
        hostLabelCons.gridx = 0;
        hostLabelCons.gridy = 1;
        hostLabelCons.anchor = GridBagConstraints.EAST;
        hostLabelCons.weightx = 1.0d;
        hostLabelCons.insets = new Insets(0, 25, 25, 0);
        frame.add(hostLabel, hostLabelCons);

        // Host address input
        hostField = new JTextField();
        hostField.setColumns(25);
        GridBagConstraints hostCons = new GridBagConstraints();
        hostCons.ipadx = 25;
        hostCons.ipady = 25;
        hostCons.insets = new Insets(5, 25, 25, 25);
        hostCons.gridx = 1;
        hostCons.gridy = 1;
        hostCons.weightx = 4.0d;
        frame.add(hostField, hostCons);

        // Port label
        JLabel portLabel = new JLabel("Port");
        GridBagConstraints portLabelCons = new GridBagConstraints();
        portLabelCons.gridx = 0;
        portLabelCons.gridy = 2;
        portLabelCons.anchor = GridBagConstraints.EAST;
        portLabelCons.weightx = 1.0d;
        portLabelCons.insets = new Insets(0, 25, 25, 0);
        frame.add(portLabel, portLabelCons);

        // Port input
        portField = new JTextField();
        portField.setEditable(true);
        portField.setColumns(25);
        GridBagConstraints portCons = new GridBagConstraints();
        portCons.ipadx = 25;
        portCons.ipady = 25;
        portCons.insets = new Insets(5, 25, 25, 25);
        portCons.gridx = 1;
        portCons.gridy = 2;
        frame.add(portField, portCons);

        // Username label
        JLabel usernameLabel = new JLabel("Username");
        GridBagConstraints usernameLabelCons = new GridBagConstraints();
        usernameLabelCons.gridx = 0;
        usernameLabelCons.gridy = 3;
        usernameLabelCons.anchor = GridBagConstraints.EAST;
        usernameLabelCons.weightx = 1.0d;
        usernameLabelCons.insets = new Insets(0, 25, 25, 0);
        frame.add(usernameLabel, usernameLabelCons);

        // Username input
        usernameField = new JTextField();
        usernameField.setColumns(25);
        GridBagConstraints usernameCons = new GridBagConstraints();
        usernameCons.ipadx = 25;
        usernameCons.ipady = 25;
        usernameCons.insets = new Insets(5, 25, 25, 25);
        usernameCons.gridx = 1;
        usernameCons.gridy = 3;
        frame.add(usernameField, usernameCons);

        details = new ChatterServerConnectionDetails();

        // Submit button
        JButton button = new JButton("Submit");
        GridBagConstraints buttonCons = new GridBagConstraints();
        buttonCons.ipadx = 25;
        buttonCons.ipady = 25;
        buttonCons.insets = new Insets(25, 25, 25, 25);
        buttonCons.gridx = 0;
        buttonCons.gridy = 4;
        buttonCons.gridwidth = 2;
        frame.add(button, buttonCons);
        
        button.addMouseListener(new MouseListener() {
             public void mouseClicked(MouseEvent e) {
                 handleConnectClick();
            }
            public void mouseEntered(MouseEvent e) { }
            public void mouseExited(MouseEvent e) { }
            public void mousePressed(MouseEvent e) { }
            public void mouseReleased(MouseEvent e) { }
        });
        KeyListener connectClickHandler = new KeyListener() {
            public void keyTyped(KeyEvent e) { }
            public void keyPressed(KeyEvent e) { }
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleConnectClick();
                }
            }
        };
        button.addKeyListener(connectClickHandler);
        usernameField.addKeyListener(connectClickHandler);
        portField.addKeyListener(connectClickHandler);
        hostField.addKeyListener(connectClickHandler);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        while (details.host == null || details.port == -1 || details.username == null) { System.out.print(""); }
        frame.dispose();
        return details;
    }

    /**
     * Clears the screen and prints the header
     * @param server
     * @throws IOException
     */
    public static void init(ChatterClient client, ChatterServerConnection server) throws Exception {
        frame = new JFrame("Messenger client: " + server.getUsername());
        frame.setLayout(new GridBagLayout());
        frame.setSize(1280, 720);
        frame.setResizable(true);

        // Message history
        messageHistory = new ColorPane();
        messageHistory.setFont(new Font("Helvetica", Font.BOLD, 14));
        messageHistory.setEditable(false);
        GridBagConstraints messageHistoryConstraints = new GridBagConstraints();
        messageHistoryConstraints.gridx = 0;
        messageHistoryConstraints.gridy = 0;
        messageHistoryConstraints.gridwidth = 1;
        messageHistoryConstraints.gridheight = 1;
        messageHistoryConstraints.fill = GridBagConstraints.BOTH;
        messageHistoryConstraints.weighty = 10.0d;
        messageHistoryConstraints.weightx = 1.0d;
        frame.add(new JScrollPane(messageHistory), messageHistoryConstraints);

        // Send message bar
        messageInput = new TextField();
        messageInput.setFont(new Font("Helvetica", Font.PLAIN, 24));
        messageInput.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) { }
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        String content = messageInput.getText();
                        messageInput.setText("");
                        if (content.startsWith("/")) {
                            CommandManager.handleCommand(content, client);
                            return;
                        }
                        server.sendMessage(content);
                        ChatterClientMessage selfMessage = new ChatterClientMessage(server.getSelfUser(false), content);
                        showMessage(selfMessage);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            public void keyTyped(KeyEvent e) { }
        });
        GridBagConstraints messageInputConstraints = new GridBagConstraints();
        messageInputConstraints.gridx = 0;
        messageInputConstraints.gridy = 1;
        messageInputConstraints.gridwidth = 1;
        messageInputConstraints.gridheight = 1;
        messageInputConstraints.fill = GridBagConstraints.BOTH;
        messageInputConstraints.weighty = 1.0d;
        messageInputConstraints.weightx = 1.0d;
        frame.add(messageInput, messageInputConstraints);

        frame.setVisible(true);
        SwingUtilities.invokeLater(() -> {
            frame.requestFocus();
        });

        String headerText = getHeaderText(server);
        System.out.println(ansi().eraseScreen().fg(YELLOW).bold().a(headerText).newline().reset());
        showMessage(headerText);
    }

    public static synchronized void showMessage(String message) {
        messageHistory.setEditable(true);
        messageHistory.appendANSI(message + "\n");
        messageHistory.setEditable(false);
    }
    
    public static synchronized void showMessage(ChatterClientMessage message) throws IOException {
        if (message.content.length() == 0 || message.content.contains("\n")) {
            return;
        }
        messageHistory.setEditable(true);
        if (message.sender.username.equals(message.sender.server.getSelfUser(false).username)) {
            // Sender is self
            messageHistory.appendANSI(ansi().fgRed().a(message.sender.username + ": ").fgBlack().a(message.content).newline().toString());
        } else {
            // Sender is someone else
            messageHistory.appendANSI(ansi().fgGreen().a(message.sender.username + ": ").fgBlack().a(message.content).newline().toString());
        }
        messageHistory.setEditable(false);
    }

    public static String getHeaderText(ChatterServerConnection server) throws IOException {
        return String.format("Connected to %s on port %s, %s connected users", server.socket.getInetAddress(), server.socket.getPort(), server.getConnectedUsers(true).size());
    }

    public static JFrame getFrame() {
        return frame;
    }

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
