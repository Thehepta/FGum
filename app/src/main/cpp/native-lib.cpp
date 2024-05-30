#include <jni.h>
#include <string>

#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#include <mutex>
#include <thread>
#include <condition_variable>
#include <chrono>

#include "logging.h"
#include "native-lib.h"
#include "ThreadSafeQueue.h"

static pthread_mutex_t mtx = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t cond = PTHREAD_COND_INITIALIZER;
static GumScriptBackend *backend;
static GCancellable *cancellable = NULL;
static GError *error = NULL;
static GumScript *script;
static GMainContext *context;
static GMainLoop *loop;


ThreadSafeQueue threadSafeQueue;

static void on_message(const gchar *message, GBytes *data, gpointer user_data) {

    JsonParser *parser;
    JsonObject *root;
    const gchar *type;
    parser = json_parser_new();
    json_parser_load_from_data(parser, message, -1, NULL);
    root = json_node_get_object(json_parser_get_root(parser));

    type = json_object_get_string_member(root, "type.proto");
    if (strcmp(type, "log") == 0) {
        const gchar *log_message;
        log_message = json_object_get_string_member(root, "payload");
        threadSafeQueue.enqueue(log_message);
        LOGD ("[*] log : %s ", log_message);
    } else {
        threadSafeQueue.enqueue(message);
        LOGD ("[*] %s ", message);

    }
    g_object_unref(parser);

}


extern "C"
JNIEXPORT void JNICALL loadScript(JNIEnv *env, jclass thiz, jbyteArray js_buff) {
    // TODO: implement loadbuff()
    jbyte* buffer = env->GetByteArrayElements(js_buff, NULL);
    GError * err = NULL;

    if (script != NULL)
    {
        gum_script_unload_sync (script, NULL);
        g_object_unref (script);
        script = NULL;
    }

    script = gum_script_backend_create_sync (backend,
                                             "testcase", reinterpret_cast<const gchar *>(buffer), NULL, NULL, &err);
    if (err != NULL)
        g_printerr ("%s\n", err->message);
    g_assert_nonnull (script);
    g_assert_null (err);

    gum_script_set_message_handler (script,on_message, nullptr, NULL);
    gum_script_load_sync (script, NULL);
    env->ReleaseByteArrayElements(js_buff, buffer, 0);

}
#include <future>

extern "C" JNIEXPORT
void JNICALL frida_log(JNIEnv *env , jclass thiz) {

    jclass LoadEntry = env->FindClass("com/test/fgum/LoadEntry");
    jmethodID jsendlog = env->GetStaticMethodID(LoadEntry,"sendlog", "(Ljava/lang/String;)Z");
    std::string   msg;
    while (true){
        bool empty = threadSafeQueue.try_dequeue(msg);
        if(empty){
            jstring jmsg = env->NewStringUTF(msg.c_str());
            env->CallStaticBooleanMethod(LoadEntry,jsendlog,jmsg);
        } else{

        }
    }
}
extern "C" JNIEXPORT
void JNICALL frida_start(JNIEnv *env , jclass thiz) {
    LOGD ("[*] frida entry");
    gum_init_embedded();
    backend = gum_script_backend_obtain_qjs();

    context = g_main_context_get_thread_default();
    while (g_main_context_pending(context))
        g_main_context_iteration(context, FALSE);

    loop = g_main_loop_new(g_main_context_get_thread_default(), FALSE);
    g_main_loop_run(loop);//block here
    LOGE("frida end");    //会在前面个阻塞住，这个线程不会退出

}

int frida(jbyte* buffer) {
    LOGD ("[*] frida entry");
    gum_init_embedded();
    backend = gum_script_backend_obtain_qjs();

    context = g_main_context_get_thread_default();
    while (g_main_context_pending(context))
        g_main_context_iteration(context, FALSE);

    loop = g_main_loop_new(g_main_context_get_thread_default(), FALSE);
    g_main_loop_run(loop);//block here
    LOGE("frida end");    //会在前面个阻塞住，这个线程不会退出
    return 0;
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv( (void**) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    jclass LoadEntry = env->FindClass("com/test/fgum/LoadEntry");
    JNINativeMethod methods[]= {
            {"loadScript", "([B)V",(void*) loadScript},
            {"startWritingThread", "()V",(void*) frida_log},
            {"startFridaThread", "()V",(void*) frida_start},
    };
    env->RegisterNatives(LoadEntry, methods, sizeof(methods)/sizeof(JNINativeMethod));

//    pthread_t pthread_frida;
//    pthread_create(&pthread_frida, NULL, (void *(*)(void *)) (frida),(void *) nullptr);
//    pthread_detach(pthread_frida);


    return JNI_VERSION_1_6;
}