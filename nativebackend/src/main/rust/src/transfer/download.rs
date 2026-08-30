use crate::java_api_binds::LambdaReturnIntJava;
use jni::{
    Env, EnvUnowned,
    objects::{JClass, JObjectArray, JString},
    sys::{jint, jlong},
};
use share_any_backend::web_server::{self, sessions::transfer::download::OpenFdCallback};

jni::bind_java_type! {
    DownloadItemJava => com.ammar.sharing.nativebackend.DownloadItem,
    type_map = {
        LambdaReturnIntJava => com.ammar.sharing.nativebackend.lambda.LambdaReturnInt,
    },
    constructors {
        fn new(name: JString, mime_type: JString, size: jlong),
    },
    fields {
        name: JString,
        size: jlong,
    }
}

jni::bind_java_type! {
    DownloadItemManagerJava => com.ammar.sharing.nativebackend.DownloadItemsManager,
    type_map = {
        LambdaReturnIntJava => com.ammar.sharing.nativebackend.lambda.LambdaReturnInt,
        DownloadItemJava => com.ammar.sharing.nativebackend.DownloadItem
    },
    native_methods {
        static fn add_new_download_item(name: JString, size: jlong, fd_getter: LambdaReturnIntJava) -> jint,
        static fn add_new_grouped_download_item(name: JString, total_size: jlong, children_names: JString[], children_fd_openers: LambdaReturnIntJava[]) -> jint,
        static raw fn remove_download_item(index: jint) -> jboolean,
        static fn get_download_item(index: jint) -> DownloadItemJava,
        static raw fn get_download_items_count() -> jint,
        static raw fn get_native_fd_of_download_item(index: jint) -> jint,
    }
}

impl DownloadItemManagerJavaNativeInterface for DownloadItemManagerJavaAPI {
    type Error = jni::errors::Error;

    fn add_new_download_item<'local>(
        env: &mut Env<'local>,
        _this: JClass<'local>,
        name: JString<'local>,
        size: jlong,
        fd_getter: LambdaReturnIntJava<'local>,
    ) -> Result<jint, Self::Error> {
        let fd_getter = env.new_global_ref(fd_getter)?;

        let java_vm = env.get_java_vm()?;
        let index = web_server::add_download_item(
            name.to_string(),
            size as u64,
            Box::new(move || {
                java_vm
                    .attach_current_thread(|env| -> jni::errors::Result<Option<i32>> {
                        let fd = fd_getter.get(env)?;
                        Ok(Some(fd))
                    })
                    .unwrap_or_else(|err| {
                        error!("Error while opening download item. Error: {err}");
                        None
                    })
            }),
        );
        Ok(index as jint)
    }

    fn add_new_grouped_download_item<'local>(
        env: &mut ::jni::Env<'local>,
        _: JClass<'local>,
        name: JString<'local>,
        total_size: jlong,
        children_names: JObjectArray<'local, JString<'local>>,
        children_fd_openers: JObjectArray<'local, LambdaReturnIntJava<'local>>,
    ) -> Result<jint, Self::Error> {
        let len = children_names.len(env)?;
        if len != children_fd_openers.len(env)? {
            return Err(Self::Error::JavaException);
        }

        let mut entries: Vec<(String, Box<OpenFdCallback>)> = Vec::with_capacity(len);

        for i in 0..len {
            let name = children_names.get_element(env, i)?.try_to_string(env)?;
            let java_callback = children_fd_openers.get_element(env, i)?;
            let java_callback = env.new_global_ref(java_callback)?;
            let java_vm = env.get_java_vm()?;

            let callback: Box<OpenFdCallback> = Box::new(move || {
                java_vm
                    .attach_current_thread(|env| -> jni::errors::Result<Option<i32>> {
                        let fd = java_callback.get(env)?;
                        Ok(Some(fd))
                    })
                    .unwrap_or_else(|err| {
                        error!("Error while opening download item. Error: {err}");
                        None
                    })
            });

            let child = (name, callback);
            entries.push(child);
        }
        let name = name.try_to_string(env)?;
        let pos = web_server::add_grouped_download_item(name, total_size as u64, entries);
        Ok(pos as jint)
    }

    fn remove_download_item<'local>(_: EnvUnowned<'local>, _: JClass<'local>, index: jint) -> bool {
        web_server::remove_download_item_by_index(index as usize)
    }

    fn get_download_item<'local>(
        env: &mut Env<'local>,
        _this: JClass<'local>,
        index: jint,
    ) -> Result<DownloadItemJava<'local>, Self::Error> {
        let item = web_server::get_download_item_by_index(index as usize);
        match item {
            Some(item) => {
                let name: JString<'_> = JString::from_str(env, item.name())?;
                let mime_type = JString::from_str(env, item.mime_type_string())?;
                DownloadItemJava::new(env, name, mime_type, item.size() as jlong)
            }
            None => Err(Self::Error::IndexOutOfBounds),
        }
    }

    fn get_download_items_count<'local>(_: EnvUnowned<'local>, _: JClass<'local>) -> jint {
        web_server::get_download_items_count() as jint
    }

    fn get_native_fd_of_download_item<'local>(
        _: EnvUnowned<'local>,
        _: JClass<'local>,
        index: jint,
    ) -> jint {
        let item = web_server::get_download_item_by_index(index as usize);
        if let Some(item) = item {
            unsafe { item.open_raw().unwrap_or(-1) }
        } else {
            -1
        }
    }
}
