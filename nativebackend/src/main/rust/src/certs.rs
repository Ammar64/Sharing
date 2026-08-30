use jni::objects::JClass;

jni::bind_java_type! {
    pub(crate) CertFingerprintsJava => com.ammar.sharing.nativebackend.CertFingerprints,
    constructors {
        fn new(cert_fingerprint: JString, key_fingerprint: JString)
    }
}

jni::bind_java_type! {
    pub(crate) CertificateManagerWrapperJava => com.ammar.sharing.nativebackend.CertificateManagerWrapper,
    type_map = {
        CertFingerprintsJava => com.ammar.sharing.nativebackend.CertFingerprints
    },
    native_methods {
        static fn get_cert_sha256_fingerprints() -> CertFingerprintsJava
    }
}

impl CertificateManagerWrapperJavaNativeInterface for CertificateManagerWrapperJavaAPI {
    type Error = jni::errors::Error;

    fn get_cert_sha256_fingerprints<'local>(
        env: &mut ::jni::Env<'local>,
        _: JClass<'local>,
    ) -> ::std::result::Result<CertFingerprintsJava<'local>, Self::Error> {
        let cert_sha256 = share_any_backend::certs::get_cert_sha256_fingerprint();
        let key_sha256 = share_any_backend::certs::get_key_sha256_fingerprint();

        let cert_fingerprint = env.new_string(cert_sha256)?;
        let key_fingerprint = env.new_string(key_sha256)?;

        let cert_fingerprints = CertFingerprintsJava::new(env, cert_fingerprint, key_fingerprint)?;
        Ok(cert_fingerprints)
    }
}
