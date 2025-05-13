/**
 * A simple user storage system that allows user registration and login functionality.
 * Users are stored in a serialized file for persistence.
 */
import java.io.*;
import java.util.ArrayList;
import java.util.List;
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

        /**
         * Constructs a new User with the specified details.
         *
         * @param id the user's unique identifier
         * @param name the user's name
         * @param email the user's email address
         * @param password the user's password
         */
        public User(int id, String name, String email, String password) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.password = password;
        }

        /**
         * Handles the user registration process by collecting user information,
         * validating inputs, and saving the new user to storage.
         */
        public static void register() {
            loadUsers();

            String userName, userEmail, userPassword;
            System.out.println("===Registration Page===\n");
            while (true) {
                System.out.println("Enter your name: ");
                userName = scanner.nextLine();
                if (userName == "") {
                    System.out.println("Name cannot be null. Please try again.");
                    continue;
                }
                break;
            }
            while (true) {
                System.out.println("Enter your email: ");
                userEmail = scanner.nextLine();
                if(userEmail == ""){
                    System.out.println("Email cannot be null. Please try again.");
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
                if(userPassword == ""){
                    System.out.println("Password cannot be null. Please try again.");
                    continue;
                }
                break;
            }
            User newUser = new User(nextId++, userName, userEmail, userPassword);
            usersToSave.add(newUser);
            saveUsers();
            System.out.println("Registration successful!");
        }

        /**
         * Checks if a user with the given email exists in the system.
         *
         * @param email the email to check
         * @return true if a user with the email exists, false otherwise
         */
        public static boolean authorize(String email) {
            return authorize(email, null);
        }

        /**
         * Authenticates a user by checking if the email and password match a stored user.
         *
         * @param email the user's email
         * @param password the user's password (can be null for email-only check)
         * @return true if authentication succeeds, false otherwise
         */
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

        /**
         * Handles the user login process by collecting credentials and verifying them.
         */
        public static void login() {
            loadUsers();
            System.out.println("===Login Page===\n");
            System.out.println("Enter email: ");
            String email = scanner.nextLine();
            System.out.println("Enter password: ");
            String password = scanner.nextLine();
            
            if (authorize(email, password)) {
                System.out.println("Login successful!");
            } else {
                System.out.println("Invalid email or password\n");
            }
        }

        /**
         * Saves the current list of users to a serialized file.
         */
        public static void saveUsers() {
            try (FileOutputStream fos = new FileOutputStream(FILENAME);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(usersToSave);
            } catch (IOException e) {
                System.err.println("Error saving users: " + e.getMessage());
            }
        }

        /**
         * Loads the list of users from a serialized file.
         * Initializes the nextId counter based on the highest existing user ID.
         */
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
                // Silently initialize empty user list instead of showing error
                usersToSave = new ArrayList<>();
                nextId = 1;
            }
        }

        /**
         * Returns a string representation of the user.
         *
         * @return a string containing the user's id, name, and email
         */
        @Override
        public String toString() {
            return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
        }
    }

    /**
     * The main entry point for the application.
     * Presents a menu for user registration, login, or exit.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("***Welcome to Self Budgeting app***");
        while (true) { 
            System.out.println("What would you like to do?");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    System.out.println();
                    User.register();
                    break;
                case 2:
                    System.out.println();
                    User.login();
                    break;
                case 3:
                    System.out.println();
                    System.out.println("Thank you for using the Self Budgeting app!");
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.\n");
            }
        }
    }
}