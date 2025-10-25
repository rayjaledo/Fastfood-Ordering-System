package Main;

import config.dbConnect;
import java.util.*;

public class Admin {
    private dbConnect con;
    private Map<String, Object> user;
    private Scanner sc = new Scanner(System.in);

    public Admin(dbConnect con, Map<String, Object> user) {
        this.con = con;
        this.user = user;
    }

    public void adminDashboard() {
        while (true) {
            System.out.println("\n---- ADMIN DASHBOARD ----");
            System.out.println("1. View Menu");
            System.out.println("2. Add Food Item");
            System.out.println("3. View Orders");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");

            int opt = Integer.parseInt(sc.nextLine());
            switch (opt) {
                case 1:
                    Main.viewMenu();
                    break;

                case 2:
                    System.out.print("Enter Food Name: ");
                    String fname = sc.nextLine();
                    System.out.print("Enter Price: ");
                    double price = Double.parseDouble(sc.nextLine());
                    System.out.print("Enter Category: ");
                    String category = sc.nextLine();

                    String addFood = "INSERT INTO tbl_food(f_name, f_price, f_category) VALUES (?, ?, ?)";
                    con.addRecord(addFood, fname, price, category);
                    System.out.println("Food Added Successfully!");
                    Main.viewMenu();
                    break;

                case 3:
                    Main.viewOrders();
                    break;

                case 4:
                    System.out.println("Exiting Admin Dashboard...");
                    return;

                default:
                    System.out.println("Invalid option!");
            }
        }
    }
}