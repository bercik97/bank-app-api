package pl.robert.project.app.user.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pl.robert.project.app.admin.domain.AdminFacade;
import pl.robert.project.app.admin.query.BaseAdminQuery;
import pl.robert.project.app.user.domain.dto.CreateUserDto;

@Configuration
@Import({AdminFacade.class})
class UserConfiguration {

    @Bean
    UserFacade facade(UserRepository repository,
                      UserFactory factory,
                      UserValidator validator,
                      BaseAdminQuery query,
                      CreateUserDto createDto) {
        return new UserFacade(repository, factory, validator, query,
                              createDto);
    }
}
