package com.data.fix.ro.config; 
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
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
 
@Configuration
public class DocDbMongoConfig { 
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri; 
    @Value("${docdb.tls.caFile}")
    private String caFilePath; 
    @Bean
    public MongoClient mongoClient() throws Exception {
        SSLContext sslContext = buildSslContextFromPem(caFilePath); 
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoUri))
                .applyToSslSettings(ssl -> {
                    ssl.enabled(true);
                    ssl.invalidHostNameAllowed(true); // you already use tlsAllowInvalidHostnames=true
                    ssl.context(sslContext);
                })
                .build(); 
        return MongoClients.create(settings);
    } 
    private SSLContext buildSslContextFromPem(String pemPath) throws Exception {
        // Load ALL certs from the PEM bundle (global-bundle.pem often contains multiple certs)
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
 

