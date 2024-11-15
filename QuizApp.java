import java.sql.*;
import java.util.*;

public class QuizApp {

    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/quiz_app?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";  // MySQL username
    private static final String PASSWORD = "Kavya@12";  // MySQL password
    private static Connection conn;

    public static void main(String[] args) {
        try {
            // Establish a connection to the database
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the database.");

            // Main menu for CRUD operations
            Scanner scanner = new Scanner(System.in);
            int choice;
            do {
                System.out.println("\nMenu:");
                System.out.println("1. Add Question");
                System.out.println("2. View Questions");
                System.out.println("3. Update Question");
                System.out.println("4. Delete Question");
                System.out.println("5. Take Quiz");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();
                scanner.nextLine(); // To consume the newline character after the number input

                switch (choice) {
                    case 1:
                        addQuestion(scanner);
                        break;
                    case 2:
                        viewQuestions();
                        break;
                    case 3:
                        updateQuestion(scanner);
                        break;
                    case 4:
                        deleteQuestion(scanner);
                        break;
                    case 5:
                        takeQuiz(scanner);
                        break;
                    case 6:
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } while (choice != 6);

            closeConnection();
            scanner.close();
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    // Add a new quiz question to the database
    private static void addQuestion(Scanner scanner) {
        try {
            System.out.println("\nEnter the question:");
            String question = scanner.nextLine();
            System.out.println("Enter option A:");
            String optionA = scanner.nextLine();
            System.out.println("Enter option B:");
            String optionB = scanner.nextLine();
            System.out.println("Enter option C:");
            String optionC = scanner.nextLine();
            System.out.println("Enter option D:");
            String optionD = scanner.nextLine();
            System.out.println("Enter the correct option (A/B/C/D):");
            String correctOption = scanner.nextLine().toUpperCase();

            String sql = "INSERT INTO questions (question, option_a, option_b, option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, question);
                pstmt.setString(2, optionA);
                pstmt.setString(3, optionB);
                pstmt.setString(4, optionC);
                pstmt.setString(5, optionD);
                pstmt.setString(6, correctOption);
                pstmt.executeUpdate();
                System.out.println("Question added successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Error adding question: " + e.getMessage());
        }
    }

    // View all quiz questions from the database
    private static void viewQuestions() {
        String sql = "SELECT * FROM questions";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\nAll Questions:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Question: " + rs.getString("question"));
                System.out.println("A. " + rs.getString("option_a"));
                System.out.println("B. " + rs.getString("option_b"));
                System.out.println("C. " + rs.getString("option_c"));
                System.out.println("D. " + rs.getString("option_d"));
                System.out.println("Correct Answer: " + rs.getString("correct_option"));
                System.out.println();
            }
        } catch (SQLException e) {
            System.out.println("Error viewing questions: " + e.getMessage());
        }
    }

    // Update an existing quiz question in the database
    private static void updateQuestion(Scanner scanner) {
        try {
            System.out.print("\nEnter the ID of the question to update: ");
            int id = scanner.nextInt();
            scanner.nextLine();  // Consume newline character

            System.out.println("Enter the updated question:");
            String question = scanner.nextLine();
            System.out.println("Enter updated option A:");
            String optionA = scanner.nextLine();
            System.out.println("Enter updated option B:");
            String optionB = scanner.nextLine();
            System.out.println("Enter updated option C:");
            String optionC = scanner.nextLine();
            System.out.println("Enter updated option D:");
            String optionD = scanner.nextLine();
            System.out.println("Enter updated correct option (A/B/C/D):");
            String correctOption = scanner.nextLine().toUpperCase();

            String sql = "UPDATE questions SET question = ?, option_a = ?, option_b = ?, option_c = ?, option_d = ?, correct_option = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, question);
                pstmt.setString(2, optionA);
                pstmt.setString(3, optionB);
                pstmt.setString(4, optionC);
                pstmt.setString(5, optionD);
                pstmt.setString(6, correctOption);
                pstmt.setInt(7, id);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Question updated successfully.");
                } else {
                    System.out.println("No question found with the provided ID.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error updating question: " + e.getMessage());
        }
    }

    // Delete a quiz question from the database
    private static void deleteQuestion(Scanner scanner) {
        try {
            System.out.print("\nEnter the ID of the question to delete: ");
            int id = scanner.nextInt();

            String sql = "DELETE FROM questions WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Question deleted successfully.");
                } else {
                    System.out.println("No question found with the provided ID.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error deleting question: " + e.getMessage());
        }
    }

    // Allow the user to take the quiz
    private static void takeQuiz(Scanner scanner) {
        List<Question> questions = fetchQuestions();
        if (questions.isEmpty()) {
            System.out.println("No questions available for the quiz.");
            return;
        }

        int score = 0;
        for (Question question : questions) {
            System.out.println(question.getQuestion());
            System.out.println("A. " + question.getOptionA());
            System.out.println("B. " + question.getOptionB());
            System.out.println("C. " + question.getOptionC());
            System.out.println("D. " + question.getOptionD());
            System.out.print("Your answer (A/B/C/D): ");
            String answer = scanner.nextLine().toUpperCase();

            if (answer.equals(question.getCorrectOption())) {
                System.out.println("Correct!");
                score++;
            } else {
                System.out.println("Wrong! The correct answer was: " + question.getCorrectOption());
            }
            System.out.println();
        }

        System.out.println("Your total score is: " + score + "/" + questions.size());
    }

    // Fetch all quiz questions from the database
    private static List<Question> fetchQuestions() {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String question = rs.getString("question");
                String optionA = rs.getString("option_a");
                String optionB = rs.getString("option_b");
                String optionC = rs.getString("option_c");
                String optionD = rs.getString("option_d");
                String correctOption = rs.getString("correct_option");

                questions.add(new Question(id, question, optionA, optionB, optionC, optionD, correctOption));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching questions: " + e.getMessage());
        }
        return questions;
    }

    // Close database connection
    private static void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    // Question class to store question data
    static class Question {
        private int id;
        private String question;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private String correctOption;

        public Question(int id, String question, String optionA, String optionB, String optionC, String optionD, String correctOption) {
            this.id = id;
            this.question = question;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.correctOption = correctOption;
        }

        public String getQuestion() {
            return question;
        }

        public String getOptionA() {
            return optionA;
        }

        public String getOptionB() {
            return optionB;
        }

        public String getOptionC() {
            return optionC;
        }

        public String getOptionD() {
            return optionD;
        }

        public String getCorrectOption() {
            return correctOption;
        }
    }
}
