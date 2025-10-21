package Main;

import config.dbConnect;
import java.util.Scanner;
import java.util.List;
import java.util.Map;

public class Main {


public static void viewUsers() {
    dbConnect conf = new dbConnect();
    String Query = "SELECT u_id, u_name, u_email, u_type, u_status FROM tbl_user";
    List<Map<String, Object>> data = conf.fetchRecords(Query);
    if (data.isEmpty()) {
        System.out.println("No users found.");
    } else {
        String[] headers = {"ID", "Name", "Email", "Role", "Status"};
        String[] columns = {"u_id", "u_name", "u_email", "u_type", "u_status"};
        conf.viewRecords(Query, headers, columns);
    }
}

public static void viewMenu() {
    dbConnect conf = new dbConnect();
    String Query = "SELECT f_id, f_name, f_price, f_category FROM tbl_food";
    List<Map<String, Object>> data = conf.fetchRecords(Query);
    if (data.isEmpty()) {
        System.out.println("No menu items found.");
    } else {
        String[] headers = {"Food ID", "Name", "Price", "Category"};
        String[] columns = {"f_id", "f_name", "f_price", "f_category"};
        conf.viewRecords(Query, headers, columns);
    }
}

public static void viewOrders() {
    dbConnect conf = new dbConnect();
    String Query = "SELECT o_id, cust_id, food_id, qty, total, status, order_date FROM tbl_orders";
    List<Map<String, Object>> data = conf.fetchRecords(Query);
    if (data.isEmpty()) {
        System.out.println("No orders found.");
    } else {
        String[] headers = {"Order ID", "Customer ID", "Food ID", "Qty", "Total", "Status", "Date"};
        String[] columns = {"o_id", "cust_id", "food_id", "qty", "total", "status", "order_date"};
        conf.viewRecords(Query, headers, columns);
    }
}


public static void customerDashboard(dbConnect con, Map<String, Object> user) {
    Scanner sc = new Scanner(System.in);
    int choice;
    do {
        System.out.println("\n===== CUSTOMER DASHBOARD =====");
        System.out.println("Welcome, " + user.get("u_name") + "!");
        System.out.println("1. View Menu");
        System.out.println("2. Place Order");
        System.out.println("3. View My Orders");
        System.out.println("4. Logout");
        System.out.print("Enter choice: ");
        String line = sc.nextLine();

        try {
            choice = Integer.parseInt(line);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            choice = 0;
        }

        switch (choice) {
            case 1:
                viewMenu();
                break;

            case 2:
                placeOrder(con, user);
                break;

            case 3:
                viewMyOrders(con, user);
                break;

            case 4:
                System.out.println("Logging out...");
                return;

            default:
                System.out.println("Invalid choice. Try again.");
        }
    } while (true);
}


public static void placeOrder(dbConnect con, Map<String, Object> user) {
    Scanner sc = new Scanner(System.in);
    viewMenu();

    System.out.print("Enter Food ID to order: ");
    String foodId = sc.nextLine();

    String checkFood = "SELECT * FROM tbl_food WHERE f_id = ?";
    List<Map<String, Object>> food = con.fetchRecords(checkFood, foodId);
    if (food.isEmpty()) {
        System.out.println("Invalid Food ID!");
        return;
    }

    Map<String, Object> selectedFood = food.get(0);
    double price = Double.parseDouble(selectedFood.get("f_price").toString());
    System.out.print("Enter Quantity: ");
    int qty = Integer.parseInt(sc.nextLine());
    double total = price * qty;

    String sqlInsert = "INSERT INTO tbl_orders (cust_id, food_id, qty, total, status, order_date) VALUES (?, ?, ?, ?, ?, datetime('now'))";
    con.addRecord(sqlInsert, user.get("u_id"), foodId, qty, total, "Pending");

    System.out.println("Order placed successfully!");
    System.out.println("You ordered " + qty + " x " + selectedFood.get("f_name") + " for ₱" + total);
}

public static void viewMyOrders(dbConnect con, Map<String, Object> user) {
    String Query = "SELECT o_id, food_id, qty, total, status, order_date FROM tbl_orders WHERE cust_id = ?";
    List<Map<String, Object>> data = con.fetchRecords(Query, user.get("u_id"));

    if (data.isEmpty()) {
        System.out.println("You have no orders yet.");
    } else {
        String[] headers = {"Order ID", "Food ID", "Qty", "Total", "Status", "Date"};
        String[] columns = {"o_id", "food_id", "qty", "total", "status", "order_date"};
        con.viewRecords(Query, headers, columns, (int) user.get("u_id"));
    }
}


public static void main(String[] args) {
    dbConnect con = new dbConnect();
    con.connectDB();
    int choice;
    char cont;
    Scanner sc = new Scanner(System.in);

    do {
        System.out.println("\n===== MAIN MENU =====");
        System.out.println("1. Login");
        System.out.println("2. Register New User");
        System.out.println("3. Exit");
        System.out.print("Enter choice: ");

        String choiceLine = sc.nextLine();
        try {
            choice = Integer.parseInt(choiceLine);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            choice = 0;
        }

        switch (choice) {
           
            case 1:
                System.out.print("Enter email: ");
                String em = sc.nextLine();
                System.out.print("Enter Password: ");
                String pas = sc.nextLine();

                String qry = "SELECT * FROM tbl_user WHERE u_email = ? AND u_pass = ?";
                List<Map<String, Object>> result = con.fetchRecords(qry, em, pas);

                if (result.isEmpty()) {
                    System.out.println("INVALID CREDENTIALS");
                    break;
                }

                Map<String, Object> user = result.get(0);
                String stat = user.get("u_status").toString();
                String type = user.get("u_type").toString();

                if (stat.equalsIgnoreCase("Pending")) {
                    System.out.println("Account is Pending. Contact the Super Admin!");
                    break;
                }

                System.out.println("LOGIN SUCCESS!");

                if (type.equals("Admin")) {
                    Admin admin = new Admin(con, user);
                    admin.adminDashboard();
                } else if (type.equals("Staff")) {
                    Staff staff = new Staff(con, user);
                    staff.staffDashboard();
                } else if (type.equals("Customer")) {
                    customerDashboard(con, user);
                } else {
                    System.out.println("Unknown account type.");
                }
                break;

            
            case 2:
                System.out.print("Enter user name: ");
                String name = sc.nextLine();
                System.out.print("Enter user email: ");
                String email = sc.nextLine();

                while (true) {
                    String checkEmail = "SELECT * FROM tbl_user WHERE u_email = ?";
                    List<Map<String, Object>> resultEmail = con.fetchRecords(checkEmail, email);
                    if (resultEmail.isEmpty()) break;
                    else {
                        System.out.print("Email already exists, Enter another email: ");
                        email = sc.nextLine();
                    }
                }

                System.out.print("Enter user Role (1 - Admin / 2 - Staff / 3 - Customer): ");
                int roleChoice = Integer.parseInt(sc.nextLine());
                while (roleChoice < 1 || roleChoice > 3) {
                    System.out.print("Invalid, choose between 1 - 3 only: ");
                    roleChoice = Integer.parseInt(sc.nextLine());
                }

                String tp = (roleChoice == 1) ? "Admin" : (roleChoice == 2) ? "Staff" : "Customer";

                System.out.print("Enter Password: ");
                String pass = sc.nextLine();

                String sqlInsert = "INSERT INTO tbl_user(u_name, u_email, u_type, u_status, u_pass) VALUES (?, ?, ?, ?, ?)";
                con.addRecord(sqlInsert, name, email, tp, "Pending", pass);
                System.out.println("Registration successful! Account is pending Super Admin approval.");
                break;

             
            case 3:
                System.out.println("Exiting application.");
                System.exit(0);
                break;

            default:
                System.out.println("Invalid choice. Please try again.");
        }

        System.out.print("Do you want to continue? (Y/N): ");
        cont = sc.nextLine().toUpperCase().charAt(0);

    } while (cont == 'Y');

    System.out.println("Thank you! Program ended.");
} 

} 
