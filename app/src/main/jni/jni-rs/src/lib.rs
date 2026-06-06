use jni::EnvUnowned;
use jni::Env;
use jni::objects::{JClass, JString};
use jni::strings::JNIString;

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_ammar_sharing_common_utils_Utils_hello<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    class: JClass<'caller>,
    input: JString<'caller>)
    -> JString<'caller>
{
    let outcome = unowned_env.with_env(|env| -> Result<_, jni::errors::Error> {
        let input: String = input.to_string();
        JString::from_str(env, format!("Hello, {}!, {:?}", input, nth_prime(100)))
    });

    outcome.resolve::<jni::errors::ThrowRuntimeExAndDefault>()
}

fn nth_prime(n: usize) -> Option<u64> {
    if n == 0 {
        return None;
    }

    let mut count = 0;
    let mut candidate = 2;

    loop {
        if is_prime(candidate) {
            count += 1;

            if count == n {
                return Some(candidate);
            }
        }

        candidate += 1;
    }
}

fn is_prime(num: u64) -> bool {
    if num < 2 {
        return false;
    }

    if num == 2 {
        return true;
    }

    if num % 2 == 0 {
        return false;
    }

    let limit = (num as f64).sqrt() as u64;

    for i in (3..=limit).step_by(2) {
        if num % i == 0 {
            return false;
        }
    }

    true
}
