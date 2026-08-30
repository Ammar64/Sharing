

jni::bind_java_type! {
    pub(crate) AndroidBundleJava => android.os.Bundle,
    constructors {
        fn new()
    },

    methods {
        fn put_char(key: JString, value: jchar),
        fn put_int(key: JString, value: jint)
    }
}

jni::bind_java_type! {
    pub(crate) FileDescriptorJava => java.io.FileDescriptor,
    fields {
        fd: jint
    }
}

jni::bind_java_type! {
    pub(crate) QueueMutableLiveDataJava => com.ammar.sharing.custom.data.QueueMutableLiveData,
    methods {
        fn force_post_value(value :JObject)
    }
}

jni::bind_java_type! {
    pub(crate) LiveDataSingletonsJava => com.ammar.sharing.common.LiveDataSingletons,
    type_map = {
        QueueMutableLiveDataJava => com.ammar.sharing.custom.data.QueueMutableLiveData,
    },
    fields {
        static users_list_observer: QueueMutableLiveDataJava,
    }
}

jni::bind_java_type! {
    pub(crate) LambdaReturnIntJava => com.ammar.sharing.nativebackend.lambda.LambdaReturnInt,
    methods {
        fn get() -> jint
    }
}

jni::bind_java_type! {
    pub(crate) FileJava => java.io.File,
    methods {
        fn get_path() -> JString,
    }

}

jni::bind_java_type! {
    pub(crate) EnvironmentJava => android.os.Environment,
    type_map = {
        FileJava => java.io.File,
    },
    methods {
        static fn get_external_storage_directory() -> FileJava,
    }
}