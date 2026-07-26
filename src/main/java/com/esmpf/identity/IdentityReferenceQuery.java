package com.esmpf.identity;

import com.esmpf.identity.IdentityDtos.UserReference;
import java.util.UUID;

public interface IdentityReferenceQuery {
    UserReference requireUser(UUID userId);
}