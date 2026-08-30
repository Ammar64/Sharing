use jni::{objects::JString, sys::jint, vm::JavaVM};
use share_any_backend::web_server::{self, my_observer::ChangeType};
use share_any_backend::web_server::user::UserOS;
use crate::java_api_binds::{AndroidBundleJava, LiveDataSingletonsJava};


jni::bind_java_type! {
    UserJava => com.ammar.sharing.models.User,
    constructors {
        fn new(name: JString, ip: JString, index: jint, is_blocked: jboolean, is_connected: jboolean)
    },
    fields {
        native_index: jint
    },
    native_methods {
        static extern fn get_user(index: jint) -> UserJava,
        raw static extern fn users_count() -> jint,
        raw static extern fn no_users() -> jboolean,
        raw extern fn native_set_blocked(index: jint, blocked: jboolean),
        raw extern fn native_get_os(index: jint) -> jint
    }
}

impl UserJavaNativeInterface for UserJavaAPI {
    type Error = jni::errors::Error;

    fn get_user<'local>(
        env: &mut ::jni::Env<'local>,
        _class: ::jni::objects::JClass<'local>,
        index: ::jni::sys::jint,
    ) -> ::std::result::Result<UserJava<'local>, Self::Error> {
        let user = web_server::get_user_by_index(index as usize);
        let name = JString::from_str(env, user.name()).unwrap();
        let ip = JString::from_str(env, user.ip_addr().to_string()).unwrap();
        return Ok(
            UserJava::new(env, name, ip, index, user.is_blocked(), user.is_connected()).unwrap(),
        );
    }

    fn users_count<'local>(
        _unowned_env: ::jni::EnvUnowned<'local>,
        _class: ::jni::objects::JClass<'local>,
    ) -> ::jni::sys::jint {
        let num_users = web_server::get_users_count();
        num_users as jint
    }

    fn no_users<'local>(
        _unowned_env: ::jni::EnvUnowned<'local>,
        _class: ::jni::objects::JClass<'local>,
    ) -> ::jni::sys::jboolean {
        web_server::users_vec_is_empty()
    }

    fn native_set_blocked<'local>(
        _unowned_env: ::jni::EnvUnowned<'local>,
        _this: UserJava<'local>,
        index: jint,
        blocked: ::jni::sys::jboolean,
    ) {
        let user = web_server::get_user_by_index(index as usize);
        user.set_blocked(blocked);
    }

    fn native_get_os<'local>(
        _unowned_env: ::jni::EnvUnowned<'local>,
        _this: UserJava<'local>,
        index: jint,
    ) -> ::jni::sys::jint {
        let user = web_server::get_user_by_index(index as usize);
        let user_os = user.os();
        // must be in the same order as the kotlin enum.
        match user_os {
            UserOS::Linux => 0,
            UserOS::Windows => 1,
            UserOS::Android => 2,
            UserOS::IOS => 3,
            UserOS::Mac => 4,
            UserOS::Unknown => 5
        }
    }
}


pub(crate) fn setup_users_observer(java_vm: JavaVM) {
    web_server::subscribe_to_users_observable_vec(move |diff| {
        java_vm
            .attach_current_thread(|env| -> Result<(), jni::errors::Error> {
                match diff {
                    ChangeType::ValueChanged { index } => {
                        let bundle = AndroidBundleJava::new(env)?;
                        let key_action = JString::from_jni_str(env, jni::jni_str!("action"))?;
                        let key_index = JString::from_jni_str(env, jni::jni_str!("index"))?;

                        bundle.put_char(
                            env,
                            key_action,
                            jni::strings::char_to_java('C').unwrap(),
                        )?;
                        bundle.put_int(env, key_index, *index as i32)?;
                        LiveDataSingletonsJava::users_list_observer(env)?
                            .force_post_value(env, bundle)?;
                    }
                    ChangeType::ValueInserted { index } => {
                        let bundle = AndroidBundleJava::new(env)?;
                        let key_action = JString::from_jni_str(env, jni::jni_str!("action"))?;
                        let key_index = JString::from_jni_str(env, jni::jni_str!("index"))?;

                        bundle.put_char(
                            env,
                            key_action,
                            jni::strings::char_to_java('A').unwrap(),
                        )?;
                        bundle.put_int(env, key_index, *index as i32)?;
                        debug!("Before force push value");
                        LiveDataSingletonsJava::users_list_observer(env)?
                            .force_post_value(env, bundle)?;
                    }
                    ChangeType::ValueRemoved { index: _ } => todo!(),
                };
                Ok(())
            })
            .unwrap_or_else(|err| {
                error!("JNI error: {}", err);
            });
    });
}
