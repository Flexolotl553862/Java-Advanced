package info.kgeorgiy.ja.morozov.bank;

import info.kgeorgiy.ja.morozov.bank.account.Account;
import info.kgeorgiy.ja.morozov.bank.account.AccountImpl;
import info.kgeorgiy.ja.morozov.bank.bank.Bank;
import info.kgeorgiy.ja.morozov.bank.bank.RemoteBank;
import info.kgeorgiy.ja.morozov.bank.person.Person;
import info.kgeorgiy.ja.morozov.bank.person.RemotePerson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Tests for {@link RemoteBank}.
 */
public class BankJUnitTests {

    private static Bank bank;

    private static final int DEFAULT_PORT = 8888;
    private static final int RMI_PORT = 1099;

    private final List<Person> TEST_PERSONS = List.of(
            new RemotePerson("Bugs", "Bunny", "1111_123456", Collections.emptyList()),
            new RemotePerson("Daffy", "Duck", "1111_123457", Collections.emptyList()),
            new RemotePerson("Porky", "Pig", "1111_123458", Collections.emptyList()),
            new RemotePerson("Roger", "Rabbit", "1111_123459", Collections.emptyList())
    );

    @BeforeAll
    public static void startRegistry() {
        try {
            LocateRegistry.createRegistry(RMI_PORT);
        } catch (RemoteException e) {
            System.err.println("Failed to create RMI registry on port " + RMI_PORT + ": " + e.getMessage());
            throw new RuntimeException("RMI Registry setup failed", e);
        }
    }

    @BeforeEach
    public void setUpBank() {
        try {
            bank = new RemoteBank(RMI_PORT);
            UnicastRemoteObject.exportObject(bank, DEFAULT_PORT);
            Naming.rebind("//localhost/bank", bank);
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (RemoteException | MalformedURLException e) {
            System.err.println("Can't create new bank: " + e.getMessage());
            e.printStackTrace();
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void setAmountTest() throws RemoteException {
        bank.createAccount("123");
        Account account = bank.getAccount("123");
        Assertions.assertNotNull(account);
        Assertions.assertEquals(account.getId(), "123");
        Assertions.assertEquals(account.getAmount(), BigDecimal.ZERO);
        account.setAmount(BigDecimal.TEN);
        Assertions.assertEquals(bank.getAccount("123").getAmount(), BigDecimal.TEN);
    }

    @Test
    public void createRemoteAccountTest() throws RemoteException {
        bank.createAccount("123");
        Account account = bank.getAccount("123");
        Assertions.assertNotNull(account);
        Assertions.assertEquals(account.getId(), "123");
        Assertions.assertEquals(account.getAmount(), BigDecimal.ZERO);
    }

    @Test
    public void accountMethodsTest() throws RemoteException {
        bank.createAccount("123");
        Account account = bank.getAccount("123");
        Assertions.assertNotNull(account);
        Assertions.assertEquals(account.getId(), "123");
        Assertions.assertEquals(account.getAmount(), BigDecimal.ZERO);
    }

    @Test
    public void createLocalAccountTest() throws RemoteException {
        bank.createAccount("123");
        Account account = new AccountImpl(bank.getAccount("123"));
        account.addAmount(BigDecimal.valueOf(100));
        Assertions.assertNotNull(account);
        Assertions.assertEquals(account.getAmount(), BigDecimal.valueOf(100));
        Assertions.assertEquals(bank.getAccount("123").getAmount(), BigDecimal.ZERO);
    }

    @Test
    public void localPersonTest() throws RemoteException {
        String passportData = "1111_123456";
        String accountId = "1";
        bank.createRemotePerson("Bugs", "BunnyJR", passportData, "1");
        for (int i = 0; i < 10; i++) {
            try {
                Account acc = bank.getLocalPerson(passportData).getAccounts().getFirst();
                acc.addAmount(BigDecimal.valueOf(100));
                Assertions.assertEquals(acc.getAmount(), BigDecimal.valueOf(100));
            } catch (NullPointerException e) {
                Assertions.fail("Expected more than 0 accounts for each person");
            }
        }
        Assertions.assertEquals(bank.getAccount(passportData + ":" + accountId).getAmount(), BigDecimal.ZERO);
    }

    @Test
    public void negativeAmountOfMoneyTest() throws RemoteException {
        bank.createAccount("123");
        final Account account = bank.getAccount("123");
        Assertions.assertNotNull(account);
        Assertions.assertEquals(account.getAmount(), BigDecimal.ZERO);
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < 10; i++) {
                int finalI = i;
                executor.execute(() -> {
                    try {
                        Account copyAcc = bank.getAccount("123");
                        copyAcc.addAmount(BigDecimal.valueOf(finalI * (1 + (-1) * (finalI % 2))));
                    } catch (RemoteException ignored) {
                    }
                });
            }
            executor.awaitTermination(100, TimeUnit.MILLISECONDS);
            executor.shutdownNow();
            Account finalAccount = bank.getAccount("123");
            Assertions.assertTrue(finalAccount.getAmount().compareTo(BigDecimal.ZERO) >= 0);
        } catch (InterruptedException e) {
            System.err.println("Can't await an executor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void invalidPersonTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                bank.createRemotePerson(null, "smt", "1234_567890", "1"));
    }

    @Test
    public void parallelUpdatingTest() throws RemoteException {
        Person person = TEST_PERSONS.getFirst();
        bank.createRemotePerson(person.getName(), person.getSurname(), person.getPassportData(), "1");
        Person remote = bank.getRemotePerson(person.getPassportData());
        Assertions.assertNotNull(remote);
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < 10; i++) {
                int finalI = i;
                executor.execute(() -> {
                    try {
                        remote.getAccounts().getFirst().addAmount(BigDecimal.valueOf(finalI + 1));
                        bank.getLocalPerson(remote.getPassportData()).getAccounts().getFirst()
                                .addAmount(BigDecimal.valueOf(100 + finalI));
                    } catch (RemoteException ignored) {
                    }
                });
            }
            executor.shutdown();
            Assertions.assertTrue(executor.awaitTermination(1000, TimeUnit.MILLISECONDS));
            Assertions.assertEquals(BigDecimal.valueOf(55), remote.getAccounts().getFirst().getAmount());
        } catch (InterruptedException e) {
            System.err.println("Can't await an executor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void manyUsersTest() throws RemoteException {
        for (Person person : TEST_PERSONS) {
            bank.createRemotePerson(person.getName(), person.getSurname(), person.getPassportData(), "1");
            Assertions.assertNotNull(bank.getRemotePerson(person.getPassportData()));
        }
    }
}
