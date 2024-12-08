import java.sql.Connection;
import java.sql.DriverManager;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ShoppingCartSystem", "root", "0926")) {
            System.out.println("Connection Successful!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
