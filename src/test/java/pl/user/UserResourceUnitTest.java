package pl.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.security.user_role.UserRole;
import pl.security.user_role.UserRoleProvider;
import pl.security.user_role.UserRoleService;
import pl.user.item_key.UserItemKey;
import pl.user.item_key.UserItemKeyService;
import pl.wallet.WalletProvider;
import pl.wallet.category.CategoryProvider;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.http.HttpStatus.CREATED;

class UserResourceUnitTest {
    private UserRepository userRepository;
    private UserRoleProvider userRoleProvider;
    private PasswordEncoder passwordEncoder;
    private CategoryProvider categoryProvider;
    private UserItemKeyService userItemKeyService;
    private WalletProvider walletProvider;
    private UserService userService;
    private UserResource userResource;

    @BeforeEach
    void init() {
        userRepository = Mockito.mock(UserRepository.class);
        userRoleProvider = Mockito.mock(UserRoleProvider.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        categoryProvider = Mockito.mock(CategoryProvider.class);
        userItemKeyService = Mockito.mock(UserItemKeyService.class);
        walletProvider = Mockito.mock(WalletProvider.class);
        userService = new UserService(userRepository, userRoleProvider, passwordEncoder, categoryProvider, userItemKeyService, walletProvider);
        userResource = new UserResource(userService);
    }

    @Test
    void registerReturnUserDtoWhenRegisteredSuccessful() {
        // given
        UserDto userToRegistration = UserRandomTool.randomUserDto();
        // when
        Mockito.when(passwordEncoder.encode(any())).thenReturn(userToRegistration.getPassword());
        List<UserRole> defaultUserRoles = Collections.singletonList(new UserRole("ROLE_USER", "Default role"));
        Mockito.when(userRoleProvider.getDefaults()).thenReturn(new ArrayList<>(defaultUserRoles));
        User userReturnedByUserRepository = User.builder().userName(userToRegistration.getUserName()).email(userToRegistration.getEmail()).password(userToRegistration.getPassword()).id(1L).build();
        Mockito.when(userRepository.save(any())).thenReturn(userReturnedByUserRepository);
        ResponseEntity<UserDto> registeredUserResponseEntity = userResource.register(userToRegistration);
        // then
        UserDto expectedUserDto = UserDto.builder().email(userToRegistration.getEmail()).userName(userToRegistration.getUserName()).build();
        Assertions.assertEquals(registeredUserResponseEntity.getStatusCode(), CREATED);
        Assertions.assertEquals(registeredUserResponseEntity.getBody(), expectedUserDto);
    }

    @Test
    void editUserReturnEditedUserWhenAllFieldsValid() {
        // given
        User user = UserRandomTool.randomUser();
        user.setPassword(null);
        UserDto userWithUpdatedFields = UserRandomTool.randomUserDto();
        userWithUpdatedFields.addItem("favoriteWalletId", "1");
        // when
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.empty(), Optional.of(user));
        Mockito.when(userItemKeyService.getAll()).thenReturn(new ArrayList<>(Collections.singletonList(UserItemKey.builder().name("favoriteWalletId").build())));
        Mockito.when(userRepository.save(any())).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0, User.class);
            userToSave.setPassword(null);
            return userToSave;
        });
        Principal principal = user::getEmail;
        ResponseEntity<UserDto> updatedUserResponseEntity = userResource.editUser(principal, userWithUpdatedFields);
        // then
        UserDto expectedUpdatedUser = UserDto.builder().email(userWithUpdatedFields.getEmail()).items(userWithUpdatedFields.getItems()).userName(user.getUserName()).build();
        Assertions.assertEquals(updatedUserResponseEntity.getStatusCode(), HttpStatus.OK);
        Assertions.assertEquals(updatedUserResponseEntity.getBody(), expectedUpdatedUser);
    }
}
