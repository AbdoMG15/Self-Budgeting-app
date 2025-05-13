import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Manages expense records by reading and writing to a shared file.
 * Supports adding, deleting, viewing, and calculating total expenses under the [Expense] section.
 */
public class ExpenseTracker {

    private File file;

    /**
     * Constructs a new ExpenseTracker for a specific file.
     *
     * @param filePath the path to the shared file that stores all data
     */
    public ExpenseTracker(String filePath) {
        this.file = new File(filePath);
    }

    /**
     * Adds a new expense to the file under the [Expense] section.
     * If the section does not exist, it creates it.
     *
     * @param date        the date of the expense (format: yyyy-MM-dd)
     * @param type        the category/type of the expense (e.g., "Food", "Transport")
     * @param amount      the amount spent
     * @param description a short description of the expense
     */
    public void addExpense(String date, String type, double amount, String description) {
        try {
            List<String> lines = new ArrayList<>(Files.readAllLines(file.toPath()));
            int sectionIndex = -1;

            // Find [Expense] section
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).trim().equals("[Expense]")) {
                    sectionIndex = i;
                    break;
                }
            }

            String newEntry = String.format("%s | %s | %.2f | %s", date, type, amount, description);

            if (sectionIndex == -1) {
                // Section not found – add it at the end
                lines.add("");
                lines.add("[Expense]");
                lines.add(newEntry);
            } else {
                // Insert new entry below the section
                int insertIndex = sectionIndex + 1;

                // Find the next section to avoid inserting into another section
                while (insertIndex < lines.size() && !lines.get(insertIndex).startsWith("[")) {
                    insertIndex++;
                }

                lines.add(insertIndex, newEntry);
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
                writer.newLine();
            }

            System.out.println("Expense added.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes an expense entry that exactly matches all given fields.
     *
     * @param date        the date of the expense
     * @param category    the category/type of the expense
     * @param amount      the amount spent
     * @param description the description of the expense
     * @return true if the expense was found and deleted, false otherwise
     */
    public boolean deleteExpense(String date, String category, double amount, String description) {
        String targetLine = String.format("%s | %s | %.2f | %s", date, category, amount, description).trim();
        try {
            List<String> lines = new ArrayList<>(Files.readAllLines(file.toPath()));
            boolean inExpenseSection = false;
            boolean deleted = false;

            List<String> updatedLines = new ArrayList<>();

            for (String line : lines) {
                if (line.trim().equals("[Expense]")) {
                    inExpenseSection = true;
                    updatedLines.add(line);
                    continue;
                }

                if (line.startsWith("[") && !line.trim().equals("[Expense]")) {
                    inExpenseSection = false;
                }

                if (inExpenseSection && line.trim().equals(targetLine)) {
                    deleted = true;
                    continue; // Skip this line (delete it)
                }

                updatedLines.add(line);
            }

            if (deleted) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    for (String line : updatedLines) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
                System.out.println("Expense deleted.");
                return true;
            } else {
                System.out.println("Expense not found.");
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Calculates the total amount of expenses recorded for a specific month.
     *
     * @param month the month to calculate for, in "yyyy-MM" format
     * @return the total amount of expenses in the specified month
     */
    public double calculateTotalForMonth(String month) {
        double total = 0.0;
        boolean inExpenseSection = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("[Expense]")) {
                    inExpenseSection = true;
                    continue;
                }

                if (line.startsWith("[") && !line.trim().equals("[Expense]")) {
                    inExpenseSection = false;
                }

                if (inExpenseSection && !line.trim().isEmpty()) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) {
                        String date = parts[0].trim();
                        String amountStr = parts[2].trim();

                        if (date.startsWith(month)) {
                            try {
                                total += Double.parseDouble(amountStr);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid amount: " + amountStr);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return total;
    }

    /**
     * Displays all recorded expenses under the [Expense] section.
     */
    public void showAllExpenses() {
        boolean inExpenseSection = false;
        System.out.println("=== [Expense] Records ===");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("[Expense]")) {
                    inExpenseSection = true;
                    continue;
                }

                if (line.startsWith("[") && !line.trim().equals("[Expense]")) {
                    inExpenseSection = false;
                }

                if (inExpenseSection && !line.trim().isEmpty()) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
/**
 * Demonstrates basic usage of the ExpenseTracker class.
 */
class Main {
    public static void main(String[] args) {
        ExpenseTracker tracker = new ExpenseTracker("all_in_one.txt");

        tracker.addExpense("2025-07-20", "Repair", 200.00, "repaire the car");

        double totalMay = tracker.calculateTotalForMonth("2025-06");
        System.out.println("Total spent in May 2025: $" + totalMay);
    }
}

