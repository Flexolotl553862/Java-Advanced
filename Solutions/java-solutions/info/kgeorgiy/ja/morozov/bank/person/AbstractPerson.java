package info.kgeorgiy.ja.morozov.bank.person;

import info.kgeorgiy.ja.morozov.bank.account.Account;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Common class for {@link LocalPerson} and {@link RemotePerson}.
 */
public class AbstractPerson implements Person, Serializable {
    private final String name;
    private final String surname;
    private final String passportData;
    protected final List<Account> accounts;

    /**
     * This constructor creates a new person by given data. It also creates an account linked with this person.
     */
    public AbstractPerson(String name, String surname, String passportData, List<Account> accounts) {
        this.name = name;
        this.surname = surname;
        this.passportData = passportData;
        this.accounts = accounts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSurname() {
        return surname;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPassportData() {
        return passportData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Account> getAccounts() {
        return accounts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAccount(Account account) throws RemoteException {
        accounts.add(account);
    }
}
