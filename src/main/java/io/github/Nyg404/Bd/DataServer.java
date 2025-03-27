package io.github.Nyg404.Bd;

import lombok.Getter;

@Getter
public class DataServer {
    private final Long serverId;
    private final String prefix;

    public DataServer(Long serverId, String prefix) {
        this.serverId = serverId;
        this.prefix = prefix;
    }

}
