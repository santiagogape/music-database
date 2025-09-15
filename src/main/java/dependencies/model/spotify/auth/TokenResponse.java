package dependencies.model.spotify.auth;

public record TokenResponse(String access_token, String token_type, String expires_in) {}

