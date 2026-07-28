package com.esmpf;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.esmpf.identity.IdentityDtos.UserAccountCreateCommand;
import com.esmpf.identity.IdentityDtos.UserAccountResponse;
import com.esmpf.identity.IdentityDtos.UserAccountUpdateCommand;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserRestContractArchitectureTests {
    private static final Set<String> FORBIDDEN_PUBLIC_USER_FIELDS = Set.of(
            "passwordHash",
            "role",
            "externalProvider",
            "externalSubject"
    );

    @Test
    void ordinaryUserDtosMustNotExposeCredentialsRolesOrExternalIdentity() {
        assertSafe(UserAccountCreateCommand.class);
        assertSafe(UserAccountUpdateCommand.class);
        assertSafe(UserAccountResponse.class);
    }

    private static void assertSafe(Class<?> type) {
        var names = Arrays.stream(type.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();
        FORBIDDEN_PUBLIC_USER_FIELDS.forEach(field ->
                assertFalse(names.contains(field), () -> type.getSimpleName() + " exposes forbidden field " + field));
    }
}
