#include "frida-gumjs.h"
#include <fcntl.h>
#include <glib.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

static void on_message(const gchar* message, GBytes* data, gpointer user_data);

static gchar* read_file(const gchar* filepath) {
    gchar* content = NULL;
    GError* error = NULL;

    if (!g_file_get_contents(filepath, &content, NULL, &error)) {
        g_printerr("Error reading file: %s\n", error->message);
        g_error_free(error);
        return NULL;
    }

    return content;
}

static void load_script(const gchar* script_path, GumScriptBackend* backend, GumScript** script, GCancellable* cancellable) {
    gchar* script_content = read_file(script_path);
    if (script_content == NULL) {
        g_printerr("Failed to read script file: %s\n", script_path);
        return;
    }

    GError* error = NULL;
    if (*script != NULL) {
        gum_script_unload_sync(*script, cancellable);
        g_object_unref(*script);
    }

    *script = gum_script_backend_create_sync(backend, "example", script_content, NULL, cancellable, &error);
    if (error != NULL) {
        g_printerr("Error creating script: %s\n", error->message);
        g_error_free(error);
        g_free(script_content);
        return;
    }

    gum_script_set_message_handler(*script, on_message, NULL, NULL);
    gum_script_load_sync(*script, cancellable);

    g_free(script_content);
}

int main(int argc, char* argv[]) {
    if (argc != 2) {
        g_printerr("Usage: %s <script.js>\n", argv[0]);
        return 1;
    }

    const gchar* script_path = argv[1];
    GumScriptBackend* backend;
    GCancellable* cancellable = NULL;
    GumScript* script = NULL;
    GMainContext* context;

    gum_init_embedded();

    backend = gum_script_backend_obtain_qjs();
    load_script(script_path, backend, &script, cancellable);

    // Example calls to trigger hooks
    close(open("/etc/hosts", O_RDONLY));
    close(open("/etc/fstab", O_RDONLY));

    context = g_main_context_get_thread_default();
    gboolean quit = FALSE;
    while (!quit) {
        while (g_main_context_pending(context)) {
            g_main_context_iteration(context, FALSE);
        }

        // Check for user input to reload the script
        g_print("Press 'r' to reload script or 'q' to quit: ");
        int ch = getchar();
        if (ch == 'r') {
            load_script(script_path, backend, &script, cancellable);
        } else if (ch == 'q') {
            quit = TRUE;
        }
        // Consume the newline character left in the input buffer
        while ((ch = getchar()) != '\n' && ch != EOF);
    }

    if (script != NULL) {
        gum_script_unload_sync(script, cancellable);
        g_object_unref(script);
    }

    gum_deinit_embedded();

    return 0;
}

static void on_message(const gchar* message, GBytes* data, gpointer user_data) {
    JsonParser* parser;
    JsonObject* root;
    const gchar* type;

    parser = json_parser_new();
    json_parser_load_from_data(parser, message, -1, NULL);
    root = json_node_get_object(json_parser_get_root(parser));

    type = json_object_get_string_member(root, "type.proto");
    if (strcmp(type, "log") == 0) {
        const gchar* log_message;
        log_message = json_object_get_string_member(root, "payload");
        g_print("%s\n", log_message);
    } else {
        g_print("on_message: %s\n", message);
    }

    g_object_unref(parser);
}
