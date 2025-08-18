package info.kgeorgiy.ja.morozov.bank.person;

import info.kgeorgiy.ja.morozov.bank.account.Account;
import info.kgeorgiy.ja.morozov.bank.account.AccountImpl;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * This class implements {@link Person}. It's not a remote object.
 */
public class LocalPerson extends AbstractPerson {

    /**
     * This constructor creates local person using remote person.
     */
    public LocalPerson(Person remotePerson) throws RemoteException {
        super(remotePerson.getName(), remotePerson.getSurname(), remotePerson.getPassportData(), new ArrayList<>());
        for (Account account : remotePerson.getAccounts()) {
            this.accounts.add(new AccountImpl(account));
        }
    }
}
