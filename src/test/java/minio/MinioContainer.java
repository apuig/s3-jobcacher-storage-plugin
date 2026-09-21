package minio;

import java.util.UUID;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

public class MinioContainer extends GenericContainer<MinioContainer> {

    public MinioContainer() {
        this("rustfs/rustfs:1.0.0");
    }

    public MinioContainer(String dockerImageName) {
        super(dockerImageName);

        setWaitStrategy(Wait.forListeningPort());

        withEnv("RUSTFS_ACCESS_KEY", UUID.randomUUID().toString());
        withEnv("RUSTFS_SECRET_KEY", UUID.randomUUID().toString());
        withExposedPorts(9000);
        withNetwork(
                Network.newNetwork()); // we need a dedicated network otherwise the aws-cli container cannot participate
    }

    public String accessKey() {
        return getEnvMap().get("RUSTFS_ACCESS_KEY");
    }

    public String secretKey() {
        return getEnvMap().get("RUSTFS_SECRET_KEY");
    }

    public String getExternalAddress() {
        return "http://localhost:" + getMappedPort(9000);
    }
}
