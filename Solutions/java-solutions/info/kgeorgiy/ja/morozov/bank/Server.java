package info.kgeorgiy.ja.morozov.bank;

import info.kgeorgiy.ja.morozov.bank.bank.Bank;
import info.kgeorgiy.ja.morozov.bank.bank.RemoteBank;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.net.*;

/**
 * This class creates a remote bank launches on given port and registers it.
 */
public final class Server {
    private final static int DEFAULT_PORT = 8888;
    private final static int RMI_PORT = 1099;

    /**
     * This method starts server.
     *
     * @param args you can specify port there.
     */
    public static void main(final String... args) {
        final int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        try {
            LocateRegistry.createRegistry(RMI_PORT);
        } catch (RemoteException e) {
            System.err.println("Failed to create RMI registry on port " + RMI_PORT + ": " + e.getMessage());
            throw new RuntimeException("RMI Registry setup failed", e);
        }

        final Bank bank = new RemoteBank(port);
        try {
            UnicastRemoteObject.exportObject(bank, port);
            Naming.rebind("//localhost/bank", bank);
            System.out.println("Server started");
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (final MalformedURLException e) {
            System.out.println("Malformed URL");
        }
    }
}
