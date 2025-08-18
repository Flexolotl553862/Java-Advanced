package info.kgeorgiy.ja.morozov.bank.bank;

import info.kgeorgiy.ja.morozov.bank.account.Account;
import info.kgeorgiy.ja.morozov.bank.account.AccountImpl;
import info.kgeorgiy.ja.morozov.bank.person.*;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class implements interface {@link Bank}.
 * You can use this from different threads.
 */
public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accountByID = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Person> personByPassportData = new ConcurrentHashMap<>();

    /**
     * A constructor with given port.
     *
     * @param port bank port.
     */
    public RemoteBank(final int port) {
        this.port = port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account createAccount(final String id) throws RemoteException {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException();
        }
        System.out.println("Creating account " + id);
        final Account account = new AccountImpl(id, BigDecimal.ZERO);
        if (accountByID.putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account getAccount(final String id) {
        System.out.println("Retrieving account " + id);
        return accountByID.get(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Person getRemotePerson(String PassportData) {
        System.out.println("Retrieving person " + PassportData);
        return personByPassportData.get(PassportData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Person createRemotePerson(
            final String name,
            final String surname,
            final String passportData,
            final String accountId
    ) throws RemoteException {
        if (name == null || surname == null || passportData == null || accountId == null) {
            throw new IllegalArgumentException();
        }
        final Account account = createAccount(passportData + ":" + accountId);
        System.out.println("Creating person " + passportData);
        final Person person = new RemotePerson(name, surname, passportData, List.of(account));
        if (personByPassportData.putIfAbsent(passportData, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } else {
            return getRemotePerson(passportData);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalPerson getLocalPerson(final String passportData) throws RemoteException {
        Person remotePerson = getRemotePerson(passportData);
        if (remotePerson == null) {
            return null;
        }
        return new LocalPerson(remotePerson);
    }
}
