public class BoardTest {
    public static void main(String[] args) {
        BoardDAO boardDAO = new BoardDAO();

        // 게시글 추가 테스트
        boardDAO.addPost("Test Title", "This is a test post.", "TestAuthor");

        // 게시글 목록 가져오기 테스트
        System.out.println("=== 게시글 목록 ===");
        for (String post : boardDAO.getPosts()) {
            System.out.println(post);
        }
    }
}
