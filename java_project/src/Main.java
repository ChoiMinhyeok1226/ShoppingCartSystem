import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {

        JFrame frame = new JFrame("ShoppingCartSystem");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);

        // CardLayout을 사용하여 패널을 전환
        JPanel cardPanel = new JPanel(new CardLayout());  // 수정된 부분: CardLayout을 사용하여 패널 전환을 관리

        JPanel LoginPanel = new JPanel();
        LoginPanel.setLayout(null); // 수정된 부분: null 레이아웃을 사용하여 컴포넌트 위치 지정
        JPanel RegisterPanel = new JPanel();
        RegisterPanel.setLayout(null);

        // 각 패널을 cardPanel에 추가
        cardPanel.add(LoginPanel, "LoginPanel");
        cardPanel.add(RegisterPanel, "RegisterPanel");

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
        Login_Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 로그인 로직 (UserDAO.login 메소드 호출)
                JOptionPane.showMessageDialog(null, "Login Success", "Message", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton Register_Button_Login = new JButton("Register");
        Register_Button_Login.setBounds(250, 300, 100, 20);
        Register_Button_Login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // RegisterPanel로 전환
                cardLayout.show(cardPanel, "RegisterPanel");  // 수정된 부분: "RegisterPanel"로 전환
            }
        });

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
        Register_Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 회원가입 로직 (UserDAO.registerUser 메소드 호출)
                JOptionPane.showMessageDialog(null, "Registration Success", "Message", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        RegisterPanel.add(Name_Register);
        RegisterPanel.add(Email_Register);
        RegisterPanel.add(Password_Register);
        RegisterPanel.add(NameField_Register);
        RegisterPanel.add(EmailField_Register);
        RegisterPanel.add(PasswordField_Register);
        RegisterPanel.add(Register_Button);

        // 프레임에 cardPanel 추가
        frame.add(cardPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
