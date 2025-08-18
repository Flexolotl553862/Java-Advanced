package info.kgeorgiy.ja.morozov.bank.bank;

import info.kgeorgiy.ja.morozov.bank.account.Account;
import info.kgeorgiy.ja.morozov.bank.person.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for bank app.
 */
public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it does not already exist.
     *
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     *
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exist.
     */
    Account getAccount(String id) throws RemoteException;

    /**
     * Returns remote person if it exists or {@code null}.
     *
     * @param passportData your passport number.
     * @return person with given passport number or {@code null} if person doesn't exist.
     * @throws RemoteException if a bank doesn't exist, or if you don't have access to it.
     */
    Person getRemotePerson(String passportData) throws RemoteException;

    /**
     * Returns local person if it exists or {@code null}.
     * Local person created by remote person.
     *
     * @param passportData your passport number.
     * @return person with given passport number or {@code null} if person doesn't exist.
     * @throws RemoteException if a bank doesn't exist, or if you don't have access to it.
     */
    LocalPerson getLocalPerson(String passportData) throws RemoteException;

    /**
     * Creates new remote person.
     *
     * @param name         given name.
     * @param surname      given surname.
     * @param passportData given passport data.
     * @param accountId    id of a bank account.
     * @throws RemoteException if there are some problems with remote bank.
     */
    Person createRemotePerson(
            String name,
            String surname,
            String passportData,
            String accountId
    ) throws RemoteException;
}
