package com.weironx.avalon.token;

import java.util.UUID;

/**
 * @author weironx
 */
public class UuidTokenStrategy implements TokenStrategy {

    @Override
    public String generate() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

}
