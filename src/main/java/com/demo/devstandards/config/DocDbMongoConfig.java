package com.demo.devstandards.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import javax.net.ssl.TrustManagerFactory;

/**
 * Configuration class for AWS DocumentDB (MongoDB-compatible) connectivity.
 * Uses TLS/SSL with a PEM certificate bundle for secure connections.
 * Authenticates via AWS IAM (MONGODB-AWS mechanism).
 *
 * Prerequisites:
 * - SSH tunnel to DocumentDB must be running (localhost:27017)
 * - AWS credentials must be set as environment variables:
 *   AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_SESSION_TOKEN
 * - Run saml2aws login before starting the application
 */
@Configuration
public class DocDbMongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${docdb.tls.caFile}")
    private String caFilePath;

    @Bean
    public MongoClient mongoClient() throws Exception {
        SSLContext sslContext = buildSslContextFromPem(caFilePath);

        // MONGODB-AWS credential: the driver reads AWS_ACCESS_KEY_ID,
        // AWS_SECRET_ACCESS_KEY, and AWS_SESSION_TOKEN from environment variables
        MongoCredential credential = MongoCredential.createAwsCredential(null, null);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoUri))
                .credential(credential)
                .applyToSslSettings(ssl -> {
                    ssl.enabled(true);
                    ssl.invalidHostNameAllowed(true);
                    ssl.context(sslContext);
                })
                .build();

        return MongoClients.create(settings);
    }

    private SSLContext buildSslContextFromPem(String pemPath) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certs;

        try (InputStream is = new FileInputStream(pemPath)) {
            certs = (Collection<? extends Certificate>) cf.generateCertificates(is);
        }

        if (certs == null || certs.isEmpty()) {
            throw new IllegalArgumentException("No certificates found in PEM file: " + pemPath);
        }

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null);

        int i = 1;
        for (Certificate cert : certs) {
            ks.setCertificateEntry("docdb-ca-" + (i++), cert);
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
        return sslContext;
    }
}
