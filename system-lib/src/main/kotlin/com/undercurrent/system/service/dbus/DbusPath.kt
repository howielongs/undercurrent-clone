package com.undercurrent.system.service.dbus

import com.undercurrent.shared.messages.StringWrapper

inline class DbusPath(override val value: String) : StringWrapper
inline class DbusExtensionUnderscored(val value: String)
inline class DbusPathRoot(val value: String)
