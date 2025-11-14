package Main;

import config.dbConnect;
import java.util.*;

public class Main {

    // ========================= VIEW USERS =========================
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

    // ========================= VIEW MENU =========================
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

    // ========================= VIEW ALL ORDERS =========================
    public static void viewAllOrders(dbConnect con) {
        String Query = "SELECT o.o_id, u.u_name as customer_name, o.total, o.status, o.order_date, o.payment_method FROM tbl_orders o JOIN tbl_user u ON o.cust_id = u.u_id";
        List<Map<String, Object>> data = con.fetchRecords(Query);
        
        if (data.isEmpty()) {
            System.out.println("No orders found.");
        } else {
            String[] headers = {"Order ID", "Customer", "Total", "Status", "Date", "Payment"};
            String[] columns = {"o_id", "customer_name", "total", "status", "order_date", "payment_method"};
            con.viewRecords(Query, headers, columns);
        }
    }

    // ========================= PLACE ORDER =========================
    public static void placeOrder(dbConnect con, Map<String, Object> user, Scanner sc) {
        viewMenu();

        List<Map<String, Object>> itemsToOrder = new ArrayList<>();
        double grandTotal = 0;

        while (true) {
            System.out.print("Enter Food ID to order (or 'done' to finish): ");
            String foodId = sc.nextLine();
            if (foodId.equalsIgnoreCase("done")) break;

            String checkFood = "SELECT * FROM tbl_food WHERE f_id = ?";
            List<Map<String, Object>> food = con.fetchRecords(checkFood, foodId);
            if (food.isEmpty()) {
                System.out.println("Invalid Food ID! Please try again.");
                continue;
            }

            Map<String, Object> selectedFood = food.get(0);
            double price = Double.parseDouble(selectedFood.get("f_price").toString());

            System.out.print("Enter Quantity: ");
            
            try {
                int qty = Integer.parseInt(sc.nextLine()); 
                if (qty <= 0) {
                    System.out.println("Quantity must be greater than zero.");
                    continue;
                }
                
                double subtotal = price * qty;
                grandTotal += subtotal;

                Map<String, Object> orderLine = new HashMap<>();
                orderLine.put("food_id", foodId);
                orderLine.put("qty", qty);
                orderLine.put("price", price);
                orderLine.put("total", subtotal);
                orderLine.put("name", selectedFood.get("f_name"));
                itemsToOrder.add(orderLine);

            } catch (NumberFormatException e) {
                System.out.println("Invalid quantity input. Please enter a number.");
                continue;
            }
        }

        if (itemsToOrder.isEmpty()) {
            System.out.println("No items selected. Order cancelled.");
            return;
        }

        System.out.println("\n--- Order Summary ---");
        System.out.printf("Total amount to pay: ₱%.2f\n", grandTotal);
        System.out.println("---------------------");
        
        System.out.println("Choose payment method:");
        System.out.println("1. Cash");
        System.out.println("2. Card");
        
        int payChoice;
        try {
            System.out.print("Enter choice: ");
            payChoice = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid payment choice. Order cancelled.");
            return;
        }

        String paymentMethod;
        if (payChoice == 1) {
            paymentMethod = "Cash";
        } else if (payChoice == 2) {
            paymentMethod = "Card";
        } else {
            System.out.println("Invalid payment choice. Order cancelled.");
            return;
        }

        String sqlInsertOrder = "INSERT INTO tbl_orders (cust_id, total, status, order_date, payment_method) VALUES (?, ?, ?, datetime('now'), ?)";
        
        int orderId = con.addRecordAndGetId(sqlInsertOrder, user.get("u_id"), grandTotal, "Pending", paymentMethod); 

        if (orderId == 0) {
             System.out.println("❌ ERROR: Could not retrieve Order ID. Order not fully logged.");
             return;
        }

        for (Map<String, Object> item : itemsToOrder) {
            String sqlInsertItem = "INSERT INTO order_items (order_id, food_id, qty, price, total) VALUES (?, ?, ?, ?, ?)";
            con.addRecord(sqlInsertItem, orderId, item.get("food_id"), item.get("qty"), item.get("price"), item.get("total"));
        }

        System.out.println("\n✅ Order placed successfully! Your Order ID is: " + orderId);
        System.out.printf("Grand Total: ₱%.2f\n", grandTotal);
    }

    // ========================= VIEW MY ORDERS =========================
    public static void viewMyOrders(dbConnect con, Map<String, Object> user) {
        String Query = "SELECT * FROM tbl_orders WHERE cust_id = ?";
        List<Map<String, Object>> orders = con.fetchRecords(Query, user.get("u_id"));

        if (orders.isEmpty()) {
            System.out.println("You have no orders yet.");
            return;
        }

        System.out.println("\n===== YOUR ORDERS =====");
        for (Map<String, Object> order : orders) {
            System.out.println("\nOrder ID: " + order.get("o_id") +
                                 " | Total: ₱" + order.get("total") +
                                 " | Status: " + order.get("status") +
                                 " | Date: " + order.get("order_date") +
                                 " | Payment: " + order.get("payment_method"));

            String itemQuery = "SELECT i.item_id, f.f_name, i.qty, i.price, i.total " +
                               "FROM order_items i JOIN tbl_food f ON i.food_id = f.f_id " +
                               "WHERE i.order_id = ?";
            List<Map<String, Object>> items = con.fetchRecords(itemQuery, order.get("o_id"));

            if (items.isEmpty()) {
                System.out.println("  No items found for this order.");
            } else {
                System.out.println("  Items:");
                for (Map<String, Object> item : items) {

                    String name = item.get("f_name").toString();
                    Object qtyObj = item.get("qty");
                    double price = Double.parseDouble(item.get("price").toString());
                    double total = Double.parseDouble(item.get("total").toString());

                    System.out.printf("    - %s x %s @ ₱%.2f = ₱%.2f\n", 
                        qtyObj, 
                        name, 
                        price, 
                        total 
                    );
                }
            }
        }
    }

    // ========================= CUSTOMER DASHBOARD =========================
    public static void customerDashboard(dbConnect con, Map<String, Object> user, Scanner sc) {
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
                    placeOrder(con, user, sc); 
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

    // ========================= STAFF DASHBOARD (CAN UPDATE STATUS) =========================
    public static void staffDashboard(dbConnect con, Scanner sc) {
        int choice;
        do {
            System.out.println("\n===== STAFF DASHBOARD =====");
            System.out.println("1. View All Orders");
            System.out.println("2. Update Order Status");
            System.out.println("3. View Menu");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");

            String line = sc.nextLine();
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                choice = 0;
            }

            switch (choice) {
                case 1:
                    viewAllOrders(con);
                    break;

                case 2:
                    updateOrderStatusStaff(con, sc);
                    break;

                case 3:
                    viewMenu();
                    break;

                case 4:
                    System.out.println("Logging out Staff...");
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        } while (true);
    }

    // ================= STAFF: UPDATE ORDER STATUS =================
    public static void updateOrderStatusStaff(dbConnect con, Scanner sc) {

        viewAllOrders(con);

        System.out.print("Enter Order ID to update: ");
        String oid = sc.nextLine();

        String query = "SELECT * FROM tbl_orders WHERE o_id = ?";
        List<Map<String, Object>> orderData = con.fetchRecords(query, oid);

        if (orderData.isEmpty()) {
            System.out.println("Order ID not found!");
            return;
        }

        Map<String, Object> order = orderData.get(0);
        String currentStatus = order.get("status").toString();

        if (currentStatus.equalsIgnoreCase("Served") ||
            currentStatus.equalsIgnoreCase("Completed")) {
            System.out.println("This order is already SERVED or COMPLETED. You cannot update it.");
            return;
        }

        System.out.println("\nCurrent Status: " + currentStatus);
        System.out.println("Select new status:");
        System.out.println("1. Preparing");
        System.out.println("2. Served");
        System.out.println("3. Completed");
        System.out.print("Choose: ");

        int opt;
        try {
            opt = Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid input.");
            return;
        }

        String newStatus;
        if (opt == 1) newStatus = "Preparing";
        else if (opt == 2) newStatus = "Served";
        else if (opt == 3) newStatus = "Completed";
        else {
            System.out.println("Invalid choice.");
            return;
        }

        String update = "UPDATE tbl_orders SET status = ? WHERE o_id = ?";
        con.updateRecord(update, newStatus, oid);

        System.out.println("✅ Order updated successfully!");

        viewAllOrders(con);
    }

    // ========================= ADMIN DASHBOARD (NO UPDATE OPTION) =========================
    public static void adminDashboard(dbConnect con, Scanner sc) {
        int choice;
        do {
            System.out.println("\n===== ADMIN DASHBOARD =====");
            System.out.println("1. View All Orders");
            System.out.println("2. Add Food Item");
            System.out.println("3. View Menu");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");

            String line = sc.nextLine();
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                choice = 0;
            }

            switch (choice) {
                case 1:
                    viewAllOrders(con);
                    break;

                case 2:
                    System.out.print("Enter Food Name: ");
                    String fname = sc.nextLine();

                    double fprice;
                    while (true) {
                        try {
                            System.out.print("Enter Food Price: ");
                            fprice = Double.parseDouble(sc.nextLine());
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid price.");
                        }
                    }

                    System.out.print("Enter Food Category: ");
                    String category = sc.nextLine();

                    String addFood = "INSERT INTO tbl_food(f_name, f_price, f_category) VALUES (?, ?, ?)";
                    con.addRecord(addFood, fname, fprice, category);
                    System.out.println("Food Added Successfully!");
                    viewMenu();
                    break;

                case 3:
                    viewMenu();
                    break;

                case 4:
                    System.out.println("Logging out Admin...");
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        } while (true);
    }

    // ========================= SUPER ADMIN DASHBOARD =========================
    public static void superAdminDashboard(dbConnect con, Scanner sc) {
        int choice;
        do {
            System.out.println("\n===== SUPER ADMIN DASHBOARD =====");
            System.out.println("1. View All Users");
            System.out.println("2. Update User Status");
            System.out.println("3. View All Orders"); 
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");

            String line = sc.nextLine();
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                choice = 0;
            }

            switch (choice) {
                case 1:
                    viewUsers();
                    break;

                case 2:
                    viewUsers();
                    System.out.print("Enter User ID to update: ");
                    String uid = sc.nextLine();

                    String checkUser = "SELECT * FROM tbl_user WHERE u_id = ?";
                    List<Map<String, Object>> userCheck = con.fetchRecords(checkUser, uid);
                    if (userCheck.isEmpty()) {
                        System.out.println("User not found!");
                        break;
                    }

                    Map<String, Object> selectedUser = userCheck.get(0);

                    if (selectedUser.get("u_type").toString().equalsIgnoreCase("SuperAdmin")) {
                        System.out.println("⚠️ You cannot update the Super Admin account!");
                        break;
                    }

                    System.out.println("\nSelected User: " + selectedUser.get("u_name"));
                    System.out.println("Current Status: " + selectedUser.get("u_status"));

                    System.out.println("\nChoose new status:");
                    System.out.println("1. Approved");
                    System.out.println("2. Pending");
                    System.out.println("3. Deactivated");
                    System.out.print("Enter number: ");

                    int statusChoice;
                    try {
                        statusChoice = Integer.parseInt(sc.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input.");
                        break;
                    }

                    String newStatus;
                    switch (statusChoice) {
                        case 1:
                            newStatus = "Approved";
                            break;
                        case 2:
                            newStatus = "Pending";
                            break;
                        case 3:
                            newStatus = "Deactivated";
                            break;
                        default:
                            System.out.println("Invalid option!");
                            continue;
                    }

                    String updateStatus = "UPDATE tbl_user SET u_status = ? WHERE u_id = ?";
                    con.updateRecord(updateStatus, newStatus, uid);

                    System.out.println("✅ User status updated!");
                    viewUsers();
                    break;

                case 3:
                    viewAllOrders(con);
                    break;

                case 4:
                    System.out.println("Exiting Super Admin Dashboard...");
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        } while (true);
    }

    // ========================= MAIN METHOD =========================
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
                System.out.println("Invalid input.");
                choice = 0;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter email: ");
                    String em = sc.nextLine();
                    System.out.print("Enter Password: ");
                    String pas = sc.nextLine();
                    String hashpass = con.hashPassword(pas);

                    String qry = "SELECT * FROM tbl_user WHERE u_email = ? AND u_pass = ?";
                    List<Map<String, Object>> result = con.fetchRecords(qry, em, hashpass);

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

                    if (type.equals("SuperAdmin")) {
                        superAdminDashboard(con, sc); 
                    } else if (type.equals("Admin")) {
                        adminDashboard(con, sc);
                    } else if (type.equals("Staff")) {
                        staffDashboard(con, sc);
                    } else if (type.equals("Customer")) {
                        customerDashboard(con, user, sc); 
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
                    
                    int roleChoice;
                    try {
                        roleChoice = Integer.parseInt(sc.nextLine());
                        while (roleChoice < 1 || roleChoice > 3) {
                            System.out.print("Invalid, choose between 1 - 3 only: ");
                            roleChoice = Integer.parseInt(sc.nextLine());
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input for role.");
                        break;
                    }
                    
                    String tp = (roleChoice == 1) ? "Admin" : (roleChoice == 2) ? "Staff" : "Customer";

                    System.out.print("Enter Password: ");
                    String pass = sc.nextLine();
                    String hashedPassword = con.hashPassword(pass);

                    String sqlInsert = "INSERT INTO tbl_user(u_name, u_email, u_type, u_status, u_pass) VALUES (?, ?, ?, ?, ?)";
                    con.addRecord(sqlInsert, name, email, tp, "Pending", hashedPassword);
                    System.out.println("Registration successful!");
                    break;

                case 3:
                    System.out.println("Exiting application.");
                    sc.close(); 
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice.");
            }

            System.out.print("Do you want to continue? (Y/N): ");
            String continueLine = sc.nextLine();
            cont = continueLine.isEmpty() ? 'N' : continueLine.toUpperCase().charAt(0);

        } while (cont == 'Y');

        sc.close(); 
        System.out.println("Thank you! Program ended.");
    }

    static void viewOrders() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
