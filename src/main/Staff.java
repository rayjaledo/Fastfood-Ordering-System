package Main;

import config.dbConnect;
import java.util.*;

public class Staff {
    private dbConnect con;
    private Map<String, Object> user;
    private Scanner sc = new Scanner(System.in);

    public Staff(dbConnect con, Map<String, Object> user) {
        this.con = con;
        this.user = user;
    }

    public void staffDashboard() {
        while (true) {
            System.out.println("\n---- STAFF DASHBOARD ----");
            System.out.println("1. View Orders");
            System.out.println("2. Update Order Status");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            int stOpt = Integer.parseInt(sc.nextLine());

            switch (stOpt) {
                case 1:
                    Main.viewOrders();
                    break;
                case 2:
                    Main.viewOrders();
                    System.out.print("Enter Order ID to Update: ");
                    int oid = Integer.parseInt(sc.nextLine());

                    String checkOrder = "SELECT * FROM tbl_orders WHERE o_id = ?";
                    List<Map<String, Object>> orderExist = con.fetchRecords(checkOrder, oid);
                    if (orderExist.isEmpty()) {
                        System.out.println("Order ID not found!");
                        break;
                    }

                    System.out.println("Select new status:");
                    System.out.println("1. Preparing");
                    System.out.println("2. Served");
                    System.out.println("3. Completed");
                    int st = Integer.parseInt(sc.nextLine());
                    String nstat = (st == 1) ? "Preparing" : (st == 2) ? "Served" : "Completed";

                    String up = "UPDATE tbl_orders SET status = ? WHERE o_id = ?";
                    con.updateRecord(up, nstat, oid);
                    System.out.println("Order updated!");
                    Main.viewOrders();
                    break;
                case 3:
                    System.out.println("Exiting Staff Dashboard...");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}
