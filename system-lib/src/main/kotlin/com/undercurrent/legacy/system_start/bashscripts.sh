# deployment functions

#args: version
function rundaemonsignal() {
        local package_name="signal-cli-$1"
        local dir_path="$HOME/deploy/$package_name/"
        echo $dir_path

        if [ -d "$dir_path" ]
        then
                echo "Directory exists"
        else
                echo "Directory does not exist"
                cd "$HOME/deploy/"
                unzip -o "$package_name.zip"
        fi

        cd "$dir_path"/bin
        ./signal-cli --trust-new-identities always daemon
        #./signal-cli --trust-new-identities always daemon > ~/logs/daemon17.5.0.txt
        #add logging output too
        #reuse this code instead of copy-paste for other roles


}

#args: version,env,role
function runsignal() {
        local package_name="signal-cli-$1"
        local dir_path="$HOME/deploy/$package_name/"
        echo $dir_path

        if [ -d "$dir_path" ]
        then
                echo "Directory exists"
        else
                echo "Directory does not exist"
                cd "$HOME/deploy/"
                unzip -o "$package_name.zip"
        fi

        cd "$dir_path"/bin
        ./signal-cli -e "$2" -r "$3"  --dbus send -m "Hey there" +18065895159
       #add logging output too
        #reuse this code instead of copy-paste for other roles


}

#add something to handle more version numbers
#args: version,env
function run-vendor() {
        runsignal $2 $1 "vendor"
}

#args: version,env
function run-customer() {
        runsignal $2 $1 "customer"
}

#args: version,env
function run-admin() {
        runsignal $2 $1 "admin"
}


#args: version
function unpack-signal() {
        local package_name="signal-cli-$1"
        local dir_path="$HOME/deploy/$package_name/"
        echo $dir_path

        if [ -d "$dir_path" ]
        then
                echo "Directory exists"
        else
                echo "Directory does not exist"
                cd "$HOME/deploy/"
                unzip -o "$package_name.zip"
        fi
}

#args: env (dev, qa, prod), version
function signal-logs() {
        touch "$HOME/logs/log_daemon_$2.txt"
        touch "$HOME/logs/log_$1_admin_$2.txt"
        touch "$HOME/logs/log_$1_vendor_$2.txt"
        touch "$HOME/logs/log_$1_customer_$2.txt"
        tail -F $HOME/logs/log_$1_*_$2.txt
}

#args: env (dev, qa, live, prod), version
function run-bot() {
        touch "$HOME/logs/log_$1_admin_$2.txt"
        touch "$HOME/logs/log_$1_vendor_$2.txt"
        touch "$HOME/logs/log_$1_customer_$2.txt"
        unpack-signal $2 $1
        (trap 'kill 0' SIGINT; run-admin $1 $2 >> ~/logs/log_"$1"_admin_"$2".txt & run-customer $1 $2 >> ~/logs/log_"$1"_customer_"$2".txt & run-vendor $1 $2 >> ~/logs/log_"$1"_vendor_"$2".txt & signal-logs $1 $2 )
        echo "ps -A | grep java"
        ps -A | grep java
}

#args: version
function run-daemon() {
        touch "$HOME/logs/log_daemon_$1.txt"
        unpack-signal $1
        (trap 'kill 0' SIGINT; rundaemonsignal $1 >> ~/logs/log_daemon_"$1".txt)
        echo "ps -A | grep java"
        ps -A | grep java
}