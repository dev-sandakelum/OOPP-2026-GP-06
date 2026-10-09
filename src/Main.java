import UI.Pages.AdminPages;
import database.Database;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args){

        try {
            Database.getConnection();
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
        }
        AdminPages Apg = new AdminPages();
    }
}