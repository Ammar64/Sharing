package com.ammar.sharing.nativebackend;

@SuppressWarnings("JavaJniMissingFunction")
public class CertificateManagerWrapper {
    public static native CertFingerprints getCertSha256Fingerprints();
    public static native void forceMakeNewCert();

}
