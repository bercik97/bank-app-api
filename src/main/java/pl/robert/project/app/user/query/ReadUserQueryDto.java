package pl.robert.project.app.user.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.robert.project.app.user_address.UserAddress;
import pl.robert.project.app.user_bank_account.UserBankAccount;
import pl.robert.project.app.user_contact.UserContact;

@Getter
@AllArgsConstructor
public class ReadUserQueryDto {

    private String pesel;
    private String firstName;
    private String lastName;
    private String password;
    private String decodedBCryptPassword;
    private UserContact contact;
    private UserAddress address;
    private UserBankAccount bankAccount;
}
