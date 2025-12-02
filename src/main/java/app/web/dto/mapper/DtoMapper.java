package app.web.dto.mapper;

import app.subscription.model.Subscription;
import app.transactions.model.Transaction;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.web.dto.SubscriptionDto;
import app.web.dto.TransactionDto;
import app.web.dto.UserEditRequest;

public class DtoMapper {

    public static UserEditRequest mapUserToUserEditRequest(User user) {
        UserEditRequest dto = new UserEditRequest();
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setCountry(user.getCountry());
        return dto;
    }

    public static void mapUserEditRequestToUser(UserEditRequest dto, User user) {
        user.setUsername(dto.getUsername());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setProfilePicture(dto.getProfilePicture());
        user.setCountry(dto.getCountry());
    }

    public static Transaction mapTransactionDtoToEntity(TransactionDto dto, Wallet wallet) {
        return Transaction.builder()
                .amount(dto.getAmount())
                .date(dto.getDate())
                .type(dto.getType())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .wallet(wallet)
                .build();
    }

    public static Subscription mapSubscriptionDtoToEntity(SubscriptionDto dto, User user) {
        Subscription subscription = new Subscription();
        subscription.setName(dto.getName());
        subscription.setPeriod(dto.getPeriod());
        subscription.setExpiryOn(dto.getExpiryOn());
        subscription.setType(dto.getType());
        subscription.setPrice(dto.getPrice());
        subscription.setUser(user);
        return subscription;
    }
}
