import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UltraSimpleUserStorage {

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

        public User(int id, String name, String email, String password) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.password = password;
        }

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
                System.out.println("Invalid email or password");
            }
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
            if (!file.exists()) return;

            try (FileInputStream fis = new FileInputStream(FILENAME);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                usersToSave = (List<User>) ois.readObject();
                nextId = usersToSave.stream()
                                 .mapToInt(u -> u.id)
                                 .max()
                                 .orElse(0) + 1;
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading users: " + e.getMessage());
            }
        }

        @Override
        public String toString() {
            return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
        }
    }

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
                    System.out.println("\n");
                    User.register();
                case 2:
                    System.out.println("\n");
                    User.login();
                case 3:
                    System.out.println("\n");
                    System.out.println("Thank you for using the Self Budgeting app!");
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.\n");
            }
        }
    }
}