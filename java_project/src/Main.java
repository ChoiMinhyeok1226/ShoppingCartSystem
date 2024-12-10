
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.*;
import java.util.List;

abstract class BaseDAO {

    // 공통적으로 데이터베이스 연결 가져오기
    protected Connection getConnection() throws Exception {
        return DatabaseUtil.getConnection(); // 공통 유틸 클래스 사용
    }

    // PreparedStatement를 사용한 데이터 수정 작업 (INSERT, UPDATE, DELETE)
    protected boolean executeUpdate(String query, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            setParameters(pstmt, params); // 파라미터 설정
            return pstmt.executeUpdate() > 0; // 수정된 행이 1개 이상일 때 true

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // PreparedStatement를 사용한 데이터 조회 작업 (SELECT)
    protected ResultSet executeQuery(String query, Object... params) throws Exception {
        Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query);
        setParameters(pstmt, params); // 파라미터 설정
        return pstmt.executeQuery(); // 호출한 DAO에서 ResultSet 처리
    }

    // PreparedStatement 파라미터 설정 메서드
    public void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}


class UserDAO extends BaseDAO {

    // 회원 등록
    public boolean registerUser(String name, String email, String password, String role) {
        String query = "INSERT INTO User (name, email, password, role) VALUES (?, ?, ?, ?)";
        return executeUpdate(query, name, email, password, role); // BaseDAO 메서드 사용
    }

    // 로그인
    public boolean login(String email, String password) {
        String query = "SELECT password FROM User WHERE email = ?";
        try (ResultSet rs = executeQuery(query, email)) { // BaseDAO 메서드 사용
            if (rs.next()) {
                return rs.getString("password").equals(password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    // 역할 확인
    public boolean roleCheck(String email) {
        String query = "SELECT role FROM User WHERE email = ?";
        try (ResultSet rs = executeQuery(query, email)) { // BaseDAO 메서드 사용
            if (rs.next()) {
                return "admin".equals(rs.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    // 고객 ID 조회
    public int getCustomerID(String email) {
        String query = "SELECT id FROM User WHERE email = ?";
        try (ResultSet rs = executeQuery(query, email)) { // BaseDAO 메서드 사용
            return rs.next() ? rs.getInt("id") : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

class OrderDAO extends BaseDAO {

    // 주문 정보 저장
    public void addOrder(Order order) {
        String orderQuery = "INSERT INTO Orders (customer_id, order_date, total_price, status) VALUES (?, NOW(), ?, ?)";
        String detailsQuery = "INSERT INTO OrderDetails (order_id, product_name, product_id, quantity, price) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement orderStmt = conn.prepareStatement(orderQuery, PreparedStatement.RETURN_GENERATED_KEYS);
             PreparedStatement detailsStmt = conn.prepareStatement(detailsQuery)) {

            // 1. Orders 테이블에 데이터 삽입
            setParameters(orderStmt, order.getCustomerId(), order.getTotalPrice(), "Pending");
            orderStmt.executeUpdate();

            // 2. 생성된 order_id 가져오기
            int orderId = 0;
            try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1); // auto_increment된 order_id
                } else {
                    throw new SQLException("주문 ID 생성 실패");
                }
            }

            // 3. OrderDetails 테이블에 데이터 삽입
            for (OrderDetail detail : order.getOrderDetails()) {
                setParameters(detailsStmt, orderId, detail.getProductName(), detail.getProductId(), detail.getQuantity(), detail.getPrice());
                detailsStmt.addBatch(); // 배치에 추가
            }
            detailsStmt.executeBatch(); // 배치 실행

            System.out.println("주문 정보와 상세 정보가 성공적으로 저장되었습니다.");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("주문 정보를 저장하는 중 오류 발생", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("예기치 못한 오류 발생", e);
        }
    }

    // 고객 주문 목록 가져오기
    public void loadOrderList(int customerId, DefaultTableModel model) {
        String query = "SELECT order_id, order_date, total_price FROM Orders WHERE customer_id = ?";
        try (ResultSet rs = executeQuery(query, customerId)) { // BaseDAO 메서드 사용
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                Timestamp orderDate = rs.getTimestamp("order_date");
                double totalPrice = rs.getDouble("total_price");

                model.addRow(new Object[]{orderId, orderDate, totalPrice});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 주문 상세 목록 가져오기
    public void loadOrderDetailList(int orderId, DefaultTableModel model) {
        String query = "SELECT product_id, product_name, quantity, price FROM OrderDetails WHERE order_id = ?";
        try (ResultSet rs = executeQuery(query, orderId)) { // BaseDAO 메서드 사용
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");

                model.addRow(new Object[]{productId, productName, quantity, price});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

class BoardDAO extends BaseDAO {

    // 게시글 추가
    public boolean addPost(String title, String content, String author) {
        String query = "INSERT INTO Board (title, content, author) VALUES (?, ?, ?)";
        return executeUpdate(query, title, content, author); // BaseDAO 메서드 사용
    }

    // 게시글 목록 가져오기
    public List<String> getPosts() {
        List<String> posts = new ArrayList<>();
        String query = "SELECT * FROM Board ORDER BY created_at DESC";

        try (ResultSet rs = executeQuery(query)) { // BaseDAO 메서드 사용
            while (rs.next()) {
                String post = String.format("Title: %s\nAuthor: %s\nContent: %s\nDate: %s\n",
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at"));
                posts.add(post);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return posts;
    }


}


class AdminInventoryDatabase {
    public DefaultTableModel getInventoryTableModel() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 모든 셀을 수정 불가로 설정
            }
        };
        String query = "SELECT * FROM Inventory";
        Connection conn= null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {

            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            int colcount = rs.getMetaData().getColumnCount();
            for(int c = 1; c<=colcount; c++) {
                tableModel.addColumn(rs.getMetaData().getColumnName(c));
            }

            while (rs.next()) {
                Object[] rowData = new Object[colcount];
                for (int i = 1; i <= colcount; i++) {
                    rowData[i - 1] = rs.getObject(i);
                }
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {

            try {
                pstmt.close();
                conn.close();
            } catch (Exception e2) {
                // TODO: handle exception
            }
        }
        return tableModel;
    }

    public boolean updateProductQuantity(int product_id, int quantity) {
        String selectQuery = "SELECT * FROM Inventory WHERE product_id = ?";
        String updateQuery = "UPDATE Inventory SET quantity = quantity + ? WHERE product_id = ?";
        Connection conn= null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmt2 = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(selectQuery);

            pstmt.setInt(1, product_id);
            rs = pstmt.executeQuery();

            if (rs.next()) { // 존재하면
                try {
                    pstmt2 = conn.prepareStatement(updateQuery);
                    pstmt2.setInt(1, quantity);
                    pstmt2.setInt(2, product_id);
                    pstmt2.executeUpdate();
                    return true; // 수량 업데이트 성공
                }catch (Exception e) {
                    e.printStackTrace();
                    return false; // 에러 발생
                }
            }
            else {
                return false; // 제품이 존재하지 않음
            }
        }catch (Exception e) {
            e.printStackTrace();
            return false; // 에러 발생
        }
    }
    public boolean removeProductQuantity(int product_id, int quantity) {
        String selectQuery = "SELECT * FROM Inventory WHERE product_id = ?";
        String updateQuery = "UPDATE Inventory SET quantity = quantity - ? WHERE product_id = ?";
        Connection conn= null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmt2 = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(selectQuery);

            pstmt.setInt(1, product_id);
            rs = pstmt.executeQuery();

            if (rs.next()) { // 존재하면
                try {
                    pstmt2 = conn.prepareStatement(updateQuery);
                    pstmt2.setInt(1, quantity);
                    pstmt2.setInt(2, product_id);
                    pstmt2.executeUpdate();
                    return true; // 수량 업데이트 성공
                }catch (Exception e) {
                    e.printStackTrace();
                    return false; // 에러 발생
                }
            }
            else {
                return false; // 제품이 존재하지 않음
            }
        }catch (Exception e) {
            e.printStackTrace();
            return false; // 에러 발생
        }
    }

    public boolean newProductQuantity(String productName, String category,String description,int quantity,float price) {
        String insertQuery = "INSERT INTO Inventory (product_name, category, description, quantity,price) VALUES(?, ?, ?, ?, ?)";
        Connection conn= null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(insertQuery);

            pstmt.setString(1, productName);
            pstmt.setString(2, category);
            pstmt.setString(3, description);
            pstmt.setInt(4, quantity);
            pstmt.setFloat(5, price);

            int isInserted = pstmt.executeUpdate();
            return isInserted > 0;
        }catch (Exception e) {
            e.printStackTrace();
            return false; // 에러 발생
        }
    }
    public boolean delProductQuantity(String productName) {
        String delQuery = "DELETE FROM Inventory WHERE product_name = ?";
        Connection conn= null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(delQuery);

            pstmt.setString(1, productName);

            int isDeleted = pstmt.executeUpdate();
            return isDeleted > 0;
        }catch (Exception e) {
            e.printStackTrace();
            return false; // 에러 발생
        }
    }
}


class OrderDetail {
    private int productId;
    private int quantity;
    private double price;
    private String productName;

    // 생성자
    public OrderDetail(int productId, String productName, int quantity, double price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.productName = productName;
    }

    // Getter 및 Setter
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getProductName() { return productName; }
}

class Order {
    private int customerId;
    private double totalPrice;
    private List<OrderDetail> orderDetails;

    // 생성자
    public Order(int customerId, double totalPrice, List<OrderDetail> orderDetails) {
        this.customerId = customerId;
        this.totalPrice = totalPrice;
        this.orderDetails = orderDetails;
    }

    // Getter 및 Setter
    public int getCustomerId() { return customerId; }
    public double getTotalPrice() { return totalPrice; }
    public List<OrderDetail> getOrderDetails() { return orderDetails; }
}






class Item{
    public void loadInventoryData(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        String query = "SELECT * FROM Inventory";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                String category = rs.getString("category");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");

                // 데이터를 테이블 모델에 추가
                tableModel.addRow(new Object[]{productId, productName, category, quantity, price});
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateDatabase(DefaultTableModel CarttableModel) {
        String query = "UPDATE Inventory SET quantity = quantity - ? WHERE product_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < CarttableModel.getRowCount(); i++) {
                int ProductId = (int) CarttableModel.getValueAt(i, 0); // Product ID 가져오기
                int decrementQuantity = (int) CarttableModel.getValueAt(i, 2); // 감소시킬 수량 가져오기
                stmt.setInt(1, decrementQuantity);
                stmt.setInt(2, ProductId);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

class Cart{
    public static boolean isProductInCart(int productId, DefaultTableModel cartTableModel) {
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            int cartProductId = (int) cartTableModel.getValueAt(i, 0);
            if (cartProductId == productId) {
                return true;
            }
        }
        return false;
    }

    public static void updateProductQuantity(int productId, int newQuantity, DefaultTableModel inventoryModel, DefaultTableModel cartModel) {
        double itemPrice = 0.0;
        // inventoryModel (재고 목록 테이블)에서 수량을 업데이트
        for (int i = 0; i < inventoryModel.getRowCount(); i++) {
            int cartProductId = (int) inventoryModel.getValueAt(i, 0);
            itemPrice = (double) inventoryModel.getValueAt(i, 4);
            if (cartProductId == productId) {
                int currentQuantity = (int) inventoryModel.getValueAt(i, 3);  // 재고 테이블에서 수량 가져오기
                inventoryModel.setValueAt(currentQuantity - newQuantity, i, 3); // 재고 수량 업데이트
                break;
            }
        }

        // cartModel (장바구니 테이블)에서 수량을 업데이트
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int cartProductId = (int) cartModel.getValueAt(i, 0);
            double currentPrice = (double) cartModel.getValueAt(i, 3);
            if (cartProductId == productId) {
                int currentQuantity = (int) cartModel.getValueAt(i, 2);  // 장바구니에서 수량 가져오기
                cartModel.setValueAt(currentQuantity + newQuantity, i, 2); // 장바구니 수량 업데이트
                cartModel.setValueAt(currentPrice + itemPrice*newQuantity, i, 3);
                break;
            }
        }
    }
}


public class Main {
    private static DefaultTableModel tableModel;
    private static DefaultTableModel cartTableModel;
    private static JLabel totalPrice;
    private static JLabel totalPrice2;
    private static double sum = 0.0;
    private static int customer_id = 0;
    private static String Email;
    private static JLabel userinfo;
    private static DefaultTableModel orderTableModel;
    private static DefaultTableModel orderdetailTableModel;
    private static DefaultTableModel admintableModel;
    private static JTable inventoryTable;
    private static JTextField newTitle;
    private static JTextArea newContent;

    public static void main(String[] args) {

        JFrame frame = new JFrame("ShoppingCartSystem");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);

        // CardLayout을 사용하여 패널을 전환
        JPanel cardPanel = new JPanel(new CardLayout());  // 수정된 부분: CardLayout을 사용하여 패널 전환을 관리

        JPanel LoginPanel = new JPanel();
        LoginPanel.setLayout(null); // 수정된 부분: null 레이아웃을 사용하여 컴포넌트 위치 지정
        JPanel RegisterPanel = new JPanel();
        RegisterPanel.setLayout(null);
        JPanel BuyPanel = new JPanel();//구매창 패널
        BuyPanel.setLayout(null);
        JPanel CartPanel = new JPanel();//결제창 패널
        CartPanel.setLayout(null);
        JPanel orderPanel = new JPanel();//구매 기록 패널
        orderPanel.setLayout(null);
        JPanel orderdetailPanel = new JPanel();
        orderdetailPanel.setLayout(null);
        JPanel AdminPanel = new JPanel();
        AdminPanel.setLayout(null);
        JPanel BoardPanel = new JPanel(); // 게시판 패널
        BoardPanel.setLayout(null);

        // 각 패널을 cardPanel에 추가
        cardPanel.add(LoginPanel, "LoginPanel");
        cardPanel.add(RegisterPanel, "RegisterPanel");
        cardPanel.add(BuyPanel, "BuyPanel");
        cardPanel.add(CartPanel, "CartPanel");
        cardPanel.add(orderPanel, "OrderPanel");
        cardPanel.add(orderdetailPanel, "OrderdetailPanel");
        cardPanel.add(AdminPanel, "AdminPanel");
        cardPanel.add(BoardPanel, "BoardPanel");

        CardLayout cardLayout = (CardLayout) cardPanel.getLayout();

        // 로그인 패널 구성
        JLabel Email_Login = new JLabel("Email");
        Email_Login.setBounds(300, 100, 100, 30);
        JLabel Password_Login = new JLabel("Password");
        Password_Login.setBounds(300, 150, 100, 30);

        JTextField EmailField_Login = new JTextField();
        EmailField_Login.setBounds(480, 100, 200, 30);
        JPasswordField PasswordField_Login = new JPasswordField();
        PasswordField_Login.setBounds(480, 150, 200, 30);

        JButton Login_Button = new JButton("Login");
        Login_Button.setBounds(350, 300, 100, 40);
        Login_Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UserDAO userDAO = new UserDAO();
                customer_id = userDAO.getCustomerID(EmailField_Login.getText());
                Email = EmailField_Login.getText();
                userinfo.setText(Email +" : "+customer_id);

                if(userDAO.login(EmailField_Login.getText(), PasswordField_Login.getText())){
                    JOptionPane.showMessageDialog(null, "Login Success", "Message", JOptionPane.INFORMATION_MESSAGE);
                    if(userDAO.roleCheck(EmailField_Login.getText())){
                        AdminInventoryDatabase InventoryDatabase = new AdminInventoryDatabase();

                        admintableModel = InventoryDatabase.getInventoryTableModel();
                        inventoryTable.setModel(admintableModel);
                        cardLayout.show(cardPanel, "AdminPanel");
                    }
                    else{
                        tableModel.setRowCount(0);
                        Item item = new Item();
                        item.loadInventoryData(tableModel); // 테이블 모델에 데이터를 로드
                        cardLayout.show(cardPanel, "BuyPanel");//로그인에 성공하고 고객 계정이라면 구매 패널로 이동
                    }
                }
                else
                    JOptionPane.showMessageDialog(null, "Login Fail", "Message", JOptionPane.INFORMATION_MESSAGE);


            }
        });

        JButton Register_Button_Login = new JButton("Register");
        Register_Button_Login.setBounds(550, 300, 100, 40);
        Register_Button_Login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
        Name_Register.setBounds(100, 100, 100, 30);
        JLabel Email_Register = new JLabel("Email");
        Email_Register.setBounds(100, 150, 100, 30);
        JLabel Password_Register = new JLabel("Password");
        Password_Register.setBounds(100, 200, 100, 30);

        JTextField NameField_Register = new JTextField();
        NameField_Register.setBounds(180, 100, 200, 30);
        JTextField EmailField_Register = new JTextField();
        EmailField_Register.setBounds(180, 150, 200, 30);
        JPasswordField PasswordField_Register = new JPasswordField();
        PasswordField_Register.setBounds(180, 200, 200, 30);

        JButton Register_Button = new JButton("Register");
        Register_Button.setBounds(100, 300, 100, 30);
        Register_Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UserDAO userDAO = new UserDAO();
                //회원가입은 고객 계정으로만 가능
                if(userDAO.registerUser(NameField_Register.getText(), EmailField_Register.getText(), PasswordField_Register.getText(), "customer")) {
                    JOptionPane.showMessageDialog(null, "Registration Success", "Message", JOptionPane.INFORMATION_MESSAGE);
                    cardLayout.show(cardPanel, "LoginPanel");
                }
                else
                    JOptionPane.showMessageDialog(null, "Registration Fail", "Message", JOptionPane.INFORMATION_MESSAGE);
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
        JScrollPane boardscrollPane = new JScrollPane(boardTextArea);
        boardscrollPane.setBounds(150,100,700,400);
        BoardPanel.add(boardscrollPane);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            BoardDAO boardDAO = new BoardDAO();
            List<String> posts = boardDAO.getPosts();
            boardTextArea.setText(""); // 기존 내용 지우기
            for (String post : posts) {
                boardTextArea.append(post + "\n\n");
            }
        });
        refreshButton.setBounds(650,30,100,40);
        BoardPanel.add(refreshButton);

        JButton boardBackButton = new JButton("Back");
        boardBackButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "BuyPanel");
        });
        boardBackButton.setBounds(750, 30, 100, 40);
        BoardPanel.add(boardBackButton);

        // 게시판 버튼
        JButton BoardButton = new JButton("Board");
        BoardButton.setBounds(250, 30, 100, 40);
        BoardButton.addActionListener(e -> cardLayout.show(cardPanel, "BoardPanel"));
        BuyPanel.add(BoardButton);


        //관리자 패널 구성
        AdminInventoryDatabase inventoryDatabase = new AdminInventoryDatabase();
        admintableModel = inventoryDatabase.getInventoryTableModel();

        inventoryTable = new JTable(admintableModel);
        JScrollPane adminscrollPane = new JScrollPane(inventoryTable);
        adminscrollPane.setBounds(330,100,600,180);
        AdminPanel.add(adminscrollPane);

        JLabel AdminLabel = new JLabel("Admin Page");
        AdminLabel.setBounds(30, 30, 200, 50);
        AdminLabel.setFont(new Font("Times New Roman", Font.PLAIN | Font.BOLD, 30));
        AdminPanel.add(AdminLabel);

        // 관리자 버튼
        JButton AddProductButton = new JButton("Add Amount");
        AddProductButton.setBounds(30, 100, 130, 40);
        AddProductButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // 제품명과 수량 입력받기
                String product_id = JOptionPane.showInputDialog(null, "Enter Product ID:");
                String quantityInput = JOptionPane.showInputDialog(null, "Enter Quantity:");
                int pid;
                pid = Integer.parseInt(product_id);
                int quantity;
                quantity = Integer.parseInt(quantityInput);

                // 데이터베이스 업데이트
                AdminInventoryDatabase inventoryDatabase = new AdminInventoryDatabase();
                boolean updated = inventoryDatabase.updateProductQuantity(pid, quantity);

                if (updated) {
                    JOptionPane.showMessageDialog(null, "Product quantity updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // 테이블 갱신
                    admintableModel = inventoryDatabase.getInventoryTableModel();
                    inventoryTable.setModel(admintableModel);
                } else {
                    JOptionPane.showMessageDialog(null, "Product not found in the database.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
        AdminPanel.add(AddProductButton);

        JButton RemoveProductButton = new JButton("Reduce Amount");
        RemoveProductButton.setBounds(170, 100, 130, 40);
        RemoveProductButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                // 제품명과 수량 입력받기
                String product_id = JOptionPane.showInputDialog(null, "Enter Product ID:");
                String quantityInput = JOptionPane.showInputDialog(null, "Enter Quantity:");
                int pid;
                pid = Integer.parseInt(product_id);
                int quantity;
                quantity = Integer.parseInt(quantityInput);

                // 데이터베이스 업데이트
                AdminInventoryDatabase inventoryDatabase = new AdminInventoryDatabase();
                boolean updated = inventoryDatabase.removeProductQuantity(pid, quantity);

                if (updated) {
                    JOptionPane.showMessageDialog(null, "Product quantity updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // 테이블 갱신
                    admintableModel = inventoryDatabase.getInventoryTableModel();
                    inventoryTable.setModel(admintableModel);
                } else {
                    JOptionPane.showMessageDialog(null, "Product not found in the database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        AdminPanel.add(RemoveProductButton);

        JButton newProductButton = new JButton("New Product");
        newProductButton.setBounds(30, 170, 130, 40);
        newProductButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                // 제품 정보 입력하기
                String productName = JOptionPane.showInputDialog(null, "Enter Product Name:");
                String category = JOptionPane.showInputDialog(null, "Enter Product Category:");
                String description = JOptionPane.showInputDialog(null, "Enter Product Description:");
                String quantityInput = JOptionPane.showInputDialog(null, "Enter Produce Quantity:");
                String price = JOptionPane.showInputDialog(null, "Enter Product price:");

                int quantity;
                quantity = Integer.parseInt(quantityInput);
                float price_i;
                price_i = Float.parseFloat(price);
                // 데이터베이스 업데이트
                AdminInventoryDatabase inventoryDatabase = new AdminInventoryDatabase();
                boolean updated = inventoryDatabase.newProductQuantity(productName, category,description,quantity,price_i);

                if (updated) {
                    JOptionPane.showMessageDialog(null, "Product quantity updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // 테이블 갱신
                    admintableModel = inventoryDatabase.getInventoryTableModel();
                    inventoryTable.setModel(admintableModel);
                } else {
                    JOptionPane.showMessageDialog(null, "Product not found in the database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        AdminPanel.add(newProductButton);

        JButton delProductButton = new JButton("Delete Product");
        delProductButton.setBounds(170, 170, 130, 40);
        delProductButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                // 제품 정보 입력하기
                String productName = JOptionPane.showInputDialog(null, "Enter Product Name:");

                // 데이터베이스 업데이트
                AdminInventoryDatabase inventoryDatabase = new AdminInventoryDatabase();
                boolean updated = inventoryDatabase.delProductQuantity(productName);

                if (updated) {
                    JOptionPane.showMessageDialog(null, "Product quantity updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // 테이블 갱신
                    admintableModel = inventoryDatabase.getInventoryTableModel();
                    inventoryTable.setModel(admintableModel);
                } else {
                    JOptionPane.showMessageDialog(null, "Product not found in the database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        AdminPanel.add(delProductButton);


        JButton RefreshButton = new JButton("Refresh");
        RefreshButton.setBounds(30, 240, 130, 40);
        RefreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 테이블 데이터 새로고침
                DefaultTableModel newTableModel = inventoryDatabase.getInventoryTableModel();
                inventoryTable.setModel(newTableModel);
            }
        });
        AdminPanel.add(RefreshButton);

        JButton LogoutButton = new JButton("Logout");
        LogoutButton.setBounds(170, 240, 130, 40);
        LogoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 로그인 화면으로 전환
                cardLayout.show(cardPanel, "LoginPanel");
            }
        });
        AdminPanel.add(LogoutButton);

        JButton addBoardButton = new JButton("Add Board");
        addBoardButton.setBounds(30, 350, 130, 40);
        addBoardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BoardDAO boardDAO = new BoardDAO();
                boardDAO.addPost(newTitle.getText(), newContent.getText(), "Admin");
                newTitle.setText("");
                newContent.setText("");
                JOptionPane.showMessageDialog(null, "Board added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });


        JLabel newTitleLabel = new JLabel("New Title");
        newTitleLabel.setBounds(170, 350, 130, 40);
        newTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel newContentLabel = new JLabel("New Content");
        newContentLabel.setBounds(170, 400, 130, 40);
        newContentLabel.setHorizontalAlignment(SwingConstants.CENTER);


        newTitle = new JTextField();
        newTitle.setBounds(330, 350, 600, 40);
        newContent = new JTextArea();
        JScrollPane contentScrollPane = new JScrollPane(newContent);
        contentScrollPane.setBounds(330, 400, 600, 80);

        newTitle.setFont(new Font("", Font.PLAIN, 15));
        newContent.setFont(new Font("", Font.PLAIN, 15));
        newTitleLabel.setFont(new Font("", Font.PLAIN, 15));
        newContentLabel.setFont(new Font("", Font.PLAIN, 15));

        AdminPanel.add(newTitleLabel);
        AdminPanel.add(newContentLabel);
        AdminPanel.add(newTitle);
        AdminPanel.add(contentScrollPane);
        AdminPanel.add(addBoardButton);



        //구매 패널 구성
        String[] columnNames = {"Product ID", "Product Name", "Category", "Quantity", "Price"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 모든 셀에 대해 수정 불가 설정
                return false;
            }
        };
        JTable menuTable = new JTable(tableModel);
        menuTable.getSelectionModel().addListSelectionListener(event -> {
            // 이벤트가 발생한 JTable에서 선택된 행 가져오기
            int selectedRow = menuTable.getSelectedRow();
            // 선택된 행이 있는 경우
            if (selectedRow != -1) {
                int confirmResult = JOptionPane.showConfirmDialog(null,
                        "Do you want to add cart?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirmResult == JOptionPane.YES_OPTION) {
                    double price = (double) tableModel.getValueAt(selectedRow, 4);
                    String quantityInput = JOptionPane.showInputDialog(null,
                            "Enter the quantity:", "Quantity", JOptionPane.QUESTION_MESSAGE);
                    try {
                        int quantity = Integer.parseInt(quantityInput);
                        int productId = (int) tableModel.getValueAt(selectedRow, 0);
                        int Max_quan = (int) tableModel.getValueAt(selectedRow, 3);
                        if(quantity > Max_quan){
                            JOptionPane.showMessageDialog(null, "Quantity Exceeded", "Message", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else if (quantity > 0) {
                            Cart cart = new Cart();
                            if (!cart.isProductInCart(productId, cartTableModel)) {
                                cartTableModel.addRow(new Object[]{
                                        productId,
                                        tableModel.getValueAt(selectedRow, 1),  // 상품명
                                        0,
                                        0.0
                                });
                            }
                            cart.updateProductQuantity(productId, quantity, tableModel,cartTableModel);

                            sum += quantity*price; // 총합 업데이트
                            totalPrice.setText("Total Price : " + sum); // 합계 레이블 업데이트
                            totalPrice2.setText("Total Price : "+sum);
                        }else {
                            JOptionPane.showMessageDialog(null, "Please enter a valid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Invalid quantity. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                } else {
                    System.out.println("You selected NO");
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(menuTable);
        scrollPane.setBounds(150, 100, 700, 300);
        BuyPanel.add(scrollPane);

        totalPrice = new JLabel("Total Price : "+"0");
        totalPrice.setBounds(150, 400, 300, 100);
        totalPrice.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        BuyPanel.add(totalPrice);

        JButton goCart = new JButton("Go Cart");
        goCart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "CartPanel");
            }
        });
        goCart.setBounds(750, 30, 100, 40);
        BuyPanel.add(goCart);

        JButton logout = new JButton("Logout");
        logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirmResult = JOptionPane.showConfirmDialog(null,
                        "Your shopping cart will be cleared upon logging out. Are you sure you want to log out?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirmResult == JOptionPane.YES_OPTION) {
                    sum = 0.0; // 총합 초기화
                    cartTableModel.setRowCount(0); // 카트 테이블 초기화
                    totalPrice.setText("Total Price : 0");// 총합 레이블 초기화
                    totalPrice2.setText("Total Price : 0");
                    cardLayout.show(cardPanel, "LoginPanel");
                }else ;
            }
        });
        logout.setBounds(150, 30, 100, 40);
        BuyPanel.add(logout);

        userinfo = new JLabel();
        userinfo.setBounds(150 ,70 ,100, 40);
        BuyPanel.add(userinfo);

        JButton orderlist = new JButton("Orderlist");
        orderlist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "OrderPanel");
                OrderDAO orderDAO = new OrderDAO();
                orderTableModel.setRowCount(0);
                orderDAO.loadOrderList(customer_id,orderTableModel);
            }
        });
        orderlist.setBounds(650, 30, 100, 40);
        BuyPanel.add(orderlist);

        //주문목록 패널
        String[] orderColumnNames = {"Order ID", "Order date", "Total price"};
        orderTableModel = new DefaultTableModel(orderColumnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable orderTable = new JTable(orderTableModel);
        orderTable.getSelectionModel().addListSelectionListener(event -> {
            int selectedRow = orderTable.getSelectedRow();
            if (selectedRow != -1) {
                orderdetailTableModel.setRowCount(0);
                int orderId = (int) orderTableModel.getValueAt(selectedRow, 0);
                OrderDAO orderDAO = new OrderDAO();
                orderDAO.loadOrderDetailList(orderId, orderdetailTableModel);
                cardLayout.show(cardPanel, "OrderdetailPanel");
            }
        });

        JScrollPane orderScrollPane = new JScrollPane(orderTable);
        orderScrollPane.setBounds(150, 100, 700, 300);
        orderPanel.add(orderScrollPane);

        JButton goBuy = new JButton("Back");
        goBuy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "BuyPanel");
            }
        });
        goBuy.setBounds(750, 30, 100, 40);
        orderPanel.add(goBuy);

        //상세 주문 패널 구성
        String[] orderdetailColumnNames = {"Product ID", "Product Name", "Quantity", "Price"};
        orderdetailTableModel = new DefaultTableModel(orderdetailColumnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable orderdetailTable = new JTable(orderdetailTableModel);

        JScrollPane orderdetailScrollPane = new JScrollPane(orderdetailTable);
        orderdetailScrollPane.setBounds(150, 100, 700, 300);
        orderdetailPanel.add(orderdetailScrollPane);

        JButton goOrder = new JButton("Back");
        goOrder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "OrderPanel");
            }
        });
        goOrder.setBounds(750, 30, 100, 40);
        orderdetailPanel.add(goOrder);

        //카트 패널 구성
        String[] cartColumnNames = {"Product ID", "Product Name", "Quantity", "Price"};
        cartTableModel = new DefaultTableModel(cartColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 셀 수정 불가
            }
        };

        JTable cartTable = new JTable(cartTableModel);
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartScrollPane.setBounds(150, 100, 700, 300);
        CartPanel.add(cartScrollPane);

        JButton payment = new JButton("Payment");
        payment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirmResult = JOptionPane.showConfirmDialog(null,
                        "Total Price: " + sum + "\nWould you like to pay for the items in your shopping cart?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirmResult == JOptionPane.YES_OPTION) {
                    // cartTableModel에서 주문 상세 정보 추출
                    List<OrderDetail> orderDetails = new ArrayList<>();
                    for (int i = 0; i < cartTableModel.getRowCount(); i++) {
                        int productId = (int) cartTableModel.getValueAt(i, 0); // Product ID
                        String productName = (String) cartTableModel.getValueAt(i, 1); // Product Name
                        int quantity = (int) cartTableModel.getValueAt(i, 2); // Quantity
                        double price = (double) cartTableModel.getValueAt(i, 3); // Price

                        // OrderDetail 객체 생성 후 리스트에 추가
                        orderDetails.add(new OrderDetail(productId, productName,quantity, price));
                    }

                    // Order 객체 생성
                    Order order = new Order(customer_id, sum, orderDetails);

                    // 주문 정보를 데이터베이스에 저장
                    Item item = new Item();

                    OrderDAO orderDAO = new OrderDAO();
                    orderDAO.addOrder(order); // 주문 정보를 데이터베이스에 저장하는 메서드 호출

                    // 성공 메시지 표시
                    JOptionPane.showMessageDialog(null, "Your order has been successfully placed.", "Order", JOptionPane.INFORMATION_MESSAGE);

                    //데이터베이스 재고 정보 업데이트
                    item.updateDatabase(cartTableModel);

                    // 재고 정보 새로고침
                    item.loadInventoryData(tableModel);

                    // 카트 비우기 및 UI 업데이트
                    sum = 0.0;
                    totalPrice.setText("Total Price : 0");
                    totalPrice2.setText("Total Price : 0");
                    cartTableModel.setRowCount(0); // 카트 내용 비우기
                    cardLayout.show(cardPanel, "BuyPanel");
                }
            }
        });

        payment.setBounds(750, 430, 100, 40);
        CartPanel.add(payment);

        JButton back = new JButton("Back");
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "BuyPanel");
            }
        });
        back.setBounds(750, 30, 100, 40);
        CartPanel.add(back);

        totalPrice2 = new JLabel("Total Price : "+"0");
        totalPrice2.setBounds(150, 400, 300, 100);
        totalPrice2.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        CartPanel.add(totalPrice2);

        // 프레임에 cardPanel 추가
        frame.add(cardPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
