package com.esmpf.support;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class BusinessTestFixture {

    private final JdbcTemplate jdbcTemplate;

    public BusinessTestFixture(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UUID createActiveBusiness() {
        UUID id = UUID.randomUUID();
        String code = "TEST-" + UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO business(
                    id,created_at,updated_at,version,name,code,timezone,
                    default_language,currency,status
                ) VALUES (?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,?,?,'Asia/Almaty','ru','KZT','ACTIVE')
                """, id, code, code);
        return id;
    }
}
