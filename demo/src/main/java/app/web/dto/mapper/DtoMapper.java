package app.web.dto.mapper;

import app.user.model.User;
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
}
