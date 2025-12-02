package app.security;

import app.user.model.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final UUID userId;
    private final String username;
    private final String email;
    private final Role role;
    private final boolean isActive;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(UUID userId, String username, String email, Role role,
                            boolean isActive, Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
        this.authorities = authorities;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return username;
    }
}


