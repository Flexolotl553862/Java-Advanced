package info.kgeorgiy.ja.morozov.bank;

import info.kgeorgiy.ja.morozov.bank.account.Account;
import info.kgeorgiy.ja.morozov.bank.bank.Bank;
import info.kgeorgiy.ja.morozov.bank.person.Person;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public final class Client {
    /**
     * Utility class.
     */
    private Client() {
    }

    /**
     * Demonstration of bank`s work.
     *
     * @param args expected next args: <name> <surname> <passportData> <accountId> <difference>
     * @throws RemoteException if a bank doesn't exist, or if you don't have access to it.
     */
    public static void main(final String... args) throws RemoteException {
        int diff;
        try {
            if (args.length != 5) {
                System.out.println(
                        "Usage: java Client <name> <surname> <passportData> <accountId> <difference>"
                );
                return;
            }
            diff = Integer.parseInt(args[4]);
        } catch (NumberFormatException ignored) {
            System.out.println("<difference> must be an integer");
            return;
        }

        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        } catch (final RemoteException e) {
            System.out.println("Bank not found");
            return;
        } catch (SecurityException e) {
            System.out.println("Security exception" + e.getMessage());
            return;
        }

        String name = args[0], surname = args[1], passportData = args[2], accountId = args[3];
        Person person = bank.getRemotePerson(passportData);
        Account account = null;
        if (person == null) {
            System.out.println("Creating person " + passportData);
            person = bank.createRemotePerson(name, surname, passportData, accountId);
        }
        boolean ok = false;
        for (Account acc : person.getAccounts()) {
            if (acc.getId().equals(passportData + ":" + accountId)) {
                account = acc;
                ok = true;
                break;
            }
        }
        if (!ok) {
            System.out.println("Creating account " + passportData + ":" + accountId);
            account = bank.createAccount(passportData + ":" + accountId);
            person.addAccount(account);
        }
        if (name.equals(person.getName()) && surname.equals(person.getSurname()) && account != null) {
            System.out.println("Person already exists");
        } else {
            System.out.println("Wrong data for this person");
            return;
        }
        System.out.println("Account id: " + account.getId());
        System.out.println("Money: " + account.getAmount());
        System.out.println("Try to add " + diff);
        account.addAmount(BigDecimal.valueOf(diff));
        System.out.println("Current amount of money: " + account.getAmount());
    }
}
