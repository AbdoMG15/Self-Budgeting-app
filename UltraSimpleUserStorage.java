/**
 * A simple user storage system that allows user registration and login functionality.
 * Users are stored in a serialized file for persistence.
 */
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class UltraSimpleUserStorage {

    /**
     * Represents a user in the system with basic information and authentication capabilities.
     * Implements Serializable to allow object serialization for persistent storage.
     */
    static class User implements Serializable {
        private static final long serialVersionUID = 1L;
        private static final String FILENAME = "users_list.ser";
        private static List<User> usersToSave = new ArrayList<>();
        private static Scanner scanner = new Scanner(System.in);
        
        private int id;
        private String name;
        private String email;
        private String password;
        private static int nextId = 1;

        private BudgetSystem budgetSystem = new BudgetSystem(); // Initialize here

        public User(int id, String name, String email, String password) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.password = password;
        }

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();
            // Ensure budgetSystem is initialized after deserialization
            if (budgetSystem == null) {
                budgetSystem = new BudgetSystem();
            }
            // Initialize transient scanner in BudgetSystem
            if (budgetSystem != null) {
                budgetSystem.initializeTransientFields();
            }
        }

        public BudgetSystem getBudgetSystem() {
            return budgetSystem;
        }

        public static void register() {
            loadUsers();

            String userName, userEmail, userPassword;
            System.out.println("===Registration Page===\n");
            while (true) {
                System.out.println("Enter your name: ");
                userName = scanner.nextLine();
                if (userName.isEmpty()) {
                    System.out.println("Name cannot be empty. Please try again.");
                    continue;
                }
                break;
            }
            while (true) {
                System.out.println("Enter your email: ");
                userEmail = scanner.nextLine();
                if(userEmail.isEmpty()){
                    System.out.println("Email cannot be empty. Please try again.");
                    continue;
                }
                if (authorize(userEmail)) {
                    System.out.println("Email already exists. Please try again.");
                    continue;
                }
                break;
            }
            while(true){
                System.out.println("Enter your password: ");
                userPassword = scanner.nextLine();
                if(userPassword.isEmpty()){
                    System.out.println("Password cannot be empty. Please try again.");
                    continue;
                }
                break;
            }
            User newUser = new User(nextId++, userName, userEmail, userPassword);
            usersToSave.add(newUser);
            saveUsers();
            System.out.println("Registration successful!");
        }

        public static boolean authorize(String email) {
            return authorize(email, null);
        }

        public static boolean authorize(String email, String password) {
            loadUsers();
            
            for (User user : usersToSave) {
                if (user.email.equalsIgnoreCase(email)) {
                    if (password != null) {
                        return user.password.equals(password);
                    }
                    return true;
                }
            }
            return false;
        }

        public static User login() {
            loadUsers();
            System.out.println("===Login Page===\n");
            System.out.println("Enter email: ");
            String email = scanner.nextLine();
            System.out.println("Enter password: ");
            String password = scanner.nextLine();
            
            for (User user : usersToSave) {
                if (user.email.equalsIgnoreCase(email) && user.password.equals(password)) {
                    System.out.println("Login successful!");
                    return user;
                }
            }
            
            System.out.println("Invalid email or password\n");
            return null;
        }

        public static void saveUsers() {
            try (FileOutputStream fos = new FileOutputStream(FILENAME);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(usersToSave);
            } catch (IOException e) {
                System.err.println("Error saving users: " + e.getMessage());
            }
        }

        public static void loadUsers() {
            File file = new File(FILENAME);
            if (!file.exists() || file.length() == 0) {
                usersToSave = new ArrayList<>();
                nextId = 1;
                return;
            }

            try (FileInputStream fis = new FileInputStream(FILENAME);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                Object obj = ois.readObject();
                if (obj != null) {
                    usersToSave = (List<User>) obj;
                    nextId = usersToSave.stream()
                                     .mapToInt(u -> u.id)
                                     .max()
                                     .orElse(0) + 1;
                } else {
                    usersToSave = new ArrayList<>();
                    nextId = 1;
                }
            } catch (IOException | ClassNotFoundException e) {
                usersToSave = new ArrayList<>();
                nextId = 1;
            }
        }

        @Override
        public String toString() {
            return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
        }
    }

    /**
     * Handles all budgeting functionality for a user
     */
    static class BudgetSystem implements Serializable {
        private static final long serialVersionUID = 1L;
        private Map<String, Double> budgets = new HashMap<>();
        private transient Scanner scanner;
        private transient DecimalFormat df;

        public void initializeTransientFields() {
            this.scanner = new Scanner(System.in);
            this.df = new DecimalFormat("#.00");
        }

        public BudgetSystem() {
            initializeTransientFields();
        }

        public void showBudgetMenu() {
            while (true) {
                System.out.println("\n=== Budgeting Menu ===");
                System.out.println("1. Set a new budget");
                System.out.println("2. View current budgets");
                System.out.println("3. Back to Main Menu");
                System.out.print("Enter your choice: ");
                
                try {
                    int choice = Integer.parseInt(scanner.nextLine());
                    switch (choice) {
                        case 1:
                            setNewBudget();
                            break;
                        case 2:
                            viewCurrentBudgets();
                            break;
                        case 3:
                            return;
                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number.");
                }
            }
        }

        private void setNewBudget() {
            System.out.println("\n=== Set New Budget ===");
            System.out.print("Enter budget category: ");
            String category = scanner.nextLine();
            System.out.print("Enter budget amount: ");
            
            try {
                double amount = Double.parseDouble(scanner.nextLine());
                budgets.put(category, amount);
                System.out.println("Budget for " + category + " set to $" + df.format(amount));
                User.saveUsers();
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a number.");
            }
        }

        private void viewCurrentBudgets() {
            System.out.println("\n=== Current Budgets ===");
            if (budgets.isEmpty()) {
                System.out.println("No budgets set yet.");
            } else {
                budgets.forEach((category, amount) -> 
                    System.out.printf("%-20s: $%s%n", category, df.format(amount)));
            }
        }
    }

    static class MainMenu {
        private static Scanner scanner = new Scanner(System.in);

        public static void showMainMenu(User user) {
            while (true) {
                System.out.println("\n=== Main Menu ===");
                System.out.println("1. Budgeting");
                System.out.println("2. Logout");
                System.out.print("Enter your choice: ");
                
                try {
                    int choice = Integer.parseInt(scanner.nextLine());
                    switch (choice) {
                        case 1:
                            // Ensure budget system is properly initialized
                            if (user.getBudgetSystem() == null) {
                                user = new User(user.id, user.name, user.email, user.password);
                            }
                            user.getBudgetSystem().showBudgetMenu();
                            break;
                        case 2:
                            System.out.println("Logging out...");
                            return;
                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number.");
                }
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("***Welcome to Self Budgeting app***");
        while (true) {
            System.out.println("\nWhat would you like to do?");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        User.register();
                        break;
                    case 2:
                        User loggedInUser = User.login();
                        if (loggedInUser != null) {
                            MainMenu.showMainMenu(loggedInUser);
                        }
                        break;
                    case 3:
                        System.out.println("Thank you for using the Self Budgeting app!");
                        System.out.println("Exiting...");
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}