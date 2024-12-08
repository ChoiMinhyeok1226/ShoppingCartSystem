import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

class UserDAO {
    public boolean registerUser(String name, String email, String password, String role) {
        String query = "INSERT INTO User (name, email, password, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, role);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean login(String email, String password) {
        String query = "SELECT password FROM User WHERE email = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return storedPassword.equals(password);
            } else {
                System.out.println("해당 이메일이 존재하지 않습니다.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean role_check(String email) {
        String query = "SELECT role FROM User WHERE email = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("role").equals("admin");
            } else {
                System.out.println("해당 이메일이 존재하지 않습니다.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

class DatabaseUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/ShoppingCartSystem";
    private static final String USER = "root";
    private static final String PASSWORD = "0926";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

public class Main {
    public static void main(String[] args) {

        JFrame frame = new JFrame("ShoppingCartSystem");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);

        // CardLayout을 사용하여 패널을 전환
        JPanel cardPanel = new JPanel(new CardLayout());

        JPanel LoginPanel = new JPanel();
        LoginPanel.setLayout(null);
        JPanel RegisterPanel = new JPanel();
        RegisterPanel.setLayout(null);
        JPanel BuyPanel = new JPanel();
        BuyPanel.setLayout(null);
        JPanel BoardPanel = new JPanel(new BorderLayout()); // 게시판 패널

        cardPanel.add(LoginPanel, "LoginPanel");
        cardPanel.add(RegisterPanel, "RegisterPanel");
        cardPanel.add(BuyPanel, "BuyPanel");
        cardPanel.add(BoardPanel, "BoardPanel");

        CardLayout cardLayout = (CardLayout) cardPanel.getLayout();

        // 로그인 패널 구성
        JLabel Email_Login = new JLabel("Email");
        Email_Login.setBounds(100, 100, 100, 20);
        JLabel Password_Login = new JLabel("Password");
        Password_Login.setBounds(100, 150, 100, 20);

        JTextField EmailField_Login = new JTextField();
        EmailField_Login.setBounds(180, 100, 200, 20);
        JPasswordField PasswordField_Login = new JPasswordField();
        PasswordField_Login.setBounds(180, 150, 200, 20);

        JButton Login_Button = new JButton("Login");
        Login_Button.setBounds(100, 300, 100, 20);
        Login_Button.addActionListener(e -> {
            UserDAO userDAO = new UserDAO();
            if (userDAO.login(EmailField_Login.getText(), PasswordField_Login.getText())) {
                JOptionPane.showMessageDialog(null, "Login Success", "Message", JOptionPane.INFORMATION_MESSAGE);
                if (userDAO.role_check(EmailField_Login.getText())) {
                    // 관리자 계정일 경우
                    JOptionPane.showMessageDialog(null, "Welcome Admin", "Message", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    cardLayout.show(cardPanel, "BuyPanel");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Login Fail", "Message", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton Register_Button_Login = new JButton("Register");
        Register_Button_Login.setBounds(250, 300, 100, 20);
        Register_Button_Login.addActionListener(e -> cardLayout.show(cardPanel, "RegisterPanel"));

        LoginPanel.add(Email_Login);
        LoginPanel.add(Password_Login);
        LoginPanel.add(EmailField_Login);
        LoginPanel.add(PasswordField_Login);
        LoginPanel.add(Login_Button);
        LoginPanel.add(Register_Button_Login);

        // 회원가입 패널 구성
        JLabel Name_Register = new JLabel("Name");
        Name_Register.setBounds(100, 100, 100, 20);
        JLabel Email_Register = new JLabel("Email");
        Email_Register.setBounds(100, 150, 100, 20);
        JLabel Password_Register = new JLabel("Password");
        Password_Register.setBounds(100, 200, 100, 20);

        JTextField NameField_Register = new JTextField();
        NameField_Register.setBounds(180, 100, 200, 20);
        JTextField EmailField_Register = new JTextField();
        EmailField_Register.setBounds(180, 150, 200, 20);
        JPasswordField PasswordField_Register = new JPasswordField();
        PasswordField_Register.setBounds(180, 200, 200, 20);

        JButton Register_Button = new JButton("Register");
        Register_Button.setBounds(100, 300, 100, 20);
        Register_Button.addActionListener(e -> {
            UserDAO userDAO = new UserDAO();
            if (userDAO.registerUser(NameField_Register.getText(), EmailField_Register.getText(),
                    PasswordField_Register.getText(), "customer")) {
                JOptionPane.showMessageDialog(null, "Registration Success", "Message", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(cardPanel, "LoginPanel");
            } else {
                JOptionPane.showMessageDialog(null, "Registration Failed", "Message", JOptionPane.ERROR_MESSAGE);
            }
        });

        RegisterPanel.add(Name_Register);
        RegisterPanel.add(Email_Register);
        RegisterPanel.add(Password_Register);
        RegisterPanel.add(NameField_Register);
        RegisterPanel.add(EmailField_Register);
        RegisterPanel.add(PasswordField_Register);
        RegisterPanel.add(Register_Button);

        // 게시판 패널 구성
        JTextArea boardTextArea = new JTextArea();
        boardTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(boardTextArea);
        BoardPanel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            BoardDAO boardDAO = new BoardDAO();
            List<String> posts = boardDAO.getPosts();
            boardTextArea.setText(""); // 기존 내용 지우기
            for (String post : posts) {
                boardTextArea.append(post + "\n\n");
            }
        });
        BoardPanel.add(refreshButton, BorderLayout.SOUTH);

        // 게시판 버튼
        JButton BoardButton = new JButton("Board");
        BoardButton.setBounds(250, 350, 100, 20);
        BoardButton.addActionListener(e -> cardLayout.show(cardPanel, "BoardPanel"));
        RegisterPanel.add(BoardButton);

        frame.add(cardPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

