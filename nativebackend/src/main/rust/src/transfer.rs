pub(crate) mod download;

use std::{sync::atomic::Ordering::Relaxed, time::Duration};

use jni::{
    errors::Error::IndexOutOfBounds,
    objects::{JClass, JString},
    sys::jlong,
};
use share_any_backend::web_server::{self, TransferStatus};

jni::bind_java_type! {
    TransferOperationJava => com.ammar.sharing.nativebackend.TransferOperation,
    methods {
        static fn new_using_int_duration(
            item_name: JString,
            progress: jlong,
            total: jlong,
            speed: jdouble,
            avg_speed: jdouble,
            expected_remaining_time: jlong,
            status: jint,
            err: JString,
            time_to_complete: jlong,
            transfer_type: jint,
            user_name: JString
        ) -> TransferOperationJava
    }
}

jni::bind_java_type! {
    TransferOperationsManagerJava => com.ammar.sharing.nativebackend.TransferOperationsManager,
    type_map = {
        TransferOperationJava => com.ammar.sharing.nativebackend.TransferOperation,
    },
    native_methods {
        static raw fn get_operations_count() -> jint,
        static fn get_transfer_operation_at_index(index: jint) -> TransferOperationJava,
        static raw fn remove_or_cancel_transfer_operation_at_index(index: jint)
    }
}

impl TransferOperationsManagerJavaNativeInterface for TransferOperationsManagerJavaAPI {
    type Error = jni::errors::Error;

    fn get_operations_count<'local>(
        _: ::jni::EnvUnowned<'local>,
        _: JClass<'local>,
    ) -> ::jni::sys::jint {
        web_server::get_transfer_operations_count() as i32
    }

    fn get_transfer_operation_at_index<'local>(
        env: &mut ::jni::Env<'local>,
        _: JClass<'local>,
        index: ::jni::sys::jint,
    ) -> ::std::result::Result<TransferOperationJava<'local>, Self::Error> {
        let op = web_server::get_transfer_operation_at_index(index as usize);
        if let Some(op) = op {
            let op_read = op.blocking_read();
            let transfer_data = op_read.transfer_data();
            let item_name = JString::from_str(env, &transfer_data.item_name)?;
            let progress: jlong = match transfer_data.progress.load(Relaxed).try_into() {
                Ok(progress) => progress,
                Err(_) => return Err(jni::errors::Error::JavaException),
            };
            let total = match transfer_data.total.try_into() {
                Ok(total) => total,
                Err(_) => return Err(jni::errors::Error::JavaException),
            };
            let speed = transfer_data.speed.bytes_per_second();
            let avg_speed = transfer_data.avg_speed.bytes_per_second();

            let expected_remaining_time = match transfer_data
                .expected_remaining_time
                .unwrap_or(Duration::new(0, 0))
                .as_secs()
                .try_into()
            {
                Ok(total) => total,
                Err(_) => return Err(jni::errors::Error::JavaException),
            };

            let mut error_message = None::<Box<str>>;
            let status = match &transfer_data.status {
                TransferStatus::InProgress => 0,
                TransferStatus::Completed => 1,
                TransferStatus::CancelledByUser => 2,
                TransferStatus::Failed(err) => {
                    error_message = Some(err.clone());
                    3
                }
            };

            let j_error_message = match error_message {
                Some(m) => JString::from_str(env, m)?,
                None => JString::null(),
            };

            let time_to_complete = match transfer_data.time_to_complete {
                Some(t) => t.as_secs() as i64,
                None => -1i64,
            };

            let transfer_type = transfer_data.transfer_type;
            let user_name = JString::from_str(env, transfer_data.user.name())?;

            return TransferOperationJava::new_using_int_duration(
                env,
                item_name,
                progress,
                total,
                speed,
                avg_speed,
                expected_remaining_time,
                status,
                j_error_message,
                time_to_complete,
                transfer_type,
                user_name,
            );
        } else {
            return Err(IndexOutOfBounds);
        }
    }

    fn remove_or_cancel_transfer_operation_at_index<'local>(
        _: ::jni::EnvUnowned<'local>,
        _: JClass<'local>,
        index: ::jni::sys::jint,
    ) {
        web_server::cancel_transfer_op_and_remove_it_from_list_if_completed(index as usize);
    }
}
