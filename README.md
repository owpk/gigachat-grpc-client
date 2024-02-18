# Gigachat-CLI Project
The `Gigachat-CLI` project is a command-line `gRPC` client for interacting with the [Gigachat](https://developers.sber.ru/docs/ru/gigachat/api/reference/rest/gigachat-api) service. Written in java using `micronaut` framework.
## Installing
- To use `GigaChat CLI`, download the [latest version](https://github.com/owpk/gigachat-grpc-client/releases/latest)  
- For manual installation, see section ["Build"](#build2)

## Configuration

You need to register account and retrieve `Client ID` and `Client Secret` encoded in Base 64 string.
Specify encoded credentials in the `gigachat.composedCredentials` property of the configuration file. Create or change a configuration using the `config -d <your credentials hash>` command.

### Please look for the gigachat api documentation for more information

[official GigaChat API documentation](https://developers.sber.ru/docs/ru/gigachat/api/reference/rest/post-token).

## Usage 

### Base command

```shell
gigachat
```
The main command for interacting with GigaChat. Use `-h` or `--help` for more information.

### Chat

```shell
gigachat chat <your query>
```
Initiates a chat with GigaChat. Use `-u` or `--unary` to get a single response. The default response type is stream.

### Config

```shell
gigachat config -s
```
Shows the current configuration settings. Use `-c` or `--create` to create a default configuration, `-f` or `--force` to force the creation/overwrite of a default configuration, `-d` or `--credentials` to set the credentials property.

### Model

```shell
gigachat model
```
Getting a list of available chat models

---

# Build native image
To build a project using GraalVM native image, you will need to install GraalVM and configure it for your project. Here are the general steps:

#### Install GraalVM: 
Download and install GraalVM from the official website: [GraalVM Downloads](https://www.graalvm.org/downloads/).
Install native-image: After installing GraalVM, make sure you have native-image installed. If not, run the following command at the command prompt:

```shell
gu install native-image
```

#### <a name="build2"></a> Build the project: 
Then run the command to build the native image of your project. 
```shell
./gradlew nativeBuild --info
```

#### Running native image: 
After a successful build, you can run your application compiled in native image.
```shell
cd build/native/nativImage
./gigachat -h
```
