package com.kik.atn;


class OrbsNodeUrlProvider {

    boolean isHttps() {
        return false;
    }

    String getHost() {
        return "kin.nodes.orbs.network";
    }

    int getPort() {
        return 80;
    }
}
