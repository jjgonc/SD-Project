import Client.Client;
import Model.Account;
import Model.Info;

import java.io.IOException;
import java.util.Scanner;

public class Tester {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Info i = new Info(null);
        if (i.createAccount("admin", "admin", true)) {          //para criar um user sem beneficios de admin, basta por isto a false!!
            System.out.println("Admin criado com sucesso!");
        }
        else {
            System.out.println("Ocorreu um erro ao criar um admin :(");
        }
        i.saveData("saves");
    }
}
