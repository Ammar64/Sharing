use std::path::PathBuf;
use jni::{EnvUnowned, sys::jboolean};


use share_any_backend::web_server::{self, WebAppServerConfig, WebAppSettings};

use crate::java_api_binds::EnvironmentJava;

jni::bind_java_type! {
    pub(crate) WebAppServerConfigJava => com.ammar.sharing.network.WebServer::WebAppServerConfig,
    fields { 
        files_dir: JString,
        port: jshort,
        is_https: jboolean
    }
}

jni::bind_java_type! {
    pub(crate) WebAppSettingsJava => com.ammar.sharing.network.WebServer::WebAppSettings,
    fields {
        downloads_translation: JString,
        download_all_translation: JString,
        language: JString,
        direction: JString,
        is_dark_mode: jboolean,
        upload_allowed: jboolean
    }
}

jni::bind_java_type! {
    pub(crate) WebServerJava => com.ammar.sharing.network.WebServer,
    type_map = {
        WebAppServerConfigJava => com.ammar.sharing.network.WebServer::WebAppServerConfig,
        WebAppSettingsJava => com.ammar.sharing.network.WebServer::WebAppSettings,
    },
    native_methods {
        fn native_start_server(web_app_server_config: WebAppServerConfigJava) -> jboolean,
        raw fn native_stop_server() -> jboolean,
        fn native_update_ui_config(ui_config: WebAppSettingsJava),
    }
}

impl WebServerJavaNativeInterface for WebServerJavaAPI {
    type Error = jni::errors::Error;

    fn native_start_server<'local>(
        env: &mut ::jni::Env<'local>,
        _this: WebServerJava<'local>,
        web_app_server_config: WebAppServerConfigJava<'local>,
    ) -> ::std::result::Result<::jni::sys::jboolean, Self::Error> {
        let app_dir = web_app_server_config.files_dir(env)?.try_to_string(env)?;
        let app_dir = PathBuf::from(app_dir);
        let port = web_app_server_config.port(env)? as u16;
        let is_https = web_app_server_config.is_https(env)?;

        let web_app_server_config: WebAppServerConfig = WebAppServerConfig {
            app_dir,
            port,
            is_https,
            upload_dir: EnvironmentJava::get_external_storage_directory(env)?.get_path(env)?.to_string().into(),
        };

        let java_vm = env.get_java_vm()?;
        crate::users::setup_users_observer(java_vm);
        Ok(web_server::start_share_any_server(web_app_server_config))
    }

    fn native_stop_server<'local>(_: EnvUnowned<'local>, _this: WebServerJava<'local>) -> jboolean {
        web_server::clear_all_users_observable_vec_subscriptions();
        web_server::stop_share_any_server()
    }

    fn native_update_ui_config<'local>(
        env: &mut ::jni::Env<'local>,
        _this: WebServerJava<'local>,
        web_app_settings: WebAppSettingsJava<'local>,
    ) -> ::std::result::Result<(), Self::Error> {
        let downloads_translation = web_app_settings.downloads_translation(env)?.try_to_string(env)?.into_boxed_str();
        let download_all_translation = web_app_settings.download_all_translation(env)?.try_to_string(env)?.into_boxed_str();
        let language = web_app_settings.language(env)?.try_to_string(env)?.into_boxed_str();
        let direction = web_app_settings.direction(env)?.try_to_string(env)?.into_boxed_str();
        let is_dark_mode = web_app_settings.is_dark_mode(env)?;
        let upload_allowed = web_app_settings.upload_allowed(env)?;
        web_server::update_share_any_web_app_settings(WebAppSettings {
            downloads_translation,
            download_all_translation,
            language,
            direction,
            is_dark_mode,
            upload_allowed,
        });
        Ok(())
    }
}


