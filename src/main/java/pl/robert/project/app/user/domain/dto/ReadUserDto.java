package pl.robert.project.app.user.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter @Setter
@NoArgsConstructor
public class ReadUserDto extends UserDto {

    public ReadUserDto(String pesel, String firstName, String lastName,
                       String province, String city, String zipCode, String street, String houseNumber,
                       String email, String phoneNumber,
                       String password,
                       String accountNumber, Double accountBalance) {
        super(pesel, firstName, lastName,
              province, city, zipCode, street, houseNumber,
              email, phoneNumber, password, null,
              accountNumber, accountBalance,
              0);
    }
}
