package net.knasmueller.pathfinder.entities;


public class VispRuntime {
    String ip;
    int port;

    public VispRuntime(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public VispRuntime(String endpoint) {
        String[] splitted = endpoint.split(":");
        this.ip = splitted[0];
        this.port = Integer.parseInt(splitted[1]);
    }

    @Override
    public String toString() {
        return this.ip + ":" + this.port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VispRuntime that = (VispRuntime) o;

        if (port != that.port) return false;
        return ip.equals(that.ip);
    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + port;
        return result;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
