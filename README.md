# Undercurrent

Undercurrent is a fork of [signal-cli](...), a commandline interface for the [Signal messenger](https://signal.org/).

[//]: # ()
[//]: # (signal-cli is a commandline interface for the [Signal messenger]&#40;https://signal.org/&#41;.)

[//]: # (It supports registering, verifying, sending and receiving messages.)

[//]: # (signal-cli uses a [patched libsignal-service-java]&#40;https://github.com/Turasa/libsignal-service-java&#41;,)

[//]: # (extracted from the [Signal-Android source code]&#40;https://github.com/signalapp/Signal-Android/tree/main/libsignal/service&#41;.)

[//]: # (For registering you need a phone number where you can receive SMS or incoming calls.)

[//]: # ()
[//]: # (signal-cli is primarily intended to be used on servers to notify admins of important events. For this use-case, it has a daemon mode with D-BUS)

[//]: # (interface &#40;[man page]&#40;https://github.com/AsamK/signal-cli/blob/master/man/signal-cli-dbus.5.adoc&#41;&#41; and JSON-RPC interface &#40;[documentation]&#40;https://github.com/AsamK/signal-cli/wiki/JSON-RPC-service&#41;&#41;.)

[//]: # (For the JSON-RPC interface there's also a simple [example client]&#40;https://github.com/AsamK/signal-cli/tree/master/client&#41;, written in Rust.)

[//]: # ()
[//]: # (## Installation)

[//]: # ()
[//]: # (You can [build signal-cli]&#40;#building&#41; yourself or use)

[//]: # (the [provided binary files]&#40;https://github.com/AsamK/signal-cli/releases/latest&#41;, which should work on Linux, macOS and)

[//]: # (Windows. There's also a [docker image and some Linux packages]&#40;https://github.com/AsamK/signal-cli/wiki/Binary-distributions&#41; provided by the community.)

[//]: # ()
[//]: # (System requirements:)

[//]: # ()
[//]: # (- at least Java Runtime Environment &#40;JRE&#41; 17)

[//]: # (- native library: libsignal-client)

[//]: # ()
[//]: # (  The native libs are bundled for x86_64 Linux &#40;with recent enough glibc&#41;, Windows and MacOS. For other)

[//]: # (  systems/architectures)

[//]: # (  see: [Provide native lib for libsignal]&#40;https://github.com/AsamK/signal-cli/wiki/Provide-native-lib-for-libsignal&#41;)

[//]: # ()
[//]: # (### Install system-wide on Linux)

[//]: # ()
[//]: # (See [latest version]&#40;https://github.com/AsamK/signal-cli/releases&#41;.)

[//]: # ()
[//]: # (```sh)

[//]: # (export VERSION=<latest version, format "x.y.z">)

[//]: # (wget https://github.com/AsamK/signal-cli/releases/download/v"${VERSION}"/signal-cli-"${VERSION}"-Linux.tar.gz)

[//]: # (sudo tar xf signal-cli-"${VERSION}"-Linux.tar.gz -C /opt)

[//]: # (sudo ln -sf /opt/signal-cli-"${VERSION}"/bin/signal-cli /usr/local/bin/)

[//]: # (```)

[//]: # ()
[//]: # (You can find further instructions on the Wiki:)

[//]: # ()
[//]: # (- [Quickstart]&#40;https://github.com/AsamK/signal-cli/wiki/Quickstart&#41;)

[//]: # (- [DBus Service]&#40;https://github.com/AsamK/signal-cli/wiki/DBus-service&#41;)

[//]: # ()
[//]: # (## Usage)

[//]: # ()
[//]: # (For a complete usage overview please read)

[//]: # (the [man page]&#40;https://github.com/AsamK/signal-cli/blob/master/man/signal-cli.1.adoc&#41; and)

[//]: # (the [wiki]&#40;https://github.com/AsamK/signal-cli/wiki&#41;.)

[//]: # ()
[//]: # (Important: The ACCOUNT is your phone number in international format and must include the country calling code. Hence it)

[//]: # (should start with a "+" sign. &#40;See [Wikipedia]&#40;https://en.wikipedia.org/wiki/List_of_country_calling_codes&#41; for a list)

[//]: # (of all country codes.&#41;)

```sh
export VERSION=<latest version, format "x.y.z">
wget https://github.com/AsamK/signal-cli/releases/download/v"${VERSION}"/signal-cli-"${VERSION}".tar.gz
sudo tar xf signal-cli-"${VERSION}".tar.gz -C /opt
sudo ln -sf /opt/signal-cli-"${VERSION}"/bin/signal-cli /usr/local/bin/
```

[//]: # ()
[//]: # (      signal-cli -a ACCOUNT register)

[//]: # ()
[//]: # (  You can register Signal using a landline number. In this case you can skip SMS verification process and jump directly)

[//]: # (  to the voice call verification by adding the `--voice` switch at the end of above register command.)

[//]: # ()
[//]: # (  Registering may require solving a CAPTCHA)

[//]: # (  challenge: [Registration with captcha]&#40;https://github.com/AsamK/signal-cli/wiki/Registration-with-captcha&#41;)

[//]: # ()
[//]: # (* Verify the number using the code received via SMS or voice, optionally add `--pin PIN_CODE` if you've added a pin code)

[//]: # (  to your account)

[//]: # ()
[//]: # (      signal-cli -a ACCOUNT verify CODE)

[//]: # ()
[//]: # (* Send a message)

[//]: # ()
[//]: # (     ```sh)

[//]: # (     signal-cli -a ACCOUNT send -m "This is a message" RECIPIENT)

[//]: # (     ```)

[//]: # ()
[//]: # (* Pipe the message content from another process.)

[//]: # ()
[//]: # (      uname -a | signal-cli -a ACCOUNT send --message-from-stdin RECIPIENT)

[//]: # ()
[//]: # (* Receive messages)

[//]: # ()
[//]: # (      signal-cli -a ACCOUNT receive)

[//]: # ()
[//]: # (**Hint**: The Signal protocol expects that incoming messages are regularly received &#40;using `daemon` or `receive`)

[//]: # (command&#41;. This is required for the encryption to work efficiently and for getting updates to groups, expiration timer)

[//]: # (and other features.)

[//]: # ()
[//]: # (## Storage)

[//]: # ()
[//]: # (The password and cryptographic keys are created when registering and stored in the current users home directory:)

[//]: # ()
[//]: # (    $XDG_DATA_HOME/signal-cli/data/)

[//]: # (    $HOME/.local/share/signal-cli/data/)

[//]: # ()
[//]: # (## Building)

[//]: # ()
[//]: # (This project uses [Gradle]&#40;http://gradle.org&#41; for building and maintaining dependencies. If you have a recent gradle)

[//]: # (version installed, you can replace `./gradlew` with `gradle` in the following steps.)

[//]: # ()
[//]: # (1. Checkout the source somewhere on your filesystem with)

[//]: # ()
[//]: # (       git clone https://github.com/AsamK/signal-cli.git)

[//]: # ()
[//]: # (2. Execute Gradle:)

[//]: # ()
[//]: # (       ./gradlew build)

[//]: # ()
[//]: # (   2a. Create shell wrapper in *build/install/signal-cli/bin*:)

[//]: # ()
[//]: # (       ./gradlew installDist)

[//]: # ()
[//]: # (   2b. Create tar file in *build/distributions*:)

[//]: # ()
[//]: # (       ./gradlew distTar)

[//]: # ()
[//]: # (   2c. Create a fat tar file in *build/libs/signal-cli-fat*:)

[//]: # ()
[//]: # (       ./gradlew fatJar)

[//]: # ()
[//]: # (   2d. Compile and run signal-cli:)

[//]: # ()
[//]: # (      ```sh)

[//]: # (      ./gradlew run --args="--help")

[//]: # (      ```)

[//]: # ()
[//]: # (### Building a native binary with GraalVM &#40;EXPERIMENTAL&#41;)

[//]: # ()
[//]: # (It is possible to build a native binary with [GraalVM]&#40;https://www.graalvm.org&#41;. This is still experimental and will not)

[//]: # (work in all situations.)

[//]: # ()
[//]: # (1. [Install GraalVM and setup the enviroment]&#40;https://www.graalvm.org/docs/getting-started/#install-graalvm&#41;)

[//]: # (2. [Install prerequisites]&#40;https://www.graalvm.org/reference-manual/native-image/#prerequisites&#41;)

[//]: # (3. Execute Gradle:)

[//]: # ()
[//]: # (       ./gradlew nativeCompile)

[//]: # ()
[//]: # (   The binary is available at *build/native/nativeCompile/signal-cli*)

[//]: # ()
[//]: # (## FAQ and Troubleshooting)

[//]: # ()
[//]: # (For frequently asked questions and issues have a look at the [wiki]&#40;https://github.com/AsamK/signal-cli/wiki/FAQ&#41;.)

[//]: # ()
[//]: # (## License)

[//]: # ()
[//]: # (This project uses libsignal-service-java from Open Whisper Systems:)

[//]: # ()
[//]: # (https://github.com/WhisperSystems/libsignal-service-java)

[//]: # ()
[//]: # (Licensed under the GPLv3: http://www.gnu.org/licenses/gpl-3.0.html)
