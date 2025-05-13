import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * The {@code IncomeTracker} class allows managing income records
 * by reading and writing them under the [Income] section of a text file.
 * It supports adding, deleting, listing, and calculating total income for a month.
 */
public class IncomeTracker {

    private File file;

    /**
     * Constructs a new {@code IncomeTracker} for the specified file.
     *
     * @param filePath the path to the file used to store income records
     */
    public IncomeTracker(String filePath) {
        this.file = new File(filePath);
    }

    /**
     * Adds a new income entry below the [Income] section in the file.
     *
     * @param date        the date of income (format: yyyy-MM-dd)
     * @param source      the source of the income (e.g., Salary, Bonus)
     * @param amount      the amount of income
     * @param description a description of the income
     */
    public void addIncome(String date, String source, double amount, String description) {
        try {
            List<String> lines = new ArrayList<>(Files.readAllLines(file.toPath()));
            int index = -1;

            // Find the [Income] section
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).trim().equals("[Income]")) {
                    index = i;
                    break;
                }
            }

            if (index == -1) {
                System.out.println("No [Income] section found.");
                return;
            }

            // Add the new income entry
            String newEntry = String.format("%s | %s | %.2f | %s", date, source, amount, description);
            lines.add(index + 1, newEntry);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            System.out.println("Income added.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a specific income entry that exactly matches all fields.
     *
     * @param date        the date of the income
     * @param source      the source of the income
     * @param amount      the amount of income
     * @param description the description of the income
     * @return {@code true} if the income was found and deleted, otherwise {@code false}
     */
    public boolean deleteIncome(String date, String source, double amount, String description) {
        String targetLine = String.format("%s | %s | %.2f | %s", date, source, amount, description).trim();
        try {
            List<String> lines = new ArrayList<>(Files.readAllLines(file.toPath()));
            boolean inIncomeSection = false;
            boolean deleted = false;

            List<String> updatedLines = new ArrayList<>();

            for (String line : lines) {
                if (line.trim().equals("[Income]")) {
                    inIncomeSection = true;
                    updatedLines.add(line);
                    continue;
                }

                if (line.startsWith("[") && !line.trim().equals("[Income]")) {
                    inIncomeSection = false;
                }

                if (inIncomeSection && line.trim().equals(targetLine)) {
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
                System.out.println("Income deleted.");
                return true;
            } else {
                System.out.println("Income not found.");
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Calculates the total income for a specific month.
     *
     * @param month the month in yyyy-MM format (e.g., "2025-05")
     * @return the total income for the given month
     */
    public double calculateTotalForMonth(String month) {
        double total = 0.0;
        boolean inIncomeSection = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("[Income]")) {
                    inIncomeSection = true;
                    continue;
                }

                if (line.startsWith("[") && !line.trim().equals("[Income]")) {
                    inIncomeSection = false;
                }

                if (inIncomeSection && !line.trim().isEmpty()) {
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
     * Displays all income entries from the [Income] section.
     */
    public void showAllIncome() {
        boolean inIncomeSection = false;
        System.out.println("=== [Income] Records ===");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("[Income]")) {
                    inIncomeSection = true;
                    continue;
                }

                if (line.startsWith("[") && !line.trim().equals("[Income]")) {
                    inIncomeSection = false;
                }

                if (inIncomeSection && !line.trim().isEmpty()) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * A simple demo class to test the {@code IncomeTracker}.
 */
class Main2 {
    /**
     * The entry point of the application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        IncomeTracker incomeTracker = new IncomeTracker("expenses.txt");

        incomeTracker.addIncome("2025-05-01", "Salary", 2000.00, "Monthly salary");

        double mayIncome = incomeTracker.calculateTotalForMonth("2025-05");
        System.out.println("Total income for May 2025: $" + mayIncome);
    }
}
