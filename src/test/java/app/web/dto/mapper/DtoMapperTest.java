package app.web.dto.mapper;

import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionType;
import app.transactions.model.Category;
import app.transactions.model.Transaction;
import app.transactions.model.Type;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.wallet.model.Wallet;
import app.web.dto.SubscriptionDto;
import app.web.dto.TransactionDto;
import app.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DtoMapperTest {

    @Test
    void testMapUserToUserEditRequest() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .profilePicture("profile.jpg")
                .country(Country.BULGARIA)
                .role(Role.USER)
                .userVersion(UserVersion.BASIC)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        UserEditRequest result = DtoMapper.mapUserToUserEditRequest(user);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("profile.jpg", result.getProfilePicture());
        assertEquals(Country.BULGARIA, result.getCountry());
    }

    @Test
    void testMapUserToUserEditRequest_withNullFields() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .firstName(null)
                .lastName(null)
                .profilePicture(null)
                .country(Country.BULGARIA)
                .role(Role.USER)
                .userVersion(UserVersion.BASIC)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        UserEditRequest result = DtoMapper.mapUserToUserEditRequest(user);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertNull(result.getFirstName());
        assertNull(result.getLastName());
        assertNull(result.getProfilePicture());
    }

    @Test
    void testMapUserEditRequestToUser() {

        UserEditRequest dto = UserEditRequest.builder()
                .username("newuser")
                .firstName("New")
                .lastName("User")
                .email("new@example.com")
                .profilePicture("new.jpg")
                .country(Country.USA)
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("olduser")
                .email("old@example.com")
                .build();

        DtoMapper.mapUserEditRequestToUser(dto, user);

        assertEquals("newuser", user.getUsername());
        assertEquals("New", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("new.jpg", user.getProfilePicture());
        assertEquals(Country.USA, user.getCountry());
    }

    @Test
    void testMapUserEditRequestToUser_withNullFields() {

        UserEditRequest dto = UserEditRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .firstName(null)
                .lastName(null)
                .profilePicture(null)
                .country(null)
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("olduser")
                .firstName("Old")
                .lastName("User")
                .build();

        DtoMapper.mapUserEditRequestToUser(dto, user);

        assertEquals("newuser", user.getUsername());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getProfilePicture());
        assertNull(user.getCountry());
    }

    @Test
    void testMapTransactionDtoToEntity() {

        TransactionDto dto = TransactionDto.builder()
                .amount(new BigDecimal("50.00"))
                .date(LocalDateTime.now())
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .description("Groceries")
                .build();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .name("Default")
                .income(new BigDecimal("1000.00"))
                .expense(new BigDecimal("200.00"))
                .balance(new BigDecimal("800.00"))
                .build();

        Transaction result = DtoMapper.mapTransactionDtoToEntity(dto, wallet);

        assertNotNull(result);
        assertEquals(new BigDecimal("50.00"), result.getAmount());
        assertEquals(dto.getDate(), result.getDate());
        assertEquals(Type.EXPENSE, result.getType());
        assertEquals(Category.FOOD, result.getCategory());
        assertEquals("Groceries", result.getDescription());
        assertEquals(wallet, result.getWallet());
    }

    @Test
    void testMapTransactionDtoToEntity_withNullDescription() {

        TransactionDto dto = TransactionDto.builder()
                .amount(new BigDecimal("50.00"))
                .date(LocalDateTime.now())
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .description(null)
                .build();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .name("Default")
                .income(new BigDecimal("1000.00"))
                .expense(new BigDecimal("200.00"))
                .balance(new BigDecimal("800.00"))
                .build();

        Transaction result = DtoMapper.mapTransactionDtoToEntity(dto, wallet);

        assertNotNull(result);
        assertNull(result.getDescription());
        assertEquals(new BigDecimal("50.00"), result.getAmount());
        assertEquals(wallet, result.getWallet());
    }

    @Test
    void testMapSubscriptionDtoToEntity() {

        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Netflix")
                .price(new BigDecimal("19.99"))
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.DEFAULT)
                .expiryOn(LocalDate.now().plusDays(30))
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .role(Role.USER)
                .userVersion(UserVersion.BASIC)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        Subscription result = DtoMapper.mapSubscriptionDtoToEntity(dto, user);

        assertNotNull(result);
        assertEquals("Netflix", result.getName());
        assertEquals(SubscriptionPeriod.MONTHLY, result.getPeriod());
        assertEquals(dto.getExpiryOn(), result.getExpiryOn());
        assertEquals(SubscriptionType.DEFAULT, result.getType());
        assertEquals(new BigDecimal("19.99"), result.getPrice());
        assertEquals(user, result.getUser());
    }

    @Test
    void testMapSubscriptionDtoToEntity_withAllFields() {

        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Netflix")
                .price(new BigDecimal("19.99"))
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.DEFAULT)
                .expiryOn(LocalDate.now().plusDays(30))
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .role(Role.USER)
                .userVersion(UserVersion.BASIC)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        Subscription result = DtoMapper.mapSubscriptionDtoToEntity(dto, user);

        assertNotNull(result);
        assertNotNull(result.getName());
        assertNotNull(result.getPeriod());
        assertNotNull(result.getExpiryOn());
        assertNotNull(result.getType());
        assertNotNull(result.getPrice());
        assertNotNull(result.getUser());
    }
}
