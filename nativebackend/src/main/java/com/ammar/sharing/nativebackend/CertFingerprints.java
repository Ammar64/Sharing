package com.ammar.sharing.nativebackend;

import androidx.annotation.Keep;

@Keep
public class CertFingerprints {
    public CertFingerprints(
            String certFingerprint,
            String keyFingerprint
    ) {
        this.certSha256Fingerprint = certFingerprint;
        this.publicKeySha256Fingerprint = keyFingerprint;
    }
    public String certSha256Fingerprint;
    public String publicKeySha256Fingerprint;
}
