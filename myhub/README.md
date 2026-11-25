# MyHub Server

## Local development account and password storage

For local development only, `scripts/insert-sadmin.sql` inserts `sadmin` with raw password `sadmin` if that username does not already exist. Run it against the existing `myhub` database after Flyway has created `sys_user`. This intentionally weak credential must not be used in a remotely accessible or production environment. The script does not update an existing account.

Passwords are stored as BCrypt hashes, never plaintext. The backend uses Spring Security `BCryptPasswordEncoder` with strength 10; BCrypt generates a random salt, so the stored hash differs on each encoding of the same password. Login uses `PasswordEncoder.matches(rawPassword, storedHash)`. For example:

```java
PasswordEncoder encoder = new BCryptPasswordEncoder();
String hash = encoder.encode(rawPassword);
boolean matches = encoder.matches(rawPassword, hash);
```

Do not insert plaintext into `sys_user.password`. Existing plaintext rows must be updated with a BCrypt hash before BCrypt login can succeed.
