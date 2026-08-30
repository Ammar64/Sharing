#[macro_use]
extern crate log;
extern crate android_logger;

use android_logger::Config;
use jni::{refs::Reference, sys::jint, vm::JavaVM};
use log::LevelFilter;
use std::ffi::c_void;

use crate::{
    certs::{CertFingerprintsJava, CertificateManagerWrapperJava},
    java_api_binds::{LambdaReturnIntJava, LiveDataSingletonsJava, QueueMutableLiveDataJava},
};

mod certs;
pub(crate) mod java_api_binds;
mod transfer;
mod users;
mod web_server;

#[allow(non_snake_case)]
#[unsafe(no_mangle)]
pub extern "system" fn JNI_OnLoad(vm: *mut jni::sys::JavaVM, _: *mut c_void) -> jint {
    android_logger::init_once(
        Config::default()
            .with_max_level(LevelFilter::Trace)
            .with_tag("RUST_LOG"),
    );

    std::panic::set_hook(Box::new(|panic_info| {
        let msg = if let Some(s) = panic_info.payload().downcast_ref::<&str>() {
            s.to_string()
        } else if let Some(s) = panic_info.payload().downcast_ref::<String>() {
            s.clone()
        } else {
            "unknown panic payload".to_string()
        };
        let location = panic_info
            .location()
            .map(|l| format!("{}:{}:{}", l.file(), l.line(), l.column()))
            .unwrap_or_default();

        // if using `log`/`android_logger`:
        log::error!("PANIC at {}: {}", location, msg);
    }));

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
