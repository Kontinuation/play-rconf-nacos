# Play Remote Configuration - Nacos

Retrieves configuration from Nacos
*****

## About this project
In production, it is not always easy to manage the configuration files of a
Play Framework application, especially when it running on multiple servers.
The purpose of this project is to provide a simple way to use a remote
configuration with a Play Framework application.



## How to use

To enable this provider, just add the classpath `"io.playrconf.provider.NacosProvider"`
and the following configuration:

```hocon
remote-configuration {
  provider = "io.playrconf.provider.NacosProvider"

  ## Nacos
  # ~~~~~
  # Retrieves configuration from Nacos
  nacos {
    server-addr = "127.0.0.1:8848"
    server-addr = ${?REMOTECONF_NACOS_SERVER_ADDR}

    namespace = ""
    namespace = ${?REMOTECONF_NACOS_NS}

    group = ""
    group = ${?REMOTECONF_NACOS_GROUP}

    data-id = ""
    data-id = ${?REMOTECONF_NACOS_DATA_ID}
  }
}
```


## License
This project is released under terms of the [MIT license](./LICENSE).
