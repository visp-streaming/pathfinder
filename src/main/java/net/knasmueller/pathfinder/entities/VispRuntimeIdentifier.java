package net.knasmueller.pathfinder.entities;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Uniquely identifies a VISP runtime by IP and port
 */
@Entity
public class VispRuntimeIdentifier {
    String ip;
    int port;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    public VispRuntimeIdentifier() {

    }

    public VispRuntimeIdentifier(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public VispRuntimeIdentifier(String endpoint) {
        if(endpoint.contains("//")) {
            String[] splitted = endpoint.split("//");
            endpoint = splitted[1];
        }

        String[] splitted = endpoint.split(":");
        this.ip = splitted[0];
        this.port = Integer.parseInt(splitted[1]);

        if(this.ip.toLowerCase().equals("localhost")) {
            this.ip = "127.0.0.1";
        }
    }

    @Override
    public String toString() {
        return this.ip + ":" + this.port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VispRuntimeIdentifier that = (VispRuntimeIdentifier) o;

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
