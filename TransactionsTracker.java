import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * The {@code TransactionsTracker} class manages transaction records
 * under the [Transactions] section in a file. It supports adding,
 * deleting, listing, and calculating totals for transactions.
 */
public class TransactionsTracker {

    private File file;

    /**
     * Constructs a new {@code TransactionsTracker} for the given file path.
     *
     * @param filePath the path to the text file storing transactions
     */
    public TransactionsTracker(String filePath) {
        this.file = new File(filePath);
    }

    /**
     * Adds a new transaction under the [Transactions] section in the file.
     * If the section does not exist, it is created.
     *
     * @param date        the date of the transaction (format: yyyy-MM-dd)
     * @param type        the type of the transaction (e.g., Transfer, Purchase)
     * @param amount      the transaction amount
     * @param description a description of the transaction
     */
    public void addTransaction(String date, String type, double amount, String description) {
        try {
            List<String> lines = new ArrayList<>(Files.readAllLines(file.toPath()));
            int sectionIndex = -1;

            // Find [Transactions] section
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).trim().equals("[Transactions]")) {
                    sectionIndex = i;
                    break;
                }
            }

            String newEntry = String.format("%s | %s | %.2f | %s", date, type, amount, description);

            if (sectionIndex == -1) {
                // Section not found – add it at the end
                lines.add("");
                lines.add("[Transactions]");
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
            }

            System.out.println("Transaction added.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a specific transaction by matching all fields.
     *
     * @param date        the date of the transaction
     * @param type        the transaction type
     * @param amount      the transaction amount
     * @param description the description of the transaction
     * @return {@code true} if the transaction was found and deleted, {@code false} otherwise
     */
    public boolean deleteTransaction(String date, String type, double amount, String description) {
        String targetLine = String.format("%s | %s | %.2f | %s", date, type, amount, description).trim();
        try {
            List<String> lines = new ArrayList<>(Files.readAllLines(file.toPath()));
            boolean inTransactionSection = false;
            boolean deleted = false;

            List<String> updatedLines = new ArrayList<>();

            for (String line : lines) {
                if (line.trim().equals("[Transactions]")) {
                    inTransactionSection = true;
                    updatedLines.add(line);
                    continue;
                }

                if (line.startsWith("[") && !line.trim().equals("[Transactions]")) {
                    inTransactionSection = false;
                }

                if (inTransactionSection && line.trim().equals(targetLine)) {
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
                System.out.println("Transaction deleted.");
                return true;
            } else {
                System.out.println("Transaction not found.");
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Calculates the total transaction amount for a given month.
     * Optionally filters by transaction type.
     *
     * @param month      the month in yyyy-MM format (e.g., "2025-05")
     * @param typeFilter the transaction type to filter by (optional; can be {@code null})
     * @return the total amount for the specified month and type
     */
    public double calculateTotalForMonth(String month, String typeFilter) {
        double total = 0.0;
        boolean inTransactionSection = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("[Transactions]")) {
                    inTransactionSection = true;
                    continue;
                }

                if (line.startsWith("[") && !line.trim().equals("[Transactions]")) {
                    inTransactionSection = false;
                }

                if (inTransactionSection && !line.trim().isEmpty()) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) {
                        String date = parts[0].trim();
                        String type = parts[1].trim();
                        String amountStr = parts[2].trim();

                        if (date.startsWith(month) &&
                            (typeFilter == null || type.equalsIgnoreCase(typeFilter))) {
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
     * Displays all transactions listed under the [Transactions] section.
     */
    public void showAllTransactions() {
        boolean inTransactionSection = false;
        System.out.println("=== [Transactions] Records ===");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("[Transactions]")) {
                    inTransactionSection = true;
                    continue;
                }

                if (line.startsWith("[") && !line.trim().equals("[Transactions]")) {
                    inTransactionSection = false;
                }

                if (inTransactionSection && !line.trim().isEmpty()) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * A test class to demonstrate usage of {@code TransactionsTracker}.
 */
class Main3 {
    /**
     * The entry point of the program.
     *
     * @param args the command-line arguments (not used)
     */
    public static void main(String[] args) {
        TransactionsTracker tracker = new TransactionsTracker("expenses.txt");

        tracker.addTransaction("2025-05-03", "Transfer", 200.0, "From checking to savings");

        double mayTotal = tracker.calculateTotalForMonth("2025-05", null);
        System.out.println("Total transactions in May 2025: $" + mayTotal);
    }
}
