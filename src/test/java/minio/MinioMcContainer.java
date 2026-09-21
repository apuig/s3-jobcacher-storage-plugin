package minio;

import static java.lang.String.format;

import java.io.IOException;
import org.testcontainers.containers.GenericContainer;

public class MinioMcContainer extends GenericContainer<MinioMcContainer> {

    private final MinioContainer minio;

    public MinioMcContainer(MinioContainer minio) {
        super("amazon/aws-cli:2.36.46");
        this.minio = minio;
        dependsOn(minio);
        withNetwork(minio.getNetwork());
        withCreateContainerCmdModifier(c -> c.withTty(true).withEntrypoint("/bin/sh"));
        withEnv("AWS_ACCESS_KEY_ID", minio.accessKey());
        withEnv("AWS_SECRET_ACCESS_KEY", minio.secretKey());
    }

    private String endpointUrl() {
        return format("http://%s:9000", minio.getNetworkAliases().get(0));
    }

    public ExecResult execSecure(String command, Object... args) throws IOException, InterruptedException {
        ExecResult result = exec(command, args);
        if (result.getExitCode() != 0) {
            throw new AssertionError(result.getStderr());
        }
        return result;
    }

    public ExecResult exec(String command, Object... args) throws IOException, InterruptedException {
        return execInContainer("/bin/sh", "-c", format(command, args));
    }

    public void deleteBucket(String bucket) throws IOException, InterruptedException {
        exec("aws --endpoint-url %s s3 rb s3://%s --force", endpointUrl(), bucket);
    }

    public void createBucket(String bucket) throws IOException, InterruptedException {
        execSecure("aws --endpoint-url %s s3 mb s3://%s", endpointUrl(), bucket);
    }

    public void createObject(String bucket, String key, String content) throws IOException, InterruptedException {
        execSecure("echo -n \"%s\" | aws --endpoint-url %s s3 cp - s3://%s/%s", content, endpointUrl(), bucket, key);
    }
}
