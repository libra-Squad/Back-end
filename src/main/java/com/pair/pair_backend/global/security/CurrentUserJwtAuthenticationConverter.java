package com.pair.pair_backend.global.security;

import com.pair.pair_backend.auth.entity.User;
import com.pair.pair_backend.auth.repository.UserRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CurrentUserJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final UserRepository userRepository;
    private final EntityManagerFactory entityManagerFactory;

    @Override
    @Transactional(readOnly = true)
    public AbstractAuthenticationToken convert(Jwt jwt) {
        User user = userRepository.findById(jwt.getSubject())
                .orElseThrow(() -> new InvalidBearerTokenException("Invalid Bearer Token"));

        Object companyIdentifier = user.getCompanyId() == null ? null : entityManagerFactory.getPersistenceUnitUtil().getIdentifier(user.getCompanyId());
        String companyId = companyIdentifier == null ? null : companyIdentifier.toString();
        CurrentUser currentUser = new CurrentUser(user.getUserId(), user.getRole(), companyId);

        return new UsernamePasswordAuthenticationToken(currentUser, null, List.of(new SimpleGrantedAuthority(user.getRole().name())));
    }
}
