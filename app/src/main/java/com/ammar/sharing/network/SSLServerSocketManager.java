package com.ammar.sharing.network;

import android.content.Context;

import com.ammar.sharing.common.utils.Utils;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.BigIntegers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class SSLServerSocketManager {
    private final File mCertFile;
    private static final String mALIAS = "sharing-cert";
    private CertificateInfo mCertInfo = new CertificateInfo();

    private static SSLServerSocketManager sInstance;

    public static SSLServerSocketManager getInstance() {
        return sInstance;
    }

    public SSLServerSocketManager(Context context) {
        sInstance = this;

        File filesDir = context.getFilesDir();
        File certDir = new File(filesDir, "cert");
        if (!certDir.exists()) {
            certDir.mkdir();
        }

        mCertFile = new File(certDir, "certificate.pfx");
    }

    public SSLServerSocket generateSSLServerSocket() {
        try {
            KeyStore keyStore = getKeyStore();

            KeyManagerFactory km = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            km.init(keyStore, new char[0]);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(km.getKeyManagers(), null, null);
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(Server.PORT_NUMBER);
            sslServerSocket.setEnabledCipherSuites(sslServerSocket.getSupportedCipherSuites());
            return sslServerSocket;
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException |
                 UnrecoverableKeyException | KeyManagementException e) {
            throw new RuntimeException(e);
        }

    }

    private GeneratedCert generateRandomSelfSignedCertificate() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(4096);
            KeyPair keyPair = keyPairGen.generateKeyPair();

            X500Name issuer = new X500Name("CN=Sharing");
            X500Name subject = new X500Name("CN=Sharing");
            BigInteger serial = BigIntegers.createRandomBigInteger(160, new SecureRandom());
            Date notBefore = new Date(System.currentTimeMillis());
            Date notAfter = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L); // 1 year validity

            JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    issuer,
                    serial,
                    notBefore,
                    notAfter,
                    subject,
                    keyPair.getPublic()
            );
            // Sign the certificate
            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
            X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(certBuilder.build(signer));

            return new GeneratedCert(keyPair.getPrivate(), certificate);

        } catch (NoSuchAlgorithmException | OperatorCreationException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveKeyStoreToFile(KeyStore keyStore) {
        if (mCertFile.exists()) {
            mCertFile.delete();
        }
        try (FileOutputStream fos = new FileOutputStream(mCertFile)) {
            char[] emptyPassword = new char[0];
            keyStore.store(fos, emptyPassword);
        } catch (IOException | CertificateException | KeyStoreException |
                 NoSuchAlgorithmException e) {
            Utils.showErrorDialog("SSLServerSocketManager.saveCertificate()", e.getMessage());
        }
    }

    private KeyStore certToKeyStore(GeneratedCert cert) {
        try {
            char[] emptyPassword = new char[0];
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, emptyPassword);
            keyStore.setKeyEntry(mALIAS, cert.privateKey, emptyPassword, new Certificate[]{cert.certificate});
            return keyStore;
        } catch (KeyStoreException | CertificateException | IOException |
                 NoSuchAlgorithmException e) {
            Utils.showErrorDialog("SSLServerSocketManager.certToKeyStore()", e.getMessage());
            return null;
        }
    }

    private KeyStore getKeyStore() throws KeyStoreException {
        if (mCertFile.exists()) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(mCertFile)) {
                keyStore.load(fis, new char[0]);
            } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
                Utils.showErrorDialog("SSLServerSocketManager.getKeyStore()", e.getMessage());
            }
            X509Certificate certificate = null;
            boolean isValid = true;
            try {
                certificate = (X509Certificate) keyStore.getCertificate(mALIAS);
                certificate.checkValidity();
            } catch (CertificateExpiredException | CertificateNotYetValidException e) {
                isValid = false;
            }
            if (!isValid) {
                if (mCertFile.delete()) {
                    return getKeyStore();
                } else {
                    throw new RuntimeException("This should not happen");
                }
            }
            saveCertInfo(certificate);
            return keyStore;
        } else {
            GeneratedCert cert = generateRandomSelfSignedCertificate();
            saveCertInfo(cert.certificate);
            KeyStore keyStore = certToKeyStore(cert);
            saveKeyStoreToFile(keyStore);
            return keyStore;
        }
    }

    private void saveCertInfo(X509Certificate certificate) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] encodedCert = certificate.getEncoded();
            mCertInfo.certSha256fingerprint = Utils.bytesToHex(sha256.digest(encodedCert));

            PublicKey publicKey = certificate.getPublicKey();
            byte[] encodedPublicKey = publicKey.getEncoded();
            mCertInfo.publicKeySha256fingerprint = Utils.bytesToHex(sha256.digest(encodedPublicKey));
        } catch (NoSuchAlgorithmException | CertificateEncodingException ignore) {}
    }

    public boolean isCertCreated() {
        return mCertFile.exists();
    }

    public CertificateInfo getCertInfo() {
        return mCertInfo;
    }

    final static class GeneratedCert {
        public final PrivateKey privateKey;
        public final X509Certificate certificate;

        public GeneratedCert(PrivateKey privateKey, X509Certificate certificate) {
            this.privateKey = privateKey;
            this.certificate = certificate;
        }
    }

    public static class CertificateInfo {
        public String commonName;
        public String certSha256fingerprint;
        public String publicKeySha256fingerprint;
    }
}
