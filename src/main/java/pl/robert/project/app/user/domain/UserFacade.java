package pl.robert.project.app.user.domain;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import pl.robert.project.app.security.dto.AppUserDto;
import pl.robert.project.app.transaction.domain.TransactionFacade;
import pl.robert.project.app.transaction.domain.dto.SendTransactionDto;
import pl.robert.project.app.user.domain.dto.AboutMeUserDto;
import pl.robert.project.app.user.domain.dto.ChangeUserPasswordDto;
import pl.robert.project.app.user.domain.dto.CreateUserDto;
import pl.robert.project.app.user.domain.dto.ReadUserDto;
import pl.robert.project.app.user.query.AboutMeUserQueryDto;
import pl.robert.project.app.user.query.BaseUserQuery;
import pl.robert.project.app.user.query.CreateUserQueryDto;
import pl.robert.project.app.user.query.ReadUserQueryDto;
import pl.robert.project.app.user_address.UserAddress;
import pl.robert.project.app.user_address.UserAddressFacade;
import pl.robert.project.app.user_bank_account.UserBankAccount;
import pl.robert.project.app.user_bank_account.UserBankAccountFacade;
import pl.robert.project.app.user_contact.UserContact;
import pl.robert.project.app.user_contact.UserContactFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@AllArgsConstructor
public class UserFacade {

    private UserRepository repository;
    private UserFactory factory;
    private UserValidator validator;
    private BaseUserQuery baseQuery;
    private CreateUserDto createUserDto;
    private ReadUserDto readUserDto;
    private UserContactFacade userContactFacade;
    private UserAddressFacade userAddressFacade;
    private ChangeUserPasswordDto changePasswordDto;
    private UserBankAccountFacade userBankAccountFacade;
    private AboutMeUserDto aboutMeUserDto;
    private TransactionFacade transactionFacade;

    public CreateUserQueryDto add(CreateUserDto dto, BindingResult result) {
        if (validator.supports(dto.getClass())) {

            UserContact contact = new UserContact(dto);
            UserAddress address = new UserAddress(dto);

            userContactFacade.validate(contact, result);
            userAddressFacade.validate(address, result);
            validator.validate(dto, result);

            if (!result.hasErrors()) {

                UserBankAccount bankAccount = new UserBankAccount(dto.getPesel(), bankAccountNumberGenerator());
                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

                createUserDto.setPesel(dto.getPesel());
                createUserDto.setFirstName(dto.getFirstName());
                createUserDto.setLastName(dto.getLastName());
                createUserDto.setPassword(dto.getPassword());
                createUserDto.setRePassword(dto.getRePassword());
                dto.setPassword(passwordEncoder.encode(createUserDto.getPassword()));

                createUserDto.setProvince(dto.getProvince());
                createUserDto.setCity(dto.getCity());
                createUserDto.setZipCode(dto.getZipCode());
                createUserDto.setStreet(dto.getStreet());
                createUserDto.setHouseNumber(dto.getHouseNumber());

                createUserDto.setEmail(dto.getEmail());
                createUserDto.setPhoneNumber(dto.getPhoneNumber());

                createUserDto.setAccountNumber(bankAccount.getAccountNumber());
                dto.setAccountNumber(bankAccount.getAccountNumber());

                userContactFacade.saveUserContact(contact);
                userAddressFacade.saveUserAddress(address);
                userBankAccountFacade.saveUserBankAccount(bankAccount);

                repository.saveAndFlush(factory.create(dto));

                return baseQuery.query(createUserDto);
            }
        }

        return null;
    }

    private String bankAccountNumberGenerator() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        sb.append("PL");
        do {
            for (int i = 3; i < 30; i++) {
                if (i % 5 == 0) {
                    sb.append(" ");
                } else {
                    int num = random.nextInt(10);
                    sb.append(num);
                }
            }
        } while (isAccountNumberExists(sb.toString()));
        return sb.toString();
    }

    private boolean isAccountNumberExists(String accountNumber) {
        return userBankAccountFacade.findByAccountNumber(accountNumber) != null;
    }

    public List<ReadUserQueryDto> getAll(ReadUserDto dto, BindingResult result) {
        if (validator.supports(dto.getClass())) {

            List<User> users = repository.findAll();
            validator.validateGetAllUsers(dto, result);

            if (!result.hasErrors()) {
                List<ReadUserQueryDto> usersDto = new ArrayList<>();

                for (User user : users) {
                    usersDto.add(new ReadUserQueryDto(
                            user.getPesel(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getAddress().getProvince(),
                            user.getAddress().getCity(),
                            user.getAddress().getZipCode(),
                            user.getAddress().getStreet(),
                            user.getAddress().getHouseNumber(),
                            user.getContact().getEmail(),
                            user.getContact().getPhoneNumber(),
                            user.getDecodedBCryptPassword(),
                            user.getBankAccount().getAccountNumber(),
                            user.getBankAccount().getAccountBalance()
                    ));
                }

                return usersDto;
            }
        }

        return null;
    }

    public void deleteAllUsers(ReadUserDto dto, BindingResult result) {
        if (validator.supports(dto.getClass())) {

            validator.validateGetAllUsers(dto, result);

            if (!result.hasErrors()) {
                repository.deleteAll();
            }
        }
    }

    public void deleteUserByPesel(String pesel, ReadUserDto dto, BindingResult result) {
        if (validator.supports(dto.getClass())) {

            validator.validateGetUser(pesel, dto, result);

            if (!result.hasErrors()) {
                repository.deleteById(pesel);
            }
        }
    }

    public ReadUserQueryDto getUserByPesel(String pesel, ReadUserDto dto, BindingResult result) {
        if (validator.supports(dto.getClass())) {

            validator.validate(dto, result);

            if (!result.hasErrors()) {

                User user = repository.findByPesel(pesel);

                readUserDto.setPesel(user.getPesel());
                readUserDto.setFirstName(user.getFirstName());
                readUserDto.setLastName(user.getLastName());
                readUserDto.setPassword(user.getPassword());
                readUserDto.setProvince(user.getAddress().getProvince());
                readUserDto.setCity(user.getAddress().getCity());
                readUserDto.setZipCode(user.getAddress().getZipCode());
                readUserDto.setStreet(user.getAddress().getStreet());
                readUserDto.setHouseNumber(user.getAddress().getHouseNumber());
                readUserDto.setEmail(user.getContact().getEmail());
                readUserDto.setPhoneNumber(user.getContact().getPhoneNumber());
                readUserDto.setAccountNumber(user.getBankAccount().getAccountNumber());
                readUserDto.setAccountBalance(user.getBankAccount().getAccountBalance());

                return baseQuery.query(readUserDto);
            }
        }

        return null;
    }

    public void changePassword(String pesel, ChangeUserPasswordDto dto, BindingResult result) {
        if (validator.supports(dto.getClass())) {

            dto.setPesel(pesel);

            validator.validateChangeUserPassword(dto, result);

            if (!result.hasErrors()) {

                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

                repository.updateUserPassword(passwordEncoder.encode(dto.getNewPassword()), pesel);
                repository.updateUserDecodedBCryptPassword(dto.getNewPassword(), pesel);
                changePasswordDto.setNewPassword(dto.getNewPassword());
            }
        }
    }

    public AppUserDto getAppUser(String login) {
        User user = repository.findByPesel(login);

        if (user != null) {
            return new AppUserDto(
                    user.getPesel(),
                    user.getPassword(),
                    user.getRoles()
            );
        }

        return null;
    }

    public AboutMeUserQueryDto aboutMe(Authentication auth) {
        User user = repository.findByPesel(auth.getName());

        if (user != null) {

            aboutMeUserDto.setPesel(user.getPesel());
            aboutMeUserDto.setFirstName(user.getFirstName());
            aboutMeUserDto.setLastName(user.getLastName());
            aboutMeUserDto.setProvince(user.getAddress().getProvince());
            aboutMeUserDto.setCity(user.getAddress().getCity());
            aboutMeUserDto.setZipCode(user.getAddress().getZipCode());
            aboutMeUserDto.setStreet(user.getAddress().getStreet());
            aboutMeUserDto.setHouseNumber(user.getAddress().getHouseNumber());
            aboutMeUserDto.setEmail(user.getContact().getEmail());
            aboutMeUserDto.setPhoneNumber(user.getContact().getPhoneNumber());
            aboutMeUserDto.setPassword(user.getDecodedBCryptPassword());
            aboutMeUserDto.setAccountNumber(user.getBankAccount().getAccountNumber());
            aboutMeUserDto.setAccountBalance(user.getBankAccount().getAccountBalance());

            return baseQuery.query(aboutMeUserDto);
        }

        return null;
    }

    public void sendTransaction(Authentication auth,
                                SendTransactionDto dto, BindingResult result) {
        User user = repository.findByPesel(auth.getName());

        if (user != null) {
            dto.setCurrentAccountBalance(user.getBankAccount().getAccountBalance());
            dto.setSenderBankAccountNumber(user.getBankAccount().getAccountNumber());
            dto.setPesel(user.getPesel());
            transactionFacade.sendTransaction(dto, result);

            if (dto.getErrors().isEmpty()) {
                userBankAccountFacade.addMoneyToReceivedUser(dto.getAmount(), dto.getReceiverBankAccountNumber());
            }
        }
    }
}
