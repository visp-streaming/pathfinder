package net.knasmueller.pathfinder_deployment;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.openstack.OSFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OpenstackConnector {

    private static final Logger LOG = LoggerFactory.getLogger(OpenstackConnector.class);

    private OSClient.OSClientV2 os;

    private void setup() {

        Map<String, String> env = System.getenv();

        String OS_AUTH_URL = env.get("AUTH_URL");
        String OS_USERNAME = env.get("USERNAME");
        String OS_PASSWORD = env.get("PASSWORD");
        String OS_TENANT_NAME = env.get("PROJECT_NAME");

        os = OSFactory.builderV2()
                .endpoint(OS_AUTH_URL)
                .credentials(OS_USERNAME, OS_PASSWORD)
                .tenantName(OS_TENANT_NAME)
                .authenticate();
    }

    public String startInstance(String serverName) {
        setup();

        String KEYPAIR_NAME = System.getenv().get("KEYPAIR_NAME");

        Flavor flavor = os.compute().flavors().get("m2.medium");

        // ?
        for (Flavor f : os.compute().flavors().list()) {
            if (f.getName().equals("m2.medium")) {
                flavor = f;
                break;
            }
        }

        ServerCreate sc = Builders.server()
                .name(serverName)
                .flavor(flavor)
                .image("3a233e8a-e3c9-4076-9cbf-7526dd32634b") // coreOS
                .keypairName(KEYPAIR_NAME)
                .addSecurityGroup("default")
                .build();

        os.compute().servers().boot(sc);

        LOG.info("Server with id: " + serverName + " was started.");

        return serverName;
    }

    public final void destroyInstanceByName(final String id) {
        setup();

        ActionResponse r = null;

        for (Server f : os.compute().servers().list()) {
            if (f.getName().equals(id)) {
                r = os.compute().servers().delete(f.getId());
                break;
            }
        }


        if (!r.isSuccess()) {
            LOG.error("Dockerhost could not be stopped: " + r.getFault());
        } else {
            LOG.info("DockerHost terminated " + id);
        }
    }

    public String getIpByName(final String id) {
        setup();

        for (Server f : os.compute().servers().list()) {
            if (f.getName().equals(id)) {
                try {
                    return f.getAddresses().getAddresses().get("private").get(0).getAddr();
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }


}
