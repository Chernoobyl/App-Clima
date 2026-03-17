# WeatherApp - Clima Atual com Kotlin + OpenWeather

Aplicativo Android desenvolvido em **Kotlin** que exibe o **clima atual com base na geolocalização do usuário** usando a API do [OpenWeather](https://openweathermap.org/).

## Funcionalidades
- Localização automática via GPS
- Busca de clima por **cidade/estado**
- Exibição da temperatura atual
- Nome da cidade + país
- Descrição do clima
- Ícone oficial do OpenWeather conforme condição atual
- Animações (Lottie) dinâmicas conforme clima
- Informações extras: umidade e vento
- Interface simples com Material Design

## Ícone de instalação (launcher)
O app já está configurado com ícone de instalação no AndroidManifest:
- `android:icon="@mipmap/ic_launcher"`
- `android:roundIcon="@mipmap/ic_launcher_round"`

Arquivos de ícone incluídos para densidades principais:
- `mipmap-mdpi`, `mipmap-hdpi`, `mipmap-xhdpi`, `mipmap-xxhdpi`, `mipmap-xxxhdpi`

## Tecnologias Utilizadas
- Kotlin (Android)
- ViewBinding
- Retrofit + Gson
- Google FusedLocationProvider
- OpenWeather API
- Lottie
- Glide

## Chave da API
A chave usada no app foi mantida conforme solicitado e está centralizada em:
- `app/src/main/java/com/example/weatherapp/config/ApiConfig.kt`

## Requisitos para build local
- Android Studio (recomendado)
- **JDK 17 ou JDK 21**
- Conexão com a internet para baixar dependências Gradle e consultar a API

## Como Executar
1. Clone este repositório:
```bash
git clone https://github.com/Chernoobyl/App-Clima.git
```

2. Abra no Android Studio e sincronize o Gradle.

3. Compile e execute no emulador/dispositivo (mínimo SDK 21).

### Build por linha de comando (exemplo com JDK 21)
```bash
JAVA_HOME=/caminho/para/jdk-21 ./gradlew assembleDebug
```

## Licença
MIT. Sinta-se livre para usar e modificar.

## Autor
Feito por Rafael Camargo


## Troubleshooting (build bloqueado)
Se o `./gradlew assembleDebug` falhar com **403 Forbidden** ao baixar dependências (`dl.google.com`/`repo.maven.apache.org`), o problema não está no código do app: é bloqueio de rede/proxy do ambiente.

Checklist para garantir 100% funcionando localmente:
1. Usar **JDK 17 ou 21**.
2. Garantir acesso HTTP/HTTPS aos repositórios Maven/Google.
3. Executar:
```bash
JAVA_HOME=/caminho/para/jdk-21 ./gradlew --refresh-dependencies assembleDebug
```
4. Rodar no Android Studio em emulador/dispositivo com internet e permissão de localização.
