//
// Created by weimo on 2023/4/1.
//

#ifndef FGUM_NATIVE_LIB_H
#define FGUM_NATIVE_LIB_H

#include "frida-gumjs.h"

static void on_message( const gchar *message, GBytes *data, gpointer user_data);

int gumjsHook(jbyte* buffer);

#endif //FGUM_NATIVE_LIB_H
