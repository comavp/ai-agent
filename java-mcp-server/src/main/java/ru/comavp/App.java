package ru.comavp;

public class App {

    public static void main(String[] args) {
        System.out.println("MCP server started ...");
        EmailClient emailClient = new EmailClient();
        emailClient.sendEmail("", "Hello");
        System.out.println("MCP server finished ...");
    }
}
