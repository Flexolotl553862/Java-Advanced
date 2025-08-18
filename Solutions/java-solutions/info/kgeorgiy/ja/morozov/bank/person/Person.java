package info.kgeorgiy.ja.morozov.bank.person;

import info.kgeorgiy.ja.morozov.bank.account.Account;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote interface of person for bank app.
 */
public interface Person extends Remote {

    /**
     * Returns name of person.
     *
     * @return string with the name of this person.
     */
    String getName() throws RemoteException;

    /**
     * Returns surname of person.
     *
     * @return string with the surname of this person.
     */
    String getSurname() throws RemoteException;

    /**
     * Returns passport data. It can be a number or something else.
     *
     * @return string with passport data.
     */
    String getPassportData() throws RemoteException;

    /**
     * Returns all account ids that belong to this person.
     *
     * @return {@link List} that contains ids of accounts.
     */
    List<Account> getAccounts() throws RemoteException;

    /**
     * Add a new account to person.
     */
    void addAccount(Account account) throws RemoteException;
}
