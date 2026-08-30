#[macro_use]
extern crate log;
extern crate android_logger;

use android_logger::Config;
use jni::{
    refs::Reference,
    sys::jint,
    vm::JavaVM,
};
use log::LevelFilter;
use std::ffi::c_void;

use crate::{
    certs::{CertFingerprintsJava, CertificateManagerWrapperJava}, java_api_binds::{
        LambdaReturnIntJava, LiveDataSingletonsJava, QueueMutableLiveDataJava,
    },
};

pub(crate) mod java_api_binds;
mod web_server;
mod transfer;
mod users;
mod certs;

#[allow(non_snake_case)]
#[unsafe(no_mangle)]
pub extern "system" fn JNI_OnLoad(vm: *mut jni::sys::JavaVM, _: *mut c_void) -> jint {
    android_logger::init_once(
        Config::default()
            .with_max_level(LevelFilter::Trace)
            .with_tag("RUST_LOG"),
    );
    let vm = unsafe { JavaVM::from_raw(vm) };
    vm.with_top_local_frame(|env| -> Result<jint, jni::errors::Error> {
        let jni_version = env.version().expect("Failed to get JNI version");
        info!("JNI_VERSION: {:?}", jni_version);

        LiveDataSingletonsJava::lookup_class(env, &jni::refs::LoaderContext::None)?;
        QueueMutableLiveDataJava::lookup_class(env, &jni::refs::LoaderContext::None)?;
        CertificateManagerWrapperJava::lookup_class(env, &jni::refs::LoaderContext::None)?;
        CertFingerprintsJava::lookup_class(env, &jni::refs::LoaderContext::None)?;
        LambdaReturnIntJava::lookup_class(env, &jni::refs::LoaderContext::None)?;
        

        Ok(jni_version.into())
    })
    .unwrap()
}

