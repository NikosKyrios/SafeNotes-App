package com.safeNotes.app;
import java.util.Scanner;

import com.safeNotes.controllers.auth.RegisterController;

public class Main {
    public static void main(String[] args) {
        SafeNotesApp.main(args);

        /*Scanner scanner = new Scanner(System.in);
        System.out.println("1. Register\n2. Login\n3. Exit\nWrite the number: ");
        int choice = scanner.nextInt();
        
        if (choice == 1) {
            System.out.print("Username: ");
            String username = scanner.nextLine();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            RegisterController.register(username, password);
        }
        else if (choice == 2) {
            login();
        }
        else if (choice == 3) {
            exit();
        }
        else {
            System.out.println("Choose a valid number.");
        }
        scanner.close();*/
    }
}
