import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BoardDAO {
    // 게시글 추가
    public boolean addPost(String title, String content, String author) {
        String query = "INSERT INTO Board (title, content, author) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setString(3, author);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 게시글 목록 가져오기
    public List<String> getPosts() {
        List<String> posts = new ArrayList<>();
        String query = "SELECT * FROM Board ORDER BY created_at DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String post = String.format("Title: %s\nAuthor: %s\nContent: %s\nDate: %s\n",
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at"));
                posts.add(post);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }
}
