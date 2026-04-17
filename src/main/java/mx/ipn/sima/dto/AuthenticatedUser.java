package mx.ipn.sima.dto;

import java.util.List;

public record AuthenticatedUser(
        Long userId,
        String username,
        String email,
        List<String> roles
) {
}
